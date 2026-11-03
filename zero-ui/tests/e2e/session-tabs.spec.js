import { test, expect } from '@playwright/test'
import { mockApi } from './mock-api'

async function accounts(page) {
  await mockApi(page)
  let account = 'a'
  const calls = []
  await page.route('**/dev-api/login', route => {
    account = route.request().postDataJSON().username
    calls.push('login:' + account)
    return route.fulfill({ json: { code: 200, token: 'mock-' + account } })
  })
  await page.route('**/dev-api/getInfo', route => {
    calls.push('info:' + account)
    return route.fulfill({ json: {
      code: 200, user: { userId: account === 'a' ? 1 : 2, userName: account, nickName: '账号' + account, avatar: '', dept: {} },
      roles: account === 'a' ? ['admin'] : ['common'], permissions: account === 'a' ? ['*:*:*'] : ['system:config:list']
    } })
  })
  await page.route('**/dev-api/getRouters', route => {
    calls.push('menus:' + account)
    return route.fulfill({ json: { code: 200, data: [{ path: '/system', component: 'Layout', meta: { title: '系统管理', icon: 'system' }, children:
      (account === 'a' ? ['config', 'user', 'post'] : ['config']).map(name => ({ path: name, name: name[0].toUpperCase() + name.slice(1), component: 'system/' + name + '/index', meta: { title: name, icon: 'list' } })) }] } })
  })
  const login = async name => {
    await page.getByPlaceholder('账号', { exact: true }).fill(name)
    await page.getByPlaceholder('密码', { exact: true }).fill('mock-password')
    const redirect = new URL(page.url()).searchParams.get('redirect') || '/index'
    const destination = new URL(redirect === '/' ? '/index' : redirect, page.url())
    const entered = page.waitForURL(url => url.pathname === destination.pathname)
    await page.getByRole('button', { name: /登\s*录/ }).click()
    await entered
    await expect(page.locator('.navbar .user-nickname')).toHaveText('账号' + name)
  }
  return { calls, login }
}

for (const exit of ['logout', 'cookie-expiry']) {
  test('同页账号切换清理动态菜单、权限及Tab ' + exit, async ({ page }) => {
    test.setTimeout(120000)
    const state = await accounts(page)
    await page.goto('/login')
    await state.login('a')
    await page.locator('.sidebar-container').getByText('系统管理', { exact: true }).click()
    await page.locator('.sidebar-container a[href="/system/user"]').click()
    await expect(page.locator('.tags-view-item[href="/system/user"]')).toBeVisible()
    if (exit === 'logout') {
      await page.locator('.navbar .avatar-wrapper').click()
      await page.getByText('退出登录', { exact: true }).click()
      await page.locator('.el-message-box:visible').getByRole('button', { name: '确定', exact: true }).click()
    } else {
      // 仅清理测试Cookie，通过SPA链接触发守卫，保留原页面的内存状态。
      await page.context().clearCookies({ name: 'Admin-Token' })
      await page.locator('.sidebar-container a[href="/system/config"]').click()
    }
    await page.waitForURL(url => url.pathname === '/login', { waitUntil: 'domcontentloaded' })
    await expect(page).toHaveURL(url => url.pathname === '/login')
    await state.login('b')
    await expect(page.locator('.sidebar-container a[href="/system/user"]')).toHaveCount(0)
    await expect(page.locator('.tags-view-item[href="/system/user"]')).toHaveCount(0)
    await expect(page.getByRole('button', { name: '新增', exact: true })).toHaveCount(0)
    expect(state.calls).toContain('info:b')
    expect(state.calls).toContain('menus:b')
    await page.goto('/system/user')
    await expect(page).toHaveURL(/\/404/)
  })
}

test('Tab右键刷新、左右及其它关闭、全部关闭保留固定首页', async ({ page }) => {
  test.setTimeout(120000)
  const state = await accounts(page)
  await page.goto('/login')
  await state.login('a')
  await page.locator('.sidebar-container').getByText('系统管理', { exact: true }).click()
  const open = async path => {
    await page.locator(`.sidebar-container a[href="${path}"]`).click()
    await expect(page).toHaveURL(new RegExp(path + '$'))
  }
  const action = async (path, name) => {
    await page.locator(`.tags-view-item[href="${path}"]`).click({ button: 'right' })
    await page.getByText(name, { exact: true }).click()
  }
  const tags = () => page.locator('.tags-view-item').evaluateAll(elements => elements.map(element => element.getAttribute('href')))
  await open('/system/config')
  await page.getByPlaceholder('请输入参数名称').fill('刷新前字段')
  await action('/system/config', '刷新页面')
  await expect(page.getByPlaceholder('请输入参数名称')).toHaveValue('')
  await open('/system/user')
  await open('/system/post')
  await action('/system/user', '关闭右侧')
  await expect.poll(tags).toEqual(['/index', '/system/config', '/system/user'])
  await action('/system/user', '关闭左侧')
  await expect.poll(tags).toEqual(['/index', '/system/user'])
  await open('/system/config')
  await open('/system/post')
  await action('/system/config', '关闭其他')
  await expect.poll(tags).toEqual(['/index', '/system/config'])
  await action('/system/config', '全部关闭')
  await expect.poll(tags).toEqual(['/index'])
  await expect(page.locator('.zero-home')).toBeVisible()
})
