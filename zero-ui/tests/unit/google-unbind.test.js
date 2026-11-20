import { beforeEach, describe, expect, it, vi } from 'vitest'
import GoogleBinding from '@/views/system/google/index.vue'
import { unbindGoogle } from '@/api/system/google'
import { getToken } from '@/utils/auth'

vi.mock('@/api/system/google', () => ({
  getGoogleAuthKey: vi.fn(),
  listGoogle: vi.fn(),
  unbindGoogle: vi.fn()
}))
vi.mock('@/utils/auth', () => ({ getToken: vi.fn() }))

describe('谷歌解绑后的退出', () => {
  let page
  beforeEach(() => {
    vi.resetAllMocks()
    getToken.mockReturnValue('current-session')
    unbindGoogle.mockResolvedValue({ code: 200 })
    page = {
      $modal: { confirm: vi.fn().mockResolvedValue(), msgSuccess: vi.fn() },
      $store: {
        getters: { id: 2, token: 'current-session' },
        dispatch: vi.fn().mockImplementation(() => getToken.mockReturnValue(undefined))
      },
      $route: { path: '/system/google' },
      $router: { replace: vi.fn() },
      getList: vi.fn()
    }
  })

  it('自己解绑成功时，即使没有收到退出通知也返回登录页', async () => {
    await GoogleBinding.methods.handleUnbind.call(page, { userId: '2', userName: 'self' })
    expect(page.$store.dispatch).toHaveBeenCalledExactlyOnceWith('FedLogOut')
    expect(page.$router.replace).toHaveBeenCalledExactlyOnceWith('/login')
    expect(page.getList).not.toHaveBeenCalled()
  })

  it('解绑其他账号后只刷新当前管理员的列表', async () => {
    await GoogleBinding.methods.handleUnbind.call(page, { userId: 3, userName: 'other' })
    expect(page.getList).toHaveBeenCalledTimes(1)
    expect(page.$store.dispatch).not.toHaveBeenCalled()
    expect(page.$router.replace).not.toHaveBeenCalled()
  })

  it('延迟的解绑响应不会清除之后建立的新登录态', async () => {
    unbindGoogle.mockImplementationOnce(async () => {
      page.$store.getters.token = 'new-session'
      getToken.mockReturnValue('new-session')
      return { code: 200 }
    })
    await GoogleBinding.methods.handleUnbind.call(page, { userId: 2, userName: 'self' })
    expect(page.$store.dispatch).not.toHaveBeenCalled()
    expect(page.$router.replace).not.toHaveBeenCalled()
  })
})
