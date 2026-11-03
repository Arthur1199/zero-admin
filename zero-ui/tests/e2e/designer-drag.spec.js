import { test, expect } from '@playwright/test'
import fs from 'node:fs/promises'
import { parse, compileTemplate, compileScript } from '@vue/compiler-sfc'
import { authenticated } from './mock-api'

async function dragComponent(page, source, target, position = { x: 30, y: 25 }) {
  await source.hover({ position: { x: 10, y: 10 } })
  const from = await source.boundingBox()
  const to = await target.boundingBox()
  expect(from).not.toBeNull()
  expect(to).not.toBeNull()
  const point = { x: to.x + position.x, y: to.y + position.y }
  expect(await target.evaluate((element, point) => element.contains(document.elementFromPoint(point.x, point.y)), point)).toBe(true)
  await page.mouse.move(from.x + 10, from.y + 10)
  await page.mouse.down()
  await page.mouse.move(from.x + 20, from.y + 15, { steps: 5 })
  await page.mouse.move(point.x, point.y, { steps: 20 })
  // 原生拖放在目标内移动两次，确保各内核发送 dragover，再松开鼠标。
  await page.mouse.move(point.x + 1, point.y + 1, { steps: 2 })
  await page.mouse.up()
}

test('设计器真实拖入、嵌套、排序、克隆，两类实际导出均能编译挂载', async ({ page, context }) => {
  test.setTimeout(120000)
  const errors = []; page.on('pageerror', error => errors.push(error.message))
  await authenticated(page)
  await page.goto('/tool/build')
  const board = page.locator('.drawing-board')
  const source = page.locator('.components-item').filter({ hasText: '单行文本' })
  await expect(board).toBeVisible()
  const count = await board.locator('.drawing-item').count()
  await dragComponent(page, source, board)
  await expect(board.locator('.drawing-item')).toHaveCount(count + 1)
  await dragComponent(page, page.locator('.components-item').filter({ hasText: '行容器' }), board)
  const row = board.locator('.drawing-row-item')
  await expect(row).toHaveCount(1)
  await dragComponent(page, source, row.locator('.drag-wrapper'))
  await expect(row.locator('.drawing-item')).toHaveCount(1)
  const nested = row.locator('.drawing-item')
  await nested.click()
  await page.getByPlaceholder('请输入标题', { exact: true }).fill('嵌套测试字段')
  await expect(nested.getByText('嵌套测试字段', { exact: true })).toBeVisible()
  await nested.locator('.drawing-item-copy').click()
  await expect(row.locator('.drawing-item')).toHaveCount(2)
  const copied = row.locator('.drawing-item').last()
  await copied.click()
  await page.getByPlaceholder('请输入标题', { exact: true }).fill('复制字段')
  await dragComponent(page, copied, nested.first(), { x: 30, y: 10 })
  await expect(row.locator('.el-form-item__label').first()).toHaveText('复制字段')
  for (const type of ['页面', '弹窗']) {
    await page.getByRole('button', { name: '导出vue文件', exact: true }).click()
    const dialog = page.locator('.el-dialog:visible')
    await dialog.getByText(type, { exact: true }).click()
    const download = page.waitForEvent('download')
    await dialog.getByRole('button', { name: '确定', exact: true }).click()
    const result = await download
    const sourceCode = await fs.readFile(await result.path(), 'utf8')
    const { descriptor, errors: parseErrors } = parse(sourceCode)
    expect(parseErrors).toEqual([])
    expect(compileTemplate({ source: descriptor.template.content, id: type, filename: result.suggestedFilename() }).errors).toEqual([])
    expect(() => compileScript(descriptor, { id: type })).not.toThrow()
    expect(sourceCode).toContain('复制字段')
    expect(sourceCode).toContain('嵌套测试字段')
    const fields = [...sourceCode.matchAll(/v-model="formData\.(\w+)"/g)].map(match => match[1])
    expect(new Set(fields).size).toBe(fields.length)
    const fixtureName = test.info().project.name + '-' + (type === '页面' ? 'page' : 'dialog')
    const fixtureDir = 'output/designer-acceptance'
    await fs.mkdir(fixtureDir, { recursive: true })
    await fs.writeFile(`${fixtureDir}/${fixtureName}.vue`, sourceCode, 'utf8')
    await fs.writeFile(`${fixtureDir}/${fixtureName}.html`, `<!doctype html><html lang="zh-CN"><head><meta charset="utf-8"><title>实际导出挂载验收</title></head><body><div id="app"></div><script type="module">
      import { createApp, h, ref } from 'vue'
      import ElementPlus from 'element-plus'
      import 'element-plus/dist/index.css'
      import Form from './${fixtureName}.vue'
      createApp({ setup() { const visible = ref(true); return () => h(Form, { modelValue: visible.value, 'onUpdate:modelValue': value => { visible.value = value } }) } }).use(ElementPlus).mount('#app')
    </script></body></html>`, 'utf8')
    const preview = await context.newPage()
    const previewErrors = []; preview.on('pageerror', error => previewErrors.push(error.message))
    preview.on('console', message => { if (message.text().includes('[Vue warn]')) previewErrors.push(message.text()) })
    try {
      await preview.goto('/' + fixtureDir + '/' + fixtureName + '.html')
      await expect(preview.locator('.el-form')).toBeVisible()
      await expect(preview.getByText('复制字段', { exact: true })).toBeVisible()
      await expect(preview.getByText('嵌套测试字段', { exact: true })).toBeVisible()
      const copiedBox = await preview.locator('.el-form-item').filter({ hasText: '复制字段' }).boundingBox()
      const nestedBox = await preview.locator('.el-form-item').filter({ hasText: '嵌套测试字段' }).boundingBox()
      const formBox = await preview.locator('.el-form').boundingBox()
      expect(copiedBox.width).toBeGreaterThan(formBox.width * 0.95)
      expect(nestedBox.width).toBeGreaterThan(formBox.width * 0.95)
      expect(nestedBox.y).toBeGreaterThan(copiedBox.y)
      await preview.getByRole('button', { name: type === '页面' ? '提交' : '确定', exact: true }).click()
      await expect(preview.locator('.el-form-item__error').first()).toBeVisible()
      for (const input of await preview.locator('.el-form .el-input input').all()) {
        await input.fill('13800138000')
        await expect(input).toHaveValue('13800138000')
      }
      await preview.getByRole('button', { name: type === '页面' ? '提交' : '确定', exact: true }).click()
      if (type === '弹窗') await expect(preview.locator('.el-dialog')).toBeHidden()
      else {
        await expect(preview.locator('.el-form-item__error')).toHaveCount(0)
        await preview.getByRole('button', { name: '重置', exact: true }).click()
        await expect(preview.locator('.el-input input').first()).toHaveValue('')
      }
      expect(previewErrors).toEqual([])
      await preview.screenshot({ path: `${fixtureDir}/${fixtureName}.png` })
    } finally { await preview.close() }
  }
  expect(errors).toEqual([])
})

