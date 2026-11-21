/**
 * 路由初始化与登录守卫：先取得账号信息和接口谷歌验证规则，再生成动态路由并启动在线通信；初始化失败保留可重试边界。
 */
import router, { addDynamicRoutes, resetRouter } from './router'
import store from './store'
import { ElMessage } from 'element-plus'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { getToken } from '@/utils/auth'
import { isPathMatch } from '@/utils/validate'
import { isRelogin } from '@/utils/request'
import { getGoogleAuthApis } from '@/api/system/apiAuth'
import { adminWebSocket } from '@/utils/websocket'

NProgress.configure({ showSpinner: false })
const whiteList = ['/login']
const googleAuthApisKey = 'google_auth_apis'
let initialization
// 在线连接复用当前身份；强退先清理失效会话，再由 Router 进入登录页。
function startWebSocket() {
  adminWebSocket.start({
    onAuthFailure: async token => {
      await store.dispatch('ForceLogOut', token)
      if (!getToken() && router.currentRoute.value.path !== '/login') await router.replace('/login')
    },
    onRefresh: () => window.location.reload(),
    onError: message => console.warn(message)
  })
}
// 登录初始化时刷新接口验证规则；读取失败清理旧规则，避免沿用上一会话的缓存。
async function refreshGoogleAuthApis() {
  try {
    const { data = {} } = await getGoogleAuthApis()
    if (data.isNeedGoogleAuth && data.authApis?.length) localStorage.setItem(googleAuthApisKey, JSON.stringify(data.authApis))
    else localStorage.removeItem(googleAuthApisKey)
  } catch { localStorage.removeItem(googleAuthApisKey) }
}
// 并发导航复用初始化，失败后允许下一次登录重试。
async function initializeRoutes() {
  if (!initialization) initialization = (async () => {
    isRelogin.show = true
    try {
      await store.dispatch('GetInfo')
      await refreshGoogleAuthApis()
      resetRouter()
      addDynamicRoutes(await store.dispatch('GenerateRoutes'))
      startWebSocket()
    } finally { isRelogin.show = false }
  })().finally(() => { initialization = undefined })
  return initialization
}
router.beforeEach(async to => {
  NProgress.start()
  const publicPath = whiteList.some(pattern => isPathMatch(pattern, to.path))
  if (!getToken()) {
    adminWebSocket.stop()
    localStorage.removeItem(googleAuthApisKey)
    if (publicPath) return true
    return '/login?redirect=' + encodeURIComponent(to.fullPath)
  }
  if (to.meta.title) store.dispatch('settings/setTitle', to.meta.title)
  if (to.path === '/login') return '/'
  if (publicPath) return true
  if (!store.getters.roles.length) {
    try {
      await initializeRoutes()
      // 初次进入尚未注册的路由，按原URL重新匹配。
      return { path: to.path, query: to.query, hash: to.hash, replace: true }
    } catch (error) {
      await store.dispatch('FedLogOut')
      ElMessage.error(error?.message || '获取登录信息或菜单失败，请重新登录')
      return '/login'
    }
  }
  startWebSocket()
  return true
})
router.afterEach(() => NProgress.done())
router.onError(() => NProgress.done())
