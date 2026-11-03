import { test, expect } from '@playwright/test'
import { authenticated, mockApi, paths } from './mock-api'

test('登录页和登录后动态菜单', async ({ page }) => {
  test.setTimeout(60000)
  const errors = []; page.on('pageerror', error => errors.push(error.message))
  await mockApi(page)
  await page.goto('/login', { waitUntil: 'domcontentloaded' })
  await page.locator('.login-form').waitFor({ state: 'visible' })
  await page.locator('input[type=password]').fill('mock-password')
  const entered = page.waitForURL(url => url.pathname === '/index', { waitUntil: 'domcontentloaded' })
  await page.getByRole('button', { name: /登\s*录/ }).click()
  await entered
  await page.locator('.zero-home').waitFor({ state: 'visible' })
  await expect(page.locator('.sidebar-container')).toBeVisible()
  await expect(page.locator('.zero-home')).toBeVisible()
  expect(errors).toEqual([])
})

test('记住密码后重新打开登录页可以解密并登录', async ({ page }) => {
  test.setTimeout(60000)
  const errors = []; page.on('pageerror', error => errors.push(error.message))
  await mockApi(page)
  await page.goto('/login')
  await page.locator('input[type=password]').fill('mock-password')
  await page.getByText('记住密码', { exact: true }).click()
  await expect(page.getByRole('checkbox', { name: '记住密码' })).toBeChecked()
  await page.getByRole('button', { name: /登\s*录/ }).click()
  await page.waitForURL(url => url.pathname === '/index', { waitUntil: 'domcontentloaded' })
  await page.locator('.zero-home').waitFor({ state: 'visible' })
  await expect(page.locator('.zero-home')).toBeVisible()
  await page.context().clearCookies({ name: 'Admin-Token' })
  await page.goto('/login')
  await expect(page.locator('input[type=password]')).toHaveValue('mock-password')
  await expect(page.getByRole('checkbox', { name: '记住密码' })).toBeChecked()
  await page.getByRole('button', { name: /登\s*录/ }).click()
  await page.waitForURL(url => url.pathname === '/index', { waitUntil: 'domcontentloaded' })
  await page.locator('.zero-home').waitFor({ state: 'visible' })
  await expect(page.locator('.zero-home')).toBeVisible()
  expect(errors).toEqual([])
})

