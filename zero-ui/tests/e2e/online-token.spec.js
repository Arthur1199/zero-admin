import { test, expect } from '@playwright/test'
import { authenticated } from './mock-api'

// 仅使用不能认证的模拟JWT，测试产物不包含真实会话凭证。
const tokens = ['Bearer eyJ.mock-session-one.' + 'a'.repeat(180), 'Bearer eyJ.mock-session-two.' + 'b'.repeat(180)]
const sessionIds = ['00000000-0000-4000-8000-000000000001', '00000000-0000-4000-8000-000000000002']
const themes = ['default', 'glassmorphism', 'dark-premium', 'minimalist-future', 'luxury-business', 'vibrant-tech', 'warm-nature', 'material-design']

test('普通菜单授权账号可完整查看所有会话令牌并复制，八主题下不截断', async ({ page }) => {
  const errors = []
  page.on('pageerror', error => errors.push(error.message))
  const requests = await authenticated(page, { roles: ['common'], permissions: ['monitor:online:list'] })
  await page.route('**/dev-api/monitor/online/list**', route => route.fulfill({ json: {
    code: 200, total: 2, rows: tokens.map((authorizationToken, index) => ({
      tokenId: sessionIds[index], authorizationToken, userName: 'other-user-' + index,
      ipaddr: '192.0.2.' + (index + 1), loginTime: 1700000000000
    }))
  } }))
  await page.goto('/monitor/online')
  await expect(page.locator('.authorization-token')).toHaveCount(2)
  const headers = await page.locator('.el-table__header th').allTextContents()
  expect(headers.indexOf('授权令牌')).toBe(headers.indexOf('会话编号') + 1)
  for (const theme of themes) {
    await page.evaluate(theme => {
      const app = document.querySelector('#app').__vue_app__
      return app.config.globalProperties.$store.dispatch('settings/changeSetting', { key: 'themeStyle', value: theme })
    }, theme)
    for (let index = 0; index < tokens.length; index++) {
      const value = page.locator('.authorization-token').nth(index)
      await expect(value).toHaveText(tokens[index])
      await expect(value).toBeVisible()
      expect(await value.evaluate(el => el.scrollWidth <= el.clientWidth + 1)).toBe(true)
      await expect.poll(() => value.evaluate(el => getComputedStyle(el).whiteSpace)).toBe('normal')
      await expect(page.locator('.online-session-id').nth(index)).toHaveText(sessionIds[index])
    }
  }
  await expect(page.getByRole('button', { name: '强退', exact: true })).toHaveCount(0)
  await page.evaluate(() => {
    const probe = document.createElement('textarea')
    probe.dataset.testid = 'token-paste'
    probe.style.cssText = 'position:fixed;bottom:8px;left:8px;z-index:10000'
    document.body.appendChild(probe)
  })
  await page.getByRole('button', { name: '复制令牌', exact: true }).nth(1).click()
  const probe = page.getByTestId('token-paste')
  await probe.focus()
  await probe.press('Control+V')
  await expect(probe).toHaveValue(tokens[1])
  await probe.evaluate(el => el.remove())
  const thumb = page.locator('.el-table .el-scrollbar__bar.is-horizontal .el-scrollbar__thumb')
  await expect(thumb).toBeVisible()
  const box = await thumb.boundingBox()
  await page.mouse.move(box.x + box.width / 2, box.y + box.height / 2)
  await page.mouse.down()
  await page.mouse.move(box.x + box.width / 2 + 150, box.y + box.height / 2, { steps: 8 })
  await page.mouse.up()
  await expect.poll(() => page.locator('.el-table .el-scrollbar__wrap').evaluate(el => el.scrollLeft)).toBeGreaterThan(0)
  expect(errors).toEqual([])
  expect(requests.filter(request => request.method !== 'GET')).toEqual([])
})

test('旧响应缺少令牌时不复制空值，读取失败后可重新查询', async ({ page }) => {
  await authenticated(page, { roles: ['common'], permissions: ['monitor:online:list'] })
  let fail = true
  await page.route('**/dev-api/monitor/online/list**', route => route.fulfill({ json: fail
    ? { code: 500, msg: '模拟在线用户查询失败' }
    : { code: 200, rows: [{ tokenId: 'legacy-session', userName: 'legacy-user' }], total: 1 } }))
  await page.goto('/monitor/online')
  await expect(page.getByText('模拟在线用户查询失败', { exact: true })).toBeVisible()
  await expect(page.locator('.app-main .el-loading-mask:visible')).toHaveCount(0)
  fail = false
  await page.getByRole('button', { name: '搜索', exact: true }).click()
  await expect(page.getByText('暂无令牌', { exact: true })).toBeVisible()
  await expect(page.getByRole('button', { name: '复制令牌', exact: true })).toHaveCount(0)
})
