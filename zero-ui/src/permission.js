import router from './router'
import store from './store'
import { Message } from 'element-ui'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { getToken } from '@/utils/auth'
import { isPathMatch } from '@/utils/validate'
import { isRelogin } from '@/utils/request'
import { getGoogleAuthApis } from '@/api/system/apiAuth'

NProgress.configure({ showSpinner: false })

const whiteList = ['/login']
const googleAuthApisKey = 'google_auth_apis'

const isWhiteList = (path) => {
  return whiteList.some(pattern => isPathMatch(pattern, path))
}

const refreshGoogleAuthApis = () => {
  return getGoogleAuthApis().then(response => {
    const data = response.data || {}
    if (data.isNeedGoogleAuth && data.authApis && data.authApis.length > 0) {
      localStorage.setItem(googleAuthApisKey, JSON.stringify(data.authApis))
    } else {
      localStorage.removeItem(googleAuthApisKey)
    }
  }).catch(() => {
    localStorage.removeItem(googleAuthApisKey)
  })
}

router.beforeEach((to, from, next) => {
  NProgress.start()
  if (getToken()) {
    to.meta.title && store.dispatch('settings/setTitle', to.meta.title)
    if (to.path === '/login') {
      next({ path: '/' })
      NProgress.done()
    } else if (isWhiteList(to.path)) {
      next()
    } else {
      if (store.getters.roles.length === 0) {
        isRelogin.show = true
        store.dispatch('GetInfo').then(() => {
          isRelogin.show = false
          refreshGoogleAuthApis().finally(() => {
            store.dispatch('GenerateRoutes').then(accessRoutes => {
              router.addRoutes(accessRoutes)
              next({ ...to, replace: true })
            })
          })
        }).catch(err => {
          store.dispatch('LogOut').then(() => {
            Message.error(err)
            next({ path: '/' })
          })
        })
      } else {
        next()
      }
    }
  } else {
    localStorage.removeItem(googleAuthApisKey)
    if (isWhiteList(to.path)) {
      next()
    } else {
      next(`/login?redirect=${encodeURIComponent(to.fullPath)}`)
      NProgress.done()
    }
  }
})

router.afterEach(() => {
  NProgress.done()
})
