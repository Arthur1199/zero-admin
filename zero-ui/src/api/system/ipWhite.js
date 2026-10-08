import request from '@/utils/request'

// 查询登录白名单列表
export function listIpWhite(query) {
  return request({
    url: '/system/ipWhite/list',
    method: 'get',
    params: query
  })
}

// 查询登录白名单详细
export function getIpWhite(id) {
  return request({
    url: '/system/ipWhite/' + id,
    method: 'get'
  })
}

// 根据IP解析地区
export function getIpWhiteRegion(ipAddr) {
  return request({
    url: '/system/ipWhite/region',
    method: 'get',
    params: { ipAddr }
  })
}

// 新增登录白名单
export function addIpWhite(data) {
  return request({
    url: '/system/ipWhite',
    method: 'post',
    data: data
  })
}

// 修改登录白名单
export function updateIpWhite(data) {
  return request({
    url: '/system/ipWhite',
    method: 'put',
    data: data
  })
}

// 删除登录白名单
export function delIpWhite(id) {
  return request({
    url: '/system/ipWhite/' + id,
    method: 'delete'
  })
}
