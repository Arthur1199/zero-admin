import { test, expect } from '@playwright/test'
import fs from 'node:fs/promises'
import { authenticated } from './mock-api'

const columns = ['id', 'name', 'remark'].map((name, index) => ({ columnId: 11 + index, tableId: 2, columnName: name,
  columnComment: ['编号', '名称', '备注'][index], columnType: 'varchar(50)', javaType: 'String', javaField: name,
  isInsert: '1', isEdit: '1', isList: '1', isQuery: '0', isRequired: '0', queryType: 'EQ', htmlType: 'input', sort: index + 1 }))
const info = { tableId: 2, tableName: 'mock_table', tableComment: '模拟生成配置', className: 'MockTable', functionAuthor: 'Mock',
  tplCategory: 'crud', tplWebType: 'element-ui', packageName: 'com.mock.system', moduleName: 'system', businessName: 'mock',
  functionName: '模拟功能', genType: '1', genPath: '/mock-output', parentMenuId: 100, columns }
const previewCode = '<template><div>模拟Vue3预览</div></template>\n'
const preview = { 'vm/java/domain.java.vm': 'public class MockTable {}', 'vm/vue/v3/index.vue.vm': previewCode }
const metadata = { code: 200, data: { info, rows: columns, tables: [{ tableName: 'mock_child', tableComment: '模拟子表', columns }] } }

function watchErrors(page) {
  const errors = []
  page.on('pageerror', error => errors.push(error.message))
  return errors
}

async function fixtures(page) {
  const requests = await authenticated(page)
  await page.route('**/dev-api/tool/gen/list?**', route => route.fulfill({ json: { code: 200, rows: [info], total: 1 } }))
  await page.route('**/dev-api/tool/gen/db/list?**', route => route.fulfill({ json: { code: 200, rows: [info], total: 1 } }))
  await page.route('**/dev-api/tool/gen/2', route => route.fulfill({ json: metadata }))
  await page.route('**/dev-api/system/menu/list', route => route.fulfill({ json: { code: 200, data: [{ menuId: 100, parentId: 0, menuName: '模拟菜单' }] } }))
  await page.route('**/dev-api/tool/gen/preview/2', route => route.fulfill({ json: { code: 200, data: preview } }))
  return requests
}

for (const create of [true, false]) {
  test('代码生成' + (create ? '创建表' : '导入表') + '失败保留输入并按原参数重试', async ({ page }) => {
    const errors = watchErrors(page), writes = []
    await fixtures(page)
    await page.route('**/dev-api/tool/gen/' + (create ? 'createTable' : 'importTable') + '?**', route => {
      const params = new URL(route.request().url()).searchParams
      writes.push({ method: route.request().method(), value: params.get(create ? 'sql' : 'tables') })
      return route.fulfill({ json: writes.length === 1 ? { code: 500, msg: '模拟生成维护失败' } : { code: 200, msg: '模拟维护成功' } })
    })
    await page.goto('/tool/gen')
    await page.getByRole('button', { name: create ? '创建' : '导入', exact: true }).click()
    const dialog = page.getByRole('dialog', { name: create ? '创建表' : '导入表', exact: true })
    const sql = 'CREATE TABLE mock_acceptance (id bigint);'
    if (create) await dialog.getByPlaceholder('请输入文本').fill(sql)
    else await dialog.getByRole('cell', { name: info.tableName, exact: true }).click()
    await dialog.getByRole('button', { name: '确 定', exact: true }).click()
    await expect(page.getByText('模拟生成维护失败', { exact: true })).toBeVisible()
    await expect(dialog).toBeVisible()
    if (create) await expect(dialog.getByPlaceholder('请输入文本')).toHaveValue(sql)
    else await expect(dialog.getByRole('row').filter({ hasText: info.tableName }).getByRole('checkbox')).toBeChecked()
    expect(errors).toEqual([])
    await page.waitForTimeout(1100)
    await dialog.getByRole('button', { name: '确 定', exact: true }).click()
    await expect(dialog).toBeHidden()
    expect(writes).toEqual(Array(2).fill({ method: 'POST', value: create ? sql : info.tableName }))
    expect(errors).toEqual([])
  })
}

