import request from '@/utils/request'

export function listFile(query) {
  return request({
    url: '/systemTools/fileManagement/list',
    method: 'get',
    params: query
  })
}

export function getFile(id) {
  return request({
    url: '/systemTools/fileManagement/' + id,
    method: 'get'
  })
}

export function getUploadConfig() {
  return request({
    url: '/systemTools/fileManagement/upload/config',
    method: 'get'
  })
}

export function delFile(ids) {
  return request({
    url: '/systemTools/fileManagement/' + ids,
    method: 'delete'
  })
}
