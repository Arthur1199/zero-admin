import request from '@/utils/request'
export const getNoticeOptions = () => request({ url: '/systemTools/systemNotice/options', method: 'get' })
export const getNoticeOnlineUsers = params => request({ url: '/systemTools/systemNotice/onlineUsers', method: 'get', params })
export const previewNotice = data => request({ url: '/systemTools/systemNotice/preview', method: 'post', data, headers: { repeatSubmit: false } })
export const sendNotice = data => request({ url: '/systemTools/systemNotice/send', method: 'post', data })

// 沿用通用文件上传，经过统一请求认证及接口谷歌验证，不向WebSocket传图片文件。
export function uploadNoticeImage(file) {
  const data = new FormData()
  data.append('file', file)
  return request({ url: '/common/upload', method: 'post', data,
    headers: { 'Content-Type': 'multipart/form-data', repeatSubmit: false } })
}
