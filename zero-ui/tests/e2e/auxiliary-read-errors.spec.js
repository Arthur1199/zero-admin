import { test, expect } from '@playwright/test'
import { authenticated } from './mock-api'

const job = { jobId: 2, jobName: '验收任务', jobGroup: 'DEFAULT', invokeTarget: 'ryTask.ryNoParams', cronExpression: '0 0 * * * ?', status: '1', concurrent: '1', misfirePolicy: '1' }
const role = { roleId: 2, roleName: '验收角色', roleKey: 'acceptance', roleSort: 1, status: '0', dataScope: '2', menuCheckStrictly: true, deptCheckStrictly: true }
const nodes = [{ id: 100, label: '测试目录', children: [{ id: 101, label: '已选节点' }] }]

function observeErrors(page) {
  const errors = []
  page.on('pageerror', error => errors.push(error.message))
  return errors
}

async function roleFixtures(page) {
  const requests = await authenticated(page)
  await page.route('**/dev-api/system/role/list?**', route => route.fulfill({ json: { code: 200, rows: [role], total: 1 } }))
  await page.route('**/dev-api/system/role/2', route => route.fulfill({ json: { code: 200, data: role } }))
  await page.route('**/dev-api/system/menu/treeselect', route => route.fulfill({ json: { code: 200, data: nodes } }))
  await page.route('**/dev-api/system/menu/roleMenuTreeselect/2', route => route.fulfill({ json: { code: 200, menus: nodes, checkedKeys: [101] } }))
  await page.route('**/dev-api/system/role/deptTree/2', route => route.fulfill({ json: { code: 200, depts: nodes, checkedKeys: [101] } }))
  return requests
}

async function openRole(page, mode) {
  if (mode === '新增') await page.getByRole('button', { name: '新增', exact: true }).click()
  else if (mode === '修改') await page.locator('.el-table__row').getByRole('button', { name: '修改', exact: true }).click()
  else {
    await page.getByRole('button', { name: '更多', exact: true }).click()
    await page.getByRole('menuitem', { name: '数据权限', exact: true }).click()
  }
}

for (const [mode, endpoint] of [
  ['新增', '/system/menu/treeselect'],
  ['修改', '/system/menu/roleMenuTreeselect/2'], ['修改', '/system/role/2'], ['修改', '/system/role/deptTree/2'],
  ['数据权限', '/system/role/deptTree/2'], ['数据权限', '/system/role/2']
]) {
  test('角色' + mode + '依赖失败不打开残缺表单，重试保留选中节点 ' + endpoint, async ({ page }) => {
    const errors = observeErrors(page), requests = await roleFixtures(page)
    let failed = true
    await page.route('**/dev-api' + endpoint, route => failed
      ? route.fulfill({ json: { code: 500, msg: '角色依赖读取失败' } }) : route.fallback())
    await page.goto('/system/role')
    const name = mode === '新增' ? '添加角色' : mode === '修改' ? '修改角色' : '分配数据权限'
    const dialog = page.getByRole('dialog', { name, exact: true })
    await Promise.all([
      page.getByText('角色依赖读取失败', { exact: true }).waitFor({ state: 'visible' }), openRole(page, mode)
    ])
    await expect(dialog).toBeHidden()
    failed = false
    await openRole(page, mode)
    await expect(dialog).toBeVisible()
    await expect(dialog.getByRole('tree').first()).toBeVisible()
    if (mode !== '新增') {
      const leaf = dialog.getByRole('tree').first().getByRole('treeitem', { name: '已选节点', exact: true })
      if (!await leaf.isVisible()) await dialog.getByText('展开/折叠', { exact: true }).first().click()
      await expect(leaf.getByRole('checkbox')).toBeChecked()
    }
    await dialog.getByRole('button', { name: '取 消', exact: true }).click()
    expect(errors).toEqual([])
    expect(requests.filter(request => request.method !== 'GET')).toEqual([])
  })
}

