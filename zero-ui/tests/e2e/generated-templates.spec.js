import { test, expect } from '@playwright/test'
import fs from 'node:fs/promises'
import path from 'node:path'
import { parse, compileTemplate, compileScript } from '@vue/compiler-sfc'
import { authenticated } from './mock-api'

for (const category of ['crud', 'tree', 'sub']) {
  test('真实后端生成的Vue3组件可编译挂载并完成增改删草稿流程 ' + category, async ({ page }, testInfo) => {
    test.setTimeout(90000)
    const business = 'fixture' + category[0].toUpperCase() + category.slice(1)
    const sourceRoot = path.resolve('../zero-admin/target/generated-template-check/' + category + '-element-plus')
    const source = await fs.readFile(path.join(sourceRoot, 'vue/views/templatecheck', business, 'index.vue'), 'utf8')
    const { descriptor, errors } = parse(source)
    expect(errors).toEqual([])
    expect(compileTemplate({ source: descriptor.template.content, id: category }).errors).toEqual([])
    expect(() => compileScript(descriptor, { id: category })).not.toThrow()
    const directory = 'output/generated-templates/' + testInfo.project.name + '-' + category
    await fs.mkdir(directory, { recursive: true })
    await fs.writeFile(directory + '/Fixture.vue', source.replace('@/api/templatecheck/' + business, './api.js'))
    await fs.copyFile(path.join(sourceRoot, 'vue/api/templatecheck', business + '.js'), directory + '/api.js')
    await fs.writeFile(directory + '/index.html', `<!doctype html><html><head><meta charset="utf-8"></head><body><div id="app"></div><script type="module">
      history.replaceState(null, '', '/index')
      await import('/src/main.js')
      const { default: Fixture } = await import('./Fixture.vue')
      const router = document.querySelector('#app').__vue_app__.config.globalProperties.$router
      await router.isReady()
      router.addRoute({ path: '/template-case', component: (await import('/src/layout/index.vue')).default, children: [{ path: '', component: Fixture, meta: { title: '模板验收' } }] })
      await router.push('/template-case')
    </script></body></html>`)
    const pageErrors = []
    page.on('pageerror', error => pageErrors.push(error.message))
    page.on('console', message => { if (message.text().includes('[Vue warn]')) pageErrors.push(message.text()) })
    await authenticated(page)
    const writes = []
    let failList = true, failSave = true
    const rows = [{ id: 1, parentId: 0, title: '隔离生成记录', status: 0, tags: null, amount: 12,
      eventTime: '2026-11-16', image: '', files: '', content: '<p>富文本验收</p>', fixtureLineList: [] }]
    await page.route('**/dev-api/templatecheck/' + business + '**', route => {
      const request = route.request(), pathname = new URL(request.url()).pathname
      if (request.method() !== 'GET') {
        writes.push({ method: request.method(), data: request.postData() ? JSON.parse(request.postData()) : null })
        return route.fulfill({ json: failSave && request.method() !== 'DELETE'
          ? { code: 500, msg: '模拟保存失败' } : { code: 200, msg: '操作成功' } })
      }
      if (pathname.endsWith('/list')) return route.fulfill({ json: failList ? { code: 500, msg: '模拟列表失败' }
        : { code: 200, rows, data: rows, total: rows.length } })
      return route.fulfill({ json: { code: 200, data: rows[0] } })
    })
    await page.goto('/' + directory + '/index.html')
    await expect(page.getByText('模拟列表失败', { exact: true })).toBeVisible()
    await expect(page.locator('.app-main .el-loading-mask:visible')).toHaveCount(0)
    failList = false
    await page.getByRole('button', { name: '搜索', exact: true }).click()
    const row = page.getByRole('row').filter({ hasText: '隔离生成记录' })
    await expect(row).toBeVisible()
    await row.getByRole('button', { name: '修改', exact: true }).click()
    const dialog = page.getByRole('dialog', { name: '修改隔离验收' + category, exact: true })
    await expect(dialog).toBeVisible()
    await expect(dialog.getByPlaceholder('请输入名称', { exact: true })).toHaveValue('隔离生成记录')
    // null复选字段仍可回显、再次提交；禁用状态保留数值0，而不是字符串。
    await dialog.getByPlaceholder('请输入名称', { exact: true }).fill('修改后名称')
    await dialog.getByRole('button', { name: '确 定', exact: true }).click()
    await expect(page.getByText('模拟保存失败', { exact: true })).toBeVisible()
    await expect(dialog).toBeVisible()
    await expect(dialog.getByPlaceholder('请输入名称', { exact: true })).toHaveValue('修改后名称')
    await page.waitForTimeout(1100)
    failSave = false
    await dialog.getByRole('button', { name: '确 定', exact: true }).click()
    await expect(dialog).toBeHidden()
    expect(writes[1].data).toMatchObject({ title: '修改后名称', tags: '', status: 0 })
    await page.getByRole('button', { name: '新增', exact: true }).first().click()
    const add = page.getByRole('dialog', { name: '添加隔离验收' + category, exact: true })
    await expect(add).toBeVisible()
    await add.getByPlaceholder('请输入名称', { exact: true }).first().fill('新增验收名称')
    if (category === 'sub') {
      await add.getByRole('button', { name: '添加', exact: true }).click()
      await add.getByPlaceholder('请输入名称', { exact: true }).nth(1).fill('明细名称')
    }
    await add.getByRole('button', { name: '确 定', exact: true }).click()
    await expect(add).toBeHidden()
    const post = writes.find(write => write.method === 'POST')
    expect(post.data.title).toBe('新增验收名称')
    if (category === 'sub') expect(post.data.fixtureLineList[0].title).toBe('明细名称')
    await row.getByRole('button', { name: '删除', exact: true }).click()
    await page.getByRole('button', { name: '确定', exact: true }).click()
    await expect.poll(() => writes.filter(write => write.method === 'DELETE').length).toBe(1)
    expect(pageErrors).toEqual([])
  })
}
