import { test, expect } from '@playwright/test'
import { authenticated, mockAdminWebSocket } from './mock-api'

const themes = ['glassmorphism', 'dark-premium', 'minimalist-future', 'default', 'luxury-business', 'vibrant-tech', 'warm-nature', 'material-design']

test('八主题图标弹层、搜索、悬浮与选中使用主题配色', async ({ page }, testInfo) => {
  test.setTimeout(120000)
  const errors = []
  page.on('pageerror', error => errors.push(error.message))
  await mockAdminWebSocket(page)
  await authenticated(page)
  await page.goto('/system/menu')
  for (const theme of themes) {
    await page.evaluate(async value => {
      const { default: store } = await import('/src/store/index.js')
      await store.dispatch('settings/changeSetting', { key: 'themeStyle', value })
    }, theme)
    await expect.poll(() => page.evaluate(value => value === 'default'
      ? ![...document.body.classList].some(name => name.startsWith('theme-'))
      : document.body.classList.contains(`theme-${value}`), theme)).toBe(true)
    await expect(page.locator('.app-main .fade-transform-enter-active, .app-main .fade-transform-leave-active')).toHaveCount(0)
    await page.getByRole('button', { name: '新增', exact: true }).and(page.locator('.is-plain')).click()
    const trigger = page.getByPlaceholder('点击选择图标')
    await trigger.click()
    const picker = page.locator('.zero-icon-select-popper:visible')
    await expect(picker).toBeVisible()
    // show 回调会重置检索条件，等弹层入场完成后再验证输入，避免与重置交错。
    await expect(picker).not.toHaveClass(/enter-active|enter-from/)
    const expected = await picker.evaluate(element => {
      const probe = document.createElement('span')
      probe.style.cssText = 'color:var(--zero-text-heading);background:var(--zero-surface-hover);border-color:var(--zero-border)'
      element.appendChild(probe)
      const state = getComputedStyle(probe)
      const result = { activeText: state.color, activeBackground: state.backgroundColor, border: state.borderTopColor }
      probe.style.backgroundColor = 'var(--zero-icon-select-surface)'
      result.surface = getComputedStyle(probe).backgroundColor
      probe.remove()
      return result
    })
    await expect(picker, theme).toHaveCSS('background-color', expected.surface)
    // 只有毛玻璃允许透色；浅色弹层必须完整遮住背后的表单文字。
    const alpha = expected.surface.match(/^rgba\([^,]+,[^,]+,[^,]+,\s*([\d.]+)\)$/)?.[1] ?? '1'
    if (theme === 'glassmorphism') expect(Number(alpha)).toBeLessThan(1)
    else expect(Number(alpha), theme).toBe(1)
    await expect(picker, theme).toHaveCSS('border-top-color', expected.border)
    await expect(picker.locator('.el-popper__arrow')).toBeAttached()
    await expect.poll(() => picker.locator('.el-popper__arrow').evaluate(element => getComputedStyle(element, '::before').backgroundColor)).toBe(expected.surface)

    const search = picker.getByPlaceholder('请输入图标名称')
    await search.fill('download')
    await expect(picker.locator('.icon-item')).toHaveCount(1)
    const icon = picker.locator('.icon-item').filter({ hasText: /^download$/ })
    await icon.hover()
    await expect(icon, theme).toHaveCSS('background-color', expected.activeBackground)
    await expect(icon, theme).toHaveCSS('color', expected.activeText)
    await expect(icon.locator('svg'), theme).toHaveCSS('color', expected.activeText)
    await expect(icon.locator('svg'), theme).toHaveCSS('fill', expected.activeText)
    if (testInfo.project.name === 'chromium') await page.screenshot({ path: testInfo.outputPath(`${theme}-icon-hover.png`) })
    await icon.click()
    await expect(trigger).toHaveValue('download')
    if (await picker.isVisible()) await page.locator('.el-dialog__title').click()
    await expect(picker).toBeHidden()
    await trigger.click()
    await expect(search).toHaveValue('')
    const active = picker.locator('.icon-item.active')
    await expect(active).toHaveCount(1)
    await expect(active).toHaveText('download')
    await expect(active).toHaveCSS('background-color', expected.activeBackground)
    await page.locator('.el-dialog__title').click()
    await page.getByRole('button', { name: /取\s*消/ }).click()
    await expect(page.locator('.el-dialog')).toBeHidden()
  }
  // 单色 SVG 不能再用固定色覆盖图标实例的主题色；同时核对整个 sprite 的 ID 和命名空间。
  expect(await page.evaluate(() => {
    const symbols = [...document.querySelectorAll('body > svg symbol')]
    return symbols.length > 0 && new Set(symbols.map(node => node.id)).size === symbols.length && symbols.every(node =>
      [node, ...node.querySelectorAll('*')].every(child => child.namespaceURI === 'http://www.w3.org/2000/svg')) && !document.querySelector('parsererror')
  })).toBe(true)
  for (const [id, attribute] of [['download', 'fill'], ['user', 'stroke'], ['validCode', 'fill']]) {
    await expect(page.locator(`#icon-${id} [${attribute}]`).first()).toHaveAttribute(attribute, 'currentColor')
  }
  expect(errors).toEqual([])
})
