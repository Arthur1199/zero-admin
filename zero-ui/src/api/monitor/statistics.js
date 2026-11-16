import request from '@/utils/request'

export function getStatisticsOverview() {
  return request({ url: '/monitor/statistics/overview', method: 'get' })
}
export function getStatisticsTrend(params) {
  return request({ url: '/monitor/statistics/trend', method: 'get', params })
}
export function getStatisticsDistribution(params) {
  return request({ url: '/monitor/statistics/distribution', method: 'get', params })
}
export function getStatisticsRanking(params) {
  return request({ url: '/monitor/statistics/ranking', method: 'get', params })
}
export function listStatistics(params) {
  return request({ url: '/monitor/statistics/list', method: 'get', params })
}