for (const operation of ['详情', '修改']) {
  test('任务' + operation + '读取失败后可重试且无未处理异常', async ({ page }) => {
    const errors = observeErrors(page), requests = await authenticated(page)
    let failed = true
    await page.route('**/dev-api/monitor/job/list?**', route => route.fulfill({ json: { code: 200, rows: [job], total: 1 } }))
    await page.route('**/dev-api/monitor/job/2', route => route.fulfill({ json: failed
      ? { code: 500, msg: '任务读取失败' } : { code: 200, data: job } }))
    await page.goto('/monitor/job')
    const open = async () => {
      if (operation === '修改') await page.locator('.el-table__row').getByRole('button', { name: '修改', exact: true }).click()
      else {
        await page.getByRole('button', { name: '更多', exact: true }).click()
        await page.getByRole('menuitem', { name: '任务详细', exact: true }).click()
      }
    }
    const dialog = page.getByRole('dialog', { name: operation === '修改' ? '修改任务' : '任务详细', exact: true })
    await Promise.all([page.getByText('任务读取失败', { exact: true }).waitFor({ state: 'visible' }), open()])
    await expect(dialog).toBeHidden()
    failed = false
    await open()
    await expect(dialog).toBeVisible()
    if (operation === '修改') await expect(dialog.getByPlaceholder('请输入任务名称')).toHaveValue(job.jobName)
    else await expect(dialog).toContainText(job.jobName)
    await dialog.getByRole('button', { name: operation === '修改' ? '取 消' : '关 闭', exact: true }).click()
    expect(errors).toEqual([])
    expect(requests.filter(request => request.method !== 'GET')).toEqual([])
  })
}

for (const [label, path, endpoint, payload, listEndpoint, key, value] of [
  ['字典类型', '/system/dict-data/index/1', '/system/dict/type/1', { dictId: 1, dictName: '测试字典', dictType: 'test_dictionary' }, '/system/dict/data/list', 'dictType', 'test_dictionary'],
  ['日志任务', '/monitor/job-log/index/2', '/monitor/job/2', job, '/monitor/jobLog/list', 'jobName', job.jobName]
]) {
  test(label + '初始化失败释放加载层，搜索重试恢复页面上下文', async ({ page }) => {
    const errors = observeErrors(page), requests = await authenticated(page)
    let failed = true, reads = 0
    await page.route('**/dev-api' + endpoint, route => {
      reads++
      return route.fulfill({ json: failed ? { code: 500, msg: '页面初始化失败' } : { code: 200, data: payload } })
    })
    await Promise.all([
      page.getByText('页面初始化失败', { exact: true }).waitFor({ state: 'visible' }), page.goto(path, { waitUntil: 'domcontentloaded' })
    ])
    await expect(page.locator('.app-main .el-loading-mask:visible')).toHaveCount(0)
    expect(requests.filter(request => request.path === listEndpoint)).toHaveLength(0)
    failed = false
    const listResponse = page.waitForResponse(response => new URL(response.url()).pathname === '/dev-api' + listEndpoint)
    await page.getByRole('button', { name: '搜索', exact: true }).click()
    const response = await listResponse
    expect(new URL(response.url()).searchParams.get(key)).toBe(value)
    expect(reads).toBe(2)
    await expect(page.locator('.app-main .el-loading-mask:visible')).toHaveCount(0)
    expect(errors).toEqual([])
  })
}

async function cacheFixtures(page, names = [{ cacheName: 'mock:', remark: '公开测试缓存' }]) {
  await authenticated(page)
  await page.route('**/dev-api/monitor/cache/getNames', route => route.fulfill({ json: { code: 200, data: names } }))
  await page.route('**/dev-api/monitor/cache/getKeys/mock:', route => route.fulfill({ json: { code: 200, data: ['mock:first', 'mock:second'] } }))
  await page.goto('/monitor/cache/list')
  await page.locator('.el-card').first().locator('.el-table__row').getByText('mock', { exact: true }).click()
  await expect(page.locator('.el-card').nth(1).locator('.el-table__row').first()).toBeVisible()
  return { keys: page.locator('.el-card').nth(1), detail: page.locator('.el-card').nth(2) }
}

test('缓存详情三个字段保持只读', async ({ page }) => {
  const state = await cacheFixtures(page)
  await page.route('**/dev-api/monitor/cache/getValue/mock:/mock:first', route => route.fulfill({ json: { code: 200, data: { cacheName: 'mock', cacheKey: 'first', cacheValue: '公开测试值' } } }))
  await state.keys.locator('.el-table__body .cell').filter({ hasText: /^first$/ }).click()
  await expect(state.detail.locator('textarea')).toHaveValue('公开测试值')
  for (const input of await state.detail.locator('input,textarea').all()) await expect(input).toHaveJSProperty('readOnly', true)
})

