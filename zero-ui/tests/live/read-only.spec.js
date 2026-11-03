import { test, expect } from '@playwright/test'
import fs from 'node:fs/promises'

test.describe.serial('真实Java服务只读验证', () => {
  let context, page, menuLinks, startErrors, loggedIn = false
  const blocked = [], results = [], failures = [], pendingResponses = new Set()

  test.beforeAll(async ({ browser }, testInfo) => {
    context = await browser.newContext({ baseURL: testInfo.project.use.baseURL })
    page = await context.newPage()
    startErrors = []
    page.on('pageerror', error => startErrors.push(error.message))
    await page.route('**/dev-api/**', route => {
      const request = route.request(), url = new URL(request.url())
      const method = request.method(), path = url.pathname.replace('/dev-api', '')
      if (['GET', 'HEAD', 'OPTIONS'].includes(method) || (method === 'POST' && ['/login', '/logout'].includes(path))) return route.continue()
      blocked.push({ method, path })
      return route.abort('blockedbyclient')
    })
    page.on('response', response => {
      const path = new URL(response.url()).pathname
      if (!path.startsWith('/dev-api/') || response.request().method() !== 'GET') return
      const work = (async () => {
        if (response.status() >= 400) failures.push({ path, status: response.status() })
        if (!response.headers()['content-type']?.includes('json')) return
        try {
          const { code } = await response.json()
          if (code && code !== 200) failures.push({ path, code })
        } catch { failures.push({ path, reason: 'JSON响应解析失败' }) }
      })()
      pendingResponses.add(work)
      work.finally(() => pendingResponses.delete(work))
    })
    const captcha = page.waitForResponse(response => new URL(response.url()).pathname.endsWith('/captchaImage'))
    await page.goto('/login')
    const configuration = await (await captcha).json()
    if (configuration.captchaEnabled || configuration.googleAuthEnabled) {
      throw new Error('真实登录需要人工验证码；本脚本未尝试解决验证码或绕过验证')
    }
    await expect(page.locator('.login-form')).toBeVisible()
    try {
      await page.getByPlaceholder('账号', { exact: true }).fill(process.env.ZERO_LIVE_USERNAME)
      await page.getByPlaceholder('密码', { exact: true }).fill(process.env.ZERO_LIVE_PASSWORD)
    } catch { throw new Error('登录输入失败；已隐藏输入值') }
    const remember = page.getByRole('checkbox', { name: '记住密码' })
    if (await remember.isChecked()) await page.getByText('记住密码', { exact: true }).click()
    await page.getByRole('button', { name: /登\s*录/ }).click()
    await expect(page.locator('.zero-home')).toBeVisible({ timeout: 30000 })
    loggedIn = true
    if (await page.locator('.el-message-box:visible').count()) await page.locator('.el-message-box:visible').getByRole('button', { name: '取消', exact: true }).click()
    menuLinks = await page.locator('.sidebar-container a[href]').evaluateAll(anchors => {
      const seen = new Set()
      return anchors.flatMap(anchor => {
        const url = new URL(anchor.getAttribute('href'), location.href)
        if (url.origin !== location.origin || seen.has(url.pathname)) return []
        seen.add(url.pathname)
        return [{ path: url.pathname, label: anchor.textContent.trim() }]
      })
    })
    expect(menuLinks.length).toBeGreaterThan(20)
  })

  test.afterAll(async ({}, testInfo) => {
    await Promise.allSettled([...pendingResponses])
    // 仅退出此脚本创建的会话，不使用用户浏览器Cookie，也不导出storageState。
    try {
      if (loggedIn && page && !page.isClosed() && !new URL(page.url()).pathname.startsWith('/login')) {
        await page.locator('.navbar .avatar-wrapper').click({ timeout: 5000 })
        const logout = page.getByText('退出登录', { exact: true })
        await expect(logout).toBeVisible({ timeout: 5000 })
        await logout.click()
        const box = page.locator('.el-message-box:visible')
        await expect(box).toBeVisible()
        await box.getByRole('button', { name: '确定', exact: true }).click()
        await expect(page).toHaveURL(/\/login/, { timeout: 15000 })
        results.push({ status: '脚本独立会话退出通过' })
      }
    } finally {
      await fs.mkdir('output/playwright/live-readonly', { recursive: true })
      await fs.writeFile(`output/playwright/live-readonly/${testInfo.project.name}.json`, JSON.stringify({
        results, blocked, failures, errors: startErrors
      }, null, 2), 'utf8')
      await context?.close()
    }
  })

  test('首页、动态菜单深层地址和刷新均连接真实服务', async () => {
    await expect(page.getByRole('heading', { name: 'Zero管理框架', exact: true })).toBeVisible()
    const failedPages = []
    for (const link of menuLinks) {
      if (/\/druid$|\/swagger$/.test(link.path)) {
        results.push({ ...link, status: '待验证独立嵌入服务' })
        continue
      }
      try {
        await page.goto(link.path)
        await expect(page.locator('.app-main')).toBeVisible()
        await expect(page.locator('.app-main')).not.toBeEmpty()
        await expect(page).not.toHaveURL(/\/(login|404)(?:[/?#]|$)/)
        await expect(page.locator('.el-loading-mask:visible')).toHaveCount(0, { timeout: 15000 })
        await Promise.allSettled([...pendingResponses])
        results.push({ ...link, status: '只读页面加载通过' })
      } catch {
        failedPages.push(link.path)
        results.push({ ...link, status: '页面加载失败' })
      }
    }
    expect(failedPages).toEqual([])
    expect(blocked).toEqual([])
    expect(failures).toEqual([])
    expect(startErrors).toEqual([])
  })

  test('参数查询重置与业务字典展开使用真实响应', async () => {
    const config = menuLinks.find(link => link.label === '后台参数设置')
    const bizDict = menuLinks.find(link => link.label === '业务参数设置')
    expect(config).toBeTruthy()
    expect(bizDict).toBeTruthy()
    await page.goto(config.path)
    await page.getByPlaceholder('请输入参数名称', { exact: true }).fill('readonly-unmatched-browser-check')
    const query = page.waitForResponse(response => {
      const url = new URL(response.url())
      return url.pathname.endsWith('/system/config/list') && url.searchParams.get('configName') === 'readonly-unmatched-browser-check'
    })
    await page.getByRole('button', { name: '搜索', exact: true }).click()
    const { code, total } = await (await query).json()
    expect(code).toBe(200)
    expect(total).toBe(0)
    await expect(page.locator('.el-table__row')).toHaveCount(0)
    const reset = page.waitForResponse(response => {
      const url = new URL(response.url())
      return url.pathname.endsWith('/system/config/list') && !url.searchParams.has('configName')
    })
    await page.getByRole('button', { name: '重置', exact: true }).click()
    const resetResponse = await (await reset).json()
    expect(resetResponse.code).toBe(200)
    await expect(page.locator('.el-table__row')).toHaveCount(resetResponse.rows.length)
    await expect(page.getByPlaceholder('请输入参数名称', { exact: true })).toHaveValue('')
    await expect(page.locator('.el-loading-mask:visible')).toHaveCount(0)
    await page.goto(bizDict.path)
    await expect(page.locator('.biz-dict-page')).toBeVisible()
    await page.locator('.biz-dict-page > .el-table .el-table__expand-icon').first().click()
    await expect(page.locator('.dict-item-table')).toBeVisible()
    await expect(page.locator('.el-loading-mask:visible')).toHaveCount(0)
    await Promise.allSettled([...pendingResponses])
    results.push({ status: '真实参数查询重置与业务字典展开通过' })
    expect(blocked).toEqual([])
    expect(failures).toEqual([])
    expect(startErrors).toEqual([])
  })
})
