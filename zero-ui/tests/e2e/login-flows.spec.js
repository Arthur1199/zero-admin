import { test, expect } from '@playwright/test'
import { mockApi } from './mock-api'

const png = Buffer.from('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+aV1cAAAAASUVORK5CYII=', 'base64')

async function loginFields(page) {
  await page.getByPlaceholder('账号', { exact: true }).fill('mock-login')
  await page.getByPlaceholder('密码', { exact: true }).fill('mock-password')
}

test('图形及谷歌登录码校验、失败刷新验证码、重试返回原地址', async ({ page }) => {
  test.setTimeout(60000)
  const errors = []; page.on('pageerror', error => errors.push(error.message))
  await mockApi(page)
  let captchaCount = 0
  const logins = []
  await page.route('**/dev-api/captchaImage', route => route.fulfill({ json: {
    code: 200, captchaEnabled: true, googleAuthEnabled: true, uuid: 'mock-uuid-' + (++captchaCount), img: png.toString('base64')
  } }))
  await page.route('**/dev-api/login', route => {
    logins.push(route.request().postDataJSON())
    return route.fulfill({ json: logins.length === 1 ? { code: -1002, msg: '模拟登录动态码错误' } : { code: 200, token: 'mock-token' } })
  })
  await page.goto('/login?redirect=/system/config')
  await loginFields(page)
  const captcha = page.getByPlaceholder('验证码', { exact: true })
  const google = page.getByPlaceholder('谷歌验证码(首次绑定请输入123456)', { exact: true })
  await expect(captcha).toBeVisible()
  await captcha.fill('mock-captcha')
  await google.fill('12')
  await page.getByRole('button', { name: /登\s*录/ }).click()
  await expect(page.getByText('请输入6位数字验证码', { exact: true })).toBeVisible()
  expect(logins).toHaveLength(0)
  await google.fill('654321')
  await page.getByRole('button', { name: /登\s*录/ }).click()
  await expect(page.getByText('模拟登录动态码错误', { exact: true })).toBeVisible()
  await expect.poll(() => captchaCount).toBe(2)
  await expect(page.getByRole('button', { name: /登\s*录/ })).toBeEnabled()
  await google.fill('987654')
  const entered = page.waitForURL(url => url.pathname === '/system/config')
  await page.getByRole('button', { name: /登\s*录/ }).click()
  await entered
  await expect(page).toHaveURL(url => url.pathname === '/system/config')
  await expect(page.locator('.app-main .el-table')).toBeVisible()
  expect(logins[0]).toMatchObject({ username: 'mock-login', password: 'mock-password', code: 'mock-captcha', googleCode: '654321', uuid: 'mock-uuid-1' })
  expect(logins[1]).toMatchObject({ googleCode: '987654', uuid: 'mock-uuid-2' })
  expect(errors).toEqual([])
})

test('谷歌首次绑定取消不提交，失败释放按钮并保留输入，成功后重新登录', async ({ page }) => {
  const errors = []; page.on('pageerror', error => errors.push(error.message))
  await mockApi(page)
  const binds = []
  await page.route('**/dev-api/login', route => route.fulfill({ json: { code: -1001, msg: '需要绑定动态码' } }))
  await page.route('**/dev-api/google/generateGaImage?**', route => route.fulfill({ contentType: 'image/png', body: png }))
  await page.route('**/dev-api/google/bind', route => {
    binds.push(route.request().postDataJSON())
    return route.fulfill({ json: binds.length === 1 ? { code: 500, msg: '模拟绑定失败' } : { code: 200 } })
  })
  await page.goto('/login')
  await loginFields(page)
  const open = async () => {
    await page.getByRole('button', { name: /登\s*录/ }).click()
    await expect(page.getByRole('dialog')).toBeVisible()
  }
  await open()
  const dialog = page.getByRole('dialog'), code = dialog.getByPlaceholder('请输入6位谷歌验证码')
  await expect(dialog.locator('.google-qrcode')).toBeVisible()
  await code.fill('12')
  await dialog.getByRole('button', { name: /确\s*定/ }).click()
  await expect(dialog.getByText('请输入6位数字验证码', { exact: true })).toBeVisible()
  expect(binds).toHaveLength(0)
  await dialog.getByRole('button', { name: /取\s*消/ }).click()
  await expect(dialog).not.toBeVisible()
  expect(binds).toHaveLength(0)
  await open()
  await code.fill('654321')
  await dialog.getByRole('button', { name: /确\s*定/ }).click()
  await expect(page.getByText('模拟绑定失败', { exact: true })).toBeVisible()
  await expect(dialog.getByRole('button', { name: /确\s*定/ })).toBeEnabled()
  await expect(code).toHaveValue('654321')
  // 防重复提交窗口为一秒；失败后重试仍遵守原有节流规则。
  await page.waitForTimeout(1100)
  await dialog.getByRole('button', { name: /确\s*定/ }).click()
  await expect(dialog).not.toBeVisible()
  await expect(page.getByText('绑定成功，请重新登录', { exact: true })).toBeVisible()
  expect(binds).toEqual([{ userName: 'mock-login', googleCode: '654321' }, { userName: 'mock-login', googleCode: '654321' }])
  await expect(page).toHaveURL(/\/login$/)
  expect((await page.context().cookies()).find(cookie => cookie.name === 'Admin-Token')).toBeUndefined()
  expect(errors).toEqual([])
})

for (const expired of [false, true]) {
  test('密码策略提示确认跳转个人中心修改密码Tab ' + (expired ? 'expired' : 'initial'), async ({ page }) => {
    test.setTimeout(60000)
    await mockApi(page)
    await page.route('**/dev-api/getInfo', route => route.fulfill({ json: {
      code: 200, user: { userId: 1, userName: 'mock-user', nickName: '模拟用户', avatar: '' }, roles: ['admin'], permissions: ['*:*:*'],
      isDefaultModifyPwd: !expired, isPasswordExpired: expired
    } }))
    await page.goto('/login')
    await loginFields(page)
    await page.getByRole('button', { name: /登\s*录/ }).click()
    await page.waitForURL(url => url.pathname === '/index', { waitUntil: 'domcontentloaded' })
    await page.locator('.zero-home').waitFor({ state: 'visible' })
    await expect(page.locator('.zero-home')).toBeVisible()
    const dialog = page.getByRole('dialog', { name: '安全提示' })
    await expect(dialog).toContainText(expired ? '您的密码已过期' : '您的密码还是初始密码')
    await dialog.getByRole('button', { name: '确定', exact: true }).click()
    await expect(page).toHaveURL(/\/user\/profile\?activeTab=resetPwd$/)
    await expect(page.getByRole('tab', { name: '修改密码', exact: true })).toHaveAttribute('aria-selected', 'true')
    // 仅验证跳转和激活页签，不输入或提交新密码。
  })
}
