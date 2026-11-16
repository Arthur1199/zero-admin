import { test, expect } from '@playwright/test'
import { authenticated } from './mock-api'

test('用户角色按逗号展示，空值兼容且显隐不影响手机状态列', async ({ page }) => {
  const requests = await authenticated(page)
  const rows = [
    { userId: 2, userName: 'mock-one', status: '0', phonenumber: '13800000000', createBy: 'mock-creator', createTime: '2026-11-16 09:00:00', updateBy: 'mock-editor', updateTime: '2026-11-16 10:00:00', roles: [{ roleId: 2, roleName: '普通角色' }, { roleId: 3, roleName: '财务角色' }] },
    { userId: 3, userName: 'mock-two', status: '0', roles: [] },
    { userId: 4, userName: 'mock-three', status: '0' }
  ]
  await page.route('**/dev-api/system/user/list?**', route => route.fulfill({ json: { code: 200, rows, total: 3 } }))
  await page.goto('/system/user')
  const header = page.getByRole('columnheader', { name: '用户角色', exact: true })
  await expect(header).toBeVisible()
  const labels = await page.locator('.el-table__header-wrapper th .cell').allTextContents()
  expect(labels.indexOf('用户角色')).toBe(labels.indexOf('部门') + 1)
  expect(labels.indexOf('手机号码')).toBe(labels.indexOf('用户角色') + 1)
  expect(labels.slice(labels.indexOf('状态') + 1, labels.indexOf('状态') + 6)).toEqual(['删除状态', '创建人', '创建时间', '更新人', '更新时间'])
  const records = page.locator('.el-table__body .el-table__row')
  await expect(records.nth(0)).toContainText('普通角色,财务角色')
  await expect(records.nth(0)).toContainText('mock-creator')
  await expect(records.nth(0)).toContainText('mock-editor')
  await expect(records.nth(0)).toContainText('2026-11-16 09:00:00')
  await expect(records.nth(0)).toContainText('2026-11-16 10:00:00')
  await expect(records.nth(1)).toContainText('—')
  await expect(records.nth(2)).toContainText('—')
  await page.locator('.top-right-btn .el-dropdown .el-button').click()
  const checkbox = page.getByRole('checkbox', { name: '用户角色', exact: true })
  const checkboxLabel = page.getByRole('menuitem', { name: '用户角色', exact: true }).locator('.el-checkbox')
  await checkboxLabel.click()
  await expect(checkbox).not.toBeChecked()
  await expect(header).toHaveCount(0)
  await expect(page.getByRole('columnheader', { name: '手机号码', exact: true })).toBeVisible()
  await expect(page.getByRole('columnheader', { name: '状态', exact: true })).toBeVisible()
  await checkboxLabel.click()
  await expect(checkbox).toBeChecked()
  await expect(header).toBeVisible()
  expect(requests.filter(request => request.method !== 'GET')).toEqual([])
})

test('昵称部门和角色长文本在四代表主题完整换行且表格支持横向滚动', async ({ page }, testInfo) => {
  await authenticated(page)
  const nickName = '较长的用户昵称'.repeat(8)
  const deptName = '研发中心下属业务系统管理部门'.repeat(6)
  const roleNames = ['系统管理员', '财务管理角色', '风险管理角色', '跨部门业务审核角色', 'A'.repeat(80)]
  await page.route('**/dev-api/system/user/list?**', route => route.fulfill({ json: { code: 200, total: 1, rows: [{ userId: 2, userName: 'mock-long', status: '0', nickName, dept: { deptName }, roles: roleNames.map((roleName, index) => ({ roleId: index + 1, roleName })) }] } }))
  await page.goto('/system/user')
  const cells = page.locator('.el-table__body td.user-long-text .cell')
  await expect(cells).toHaveText([nickName, deptName, roleNames.join(',')])
  for (const theme of ['glassmorphism', 'dark-premium', 'minimalist-future', 'default']) {
    await page.evaluate(theme => {
      document.body.className = document.body.className.replace(/\btheme-\S+/g, '').trim()
      if (theme !== 'default') document.body.classList.add('theme-' + theme)
    }, theme)
    for (const cell of await cells.all()) {
      expect(await cell.evaluate(el => el.scrollWidth <= el.clientWidth + 1 && el.scrollHeight <= el.clientHeight + 1)).toBe(true)
      expect(await cell.evaluate(el => el.clientHeight > 40)).toBe(true)
    }
    expect(await page.locator('.el-table__body-wrapper .el-scrollbar__wrap').evaluate(el => el.scrollWidth > el.clientWidth)).toBe(true)
    if (testInfo.project.name === 'chromium') await page.screenshot({ path: 'output/playwright/user-long-text-' + theme + '.png' })
  }
})
