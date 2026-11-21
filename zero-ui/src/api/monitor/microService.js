/**
 * 微服务状态查询的请求封装；复用公共 request 的认证和错误处理，由页面负责交互及加载状态。
 */
import request from '@/utils/request'

// 查询微服务状态列表
export function listMicroService(query) {
  return request({
    url: '/monitor/microService/list',
    method: 'get',
    params: query
  })
}