test('生成预览读取失败不打开空弹窗，重试显示两类模板', async ({ page }) => {
  const errors = watchErrors(page), requests = await fixtures(page)
  let fail = true
  await page.route('**/dev-api/tool/gen/preview/2', route => fail ? route.fulfill({ json: { code: 500, msg: '模拟预览失败' } }) : route.fallback())
  await page.goto('/tool/gen')
  const open = page.getByRole('row').filter({ hasText: info.tableName }).getByRole('button', { name: '预览', exact: true })
  await open.click()
  await expect(page.getByText('模拟预览失败', { exact: true })).toBeVisible()
  await expect(page.getByRole('dialog', { name: '代码预览', exact: true })).toBeHidden()
  expect(errors).toEqual([])
  fail = false
  await open.click()
  const dialog = page.getByRole('dialog', { name: '代码预览', exact: true })
  await expect(dialog).toContainText('public class MockTable {}')
  await dialog.getByRole('tab', { name: 'index.vue', exact: true }).click()
  await expect(dialog.locator('.el-tab-pane:visible code')).toHaveText(previewCode.trim())
  await dialog.getByRole('button', { name: 'Close this dialog', exact: true }).or(dialog.getByRole('button', { name: '关闭此对话框', exact: true })).click()
  expect(requests.filter(request => request.method !== 'GET')).toEqual([])
  expect(errors).toEqual([])
})

test('自定义路径生成失败不提前成功，重试使用原路径接口', async ({ page }) => {
  const errors = watchErrors(page), reads = []
  await fixtures(page)
  await page.route('**/dev-api/tool/gen/genCode/mock_table', route => {
    reads.push(route.request().method())
    return route.fulfill({ json: reads.length === 1 ? { code: 500, msg: '模拟路径生成失败' } : { code: 200 } })
  })
  await page.goto('/tool/gen')
  const generate = page.getByRole('button', { name: '生成代码', exact: true })
  await generate.click()
  await expect(page.getByText('模拟路径生成失败', { exact: true })).toBeVisible()
  await expect(page.locator('.el-message--success')).toHaveCount(0)
  expect(errors).toEqual([])
  await generate.click()
  await expect(page.getByText('成功生成到自定义路径：/mock-output', { exact: true })).toBeVisible()
  expect(reads).toEqual(['GET', 'GET'])
  expect(errors).toEqual([])
})

for (const endpoint of ['/tool/gen/2', '/system/dict/type/optionselect', '/system/menu/list']) {
  test('生成配置依赖读取失败结束加载且禁止提交，刷新恢复 ' + endpoint, async ({ page }) => {
    const errors = watchErrors(page), requests = await fixtures(page)
    let fail = true
    await page.route('**/dev-api' + endpoint, route => fail ? route.fulfill({ json: { code: 500, msg: '模拟生成配置读取失败' } }) : route.fallback())
    await page.goto('/tool/gen-edit/index/2?pageNum=3')
    await expect(page.getByText('模拟生成配置读取失败', { exact: true })).toBeVisible()
    await expect(page.locator('.app-main .el-loading-mask:visible')).toHaveCount(0)
    await expect(page.getByRole('button', { name: '提交', exact: true })).toBeDisabled()
    expect(errors).toEqual([])
    fail = false
    await page.reload()
    await expect(page.getByRole('button', { name: '提交', exact: true })).toBeEnabled()
    await expect(page.locator('.el-table__body-wrapper')).toContainText('id')
    expect(requests.filter(request => request.method !== 'GET')).toEqual([])
    expect(errors).toEqual([])
  })
}

