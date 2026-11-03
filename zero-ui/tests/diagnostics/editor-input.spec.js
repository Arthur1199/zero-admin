import { test, expect } from '@playwright/test'

test('比较项目Editor、独立Quill和原生框的快速及逐字输入', async ({ page }, testInfo) => {
  test.setTimeout(90000)
  const results = []
  for (const mode of ['fast', 'paced', 'fill']) {
    for (const target of ['native', 'plain', 'vue']) {
      await page.goto('/tests/fixtures/editor-input-probe.html')
      const editor = page.locator(target === 'native' ? '#native-editor' : target === 'plain' ? '#plain-editor .ql-editor' : '#app .ql-editor')
      await expect(editor).toBeVisible()
      await editor.click()
      if (target !== 'native') await page.locator(target === 'plain' ? '.ql-toolbar:has(+ #plain-editor) .ql-bold' : '#app .ql-bold').click()
      if (mode === 'fill') await editor.fill('富文本验收')
      else await editor.pressSequentially('富文本验收', { delay: mode === 'paced' ? 100 : 0 })
      const text = await editor.innerText()
      results.push({ mode, target, text, html: await editor.innerHTML(), writes: await page.evaluate(() => window.editorWrites) })
    }
  }
  await testInfo.attach('input-comparison', { body: JSON.stringify(results), contentType: 'application/json' })
  console.log(JSON.stringify(results))
  // 比较记录不代表快速输入通过；逐字及fill的正确性单独断言。
  for (const result of results.filter(result => result.mode !== 'fast')) expect(result.text.trim(), result.mode + '/' + result.target).toBe('富文本验收')
})

test('连续20轮比较零延迟中文输入，记录顺序异常频次', async ({ page }, testInfo) => {
  test.setTimeout(120000)
  const results = []
  for (let trial = 0; trial < 20; trial++) {
    await page.goto('/tests/fixtures/editor-input-probe.html')
    for (const target of ['native', 'plain', 'vue']) {
      const editor = page.locator(target === 'native' ? '#native-editor' : target === 'plain' ? '#plain-editor .ql-editor' : '#app .ql-editor')
      await expect(editor).toBeVisible()
      await editor.click()
      if (target !== 'native') await page.locator(target === 'plain' ? '.ql-toolbar:has(+ #plain-editor) .ql-bold' : '#app .ql-bold').click()
      await editor.pressSequentially('富文本验收')
      results.push({ trial, target, text: await editor.innerText(), writes: await page.evaluate(() => window.editorWrites) })
    }
  }
  await testInfo.attach('fast-input-trials', { body: JSON.stringify(results), contentType: 'application/json' })
  console.log(JSON.stringify({ failures: results.filter(result => result.text !== '富文本验收'), counts: ['native', 'plain', 'vue'].map(target => ({ target, failed: results.filter(result => result.target === target && result.text !== '富文本验收').length })) }))
  expect(results).toHaveLength(60)
})
