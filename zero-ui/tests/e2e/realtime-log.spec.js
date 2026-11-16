import { test, expect } from '@playwright/test'
import { authenticated } from './mock-api'

const path = '/monitor/admin/realtimeLog'
const menus = [{ path: '/monitor', component: 'Layout', meta: { title: '系统监控' }, children: [{ path: 'admin', component: 'ParentView', meta: { title: '管理后台' }, children: [{ path: 'realtimeLog', name: 'RealtimeLog', component: 'monitor/realtimeLog/index', meta: { title: '实时日志' } }] }] }]
const event = (sequence, text = `运行事件 ${sequence}\n`, level = 'INFO') => ({ sequence, timestamp: 1700000000000 + sequence, level, thread: 'main', logger: 'test', text, truncated: false })

test('完整控制台按原文拼接stdout、stderr与JVM输出，不补换行或重复格式化', async ({ page }) => {
  const prefix = 'stdout中文' + 'x'.repeat(44)
  await page.clock.install()
  await page.clock.pauseAt(new Date(Date.now() + 1000))
  await setup(page, count => ({ sourceMode: 'console-file', processId: 'console-one', nextCursor: count * 100,
    events: count === 1 ? Array.from({ length: 45 }, (_, index) => ({ ...event(index + 1, index === 0 ? 'stdout中文' : 'x', 'CONSOLE'), raw: true }))
      : count === 2 ? [{ ...event(200, 'stderr\n\n[info][gc] native\npassword=fictional\n', 'CONSOLE'), raw: true }] : [] }))
  await page.goto(path)
  await expect(page.getByRole('heading', { name: '实时日志', exact: true })).toBeVisible()
  await page.clock.runFor(10)
  await expect(page.locator('.log-viewport')).toHaveText(prefix)
  await expect(page.locator('.log-chunk')).toHaveCount(2)
  await expect(page.locator('.log-chunk').first()).toHaveCSS('display', 'inline')
  await expect(page.locator('.log-heading')).toContainText('完整进程控制台')
  await expect(page.locator('.log-level input')).toBeDisabled()
  await page.clock.runFor(1100)
  await expect(page.locator('.log-viewport')).toHaveText(prefix + 'stderr\n\n[info][gc] native\npassword=fictional\n')
  await expect(page.locator('.log-footnote')).toContainText('不做脱敏或补换行')
  await expect(page.locator('.log-mode-notice')).toHaveCount(0)
})

async function setup(page, reply) {
  const requests = await authenticated(page, { menus, roles: ['common'], permissions: ['monitor:realtimeLog:list'] })
  const calls = []
  await page.route('**/dev-api/monitor/realtimeLog/events**', async route => {
    const params = Object.fromEntries(new URL(route.request().url()).searchParams)
    calls.push(params)
    const result = reply ? await reply(calls.length, params) : { events: [event(1)], processId: 'process-one', nextCursor: 1 }
    await route.fulfill({ json: result.code ? result : { code: 200, data: { hasMore: false, gap: false, reset: false, ...result } } })
  })
  return { calls, requests }
}

test('增量滚动、纯文本堆栈、过滤与清空保留游标', async ({ page }) => {
  await page.clock.install()
  const { calls, requests } = await setup(page, count => ({ processId: 'process-one', nextCursor: count,
    events: [event(count, count === 1 ? '<img src=x onerror=alert(1)>\njava.lang.IllegalStateException\n    at example.run(Test.java:1)\n' : `新增日志 ${count}\n`, count === 1 ? 'ERROR' : 'INFO')] }))
  await page.goto(path)
  await expect(page.locator('.log-viewport')).toContainText('IllegalStateException')
  await expect(page.locator('.log-viewport img')).toHaveCount(0)
  await page.clock.runFor(1100)
  await expect(page.locator('.log-viewport')).toContainText('新增日志 2')
  expect(calls[1]).toMatchObject({ processId: 'process-one', after: '1', limit: '500' })
  await page.getByPlaceholder('过滤当前保留日志').fill('IllegalStateException')
  await expect(page.locator('.log-viewport')).not.toContainText('新增日志 2')
  await page.getByPlaceholder('过滤当前保留日志').fill('')
  await page.getByRole('button', { name: '清空当前显示' }).click()
  await expect(page.locator('.log-summary')).toContainText('保留 0 条')
  await page.clock.runFor(1100)
  await expect(page.locator('.log-viewport')).toContainText('新增日志 3')
  expect(calls[2].after).toBe('2')
  expect(requests.filter(request => request.path.includes('realtimeLog')).every(request => request.method === 'GET')).toBe(true)
})

test('暂停、恢复、隐藏和离开页面停止轮询', async ({ page }) => {
  await page.clock.install()
  const { calls } = await setup(page)
  await page.goto(path)
  await expect(page.locator('.log-viewport')).toContainText('运行事件 1')
  await page.getByRole('button', { name: '暂停', exact: true }).click()
  const count = calls.length
  await page.clock.runFor(5000)
  expect(calls).toHaveLength(count)
  await page.getByRole('button', { name: '继续', exact: true }).click()
  await page.clock.runFor(10)
  await expect.poll(() => calls.length).toBe(count + 1)
  await page.evaluate(() => {
    Object.defineProperty(document, 'hidden', { configurable: true, value: true })
    document.dispatchEvent(new Event('visibilitychange'))
  })
  await page.clock.runFor(5000)
  expect(calls).toHaveLength(count + 1)
  await page.evaluate(() => {
    Object.defineProperty(document, 'hidden', { configurable: true, value: false })
    document.dispatchEvent(new Event('visibilitychange'))
  })
  await page.clock.runFor(10)
  await expect.poll(() => calls.length).toBe(count + 2)
  await page.getByRole('link', { name: '首页', exact: true }).first().click()
  await expect(page.locator('.realtime-log')).toHaveCount(0)
  const afterLeave = calls.length
  await page.clock.runFor(5000)
  expect(calls).toHaveLength(afterLeave)
})

