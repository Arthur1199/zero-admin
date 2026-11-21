import { expect, test } from '@playwright/test'
import { authenticated, mockAdminWebSocket } from './mock-api'

test('版权在岗位管理和用户管理中固定于视口右下角', async ({ page }) => {
  await mockAdminWebSocket(page)
  await authenticated(page)
  await page.addInitScript(() => {
    localStorage.setItem('layout-setting', JSON.stringify({ footerVisible: true, themeStyle: 'glassmorphism' }))
  })
  await page.route('**/dev-api/system/post/list?**', route => route.fulfill({ json: {
    code: 200, total: 30,
    rows: Array.from({ length: 30 }, (_, index) => ({ postId: index + 1, postCode: 'mock-post-' + index, postName: '模拟岗位', postSort: index, status: '0' }))
  } }))
  const footer = page.locator('body > footer.copyright')
  const viewportGaps = () => footer.evaluate(element => {
    const rect = element.getBoundingClientRect()
    return {
      right: Math.abs(rect.right - document.documentElement.clientWidth),
      bottom: Math.abs(rect.bottom - document.documentElement.clientHeight)
    }
  })

  await page.goto('/system/post')
  await expect(page.getByRole('row').filter({ hasText: 'mock-post-29' }).getByText('mock-post-29', { exact: true })).toBeAttached()
  await expect(footer).toBeVisible()
  await expect.poll(viewportGaps).toEqual({ right: 0, bottom: 0 })
  await page.evaluate(() => window.scrollTo(0, document.documentElement.scrollHeight))
  await expect.poll(() => page.evaluate(() => window.scrollY)).toBeGreaterThan(0)
  await expect.poll(viewportGaps).toEqual({ right: 0, bottom: 0 })

  await page.goto('/system/user')
  await expect(page.getByPlaceholder('请输入部门名称')).toBeVisible()
  await expect.poll(viewportGaps).toEqual({ right: 0, bottom: 0 })
})
