import { test, expect } from '@playwright/test'
import { authenticated } from './mock-api'

test('树形选择支持父节点、子节点和数字ID', async ({ page }) => {
  const requests = await authenticated(page)
  await page.goto('/system/dept')
  await page.getByRole('button', { name: '新增', exact: true }).first().click()
  const dialog = page.locator('.el-dialog')
  await dialog.locator('.zero-tree-select').click()
  await page.locator('.el-select-dropdown').getByText('测试部门', { exact: true }).click()
  await dialog.getByPlaceholder('请输入部门名称').fill('新部门')
  await dialog.getByRole('spinbutton').fill('1')
  await dialog.getByRole('button', { name: /确\s*定/ }).click()
  await expect.poll(() => requests.some(request => request.path === '/system/dept' && request.method === 'POST')).toBe(true)
  expect(JSON.parse(requests.find(request => request.path === '/system/dept' && request.method === 'POST').body).parentId).toBe(100)
})

test('头像裁剪窗口可以打开和旋转缩放', async ({ page }) => {
  const errors = []; page.on('pageerror', error => errors.push(error.message))
  await authenticated(page)
  await page.goto('/user/profile')
  await page.locator('.user-info-head').click()
  const dialog = page.locator('.el-dialog')
  await expect(dialog.locator('.vue-cropper')).toBeVisible()
  // Cropper使用初始本地头像，不需要真实上传。
  await dialog.getByRole('button', { name: '向左旋转' }).click()
  await dialog.getByRole('button', { name: '向右旋转' }).click()
  await dialog.getByRole('button', { name: '放大' }).click()
  await dialog.getByRole('button', { name: '缩小' }).click()
  await dialog.locator('.el-dialog__headerbtn').click()
  expect(errors).toEqual([])
})

test('Tab缓存、刷新、双击定位和布局尺寸', async ({ page }) => {
  const errors = []; page.on('pageerror', error => errors.push(error.message))
  await authenticated(page)
  await page.goto('/system/config')
  await page.getByPlaceholder('请输入参数名称').fill('缓存测试')
  await page.locator('.sidebar-container').getByText('系统管理', { exact: true }).click()
  await page.locator('.sidebar-container a[href="/system/post"]').click()
  await expect(page).toHaveURL(/\/system\/post$/)
  await page.locator('.tags-view-item').filter({ hasText: 'config' }).click()
  await expect(page.getByPlaceholder('请输入参数名称')).toHaveValue('缓存测试')
  await page.locator('.tags-view-item.active').dblclick()
  await expect(page.locator('.sidebar-container a[href="/system/config"]')).toBeVisible()
  for (const label of ['迷你', '较小', '中等', '默认']) {
    await page.locator('#size-select').click()
    await page.getByRole('menuitem', { name: label, exact: true }).click()
    await expect(page.locator('.el-table')).toBeVisible()
    await expect(page).toHaveURL(/\/system\/config$/)
  }
  expect(errors).toEqual([])
})
