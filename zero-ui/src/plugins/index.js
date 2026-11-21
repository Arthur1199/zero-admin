/**
 * 将页签、权限、缓存、弹窗和下载能力挂到应用实例，业务页面通过同一入口调用。
 */
import tab from './tab'
import auth from './auth'
import cache from './cache'
import modal from './modal'
import download from './download'

export default {
  install(app) {
    // 页签操作
    app.config.globalProperties.$tab = tab
    // 认证对象
    app.config.globalProperties.$auth = auth
    // 缓存对象
    app.config.globalProperties.$cache = cache
    // 模态框对象
    app.config.globalProperties.$modal = modal
    // 下载文件
    app.config.globalProperties.$download = download
  }
}