test('生成配置保存拒绝保留草稿，重试完整字段并回到原页码', async ({ page }) => {
  const errors = watchErrors(page), writes = []
  await fixtures(page)
  await page.route('**/dev-api/tool/gen', route => {
    writes.push({ method: route.request().method(), data: route.request().postDataJSON() })
    return route.fulfill({ json: writes.length === 1 ? { code: 500, msg: '模拟生成配置保存失败' } : { code: 200, msg: '模拟保存成功' } })
  })
  await page.goto('/tool/gen-edit/index/2?pageNum=3')
  await expect(page).toHaveURL(url => url.searchParams.get('pageNum') === '3')
  await page.getByRole('tab', { name: '基本信息', exact: true }).click()
  const comment = page.locator('.el-tab-pane:visible .el-form-item').filter({ hasText: '表描述' }).locator('input')
  await expect(comment).toHaveValue(info.tableComment)
  await comment.fill('失败后保留的生成配置')
  await page.getByRole('button', { name: '提交', exact: true }).click()
  await expect(page.getByText('模拟生成配置保存失败', { exact: true })).toBeVisible()
  await expect(comment).toHaveValue('失败后保留的生成配置')
  expect(errors).toEqual([])
  await page.waitForTimeout(1100)
  await page.getByRole('button', { name: '提交', exact: true }).click()
  await expect(page).toHaveURL(url => url.pathname === '/tool/gen' && url.searchParams.get('pageNum') === '3')
  expect(writes).toHaveLength(2)
  expect(writes[0]).toEqual(writes[1])
  expect(writes[0]).toMatchObject({ method: 'PUT', data: { tableId: 2, tableComment: '失败后保留的生成配置', columns, params: { parentMenuId: 100 } } })
  expect(errors).toEqual([])
})

for (const webType of ['element-ui', 'element-plus']) {
  test('生成配置字段可真实拖拽，复选字符串及模板选项保持 ' + webType, async ({ page }) => {
    const errors = watchErrors(page), writes = []
    await fixtures(page)
    await page.route('**/dev-api/tool/gen', route => {
      writes.push(route.request().postDataJSON())
      return route.fulfill({ json: { code: 200, msg: '模拟保存成功' } })
    })
    await page.goto('/tool/gen-edit/index/2?pageNum=3')
    const rows = page.locator('.el-tab-pane:visible .el-table__body-wrapper tbody tr')
    await expect(rows).toHaveCount(3)
    await expect(page.locator('.fade-transform-enter-active')).toHaveCount(0)
    await rows.nth(0).locator('td.allowDrag').first().hover()
    await page.locator('.el-tab-pane:visible tbody').evaluate(element => {
      element.addEventListener('end', event => {
        element.dataset.dragOld = String(event.oldIndex)
        element.dataset.dragNew = String(event.newIndex)
      }, { once: true })
    })
    expect(errors).toEqual([])
    const from = await rows.nth(0).locator('td.allowDrag').first().boundingBox()
    const to = await rows.nth(1).locator('td.allowDrag').first().boundingBox()
    await page.mouse.move(from.x + from.width / 2, from.y + from.height / 2)
    await page.mouse.down()
    await page.mouse.move(from.x + from.width / 2, from.y + from.height / 2 + 5, { steps: 3 })
    await page.mouse.move(to.x + to.width / 2, to.y + to.height - 3, { steps: 18 })
    await page.mouse.move(to.x + to.width / 2 + 1, to.y + to.height - 2, { steps: 2 })
    await page.mouse.up()
    const body = page.locator('.el-tab-pane:visible tbody')
    await expect(body).toHaveAttribute('data-drag-old', '0')
    await expect(body).toHaveAttribute('data-drag-new', /^[12]$/)
    // 各内核原生拖放交换时机不同，按真实落位核对显示和提交，而非假定鼠标轨迹对应固定下标。
    const destination = Number(await body.getAttribute('data-drag-new'))
    const expectedNames = destination === 1 ? ['name', 'id', 'remark'] : ['name', 'remark', 'id']
    const expectedIds = destination === 1 ? [12, 11, 13] : [12, 13, 11]
    await expect(rows.nth(0).locator('td').nth(1)).toHaveText('name')
    await expect(rows.nth(1).locator('td').nth(1)).toHaveText(expectedNames[1])
    await expect(rows.nth(2).locator('td').nth(1)).toHaveText(expectedNames[2])
    await rows.nth(0).locator('.el-checkbox').first().click()
    await page.getByRole('tab', { name: '生成信息', exact: true }).click()
    await page.locator('.el-tab-pane:visible .el-form-item').filter({ hasText: '前端类型' }).locator('.el-select__wrapper').click()
    await expect(page.getByRole('option', { name: 'Vue2 Element UI 模版', exact: true })).toBeVisible()
    await expect(page.getByRole('option', { name: 'Vue3 Element Plus 模版', exact: true })).toBeVisible()
    await page.getByRole('option', { name: webType === 'element-ui' ? 'Vue2 Element UI 模版' : 'Vue3 Element Plus 模版', exact: true }).click()
    await page.getByRole('button', { name: '提交', exact: true }).click()
    await expect(page).toHaveURL(url => url.pathname === '/tool/gen')
    expect(writes).toHaveLength(1)
    expect(writes[0].columns.map(column => column.columnId)).toEqual(expectedIds)
    expect(writes[0].columns.map(column => column.sort)).toEqual([1, 2, 3])
    expect(writes[0].columns[0].isInsert).toBe('0')
    expect(writes[0].tplWebType).toBe(webType)
    expect(errors).toEqual([])
  })
}

