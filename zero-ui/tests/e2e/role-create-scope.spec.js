import { test, expect } from '@playwright/test'
import { authenticated } from './mock-api'

const scopes = ['全部数据权限', '自定义数据权限', '本部门数据权限', '本部门及以下数据权限', '仅本人数据权限']
const departments = [{ id: 200, label: '公司', children: [{ id: 201, label: '研发部' }, { id: 202, label: '测试部' }] }]

async function open(page, options = {}) {
  const requests = await authenticated(page, { roles: ['common'], permissions: ['system:role:list', 'system:role:add'] })
  await page.route('**/dev-api/system/role/list?**', route => route.fulfill({ json: { code: 200, rows: [], total: 0 } }))
  await page.route('**/dev-api/system/role/deptTree', route => options.departments ? options.departments(route)
    : route.fulfill({ json: { code: 200, data: departments } }))
  await page.goto('/system/role')
  await page.getByRole('button', { name: '新增', exact: true }).click()
  const dialog = page.getByRole('dialog', { name: '添加角色', exact: true })
  await expect(dialog).toBeVisible()
  await dialog.getByPlaceholder('请输入角色名称', { exact: true }).fill('模拟角色')
  await dialog.getByPlaceholder('请输入角色英文名称', { exact: true }).fill('mock_scope_role')
  return { dialog, requests }
}

async function select(dialog, page, scope) {
  await dialog.locator('.el-select').click()
  await page.getByRole('option', { name: scopes[Number(scope) - 1], exact: true }).click()
}

for (const scope of ['1', '2', '3', '4', '5']) {
  test('新增角色明确提交数据范围 ' + scope, async ({ page }) => {
    const errors = [], writes = []
    page.on('pageerror', error => errors.push(error.message))
    const { dialog } = await open(page)
    await page.route('**/dev-api/system/role', async route => {
      writes.push(route.request().postDataJSON())
      await new Promise(resolve => setTimeout(resolve, 300))
      return route.fulfill({ json: { code: 200 } })
    })
    await select(dialog, page, scope)
    if (scope === '2') {
      await dialog.locator('.el-tree-node__content').filter({ hasText: '研发部' }).locator('.el-checkbox').click()
    }
    await dialog.getByRole('button', { name: '确 定', exact: true }).click()
    await expect(dialog).toBeHidden()
    expect(writes).toHaveLength(1)
    expect(writes[0]).toMatchObject({ dataScope: scope, deptIds: scope === '2' ? [201] : [] })
    expect(errors).toEqual([])
  })
}

test('自定范围必须选部门，读取和保存失败可重试且保留勾选', async ({ page }) => {
  let failTree = true, failSave = true
  const writes = []
  const { dialog } = await open(page, { departments: route => route.fulfill({ json: failTree
    ? { code: 500, msg: '模拟部门读取失败' } : { code: 200, data: departments } }) })
  await page.route('**/dev-api/system/role', route => {
    writes.push(route.request().postDataJSON())
    return route.fulfill({ json: failSave ? { code: 500, msg: '模拟角色保存失败' } : { code: 200 } })
  })
  await select(dialog, page, '2')
  await expect(page.getByText('模拟部门读取失败', { exact: true })).toBeVisible()
  await dialog.getByRole('button', { name: '确 定', exact: true }).click()
  await expect(page.getByText('请先加载部门信息', { exact: true })).toBeVisible()
  expect(writes).toHaveLength(0)
  failTree = false
  await dialog.getByRole('button', { name: '重新加载部门', exact: true }).click()
  await expect(dialog.getByText('研发部', { exact: true })).toBeVisible()
  await dialog.getByRole('button', { name: '确 定', exact: true }).click()
  await expect(page.getByText('请至少选择一个部门', { exact: true })).toBeVisible()
  expect(writes).toHaveLength(0)
  const checkbox = dialog.locator('.el-tree-node__content').filter({ hasText: '研发部' }).getByRole('checkbox')
  await dialog.locator('.el-tree-node__content').filter({ hasText: '研发部' }).locator('.el-checkbox').click()
  await dialog.getByRole('button', { name: '确 定', exact: true }).click()
  await expect(page.getByText('模拟角色保存失败', { exact: true })).toBeVisible()
  await expect(checkbox).toBeChecked()
  await expect(dialog).toBeVisible()
  await page.waitForTimeout(1100)
  failSave = false
  await dialog.getByRole('button', { name: '确 定', exact: true }).click()
  await expect(dialog).toBeHidden()
  expect(writes).toHaveLength(2)
  expect(writes[0]).toEqual(writes[1])
})

