/**
 * 将后端 component 标识映射到 Vite 构建时收集的页面模块，保留目录入口和 .vue 路径兼容。
 */
const views = import.meta.glob('../views/**/*.vue')
/**
 * @param {string} view 后端返回的页面组件标识，支持目录入口及 .vue 后缀
 * @returns {Function} 页面模块的异步加载函数；未知标识直接抛错，不加载任意路径
 */
export function loadView(view) {
  const normalized = String(view || '').replace(/^\/+/, '').replace(/\.vue$/, '')
  const loader = views[`../views/${normalized}.vue`] || views[`../views/${normalized}/index.vue`]
  if (!loader) throw new Error('菜单组件不存在：' + normalized)
  return loader
}