test('代码预览复制可实际粘贴完整内容', async ({ page }) => {
  const errors = watchErrors(page)
  await fixtures(page)
  await page.goto('/tool/gen')
  await page.getByRole('button', { name: '预览', exact: true }).click()
  const dialog = page.getByRole('dialog', { name: '代码预览', exact: true })
  await dialog.getByRole('tab', { name: 'index.vue', exact: true }).click()
  await dialog.evaluate(element => {
    const probe = document.createElement('textarea')
    probe.dataset.testid = 'gen-copy-probe'
    probe.setAttribute('aria-label', '预览复制验收')
    probe.style.cssText = 'width:100%;height:60px'
    element.appendChild(probe)
  })
  const probe = page.getByTestId('gen-copy-probe')
  await probe.fill('旧剪贴板对照')
  await probe.press('Control+A')
  await probe.press('Control+C')
  await probe.fill('')
  await dialog.locator('.el-tab-pane:visible .el-link').click()
  await expect(page.getByText('复制成功', { exact: true })).toBeVisible()
  await probe.click()
  await probe.press('Control+V')
  await expect(probe).toHaveValue(previewCode)
  expect(errors).toEqual([])
})

test('批量生成沿用表名参数并下载服务端ZIP响应', async ({ page }, testInfo) => {
  const errors = watchErrors(page), requested = []
  await fixtures(page)
  // 空ZIP只验证客户端下载协议，不代表后端模板生成成功。
  const zip = Buffer.from('UEsFBgAAAAAAAAAAAAAAAAAAAAAAAA==', 'base64')
  await page.route('**/dev-api/tool/gen/batchGenCode?**', route => {
    const request = route.request()
    requested.push({ method: request.method(), tables: new URL(request.url()).searchParams.get('tables') })
    return route.fulfill({ contentType: 'application/zip', body: zip })
  })
  await page.goto('/tool/gen')
  await page.getByRole('row').filter({ hasText: info.tableName }).locator('.el-checkbox').click()
  const download = page.waitForEvent('download')
  await page.getByRole('button', { name: '生成', exact: true }).click()
  const file = await download
  expect(file.suggestedFilename()).toBe('zero.zip')
  const target = testInfo.outputPath('mock-zero.zip')
  await file.saveAs(target)
  expect(await fs.readFile(target)).toEqual(zip)
  expect(requested).toEqual([{ method: 'GET', tables: info.tableName }])
  expect(errors).toEqual([])
})
