/**
 * 备忘录 API：列表只读元数据，打开详情后再取正文；可见范围和可维护权限继续由服务端校验。
 */
import request from '@/utils/request'

// 查询可见备忘录元数据，列表不返回正文。
export function listMemo(query) {
  return request({ url: '/systemTools/memo/list', method: 'get', params: query })
}

// 查看可见备忘录正文，超级管理员可查看所有私有记录。
export function getMemo(memoId) {
  return request({ url: '/systemTools/memo/' + memoId, method: 'get' })
}

// 不让通用防重复提交逻辑把敏感正文写入sessionStorage；页面提交锁防止重复点击。
export function addMemo(data) {
  return request({ url: '/systemTools/memo', method: 'post', headers: { repeatSubmit: false }, data })
}

export function updateMemo(data) {
  return request({ url: '/systemTools/memo', method: 'put', headers: { repeatSubmit: false }, data })
}

// 删除必须携带当前版本，避免删除确认期间内容被修改。
export function delMemo(memoId, version) {
  return request({ url: '/systemTools/memo/' + memoId, method: 'delete', params: { version } })
}

