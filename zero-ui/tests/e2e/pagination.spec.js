import { test, expect } from '@playwright/test'
import { authenticated } from './mock-api'

test('八主题跳页框没有双层背景，页码与每页条数请求正确', async ({ page }) => {
  test.setTimeout(120000)
  await authenticated(page)
  const queries = []
  await page.route('**/dev-api/system/config/list?**', route => {
    const params = new URL(route.request().url()).searchParams
    const pageNum = Number(params.get('pageNum')), pageSize = Number(params.get('pageSize'))
    queries.push({ pageNum, pageSize })
    return route.fulfill({ json: { code: 200, total: 55, rows: [{ configId: pageNum, configName: '分页验证第' + pageNum + '页', configType: 'N' }] } })
  })
  await page.goto('/system/config')
  for (const [theme, label] of [
    ['glassmorphism', '毛玻璃拟态风'], ['dark-premium', '高端暗黑风'], ['minimalist-future', '极简未来风'],
    ['default', '原始风格'], ['luxury-business', '轻奢商务风'], ['vibrant-tech', '活力科技风'],
    ['warm-nature', '温馨自然风'], ['material-design', '质感设计风']
  ]) {
    await page.locator('.navbar .setting').click()
    await page.locator('.drawer-select').first().click()
    await page.getByRole('option', { name: label, exact: true }).click()
    if (theme === 'default') await expect(page.locator('html > body')).not.toHaveClass(/theme-/)
    else await expect(page.locator('html > body')).toHaveClass(new RegExp(`theme-${theme}`))
    await page.getByRole('button', { name: '关闭设置', exact: true }).click()
    await expect(page.locator('.el-drawer')).not.toBeVisible()
    await expect(page.locator('.app-main .fade-transform-leave-active, .app-main .fade-transform-enter-active')).toHaveCount(0)
    const editor = page.locator('.el-pagination__jump input')
    await expect(editor).toBeVisible()
    await expect.poll(() => editor.evaluate(element => ({
      background: getComputedStyle(element).backgroundColor,
      border: getComputedStyle(element).borderTopWidth,
      shadow: getComputedStyle(element).boxShadow
    })), { message: theme }).toEqual({ background: 'rgba(0, 0, 0, 0)', border: '0px', shadow: 'none' })
    await editor.fill('2')
    // 页末前向Tab在本机无界面Firefox及原生input中都不失焦；反向Tab明确移动到页内控件。
    await editor.press('Shift+Tab')
    await expect(editor).not.toBeFocused()
    await expect(page.locator('.el-pager .is-active')).toHaveText('2')
    await expect(page.getByText('分页验证第2页', { exact: true })).toBeVisible()
    await expect.poll(() => queries.at(-1)).toEqual({ pageNum: 2, pageSize: 10 })
    await editor.fill('3')
    await editor.press('Enter')
    await expect(page.getByText('分页验证第3页', { exact: true })).toBeVisible()
    await editor.fill('2')
    await editor.press('Enter')
    await expect(page.getByText('分页验证第2页', { exact: true })).toBeVisible()
    await page.locator('.el-pagination__sizes .el-select').click()
    await page.getByRole('option', { name: '20条/页', exact: true }).click()
    await expect.poll(() => queries.at(-1)).toEqual({ pageNum: 2, pageSize: 20 })
  }
})
