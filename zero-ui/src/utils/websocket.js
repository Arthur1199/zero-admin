/**
 * 每个浏览器标签页的在线通信实例：首帧认证后才分发业务消息，并管理心跳、重连、身份切换和退出清理。
 */
import { getToken } from '@/utils/auth'
import { createWebSocketPresenter } from '@/utils/websocketPresenter'
import { WebSocketMessageType as Info, isWebSocketBusinessType } from '@/utils/websocketMessageType'

let messageSequence = 0

// 沿用HTTP API的域名和前缀，凭证禁止出现在URL中。
export function buildWebSocketUrl(baseApi, pageUrl = window.location.href) {
  const page = new URL(pageUrl)
  const api = new URL(baseApi || '/', page.origin + '/')
  if (!['http:', 'https:'].includes(api.protocol) || api.username || api.password || api.search || api.hash) {
    throw new Error('WebSocket的API地址必须是有效的HTTP或HTTPS地址，不能包含凭证、查询参数或片段')
  }
  if (page.protocol === 'https:' && api.protocol !== 'https:') {
    throw new Error('HTTPS页面必须使用HTTPS API以建立安全WebSocket连接')
  }
  api.pathname = api.pathname.replace(/\/+$/, '') + '/ws/admin'
  api.protocol = api.protocol === 'https:' ? 'wss:' : 'ws:'
  return api.href
}

