import axios from 'axios'
import { ElNotification as Notification, ElMessageBox as MessageBox, ElMessage as Message, ElLoading as Loading } from 'element-plus'
import store from '@/store'
import { getToken } from '@/utils/auth'
import errorCode from '@/utils/errorCode'
import { tansParams, blobValidate } from '@/utils/zero'
import cache from '@/plugins/cache'
import { saveAs } from 'file-saver'

export let isRelogin = { show: false }

const googleAuthApisKey = 'google_auth_apis'
const googleCodeHeader = 'X-Google-Code'
const googleBindRequiredCode = -1001
const googleAuthErrorCodes = [googleBindRequiredCode, -1002, -1003]
const googleCodeMaxLength = 6
const googleCodePattern = /^\d{6}$/

axios.defaults.headers['Content-Type'] = 'application/json;charset=utf-8'

const service = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_API,
  timeout: 10000
})

service.interceptors.request.use(async config => {
  const isToken = (config.headers || {}).isToken === false
  const isRepeatSubmit = (config.headers || {}).repeatSubmit === false
  if (getToken() && !isToken) {
    config.headers['Authorization'] = 'Bearer ' + getToken()
  }

  config = await handleApiGoogleAuth(config, isToken)

  if (config.method === 'get' && config.params) {
    let url = config.url + '?' + tansParams(config.params)
    url = url.slice(0, -1)
    config.params = {}
    config.url = url
  }

  if (!isRepeatSubmit && (config.method === 'post' || config.method === 'put')) {
    const requestObj = {
      url: config.url,
      data: typeof config.data === 'object' ? JSON.stringify(config.data) : config.data,
      time: new Date().getTime()
    }
    const requestSize = Object.keys(JSON.stringify(requestObj)).length
    const limitSize = 5 * 1024 * 1024
    if (requestSize >= limitSize) {
      console.warn(`[${config.url}]: 请求数据大小超出允许的5M限制，无法进行防重复提交验证。`)
      return config
    }
    const sessionObj = cache.session.getJSON('sessionObj')
    if (sessionObj === undefined || sessionObj === null || sessionObj === '') {
      cache.session.setJSON('sessionObj', requestObj)
    } else {
      const s_url = sessionObj.url
      const s_data = sessionObj.data
      const s_time = sessionObj.time
      const interval = 1000
      if (s_data === requestObj.data && requestObj.time - s_time < interval && s_url === requestObj.url) {
        const message = '数据正在处理，请勿重复提交'
        console.warn(`[${s_url}]: ` + message)
        return Promise.reject(new Error(message))
      } else {
        cache.session.setJSON('sessionObj', requestObj)
      }
    }
  }
  return config
}, error => {
  return Promise.reject(error)
})

service.interceptors.response.use(res => {
  const code = res.data.code || 200
  const msg = errorCode[code] || res.data.msg || errorCode['default']
  if (res.request.responseType === 'blob' || res.request.responseType === 'arraybuffer') {
    return res.data
  }
  if (code === 401) {
    if (!isRelogin.show) {
      isRelogin.show = true
      MessageBox.confirm('登录状态已过期，您可以继续留在该页面，或者重新登录', '系统提示', {
        confirmButtonText: '重新登录',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        isRelogin.show = false
        store.dispatch('LogOut').then(() => {
          location.href = '/index'
        })
      }).catch(() => {
        isRelogin.show = false
      })
    }
    return Promise.reject('无效的会话，或者会话已过期，请重新登录。')
  } else if (code === 500) {
    Message({ message: msg, type: 'error' })
    return Promise.reject(new Error(msg))
  } else if (code === 601) {
    Message({ message: msg, type: 'warning' })
    return Promise.reject('error')
  } else if (googleAuthErrorCodes.includes(code)) {
    if (!(code === googleBindRequiredCode && normalizeApiPath(res.config && res.config.url) === '/login')) {
      Message({ message: msg, type: 'error' })
    }
    return Promise.reject(res.data)
  } else if (code !== 200) {
    Notification.error({ title: msg })
    return Promise.reject('error')
  } else {
    return res.data
  }
}, error => {
  // 用户取消安全验证时终止调用，不改写成网络错误或暴露请求配置。
  if (error === 'cancel' || error === 'close') return Promise.reject(error)
  let message = error?.message || '请求失败，请重试'
  if (message === 'Network Error') {
    message = '后端接口连接异常,请检查网络连接!'
  } else if (message.includes('timeout')) {
    message = '系统接口请求超时,请联系管理员!'
  } else if (message.includes('Request failed with status code')) {
    // message = '系统接口' + message.substr(message.length - 3) + '异常'
    message = '系统接口异常,请联系管理员,状态码:' + message.substr(message.length - 3);
  }
  Message({ message: message, type: 'error', duration: 5 * 1000 })
  return Promise.reject(error)
})

