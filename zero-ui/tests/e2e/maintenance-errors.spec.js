import { test, expect } from '@playwright/test'
import { authenticated, mockAdminWebSocket } from './mock-api'

const user = { userId: 2, userName: 'mock-user', nickName: '模拟用户', phonenumber: '13800000000', email: 'mock@example.test', sex: '0', status: '0', deptId: 100, dept: { deptName: '模拟部门' } }
const config = { configId: 2, configName: '模拟参数', configKey: 'mock.key', configValue: 'mock-value', configType: 'N' }
const departments = [{ id: 100, label: '模拟部门', children: [{ id: 101, label: '启用子部门' }, { id: 102, label: '停用子部门', disabled: true }] }]

function watchErrors(page) {
  const errors = []
  page.on('pageerror', error => errors.push(error.message))
  return errors
}

test('参数详情失败不打开空表单，重试正常回显', async ({ page }) => {
  const errors = watchErrors(page), requests = await authenticated(page)
  let fail = true
  await page.route('**/dev-api/system/config/list?**', route => route.fulfill({ json: { code: 200, rows: [config], total: 1 } }))
  await page.route('**/dev-api/system/config/2', route => route.fulfill({ json: fail ? { code: 500, msg: '模拟参数读取失败' } : { code: 200, data: config } }))
  await page.goto('/system/config')
  const edit = page.getByRole('row').filter({ hasText: config.configName }).getByRole('button', { name: '修改', exact: true })
  await edit.click()
  await expect(page.getByText('模拟参数读取失败', { exact: true })).toBeVisible()
  await expect(page.getByRole('dialog')).toBeHidden()
  expect(errors).toEqual([])
  fail = false
  await edit.click()
  await expect(page.getByRole('dialog').getByPlaceholder('请输入参数键值')).toHaveValue(config.configValue)
  await page.getByRole('dialog').getByRole('button', { name: '取 消', exact: true }).click()
  expect(requests.filter(request => request.method !== 'GET')).toEqual([])
})

for (const [path, api] of [['/system/config', '/system/config/refreshCache'], ['/system/dict', '/system/dict/type/refreshCache']]) {
  test('刷新缓存拒绝不产生未处理异常且可重试 ' + path, async ({ page }) => {
    const errors = watchErrors(page)
    await authenticated(page)
    const writes = []
    await page.route('**/dev-api' + api, route => {
      writes.push(route.request().method())
      return route.fulfill({ json: writes.length === 1 ? { code: 500, msg: '模拟缓存刷新失败' } : { code: 200 } })
    })
    await page.goto(path)
    await page.getByRole('button', { name: '刷新缓存', exact: true }).click()
    await expect(page.getByText('模拟缓存刷新失败', { exact: true })).toBeVisible()
    await expect(page.locator('.el-message--success')).toHaveCount(0)
    expect(errors).toEqual([])
    await page.getByRole('button', { name: '刷新缓存', exact: true }).click()
    await expect(page.getByText('刷新成功', { exact: true })).toBeVisible()
    expect(writes).toEqual(['DELETE', 'DELETE'])
    expect(errors).toEqual([])
  })
}

async function profile(page) {
  await mockAdminWebSocket(page)
  const requests = await authenticated(page)
  await page.route('**/dev-api/system/user/profile', route => route.fulfill({ json: { code: 200, data: user, roleGroup: '模拟角色', postGroup: '模拟岗位' } }))
  return requests
}

test('个人资料初始读取失败没有未处理拒绝，刷新后恢复资料', async ({ page }) => {
  const errors = watchErrors(page)
  await profile(page)
  let fail = true
  await page.route('**/dev-api/system/user/profile', route => fail ? route.fulfill({ json: { code: 500, msg: '模拟个人资料读取失败' } }) : route.fallback())
  await page.goto('/user/profile')
  await expect(page.getByText('模拟个人资料读取失败', { exact: true })).toBeVisible()
  await expect(page.getByRole('tab', { name: '基本资料', exact: true })).toBeVisible()
  expect(errors).toEqual([])
  fail = false
  await page.reload()
  await expect(page.locator('.el-tab-pane:visible .el-form-item').filter({ hasText: '用户昵称' }).locator('input')).toHaveValue(user.nickName)
  expect(errors).toEqual([])
})