// 每个浏览器标签页一个实例；依赖可注入，便于验证断线、并发初始化和退出边界。
export function createWebSocketClient({
  readToken = getToken,
  baseApi = import.meta.env.VITE_APP_BASE_API,
  pageUrl = () => window.location.href,
  createSocket = url => new WebSocket(url),
  presenter,
  random = Math.random
} = {}) {
  let socket
  let running = false
  let authenticated = false
  let identity
  let failedIdentity
  let generation = 0
  let attempts = 0
  let retryTimer
  let openingTimer
  let heartbeatTimer
  let lastReceived = 0
  let authFailure
  let refreshHandler
  let refreshRequested = false
  let connectionError
  let pauseCount = 0
  let resumeAfterPause = false
  const listeners = new Map()

  function clearTimers() {
    clearTimeout(retryTimer)
    clearTimeout(openingTimer)
    clearInterval(heartbeatTimer)
    retryTimer = openingTimer = heartbeatTimer = undefined
  }

  function discardSocket() {
    const previous = socket
    socket = undefined
    authenticated = false
    if (previous) {
      previous.onopen = previous.onmessage = previous.onerror = previous.onclose = null
      try { previous.close() } catch { /* 已关闭连接无需再次处理 */ }
    }
  }

  function frame(messageType, msgData = {}) {
    return JSON.stringify({ id: `ui-${Date.now()}-${++messageSequence}`, sendTime: Date.now(), msgType: { messageType }, msgData, forwardUrl: '' })
  }

  function reportError(message) {
    // 只提供固定提示，不转发浏览器事件、URL或原始报文。
    try { connectionError?.(message) } catch { /* 错误观察者不能破坏连接清理 */ }
  }

  function expire() {
    if (!running) return
    const failedToken = identity
    // Cookie已被其它标签页换成新身份时，旧连接失败不能清除新登录态。
    if (readToken() && readToken() !== failedToken) {
      clearTimers()
      discardSocket()
      presenter?.clear()
      identity = readToken()
      refreshRequested = false
      attempts = 0
      connect()
      return
    }
    presenter?.clear()
    running = false
    failedIdentity = failedToken
    identity = undefined
    ++generation
    clearTimers()
    discardSocket()
    Promise.resolve().then(() => {
      // 主动退出的pause不能取消强退；stop或新身份启动仍会清除失效标记，阻止旧回调。
      if (!failedToken || failedIdentity !== failedToken || running || (readToken() && readToken() !== failedToken)) return
      return authFailure?.(failedToken)
    }).catch(() => reportError('登录状态清理失败'))
  }

  function retry() {
    if (!running || retryTimer) return
    clearTimers()
    discardSocket()
    const delay = Math.min(30000, 1000 * 2 ** Math.min(attempts++, 5))
    retryTimer = setTimeout(() => {
      retryTimer = undefined
      connect()
    }, Math.min(30000, delay + Math.floor(random() * delay * 0.2)))
  }

  function connect() {
    if (!running) return
    const token = readToken()
    if (!token) { expire(); return }
    if (identity && identity !== token) {
      presenter?.clear()
      refreshRequested = false
    }
    identity = token
    const currentGeneration = ++generation
    let current
    try {
      current = createSocket(buildWebSocketUrl(baseApi, pageUrl()))
      socket = current
    } catch {
      // 配置或浏览器不支持时停止尝试，不影响HTTP页面初始化。
      running = false
      reportError('WebSocket地址配置错误或浏览器不支持通信，请检查API配置')
      return
    }
    const isCurrent = () => running && socket === current && generation === currentGeneration
    openingTimer = setTimeout(() => { if (isCurrent()) retry() }, 10000)
    current.onopen = () => {
      if (!isCurrent()) return
      if (readToken() !== token) { retry(); return }
      try { current.send(JSON.stringify({ token })) } catch { retry() }
    }
    current.onmessage = event => {
      if (!isCurrent()) return
      if (readToken() !== token) { retry(); return }
      let message
      try { message = JSON.parse(event.data) } catch { retry(); return }
      const messageType = message?.msgType?.messageType
      if (!Number.isInteger(messageType)) { retry(); return }
      if (messageType === Info.CREATE_CONNECTION && !authenticated) {
        const interval = message.msgData?.heartbeatInterval
        const idle = message.msgData?.idleTimeout
        if (!Number.isFinite(interval) || interval < 1000 || !Number.isFinite(idle) || idle <= interval) {
          retry()
          return
        }
        clearTimeout(openingTimer)
        openingTimer = undefined
        authenticated = true
        lastReceived = Date.now()
        heartbeatTimer = setInterval(() => {
          if (!isCurrent()) return
          if (!readToken()) { expire(); return }
          if (readToken() !== token || Date.now() - lastReceived >= idle) { retry(); return }
          try { current.send(frame(Info.PING_PONG)) } catch { retry() }
        }, interval)
        return
      }
      if (!authenticated) { retry(); return }
      lastReceived = Date.now()
      // 认证成功后立即断开仍视为不稳定，完成一次心跳往返才恢复最短重连间隔。
      if (messageType === Info.PING_PONG) attempts = 0
      if (messageType === Info.LOGIN_OUT) {
        // 与4401共用幂等退出流程，先关闭连接和定时器，再调用退出接口。
        expire()
        return
      }
      if (messageType === Info.REFRESH_BROWSER && refreshHandler) {
        // 同一页面只触发一次全局刷新，不重复弹窗；退出或旧身份回调已由isCurrent拦截。
        if (!refreshRequested) {
          refreshRequested = true
          try {
            Promise.resolve(refreshHandler()).catch(() => {
              if (isCurrent()) {
                refreshRequested = false
                reportError('WebSocket页面刷新失败')
              }
            })
          } catch {
            refreshRequested = false
            reportError('WebSocket页面刷新失败')
          }
        }
        return
      }
      if (isWebSocketBusinessType(messageType)) {
        try { presenter?.show(message) } catch { reportError('WebSocket消息展示失败') }
        const callbacks = listeners.get(messageType)
        callbacks?.forEach(callback => {
          if (!isCurrent() || readToken() !== token) return
          try {
            Promise.resolve(callback(message.msgData, message)).catch(() => {
              if (isCurrent()) reportError('WebSocket业务订阅处理失败')
            })
          } catch { reportError('WebSocket业务订阅处理失败') }
        })
      }
    }
    current.onclose = event => {
      if (!isCurrent()) return
      if (event.code === 4401) expire()
      else retry()
    }
    current.onerror = () => { if (isCurrent()) retry() }
  }

  function start({ onAuthFailure, onError, onRefresh } = {}) {
    if (onAuthFailure !== undefined) authFailure = onAuthFailure
    if (onError !== undefined) connectionError = onError
    if (onRefresh !== undefined) refreshHandler = onRefresh
    if (pauseCount) return
    const token = readToken()
    if (!token || failedIdentity === token || (running && identity === token)) return
    presenter?.clear()
    clearTimers()
    discardSocket()
    failedIdentity = undefined
    refreshRequested = false
    running = true
    attempts = 0
    connect()
  }

  return {
    start,
    // 退出HTTP请求期间暂停连接，保留订阅；并发退出请求全部失败后才能恢复。
    pause() {
      presenter?.clear()
      if (!pauseCount) resumeAfterPause = running
      pauseCount++
      running = false
      generation++
      clearTimers()
      discardSocket()
    },
    resume() {
      if (!pauseCount) return
      pauseCount--
      if (!pauseCount && resumeAfterPause) start()
    },
    stop() {
      presenter?.clear()
      running = false
      authenticated = false
      identity = failedIdentity = undefined
      generation++
      pauseCount = 0
      resumeAfterPause = false
      clearTimers()
      discardSocket()
      listeners.clear()
      authFailure = connectionError = refreshHandler = undefined
      refreshRequested = false
    },
    // 返回解除订阅函数，Options API页面在beforeUnmount中调用。
    subscribe(event, callback) {
      if (!isWebSocketBusinessType(event) || typeof callback !== 'function') {
        throw new Error('WebSocket订阅必须提供业务事件编号和处理函数')
      }
      if (!listeners.has(event)) listeners.set(event, new Set())
      const callbacks = listeners.get(event)
      callbacks.add(callback)
      return () => {
        callbacks.delete(callback)
        if (!callbacks.size && listeners.get(event) === callbacks) listeners.delete(event)
      }
    }
  }
}

export const adminWebSocket = createWebSocketClient({ presenter: createWebSocketPresenter() })
