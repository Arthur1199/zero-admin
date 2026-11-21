import { test, expect } from '@playwright/test'
import { authenticated, mockAdminWebSocket } from './mock-api'

test('个人中心可还原草稿、同步昵称并快捷进入账户设置', async ({ page }, testInfo) => {
  await mockAdminWebSocket(page)
  await authenticated(page)
  const user = {
    userName: 'profile-user', nickName: '原昵称', phonenumber: '13800000000', email: 'profile@example.test',
    sex: '0', dept: { deptName: '测试部门' }, createTime: '2026-11-01 09:00:00', loginDate: '2026-11-21 10:20:30'
  }
  const updates = []
  await page.route('**/dev-api/system/user/profile', route => {
    if (route.request().method() === 'PUT') updates.push(route.request().postDataJSON())
    return route.fulfill({ json: { code: 200, data: user, roleGroup: '管理员', postGroup: '技术支持' } })
  })

  await page.goto('/user/profile')
  const form = page.locator('.profile-form')
  const nickname = form.getByRole('textbox', { name: '用户昵称' })
  const save = form.getByRole('button', { name: '保存', exact: true })
  await expect(page.getByText('最近登录', { exact: true })).toBeVisible()
  await expect(page.getByRole('dialog')).toHaveCount(0)
  const openInfo = page.getByRole('button', { name: '修改个人信息', exact: true })
  const info = page.getByRole('region', { name: '修改个人信息', exact: true })
  const editor = page.locator('.profile-editor')
  await expect(editor).toBeHidden()
  await openInfo.click()
  await expect(info).toBeVisible()
  await expect(nickname).toHaveValue('原昵称')
  await expect(save).toBeDisabled()
  await nickname.fill('未保存草稿')
  await form.getByRole('button', { name: '还原修改' }).click()
  await expect(nickname).toHaveValue('原昵称')
  expect(updates).toHaveLength(0)
  await nickname.fill('更新昵称')
  await save.click()
  await expect(save).toBeDisabled()
  await info.getByRole('button', { name: '关闭', exact: true }).click()
  await expect(info).toBeHidden()
  await expect(page.getByRole('heading', { name: '更新昵称' })).toBeVisible()
  await expect(page.locator('.user-nickname')).toContainText('更新昵称')
  expect(updates).toEqual([{ nickName: '更新昵称', phonenumber: user.phonenumber, email: user.email, sex: '0' }])

  await page.getByRole('button', { name: '更换头像' }).click()
  const avatar = page.getByRole('region', { name: '修改头像', exact: true })
  await expect(avatar).toBeVisible()
  await expect(form).toBeHidden()
  await page.getByRole('button', { name: '修改密码' }).click()
  await expect(avatar).toBeHidden()
  await expect(page.getByRole('dialog')).toHaveCount(0)
  const password = page.getByRole('region', { name: '修改密码', exact: true })
  await expect(form).toBeHidden()
  await password.getByRole('button', { name: '关闭', exact: true }).click()
  await expect(password).toBeHidden()

  const layout = page.locator('.profile-layout')
  const actions = page.locator('.profile-actions')
  const buttonsFitInOneRow = () => actions.evaluate(element => {
    const positions = Array.from(element.querySelectorAll('button'), button => button.offsetTop)
    return positions.length === 3 && positions.every(top => top === positions[0])
      && element.scrollWidth <= element.clientWidth + 1
  })
  const columns = () => layout.evaluate(element => getComputedStyle(element).gridTemplateColumns.split(' ').length)
  await expect.poll(columns).toBe(2)
  await expect.poll(buttonsFitInOneRow).toBe(true)
  await layout.screenshot({ path: testInfo.outputPath('profile-desktop.png'), animations: 'disabled' })
  await page.setViewportSize({ width: 390, height: 844 })
  await expect.poll(columns).toBe(1)
  await expect.poll(buttonsFitInOneRow).toBe(true)
  await expect.poll(() => layout.evaluate(element => element.scrollWidth <= element.clientWidth + 1)).toBe(true)
  await openInfo.click()
  await expect(info).toBeVisible()
  await expect.poll(async () => (await info.boundingBox()).width).toBeLessThan(390)
  await info.getByRole('button', { name: '关闭', exact: true }).click()

  await page.setViewportSize({ width: 1280, height: 900 })
  for (const themeStyle of ['dark-premium', 'minimalist-future', 'default']) {
    await page.evaluate(themeStyle => {
      localStorage.setItem('layout-setting', JSON.stringify({ themeStyle }))
    }, themeStyle)
    await page.reload()
    await expect(page.getByRole('dialog')).toHaveCount(0)
    await expect(editor).toBeHidden()
    await openInfo.click()
    await expect(nickname).toHaveValue('原昵称')
    await layout.screenshot({ path: testInfo.outputPath(`profile-${themeStyle}.png`), animations: 'disabled' })
  }
})
