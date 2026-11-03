const views = import.meta.glob('../views/**/*.vue')
/** 仅匹配构建时已知页面，保持后端目录入口及.vue路径兼容。 */
export function loadView(view) {
  const normalized = String(view || '').replace(/^\/+/, '').replace(/\.vue$/, '')
  const loader = views[`../views/${normalized}.vue`] || views[`../views/${normalized}/index.vue`]
  if (!loader) throw new Error('菜单组件不存在：' + normalized)
  return loader
}
