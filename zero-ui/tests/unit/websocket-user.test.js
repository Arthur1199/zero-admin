import { beforeEach, expect, it, vi } from 'vitest'

const mocks = vi.hoisted(() => ({
  logout: vi.fn(), pause: vi.fn(), resume: vi.fn(), stop: vi.fn(), removeToken: vi.fn(), resetRouter: vi.fn()
}))
vi.mock('@/api/login', () => ({ login: vi.fn(), getInfo: vi.fn(), logout: mocks.logout }))
vi.mock('@/router', () => ({ default: {}, resetRouter: mocks.resetRouter }))
vi.mock('@/utils/auth', () => ({ getToken: () => 'isolated-user-token', setToken: vi.fn(), removeToken: mocks.removeToken }))
vi.mock('@/utils/websocket', () => ({ adminWebSocket: { pause: mocks.pause, resume: mocks.resume, stop: mocks.stop } }))
vi.mock('element-plus', () => ({ ElMessageBox: {} }))
import user from '@/store/modules/user'

beforeEach(() => {
  vi.clearAllMocks()
  vi.stubGlobal('localStorage', { removeItem: vi.fn() })
})

it('退出HTTP失败只恢复通信，不清理登录态或路由', async () => {
  const context = { commit: vi.fn(), dispatch: vi.fn(), state: { token: 'isolated-user-token' } }
  mocks.logout.mockRejectedValue(new Error('isolated-network-error'))
  await expect(user.actions.LogOut(context)).rejects.toThrow('isolated-network-error')
  expect(mocks.pause).toHaveBeenCalledTimes(1)
  expect(mocks.resume).toHaveBeenCalledTimes(1)
  expect(mocks.stop).not.toHaveBeenCalled()
  expect(mocks.removeToken).not.toHaveBeenCalled()
  expect(context.commit).not.toHaveBeenCalled()
  expect(mocks.resetRouter).not.toHaveBeenCalled()
})

it('退出HTTP成功后清理通信和原有登录状态', async () => {
  const context = { commit: vi.fn(), dispatch: vi.fn(), state: { token: 'isolated-user-token' } }
  mocks.logout.mockResolvedValue({ code: 200 })
  await user.actions.LogOut(context)
  expect(mocks.pause).toHaveBeenCalledTimes(1)
  expect(mocks.stop).toHaveBeenCalledTimes(1)
  expect(mocks.resume).not.toHaveBeenCalled()
  expect(mocks.removeToken).toHaveBeenCalledTimes(1)
  expect(context.commit).toHaveBeenCalledWith('SET_TOKEN', '')
  expect(mocks.resetRouter).toHaveBeenCalledTimes(1)
})

it('前端登录失效直接清理连接，不等待退出接口', async () => {
  const context = { commit: vi.fn(), dispatch: vi.fn() }
  await user.actions.FedLogOut(context)
  expect(mocks.stop).toHaveBeenCalledTimes(1)
  expect(mocks.pause).not.toHaveBeenCalled()
  expect(mocks.logout).not.toHaveBeenCalled()
  expect(mocks.removeToken).toHaveBeenCalledTimes(1)
})

it.each([true, false])('被动退出调用接口，接口成功或失败都清理登录态：%s', async success => {
  const context = { state: { token: 'isolated-user-token' }, dispatch: vi.fn() }
  if (success) mocks.logout.mockResolvedValue({ code: 200 })
  else mocks.logout.mockRejectedValue(new Error('isolated-network-error'))
  await user.actions.ForceLogOut(context, 'isolated-user-token')
  expect(mocks.logout).toHaveBeenCalledExactlyOnceWith('isolated-user-token')
  expect(context.dispatch).toHaveBeenCalledExactlyOnceWith('FedLogOut')
  expect(mocks.resume).not.toHaveBeenCalled()
})

it('退出接口等待期间新身份登录，不清除新身份', async () => {
  const context = { state: { token: 'isolated-user-token' }, dispatch: vi.fn() }
  let complete
  mocks.logout.mockReturnValue(new Promise(resolve => { complete = resolve }))
  const pending = user.actions.ForceLogOut(context, 'isolated-user-token')
  context.state.token = 'new-isolated-token'
  complete({ code: 200 })
  await pending
  expect(context.dispatch).not.toHaveBeenCalled()
})

it('主动退出的旧请求成功返回，不清除期间新登录的身份', async () => {
  const context = { state: { token: 'isolated-user-token' }, dispatch: vi.fn(), commit: vi.fn() }
  let complete
  mocks.logout.mockReturnValue(new Promise(resolve => { complete = resolve }))
  const pending = user.actions.LogOut(context)
  context.state.token = 'new-isolated-token'
  complete({ code: 200 })
  await pending
  expect(context.commit).not.toHaveBeenCalled()
  expect(mocks.removeToken).not.toHaveBeenCalled()
  expect(mocks.stop).not.toHaveBeenCalled()
  expect(mocks.resume).toHaveBeenCalledOnce()
})