for (const password of [false, true]) {
  test('个人中心' + (password ? '修改密码' : '基本资料') + '保存失败保留输入且重试字段不变', async ({ page }) => {
    const errors = watchErrors(page), writes = []
    const requests = await profile(page)
    await page.route('**/dev-api/system/user/profile' + (password ? '/updatePwd' : ''), route => {
      if (route.request().method() === 'GET') return route.fallback()
      writes.push({ method: route.request().method(), data: route.request().postDataJSON() })
      return route.fulfill({ json: writes.length === 1 ? { code: 500, msg: '模拟个人中心保存失败' } : { code: 200 } })
    })
    await page.goto('/user/profile')
    const pane = page.getByRole('region', { name: password ? '修改密码' : '修改个人信息', exact: true })
    if (password) {
      await page.getByRole('button', { name: '修改密码', exact: true }).click()
      await pane.getByPlaceholder('请输入旧密码').fill('mock-old-password')
      await pane.getByPlaceholder('请输入新密码').fill('mock-new-password')
      await pane.getByPlaceholder('请确认新密码').fill('mock-new-password')
    } else {
      await page.getByRole('button', { name: '修改个人信息', exact: true }).click()
      await expect(pane.locator('.el-form-item').filter({ hasText: '用户昵称' }).locator('input')).toHaveValue(user.nickName)
      await pane.locator('.el-form-item').filter({ hasText: '用户昵称' }).locator('input').fill('失败后保留的昵称')
    }
    await pane.getByRole('button', { name: '保存', exact: true }).click()
    await expect(page.getByText('模拟个人中心保存失败', { exact: true })).toBeVisible()
    expect(errors).toEqual([])
    if (password) {
      await expect(pane.getByPlaceholder('请输入新密码')).toHaveValue('mock-new-password')
      expect(requests.some(request => request.path === '/logout')).toBe(false)
    } else await expect(pane.locator('.el-form-item').filter({ hasText: '用户昵称' }).locator('input')).toHaveValue('失败后保留的昵称')
    await page.waitForTimeout(1100)
    await pane.getByRole('button', { name: '保存', exact: true }).click()
    await expect(page.getByText(password ? '密码修改成功，请重新登录' : '修改成功', { exact: true })).toBeVisible()
    if (password) {
      await expect(page).toHaveURL(url => url.pathname === '/login' && url.searchParams.get('redirect') === '/index')
      expect(requests.filter(request => request.path === '/logout')).toHaveLength(1)
      expect((await page.context().cookies()).some(cookie => cookie.name === 'Admin-Token')).toBe(false)
      await page.getByPlaceholder('账号', { exact: true }).fill('mock-user')
      await page.getByPlaceholder('密码', { exact: true }).fill('mock-new-password')
      await page.getByRole('button', { name: /登\s*录/ }).click()
      await expect(page).toHaveURL(url => url.pathname === '/index')
    }
    expect(writes).toHaveLength(2)
    expect(writes[0]).toEqual(writes[1])
    expect(writes[0]).toEqual({ method: 'PUT', data: password
      ? { oldPassword: 'mock-old-password', newPassword: 'mock-new-password' }
      : { nickName: '失败后保留的昵称', phonenumber: user.phonenumber, email: user.email, sex: '0' } })
    expect(errors).toEqual([])
  })
}

async function userFixtures(page) {
  const requests = await authenticated(page)
  await page.route('**/dev-api/system/user/list?**', route => route.fulfill({ json: { code: 200, rows: [user], total: 1 } }))
  await page.route('**/dev-api/system/user/deptTree', route => route.fulfill({ json: { code: 200, data: departments } }))
  await page.route('**/dev-api/system/config/configKey/sys.user.initPassword', route => route.fulfill({ json: { code: 200, msg: 'mock-init-password' } }))
  await page.route(/\/dev-api\/system\/user\/(?:2)?$/, route => route.fulfill({ json: { code: 200, data: user,
    posts: [{ postId: 2, postName: '模拟岗位', status: '0' }], roles: [{ roleId: 2, roleName: '模拟角色', status: '0' }], postIds: [2], roleIds: [2] } }))
  return requests
}

async function openUser(page, action) {
  if (action === '新增') await page.getByRole('button', { name: '新增', exact: true }).click()
  else await page.getByRole('row').filter({ hasText: user.userName }).getByRole('button', { name: '修改', exact: true }).click()
}

