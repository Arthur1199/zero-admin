import { test, expect } from '@playwright/test'
import { authenticated } from './mock-api'

for (const [path, api] of [
  ['/system/config', '/system/config/list'],
  ['/system/user', '/system/user/list'],
  ['/monitor/operlog', '/monitor/operlog/list'],
  ['/system/apiAuth', '/system/apiAuth/list'],
  ['/system/dept', '/system/dept/list'],
  ['/system/menu', '/system/menu/list'],
  ['/system/dict', '/system/dict/type/list'],
  ['/system/dict-data/index/1', '/system/dict/data/list'],
  ['/system/google', '/system/google/list'],
  ['/system/ipWhite', '/system/ipWhite/list'],
  ['/system/notice', '/system/notice/list'],
  ['/system/post', '/system/post/list'],
  ['/system/role', '/system/role/list'],
  ['/system/role-auth/user/2', '/system/role/authUser/allocatedList'],
  ['/monitor/job', '/monitor/job/list'],
  ['/monitor/job-log/index/2', '/monitor/jobLog/list'],
  ['/monitor/logininfor', '/monitor/logininfor/list'],
  ['/monitor/online', '/monitor/online/list'],
  ['/tool/gen', '/tool/gen/list']
]) {
  test('查询失败后结束加载并允许重试 ' + path, async ({ page }) => {
    const errors = []; page.on('pageerror', error => errors.push(error.message))
    await authenticated(page)
    let unavailable = true
    await page.route(new RegExp('/dev-api' + api + '(?:\\?.*)?$'), route => route.fulfill({ json: unavailable
      ? { code: 500, msg: '测试查询失败' }
      : { code: 200, rows: [], total: 0, data: [] } }))
    // 从发起导航时观察短暂提示，不等待无关图片加载后才检查已经消失的消息。
    await Promise.all([
      page.getByText('测试查询失败', { exact: true }).waitFor({ state: 'visible' }),
      page.goto(path, { waitUntil: 'domcontentloaded' })
    ])
    await expect(page.locator('.app-main .el-loading-mask').first()).not.toBeVisible()
    unavailable = false
    const response = page.waitForResponse(result => result.url().includes('/dev-api' + api) && result.status() === 200)
    await page.getByRole('button', { name: '搜索', exact: true }).click()
    await response
    await expect(page.locator('.app-main .el-loading-mask').first()).not.toBeVisible()
    expect(errors).toEqual([])
  })
}

for (const path of ['/monitor/server', '/monitor/cache']) {
  test('监控查询失败释放全屏加载层 ' + path, async ({ page }) => {
    test.setTimeout(60000)
    const errors = []; page.on('pageerror', error => errors.push(error.message))
    await authenticated(page)
    await page.route('**/dev-api' + path, route => route.fulfill({ json: { code: 500, msg: '测试监控失败' } }))
    await Promise.all([
      page.getByText('测试监控失败', { exact: true }).waitFor({ state: 'visible' }),
      page.goto(path, { waitUntil: 'domcontentloaded' })
    ])
    await expect(page.locator('.el-loading-mask:visible')).toHaveCount(0)
    await page.goto('/system/config', { waitUntil: 'domcontentloaded' })
    await page.getByRole('button', { name: '搜索', exact: true }).waitFor({ state: 'visible' })
    await expect(page.getByRole('button', { name: '搜索', exact: true })).toBeEnabled()
    expect(errors).toEqual([])
  })
}

test('缓存名称与键名查询失败后可以重试', async ({ page }) => {
  const errors = []; page.on('pageerror', error => errors.push(error.message))
  await authenticated(page)
  let failedNames = true, failedKeys = true
  await page.route('**/dev-api/monitor/cache/getNames', route => route.fulfill({ json: failedNames
    ? { code: 500, msg: '缓存名称查询失败' }
    : { code: 200, data: [{ cacheName: 'mock:', remark: '测试缓存' }] } }))
  await page.route('**/dev-api/monitor/cache/getKeys/mock:', route => route.fulfill({ json: failedKeys
    ? { code: 500, msg: '缓存键名查询失败' } : { code: 200, data: ['mock:key'] } }))
  await Promise.all([
    page.getByText('缓存名称查询失败', { exact: true }).waitFor({ state: 'visible' }),
    page.goto('/monitor/cache/list', { waitUntil: 'domcontentloaded' })
  ])
  await expect(page.locator('.el-loading-mask:visible')).toHaveCount(0)
  failedNames = false
  await page.locator('.el-card__header').first().getByRole('button').click()
  await page.locator('.el-table__row').getByText('mock', { exact: true }).click()
  await expect(page.getByText('缓存键名查询失败', { exact: true })).toBeVisible()
  await expect(page.locator('.el-loading-mask:visible')).toHaveCount(0)
  failedKeys = false
  await page.locator('.el-table__row').getByText('mock', { exact: true }).click()
  await expect(page.locator('.el-table__row').getByText('key', { exact: true })).toBeVisible()
  expect(errors).toEqual([])
})

for (const [path, api, trigger] of [
  ['/system/role-auth/user/2', '/system/role/authUser/unallocatedList', '添加用户'],
  ['/tool/gen', '/tool/gen/db/list', '导入']
]) {
  test('辅助选择弹窗查询失败不产生未处理异常 ' + api, async ({ page }) => {
    const errors = []; page.on('pageerror', error => errors.push(error.message))
    await authenticated(page)
    let unavailable = true
    await page.route(new RegExp('/dev-api' + api + '(?:\\?.*)?$'), route => route.fulfill({ json: unavailable
      ? { code: 500, msg: '选择列表查询失败' } : { code: 200, rows: [], total: 0 } }))
    await page.goto(path)
    await page.getByRole('button', { name: trigger, exact: true }).click()
    await expect(page.getByText('选择列表查询失败', { exact: true })).toBeVisible()
    unavailable = false
    const response = page.waitForResponse(result => result.url().includes('/dev-api' + api))
    await page.locator('.el-dialog:visible').getByRole('button', { name: '搜索', exact: true }).click()
    await response
    expect(errors).toEqual([])
  })
}