test('切换范围和重新打开不携带旧部门；部门慢响应不污染新草稿', async ({ page }) => {
  let release
  const gate = new Promise(resolve => { release = resolve })
  const writes = []
  const { dialog } = await open(page, { departments: async route => {
    await gate
    await route.fulfill({ json: { code: 200, data: departments } })
  } })
  await page.route('**/dev-api/system/role', route => {
    writes.push(route.request().postDataJSON())
    return route.fulfill({ json: { code: 200 } })
  })
  await select(dialog, page, '2')
  await select(dialog, page, '5')
  release()
  await expect(dialog.getByText('研发部', { exact: true })).toHaveCount(0)
  await dialog.getByRole('button', { name: '确 定', exact: true }).click()
  await expect(dialog).toBeHidden()
  expect(writes[0]).toMatchObject({ dataScope: '5', deptIds: [] })
  await page.getByRole('button', { name: '新增', exact: true }).click()
  await expect(dialog.locator('.el-select')).toContainText('全部数据权限')
  await expect(dialog.getByText('研发部', { exact: true })).toHaveCount(0)
})

test('四个代表主题下新增自定部门树占满表单行', async ({ page }, testInfo) => {
  const { dialog } = await open(page)
  await select(dialog, page, '2')
  await expect(dialog.getByText('研发部', { exact: true })).toBeVisible()
  for (const theme of ['glassmorphism', 'dark-premium', 'minimalist-future', 'default']) {
    // 只切换body主题检查弹层；实际主题切换会重建页面并清空当前草稿。
    await page.evaluate(theme => {
      document.body.className = document.body.className.replace(/\btheme-\S+/g, '').trim()
      if (theme !== 'default') document.body.classList.add('theme-' + theme)
    }, theme)
    const tree = dialog.locator('.el-tree').nth(1)
    expect(await tree.evaluate(el => el.clientWidth > 250)).toBe(true)
    await dialog.screenshot({ path: 'output/playwright/role-create-scope-' + testInfo.project.name + '-' + theme + '.png' })
  }
})

for (const scope of ['1', '2', '3', '4', '5']) {
  test('修改角色回显部门并保存数据范围 ' + scope, async ({ page }) => {
    const writes = []
    await authenticated(page)
    const role = { roleId: 2, roleName: '模拟角色', roleKey: 'mock_role', roleSort: 2, status: '0', dataScope: '2', menuCheckStrictly: true, deptCheckStrictly: true }
    await page.route('**/dev-api/system/role/list?**', route => route.fulfill({ json: { code: 200, rows: [role], total: 1 } }))
    await page.route('**/dev-api/system/role/2', route => route.fulfill({ json: { code: 200, data: role } }))
    await page.route('**/dev-api/system/menu/roleMenuTreeselect/2', route => route.fulfill({ json: { code: 200, menus: [], checkedKeys: [] } }))
    await page.route('**/dev-api/system/role/deptTree/2', route => route.fulfill({ json: { code: 200, depts: departments, checkedKeys: [201] } }))
    await page.route('**/dev-api/system/role', route => {
      writes.push({ method: route.request().method(), ...route.request().postDataJSON() })
      return route.fulfill({ json: { code: 200 } })
    })
    await page.goto('/system/role')
    await page.locator('.el-table__row').getByRole('button', { name: '修改', exact: true }).click()
    const dialog = page.getByRole('dialog', { name: '修改角色', exact: true })
    await expect(dialog.locator('.el-select')).toContainText('自定义数据权限')
    const tree = dialog.getByRole('tree').nth(1)
    await expect(tree.getByRole('treeitem', { name: '研发部', exact: true }).getByRole('checkbox')).toBeChecked()
    if (scope === '2') await tree.locator('.el-tree-node__content').filter({ hasText: '测试部' }).locator('.el-checkbox').click()
    else await select(dialog, page, scope)
    await dialog.getByRole('button', { name: '确 定', exact: true }).click()
    await expect(dialog).toBeHidden()
    expect(writes).toHaveLength(1)
    expect(writes[0]).toMatchObject({ method: 'PUT', roleId: 2, dataScope: scope })
    expect([...writes[0].deptIds].sort()).toEqual(scope === '2' ? [200, 201, 202] : [])
  })
}
