import { test, expect } from '@playwright/test'
import { authenticated, mockApi } from './mock-api'

const image = 'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+aV1cAAAAASUVORK5CYII='
const account = { userId: 2, nickName: '模拟用户', userName: 'mock-user' }
const roles = [{ roleId: 2, roleName: '模拟角色', roleKey: 'mock-role', status: '0', flag: true }]
const white = { id: 2, ipAddr: '192.0.2.2', ipRegion: '模拟地区', status: '0', remark: '模拟记录' }

function errorsOf(page) {
  const errors = []
  page.on('pageerror', error => errors.push(error.message))
  return errors
}

for (const initialFailure of [true, false]) {
  test('验证码' + (initialFailure ? '初始' : '刷新') + '读取失败可重试且不沿用旧验证码', async ({ page }) => {
    const errors = errorsOf(page), requests = await mockApi(page)
    let fail = initialFailure, reads = 0
    await page.route('**/dev-api/captchaImage', route => {
      reads++
      return route.fulfill({ json: fail ? { code: 500, msg: '模拟验证码读取失败' }
        : { code: 200, captchaEnabled: true, googleAuthEnabled: false, uuid: 'mock-captcha-' + reads, img: image } })
    })
    await page.goto('/login')
    if (!initialFailure) {
      await expect(page.locator('.login-code-img')).toHaveAttribute('src', 'data:image/gif;base64,' + image)
      await page.getByPlaceholder('验证码', { exact: true }).fill('old-code')
      fail = true
      await page.locator('.login-code-img').click()
    }
    await expect(page.getByText('模拟验证码读取失败', { exact: true })).toBeVisible()
    const retry = page.getByRole('button', { name: '重新获取验证码', exact: true })
    await expect(retry).toBeVisible()
    await expect(page.getByPlaceholder('验证码', { exact: true })).toHaveValue('')
    expect(errors).toEqual([])
    fail = false
    await retry.click()
    await expect(page.locator('.login-code-img')).toHaveAttribute('src', 'data:image/gif;base64,' + image)
    await expect(retry).toBeHidden()
    await page.getByPlaceholder('账号', { exact: true }).fill('mock-login')
    await page.getByPlaceholder('密码', { exact: true }).fill('mock-password')
    await page.getByPlaceholder('验证码', { exact: true }).fill('new-code')
    await page.getByRole('button', { name: /登\s*录/ }).click()
    await expect(page).toHaveURL(url => url.pathname === '/index')
    const login = requests.find(request => request.path === '/login')
    expect(JSON.parse(login.body)).toMatchObject({ code: 'new-code', uuid: 'mock-captcha-' + reads })
    expect(errors).toEqual([])
  })
}

test('分配角色初始化失败释放遮罩并禁止空数据提交，刷新后回显原选择', async ({ page }) => {
  const errors = errorsOf(page), requests = await authenticated(page)
  let fail = true
  await page.route('**/dev-api/system/user/authRole/2', route => route.fulfill({ json: fail
    ? { code: 500, msg: '模拟用户角色读取失败' } : { code: 200, user: account, roles } }))
  await page.goto('/system/user-auth/role/2')
  await expect(page.getByText('模拟用户角色读取失败', { exact: true })).toBeVisible()
  await expect(page.locator('.app-main .el-loading-mask:visible')).toHaveCount(0)
  await expect(page.getByRole('button', { name: '提交', exact: true })).toBeDisabled()
  expect(errors).toEqual([])
  fail = false
  await page.reload()
  await expect(page.getByRole('row').filter({ hasText: '模拟角色' }).getByRole('checkbox')).toBeChecked()
  await expect(page.getByRole('button', { name: '提交', exact: true })).toBeEnabled()
  expect(requests.filter(request => request.method !== 'GET')).toEqual([])
  expect(errors).toEqual([])
})

