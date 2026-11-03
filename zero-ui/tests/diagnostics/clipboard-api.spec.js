import { test, expect } from '@playwright/test'
test('比较原生复制与标准Clipboard API的实际粘贴结果', async ({ page }, testInfo) => {
  await page.goto('/tests/fixtures/clipboard-api-probe.html')
  await page.getByRole('button', { name: '原生复制', exact: true }).click()
  const paste = page.getByRole('textbox', { name: '粘贴对照', exact: true })
  await paste.click(); await paste.press('Control+V')
  const legacy = await paste.inputValue()
  expect(legacy).toBe('legacy-probe')
  await paste.fill('')
  await page.getByRole('button', { name: '标准API复制', exact: true }).click()
  await expect(page.locator('#status')).toHaveText(/written|writeError/)
  const modern = JSON.parse(await page.locator('#status').innerText())
  await paste.click(); await paste.press('Control+V')
  const result = { legacy, modern, pasted: await paste.inputValue() }
  await testInfo.attach('clipboard-api-comparison', { body: JSON.stringify(result), contentType: 'application/json' })
  console.log(JSON.stringify(result))
})
