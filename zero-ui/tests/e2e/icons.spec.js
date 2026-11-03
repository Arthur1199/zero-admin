import { test, expect } from '@playwright/test'
import fs from 'node:fs'
import { authenticated } from './mock-api'

test('全部本地SVG注册为有效sprite，设计器按钮图标不包含解析错误节点', async ({ page }) => {
  await authenticated(page)
  await page.goto('/tool/build')
  await expect(page.locator('.drawing-board')).toBeVisible()
  const expected = fs.readdirSync('src/assets/icons/svg').filter(file => file.endsWith('.svg')).map(file => 'icon-' + file.slice(0, -4)).sort()
  const actual = await page.locator('symbol[id^="icon-"]').evaluateAll(elements => ({
    ids: elements.map(element => element.id).sort(),
    invalid: elements.filter(element => element.querySelector('parsererror') || [...element.children].some(child => child.namespaceURI !== 'http://www.w3.org/2000/svg')).map(element => element.id)
  }))
  expect(actual.ids).toEqual(expected)
  expect(actual.invalid).toEqual([])
  await expect(page.locator('#icon-button')).toHaveAttribute('viewBox', '0 0 1024 1024')
  await expect(page.locator('.components-item').filter({ hasText: '按钮' }).locator('use')).toHaveAttribute('xlink:href', '#icon-button')
})