test('分配角色保存失败保留选择与用户参数，重试沿用原接口协议', async ({ page }) => {
  const errors = errorsOf(page)
  await authenticated(page)
  const writes = []
  await page.route('**/dev-api/system/user/authRole/2', route => route.fulfill({ json: { code: 200, user: account, roles } }))
  await page.route('**/dev-api/system/user/authRole?**', route => {
    const request = route.request(), url = new URL(request.url())
    writes.push({ method: request.method(), userId: url.searchParams.get('userId'), roleIds: url.searchParams.get('roleIds') })
    return route.fulfill({ json: writes.length === 1 ? { code: 500, msg: '模拟角色保存失败' } : { code: 200 } })
  })
  await page.goto('/system/user-auth/role/2')
  const selected = page.getByRole('row').filter({ hasText: '模拟角色' }).getByRole('checkbox')
  await expect(selected).toBeChecked()
  await page.getByRole('button', { name: '提交', exact: true }).click()
  await expect(page.getByText('模拟角色保存失败', { exact: true })).toBeVisible()
  await expect(selected).toBeChecked()
  expect(errors).toEqual([])
  await page.waitForTimeout(1100)
  await page.getByRole('button', { name: '提交', exact: true }).click()
  await expect(page).toHaveURL(url => url.pathname === '/system/user')
  expect(writes).toEqual(Array(2).fill({ method: 'PUT', userId: '2', roleIds: '2' }))
  expect(errors).toEqual([])
})

async function whitePage(page) {
  const requests = await authenticated(page)
  await page.route('**/dev-api/system/ipWhite/list?**', route => route.fulfill({ json: { code: 200, rows: [white], total: 1 } }))
  await page.route('**/dev-api/system/ipWhite/2', route => route.fulfill({ json: { code: 200, data: white } }))
  await page.goto('/system/ipWhite')
  await expect(page.getByRole('row').filter({ hasText: white.ipAddr })).toBeVisible()
  return requests
}

test('白名单详情失败不打开空表单，重试后回显原字段', async ({ page }) => {
  const errors = errorsOf(page), requests = await whitePage(page)
  let fail = true
  await page.route('**/dev-api/system/ipWhite/2', route => fail
    ? route.fulfill({ json: { code: 500, msg: '模拟白名单读取失败' } }) : route.fallback())
  const edit = page.getByRole('row').filter({ hasText: white.ipAddr }).getByRole('button', { name: '修改', exact: true })
  await edit.click()
  await expect(page.getByText('模拟白名单读取失败', { exact: true })).toBeVisible()
  await expect(page.getByRole('dialog')).toBeHidden()
  expect(errors).toEqual([])
  fail = false
  await edit.click()
  await expect(page.getByRole('dialog').getByPlaceholder('请输入IP地址')).toHaveValue(white.ipAddr)
  await page.getByRole('dialog').getByRole('button', { name: '取 消', exact: true }).click()
  expect(requests.filter(request => request.method !== 'GET')).toEqual([])
  expect(errors).toEqual([])
})

test('白名单地区解析失败结束加载，保留IP并可失焦重试', async ({ page }) => {
  const errors = errorsOf(page), requests = await whitePage(page)
  let fail = true
  const queried = []
  await page.route('**/dev-api/system/ipWhite/region?**', route => {
    queried.push(new URL(route.request().url()).searchParams.get('ipAddr'))
    return route.fulfill({ json: fail ? { code: 500, msg: '模拟地区查询失败' } : { code: 200, data: '重试地区' } })
  })
  await page.getByRole('button', { name: '新增', exact: true }).click()
  const dialog = page.getByRole('dialog'), ip = dialog.getByPlaceholder('请输入IP地址')
  await ip.fill('192.0.2.10')
  await dialog.getByPlaceholder('请输入内容').click()
  await expect(page.getByText('模拟地区查询失败', { exact: true })).toBeVisible()
  await expect(dialog.getByPlaceholder('不填写时由后台自动解析')).toBeVisible()
  await expect(ip).toHaveValue('192.0.2.10')
  expect(errors).toEqual([])
  fail = false
  await ip.focus()
  await dialog.getByPlaceholder('请输入内容').click()
  await expect(dialog.getByPlaceholder('不填写时由后台自动解析')).toHaveValue('重试地区')
  expect(queried).toEqual(['192.0.2.10', '192.0.2.10'])
  await dialog.getByRole('button', { name: '取 消', exact: true }).click()
  expect(requests.filter(request => request.method !== 'GET')).toEqual([])
  expect(errors).toEqual([])
})

