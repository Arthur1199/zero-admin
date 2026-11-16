import { test, expect } from '@playwright/test'
import { authenticated } from './mock-api'

const menus = [{ path: '/monitor', component: 'Layout', meta: { title: '系统监控' }, children: [{ path: 'admin', component: 'ParentView', meta: { title: '管理后台' }, children: [{ path: 'statistics', name: 'Statistics', component: 'monitor/statistics/index', meta: { title: '运行统计', noCache: true } }] }] }]
const today = () => new Intl.DateTimeFormat('en-CA', { timeZone: 'Asia/Shanghai', year: 'numeric', month: '2-digit', day: '2-digit' }).format(new Date())
async function setup(page) {
  const requests = await authenticated(page, { menus })
  const calls = []
  await page.route('**/dev-api/monitor/statistics/**', async route => {
    const url = new URL(route.request().url()), action = url.pathname.split('/').at(-1)
    calls.push({ action, params: Object.fromEntries(url.searchParams), method: route.request().method() })
    let data
    if (action === 'overview') data = { databaseAvailable: true, status: { enabled: true, redisAvailable: true, lostSinceStart: 0, lastSuccessAt: Date.now(), lastErrorAt: 0 }, health: { sampledAt: Date.now(), collectedSince: Date.now(), lastConsumedAt: Date.now(), onlineUsers: 6, onlineSessions: 8, recentActive: 4 }, metrics: { loginSuccess: 31, loginAttempts: 33, loginFailed: 2, loginUsers: 12, activeUsers: 18, total: 1234, success: 1200, failed: 34, successRate: 97.24, avgMs: 42.5, maxMs: 1600, slow: 8, peakMinute: 95 } }
    else if (action === 'trend') data = [{ kind: 'api', bucket: today() + ' 10:00', total: 80, success: 75, failed: 5 }, { kind: 'online', bucket: today() + ' 10:00', onlineUsers: 6, active: 4, peak: 7 }, { kind: 'login', bucket: today(), loginSuccess: 31, loginFailed: 2 }]
    else if (action === 'distribution') data = [{ module: 'system', total: 900, business: 12, auth: 8, system: 2, http: 1, unknown: 1 }, { module: 'reportSystem', total: 334, business: 5, auth: 3, system: 2, http: 0, unknown: 0 }]
    else data = [{ name: 'SysUserController.list', route: '/system/user/list', method: 'GET', module: 'system', total: 900, success: 876, failed: 24, successRate: 97.33, avgMs: 38.2, maxMs: 1500, slow: 4 }]
    return route.fulfill({ json: action === 'list' ? { code: 200, rows: data, total: 25 } : { code: 200, data } })
  })
  return { calls, requests }
}

test('运行统计展示指标图表、历史筛选与分页，仅发送读取请求', async ({ page }) => {
  await page.clock.install()
  const errors = []
  page.on('pageerror', error => errors.push(error.message))
  const { calls, requests } = await setup(page)
  await page.goto('/monitor/admin/statistics')
  await expect(page.getByRole('heading', { name: '运行统计' })).toBeVisible()
  await expect(page.locator('.metric-value').nth(2)).toContainText('1,234')
  await expect(page.locator('.statistics-chart canvas')).toHaveCount(6)
  await expect(page.locator('.el-table__body')).toContainText('SysUserController.list')
  await page.getByText('近7天', { exact: true }).click()
  await expect.poll(() => calls.filter(call => call.action === 'trend').length).toBe(2)
  await page.getByPlaceholder('接口路径或名称').fill('user')
  await page.locator('.detail-card').getByRole('button', { name: '查询', exact: true }).click()
  await expect.poll(() => calls.filter(call => call.action === 'list').at(-1)?.params.keyword).toBe('user')
  await page.locator('.el-pagination .btn-next').scrollIntoViewIfNeeded()
  const scrollBeforePagination = await page.evaluate(() => window.scrollY)
  expect(scrollBeforePagination).toBeGreaterThan(300)
  await page.locator('.el-pagination .btn-next').click()
  await expect.poll(() => calls.filter(call => call.action === 'list').at(-1)?.params.pageNum).toBe('2')
  await page.clock.runFor(1000)
  expect(Math.abs(await page.evaluate(() => window.scrollY) - scrollBeforePagination)).toBeLessThanOrEqual(2)
  expect([...calls, ...requests].every(call => call.method === 'GET')).toBe(true)
  expect(errors).toEqual([])
})

