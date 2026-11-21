/**
 * 在线系统通知 API：选项和在线账号走查询接口，本地预览与实际发送分开；图片复用公共 HTTP 上传，不通过 WebSocket 传文件。
 */
import request from '@/utils/request'
// 读取可选发送目标和通知配置。
export const getNoticeOptions = () => request({ url: '/systemTools/systemNotice/options', method: 'get' })
// 按查询条件取得当前在线账号，发送前仍由后台重新核对。
export const getNoticeOnlineUsers = params => request({ url: '/systemTools/systemNotice/onlineUsers', method: 'get', params })
// 仅提交预览处理，不向在线账号发送通知；允许用户重复调整并预览。
export const previewNotice = data => request({ url: '/systemTools/systemNotice/preview', method: 'post', data, headers: { repeatSubmit: false } })
// 实际发送入口；调用前由页面完成表单校验及发送确认。
export const sendNotice = data => request({ url: '/systemTools/systemNotice/send', method: 'post', data })

// 沿用通用文件上传，经过统一请求认证及接口谷歌验证，不向WebSocket传图片文件。
export function uploadNoticeImage(file) {
  const data = new FormData()
  data.append('file', file)
  return request({ url: '/common/upload', method: 'post', data,
    headers: { 'Content-Type': 'multipart/form-data', repeatSubmit: false } })
}
