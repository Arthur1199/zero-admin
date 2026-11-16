import { test, expect } from '@playwright/test'
import { authenticated } from './mock-api'

test('删除状态默认空选、可清空和重置，已删除账号仅允许恢复', async ({ page }) => {
  const requests = await authenticated(page, { roles: ['common'], permissions: ['system:user:list', 'system:user:edit', 'system:user:remove', 'system:user:resetPwd'] })
  const queries = []
  await page.route('**/dev-api/system/user/list?**', route => {
    const params = new URL(route.request().url()).searchParams
    queries.push(Object.fromEntries(params))
    const deleted = params.get('delFlag') === '2'
    return route.fulfill({ json: { code: 200, total: 1, rows: [{ userId: 3, userName: deleted ? 'deleted-user' : 'active-user', status: '0', delFlag: deleted ? '2' : '0' }] } })
  })
  await page.goto('/system/user')
  const row = page.locator('.el-table__body .el-table__row')
  await expect(row).toContainText('active-user')
  expect(queries[0].delFlag).toBeUndefined()
  const deletionSelect = page.locator('.el-form-item').filter({ has: page.locator('label', { hasText: '删除状态' }) }).locator('.el-select')
  await deletionSelect.click()
  await page.getByRole('option', { name: '已删除', exact: true }).click()
  await page.getByRole('button', { name: '搜索', exact: true }).click()
  await expect(row).toContainText('deleted-user')
  await expect(row).toContainText('已删除')
  expect(queries.at(-1)).toMatchObject({ delFlag: '2', pageNum: '1' })
  await expect(row.getByRole('switch')).toBeDisabled()
  await expect(row.getByRole('checkbox')).toBeDisabled()
  await expect(row.getByRole('button', { name: '修改', exact: true })).toHaveCount(0)
  await expect(row.getByRole('button', { name: '删除', exact: true })).toHaveCount(0)
  await expect(row.getByRole('button', { name: '更多', exact: true })).toHaveCount(0)
  await expect(row.getByRole('button', { name: '恢复', exact: true })).toBeVisible()
  await expect(page.getByRole('button', { name: '修改', exact: true })).toBeDisabled()
  await expect(page.getByRole('button', { name: '删除', exact: true })).toBeDisabled()
  await deletionSelect.hover()
  await deletionSelect.locator('.el-select__clear').click()
  await page.getByRole('button', { name: '搜索', exact: true }).click()
  await expect(row).toContainText('active-user')
  expect(queries.at(-1).delFlag).toBeUndefined()
  await deletionSelect.click()
  await page.getByRole('option', { name: '已删除', exact: true }).click()
  await page.getByPlaceholder('请输入用户名称').fill('test')
  await page.getByRole('button', { name: '重置', exact: true }).click()
  await expect.poll(() => queries.at(-1).userName).toBeUndefined()
  expect(queries.at(-1).delFlag).toBeUndefined()
  expect(queries.at(-1).pageNum).toBe('1')
  await expect(page.getByPlaceholder('请输入用户名称')).toHaveValue('')
  await expect(row.getByRole('switch')).toBeEnabled()
  await expect(row.getByRole('button', { name: '修改', exact: true })).toBeVisible()
  expect(requests.filter(request => request.method !== 'GET')).toEqual([])
})

test('恢复需确认，失败保留记录，成功刷新删除列表', async ({ page }) => {
  await authenticated(page, { roles: ['common'], permissions: ['system:user:list', 'system:user:remove'] })
  let restored = false, attempts = 0
  await page.route('**/dev-api/system/user/list?**', route => route.fulfill({ json: {
    code: 200, total: restored ? 0 : 1,
    rows: restored ? [] : [{ userId: 3, userName: 'deleted-user', status: '1', delFlag: '2' }]
  } }))
  await page.route('**/dev-api/system/user/restore/3', route => {
    expect(route.request().method()).toBe('PUT')
    attempts++
    restored = attempts > 1
    return route.fulfill({ json: restored ? { code: 200, msg: '操作成功' } : { code: 500, msg: '账号状态或信息已变化，请刷新后重试' } })
  })
  await page.goto('/system/user')
  const restore = page.getByRole('button', { name: '恢复', exact: true })
  await restore.click()
  const confirm = page.locator('.el-message-box')
  await expect(confirm).toContainText('原角色和岗位授权不会自动恢复,需要重新设置')
  await confirm.getByRole('button', { name: '取消', exact: true }).click()
  await expect(restore).toBeEnabled()
  expect(attempts).toBe(0)
  await restore.click()
  await confirm.getByRole('button', { name: '确定', exact: true }).click()
  await expect(page.locator('.el-message--error')).toContainText('账号状态或信息已变化')
  await expect(restore).toBeEnabled()
  await page.waitForTimeout(1100) // 请求封装保留一秒防重复提交。
  await restore.click()
  await confirm.getByRole('button', { name: '确定', exact: true }).click()
  await expect(page.locator('.el-table__body .el-table__row')).toHaveCount(0)
  expect(attempts).toBe(2)
})

test('没有删除权限时不显示恢复按钮', async ({ page }) => {
  await authenticated(page, { roles: ['common'], permissions: ['system:user:list'] })
  await page.route('**/dev-api/system/user/list?**', route => route.fulfill({ json: { code: 200, total: 1, rows: [{ userId: 3, userName: 'deleted-user', status: '0', delFlag: '2' }] } }))
  await page.goto('/system/user')
  await expect(page.locator('.el-table__body .el-table__row')).toContainText('deleted-user')
  await expect(page.getByRole('button', { name: '恢复', exact: true })).toHaveCount(0)
})
