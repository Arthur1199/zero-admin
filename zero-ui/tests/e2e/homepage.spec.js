import { test, expect } from '@playwright/test'
import { authenticated } from './mock-api'

for (const path of ['/', '/index']) {
  test('首页直接访问及刷新 ' + path, async ({ page }) => {
    const errors = []
    page.on('pageerror', error => errors.push(error.message))
    await authenticated(page)
    await page.goto(path)
    await expect(page.locator('.zero-home')).toBeVisible()
    await expect(page.getByRole('heading', { name: 'Zero管理框架', exact: true })).toBeVisible()
    await expect(page.locator('#loader-wrapper')).toHaveCount(0)
    await page.reload()
    await expect(page.locator('.zero-home')).toBeVisible()
    expect(errors).toEqual([])
  })
}

test('外链和嵌套菜单不会阻断首页登录及刷新', async ({ page }) => {
  const errors = []
  page.on('pageerror', error => errors.push(error.message))
  await authenticated(page, { menus: [
    { path: 'https://example.com', name: 'External', component: 'Layout', meta: { title: '外部链接' } },
    { path: '/monitor', name: 'Monitor', component: 'Layout', meta: { title: '系统监控' }, children: [
      { path: 'cluster', component: 'ParentView', meta: { title: '集群' }, children: [
        { path: 'http://127.0.0.1:9100', component: 'ParentView', meta: { title: '外部监控' } },
        { path: 'server', name: 'Server', component: 'monitor/server/index', meta: { title: '服务监控' } }
      ] }
    ] }
  ] })
  await page.goto('/index')
  await expect(page.locator('.zero-home')).toBeVisible()
  await expect(page.locator('.sidebar-container a[href="https://example.com"]')).toBeVisible()
  await page.goto('/monitor/cluster/server')
  await expect(page.locator('.app-main .el-card').first()).toBeVisible()
  await page.reload()
  await expect(page.locator('.app-main .el-card').first()).toBeVisible()
  expect(errors).toEqual([])
})
