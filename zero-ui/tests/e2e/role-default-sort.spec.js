import { test, expect } from '@playwright/test'
import { authenticated } from './mock-api'

test('新增顺序使用服务端全量建议值、可人工修改并在再次打开时重新获取', async ({ page }) => {
  await authenticated(page, { roles: ['common'], permissions: ['system:role:list', 'system:role:add'] })
  let suggestion = 7
  const writes = []
  // 当前页只有顺序2，建议值仍来自后端的7，不能使用当前页的最大值。
  await page.route('**/dev-api/system/role/list?**', route => route.fulfill({ json: { code: 200, rows: [{ roleId: 2, roleName: '已有角色', roleSort: 2, status: '0' }], total: 30 } }))
  await page.route('**/dev-api/system/role/nextSort', route => route.fulfill({ json: { code: 200, data: suggestion } }))
  await page.route('**/dev-api/system/role', route => {
    writes.push(route.request().postDataJSON())
    return route.fulfill({ json: { code: 200 } })
  })
  await page.goto('/system/role')
  await page.getByRole('button', { name: '新增', exact: true }).click()
  const dialog = page.getByRole('dialog', { name: '添加角色', exact: true })
  const input = dialog.getByRole('spinbutton')
  await expect(input).toHaveValue('7')
  await input.fill('12')
  await dialog.getByPlaceholder('请输入角色名称', { exact: true }).fill('模拟角色')
  await dialog.getByPlaceholder('请输入角色英文名称', { exact: true }).fill('mock_sort')
  await dialog.locator('.el-select').click()
  await page.getByRole('option', { name: '全部数据权限', exact: true }).click()
  await dialog.getByRole('button', { name: '确 定', exact: true }).click()
  await expect(dialog).toBeHidden()
  expect(writes).toHaveLength(1)
  expect(writes[0].roleSort).toBe(12)
  suggestion = 13
  await page.getByRole('button', { name: '新增', exact: true }).click()
  await expect(input).toHaveValue('13')
})

test('默认顺序读取失败不打开残缺表单，重试后回显正确值', async ({ page }) => {
  const requests = await authenticated(page)
  let failed = true
  await page.route('**/dev-api/system/role/nextSort', route => route.fulfill({ json: failed ? { code: 500, msg: '模拟顺序读取失败' } : { code: 200, data: 9 } }))
  await page.goto('/system/role')
  await page.getByRole('button', { name: '新增', exact: true }).click()
  await expect(page.getByText('模拟顺序读取失败', { exact: true })).toBeVisible()
  const dialog = page.getByRole('dialog', { name: '添加角色', exact: true })
  await expect(dialog).toBeHidden()
  failed = false
  await page.getByRole('button', { name: '新增', exact: true }).click()
  await expect(dialog.getByRole('spinbutton')).toHaveValue('9')
  expect(requests.filter(request => request.method !== 'GET')).toEqual([])
})
