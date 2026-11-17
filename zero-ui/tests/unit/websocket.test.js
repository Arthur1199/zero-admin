import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
vi.mock('@/utils/auth', () => ({ getToken: () => undefined }))
import { buildWebSocketUrl, createWebSocketClient } from '@/utils/websocket'
import { WebSocketMessageType as Info } from '@/utils/websocketMessageType'

let client
let sockets
let token
let expired
let presenter
const receive = (socket, messageType, msgData = {}) => socket.onmessage?.({ data: JSON.stringify({ id: 'test-message', sendTime: Date.now(), msgType: { messageType }, msgData, forwardUrl: '' }) })

beforeEach(() => {
  vi.useFakeTimers()
  sockets = []
  token = 'isolated-test-credential'
  expired = vi.fn()
  presenter = { show: vi.fn(), clear: vi.fn() }
  client = createWebSocketClient({
    presenter,
    readToken: () => token,
    baseApi: '/dev-api/',
    pageUrl: () => 'https://admin.example.test:8443/console',
    random: () => 0,
    createSocket: url => {
      const socket = { url, send: vi.fn(), close: vi.fn() }
      sockets.push(socket)
      return socket
    }
  })
})
afterEach(() => { client.stop(); vi.useRealTimers() })

describe('WebSocket自动地址', () => {
  it.each([
    ['/dev-api', 'http://localhost:80/', 'ws://localhost/dev-api/ws/admin'],
    ['/prod-api/', 'https://example.test/admin', 'wss://example.test/prod-api/ws/admin'],
    ['https://api.example.test:8443/admin-api/', 'https://example.test/', 'wss://api.example.test:8443/admin-api/ws/admin'],
    ['admin-api', 'http://example.test/nested/page', 'ws://example.test/admin-api/ws/admin'],
    ['', 'https://example.test/', 'wss://example.test/ws/admin']
  ])('按API前缀生成地址 %s', (api, page, expected) => {
    expect(buildWebSocketUrl(api, page)).toBe(expected)
  })
  it.each(['http://api.example.test', 'ws://api.example.test', 'https://user:password@example.test', '/api?token=unsafe', '/api#fragment'])('拒绝不安全API地址 %s', api => {
    expect(() => buildWebSocketUrl(api, 'https://example.test')).toThrow()
  })
})

