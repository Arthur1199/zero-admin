import { expect, test } from '@playwright/test'
import { authenticated, mockAdminWebSocket } from './mock-api'

test('毛玻璃确认框样式统一，系统通知顶部提示与右侧卡片可本地预览', async ({ page }) => {
  const menus = [{ path: '/system', component: 'Layout', meta: { title: '系统管理' }, children: [
    { path: 'role', name: 'Role', component: 'system/role/index', meta: { title: '角色管理' } }
  ] }, { path: '/systemTools', component: 'Layout', meta: { title: '系统工具' }, children: [
    { path: 'systemNotice', name: 'SystemNotice', component: 'systemTools/systemNotice/index', meta: { title: '系统通知' } }
  ] }]
  await mockAdminWebSocket(page)
  await authenticated(page, { menus })
  await page.addInitScript(() => {
    localStorage.setItem('layout-setting', JSON.stringify({ themeStyle: 'glassmorphism', glassmorphismBgStyle: 'ocean-sunset' }))
  })
  await page.route('**/dev-api/system/role/list?**', route => route.fulfill({ json: {
    code: 200, total: 1, rows: [{ roleId: 2, roleName: '模拟普通角色', roleKey: 'common', roleSort: 1, status: '0', dataScope: '4' }]
  } }))
  await page.route('**/dev-api/systemTools/systemNotice/**', route => {
    const action = new URL(route.request().url()).pathname.split('/').at(-1)
    if (action === 'options') return route.fulfill({ json: { code: 200, data: {
      departments: [], roles: [], displayTypes: ['NOTIFICATION', 'MESSAGE'], messageLevels: ['NORMAL']
    } } })
    if (action === 'preview') return route.fulfill({ json: { code: 200, data: { matchedCount: 1 } } })
    if (action === 'onlineUsers') return route.fulfill({ json: { code: 200, rows: [], total: 1 } })
    return route.abort()
  })
  const writes = []
  page.on('request', request => {
    if (request.method() === 'DELETE' || request.url().endsWith('/systemNotice/send')) writes.push(request.url())
  })

  const confirmation = page.getByRole('dialog', { name: '系统提示' })
  const box = confirmation.locator('.el-message-box.zero-confirm-box')
  const surface = () => box.evaluate(element => {
    const base = getComputedStyle(element)
    const layer = getComputedStyle(element, '::before')
    return { background: base.backgroundColor, blur: base.backdropFilter, gradient: layer.backgroundImage, opacity: layer.opacity }
  })

  await page.goto('/system/role')
  await page.getByRole('row', { name: /模拟普通角色/ }).getByRole('button', { name: '删除' }).click()
  await expect(box).toBeVisible()
  await expect(confirmation).toContainText('是否确认删除角色')
  const roleSurface = await surface()
  expect(roleSurface.background).toBe('rgba(31, 48, 65, 0.2)')
  expect(roleSurface.gradient).toContain('rgb(124, 58, 237)')
  expect(Number(roleSurface.opacity)).toBeLessThan(1)
  await confirmation.getByRole('button', { name: '取消' }).click()

  await page.goto('/systemTools/systemNotice')
  await page.getByPlaceholder('请输入通知标题').fill('模拟主题确认')
  await page.locator('.system-notice-editor .ql-editor').fill('模拟通知正文')
  await page.getByRole('button', { name: '发送通知' }).click()
  await expect(confirmation).toContainText('当前匹配 1 个在线账号')
  expect(await surface()).toEqual(roleSurface)
  await confirmation.getByRole('button', { name: '取消' }).click()
  await expect(confirmation).toBeHidden()

  const expectGlassNotice = async notice => {
    await expect(notice).toBeVisible()
    await expect(notice).toHaveCSS('background-color', 'rgba(21, 32, 59, 0.6)')
    await expect(notice).toHaveCSS('backdrop-filter', 'blur(24px) saturate(1.2)')
    await expect(notice).toHaveCSS('color', 'rgb(255, 255, 255)')
  }
  const preview = page.getByRole('button', { name: '本地预览' })
  await preview.click()
  const card = page.locator('.el-notification.admin-ws-notice-card')
  await expectGlassNotice(card)
  await card.getByRole('button', { name: '查看详情' }).click()
  const details = page.locator('.admin-ws-notice-dialog-overlay .el-dialog')
  await expect(details).toContainText('模拟通知正文')
  await expect(card).toBeHidden()
  await details.locator('.el-dialog__headerbtn').click()
  await expect(details).toBeHidden()

  await page.locator('.el-form-item').filter({ hasText: '弹窗形式' }).locator('.el-select__wrapper').click()
  await page.getByRole('option', { name: '顶部提示', exact: true }).click()
  await preview.click()
  await expectGlassNotice(page.locator('.el-message.admin-ws-notice-message'))
  expect(writes).toEqual([])
})