for (const endpoint of ['/system/user/deptTree', '/system/config/configKey/sys.user.initPassword']) {
  test('用户初始化辅助读取失败可刷新恢复 ' + endpoint, async ({ page }) => {
    const errors = watchErrors(page), requests = await userFixtures(page)
    let fail = true
    await page.route('**/dev-api' + endpoint, route => fail ? route.fulfill({ json: { code: 500, msg: '模拟用户辅助读取失败' } }) : route.fallback())
    await page.goto('/system/user')
    await expect(page.getByText('模拟用户辅助读取失败', { exact: true })).toBeVisible()
    await expect(page.getByRole('row').filter({ hasText: user.userName })).toBeVisible()
    expect(errors).toEqual([])
    fail = false
    await page.reload()
    await expect(page.locator('.user-dept-tree').getByText('模拟部门', { exact: true })).toBeVisible()
    await openUser(page, '新增')
    await expect(page.getByRole('dialog').getByPlaceholder('请输入用户密码')).toHaveValue('mock-init-password')
    await page.getByRole('dialog').getByRole('button', { name: '取 消', exact: true }).click()
    expect(requests.filter(request => request.method !== 'GET')).toEqual([])
    expect(errors).toEqual([])
  })
}

test('用户新增保留旧参数接口msg返回的默认值兼容，部门禁用项不进入表单', async ({ page }) => {
  const errors = watchErrors(page), requests = await userFixtures(page)
  await page.goto('/system/user')
  await expect(page.locator('.user-dept-tree').getByText('停用子部门', { exact: true })).toBeVisible()
  await openUser(page, '新增')
  const dialog = page.getByRole('dialog')
  await expect(dialog.getByPlaceholder('请输入用户密码')).toHaveValue('mock-init-password')
  await dialog.locator('.el-form-item').filter({ hasText: '归属部门' }).locator('.el-select__wrapper').click()
  const popup = page.locator('.el-select__popper:visible')
  await popup.locator('.el-tree-node__expand-icon').first().click()
  await expect(popup.getByText('启用子部门', { exact: true })).toBeVisible()
  await expect(popup.getByText('停用子部门', { exact: true })).toHaveCount(0)
  await page.keyboard.press('Escape')
  await dialog.getByRole('button', { name: '取 消', exact: true }).click()
  expect(requests.filter(request => request.method !== 'GET')).toEqual([])
  expect(errors).toEqual([])
})

for (const action of ['新增', '修改']) {
  for (const departmentFailure of [false, true]) {
    test('用户' + action + (departmentFailure ? '部门树' : '详情选项') + '读取失败不打开残缺表单，重试正确回显', async ({ page }) => {
      const errors = watchErrors(page), requests = await userFixtures(page)
      let fail = true, reads = 0
      const endpoint = departmentFailure ? '/system/user/deptTree' : '/system/user/' + (action === '修改' ? '2' : '')
      await page.route('**/dev-api' + endpoint, route => {
        reads++
        return fail ? route.fulfill({ json: { code: 500, msg: '模拟用户表单读取失败' + reads } }) : route.fallback()
      })
      await page.goto('/system/user')
      if (departmentFailure) await expect(page.getByText('模拟用户表单读取失败1', { exact: true })).toBeVisible()
      else await expect(page.locator('.user-dept-tree').getByText('模拟部门', { exact: true })).toBeVisible()
      const expectedRead = reads + 1
      await openUser(page, action)
      await expect.poll(() => reads).toBe(expectedRead)
      await expect(page.getByText('模拟用户表单读取失败' + expectedRead, { exact: true })).toBeVisible()
      await expect(page.getByRole('dialog')).toBeHidden()
      expect(errors).toEqual([])
      fail = false
      await openUser(page, action)
      const dialog = page.getByRole('dialog')
      await expect(dialog).toBeVisible()
      if (action === '修改') {
        await expect(dialog.getByPlaceholder('请输入用户昵称')).toHaveValue(user.nickName)
        await expect(dialog).toContainText('模拟岗位')
        await expect(dialog).toContainText('模拟角色')
      }
      await dialog.getByRole('button', { name: '取 消', exact: true }).click()
      expect(requests.filter(request => request.method !== 'GET')).toEqual([])
      expect(errors).toEqual([])
    })
  }
  test('用户' + action + '保存失败保留草稿，重试保持原字段和类型', async ({ page }) => {
    const errors = watchErrors(page), writes = []
    await userFixtures(page)
    await page.route('**/dev-api/system/user', route => {
      writes.push({ method: route.request().method(), data: route.request().postDataJSON() })
      return route.fulfill({ json: writes.length === 1 ? { code: 500, msg: '模拟用户保存失败' } : { code: 200 } })
    })
    await page.goto('/system/user')
    await openUser(page, action)
    const dialog = page.getByRole('dialog')
    await dialog.getByPlaceholder('请输入用户昵称').fill('失败后保留的用户')
    if (action === '新增') await dialog.getByPlaceholder('请输入用户名称').fill('mock-new-user')
    await dialog.getByRole('button', { name: '确 定', exact: true }).click()
    await expect(page.getByText('模拟用户保存失败', { exact: true })).toBeVisible()
    await expect(dialog.getByPlaceholder('请输入用户昵称')).toHaveValue('失败后保留的用户')
    expect(errors).toEqual([])
    await page.waitForTimeout(1100)
    await dialog.getByRole('button', { name: '确 定', exact: true }).click()
    await expect(dialog).toBeHidden()
    expect(writes).toHaveLength(2)
    expect(writes[0]).toEqual(writes[1])
    expect(writes[0]).toMatchObject({ method: action === '新增' ? 'POST' : 'PUT', data: { nickName: '失败后保留的用户', status: '0',
      postIds: action === '新增' ? [] : [2], roleIds: action === '新增' ? [] : [2] } })
    expect(errors).toEqual([])
  })
}

