import { test, expect } from '@playwright/test'
import { createServer } from 'node:http'
import { authenticated } from './mock-api'

for (const code of [601, -1001, -1002, -1003]) {
  test('业务错误码提示后保留查询能力 ' + code, async ({ page }) => {
    const errors = []; page.on('pageerror', error => errors.push(error.message))
    await authenticated(page)
    let fail = true
    await page.route('**/dev-api/system/config/list?**', route => route.fulfill({ json: fail
      ? { code, msg: '测试业务错误' } : { code: 200, rows: [], total: 0 } }))
    const initialResponse = page.waitForResponse(result => result.url().includes('/dev-api/system/config/list'))
    await page.goto('/system/config', { waitUntil: 'domcontentloaded' })
    await initialResponse
    await expect(page.getByText('测试业务错误', { exact: true })).toBeVisible()
    await expect(page.locator('.el-loading-mask:visible')).toHaveCount(0)
    fail = false
    const response = page.waitForResponse(result => result.url().includes('/system/config/list'))
    await page.getByRole('button', { name: '搜索', exact: true }).click()
    await response
    expect(errors).toEqual([])
  })
}

for (const reason of ['network', 'timeout']) {
  test('网络故障或超时不会留下加载状态 ' + reason, async ({ page }) => {
    // 独立保留初始化和真实Axios超时的预算，不改变请求本身的十秒限制。
    test.setTimeout(60000)
    const errors = []; page.on('pageerror', error => errors.push(error.message))
    await authenticated(page)
    let timeoutServer
    if (reason === 'timeout') {
      // 让浏览器真正建立连接后等待正文，避免 WebKit 将 route.abort 当成网络故障。
      timeoutServer = createServer((request, response) => {
        response.setHeader('Access-Control-Allow-Origin', 'http://127.0.0.1:5178')
        response.setHeader('Access-Control-Allow-Headers', 'authorization,content-type')
        if (request.method === 'OPTIONS') { response.writeHead(204); response.end(); return }
        response.writeHead(200, { 'Content-Type': 'application/json' })
        response.flushHeaders()
      })
      await new Promise(resolve => timeoutServer.listen(0, '127.0.0.1', resolve))
    }
    try {
      await page.route('**/dev-api/system/config/list?**', route => reason === 'timeout'
        ? route.continue({ url: 'http://127.0.0.1:' + timeoutServer.address().port + '/timeout' })
        : route.abort('failed'))
      const requestStarted = page.waitForRequest(request => request.url().includes('/dev-api/system/config/list'))
      await page.goto('/system/config', { waitUntil: 'domcontentloaded' })
      await requestStarted
      await expect(page.getByText(reason === 'timeout' ? '系统接口请求超时,请联系管理员!' : '后端接口连接异常,请检查网络连接!', { exact: true })).toBeVisible({ timeout: 15000 })
      await expect(page.locator('.el-loading-mask:visible')).toHaveCount(0)
      expect(errors).toEqual([])
    } finally {
      if (timeoutServer) {
        timeoutServer.closeAllConnections()
        await new Promise(resolve => timeoutServer.close(resolve))
      }
    }
  })
}

test('401提示取消后结束查询加载，重试请求仍可成功', async ({ page }) => {
  const errors = []; page.on('pageerror', error => errors.push(error.message))
  await authenticated(page)
  let fail = true
  await page.route('**/dev-api/system/config/list?**', route => route.fulfill({ json: fail
    ? { code: 401, msg: '登录过期' } : { code: 200, rows: [], total: 0 } }))
  await page.goto('/system/config')
  const prompt = page.locator('.el-message-box')
  await expect(prompt.getByText('登录状态已过期，您可以继续留在该页面，或者重新登录', { exact: true })).toBeVisible()
  await prompt.getByRole('button', { name: '取消', exact: true }).click()
  await expect(page.locator('.el-loading-mask:visible')).toHaveCount(0)
  fail = false
  const response = page.waitForResponse(result => result.url().includes('/system/config/list'))
  await page.getByRole('button', { name: '搜索', exact: true }).click()
  await response
  expect(errors).toEqual([])
})

test('导出安全验证取消后不发请求，也不提示下载故障', async ({ page }) => {
  const requests = await authenticated(page, { googleApis: [{ apiMethod: 'POST', apiUrl: '/system/config/export' }] })
  const errors = []; page.on('pageerror', error => errors.push(error.message))
  await page.goto('/system/config')
  await page.getByRole('button', { name: '导出', exact: true }).click()
  await page.locator('.el-message-box').getByRole('button', { name: '取消', exact: true }).click()
  await expect(page.locator('.el-message-box')).toBeHidden()
  await expect(page.locator('.el-loading-mask:visible')).toHaveCount(0)
  await expect(page.getByText('下载文件出现错误，请联系管理员！', { exact: true })).toHaveCount(0)
  expect(requests.filter(request => request.path === '/system/config/export')).toHaveLength(0)
  expect(errors).toEqual([])
})
