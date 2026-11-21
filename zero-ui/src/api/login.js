/**
 * 登录、退出与登录前验证的请求封装；复用公共 request 的认证和错误处理，由页面负责交互及加载状态。
 */
import request from '@/utils/request'

// 登录方法
export function login(username, password, code, uuid, googleCode) {
  const data = {
    username,
    password,
    code,
    uuid,
    googleCode
  }
  return request({
    url: '/login',
    headers: {
      isToken: false,
      repeatSubmit: false
    },
    method: 'post',
    data: data
  })
}

// 获取用户详细信息
export function getInfo() {
  return request({
    url: '/getInfo',
    method: 'get'
  })
}

// 退出方法
export function logout(token) {
  return request({
    url: '/logout',
    method: 'post',
    // 固定本次退出的身份，异步请求期间其它标签页重新登录时不能退出新会话。
    headers: token ? { Authorization: 'Bearer ' + token, isToken: false, repeatSubmit: false } : undefined
  })
}

// 获取验证码
export function getCodeImg() {
  return request({
    url: '/captchaImage',
    headers: {
      isToken: false
    },
    method: 'get',
    timeout: 20000
  })
}

// 绑定谷歌动态码
export function bindGoogle(data) {
  return request({
    url: '/google/bind',
    headers: {
      isToken: false
    },
    method: 'post',
    data: data
  })
}
