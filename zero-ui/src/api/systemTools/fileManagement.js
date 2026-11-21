/**
 * 文件管理 API：经 admin 代理访问文件服务，列表与详情使用各自响应结构；上传限制配置通过独立接口读取。
 */
import request from '@/utils/request'

// 按筛选条件读取文件记录分页，返回 rows 和 total。
export function listFile(query) {
  return request({
    url: '/systemTools/fileManagement/list',
    method: 'get',
    params: query
  })
}

// 按记录 ID 取得详情，业务对象位于 data。
export function getFile(id) {
  return request({
    url: '/systemTools/fileManagement/' + id,
    method: 'get'
  })
}

// 读取文件管理入口的上传限制，不把页面默认提示当成服务端实际配置。
export function getUploadConfig() {
  return request({
    url: '/systemTools/fileManagement/upload/config',
    method: 'get'
  })
}

// 提交记录 ID 或既有格式的 ID 列表；外部存储删除与记录清理由后台协调。
export function delFile(ids) {
  return request({
    url: '/systemTools/fileManagement/' + ids,
    method: 'delete'
  })
}