test('代表主题切换后图表重新建立，表格横向滚动且无页面异常', async ({ page }, testInfo) => {
  const errors = []
  page.on('pageerror', error => errors.push(error.message))
  await setup(page)
  await page.goto('/monitor/admin/statistics')
  await expect(page.locator('.statistics-chart canvas')).toHaveCount(6)
  for (const theme of ['glassmorphism', 'dark-premium', 'minimalist-future', 'default']) {
    await page.evaluate(async value => { const { default: store } = await import('/src/store/index.js'); await store.dispatch('settings/changeSetting', { key: 'themeStyle', value }) }, theme)
    await expect(page.locator('.statistics-chart canvas')).toHaveCount(6)
    await expect(page.locator('.metric-value').nth(2)).toContainText('1,234')
    await expect(page.getByRole('heading', { name: '运行统计' })).toBeVisible()
    const colors = await page.locator('.chart-grid .el-card').first().evaluate(el => ({ text: getComputedStyle(el).color, background: getComputedStyle(el).backgroundColor }))
    expect(colors.text).not.toBe(colors.background)
    expect(await page.locator('.el-table__body-wrapper .el-scrollbar__wrap').evaluate(el => el.scrollWidth > el.clientWidth)).toBe(true)
    if (testInfo.project.name === 'chromium') await page.screenshot({ path: 'output/playwright/statistics-' + theme + '.png', fullPage: true })
  }
  expect(errors).toEqual([])
})

test('查询失败结束加载并提示重试，未启用时明确显示配置提示', async ({ page }) => {
  await setup(page)
  await page.route('**/dev-api/monitor/statistics/trend?**', route => route.fulfill({ json: { code: 500, msg: '隔离测试查询失败' } }))
  await page.route('**/dev-api/monitor/statistics/overview', route => route.fulfill({ json: { code: 200, data: { databaseAvailable: true, status: { enabled: false }, metrics: {}, health: {} } } }))
  await page.goto('/monitor/admin/statistics')
  await expect(page.locator('.health-alert')).toContainText('部分统计查询失败')
  await expect(page.getByRole('button', { name: '刷新', exact: true })).toBeEnabled()
  await page.unroute('**/dev-api/monitor/statistics/trend?**')
  await page.getByRole('button', { name: '刷新', exact: true }).click()
  await expect(page.locator('.health-alert')).toContainText('统计采集未启用')
  await expect(page.locator('.health-alert')).toContainText('sys.statistics.enabled')
  await page.route('**/dev-api/monitor/statistics/overview', route => route.fulfill({ json: { code: 200, data: { databaseAvailable: true, status: { enabled: false, switchAvailable: false }, metrics: {}, health: {} } } }))
  await page.getByRole('button', { name: '刷新', exact: true }).click()
  await expect(page.locator('.health-alert')).toContainText('统计开关读取失败')
  await expect(page.locator('.health-alert')).not.toContainText('统计采集未启用')
})


test('自动刷新可暂停，离开页面停止统计轮询', async ({ page }) => {
  await page.clock.install()
  const { calls } = await setup(page)
  await page.goto('/monitor/admin/statistics')
  await expect(page.locator('.el-table__body')).toContainText('SysUserController.list')
  await page.locator('.refresh-controls .el-switch').click()
  const before = calls.length
  await page.clock.fastForward(31000)
  expect(calls.length).toBe(before)
  await page.locator('.refresh-controls .el-switch').click()
  await page.clock.fastForward(31000)
  await expect.poll(() => calls.length).toBeGreaterThan(before)
  await expect(page.getByRole('button', { name: '刷新', exact: true })).toBeEnabled()
  await page.goto('/index')
  const left = calls.length
  await page.clock.fastForward(61000)
  expect(calls.length).toBe(left)
})