test('缓存内容失败后清除旧详情，仍可重试', async ({ page }) => {
  const errors = observeErrors(page), state = await cacheFixtures(page)
  let failed = false
  await page.route('**/dev-api/monitor/cache/getValue/mock:/mock:first', route => route.fulfill({ json: failed
    ? { code: 500, msg: '缓存内容读取失败' } : { code: 200, data: { cacheName: 'mock', cacheKey: 'first', cacheValue: '公开测试值' } } }))
  const select = () => state.keys.locator('.el-table__body .cell').filter({ hasText: /^first$/ }).click()
  await select()
  await expect(state.detail.locator('textarea')).toHaveValue('公开测试值')
  failed = true
  await Promise.all([page.getByText('缓存内容读取失败', { exact: true }).waitFor({ state: 'visible' }), select()])
  await expect(state.detail.locator('textarea')).toHaveValue('')
  failed = false
  await select()
  await expect(state.detail.locator('textarea')).toHaveValue('公开测试值')
  expect(errors).toEqual([])
})

test('缓存连续选键时较早返回的响应不覆盖当前详情', async ({ page }) => {
  const errors = observeErrors(page), state = await cacheFixtures(page)
  let releaseFirst
  const firstGate = new Promise(resolve => { releaseFirst = resolve })
  let firstStarted
  const firstRequest = new Promise(resolve => { firstStarted = resolve })
  await page.route('**/dev-api/monitor/cache/getValue/mock:/mock:first', async route => {
    firstStarted()
    await firstGate
    await route.fulfill({ json: { code: 200, data: { cacheName: 'mock', cacheKey: 'first', cacheValue: '较早选择' } } })
  })
  await page.route('**/dev-api/monitor/cache/getValue/mock:/mock:second', route => route.fulfill({ json: { code: 200, data: { cacheName: 'mock', cacheKey: 'second', cacheValue: '当前选择' } } }))
  await state.keys.locator('.el-table__body .cell').filter({ hasText: /^first$/ }).click()
  await firstRequest
  await state.keys.locator('.el-table__body .cell').filter({ hasText: /^second$/ }).click()
  await expect(state.detail.locator('textarea')).toHaveValue('当前选择')
  const lateResponse = page.waitForResponse(response => response.url().endsWith('/getValue/mock:/mock:first'))
  releaseFirst()
  await lateResponse
  // 等待所有请求结束，让较晚完成的响应有机会更新界面。
  await page.waitForLoadState('networkidle')
  await expect(state.detail.locator('textarea')).toHaveValue('当前选择')
  expect(errors).toEqual([])
})

for (const mode of ['新增', '修改', '数据权限']) {
  test('角色' + mode + '保存拒绝保留表单和权限字段', async ({ page }) => {
    const errors = observeErrors(page), writes = []
    await roleFixtures(page)
    const endpoint = mode === '数据权限' ? '/system/role/dataScope' : '/system/role'
    await page.route('**/dev-api' + endpoint, route => {
      writes.push({ method: route.request().method(), data: JSON.parse(route.request().postData()) })
      return route.fulfill({ json: { code: 500, msg: '角色保存被拒绝' } })
    })
    await page.goto('/system/role')
    await openRole(page, mode)
    const dialog = page.getByRole('dialog', { name: mode === '新增' ? '添加角色' : mode === '修改' ? '修改角色' : '分配数据权限', exact: true })
    await expect(dialog.getByRole('tree').first()).toBeVisible()
    if (mode !== '数据权限') {
      await dialog.getByPlaceholder('请输入角色名称').fill('保留输入的角色')
      await dialog.getByPlaceholder('请输入角色英文名称').fill('kept_role')
    }
    await Promise.all([page.getByText('角色保存被拒绝', { exact: true }).waitFor({ state: 'visible' }), dialog.getByRole('button', { name: '确 定', exact: true }).click()])
    await expect(dialog).toBeVisible()
    expect(writes).toHaveLength(1)
    expect(writes[0].method).toBe(mode === '新增' ? 'POST' : 'PUT')
    if (mode !== '新增') expect(writes[0].data[mode === '数据权限' ? 'deptIds' : 'menuIds']).toContain(101)
    if (mode !== '数据权限') await expect(dialog.getByPlaceholder('请输入角色名称')).toHaveValue('保留输入的角色')
    await dialog.getByRole('button', { name: '取 消', exact: true }).click()
    expect(errors).toEqual([])
  })
}

