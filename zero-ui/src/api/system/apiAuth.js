/**
 * 接口谷歌验证配置的请求封装；复用公共 request 的认证和错误处理，由页面负责交互及加载状态。
 */
import request from '@/utils/request'

// 查询API谷歌验证配置列表
export function listApiAuth(query) {
  return request({
    url: '/system/apiAuth/list',
    method: 'get',
    params: query
  })
}

// 查询API谷歌验证配置详细
export function getApiAuth(id) {
  return request({
    url: '/system/apiAuth/' + id,
    method: 'get'
  })
}

// 新增API谷歌验证配置
export function addApiAuth(data) {
  return request({
    url: '/system/apiAuth',
    method: 'post',
    data: data
  })
}

// 修改API谷歌验证配置
export function updateApiAuth(data) {
  return request({
    url: '/system/apiAuth',
    method: 'put',
    data: data
  })
}

// 删除API谷歌验证配置
export function delApiAuth(id) {
  return request({
    url: '/system/apiAuth/' + id,
    method: 'delete'
  })
}

// 获取需要接口谷歌二次验证的API列表
export function getGoogleAuthApis() {
  return request({
    url: '/system/apiAuth/googleAuthApis',
    method: 'get'
  })
}
