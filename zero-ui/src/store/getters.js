/**
 * 跨组件读取 Vuex 状态的统一别名；这里只映射状态，不发请求或修改业务数据。
 */
const getters = {
  sidebar: state => state.app.sidebar,
  size: state => state.app.size,
  device: state => state.app.device,
  menuOpenedKeys: state => state.app.menuOpenedKeys,
  dict: state => state.dict.dict,
  visitedViews: state => state.tagsView.visitedViews,
  cachedViews: state => state.tagsView.cachedViews,
  token: state => state.user.token,
  avatar: state => state.user.avatar,
  id: state => state.user.id,
  name: state => state.user.name,
  nickName: state => state.user.nickName,
  introduction: state => state.user.introduction,
  roles: state => state.user.roles,
  permissions: state => state.user.permissions,
  permission_routes: state => state.permission.routes,
  topbarRouters: state => state.permission.topbarRouters,
  defaultRoutes: state => state.permission.defaultRoutes,
  sidebarRouters: state => state.permission.sidebarRouters
}
export default getters