test('跨北京时间零点自动推进今日，自动刷新不提交正在输入的接口草稿', async ({ page }) => {
  await page.clock.install({ time: new Date('2026-11-16T15:59:45Z') })
  const { calls } = await setup(page)
  await page.goto('/monitor/admin/statistics')
  await expect(page.locator('.el-table__body')).toContainText('SysUserController.list')
  await page.getByPlaceholder('接口路径或名称').fill('  user  ')
  await page.clock.fastForward(31000)
  await expect.poll(() => calls.filter(call => call.action === 'trend').at(-1)?.params.beginTime).toBe('2026-11-17')
  await expect(page.getByRole('button', { name: '刷新', exact: true })).toBeEnabled()
  expect(calls.filter(call => call.action === 'list').at(-1)?.params.keyword || '').toBe('')
  await expect(page.locator('.filter-card .el-range-input').first()).toHaveValue('2026-11-17')
  await expect(page.getByPlaceholder('接口路径或名称')).toHaveValue('  user  ')
  await page.locator('.detail-card').getByRole('button', { name: '查询', exact: true }).click()
  await expect.poll(() => calls.filter(call => call.action === 'list').at(-1)?.params.keyword).toBe('user')
})

test('暂停自动刷新后采样仍会过期，使用服务器时间避免客户端时钟误判', async ({ page }) => {
  const serverTime = Date.now()
  await page.clock.install({ time: new Date(serverTime + 86400000) })
  const { calls } = await setup(page)
  await page.route('**/dev-api/monitor/statistics/overview', route => route.fulfill({ json: { code: 200, data: {
    serverTime, databaseAvailable: true, status: { enabled: true, redisAvailable: true, lastSuccessAt: serverTime, lastErrorAt: 0 },
    health: { sampledAt: serverTime, onlineUsers: 6, onlineSessions: 8, recentActive: 4 }, metrics: { total: 100 }
  } } }))
  await page.goto('/monitor/admin/statistics')
  await expect(page.locator('.metric-value').first()).toContainText('6')
  const serverDate = new Intl.DateTimeFormat('en-CA', { timeZone: 'Asia/Shanghai', year: 'numeric', month: '2-digit', day: '2-digit' }).format(new Date(serverTime))
  await expect.poll(() => calls.filter(call => call.action === 'trend').at(-1)?.params.beginTime).toBe(serverDate)
  await expect(page.getByRole('button', { name: '刷新', exact: true })).toBeEnabled()
  await page.locator('.refresh-controls .el-switch').click()
  const before = calls.length
  await page.clock.fastForward(120000)
  await expect(page.locator('.health-alert')).toContainText('过期')
  await expect(page.locator('.metric-value').first()).toContainText('—')
  expect(calls.length).toBe(before)
})

test('接口表格查询失败后单独重试成功即清除失败提示', async ({ page }) => {
  await setup(page)
  await page.goto('/monitor/admin/statistics')
  await expect(page.locator('.el-table__body')).toContainText('SysUserController.list')
  await page.route('**/dev-api/monitor/statistics/list?**', route => route.fulfill({ json: { code: 500, msg: '隔离列表失败' } }))
  await page.locator('.detail-card').getByRole('button', { name: '查询', exact: true }).click()
  await expect(page.locator('.health-alert')).toContainText('部分统计查询失败')
  await page.unroute('**/dev-api/monitor/statistics/list?**')
  await page.locator('.detail-card').getByRole('button', { name: '查询', exact: true }).click()
  await expect(page.locator('.health-alert')).not.toContainText('部分统计查询失败')
})
