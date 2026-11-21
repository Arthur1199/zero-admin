/**
 * 当前后台进程的实时日志轮询的请求封装；复用公共 request 的认证和错误处理，由页面负责交互及加载状态。
 */
import request from '@/utils/request'

// 读取当前管理后台进程日志；配置仅用于消费方观察响应状态，仍复用全局认证和错误处理。
export function listRealtimeLogs(params, config = {}) {
  return request({ ...config, url: '/monitor/realtimeLog/events', method: 'get', params })
}