describe('WebSocket登录、重连与订阅', () => {
  it('收到退出事件后主动退出的暂停不能取消强退回调', async () => {
    client.start({ onAuthFailure: expired })
    receive(sockets[0], Info.CREATE_CONNECTION, { heartbeatInterval: 30000, idleTimeout: 90000 })
    receive(sockets[0], Info.LOGIN_OUT)
    client.pause()
    await Promise.resolve()
    expect(expired).toHaveBeenCalledExactlyOnceWith(token)
    client.resume()
    vi.advanceTimersByTime(120000)
    expect(sockets).toHaveLength(1)
  })
  it('退出事件停止心跳和重连，只调用一次全局退出并传递原身份', async () => {
    client.start({ onAuthFailure: expired })
    const socket = sockets[0]
    receive(socket, Info.CREATE_CONNECTION, { heartbeatInterval: 30000, idleTimeout: 90000 })
    const oldMessage = socket.onmessage
    const oldClose = socket.onclose
    receive(socket, Info.LOGIN_OUT)
    oldMessage({ data: JSON.stringify({ msgType: { messageType: Info.LOGIN_OUT } }) })
    oldClose({ code: 4401 })
    await Promise.resolve()
    expect(expired).toHaveBeenCalledExactlyOnceWith(token)
    expect(presenter.show).not.toHaveBeenCalled()
    client.start()
    vi.advanceTimersByTime(120000)
    expect(sockets).toHaveLength(1)
    expect(vi.getTimerCount()).toBe(0)
  })

  it('退出通知排队后新身份登录，不执行旧身份退出', async () => {
    client.start({ onAuthFailure: expired })
    receive(sockets[0], Info.CREATE_CONNECTION, { heartbeatInterval: 30000, idleTimeout: 90000 })
    receive(sockets[0], Info.LOGIN_OUT)
    token = 'new-isolated-token'
    client.start()
    await Promise.resolve()
    expect(expired).not.toHaveBeenCalled()
    expect(sockets).toHaveLength(2)
  })
  it('重复初始化只有一个连接，凭证只出现在认证帧，认证后才发心跳', () => {
    client.start({ onAuthFailure: expired })
    client.start({ onAuthFailure: expired })
    expect(sockets).toHaveLength(1)
    const socket = sockets[0]
    expect(socket.url).not.toContain(token)
    socket.onopen()
    expect(JSON.parse(socket.send.mock.calls[0][0])).toEqual({ token })
    expect(JSON.parse(socket.send.mock.calls[0][0])).not.toHaveProperty('version')
    receive(socket, Info.CREATE_CONNECTION, { heartbeatInterval: 30000, idleTimeout: 90000 })
    vi.advanceTimersByTime(30000)
    expect(JSON.parse(socket.send.mock.calls[1][0]).msgType.messageType).toBe(Info.PING_PONG)
    expect(JSON.parse(socket.send.mock.calls[1][0])).not.toHaveProperty('version')
  })
  it('事件按编号分发，解除订阅与其它观察者异常不影响连接', () => {
    client.start()
    const first = vi.fn()
    const second = vi.fn()
    const dispose = client.subscribe(Info.SYSTEM_NOTICE, first)
    client.subscribe(Info.TASK_COMPLETED, second)
    const socket = sockets[0]
    receive(socket, Info.CREATE_CONNECTION, { heartbeatInterval: 30000, idleTimeout: 90000 })
    receive(socket, Info.SYSTEM_NOTICE, { id: 7 })
    expect(first).toHaveBeenCalledWith({ id: 7 }, expect.objectContaining({ msgType: { messageType: Info.SYSTEM_NOTICE } }))
    expect(second).not.toHaveBeenCalled()
    dispose()
    receive(socket, Info.SYSTEM_NOTICE, {})
    expect(first).toHaveBeenCalledTimes(1)
  })
  it('网络异常关闭旧连接再退避，重连读取最新登录凭证', () => {
    client.start()
    sockets[0].onerror()
    expect(sockets[0].close).toHaveBeenCalledTimes(1)
    token = 'new-isolated-test-credential'
    vi.advanceTimersByTime(1000)
    expect(sockets).toHaveLength(2)
    sockets[1].onopen()
    expect(JSON.parse(sockets[1].send.mock.calls[0][0]).token).toBe(token)
  })
  it('退出清理全部定时器，旧回调不会重新建立连接', () => {
    client.start()
    const onclose = sockets[0].onclose
    sockets[0].onerror()
    client.stop()
    onclose({ code: 1006 })
    vi.advanceTimersByTime(120000)
    expect(sockets).toHaveLength(1)
    expect(vi.getTimerCount()).toBe(0)
  })
  it('认证失败只触发一次失效处理，同一凭证不继续重连', async () => {
    client.start({ onAuthFailure: expired })
    sockets[0].onclose({ code: 4401 })
    await Promise.resolve()
    client.start({ onAuthFailure: expired })
    vi.advanceTimersByTime(120000)
    expect(expired).toHaveBeenCalledTimes(1)
    expect(sockets).toHaveLength(1)
  })
  it('握手或认证无响应超时后重连，认证后长期无响应也重连', () => {
    client.start()
    vi.advanceTimersByTime(11000)
    expect(sockets).toHaveLength(2)
    receive(sockets[1], Info.CREATE_CONNECTION, { heartbeatInterval: 30000, idleTimeout: 90000 })
    vi.advanceTimersByTime(92000)
    expect(sockets).toHaveLength(3)
  })
  it('旧会话失效不能清除其它标签页已经切换的新身份', async () => {
    client.start({ onAuthFailure: expired })
    token = 'new-identity'
    sockets[0].onclose({ code: 4401 })
    await Promise.resolve()
    expect(expired).not.toHaveBeenCalled()
    expect(sockets).toHaveLength(2)
  })
  it('失效回调排队期间重新登录，不清除新身份', async () => {
    client.start({ onAuthFailure: expired })
    sockets[0].onclose({ code: 4401 })
    token = 'replacement-identity'
    client.start({ onAuthFailure: expired })
    await Promise.resolve()
    expect(expired).not.toHaveBeenCalled()
    expect(sockets).toHaveLength(2)
  })
  it('连接建立异常不阻断HTTP登录初始化，订阅参数必须有效', () => {
    const failure = createWebSocketClient({ readToken: () => token, baseApi: 'http://unsafe.test', pageUrl: () => 'https://safe.test' })
    const onError = vi.fn()
    expect(() => failure.start({ onError })).not.toThrow()
    expect(onError).toHaveBeenCalledTimes(1)
    expect(() => client.subscribe('', () => {})).toThrow()
    failure.stop()
  })
  it('认证成功后立即断线仍累积退避，收到PONG后才恢复最短间隔', () => {
    client.start()
    for (const delay of [1000, 2000, 4000]) {
      const count = sockets.length
      const socket = sockets.at(-1)
      receive(socket, Info.CREATE_CONNECTION, { heartbeatInterval: 30000, idleTimeout: 90000 })
      socket.onclose({ code: 1013 })
      vi.advanceTimersByTime(delay - 1)
      expect(sockets).toHaveLength(count)
      vi.advanceTimersByTime(1)
      expect(sockets).toHaveLength(count + 1)
    }
    receive(sockets.at(-1), Info.CREATE_CONNECTION, { heartbeatInterval: 30000, idleTimeout: 90000 })
    receive(sockets.at(-1), Info.PING_PONG)
    sockets.at(-1).onerror()
    vi.advanceTimersByTime(1000)
    expect(sockets).toHaveLength(5)
  })
  it('退出请求期间阻止路由重新连接，退出失败恢复原订阅', () => {
    client.start({ onAuthFailure: expired })
    const callback = vi.fn()
    client.subscribe(Info.ORDER_CHANGED, callback)
    client.pause()
    client.start()
    vi.advanceTimersByTime(120000)
    expect(sockets).toHaveLength(1)
    expect(vi.getTimerCount()).toBe(0)
    client.resume()
    expect(sockets).toHaveLength(2)
    receive(sockets[1], Info.CREATE_CONNECTION, { heartbeatInterval: 30000, idleTimeout: 90000 })
    receive(sockets[1], Info.ORDER_CHANGED, 1)
    expect(callback).toHaveBeenCalledTimes(1)
  })
  it('并发退出请求未全部结束时不恢复，退出成功后迟到失败不能复活连接', () => {
    client.start()
    client.pause()
    client.pause()
    client.resume()
    client.start()
    expect(sockets).toHaveLength(1)
    client.resume()
    expect(sockets).toHaveLength(2)
    client.pause()
    client.stop()
    client.resume()
    vi.advanceTimersByTime(120000)
    expect(sockets).toHaveLength(2)
    expect(vi.getTimerCount()).toBe(0)
  })
  it('订阅回调退出后，不再把同一条旧消息发给其它观察者', () => {
    client.start()
    client.subscribe(Info.ORDER_CHANGED, () => client.stop())
    const stale = vi.fn()
    client.subscribe(Info.ORDER_CHANGED, stale)
    receive(sockets[0], Info.CREATE_CONNECTION, { heartbeatInterval: 30000, idleTimeout: 90000 })
    receive(sockets[0], Info.ORDER_CHANGED, {})
    expect(stale).not.toHaveBeenCalled()
  })
  it('异步订阅错误被独立处理，不产生未消费的Promise拒绝', async () => {
    const error = vi.fn()
    client.start({ onError: error })
    client.subscribe(Info.ORDER_CHANGED, async () => { throw new Error('isolated-test') })
    const next = vi.fn()
    client.subscribe(Info.ORDER_CHANGED, next)
    receive(sockets[0], Info.CREATE_CONNECTION, { heartbeatInterval: 30000, idleTimeout: 90000 })
    receive(sockets[0], Info.ORDER_CHANGED, {})
    await Promise.resolve()
    expect(next).toHaveBeenCalledTimes(1)
    expect(error).toHaveBeenCalledWith('WebSocket业务订阅处理失败')
  })
  it('无参数的重复启动不会清除已注册的登录失效处理', async () => {
    client.start({ onAuthFailure: expired })
    client.start()
    sockets[0].onclose({ code: 4401 })
    await Promise.resolve()
    expect(expired).toHaveBeenCalledTimes(1)
  })
})

