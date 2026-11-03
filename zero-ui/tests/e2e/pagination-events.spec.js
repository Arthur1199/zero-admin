import { test, expect } from '@playwright/test'
import { authenticated } from './mock-api'
import fs from 'node:fs/promises'

for (const mode of ['fill-tab', 'typing-tab', 'typing-shift-tab', 'typing-click', 'fill-enter']) {
  test('跳页输入及失焦事件 ' + mode, async ({ page }, testInfo) => {
    await authenticated(page)
    const queries = [], events = []
    await page.route('**/dev-api/system/config/list?**', route => {
      const params = new URL(route.request().url()).searchParams
      const pageNum = Number(params.get('pageNum'))
      queries.push(pageNum)
      return route.fulfill({ json: { code: 200, total: 55, rows: [{ configId: pageNum, configName: '分页验证第' + pageNum + '页', configType: 'N' }] } })
    })
    await page.goto('/system/config')
    const editor = page.locator('.el-pagination__jump input')
    await expect(editor).toBeVisible()
    await page.exposeFunction('recordPaginationEvent', event => events.push(event))
    await editor.evaluate(element => {
      for (const type of ['input', 'change', 'blur', 'focus', 'keydown', 'keyup']) {
        element.addEventListener(type, event => window.recordPaginationEvent({
          type, key: event.key || '', value: element.value, trusted: event.isTrusted,
          prevented: event.defaultPrevented,
          focused: element === document.activeElement
        }))
      }
    })
    try {
      if (mode.startsWith('typing')) {
        await editor.fill('')
        await editor.pressSequentially('2')
      } else await editor.fill('2')
      if (mode.endsWith('shift-tab')) await editor.press('Shift+Tab')
      else if (mode.endsWith('tab')) {
        // 与原生页末input对照，避免把浏览器未移动焦点误判为Vue未处理change。
        const native = await page.context().newPage()
        await native.setContent('<!doctype html><html><body><button>前一项</button><input type="number" value="1"></body></html>')
        await native.locator('input').fill('2')
        await native.locator('input').press('Tab')
        const nativeFocused = await native.locator('input').evaluate(element => element === document.activeElement)
        await editor.press('Tab')
        const appFocused = await editor.evaluate(element => element === document.activeElement)
        expect(appFocused).toBe(nativeFocused)
        if (appFocused) {
          expect(queries.at(-1)).toBe(1)
          await editor.press('Shift+Tab')
        }
        await native.close()
      }
      else if (mode.endsWith('enter')) await editor.press('Enter')
      else await page.locator('.el-form-item__label').filter({ hasText: '参数名称' }).click()
      await expect(page.locator('.el-pager .is-active')).toHaveText('2')
      await expect(page.getByText('分页验证第2页', { exact: true })).toBeVisible()
      await expect.poll(() => queries.at(-1)).toBe(2)
    } finally {
      const state = await editor.evaluate(element => ({
        value: element.value, focused: element === document.activeElement,
        activeTag: document.activeElement.tagName,
        activeClass: document.activeElement.className, documentFocused: document.hasFocus()
      }))
      await testInfo.attach('pagination-events', { body: JSON.stringify({ mode, events, state, queries }, null, 2), contentType: 'application/json' })
      await fs.mkdir('output/playwright/pagination-events', { recursive: true })
      await fs.writeFile(`output/playwright/pagination-events/${testInfo.project.name}-${mode}.json`, JSON.stringify({ mode, events, state, queries }, null, 2))
    }
  })
}

test('原生页面末尾数字输入框的Tab失焦基线', async ({ page }, testInfo) => {
  await page.setContent('<!doctype html><html><body><button>前一项</button><input type="number" value="1"></body></html>')
  const input = page.locator('input')
  const events = []
  await page.exposeFunction('recordNativeEvent', type => events.push(type))
  await input.evaluate(element => {
    for (const type of ['input', 'change', 'blur']) element.addEventListener(type, () => window.recordNativeEvent(type))
  })
  await input.fill('2')
  await input.press('Tab')
  const state = await input.evaluate(element => ({ value: element.value, focused: element === document.activeElement, documentFocused: document.hasFocus() }))
  await fs.mkdir('output/playwright/pagination-events', { recursive: true })
  await fs.writeFile(`output/playwright/pagination-events/${testInfo.project.name}-native-tab.json`, JSON.stringify({ events, state }, null, 2))
  // 记录平台页末Tab行为后，用页面内目标确认正常失焦能发送change。
  await page.getByRole('button', { name: '前一项' }).click()
  await expect(input).not.toBeFocused()
  expect(events).toContain('change')
})