for (const action of ['新增', '修改']) {
  test('白名单' + action + '保存拒绝保留草稿且能按原字段重试', async ({ page }) => {
    const errors = errorsOf(page)
    await whitePage(page)
    const writes = []
    await page.route('**/dev-api/system/ipWhite', route => {
      writes.push({ method: route.request().method(), data: route.request().postDataJSON() })
      return route.fulfill({ json: writes.length === 1 ? { code: 500, msg: '模拟白名单保存失败' } : { code: 200 } })
    })
    if (action === '新增') await page.getByRole('button', { name: '新增', exact: true }).click()
    else await page.getByRole('row').filter({ hasText: white.ipAddr }).getByRole('button', { name: '修改', exact: true }).click()
    const dialog = page.getByRole('dialog')
    if (action === '新增') {
      await dialog.getByPlaceholder('不填写时由后台自动解析').fill('手工地区')
      await dialog.getByPlaceholder('请输入IP地址').fill('192.0.2.20')
    }
    await dialog.getByPlaceholder('请输入内容').fill('失败后保留的备注')
    await dialog.getByRole('button', { name: '确 定', exact: true }).click()
    await expect(page.getByText('模拟白名单保存失败', { exact: true })).toBeVisible()
    await expect(dialog.getByPlaceholder('请输入内容')).toHaveValue('失败后保留的备注')
    expect(errors).toEqual([])
    await page.waitForTimeout(1100)
    await dialog.getByRole('button', { name: '确 定', exact: true }).click()
    await expect(dialog).toBeHidden()
    expect(writes).toHaveLength(2)
    expect(writes[0]).toEqual(writes[1])
    expect(writes[0]).toMatchObject({ method: action === '新增' ? 'POST' : 'PUT', data: {
      ipAddr: action === '新增' ? '192.0.2.20' : white.ipAddr,
      ipRegion: action === '新增' ? '手工地区' : white.ipRegion,
      status: action === '新增' ? '1' : '0', remark: '失败后保留的备注'
    } })
    expect(errors).toEqual([])
  })
}

for (const change of ['切换IP', '手填地区']) {
  test('白名单解析响应晚到时不覆盖' + change, async ({ page }) => {
    const errors = errorsOf(page), requests = await whitePage(page)
    let complete, started
    const pending = new Promise(resolve => { started = resolve })
    await page.route('**/dev-api/system/ipWhite/region?**', async route => {
      const ip = new URL(route.request().url()).searchParams.get('ipAddr')
      if (ip === '192.0.2.30') {
        await new Promise(resolve => { complete = resolve; started() })
        return route.fulfill({ json: { code: 200, data: '旧IP地区' } })
      }
      return route.fulfill({ json: { code: 200, data: '新IP地区' } })
    })
    await page.getByRole('button', { name: '新增', exact: true }).click()
    const dialog = page.getByRole('dialog'), ip = dialog.getByPlaceholder('请输入IP地址')
    await ip.fill('192.0.2.30')
    await dialog.getByPlaceholder('请输入内容').click()
    await pending
    if (change === '切换IP') {
      await ip.fill('192.0.2.31')
      await dialog.getByPlaceholder('请输入内容').click()
      await expect(dialog.getByPlaceholder('不填写时由后台自动解析')).toHaveValue('新IP地区')
    } else await dialog.getByPlaceholder('正在解析IP地区').fill('手工地区')
    const response = page.waitForResponse(res => res.url().includes('ipAddr=192.0.2.30'))
    complete()
    await response
    await expect(dialog.getByPlaceholder('不填写时由后台自动解析')).toHaveValue(change === '切换IP' ? '新IP地区' : '手工地区')
    await dialog.getByRole('button', { name: '取 消', exact: true }).click()
    expect(requests.filter(request => request.method !== 'GET')).toEqual([])
    expect(errors).toEqual([])
  })
}
