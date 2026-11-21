import { test, expect } from '@playwright/test'

const themes = ['glassmorphism', 'dark-premium', 'minimalist-future', 'default', 'luxury-business', 'vibrant-tech', 'warm-nature', 'material-design']

test('八主题输入控件聚焦时不叠加内框，键盘操作仍有可见反馈', async ({ page }, testInfo) => {
  await page.goto('/tests/fixtures/form-focus.html')
  await expect(page.locator('.el-dialog')).toBeVisible()
  for (const theme of themes) {
    await page.evaluate(value => { document.body.className = value === 'default' ? '' : `theme-${value}` }, theme)
    for (const selector of ['.focus-text input', '.focus-tree input', '.focus-number input', '.focus-select input', '.focus-date input', '.focus-textarea textarea']) {
      const input = page.locator(selector).first()
      await input.click()
      await expect(input).toBeFocused()
      // 文本输入即使由鼠标点击，也可能匹配 :focus-visible；内层不能重复画轮廓。
      await expect(input, `${theme}: ${selector}`).toHaveCSS('outline-style', 'none')
      if (!selector.includes('textarea')) await expect(input).toHaveCSS('box-shadow', 'none')
      await page.keyboard.press('Escape')
    }
    await page.locator('.focus-text input').focus()
    await page.keyboard.press('Tab')
    await expect(page.locator('.focus-tree input')).toBeFocused()
    await expect(page.locator('.focus-tree input')).toHaveCSS('outline-style', 'none')
    await expect(page.locator('.focus-tree .el-select__wrapper')).toHaveClass(/is-focused/)
    await expect(page.locator('.focus-tree .el-select__wrapper')).not.toHaveCSS('box-shadow', 'none')
    await page.locator('#keyboard-target').focus()
    await expect(page.locator('#keyboard-target')).toHaveCSS('outline-style', 'solid')
    await page.locator('.focus-text input').focus()
    if (testInfo.project.name === 'chromium') await page.screenshot({ path: testInfo.outputPath(`${theme}-focus.png`) })
  }
})
