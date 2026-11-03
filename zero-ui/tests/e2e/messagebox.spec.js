import { test, expect } from '@playwright/test'
import fs from 'node:fs/promises'
import path from 'node:path'
import { authenticated } from './mock-api'

const themes = [
  ['glassmorphism', '毛玻璃拟态风'], ['dark-premium', '高端暗黑风'],
  ['minimalist-future', '极简未来风'], ['default', '原始风格'],
  ['luxury-business', '轻奢商务风'], ['vibrant-tech', '活力科技风'],
  ['warm-nature', '温馨自然风'], ['material-design', '质感设计风']
]

async function selectTheme(page, style, label) {
  await page.locator('.navbar .setting').click()
  await page.locator('.drawer-select').first().click()
  await page.getByRole('option', { name: label, exact: true }).click()
  if (style !== 'default') await expect(page.locator('html > body')).toHaveClass(new RegExp(`theme-${style}`))
  else await expect(page.locator('html > body')).not.toHaveClass(/theme-/)
  await page.getByRole('button', { name: '关闭设置', exact: true }).click()
  await expect(page.locator('.el-drawer')).not.toBeVisible()
  await expect(page.locator('.app-main .fade-transform-leave-active, .app-main .fade-transform-enter-active')).toHaveCount(0)
}

async function expectFrame(box, label) {
  await expect(box).toBeVisible()
  await expect(box.page().locator('.el-overlay.is-message-box')).not.toHaveClass(/fade-in-linear-enter-(active|from|to)/)
  const frame = await box.evaluate(element => {
    const style = getComputedStyle(element)
    const rect = element.getBoundingClientRect()
    const gaps = ['header', 'content', 'btns'].map(part => {
      const section = element.querySelector(`.el-message-box__${part}`).getBoundingClientRect()
      return Math.max(Math.abs(rect.left - section.left), Math.abs(rect.width - section.width))
    })
    return { padding: style.padding, border: style.borderTopWidth, shadow: style.boxShadow, gaps }
  })
  expect(frame, label).toEqual({ padding: '0px', border: '0px', shadow: 'none', gaps: [0, 0, 0] })
  await expect(box.locator('.el-message-box__message')).toBeVisible()
  await expect(box.getByRole('button', { name: '取消', exact: true })).toBeVisible()
  await expect(box.getByRole('button', { name: '确定', exact: true })).toBeVisible()
}

test('八主题确认框没有外层留白边框阴影，取消关闭和Esc不发送清空请求', async ({ page }, testInfo) => {
  test.setTimeout(120000)
  const requests = await authenticated(page)
  const errors = []; page.on('pageerror', error => errors.push(error.message))
  await page.goto('/monitor/operlog')
  const box = page.locator('.el-message-box:visible')
  const evidenceDir = path.join('output/playwright/messagebox', testInfo.project.name)
  await fs.mkdir(evidenceDir, { recursive: true })
  for (const [style, label] of themes) {
    await selectTheme(page, style, label)
    await page.getByRole('button', { name: '清空', exact: true }).click()
    await expectFrame(box, label)
    await expect(box).toContainText('是否确认清空所有操作日志数据项？')
    await page.screenshot({ path: path.join(evidenceDir, `${style}.png`) })
    await box.getByRole('button', { name: '取消', exact: true }).click()
    await expect(box).not.toBeVisible()
    await page.getByRole('button', { name: '清空', exact: true }).click()
    await box.locator('.el-message-box__headerbtn').click()
    await expect(box).not.toBeVisible()
    await page.getByRole('button', { name: '清空', exact: true }).click()
    await page.keyboard.press('Escape')
    await expect(box).not.toBeVisible()
  }
  expect(requests.filter(request => request.method === 'DELETE')).toEqual([])
  expect(errors).toEqual([])
})

test('八主题谷歌输入提示框保持输入校验及取消，不发送目标请求', async ({ page }) => {
  test.setTimeout(120000)
  const requests = await authenticated(page, { googleApis: [{ apiMethod: 'POST', apiUrl: '/system/config' }] })
  await page.goto('/system/config')
  for (const [style, label] of themes) {
    await selectTheme(page, style, label)
    await page.getByRole('button', { name: '新增', exact: true }).click()
    const dialog = page.locator('.el-dialog:visible')
    await dialog.getByPlaceholder('请输入参数名称').fill('测试参数')
    await dialog.getByPlaceholder('请输入参数键名').fill('mock.messagebox.key')
    await dialog.getByPlaceholder('请输入参数键值').fill('mock-value')
    await dialog.getByRole('button', { name: /确\s*定/ }).click()
    const box = page.locator('.el-message-box:visible')
    await expectFrame(box, label)
    await box.locator('input').fill('12')
    await box.getByRole('button', { name: '确定', exact: true }).click()
    await expect(box.locator('.el-message-box__errormsg')).toHaveText('谷歌验证码必须为6位数字')
    await box.getByRole('button', { name: '取消', exact: true }).click()
    await expect(box).not.toBeVisible()
    await dialog.getByRole('button', { name: /取\s*消/ }).click()
  }
  expect(requests.filter(request => request.path === '/system/config' && request.method === 'POST')).toEqual([])
})
