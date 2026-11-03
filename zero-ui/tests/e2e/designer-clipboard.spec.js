import { test, expect } from '@playwright/test'
import { parse, compileTemplate, compileScript } from '@vue/compiler-sfc'
import { authenticated } from './mock-api'

async function clipboardProbe(page) {
  await page.evaluate(() => {
    const probe = document.createElement('textarea')
    probe.setAttribute('data-testid', 'clipboard-probe')
    probe.setAttribute('aria-label', '剪贴板验收')
    probe.style.cssText = 'position:fixed;bottom:8px;left:8px;width:600px;height:50px;z-index:10000'
    document.body.appendChild(probe)
  })
  const probe = page.getByTestId('clipboard-probe')
  await probe.fill('旧剪贴板对照')
  await probe.press('Control+A')
  await probe.press('Control+C')
  await probe.fill('')
  return probe
}

async function copyCode(page, probe, type = '页面') {
  await page.getByRole('button', { name: '复制代码', exact: true }).click()
  const dialog = page.getByRole('dialog', { name: '选择生成类型', exact: true })
  await dialog.getByText(type, { exact: true }).click()
  await dialog.getByRole('button', { name: '确定', exact: true }).click()
  await expect(dialog).toBeHidden()
  await expect(page.getByText('代码已复制到剪贴板，可粘贴。', { exact: true })).toBeVisible()
  await probe.click()
  await probe.press('Control+V')
  await expect(probe).toHaveValue(/<template>/)
  const code = await probe.inputValue()
  const { descriptor, errors } = parse(code)
  expect(errors).toEqual([])
  expect(compileTemplate({ source: descriptor.template.content, id: type, filename: 'clipboard.vue' }).errors).toEqual([])
  expect(() => compileScript(descriptor, { id: type })).not.toThrow()
  return code
}

for (const type of ['页面', '弹窗']) {
  test('设计器实际复制与粘贴得到完整可编译组件 ' + type, async ({ page }) => {
    const errors = []; page.on('pageerror', error => errors.push(error.message))
    await authenticated(page)
    await page.goto('/tool/build')
    await expect(page.locator('.drawing-board')).toBeVisible()
    const probe = await clipboardProbe(page)
    const code = await copyCode(page, probe, type)
    expect(code).toContain('formData.mobile')
    if (type === '弹窗') expect(code).toContain('update:modelValue')
    expect(errors).toEqual([])
  })
}

test('复制执行失败不提前提示成功，恢复后允许重试', async ({ page }) => {
  await authenticated(page)
  await page.goto('/tool/build')
  await expect(page.locator('.drawing-board')).toBeVisible()
  const probe = await clipboardProbe(page)
  await page.evaluate(() => {
    const original = document.execCommand.bind(document)
    const originalClipboard = navigator.clipboard?.writeText.bind(navigator.clipboard)
    window.restoreCopyCommand = () => {
      document.execCommand = original
      if (originalClipboard) navigator.clipboard.writeText = originalClipboard
    }
    if (originalClipboard) navigator.clipboard.writeText = () => Promise.reject(new Error('模拟剪贴板拒绝'))
    document.execCommand = (command, ...args) => command === 'copy' ? false : original(command, ...args)
  })
  await page.getByRole('button', { name: '复制代码', exact: true }).click()
  await page.getByRole('dialog', { name: '选择生成类型', exact: true }).getByRole('button', { name: '确定', exact: true }).click()
  await expect(page.getByText('代码复制失败', { exact: true })).toBeVisible()
  await expect(page.getByText('代码已复制到剪贴板，可粘贴。', { exact: true })).toHaveCount(0)
  await page.evaluate(() => window.restoreCopyCommand())
  await copyCode(page, probe)
})

test('标准剪贴板被拒绝后使用兼容复制路径，实际粘贴仍完整', async ({ page }) => {
  await authenticated(page)
  await page.goto('/tool/build')
  await expect(page.locator('.drawing-board')).toBeVisible()
  const probe = await clipboardProbe(page)
  await page.evaluate(() => {
    if (navigator.clipboard?.writeText) navigator.clipboard.writeText = () => Promise.reject(new Error('模拟标准剪贴板拒绝'))
  })
  await copyCode(page, probe)
})

test('设计器复选数量限制编辑、字段切换回显及导出保持数值类型', async ({ page }) => {
  await authenticated(page)
  await page.goto('/tool/build')
  await page.locator('.components-item').filter({ hasText: '多选框组' }).click()
  const min = page.getByRole('spinbutton', { name: '至少应选', exact: true })
  const max = page.getByRole('spinbutton', { name: '最多可选', exact: true })
  await min.fill('1'); await min.press('Tab')
  await max.fill('1'); await max.press('Tab')
  await expect(min).toHaveValue('1'); await expect(max).toHaveValue('1')
  const board = page.locator('.drawing-board')
  await board.locator('.el-checkbox').filter({ hasText: '选项一' }).click()
  await expect(board.getByRole('checkbox', { name: '选项二', exact: true })).toBeDisabled()
  await board.locator('.drawing-item').filter({ hasText: '手机号' }).click()
  await board.locator('.drawing-item').filter({ hasText: '多选框组' }).click()
  await expect(min).toHaveValue('1'); await expect(max).toHaveValue('1')
  await max.fill('2'); await max.press('Tab')
  await expect(max).toHaveValue('2')
  await expect(board.getByRole('checkbox', { name: '选项二', exact: true })).toBeEnabled()
  await board.locator('.el-checkbox').filter({ hasText: '选项二' }).click()
  await expect(board.locator('.el-checkbox.is-checked')).toHaveCount(2)
  const probe = await clipboardProbe(page)
  const code = await copyCode(page, probe)
  expect(code).toContain(':min="1"')
  expect(code).toContain(':max="2"')
  expect(code).toMatch(/field101:\s*\[1,\s*2\]/)
})