export function download(url, params, filename, config) {
  const downloadLoadingInstance = Loading.service({
    text: '正在下载数据，请稍候',
    spinner: 'el-icon-loading',
    background: 'rgba(0, 0, 0, 0.7)'
  })
  return service.post(url, params, {
    transformRequest: [(params) => {
      return tansParams(params)
    }],
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    responseType: 'blob',
    ...config
  }).then(async (data) => {
    const isBlob = blobValidate(data)
    if (isBlob) {
      const blob = new Blob([data])
      saveAs(blob, filename)
    } else {
      const resText = await data.text()
      const rspObj = JSON.parse(resText)
      const errMsg = errorCode[rspObj.code] || rspObj.msg || errorCode['default']
      Message.error(errMsg)
    }
    downloadLoadingInstance.close()
  }).catch((error) => {
    // 验证取消不提示下载故障，也不输出含凭据的 Axios 请求配置。
    if (error !== 'cancel' && error !== 'close') Message.error('下载文件出现错误，请联系管理员！')
    downloadLoadingInstance.close()
  })
}

async function handleApiGoogleAuth(config, isToken) {
  if (!getToken() || isToken) {
    return config
  }
  const apiPath = normalizeApiPath(config.url)
  if (!apiPath || apiPath === '/system/apiAuth/googleAuthApis') {
    return config
  }
  const apiMethod = normalizeApiMethod(config.method)
  const authApis = getGoogleAuthApisCache()
  if (!authApis.some(item => isGoogleAuthApiMatch(item, apiMethod, apiPath))) {
    return config
  }
  const { value } = await promptApiGoogleCode()
  config.headers = config.headers || {}
  config.headers[googleCodeHeader] = value
  return config
}

function promptApiGoogleCode() {
  const promptPromise = MessageBox.prompt('请输入6位谷歌验证码', '安全验证', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputType: 'tel',
    inputPlaceholder: '请输入6位谷歌验证码',
    inputPattern: googleCodePattern,
    inputErrorMessage: '谷歌验证码必须为6位数字',
    closeOnClickModal: false,
    closeOnPressEscape: false
  })
  limitGoogleCodePromptInput()
  return promptPromise
}

function limitGoogleCodePromptInput(retryCount = 0) {
  const input = document.querySelector('.el-message-box__input input')
  if (!input) {
    if (retryCount < 10) {
      setTimeout(() => limitGoogleCodePromptInput(retryCount + 1), 16)
    }
    return
  }
  if (input.dataset.googleCodeLimited === 'true') {
    return
  }
  input.dataset.googleCodeLimited = 'true'
  input.setAttribute('maxlength', googleCodeMaxLength)
  input.setAttribute('inputmode', 'numeric')
  input.setAttribute('autocomplete', 'one-time-code')
  const normalizeGoogleCode = () => {
    const value = input.value.replace(/\D/g, '').slice(0, googleCodeMaxLength)
    if (input.value !== value) {
      input.value = value
      input.dispatchEvent(new Event('input', { bubbles: true }))
    }
  }
  input.addEventListener('input', normalizeGoogleCode)
  input.addEventListener('paste', () => {
    setTimeout(normalizeGoogleCode, 0)
  })
  normalizeGoogleCode()
}

function getGoogleAuthApisCache() {
  const cacheValue = localStorage.getItem(googleAuthApisKey)
  if (!cacheValue) {
    return []
  }
  try {
    const authApis = JSON.parse(cacheValue)
    return Array.isArray(authApis) ? authApis : []
  } catch (e) {
    localStorage.removeItem(googleAuthApisKey)
    return []
  }
}

function isGoogleAuthApiMatch(item, apiMethod, apiPath) {
  if (!item) {
    return false
  }
  if (typeof item === 'string') {
    return isPathPatternMatch(normalizeApiPath(item), apiPath)
  }
  const ruleMethod = normalizeApiMethod(item.apiMethod || 'ALL')
  if (ruleMethod !== 'ALL' && ruleMethod !== apiMethod) {
    return false
  }
  return isPathPatternMatch(normalizeApiPath(item.apiUrl), apiPath)
}

function normalizeApiMethod(method) {
  return (method || 'GET').toString().trim().toUpperCase()
}

function isPathPatternMatch(pattern, apiPath) {
  if (!pattern || !apiPath) {
    return false
  }
  if (pattern === apiPath) {
    return true
  }
  const source = pattern
    .replace(/\{[^/]+?\}/g, '__PATH_PARAM__')
    .replace(/\*\*/g, '__DOUBLE_STAR__')
    .replace(/\*/g, '__SINGLE_STAR__')
  const regex = '^' + escapeRegExp(source)
    .replace(/__PATH_PARAM__/g, '[^/]+')
    .replace(/__DOUBLE_STAR__/g, '.*')
    .replace(/__SINGLE_STAR__/g, '[^/]*') + '$'
  return new RegExp(regex).test(apiPath)
}

function escapeRegExp(value) {
  return value.replace(/[.+^${}()|[\]\\]/g, '\\$&')
}

function normalizeApiPath(url) {
  if (!url) {
    return ''
  }
  let apiPath = url.split('?')[0]
  if (/^https?:\/\//i.test(apiPath)) {
    try {
      apiPath = new URL(apiPath).pathname
    } catch (e) {
      return ''
    }
  }
  const baseApi = import.meta.env.VITE_APP_BASE_API
  if (baseApi && apiPath.startsWith(baseApi)) {
    apiPath = apiPath.substring(baseApi.length)
  }
  if (!apiPath) {
    return '/'
  }
  return apiPath.startsWith('/') ? apiPath : '/' + apiPath
}

export default service
