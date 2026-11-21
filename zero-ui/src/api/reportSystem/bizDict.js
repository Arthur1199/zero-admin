/**
 * 业务字典 API：前端统一调用 admin 代理路径，真实数据由 report 维护；字典组全量选项与分页列表分别查询。
 */
import request from '@/utils/request'

// 查询业务字典组列表
export function listDictGroup(query) {
  return request({
    url: '/reportSystem/bizDict/group/list',
    method: 'get',
    params: query
  })
}

// 查询业务字典组下拉列表
export function listDictGroupOptions() {
  return request({
    url: '/reportSystem/bizDict/group/options',
    method: 'get'
  })
}

// 查询业务字典组详情
export function getDictGroup(id) {
  return request({
    url: '/reportSystem/bizDict/group/' + id,
    method: 'get'
  })
}

// 新增业务字典组
export function addDictGroup(data) {
  return request({
    url: '/reportSystem/bizDict/group',
    method: 'post',
    data: data
  })
}

// 修改业务字典组
export function updateDictGroup(data) {
  return request({
    url: '/reportSystem/bizDict/group',
    method: 'put',
    data: data
  })
}

// 删除业务字典组
export function delDictGroup(ids) {
  return request({
    url: '/reportSystem/bizDict/group/' + ids,
    method: 'delete'
  })
}

// 恢复业务字典组
export function restoreDictGroup(ids) {
  return request({
    url: '/reportSystem/bizDict/group/restore/' + ids,
    method: 'put'
  })
}

// 查询业务字典项列表
export function listDictItem(query) {
  return request({
    url: '/reportSystem/bizDict/item/list',
    method: 'get',
    params: query
  })
}

// 查询业务字典项详情
export function getDictItem(id) {
  return request({
    url: '/reportSystem/bizDict/item/' + id,
    method: 'get'
  })
}

// 新增业务字典项
export function addDictItem(data) {
  return request({
    url: '/reportSystem/bizDict/item',
    method: 'post',
    data: data
  })
}

// 修改业务字典项
export function updateDictItem(data) {
  return request({
    url: '/reportSystem/bizDict/item',
    method: 'put',
    data: data
  })
}

// 删除业务字典项
export function delDictItem(ids) {
  return request({
    url: '/reportSystem/bizDict/item/' + ids,
    method: 'delete'
  })
}

// 恢复业务字典项
export function restoreDictItem(ids) {
  return request({
    url: '/reportSystem/bizDict/item/restore/' + ids,
    method: 'put'
  })
}
