import request from '@/utils/request'

// 查询微服务状态列表
export function listMicroService(query) {
  return request({
    url: '/monitor/microService/list',
    method: 'get',
    params: query
  })
}