for (const mode of ['新增', '修改']) {
  test('任务' + mode + '保存拒绝保留输入和原接口字段', async ({ page }) => {
    const errors = observeErrors(page), writes = []
    await authenticated(page)
    await page.route('**/dev-api/monitor/job/list?**', route => route.fulfill({ json: { code: 200, rows: [job], total: 1 } }))
    await page.route('**/dev-api/monitor/job/2', route => route.fulfill({ json: { code: 200, data: job } }))
    await page.route('**/dev-api/monitor/job', route => {
      writes.push({ method: route.request().method(), data: JSON.parse(route.request().postData()) })
      return route.fulfill({ json: { code: 500, msg: '任务保存被拒绝' } })
    })
    await page.goto('/monitor/job')
    if (mode === '新增') await page.getByRole('button', { name: '新增', exact: true }).click()
    else await page.locator('.el-table__row').getByRole('button', { name: '修改', exact: true }).click()
    const dialog = page.getByRole('dialog', { name: mode === '新增' ? '添加任务' : '修改任务', exact: true })
    await dialog.getByPlaceholder('请输入任务名称').fill('保留输入的任务')
    await dialog.getByPlaceholder('请输入调用目标字符串').fill(job.invokeTarget)
    await dialog.getByPlaceholder('请输入cron执行表达式').fill(job.cronExpression)
    await Promise.all([page.getByText('任务保存被拒绝', { exact: true }).waitFor({ state: 'visible' }), dialog.getByRole('button', { name: '确 定', exact: true }).click()])
    await expect(dialog.getByPlaceholder('请输入任务名称')).toHaveValue('保留输入的任务')
    expect(writes).toHaveLength(1)
    expect(writes[0].method).toBe(mode === '新增' ? 'POST' : 'PUT')
    expect(writes[0].data).toMatchObject({ invokeTarget: job.invokeTarget, cronExpression: job.cronExpression })
    await dialog.getByRole('button', { name: '取 消', exact: true }).click()
    expect(errors).toEqual([])
  })

  test('字典数据' + mode + '保存拒绝保留输入和字典类型', async ({ page }) => {
    const errors = observeErrors(page), writes = []
    const data = { dictCode: 2, dictLabel: '测试标签', dictValue: '1', dictType: 'test_dictionary', dictSort: 1, status: '0', listClass: 'default' }
    await authenticated(page)
    await page.route('**/dev-api/system/dict/data/list?**', route => route.fulfill({ json: { code: 200, rows: [data], total: 1 } }))
    await page.route('**/dev-api/system/dict/data/2', route => route.fulfill({ json: { code: 200, data } }))
    await page.route('**/dev-api/system/dict/data', route => {
      writes.push({ method: route.request().method(), data: JSON.parse(route.request().postData()) })
      return route.fulfill({ json: { code: 500, msg: '字典数据保存被拒绝' } })
    })
    await page.goto('/system/dict-data/index/1')
    await expect(page.locator('.app-main .el-loading-mask:visible')).toHaveCount(0)
    if (mode === '新增') await page.getByRole('button', { name: '新增', exact: true }).click()
    else await page.locator('.el-table__row').getByRole('button', { name: '修改', exact: true }).click()
    const dialog = page.getByRole('dialog', { name: mode === '新增' ? '添加字典数据' : '修改字典数据', exact: true })
    await dialog.getByPlaceholder('请输入数据标签').fill('保留输入的标签')
    await dialog.getByPlaceholder('请输入数据键值').fill('kept_value')
    await Promise.all([page.getByText('字典数据保存被拒绝', { exact: true }).waitFor({ state: 'visible' }), dialog.getByRole('button', { name: '确 定', exact: true }).click()])
    await expect(dialog.getByPlaceholder('请输入数据标签')).toHaveValue('保留输入的标签')
    expect(writes).toHaveLength(1)
    expect(writes[0].method).toBe(mode === '新增' ? 'POST' : 'PUT')
    expect(writes[0].data.dictType).toBe('test_dictionary')
    await dialog.getByRole('button', { name: '取 消', exact: true }).click()
    expect(errors).toEqual([])
  })
}