test('动态菜单页面逐项加载，没有未处理异常', async ({ page }) => {
  test.setTimeout(120000)
  const errors = []; page.on('pageerror', error => errors.push(error.message))
  await authenticated(page)
  for (const path of paths) {
    // 外嵌页面需要真实下游网页，不把其加载结果计入mock验收。
    if (['/monitor/druid', '/tool/swagger'].includes(path)) continue
    await page.goto(path)
    await expect(page.locator('.app-main'), path + ': ' + errors.join(';')).toBeVisible()
    await expect(page).not.toHaveURL(/\/(?:login|404)(?:[/?#]|$)/)
    await expect(page.locator('.app-main')).not.toBeEmpty()
    await page.waitForTimeout(200)
  }
  expect(errors).toEqual([])
})

test('八种主题切换及弹窗、表格可见', async ({ page }) => {
  test.setTimeout(120000)
  const errors = []; page.on('pageerror', error => errors.push(error.message))
  await authenticated(page)
  await page.goto('/system/config')
  await page.locator('.navbar .setting').click()
  const themes = { glassmorphism: '毛玻璃拟态风', 'dark-premium': '高端暗黑风', 'minimalist-future': '极简未来风', default: '原始风格', 'luxury-business': '轻奢商务风', 'vibrant-tech': '活力科技风', 'warm-nature': '温馨自然风', 'material-design': '质感设计风' }
  for (const [theme, label] of Object.entries(themes)) {
    if (!await page.locator('.el-drawer').isVisible()) await page.locator('.navbar .setting').click()
    await page.locator('.drawer-select').first().click()
    await page.getByRole('option', { name: label, exact: true }).click()
    if (theme === 'default') await expect(page.locator('html > body')).not.toHaveClass(/theme-/)
    else await expect(page.locator('html > body')).toHaveClass(new RegExp('theme-' + theme))
    await expect(page.locator('.app-main .fade-transform-leave-active, .app-main .fade-transform-enter-active')).toHaveCount(0)
    await expect(page.locator('.el-table')).toBeVisible()
    await page.getByRole('button', { name: '新增', exact: true }).click()
    await expect(page.locator('.el-dialog')).toBeVisible()
    // 等待Element Plus弹窗淡入结束，截图反映最终可读性。
    await page.waitForTimeout(400)
    await page.screenshot({ path: `output/playwright/${test.info().project.name}-${theme}.png`, fullPage: true })
    await page.locator('.el-dialog').getByRole('button', { name: '取 消' }).click()
  }
  expect(errors).toEqual([])
})

test('权限指令保留普通账号的按钮边界', async ({ page }) => {
  test.setTimeout(60000)
  const errors = []; page.on('pageerror', error => errors.push(error.message))
  await authenticated(page, { roles: ['common'], permissions: ['system:config:list'] })
  const loaded = page.waitForResponse(response => response.url().includes('/dev-api/system/config/list'))
  await page.goto('/system/config', { waitUntil: 'domcontentloaded' })
  await loaded
  await page.locator('.el-table').waitFor({ state: 'visible' })
  await expect(page.locator('.el-table')).toBeVisible()
  await expect(page.getByRole('button', { name: '新增', exact: true })).toHaveCount(0)
  await expect(page.getByRole('button', { name: '搜索', exact: true })).toBeVisible()
  expect(errors).toEqual([])
})

test('谷歌保护请求确认与取消，参数和Token不变', async ({ page }) => {
  const requests = await authenticated(page, { googleApis: [{ apiMethod: 'POST', apiUrl: '/system/config' }] })
  await page.goto('/system/config')
  await page.getByRole('button', { name: '新增', exact: true }).click()
  const dialog = page.locator('.el-dialog')
  await dialog.getByPlaceholder('请输入参数名称').fill('测试参数')
  await dialog.getByPlaceholder('请输入参数键名').fill('mock.config.key')
  await dialog.getByPlaceholder('请输入参数键值').fill('mock-value')
  await dialog.getByRole('button', { name: /确\s*定/ }).click()
  const prompt = page.locator('.el-message-box')
  await expect(prompt).toBeVisible()
  await prompt.getByRole('button', { name: '取消', exact: true }).click()
  await expect(prompt).not.toBeVisible()
  expect(requests.filter(request => request.path === '/system/config' && request.method === 'POST')).toHaveLength(0)
  await dialog.getByRole('button', { name: /确\s*定/ }).click()
  await prompt.locator('input').fill('123456')
  await prompt.getByRole('button', { name: '确定', exact: true }).click()
  await expect(dialog).not.toBeVisible()
  const sent = requests.find(request => request.path === '/system/config' && request.method === 'POST')
  expect(sent.headers.authorization).toBe('Bearer mock-token')
  expect(sent.headers['x-google-code']).toBe('123456')
  expect(JSON.parse(sent.body)).toMatchObject({ configName: '测试参数', configKey: 'mock.config.key', configValue: 'mock-value' })
})

test('文件管理直传保持上传接口和Token', async ({ page }) => {
  const requests = await authenticated(page)
  await page.goto('/systemTools/fileManagement')
  await page.locator('input[type=file]').setInputFiles({ name: 'test.txt', mimeType: 'text/plain', buffer: Buffer.from('mock-file') })
  await expect.poll(() => requests.some(request => request.path === '/systemTools/fileManagement/upload')).toBe(true)
  expect(requests.find(request => request.path.endsWith('/upload')).headers.authorization).toBe('Bearer mock-token')
})

test('文件服务不可达时提示错误并结束加载，可以重新查询', async ({ page }) => {
  const errors = []; page.on('pageerror', error => errors.push(error.message))
  await authenticated(page)
  let unavailable = true
  await page.route('**/dev-api/systemTools/fileManagement/list?**', route => route.fulfill({ json: unavailable
    ? { code: 500, msg: '文件服务暂不可用' }
    : { code: 200, rows: [], total: 0 } }))
  await page.goto('/systemTools/fileManagement')
  await expect(page.getByText('文件服务暂不可用', { exact: true })).toBeVisible()
  await expect(page.locator('.app-main .el-loading-mask')).not.toBeVisible()
  unavailable = false
  await page.getByRole('button', { name: '搜索', exact: true }).click()
  await expect(page.locator('.app-main .el-loading-mask')).not.toBeVisible()
  expect(errors).toEqual([])
})

test('导出Vue3组件可以挂载并填写', async ({ page }) => {
  const errors = []; page.on('pageerror', error => errors.push(error.message))
  await page.goto('/tests/fixtures/generated.html')
  await expect(page.locator('.el-form')).toBeVisible()
  await page.locator('.el-input input').first().fill('导出测试')
  await expect(page.locator('.el-input input').first()).toHaveValue('导出测试')
  expect(errors).toEqual([])
})

test('在线设计器拖入、复制和导出', async ({ page }) => {
  const errors = []; page.on('pageerror', error => errors.push(error.message))
  await authenticated(page)
  await page.goto('/tool/build')
  await expect(page.locator('.drawing-board')).toBeVisible()
  const initialCount = await page.locator('.drawing-item').count()
  await page.locator('.components-item').first().click()
  await expect(page.locator('.drawing-item')).toHaveCount(initialCount + 1)
  await page.locator('.drawing-item-copy').last().click()
  await expect(page.locator('.drawing-item')).toHaveCount(initialCount + 2)
  await page.getByRole('button', { name: '导出vue文件' }).click()
  const dialog = page.getByRole('dialog')
  await expect(dialog).toBeVisible()
  const download = page.waitForEvent('download')
  await dialog.getByRole('button', { name: '确定', exact: true }).click()
  await download
  expect(errors).toEqual([])
})

test('登录过期跳转及未知路径', async ({ page }) => {
  await mockApi(page)
  await page.goto('/system/config')
  await expect(page).toHaveURL(/\/login\?redirect=/)
  await page.context().addCookies([{ name: 'Admin-Token', value: 'mock-token', url: 'http://127.0.0.1:5178' }])
  await page.goto('/unknown-menu')
  await expect(page).toHaveURL(/\/404/)
})