test('缓冲缺口、重启、页面容量和滚动跟随', async ({ page }) => {
  await page.clock.install()
  await page.clock.pauseAt(new Date(Date.now() + 1000))
  const { calls } = await setup(page, count => {
    if (count <= 5) return { events: Array.from({ length: 500 }, (_, index) => event((count - 1) * 500 + index + 1, `${count}-${index} ${'内容'.repeat(20)}\n`)), processId: 'process-one', nextCursor: count * 500, hasMore: count < 5 }
    if (count === 6) return { events: [event(3000)], processId: 'process-one', nextCursor: 3000, gap: true }
    return { events: [event(1, '新进程启动日志\n')], processId: 'process-two', nextCursor: 1, reset: true }
  })
  await page.goto(path)
  await expect(page.getByRole('heading', { name: '实时日志', exact: true })).toBeVisible()
  await page.clock.runFor(10)
  await expect(page.locator('.log-summary')).toContainText('保留 500 条')
  for (let index = 0; index < 4; index++) {
    await page.clock.runFor(150)
    await expect.poll(() => calls.length).toBe(index + 2)
    await expect(page.locator('.log-summary')).toContainText(`保留 ${Math.min(2000, (index + 2) * 500)} 条`)
  }
  await expect(page.locator('.log-notice')).toContainText('上限')
  expect(await page.locator('.log-chunk').count()).toBeLessThanOrEqual(50)
  const viewport = page.locator('.log-viewport')
  expect(await viewport.evaluate(element => element.clientHeight)).toBeGreaterThanOrEqual(320)
  await expect(page.locator('.log-footnote')).toBeInViewport({ ratio: 1 })
  await page.getByRole('button', { name: '回到顶部' }).click()
  await expect.poll(() => viewport.evaluate(element => element.scrollTop)).toBe(0)
  await expect(page.getByLabel('自动滚动')).not.toBeChecked()
  await page.clock.runFor(1100)
  await expect(page.locator('.log-notice')).toContainText('缓冲覆盖')
  await expect.poll(() => viewport.evaluate(element => element.scrollTop)).toBe(0)
  await page.getByRole('button', { name: '回到底部' }).click()
  await expect(page.getByLabel('自动滚动')).toBeChecked()
  await expect.poll(() => viewport.evaluate(element => element.scrollHeight - element.scrollTop - element.clientHeight)).toBeLessThan(2)
  await page.clock.runFor(1100)
  await expect(page.locator('.log-notice')).toContainText('服务已重启')
  await expect(page.locator('.log-viewport')).toContainText('新进程启动日志')
  await expect(page.locator('.log-summary')).toContainText('保留 1 条')
})

test('权限拒绝后停止请求，接口异常退避后恢复', async ({ page }) => {
  await page.clock.install()
  const { calls } = await setup(page, count => count === 1 ? { code: 500, msg: '模拟暂时不可用' } : count === 3 ? { code: 403, msg: '没有权限' } : { events: [event(1)], processId: 'process-one', nextCursor: 1 })
  await page.goto(path)
  await expect(page.getByRole('status')).toContainText('等待重试')
  await page.clock.runFor(1500)
  expect(calls).toHaveLength(1)
  await page.clock.runFor(600)
  await expect(page.locator('.log-viewport')).toContainText('运行事件 1')
  await page.clock.runFor(1100)
  await expect(page.getByRole('status')).toContainText('已停止')
  await page.clock.runFor(10000)
  expect(calls).toHaveLength(3)
})

test('四种代表主题下切换重建、可读和横向滚动', async ({ page }) => {
  const errors = []
  page.on('pageerror', error => errors.push(error.message))
  await setup(page, () => ({ events: [event(1, '完整长行日志 ' + 'abc'.repeat(500) + '\n    at demo.run(Demo.java:42)\n')], processId: 'process-one', nextCursor: 1 }))
  await page.goto(path)
  for (const theme of ['default', 'glassmorphism', 'dark-premium', 'minimalist-future']) {
    await page.evaluate(theme => document.querySelector('#app').__vue_app__.config.globalProperties.$store.dispatch('settings/changeSetting', { key: 'themeStyle', value: theme }), theme)
    await expect(page.locator('.log-viewport')).toContainText('Demo.java:42')
    await expect(page.locator('.realtime-log')).toHaveCSS('opacity', '1')
    const metrics = await page.locator('.log-viewport').evaluate(element => ({ color: getComputedStyle(element).color, background: getComputedStyle(element).backgroundColor, width: element.clientWidth, scroll: element.scrollWidth }))
    expect(metrics.color).not.toBe(metrics.background)
    expect(metrics.scroll).toBeGreaterThan(metrics.width)
    await page.screenshot({ path: `output/playwright/realtime-log-${theme}.png`, fullPage: true, animations: 'disabled' })
  }
  expect(errors).toEqual([])
})