for (const [label, endpoint, card] of [
  ['名称', '/monitor/cache/clearCacheName/mock:', 0],
  ['键名', '/monitor/cache/clearCacheKey/mock:first', 1],
  ['全部', '/monitor/cache/clearCacheAll', 2]
]) {
  test('缓存' + label + '清理拒绝由页面收尾，无未处理异常', async ({ page }) => {
    const errors = observeErrors(page), writes = []
    await cacheFixtures(page)
    await page.route('**/dev-api' + endpoint, route => {
      writes.push(route.request().method())
      return route.fulfill({ json: { code: 500, msg: '模拟缓存清理被拒绝' } })
    })
    const panel = page.locator('.el-card').nth(card)
    const action = card === 2 ? panel.getByRole('button', { name: '清理全部', exact: true }) : panel.locator('.el-table__row').first().getByRole('button')
    await Promise.all([page.getByText('模拟缓存清理被拒绝', { exact: true }).waitFor({ state: 'visible' }), action.click()])
    expect(writes).toEqual(['DELETE'])
    expect(errors).toEqual([])
  })
}

for (const [label, endpoint, panel, successText] of [
  ['名称', '/monitor/cache/getNames', 0, '刷新缓存列表成功'],
  ['键名', '/monitor/cache/getKeys/mock:', 1, '刷新键名列表成功']
]) {
  test('缓存' + label + '刷新失败不提前提示成功', async ({ page }) => {
    const errors = observeErrors(page)
    await cacheFixtures(page)
    // 记录实际出现过的成功消息，避免等待提示自动消失后误判为未显示。
    await page.evaluate(() => {
      window.__cacheRefreshMessages = []
      window.__cacheRefreshObserver = new MutationObserver(() => {
        for (const message of document.querySelectorAll('.el-message--success')) window.__cacheRefreshMessages.push(message.textContent.trim())
      })
      window.__cacheRefreshObserver.observe(document.body, { childList: true, subtree: true })
    })
    await page.route('**/dev-api' + endpoint, route => route.fulfill({ json: { code: 500, msg: '模拟刷新失败' } }))
    await Promise.all([
      page.getByText('模拟刷新失败', { exact: true }).waitFor({ state: 'visible' }),
      page.locator('.el-card').nth(panel).locator('.el-card__header').getByRole('button').click()
    ])
    const messages = await page.evaluate(() => {
      window.__cacheRefreshObserver.disconnect()
      return window.__cacheRefreshMessages
    })
    expect(messages).not.toContain(successText)
    await expect(page.locator('.app-main .el-loading-mask:visible')).toHaveCount(0)
    expect(errors).toEqual([])
  })
}

test('切换缓存名称后，旧名称的慢响应不覆盖当前键名列表', async ({ page }) => {
  const state = await cacheFixtures(page, [{ cacheName: 'mock:' }, { cacheName: 'other:' }])
  let releaseOld, startedOld
  const oldGate = new Promise(resolve => { releaseOld = resolve })
  const oldStarted = new Promise(resolve => { startedOld = resolve })
  await page.route('**/dev-api/monitor/cache/getKeys/mock:', async route => {
    startedOld()
    await oldGate
    await route.fulfill({ json: { code: 200, data: ['mock:old'] } })
  })
  await page.route('**/dev-api/monitor/cache/getKeys/other:', route => route.fulfill({ json: { code: 200, data: ['other:current'] } }))
  await state.keys.locator('.el-card__header').getByRole('button').click()
  await oldStarted
  await page.locator('.el-card').first().locator('.el-table__body .cell').filter({ hasText: /^other$/ }).click()
  await expect(state.keys.locator('.el-table__body .cell').filter({ hasText: /^current$/ })).toBeVisible()
  const oldResponse = page.waitForResponse(response => response.url().endsWith('/getKeys/mock:'))
  releaseOld()
  await oldResponse
  await page.waitForLoadState('networkidle')
  await expect(state.keys.locator('.el-table__body .cell').filter({ hasText: /^current$/ })).toBeVisible()
})
