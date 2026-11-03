import { test, expect } from '@playwright/test'
import { authenticated } from './mock-api'

const initialPost = { postId: 1, postName: '模拟岗位', postCode: 'mock-post', postSort: 2, status: '0' }

for (const action of ['新增', '修改']) {
  test('岗位' + action + '失败保留表单，提示一次且重试请求字段不变', async ({ page }) => {
    const errors = []; page.on('pageerror', error => errors.push(error.message))
    await authenticated(page)
    let failed = true
    const records = [{ ...initialPost }], writes = []
    await page.route('**/dev-api/system/post/list?**', route => route.fulfill({ json: { code: 200, rows: records, total: records.length } }))
    await page.route('**/dev-api/system/post/1', route => route.fulfill({ json: { code: 200, data: records[0] } }))
    await page.route('**/dev-api/system/post', route => {
      const request = route.request(), data = request.postDataJSON()
      writes.push({ method: request.method(), data })
      if (failed) return route.fulfill({ json: { code: 500, msg: '岗位名称已存在' } })
      if (action === '新增') records.push({ ...data, postId: 2 })
      else records[0] = { ...data }
      return route.fulfill({ json: { code: 200, msg: '操作成功' } })
    })
    const loaded = page.waitForResponse(response => response.url().includes('/dev-api/system/post/list'))
    await page.goto('/system/post', { waitUntil: 'domcontentloaded' })
    await loaded
    await page.getByRole('button', { name: '新增', exact: true }).waitFor({ state: 'visible' })
    if (action === '新增') await page.getByRole('button', { name: '新增', exact: true }).click()
    else await page.getByRole('row').filter({ hasText: 'mock-post' }).getByRole('button', { name: '修改', exact: true }).click()
    const dialog = page.getByRole('dialog', { name: action === '新增' ? '添加岗位' : '修改岗位' })
    await dialog.getByPlaceholder('请输入岗位名称').fill('失败后保留的模拟岗位')
    await dialog.getByPlaceholder('请输入编码名称').fill('mock-error-post')
    await dialog.getByRole('spinbutton').fill('3')
    await dialog.getByText('停用', { exact: true }).click()
    await expect(dialog.getByRole('radio', { name: '停用', exact: true })).toBeChecked()
    await dialog.getByRole('button', { name: '确 定', exact: true }).click()
    const message = page.getByText('岗位名称已存在', { exact: true })
    await expect(message).toBeVisible()
    await expect(message).toHaveCount(1)
    await expect(dialog).toBeVisible()
    await expect(dialog.getByPlaceholder('请输入岗位名称')).toHaveValue('失败后保留的模拟岗位')
    await expect(dialog.getByRole('spinbutton')).toHaveValue('3')
    expect(records).toEqual([initialPost])
    expect(errors).toEqual([])
    failed = false
    // 保留全局防重复窗口，失败后的用户重试遵守原有节流规则。
    await page.waitForTimeout(1100)
    await dialog.getByRole('button', { name: '确 定', exact: true }).click()
    await expect(dialog).toBeHidden()
    await expect(page.getByRole('row').filter({ hasText: 'mock-error-post' })).toBeVisible()
    expect(writes).toHaveLength(2)
    expect(writes[0]).toEqual(writes[1])
    expect(writes[0]).toMatchObject({ method: action === '新增' ? 'POST' : 'PUT', data: { postName: '失败后保留的模拟岗位', postCode: 'mock-error-post', postSort: 3, status: '1' } })
    expect(errors).toEqual([])
  })
}

test('岗位详情失败不打开空表单，不产生未处理异常且能够重试', async ({ page }) => {
  const errors = []; page.on('pageerror', error => errors.push(error.message))
  await authenticated(page)
  let failed = true
  await page.route('**/dev-api/system/post/list?**', route => route.fulfill({ json: { code: 200, rows: [initialPost], total: 1 } }))
  await page.route('**/dev-api/system/post/1', route => route.fulfill({ json: failed
    ? { code: 500, msg: '岗位详情暂不可用' } : { code: 200, data: initialPost } }))
  const loaded = page.waitForResponse(response => response.url().includes('/dev-api/system/post/list'))
  await page.goto('/system/post', { waitUntil: 'domcontentloaded' })
  await loaded
  const edit = page.getByRole('row').filter({ hasText: 'mock-post' }).getByRole('button', { name: '修改', exact: true })
  await edit.click()
  await expect(page.getByText('岗位详情暂不可用', { exact: true })).toBeVisible()
  await expect(page.getByRole('dialog')).toHaveCount(0)
  expect(errors).toEqual([])
  failed = false
  await edit.click()
  const dialog = page.getByRole('dialog', { name: '修改岗位' })
  await expect(dialog.getByPlaceholder('请输入岗位名称')).toHaveValue('模拟岗位')
  await dialog.getByRole('button', { name: '取 消', exact: true }).click()
  expect(errors).toEqual([])
})
