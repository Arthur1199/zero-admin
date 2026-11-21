import { test, expect } from '@playwright/test'
import { authenticated, mockAdminWebSocket } from './mock-api'

async function colors(page) {
  return page.evaluate(() => {
    const probe = document.createElement('span')
    document.body.appendChild(probe)
    const result = {}
    for (const state of ['bg', 'hover-bg', 'active-bg']) {
      probe.style.backgroundColor = `var(--glass-confirm-${state})`
      result[state] = getComputedStyle(probe).backgroundColor
    }
    probe.remove()
    return result
  })
}

test('毛玻璃的菜单、岗位、用户和二次确认框复用同一主按钮配色', async ({ page }, testInfo) => {
  await mockAdminWebSocket(page)
  await authenticated(page)
  await page.addInitScript(() => localStorage.setItem('layout-setting', JSON.stringify({ themeStyle: 'glassmorphism' })))
  for (const path of ['/system/menu', '/system/post', '/system/user']) {
    await page.goto(path)
    await page.getByRole('button', { name: '新增', exact: true }).and(page.locator('.is-plain')).click()
    const dialog = page.locator('.el-dialog:visible')
    const confirm = dialog.locator('.el-dialog__footer .el-button--primary')
    const expected = await colors(page)
    await page.locator('.el-dialog__title').hover()
    await expect(confirm).toHaveCSS('background-color', expected.bg)
    await expect(confirm).toHaveCSS('color', 'rgb(255, 255, 255)')
    await confirm.hover()
    await expect(confirm).toHaveCSS('background-color', expected['hover-bg'])
    await page.mouse.down()
    await expect(confirm).toHaveCSS('background-color', expected['active-bg'])
    // 在按钮外抬起鼠标，只检查按下样式，不提交表单。
    await page.locator('.el-dialog__title').hover()
    await page.mouse.up()
    await page.locator('.el-dialog__title').click()
    if (testInfo.project.name === 'chromium') await page.screenshot({ path: testInfo.outputPath(path.split('/').at(-1) + '-confirm.png') })
    await dialog.getByRole('button', { name: /取\s*消/ }).click()
    await expect(dialog).toBeHidden()
  }
  await page.evaluate(async () => {
    const { confirm } = await import('/src/plugins/modal.js')
    confirm('仅验证确认框外观').catch(action => { if (!['cancel', 'close'].includes(action)) throw action })
  })
  const box = page.locator('.el-message-box')
  await expect(box).toBeVisible()
  const expected = await colors(page)
  await expect(box.locator('.el-button--primary')).toHaveCSS('background-color', expected.bg)
  await box.getByRole('button', { name: '取消', exact: true }).click()
})

test('主按钮禁用及加载状态保持主题色，不覆盖其它按钮类型', async ({ page }) => {
  await page.goto('/tests/fixtures/glass-buttons.html')
  const expected = await colors(page)
  await expect(page.locator('#disabled')).toBeDisabled()
  await expect(page.locator('#disabled')).toHaveCSS('background-color', expected.bg)
  await expect(page.locator('#disabled')).toHaveCSS('opacity', '0.45')
  await expect(page.locator('#loading')).toHaveClass(/is-loading/)
  await expect(page.locator('#loading')).toHaveCSS('background-color', expected.bg)
  for (const id of ['outside', 'plain', 'danger', 'cancel']) {
    await expect(page.locator('#' + id)).not.toHaveCSS('background-color', expected.bg)
  }
})
