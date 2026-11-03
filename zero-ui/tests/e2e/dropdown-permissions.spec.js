import { test, expect } from '@playwright/test'
import { authenticated } from './mock-api'

test('用户更多菜单按独立权限显示，不泄露重置密码入口', async ({ page }) => {
  await authenticated(page, { roles: ['common'], permissions: ['system:user:list', 'system:user:edit'] })
  await page.route('**/dev-api/system/user/list?**', route => route.fulfill({ json: {
    code: 200, rows: [{ userId: 2, userName: 'mock-user', nickName: '测试用户', status: '0', dept: {} }], total: 1
  } }))
  await page.goto('/system/user')
  await page.getByRole('button', { name: '更多', exact: true }).click()
  await expect(page.getByRole('menuitem', { name: '分配角色', exact: true })).toBeVisible()
  await expect(page.getByRole('menuitem', { name: '重置密码', exact: true })).toHaveCount(0)
})

test('任务查询权限允许查看详情，隐藏执行一次', async ({ page }) => {
  await authenticated(page, { roles: ['common'], permissions: ['monitor:job:list', 'monitor:job:query'] })
  await page.route('**/dev-api/monitor/job/list?**', route => route.fulfill({ json: {
    code: 200, rows: [{ jobId: 2, jobName: '测试任务', jobGroup: 'DEFAULT', invokeTarget: 'mockTask.run()', cronExpression: '0/10 * * * * ?', status: '1' }], total: 1
  } }))
  await page.goto('/monitor/job')
  await page.getByRole('button', { name: '更多', exact: true }).click()
  await expect(page.getByRole('menuitem', { name: '任务详细', exact: true })).toBeVisible()
  await expect(page.getByRole('menuitem', { name: '调度日志', exact: true })).toBeVisible()
  await expect(page.getByRole('menuitem', { name: '执行一次', exact: true })).toHaveCount(0)
})

for (const [path, apiPath, permission, row] of [
  ['/system/role', '/system/role/list', 'system:role:list', { roleId: 2, roleName: '测试角色', roleKey: 'mock-role', roleSort: 1, status: '0' }],
  ['/monitor/job', '/monitor/job/list', 'monitor:job:list', { jobId: 2, jobName: '测试任务', jobGroup: 'DEFAULT', invokeTarget: 'mockTask.run()', cronExpression: '0/10 * * * * ?', status: '1' }]
]) {
  test('只有查询权限时隐藏更多操作 ' + path, async ({ page }) => {
    await authenticated(page, { roles: ['common'], permissions: [permission] })
    await page.route('**/dev-api' + apiPath + '?**', route => route.fulfill({ json: { code: 200, rows: [row], total: 1 } }))
    await page.goto(path)
    await expect(page.locator('.el-table__row')).toHaveCount(1)
    await expect(page.getByRole('button', { name: '更多', exact: true })).toHaveCount(0)
  })
}
