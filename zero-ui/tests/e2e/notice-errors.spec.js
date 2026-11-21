import { test, expect } from '@playwright/test'
import { authenticated, mockAdminWebSocket } from './mock-api'

const initialNotice = { noticeId: 1, noticeTitle: '模拟公告', noticeType: '1', status: '0', noticeContent: '<p>旧内容</p>' }

test.beforeEach(async ({ page }) => {
  await mockAdminWebSocket(page)
})

async function noticeDictionaries(page) {
  await page.route('**/dev-api/system/dict/data/type/sys_notice_type', route => route.fulfill({ json: { code: 200, data: [
    { dictLabel: '通知', dictValue: '1', listClass: 'primary', cssClass: '' },
    { dictLabel: '公告', dictValue: '2', listClass: 'success', cssClass: '' }
  ] } }))
  await page.route('**/dev-api/system/dict/data/type/sys_notice_status', route => route.fulfill({ json: { code: 200, data: [
    { dictLabel: '正常', dictValue: '0', listClass: 'success', cssClass: '' },
    { dictLabel: '关闭', dictValue: '1', listClass: 'danger', cssClass: '' }
  ] } }))
}

for (const themeStyle of ['glassmorphism', 'default']) {
  test(`公告编辑器工具栏颜色适配 ${themeStyle}`, async ({ page }) => {
    await authenticated(page)
    await noticeDictionaries(page)
    await page.addInitScript(theme => {
      localStorage.setItem('layout-setting', JSON.stringify({ themeStyle: theme }))
    }, themeStyle)
    await page.goto('/system/notice')
    await page.getByRole('button', { name: '新增', exact: true }).click()

    const toolbar = page.getByRole('dialog', { name: '添加公告' }).locator('.ql-toolbar')
    const bold = toolbar.locator('button.ql-bold')
    const boldStroke = bold.locator('.ql-stroke').first()
    const size = toolbar.locator('.ql-size .ql-picker-label')
    const color = themeStyle === 'glassmorphism' ? 'rgb(255, 255, 255)' : 'rgb(68, 68, 68)'
    await expect(boldStroke).toHaveCSS('stroke', color)
    await expect(toolbar.locator('button.ql-image circle.ql-fill')).toHaveCSS('fill', color)
    await expect(size).toHaveCSS('color', color)

    if (themeStyle === 'glassmorphism') {
      await bold.click()
      await expect(bold).toHaveClass(/ql-active/)
      await expect(boldStroke).toHaveCSS('stroke', color)
      await size.click()
      const options = toolbar.locator('.ql-size .ql-picker-options')
      await expect(options).toBeVisible()
      await expect(options).toHaveCSS('color', color)
    }
  })
}

for (const action of ['新增', '修改']) {
  test('公告' + action + '服务端校验失败保留正文，修正标题后可重试且字典主色标签正常', async ({ page }) => {
    const errors = [], warnings = []
    page.on('pageerror', error => errors.push(error.message))
    page.on('console', message => { if (message.type() === 'warning' && message.text().includes('prop "type"')) warnings.push(message.text()) })
    await authenticated(page)
    await noticeDictionaries(page)
    const records = [{ ...initialNotice }], writes = []
    await page.route('**/dev-api/system/notice/list?**', route => route.fulfill({ json: { code: 200, rows: records, total: records.length } }))
    await page.route('**/dev-api/system/notice/1', route => route.fulfill({ json: { code: 200, data: records[0] } }))
    await page.route('**/dev-api/system/notice', route => {
      const request = route.request(), data = request.postDataJSON()
      writes.push({ method: request.method(), data })
      if (data.noticeTitle.length > 50) return route.fulfill({ json: { code: 500, msg: '公告标题不能超过50个字符' } })
      if (action === '新增') records.push({ ...data, noticeId: 2 })
      else records[0] = { ...data }
      return route.fulfill({ json: { code: 200, msg: '操作成功' } })
    })
    const loaded = page.waitForResponse(response => response.url().includes('/dev-api/system/notice/list'))
    await page.goto('/system/notice', { waitUntil: 'domcontentloaded' })
    await loaded
    await expect(page.getByRole('row').filter({ hasText: '模拟公告' }).locator('.el-tag--primary')).toHaveText('通知')
    if (action === '新增') await page.getByRole('button', { name: '新增', exact: true }).click()
    else await page.getByRole('row').filter({ hasText: '模拟公告' }).getByRole('button', { name: '修改', exact: true }).click()
    const dialog = page.getByRole('dialog', { name: action === '新增' ? '添加公告' : '修改公告' })
    const invalidTitle = '模拟验收_' + 'a'.repeat(60)
    await dialog.getByPlaceholder('请输入公告标题').fill(invalidTitle)
    await dialog.locator('.el-select__wrapper').click()
    await page.getByRole('option', { name: '公告', exact: true }).click()
    await dialog.getByText('关闭', { exact: true }).click()
    await dialog.locator('.ql-editor').fill('编辑后应保留的模拟正文')
    await dialog.getByRole('button', { name: '确 定', exact: true }).click()
    await expect(page.getByText('公告标题不能超过50个字符', { exact: true })).toBeVisible()
    await expect(dialog).toBeVisible()
    await expect(dialog.getByPlaceholder('请输入公告标题')).toHaveValue(invalidTitle)
    await expect(dialog.locator('.ql-editor')).toHaveText('编辑后应保留的模拟正文')
    expect(records).toEqual([initialNotice])
    expect(errors).toEqual([])
    await dialog.getByPlaceholder('请输入公告标题').fill('校正后的模拟公告')
    await dialog.getByRole('button', { name: '确 定', exact: true }).click()
    await expect(dialog).toBeHidden()
    await expect(page.getByRole('row').filter({ hasText: '校正后的模拟公告' })).toBeVisible()
    expect(writes).toHaveLength(2)
    expect(writes[1]).toMatchObject({ method: action === '新增' ? 'POST' : 'PUT', data: { noticeTitle: '校正后的模拟公告', noticeType: '2', status: '1', noticeContent: '<p>编辑后应保留的模拟正文</p>' } })
    expect(writes[1].data).toEqual({ ...writes[0].data, noticeTitle: '校正后的模拟公告' })
    expect(errors).toEqual([])
    expect(warnings).toEqual([])
  })
}

test('公告详情失败不打开空表单并能重试，不产生未处理异常', async ({ page }) => {
  const errors = []; page.on('pageerror', error => errors.push(error.message))
  await authenticated(page)
  await noticeDictionaries(page)
  let failed = true
  await page.route('**/dev-api/system/notice/list?**', route => route.fulfill({ json: { code: 200, rows: [initialNotice], total: 1 } }))
  await page.route('**/dev-api/system/notice/1', route => route.fulfill({ json: failed
    ? { code: 500, msg: '公告详情暂不可用' } : { code: 200, data: initialNotice } }))
  const loaded = page.waitForResponse(response => response.url().includes('/dev-api/system/notice/list'))
  await page.goto('/system/notice', { waitUntil: 'domcontentloaded' })
  await loaded
  const edit = page.getByRole('row').filter({ hasText: '模拟公告' }).getByRole('button', { name: '修改', exact: true })
  await edit.click()
  await expect(page.getByText('公告详情暂不可用', { exact: true })).toBeVisible()
  await expect(page.getByRole('dialog')).toHaveCount(0)
  failed = false
  await edit.click()
  const dialog = page.getByRole('dialog', { name: '修改公告' })
  await expect(dialog.locator('.ql-editor')).toHaveText('旧内容')
  await dialog.getByRole('button', { name: '取 消', exact: true }).click()
  expect(errors).toEqual([])
})
