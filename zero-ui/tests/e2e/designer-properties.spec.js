import { test, expect } from '@playwright/test'
import { authenticated } from './mock-api'
import fs from 'node:fs/promises'
import { parse, compileTemplate, compileScript } from '@vue/compiler-sfc'

const palette = ['单行文本', '多行文本', '密码', '计数器', '下拉选择', '级联选择', '单选框组',
  '多选框组', '开关', '滑块', '时间选择', '时间范围', '日期选择', '日期范围', '评分', '颜色选择', '上传', '行容器', '按钮']

test('设计器默认值编辑及字段切换回显与画布一致', async ({ page }) => {
  const errors = []
  page.on('pageerror', error => errors.push(error.message))
  await authenticated(page)
  await page.goto('/tool/build')
  const board = page.locator('.drawing-board')
  await expect(board).toBeVisible()
  const value = page.getByPlaceholder('请输入默认值', { exact: true })
  await value.fill('属性默认文本')
  await value.press('Tab')
  await expect(value).toHaveValue('属性默认文本')
  await expect(board.locator('.el-input input')).toHaveValue('属性默认文本')
  await page.locator('.components-item').filter({ hasText: '计数器' }).click()
  await expect(value).toHaveValue('')
  await value.fill('7')
  await value.press('Tab')
  await expect(board.getByRole('spinbutton')).toHaveValue('7')
  await board.locator('.drawing-item').filter({ hasText: '手机号' }).click()
  await expect(value).toHaveValue('属性默认文本')
  await board.locator('.drawing-item').filter({ hasText: '计数器' }).click()
  await expect(value).toHaveValue('7')
  expect(errors).toEqual([])
})

test('设计器全部19类组件可编辑标题，页面和弹窗实际导出均可编译挂载', async ({ page, context }, testInfo) => {
  test.setTimeout(120000)
  const errors = []
  page.on('pageerror', error => errors.push(error.message))
  page.on('console', message => { if (message.text().includes('[Vue warn]')) errors.push(message.text()) })
  await authenticated(page)
  await page.route('https://jsonplaceholder.typicode.com/**', route => route.abort())
  await page.goto('/tool/build')
  const board = page.locator('.drawing-board')
  await expect(board).toBeVisible()
  await expect(page.locator('.components-item')).toHaveCount(palette.length)
  for (const [index, name] of palette.entries()) {
    await test.step(name, async () => {
      await page.locator('.components-body').filter({ hasText: new RegExp('^\\s*' + name + '\\s*$') }).click()
      await expect(board.locator('.drawing-item, .drawing-row-item')).toHaveCount(index + 2)
      if (name !== '行容器') {
        const label = '属性验收' + index
        await page.getByPlaceholder('请输入标题', { exact: true }).fill(label)
        await expect(board.locator('.active-from-item .el-form-item__label')).toHaveText(label)
      }
      expect(errors).toEqual([])
    })
  }
  for (const type of ['页面', '弹窗']) {
    await test.step(type + '导出', async () => {
      await page.getByRole('button', { name: '导出vue文件', exact: true }).click()
      const dialog = page.getByRole('dialog', { name: '选择生成类型', exact: true })
      await dialog.getByText(type, { exact: true }).click()
      const download = page.waitForEvent('download')
      await dialog.getByRole('button', { name: '确定', exact: true }).click()
      const file = await download
      const code = await fs.readFile(await file.path(), 'utf8')
      const { descriptor, errors: parseErrors } = parse(code)
      expect(parseErrors).toEqual([])
      expect(compileTemplate({ source: descriptor.template.content, id: type, filename: file.suggestedFilename() }).errors).toEqual([])
      expect(() => compileScript(descriptor, { id: type })).not.toThrow()
      const directory = 'output/designer-properties'
      const name = testInfo.project.name + '-' + (type === '页面' ? 'page' : 'dialog')
      await fs.mkdir(directory, { recursive: true })
      await fs.writeFile(`${directory}/${name}.vue`, code)
      await fs.writeFile(`${directory}/${name}.html`, `<!doctype html><html><head><meta charset="utf-8"></head><body><div id="app"></div><script type="module">
        import { createApp, h, ref } from 'vue'
        import ElementPlus from 'element-plus'
        import 'element-plus/dist/index.css'
        import Form from './${name}.vue'
        createApp({ setup() { const visible = ref(true); return () => h(Form, { modelValue: visible.value, 'onUpdate:modelValue': value => { visible.value = value } }) } }).use(ElementPlus).mount('#app')
      </script></body></html>`)
      const preview = await context.newPage()
      const exportErrors = []
      preview.on('pageerror', error => exportErrors.push(error.message))
      preview.on('console', message => { if (message.text().includes('[Vue warn]')) exportErrors.push(message.text()) })
      await preview.route('https://jsonplaceholder.typicode.com/**', route => route.abort())
      try {
        await preview.goto('/' + directory + '/' + name + '.html')
        await expect(preview.locator('.el-form')).toBeVisible()
        for (let index = 0; index < palette.length; index++) {
          if (palette[index] !== '行容器') await expect(preview.getByText('属性验收' + index, { exact: true })).toBeVisible()
        }
        expect(exportErrors).toEqual([])
      } finally { await preview.close() }
    })
  }
  expect(errors).toEqual([])
})
