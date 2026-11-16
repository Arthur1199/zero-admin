import { test, expect } from '@playwright/test'
import { authenticated } from './mock-api'

const role = { roleId: 2, roleName: '模拟角色', roleKey: 'mock_role', roleSort: 2, status: '0', dataScope: '2', menuCheckStrictly: true, deptCheckStrictly: true, createBy: 'mock-creator', createTime: '2026-11-16 09:00:00', updateBy: 'mock-editor', updateTime: '2026-11-16 10:00:00' }
const nodes = [{ id: 101, label: '测试部门' }]

async function setup(page, permissions) {
  await authenticated(page, { roles: ['common'], permissions: ['system:role:list', ...permissions] })
  await page.route('**/dev-api/system/role/list?**', route => route.fulfill({ json: { code: 200, rows: [role], total: 1 } }))
  await page.route('**/dev-api/system/role/2', route => route.fulfill({ json: { code: 200, data: role } }))
  await page.route('**/dev-api/system/role/deptTree/2', route => route.fulfill({ json: { code: 200, depts: nodes, checkedKeys: [101] } }))
  await page.route('**/dev-api/system/menu/roleMenuTreeselect/2', route => route.fulfill({ json: { code: 200, menus: nodes, checkedKeys: [101] } }))
  await page.goto('/system/role')
}

test('只有原角色增删改权限时不显示独立入口，普通编辑不提交数据范围', async ({ page }) => {
  const writes = []
  await setup(page, ['system:role:edit', 'system:role:add', 'system:role:remove'])
  await expect(page.getByRole('columnheader', { name: '创建人', exact: true })).toBeVisible()
  const labels = await page.locator('.el-table__header-wrapper th .cell').allTextContents()
  expect(labels.slice(labels.indexOf('状态') + 1, labels.indexOf('状态') + 5)).toEqual(['创建人', '创建时间', '更新人', '更新时间'])
  await expect(page.locator('.el-table__row')).toContainText('mock-creator')
  await expect(page.locator('.el-table__row')).toContainText('mock-editor')
  await expect(page.locator('.el-table__row')).toContainText('2026-11-16 09:00:00')
  await expect(page.locator('.el-table__row')).toContainText('2026-11-16 10:00:00')
  await expect(page.getByRole('button', { name: '更多', exact: true })).toHaveCount(0)
  await page.route('**/dev-api/system/role', route => {
    writes.push(route.request().postDataJSON())
    return route.fulfill({ json: { code: 200 } })
  })
  await page.locator('.el-table__row').getByRole('button', { name: '修改', exact: true }).click()
  const dialog = page.getByRole('dialog', { name: '修改角色', exact: true })
  await expect(dialog.locator('.el-select')).toHaveCount(0)
  await expect(dialog.getByRole('tree')).toHaveCount(1)
  await dialog.getByRole('button', { name: '确 定', exact: true }).click()
  await expect(dialog).toBeHidden()
  expect(writes).toHaveLength(1)
  for (const name of ['dataScope', 'deptIds', 'deptCheckStrictly']) expect(writes[0]).not.toHaveProperty(name)
})

test('只有数据权限时可回显保存，不依赖修改和查询权限，也不显示分配用户', async ({ page }) => {
  const writes = []
  await setup(page, ['system:role:dataScope'])
  await page.getByRole('button', { name: '更多', exact: true }).click()
  await expect(page.getByRole('menuitem', { name: '分配用户', exact: true })).toHaveCount(0)
  await page.getByRole('menuitem', { name: '数据权限', exact: true }).click()
  const dialog = page.getByRole('dialog', { name: '分配数据权限', exact: true })
  await expect(dialog.getByRole('treeitem', { name: '测试部门', exact: true }).getByRole('checkbox')).toBeChecked()
  await page.route('**/dev-api/system/role/dataScope', route => {
    writes.push(route.request().postDataJSON())
    return route.fulfill({ json: { code: 200 } })
  })
  await dialog.getByRole('button', { name: '确 定', exact: true }).click()
  await expect(dialog).toBeHidden()
  expect(writes).toHaveLength(1)
  expect(writes[0].deptIds).toContain(101)
})

test('只有分配用户权限时可进入页面并使用添加取消按钮，不依赖角色新增删除', async ({ page }) => {
  await setup(page, ['system:role:authUser'])
  await page.route('**/dev-api/system/role/authUser/allocatedList?**', route => route.fulfill({ json: { code: 200, rows: [{ userId: 3, userName: 'mock-user', status: '0' }], total: 1 } }))
  await page.getByRole('button', { name: '更多', exact: true }).click()
  await expect(page.getByRole('menuitem', { name: '数据权限', exact: true })).toHaveCount(0)
  await page.getByRole('menuitem', { name: '分配用户', exact: true }).click()
  await expect(page).toHaveURL(/\/system\/role-auth\/user\/2/)
  await expect(page.getByRole('button', { name: '添加用户', exact: true })).toBeVisible()
  await expect(page.getByRole('button', { name: '批量取消授权', exact: true })).toBeVisible()
  await expect(page.getByRole('button', { name: '取消授权', exact: true })).toBeVisible()
  await page.getByRole('button', { name: '添加用户', exact: true }).click()
  await expect(page.getByRole('dialog', { name: '选择用户', exact: true }).getByRole('button', { name: '确 定', exact: true })).toBeVisible()
})

test('同时拥有两项独立权限时两个更多入口都显示', async ({ page }) => {
  await setup(page, ['system:role:dataScope', 'system:role:authUser'])
  await page.getByRole('button', { name: '更多', exact: true }).click()
  await expect(page.getByRole('menuitem', { name: '数据权限', exact: true })).toBeVisible()
  await expect(page.getByRole('menuitem', { name: '分配用户', exact: true })).toBeVisible()
})