test('删除全部设计字段后切换主题仍可重新添加，重建后字段名不重复', async ({ page }) => {
  const errors = []; page.on('pageerror', error => errors.push(error.message))
  await authenticated(page)
  await page.goto('/tool/build')
  const board = page.locator('.drawing-board')
  await expect(board.locator('.drawing-item')).toHaveCount(1)
  await board.locator('.drawing-item-delete').click()
  await expect(board.locator('.drawing-item')).toHaveCount(0)
  const switchTheme = async label => {
    await page.locator('.navbar .setting').click()
    await page.locator('.drawer-select').first().click()
    await page.getByRole('option', { name: label, exact: true }).click()
    await page.getByRole('button', { name: '关闭设置', exact: true }).click()
    await expect(page.locator('.app-main .fade-transform-leave-active, .app-main .fade-transform-enter-active')).toHaveCount(0)
  }
  await switchTheme('高端暗黑风')
  await expect(board).toBeVisible()
  await expect(page.getByText('从左侧拖入或点选组件进行表单设计', { exact: true })).toBeVisible()
  const source = page.locator('.components-item').filter({ hasText: '单行文本' })
  await dragComponent(page, source, board)
  await expect(board.locator('.drawing-item')).toHaveCount(1)
  const firstName = await page.getByPlaceholder('请输入字段名（v-model）').inputValue()
  await switchTheme('原始风格')
  await source.click()
  await expect(board.locator('.drawing-item')).toHaveCount(2)
  expect(await page.getByPlaceholder('请输入字段名（v-model）').inputValue()).not.toBe(firstName)
  expect(errors).toEqual([])
})
