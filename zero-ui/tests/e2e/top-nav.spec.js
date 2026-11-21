import { expect, test } from '@playwright/test'
import { authenticated, mockAdminWebSocket } from './mock-api'

test('TopNav 按实际宽度折叠，并同步顶部高亮与侧栏', async ({ page }) => {
  const category = (path, title, childPath, childTitle, component) => ({
    path, component: 'Layout', meta: { title }, children: [
      { path: childPath, name: path + '/' + childPath, component, meta: { title: childTitle } }
    ]
  })
  const external = 'https://example.test/'
  const menus = [
    category('/system', '系统管理', 'role', '角色管理', 'system/role/index'),
    category('/monitor', '系统监控', 'operlog', '操作日志', 'monitor/operlog/index'),
    ...Array.from({ length: 5 }, (_, index) => category('/group-' + index, '扩展管理菜单 ' + index, 'logs', '扩展日志 ' + index, 'monitor/operlog/index')),
    { path: external, meta: { title: '零号官网' } }
  ]
  await mockAdminWebSocket(page)
  await authenticated(page, { menus })
  await page.context().route(external, route => route.fulfill({ contentType: 'text/html', body: '<title>模拟外链</title>' }))
  await page.addInitScript(() => {
    localStorage.setItem('layout-setting', JSON.stringify({ topNav: true, themeStyle: 'glassmorphism' }))
  })
  await page.setViewportSize({ width: 1800, height: 940 })
  await page.goto('/system/role')

  const nav = page.locator('#topmenu-container')
  const sidebar = page.locator('.sidebar-container')
  const more = nav.getByText('更多', { exact: true })
  const active = nav.locator('> .el-menu-item.is-active')
  await expect(nav.locator('> .el-menu-item')).toHaveCount(menus.length)
  await expect(more).toHaveCount(0)
  await expect(nav.locator('.el-sub-menu__icon-arrow')).toHaveCount(0)
  await expect(page.locator('.top-nav-measure')).toHaveCSS('opacity', '0')
  await expect(active).toHaveText('系统管理')
  await expect(nav).toHaveCSS('border-bottom-width', '0px')
  await expect(active).toHaveCSS('border-bottom-width', '4px')
  await expect(sidebar.getByText('角色管理', { exact: true })).toBeVisible()
  const website = nav.getByRole('menuitem', { name: '零号官网' })
  const [externalPage] = await Promise.all([page.context().waitForEvent('page'), website.click()])
  await expect(externalPage).toHaveURL(external)
  await externalPage.close()
  await page.bringToFront()
  await page.mouse.move(0, 0)
  await website.focus()
  await expect(website).toBeFocused()
  await expect(website).toHaveCSS('background-color', 'rgba(0, 0, 0, 0)')
  await expect(active).toHaveText('系统管理')

  await nav.getByText('系统监控', { exact: true }).click()
  await expect(active).toHaveText('系统监控')
  const operationLogs = sidebar.getByText('操作日志', { exact: true })
  await expect(operationLogs).toBeVisible()
  await expect(page).toHaveURL(/\/system\/role$/)
  await operationLogs.click()
  await expect(page).toHaveURL(/\/monitor\/operlog$/)
  await page.goBack()
  await expect(active).toHaveText('系统管理')

  await page.setViewportSize({ width: 1050, height: 940 })
  await expect(more).toBeVisible()
  await expect(nav.locator('.top-nav-more .el-sub-menu__icon-arrow')).toBeVisible()
  await expect(nav.getByText('零号官网', { exact: true })).toHaveCount(0)
  await more.hover()
  await page.getByRole('menuitem', { name: '扩展管理菜单 4' }).click()
  await expect(nav.locator('> .top-nav-more')).toHaveClass(/is-active/)
  await expect(sidebar.getByText('扩展日志 4', { exact: true })).toBeVisible()

  await page.setViewportSize({ width: 1800, height: 940 })
  await expect(more).toHaveCount(0)
  await expect(active).toHaveText('扩展管理菜单 4')
})
