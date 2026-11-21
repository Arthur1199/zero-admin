/**
 * 管理后台运行统计的请求封装；复用公共 request 的认证和错误处理，由页面负责交互及加载状态。
 */
import request from '@/utils/request'

// 读取当前健康状态与概览指标，普通业务数据从响应 data 获取。
export function getStatisticsOverview() {
  return request({ url: '/monitor/statistics/overview', method: 'get' })
}
// 按日期及粒度读取趋势序列，参数由统计页组织。
export function getStatisticsTrend(params) {
  return request({ url: '/monitor/statistics/trend', method: 'get', params })
}
// 按查询时段读取模块等分布数据。
export function getStatisticsDistribution(params) {
  return request({ url: '/monitor/statistics/distribution', method: 'get', params })
}
// 读取指定时段和排序口径的排行结果。
export function getStatisticsRanking(params) {
  return request({ url: '/monitor/statistics/ranking', method: 'get', params })
}
// 查询统计明细分页，页面读取 rows 和 total。
export function listStatistics(params) {
  return request({ url: '/monitor/statistics/list', method: 'get', params })
}
