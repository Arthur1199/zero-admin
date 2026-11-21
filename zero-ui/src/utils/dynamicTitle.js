/**
 * 根据当前页面标题和布局配置更新浏览器标题；不改变路由或菜单名称。
 */
import store from '@/store'
import defaultSettings from '@/settings'

/**
 * 动态修改标题
 */
export function useDynamicTitle() {
  if (store.state.settings.dynamicTitle) {
    document.title = store.state.settings.title + ' - ' + defaultSettings.title
  } else {
    document.title = defaultSettings.title
  }
}