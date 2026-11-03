import { authenticated } from './mock-api'

export async function mockBizDict(page, auth = {}) {
  await authenticated(page, auth)
  const groups = [
    { id: 1, groupCode: 'active', groupName: '正常组', status: '1', delFlag: '0', createTime: '2026-01-10 08:00:00' },
    { id: 2, groupCode: 'deleted', groupName: '已删除组', status: '0', delFlag: '1', createTime: '2026-01-11 08:00:00' }
  ]
  const items = [
    { id: 101, groupId: 1, itemCode: 'first', itemName: '正常项', itemValue: 'a', itemSort: 0, status: '1', delFlag: '0' },
    { id: 102, groupId: 1, itemCode: 'removed', itemName: '已删除项', itemValue: 'b', itemSort: 1, status: '0', delFlag: '1' },
    { id: 201, groupId: 2, itemCode: 'child', itemName: '父级删除子项', itemValue: 'c', itemSort: 0, status: '1', delFlag: '0' },
    { id: 202, groupId: 2, itemCode: 'child-removed', itemName: '父级删除已删子项', itemValue: 'd', itemSort: 1, status: '1', delFlag: '1' }
  ]
  const requests = [], failures = new Map()
  const state = { groups, items, requests, failures }
  await page.route('**/dev-api/reportSystem/bizDict/**', async route => {
    const request = route.request(), url = new URL(request.url())
    const pathname = url.pathname.replace('/dev-api', '')
    const method = request.method(), query = Object.fromEntries(url.searchParams)
    const data = request.postData() ? request.postDataJSON() : null
    requests.push({ path: pathname, method, query, data })
    const failKey = method + ' ' + pathname
    if (failures.get(failKey)) {
      failures.set(failKey, failures.get(failKey) - 1)
      return route.fulfill({ json: { code: 500, msg: '模拟字典请求失败' } })
    }
    const base = '/reportSystem/bizDict/'
    const kind = pathname.includes('/group') ? 'group' : 'item'
    const rows = kind === 'group' ? groups : items
    let result = { code: 200, msg: '操作成功' }
    if (pathname === base + 'group/options') result.data = groups.map(({ id, groupCode, groupName }) => ({ id, groupCode, groupName }))
    else if (pathname.endsWith('/list')) {
      const filtered = rows.filter(row => Object.entries(query).every(([key, value]) => {
        if (!value || ['pageNum', 'pageSize'].includes(key)) return true
        if (key === 'beginTime') return !row.createTime || row.createTime.slice(0, 10) >= value
        if (key === 'endTime') return !row.createTime || row.createTime.slice(0, 10) <= value
        return String(row[key] ?? '').includes(value)
      }))
      const offset = (Number(query.pageNum || 1) - 1) * Number(query.pageSize || 10)
      result = { code: 200, total: filtered.length, rows: filtered.slice(offset, offset + Number(query.pageSize || 10)) }
    } else if (method === 'POST' && pathname === base + kind) {
      rows.push({ ...data, id: Math.max(...rows.map(row => row.id), 0) + 1, delFlag: '0' })
    } else if (method === 'PUT' && pathname === base + kind) {
      Object.assign(rows.find(row => row.id === data.id), data)
    } else {
      const id = Number(pathname.split('/').at(-1)), row = rows.find(item => item.id === id)
      if (method === 'GET') result.data = row
      else if (method === 'DELETE') row.delFlag = '1'
      else if (method === 'PUT' && pathname.includes('/restore/')) row.delFlag = '0'
      else throw new Error('未覆盖的字典测试接口: ' + method + ' ' + pathname)
    }
    return route.fulfill({ json: result })
  })
  return state
}
