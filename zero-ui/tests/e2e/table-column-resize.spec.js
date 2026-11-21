import { test, expect } from '@playwright/test'
import { authenticated } from './mock-api'
import { WebSocketMessageType } from '../../src/utils/websocketMessageType'

test.beforeEach(async ({ page }) => {
  await page.routeWebSocket('**/ws/admin', socket => {
    socket.onMessage(() => socket.send(JSON.stringify({
      msgType: { messageType: WebSocketMessageType.CREATE_CONNECTION },
      msgData: { heartbeatInterval: 30000, idleTimeout: 60000 }
    })))
  })
  await authenticated(page)
})

async function resizeColumn(page, header, distance) {
  await expect(page.locator('.el-loading-mask:visible, .app-main .fade-transform-enter-active')).toHaveCount(0)
  await expect(header).toBeVisible()
  await header.scrollIntoViewIfNeeded()
  const before = await header.boundingBox()
  const position = { x: Math.floor(before.width) - 4, y: Math.floor(before.height / 2) }
  await header.hover({ position })
  await expect(page.locator('body')).toHaveCSS('cursor', 'col-resize')
  const target = await header.boundingBox()
  await page.mouse.down()
  await page.mouse.move(target.x + position.x + distance, target.y + position.y, { steps: 4 })
  await page.mouse.up()
  await expect.poll(async () => Math.round((await header.boundingBox()).width))
    .toBe(Math.round(before.width + distance))
}

test('用户管理和文件管理的表头可以左右调节列宽', async ({ page }) => {
  for (const [path, label] of [['/system/user', '用户编号'], ['/systemTools/fileManagement', '文件ID']]) {
    await page.goto(path)
    const header = page.getByRole('columnheader', { name: label, exact: true })
    await resizeColumn(page, header, 64)
    await resizeColumn(page, header, -40)
  }
})

test('角色管理在八套主题下都可以拖动表头分隔线', async ({ page }) => {
  test.setTimeout(60000)
  const themes = ['glassmorphism', 'dark-premium', 'minimalist-future', 'default',
    'luxury-business', 'vibrant-tech', 'warm-nature', 'material-design']
  await page.goto('/system/role')
  const header = page.getByRole('columnheader', { name: '角色编号', exact: true })
  for (const themeStyle of themes) {
    await page.evaluate(themeStyle => {
      localStorage.setItem('layout-setting', JSON.stringify({ themeStyle }))
    }, themeStyle)
    await page.reload()
    if (themeStyle !== 'default') {
      await expect(page.locator('body')).toHaveClass(new RegExp('theme-' + themeStyle))
    }
    await resizeColumn(page, header, 64)
  }
})

test('服务监控的 CPU、内存和磁盘表可以调整列宽并保留数据', async ({ page }) => {
  await page.route('**/dev-api/monitor/server', route => route.fulfill({ json: {
    code: 200,
    data: {
      cpu: { cpuNum: 8, used: 30, sys: 20, free: 50 },
      mem: { total: 16, used: 14, free: 2, usage: 87.5 },
      jvm: { total: 1024, used: 128, free: 896, usage: 12.5 },
      sysFiles: [{ dirName: '/', sysTypeName: 'ext4', typeName: 'local', total: '64 GB', free: '8 GB', used: '56 GB', usage: 87.5 }]
    }
  } }))
  await page.goto('/monitor/server')
  await expect(page.getByText('30%', { exact: true })).toBeVisible()
  await expect(page.getByText('1024M', { exact: true })).toBeVisible()
  await expect(page.getByText('ext4', { exact: true })).toBeVisible()
  const monitorTables = page.locator('.app-main .el-table--border')
  await expect(monitorTables).toHaveCount(3)
  for (const [index, name] of [[0, '属性'], [1, '属性'], [2, '盘符路径']]) {
    await resizeColumn(page, monitorTables.nth(index).getByRole('columnheader', { name, exact: true }), 64)
  }
})
