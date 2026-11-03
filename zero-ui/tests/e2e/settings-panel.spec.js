import { test, expect } from '@playwright/test'
import { authenticated } from './mock-api'

test('刷新不渲染左上角颜色选择器，已保存的主色仍生效', async ({ page }) => {
  const errors = []
  page.on('pageerror', error => errors.push(error.message))
  await authenticated(page)
  await page.addInitScript(() => {
    localStorage.setItem('layout-setting', JSON.stringify({ themeStyle: 'default', theme: '#13c2c2' }))
  })
  // 延迟菜单初始化，覆盖用户截图中布局尚未加载的阶段。
  await page.route('**/dev-api/getRouters', async route => {
    await new Promise(resolve => setTimeout(resolve, 500))
    await route.fallback()
  })
  for (let count = 0; count < 2; count++) {
    if (count === 0) await page.goto('/system/config')
    else await page.reload()
    await expect(page.locator('#app > .theme-picker')).toHaveCount(0)
    await expect(page.locator('.navbar')).toBeVisible()
    await expect(page.locator('.theme-picker')).toHaveCount(0)
    expect(await page.locator('html').evaluate(element => element.style.getPropertyValue('--el-color-primary'))).toBe('#13c2c2')
  }
  await page.locator('.navbar .setting').click()
  await expect(page.locator('.el-drawer .theme-picker')).toBeVisible()
  expect(errors).toEqual([])
})

test('设置栏内部操作及弹层不误关，点击外部、关闭按钮、Esc 和保存均收起', async ({ page }) => {
  test.setTimeout(120000)
  const errors = []
  page.on('pageerror', error => errors.push(error.message))
  await authenticated(page)
  await page.goto('/system/config')
  const drawer = page.locator('.el-drawer')
  const open = async () => {
    await page.locator('.navbar .setting').click()
    await expect(drawer).toBeVisible()
  }
  await open()
  const themes = ['高端暗黑风', '极简未来风', '轻奢商务风', '活力科技风', '温馨自然风', '质感设计风', '原始风格', '毛玻璃拟态风']
  for (const label of themes) {
    await drawer.locator('.drawer-select').first().click()
    await page.getByRole('option', { name: label, exact: true }).click()
    await expect(drawer).toBeVisible()
    await drawer.getByText('系统布局配置', { exact: true }).click()
    await expect(drawer).toBeVisible()
    if (label === '原始风格') {
      await drawer.locator('.theme-picker').click()
      await expect(page.locator('.theme-picker-dropdown')).toBeVisible()
      await page.locator('.theme-picker-dropdown .el-color-predefine__color-selector').first().click()
      await expect(drawer).toBeVisible()
      await page.locator('.theme-picker-dropdown').getByRole('button', { name: '确定', exact: true }).click()
      await expect(drawer).toBeVisible()
    }
    await page.locator('.navbar .breadcrumb-container').click()
    await expect(drawer).toBeHidden()
    await open()
  }
  await drawer.getByRole('button', { name: '关闭设置', exact: true }).click()
  await expect(drawer).toBeHidden()
  await open()
  await page.keyboard.press('Escape')
  await expect(drawer).toBeHidden()
  await open()
  await drawer.getByRole('button', { name: '保存配置', exact: true }).click()
  await expect(drawer).toBeHidden()
  await expect(page.locator('.el-loading-mask:visible')).toHaveCount(0)
  expect(await page.evaluate(() => JSON.parse(localStorage.getItem('layout-setting')).themeStyle)).toBe('glassmorphism')
  await page.reload()
  await expect(page.locator('.navbar')).toBeVisible()
  await expect(drawer).toBeHidden()
  await expect(page.locator('#app > .theme-picker')).toHaveCount(0)
  expect(errors).toEqual([])
})

test('配置保存失败保留设置栏并释放 loading', async ({ page }) => {
  await authenticated(page)
  await page.goto('/system/config')
  await page.locator('.navbar .setting').click()
  await page.evaluate(() => {
    const original = Storage.prototype.setItem
    Storage.prototype.setItem = function(key, value) {
      if (key === 'layout-setting') throw new DOMException('Storage unavailable', 'QuotaExceededError')
      return original.call(this, key, value)
    }
  })
  await page.getByRole('button', { name: '保存配置', exact: true }).click()
  await expect(page.getByText('配置保存失败，请检查浏览器本地存储是否可用', { exact: true })).toBeVisible()
  await expect(page.locator('.el-drawer')).toBeVisible()
  await expect(page.locator('.el-loading-mask:visible')).toHaveCount(0)
})
