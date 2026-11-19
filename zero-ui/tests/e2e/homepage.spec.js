import { test, expect } from '@playwright/test'
import { authenticated, mockApi } from './mock-api'
import { WebSocketMessageType } from '../../src/utils/websocketMessageType'

test.beforeEach(async ({ context }) => {
  // 模拟会话只在测试中有效，避免页面连接开发代理后的真实 WebSocket 并被注销。
  await context.routeWebSocket('**/ws/admin', socket => {
    socket.onMessage(() => socket.send(JSON.stringify({
      msgType: { messageType: WebSocketMessageType.CREATE_CONNECTION },
      msgData: { heartbeatInterval: 30000, idleTimeout: 60000 }
    })))
  })
})

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

test('新版首页保留横幅和系统简介，主题预览仍可独立打开', async ({ page }) => {
  await authenticated(page)
  await page.goto('/index')
  await expect(page.locator('.hero-section')).toContainText('免费开源')
  await expect(page.locator('.intro-section')).toContainText('软件作者：Arthur')
  await expect(page.getByRole('heading', { name: '项目亮点与功能' })).toBeVisible()
  await expect(page.getByRole('heading', { name: '技术框架' })).toBeVisible()

  const themeLink = page.getByRole('link', { name: '在新标签页预览毛玻璃拟态风' })
  await expect(themeLink).toHaveAttribute('href', '/theme-preview-glass')
  await expect(themeLink).toHaveAttribute('target', '_blank')
})

test('八张主题样卡按设置顺序排列，原始风格可在新标签页预览', async ({ page, context }) => {
  await mockApi(context)
  await authenticated(page)
  await page.goto('/index')
  const themes = page.locator('.home-themes a')
  await expect(themes).toHaveText(['毛玻璃拟态风', '轻奢商务风', '活力科技风', '极简未来风', '温馨自然风', '质感设计风', '高端暗黑风', '原始风格'])
  await expect(page.locator('.home-themes a[target="_blank"] .home-theme-sample')).toHaveCount(8)

  const [preview] = await Promise.all([
    page.waitForEvent('popup'),
    themes.last().click()
  ])
  await expect(preview).toHaveURL(/\/theme-preview-default$/)
  await expect(preview.getByRole('heading', { name: '原始风格预览' })).toBeVisible()
  await expect(page).toHaveURL(/\/index$/)
  await preview.getByRole('textbox', { name: '用户名称' }).fill('示例访客')
  await preview.getByRole('button', { name: '查询' }).click()
  await expect(preview.getByRole('row')).toHaveCount(2)
})
