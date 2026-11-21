/**
 * 把后端菜单转换成前端路由及顶部、侧栏菜单；仅从已知页面模块加载组件，并按现有权限规则过滤独立路由。
 */
import { loadView } from '@/utils/viewLoader'
import auth from '@/plugins/auth'
import { isExternal } from '@/utils/validate'
import { addDynamicRoutes, constantRoutes, dynamicRoutes, Layout } from '@/router'
import { getRouters } from '@/api/menu'
import ParentView from '@/components/ParentView'
import InnerLink from '@/layout/components/InnerLink'

const permission = {
  state: {
    routes: [],
    addRoutes: [],
    defaultRoutes: [],
    topbarRouters: [],
    sidebarRouters: []
  },
  mutations: {
    SET_ROUTES: (state, routes) => {
      state.addRoutes = routes
      state.routes = constantRoutes.concat(routes)
    },
    SET_DEFAULT_ROUTES: (state, routes) => {
      state.defaultRoutes = constantRoutes.concat(routes)
    },
    SET_TOPBAR_ROUTES: (state, routes) => {
      state.topbarRouters = routes
    },
    SET_SIDEBAR_ROUTERS: (state, routes) => {
      state.sidebarRouters = routes
    },
  },
  actions: {
    // 生成路由
    async GenerateRoutes({ commit }) {
      const response = await getRouters()
      const data = Array.isArray(response.data) ? response.data : []
      const sidebarRoutes = filterAsyncRouter(JSON.parse(JSON.stringify(data)))
      const rewriteRoutes = filterAsyncRouter(JSON.parse(JSON.stringify(data)), false, true)
      const asyncRoutes = filterDynamicRoutes(dynamicRoutes)
      rewriteRoutes.push({ path: '/:pathMatch(.*)*', redirect: '/404', hidden: true })
      addDynamicRoutes(asyncRoutes)
      commit('SET_ROUTES', rewriteRoutes)
      commit('SET_SIDEBAR_ROUTERS', constantRoutes.concat(sidebarRoutes))
      commit('SET_DEFAULT_ROUTES', sidebarRoutes)
      commit('SET_TOPBAR_ROUTES', sidebarRoutes)
      return rewriteRoutes
    }
  }
}

// 遍历后台传来的路由字符串，转换为组件对象
function filterAsyncRouter(asyncRouterMap, lastRouter = false, type = false) {
  return asyncRouterMap.filter(route => {
    // 外链仍在侧栏展示，由AppLink打开，不注册为Vue Router站内路由。
    if (type && isExternal(route.path)) return false
    if (type && route.children) {
      route.children = filterChildren(route.children)
    }
    if (route.component) {
      // Layout ParentView 组件特殊处理
      if (route.component === 'Layout') {
        route.component = Layout
      } else if (route.component === 'ParentView') {
        route.component = ParentView
      } else if (route.component === 'InnerLink') {
        route.component = InnerLink
      } else {
        route.component = loadView(route.component)
      }
    }
    if (route.children != null && route.children && route.children.length) {
      route.children = filterAsyncRouter(route.children, route, type)
    } else {
      delete route['children']
      delete route['redirect']
    }
    return true
  })
}

// 仅为可注册路由展开 ParentView 中间层；侧栏树使用独立副本，保留原菜单层级。
function filterChildren(childrenMap, lastRouter = false) {
  var children = []
  childrenMap.forEach(el => {
    if (isExternal(el.path)) return
    el.path = lastRouter ? lastRouter.path + '/' + el.path : el.path
    if (el.children && el.children.length && el.component === 'ParentView') {
      children = children.concat(filterChildren(el.children, el))
    } else {
      children.push(el)
    }
  })
  return children
}

// 动态路由遍历，验证是否具备权限
export function filterDynamicRoutes(routes) {
  const res = []
  routes.forEach(route => {
    if (route.permissions) {
      if (auth.hasPermiOr(route.permissions)) {
        res.push(route)
      }
    } else if (route.roles) {
      if (auth.hasRoleOr(route.roles)) {
        res.push(route)
      }
    }
  })
  return res
}

export { loadView } from '@/utils/viewLoader'

export default permission
