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

  async function openReadOnlyList(label, api) {
    const menu = menuLinks.find(link => link.label === label)
    expect(menu, label + '菜单').toBeTruthy()
    const loaded = page.waitForResponse(response => new URL(response.url()).pathname === '/dev-api' + api)
    await page.goto(menu.path)
    const response = await (await loaded).json()
    expect(response.code).toBe(200)
    await expect(page.locator('.app-main .el-loading-mask:visible')).toHaveCount(0)
    await expect(page.locator('.el-table__row')).toHaveCount(response.rows.length)
    return response.rows.length
  }

  async function recordReadOnlyResult(status) {
    await Promise.allSettled([...pendingResponses])
    results.push({ status })
    expect(blocked).toEqual([])
    expect(failures).toEqual([])
    expect(startErrors).toEqual([])
  }

  test('真实任务详情、修改草稿及Cron生成器只读打开并取消', async () => {
    const count = await openReadOnlyList('定时任务', '/monitor/job/list')
    if (!count) results.push({ status: '待验证任务详情：当前没有可读任务记录' })
    test.skip(!count, '当前没有可读任务记录')
    await page.getByRole('button', { name: '更多', exact: true }).first().click()
    await page.getByRole('menuitem', { name: '任务详细', exact: true }).click()
    const detail = page.getByRole('dialog', { name: '任务详细', exact: true })
    await expect(detail).toBeVisible()
    await expect(detail).toContainText('cron表达式')
    await detail.getByRole('button', { name: '关 闭', exact: true }).click()
    await page.locator('.el-table__row').first().getByRole('button', { name: '修改', exact: true }).click()
    const edit = page.getByRole('dialog', { name: '修改任务', exact: true })
    await expect(edit.getByPlaceholder('请输入任务名称')).not.toHaveValue('')
    const original = await edit.getByPlaceholder('请输入cron执行表达式').inputValue()
    await edit.getByRole('button', { name: '生成表达式', exact: true }).click()
    const cron = page.getByRole('dialog', { name: 'Cron表达式生成器', exact: true })
    await expect(cron.getByRole('tab')).toHaveCount(7)
    await expect(cron.locator('.popup-result-scroll li')).toHaveCount(5)
    await cron.getByRole('button', { name: '取消', exact: true }).click()
    await expect(edit.getByPlaceholder('请输入cron执行表达式')).toHaveValue(original)
    await edit.getByRole('button', { name: '取 消', exact: true }).click()
    await recordReadOnlyResult('真实任务详情、修改及Cron草稿打开取消通过，无任务写入')
  })

  test('真实角色菜单和部门权限树回显后取消，不保存权限', async () => {
    await openReadOnlyList('角色管理', '/system/role/list')
    const edits = page.locator('.el-table__row').getByRole('button', { name: '修改', exact: true })
    if (!await edits.count()) results.push({ status: '待验证角色树：当前没有可读取的编辑入口' })
    test.skip(!await edits.count(), '当前没有可读取的编辑入口')
    await edits.first().click()
    const edit = page.getByRole('dialog', { name: '修改角色', exact: true })
    await expect(edit.getByPlaceholder('请输入角色名称')).not.toHaveValue('')
    await expect(edit.getByRole('tree')).toBeVisible()
    await expect(edit.getByRole('treeitem').first()).toBeVisible()
    await edit.getByRole('button', { name: '取 消', exact: true }).click()
    await page.getByRole('button', { name: '更多', exact: true }).first().click()
    await page.getByRole('menuitem', { name: '数据权限', exact: true }).click()
    const scope = page.getByRole('dialog', { name: '分配数据权限', exact: true })
    await expect(scope).toBeVisible()
    // 非自定义范围时树由页面隐藏，仍检查实际树已挂载，保留原权限范围。
    await expect(scope.locator('.el-tree')).toBeAttached()
    await scope.getByRole('button', { name: '取 消', exact: true }).click()
    await recordReadOnlyResult('真实角色菜单与部门树回显、取消通过，无权限保存')
  })

  test('真实字典子页保持入口指定的字典类型', async () => {
    const count = await openReadOnlyList('字典管理', '/system/dict/type/list')
    if (!count) results.push({ status: '待验证字典子页：当前没有字典类型' })
    test.skip(!count, '当前没有字典类型')
    const link = page.locator('.el-table a[href^="/system/dict-data/"]').first()
    const dictType = (await link.textContent()).trim()
    const loaded = page.waitForResponse(response => new URL(response.url()).pathname === '/dev-api/system/dict/data/list')
    await link.click()
    const response = await loaded
    expect(new URL(response.url()).searchParams.get('dictType')).toBe(dictType)
    expect((await response.json()).code).toBe(200)
    await expect(page.locator('.app-main .el-loading-mask:visible')).toHaveCount(0)
    await recordReadOnlyResult('真实字典子页类型上下文与列表查询通过')
  })

  test('公开系统字典缓存详情三个字段只读，不读取凭据缓存', async () => {
    const menu = menuLinks.find(link => /\/(?:cacheList|cache\/list)$/.test(link.path))
    expect(menu, '缓存列表菜单').toBeTruthy()
    const namesLoaded = page.waitForResponse(response => new URL(response.url()).pathname === '/dev-api/monitor/cache/getNames')
    await page.goto(menu.path)
    expect((await (await namesLoaded).json()).code).toBe(200)
    const names = page.locator('.el-card').first()
    const keys = page.locator('.el-card').nth(1)
    await names.locator('.el-table__body .cell').filter({ hasText: /^sys_dict$/ }).click()
    await expect(keys.locator('.el-loading-mask:visible')).toHaveCount(0)
    const key = keys.locator('.el-table__body .cell').filter({ hasText: /^sys_user_sex$/ })
    if (!await key.count()) results.push({ status: '待验证缓存内容：公开字典缓存键不存在' })
    test.skip(!await key.count(), '公开字典缓存键不存在；不改读登录或验证码缓存')
    await key.click()
    const detail = page.locator('.el-card').nth(2)
    await expect(detail.locator('textarea')).not.toHaveValue('')
    for (const input of await detail.locator('input,textarea').all()) await expect(input).toHaveJSProperty('readOnly', true)
    await recordReadOnlyResult('公开字典缓存内容回显及三个只读字段通过，无缓存清理')
  })
})