describe('WebSocket统一消息协议', () => {
  it('认证失败关闭连接后停止重连', async () => {
    client.start({ onAuthFailure: expired })
    sockets[0].onclose({ code: 4401 })
    await Promise.resolve()
    vi.advanceTimersByTime(120000)
    expect(expired).toHaveBeenCalledTimes(1)
    expect(sockets).toHaveLength(1)
    expect(vi.getTimerCount()).toBe(0)
  })
  it('通信错误只重连，不清理HTTP登录', async () => {
    client.start({ onAuthFailure: expired })
    sockets[0].onclose({ code: 1002 })
    await Promise.resolve()
    vi.advanceTimersByTime(1000)
    expect(expired).not.toHaveBeenCalled()
    expect(sockets).toHaveLength(2)
  })
  it('旧协议不会分发业务消息或被当成认证成功', () => {
    client.start()
    const callback = vi.fn()
    client.subscribe(Info.SYSTEM_NOTICE, callback)
    sockets[0].onmessage({ data: JSON.stringify({ type: 'EVENT', data: { event: 'system.notice', payload: {} } }) })
    expect(callback).not.toHaveBeenCalled()
    vi.advanceTimersByTime(1000)
    expect(sockets).toHaveLength(2)
  })
  it('编号订阅返回业务数据及完整元信息，心跳响应不引发循环发送', () => {
    client.start()
    const callback = vi.fn()
    client.subscribe(Info.ORDER_CHANGED, callback)
    receive(sockets[0], Info.CREATE_CONNECTION, { heartbeatInterval: 30000, idleTimeout: 90000 })
    const message = { id: 'business-test', sendTime: Date.now(), msgType: { messageType: Info.ORDER_CHANGED, desc: '订单状态更新', displayType: 'NOTIFICATION', messageLevel: 'NORMAL' }, msgData: { orderId: '10001' }, forwardUrl: '/orders/detail' }
    sockets[0].onmessage({ data: JSON.stringify(message) })
    expect(callback).toHaveBeenCalledWith(message.msgData, message)
    receive(sockets[0], Info.PING_PONG)
    expect(sockets[0].send).not.toHaveBeenCalled()
    expect(() => client.subscribe(Info.CREATE_CONNECTION, callback)).toThrow()
    expect(() => client.subscribe('101', callback)).toThrow()
  })
})

