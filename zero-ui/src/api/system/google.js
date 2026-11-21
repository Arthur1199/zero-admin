/**
 * 谷歌验证绑定记录的请求封装；复用公共 request 的认证和错误处理，由页面负责交互及加载状态。
 */
import request from '@/utils/request'

// 查询谷歌绑定列表
export function listGoogle(query) {
  return request({
    url: '/system/google/list',
    method: 'get',
    params: query
  })
}

// 查看谷歌密钥明文
export function getGoogleAuthKey(userId) {
  return request({
    url: '/system/google/key/' + userId,
    method: 'get'
  })
}

// 解绑谷歌动态码
export function unbindGoogle(data) {
  return request({
    url: '/system/google/unbind',
    method: 'put',
    data: data
  })
}
