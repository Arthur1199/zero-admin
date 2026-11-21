import { test, expect } from '@playwright/test'
import { authenticated } from './mock-api'
import { WebSocketMessageType } from '../../src/utils/websocketMessageType'

test.beforeEach(async ({ page }) => {
  await page.route('**/preview-test.svg*', route => route.fulfill({
    contentType: 'image/svg+xml',
    body: '<svg xmlns="http://www.w3.org/2000/svg" width="640" height="480"><rect width="640" height="480" fill="white"/><circle cx="320" cy="240" r="100" fill="royalblue"/></svg>'
  }))
})

async function checkViewer(page) {
  const viewer = page.locator('body > .el-image-viewer__wrapper')
  await expect(viewer).toBeVisible()
  await expect.poll(() => viewer.evaluate(element => {
    const rect = element.getBoundingClientRect()
    return { x: rect.x, y: rect.y, width: rect.width, height: rect.height }
  })).toEqual({ x: 0, y: 0, ...page.viewportSize() })
  const image = viewer.locator('.el-image-viewer__img')
  await expect.poll(() => image.evaluate(element => element.naturalWidth)).toBeGreaterThan(0)
  const icons = viewer.locator('.el-image-viewer__actions__inner > .el-icon')
  await expect(icons).toHaveCount(5)
  for (const icon of await icons.all()) await expect(icon.locator('svg')).toBeVisible()
  await expect(viewer.locator('.el-image-viewer__close svg')).toBeVisible()
  const buttonStyles = await viewer.locator('.el-image-viewer__btn').evaluateAll(elements => elements.map(element => {
    const style = getComputedStyle(element)
    return { color: style.color, background: style.backgroundColor, opacity: style.opacity }
  }))
  for (const style of buttonStyles) expect(style).toEqual({ color: 'rgb(255, 255, 255)', background: 'rgba(24, 28, 36, 0.92)', opacity: '1' })
  const initial = await image.evaluate(element => element.style.transform)
  await icons.nth(1).click()
  await expect.poll(() => image.evaluate(element => element.style.transform)).not.toBe(initial)
  await icons.nth(0).click()
  await expect.poll(() => image.evaluate(element => element.style.transform)).toBe(initial)
  await icons.nth(4).click()
  await expect(image).toHaveAttribute('style', /rotate\(90deg\)/)
  await icons.nth(3).click()
  await expect(image).toHaveAttribute('style', /rotate\(0deg\)/)
  await icons.nth(2).click()
  await expect.poll(() => image.evaluate(element => element.style.maxWidth)).toBe('')
  await icons.nth(2).click()
  await expect.poll(() => image.evaluate(element => element.style.maxWidth)).toBe('100%')
  await viewer.locator('.el-image-viewer__close').click()
  await expect(viewer).toHaveCount(0)
}

test('文件图片在八主题下全屏预览，工具栏及关闭正常', async ({ page }) => {
  test.setTimeout(120000)
  const errors = []; page.on('pageerror', error => errors.push(error.message))
  // 模拟会话只在测试中有效，避免真实 WebSocket 拒绝后跳回登录页。
  await page.routeWebSocket('**/ws/admin', socket => {
    socket.onMessage(() => socket.send(JSON.stringify({
      msgType: { messageType: WebSocketMessageType.CREATE_CONNECTION },
      msgData: { heartbeatInterval: 30000, idleTimeout: 60000 }
    })))
  })
  await authenticated(page)
  await page.route('**/dev-api/systemTools/fileManagement/list?**', route => route.fulfill({ json: {
    code: 200, rows: [{ id: 1, filename: 'preview.png', originalFilename: 'preview.png', ext: 'png', url: 'http://127.0.0.1:5178/preview-test.svg' }], total: 1
  } }))
  await page.goto('/systemTools/fileManagement')
  const originalNameColumn = page.getByRole('columnheader', { name: '原始文件名', exact: true })
  await expect(originalNameColumn.locator('xpath=following-sibling::th[1]')).toHaveText('文件预览')
  for (const label of ['毛玻璃拟态风', '高端暗黑风', '极简未来风', '原始风格', '轻奢商务风', '活力科技风', '温馨自然风', '质感设计风']) {
    await page.locator('.navbar .setting').click()
    await page.locator('.drawer-select').first().click()
    await page.getByRole('option', { name: label, exact: true }).click()
    await page.getByRole('button', { name: '关闭设置', exact: true }).click()
    await expect(page.locator('.app-main .fade-transform-leave-active, .app-main .fade-transform-enter-active')).toHaveCount(0)
    const thumbnail = page.locator('.file-preview-image')
    await expect.poll(() => thumbnail.locator('img').evaluate(element => element.naturalWidth)).toBeGreaterThan(0)
    await thumbnail.click()
    await checkViewer(page)
    await thumbnail.click()
    await page.keyboard.press('Escape')
    await expect(page.locator('.el-image-viewer__wrapper')).toHaveCount(0)
  }
  expect(errors).toEqual([])
})

test('公共图片预览脱离裁剪容器，并支持多图切换及关闭', async ({ page }) => {
  await page.goto('/tests/fixtures/image-preview.html')
  await page.locator('.el-image').click()
  await expect(page.locator('body > .el-image-viewer__wrapper')).toBeVisible()
  await page.locator('.el-image-viewer__next').click()
  await expect(page.locator('.el-image-viewer__img')).toHaveAttribute('src', /\?second$/)
  await page.locator('.el-image-viewer__prev').click()
  await checkViewer(page)
})
