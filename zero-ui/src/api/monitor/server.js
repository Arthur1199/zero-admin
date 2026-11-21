/**
 * 服务器运行信息的请求封装；复用公共 request 的认证和错误处理，由页面负责交互及加载状态。
 */
import request from '@/utils/request'

// 获取服务信息
export function getServer() {
  return request({
    url: '/monitor/server',
    method: 'get'
  })
}