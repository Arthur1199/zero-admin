import { test, expect } from '@playwright/test'
import { authenticated, mockAdminWebSocket } from './mock-api'

const themeLabels = ['毛玻璃拟态风', '高端暗黑风', '极简未来风', '原始风格', '轻奢商务风', '活力科技风', '温馨自然风', '质感设计风']

test.beforeEach(async ({ page }) => {
  await mockAdminWebSocket(page)
})

test('从顶部栏入口加载不会出现循环引用导致的空白页面', async ({ page }) => {
  const errors = []; page.on('pageerror', error => errors.push(error.message))
  await authenticated(page)
  await page.goto('/tests/fixtures/navbar-entry.html')
  await expect(page.locator('.zero-home')).toBeVisible()
  expect(errors).toEqual([])
})

test('八主题页签右键菜单读取主题变量且不再注入侧栏透明覆盖', async ({ page }) => {
  await authenticated(page)
  await page.goto('/system/config')
  for (const label of themeLabels) {
    await page.locator('.navbar .setting').click()
    await page.locator('.drawer-select').first().click()
    await page.getByRole('option', { name: label, exact: true }).click()
    await page.getByRole('button', { name: '关闭设置', exact: true }).click()
    await expect(page.locator('.el-drawer')).toBeHidden()
    await page.locator('.tags-view-item').last().click({ button: 'right' })
    const menu = page.locator('.tags-view-contextmenu')
    await expect(menu).toBeVisible()
    await expect.poll(() => menu.evaluate(element => {
      const probe = document.createElement('span')
      probe.style.color = 'var(--zero-text)'
      probe.style.backgroundColor = 'var(--zero-surface)'
      element.appendChild(probe)
      const actual = getComputedStyle(element)
      const expected = getComputedStyle(probe)
      const same = actual.color === expected.color && actual.backgroundColor === expected.backgroundColor
      probe.remove()
      return same
    })).toBe(true)
    await expect(page.locator('#glassmorphism-hover-override')).toHaveCount(0)
    await page.locator('.navbar').click({ position: { x: 350, y: 20 } })
    await expect(menu).toBeHidden()
  }
})

test('八主题输入框没有双层背景，布局大小图标保持居中', async ({ page }) => {
  test.setTimeout(120000)
  await authenticated(page)
  await page.goto('/system/config')
  for (const label of themeLabels) {
    await page.locator('.navbar .setting').click()
    await page.locator('.drawer-select').first().click()
    await page.getByRole('option', { name: label, exact: true }).click()
    await page.getByRole('button', { name: '关闭设置', exact: true }).click()
    await expect(page.locator('.el-drawer')).toBeHidden()
    await expect(page.locator('.app-main .fade-transform-leave-active, .app-main .fade-transform-enter-active')).toHaveCount(0)
    await page.getByRole('button', { name: '新增', exact: true }).click()
    const input = page.locator('.el-dialog .el-input__inner').first()
    await expect(input).toBeVisible()
    const dialogFrame = await page.locator('.el-dialog:visible').evaluate(element => {
      const style = getComputedStyle(element)
      const rect = element.getBoundingClientRect()
      const header = element.querySelector('.el-dialog__header').getBoundingClientRect()
      return { padding: style.padding, border: style.borderTopWidth, shadow: style.boxShadow, headerGap: Math.abs(rect.width - header.width) }
    })
    expect(dialogFrame, label).toEqual({ padding: '0px', border: '0px', shadow: 'none', headerGap: 0 })
    await input.fill('视觉验收')
    await expect.poll(() => input.evaluate(element => {
      const computed = getComputedStyle(element)
      return { border: computed.borderTopWidth, background: computed.backgroundColor, shadow: computed.boxShadow }
    }), { message: label }).toEqual({ border: '0px', background: 'rgba(0, 0, 0, 0)', shadow: 'none' })
    const offset = await page.evaluate(() => {
      const bar = document.querySelector('.navbar').getBoundingClientRect()
      const icon = document.querySelector('#size-select .svg-icon').getBoundingClientRect()
      return Math.abs(bar.top + bar.height / 2 - icon.top - icon.height / 2)
    })
    expect(offset, label).toBeLessThanOrEqual(1)
    await page.locator('.el-dialog').getByRole('button', { name: '取 消', exact: true }).click()
  }
})

test('八主题表单构建器按需样式可加载并保持画布和面板可见', async ({ page }, testInfo) => {
  test.setTimeout(60000)
  const errors = []
  page.on('pageerror', error => errors.push(error.message))
  await authenticated(page)
  await page.goto('/tool/build')
  for (const label of themeLabels) {
    await page.locator('.navbar .setting').click()
    await page.locator('.drawer-select').first().click()
    await page.getByRole('option', { name: label, exact: true }).click()
    await page.getByRole('button', { name: '关闭设置', exact: true }).click()
    await expect(page.locator('.el-drawer')).toBeHidden()
    await expect(page.locator('.form-build-container .left-board')).toBeVisible()
    await expect(page.locator('.form-build-container .right-board')).toBeVisible()
    await expect(page.locator('.form-build-container .drawing-board')).toBeVisible()
    await page.screenshot({ path: testInfo.outputPath(label + '-form-builder.png') })
  }
  expect(errors).toEqual([])
})

test('八主题表格更多按钮与修改删除保持同一水平线', async ({ page }) => {
  test.setTimeout(120000)
  await authenticated(page)
  await page.route('**/dev-api/system/user/list?**', route => route.fulfill({ json: {
    code: 200, rows: [{ userId: 2, userName: 'mock-user', nickName: '测试用户', status: '0', dept: {} }], total: 1
  } }))
  await page.goto('/system/user')
  await page.locator('.navbar .setting').click()
  for (const label of themeLabels) {
    await page.locator('.drawer-select').first().click()
    await page.getByRole('option', { name: label, exact: true }).click()
    await expect(page.locator('.app-main .fade-transform-leave-active, .app-main .fade-transform-enter-active')).toHaveCount(0)
    await expect(page.getByRole('button', { name: '更多', exact: true })).toBeVisible()
    await expect.poll(() => page.locator('.el-table__row').getByRole('button').evaluateAll(elements => {
      if (elements.length !== 3) return Infinity
      const centers = elements.map(element => {
      const rect = element.getBoundingClientRect()
      return rect.top + rect.height / 2
      })
      return Math.max(...centers) - Math.min(...centers)
    }), { message: label }).toBeLessThanOrEqual(1)
  }
})