it('业务消息交给统一展示器，认证与心跳不弹窗，退出暂停清理提示', () => {
  client.start()
  receive(sockets[0], Info.CREATE_CONNECTION, { heartbeatInterval: 30000, idleTimeout: 90000 })
  receive(sockets[0], Info.PING_PONG)
  expect(presenter.show).not.toHaveBeenCalled()
  receive(sockets[0], Info.REFRESH_BROWSER, '业务消息')
  expect(presenter.show).toHaveBeenCalledTimes(1)
  const count = presenter.clear.mock.calls.length
  client.pause()
  expect(presenter.clear).toHaveBeenCalledTimes(count + 1)
  client.stop()
  expect(presenter.clear).toHaveBeenCalledTimes(count + 2)
})

it.each([4, 5, 6, 20, 99, -4])('编号%s不受100起点限制，仍可订阅和接收', messageType => {
  client.start()
  const callback = vi.fn()
  client.subscribe(messageType, callback)
  receive(sockets[0], Info.CREATE_CONNECTION, { heartbeatInterval: 30000, idleTimeout: 90000 })
  receive(sockets[0], messageType, '业务数据')
  expect(callback).toHaveBeenCalledWith('业务数据', expect.any(Object))
  expect(presenter.show).toHaveBeenCalledTimes(1)
})

it.each([0, 1, 3])('通信控制编号%s不能作为业务订阅', messageType => {
  expect(() => client.subscribe(messageType, () => {})).toThrow()
})

it('全局刷新事件只刷新一次，不重复弹窗，退出后旧回调不能刷新', () => {
  const refresh = vi.fn()
  client.start({ onRefresh: refresh })
  client.start()
  receive(sockets[0], Info.CREATE_CONNECTION, { heartbeatInterval: 30000, idleTimeout: 90000 })
  const stale = sockets[0].onmessage
  receive(sockets[0], Info.PING_PONG)
  expect(refresh).not.toHaveBeenCalled()
  receive(sockets[0], Info.REFRESH_BROWSER, { content: '角色已更新' })
  receive(sockets[0], Info.REFRESH_BROWSER, { content: '部门已更新' })
  expect(refresh).toHaveBeenCalledTimes(1)
  expect(presenter.show).not.toHaveBeenCalled()
  client.stop()
  stale({ data: JSON.stringify({ msgType: { messageType: Info.REFRESH_BROWSER } }) })
  expect(refresh).toHaveBeenCalledTimes(1)
})

it('刷新失败的Promise被消费，不影响连接和其它业务消息', async () => {
  const error = vi.fn()
  client.start({ onRefresh: async () => { throw new Error('isolated') }, onError: error })
  receive(sockets[0], Info.CREATE_CONNECTION, { heartbeatInterval: 30000, idleTimeout: 90000 })
  receive(sockets[0], Info.REFRESH_BROWSER)
  await Promise.resolve()
  expect(error).toHaveBeenCalledWith('WebSocket页面刷新失败')
  expect(sockets).toHaveLength(1)
})

it('重连切换到新身份后重新允许接收刷新指令', () => {
  const refresh = vi.fn()
  client.start({ onRefresh: refresh })
  receive(sockets[0], Info.CREATE_CONNECTION, { heartbeatInterval: 30000, idleTimeout: 90000 })
  receive(sockets[0], Info.REFRESH_BROWSER)
  expect(refresh).toHaveBeenCalledTimes(1)
  token = 'replacement-test-identity'
  sockets[0].onerror()
  vi.advanceTimersByTime(1000)
  receive(sockets[1], Info.CREATE_CONNECTION, { heartbeatInterval: 30000, idleTimeout: 90000 })
  receive(sockets[1], Info.REFRESH_BROWSER)
  expect(refresh).toHaveBeenCalledTimes(2)
})