test('用户重置密码请求拒绝被消费，重新打开仍可操作', async ({ page }) => {
  const errors = watchErrors(page), writes = []
  await userFixtures(page)
  await page.route('**/dev-api/system/user/resetPwd', route => {
    writes.push({ method: route.request().method(), data: route.request().postDataJSON() })
    return route.fulfill({ json: writes.length === 1 ? { code: 500, msg: '模拟重置密码失败' } : { code: 200 } })
  })
  await page.goto('/system/user')
  for (let attempt = 0; attempt < 2; attempt++) {
    await page.getByRole('button', { name: '更多', exact: true }).click()
    await page.getByRole('menuitem', { name: '重置密码', exact: true }).click()
    await expect(page.locator('.el-overlay.is-message-box')).not.toHaveClass(/fade-in-linear-enter-(active|from|to)/)
    const password = page.locator('.el-message-box__input input')
    await expect(password).toBeFocused()
    await password.fill('mock-reset-password')
    await expect(password).toHaveValue('mock-reset-password')
    await page.locator('.el-message-box').getByRole('button', { name: '确定', exact: true }).click()
    if (!attempt) {
      await expect(page.getByText('模拟重置密码失败', { exact: true })).toBeVisible()
      expect(errors).toEqual([])
      await page.waitForTimeout(1100)
    } else {
      await expect(page.locator('.el-message--success')).toBeVisible()
      await expect(page.locator('.el-message--success')).not.toContainText('mock-reset-password')
    }
  }
  expect(writes).toEqual(Array(2).fill({ method: 'PUT', data: { userId: 2, password: 'mock-reset-password' } }))
  expect(errors).toEqual([])
})

test('角色选择用户保存失败保留勾选，重试沿用角色与用户ID', async ({ page }) => {
  const errors = watchErrors(page), writes = []
  await authenticated(page)
  await page.route('**/dev-api/system/role/authUser/unallocatedList?**', route => route.fulfill({ json: { code: 200, rows: [user], total: 1 } }))
  await page.route('**/dev-api/system/role/authUser/selectAll?**', route => {
    const params = new URL(route.request().url()).searchParams
    writes.push({ method: route.request().method(), roleId: params.get('roleId'), userIds: params.get('userIds') })
    return route.fulfill({ json: writes.length === 1 ? { code: 500, msg: '模拟选择用户失败' } : { code: 200, msg: '模拟选择成功' } })
  })
  await page.goto('/system/role-auth/user/2')
  await page.getByRole('button', { name: '添加用户', exact: true }).click()
  const dialog = page.getByRole('dialog', { name: '选择用户', exact: true })
  const row = dialog.getByRole('row').filter({ hasText: user.userName })
  const selected = row.getByRole('checkbox')
  await row.getByRole('cell', { name: user.userName, exact: true }).click()
  await expect(selected).toBeChecked()
  await dialog.getByRole('button', { name: '确 定', exact: true }).click()
  await expect(page.getByText('模拟选择用户失败', { exact: true })).toBeVisible()
  await expect(selected).toBeChecked()
  expect(errors).toEqual([])
  await page.waitForTimeout(1100)
  await dialog.getByRole('button', { name: '确 定', exact: true }).click()
  await expect(dialog).toBeHidden()
  expect(writes).toEqual(Array(2).fill({ method: 'PUT', roleId: '2', userIds: '2' }))
  expect(errors).toEqual([])
})
