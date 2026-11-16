import { test, expect } from '@playwright/test'
import fs from 'node:fs/promises'

test('bootAdmin外链进入独立登录页，仅验证公开页面加载', async ({ browser }, testInfo) => {
  const context = await browser.newContext()
  const page = await context.newPage(), errors = [], blocked = []
  const copiedCookies = (await context.cookies()).length
  page.on('pageerror', error => errors.push(error.message))
  await page.route('**/*', route => {
    if (['GET', 'HEAD', 'OPTIONS'].includes(route.request().method())) return route.continue()
    blocked.push({ method: route.request().method(), path: new URL(route.request().url()).pathname })
    return route.abort('blockedbyclient')
  })
  try {
    await page.goto('http://127.0.0.1:9100/')
    await expect(page).toHaveURL(/\/login(?:[?#]|$)/)
    await expect(page.locator('input[type=password]')).toBeVisible()
    expect(copiedCookies).toBe(0)
    expect(errors).toEqual([])
    expect(blocked).toEqual([])
    await fs.mkdir('output/playwright/boot-admin-20261115', { recursive: true })
    await fs.writeFile(`output/playwright/boot-admin-20261115/${testInfo.project.name}.json`, JSON.stringify({
      url: page.url(), status: '独立登录页可加载，登录后监控功能待验证', copiedCookies, errors, blocked
    }, null, 2), 'utf8')
  } finally {
    await context.close()
  }
})
