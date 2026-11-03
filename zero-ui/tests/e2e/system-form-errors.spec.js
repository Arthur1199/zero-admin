import { test, expect } from '@playwright/test'
import { authenticated } from './mock-api'

const forms = [
  { label: '部门', path: '/system/dept', id: 'deptId', name: 'deptName', initial: { deptId: 100, parentId: 0, deptName: '模拟部门', orderNum: 1, status: '0' }, tree: true,
    async fill(dialog) { await dialog.getByPlaceholder('请输入部门名称').fill('失败后保留的模拟部门'); await dialog.getByRole('spinbutton').fill('3') } },
  { label: '字典类型', path: '/system/dict', api: '/system/dict/type', id: 'dictId', name: 'dictName', initial: { dictId: 100, dictName: '模拟字典类型', dictType: 'mock_dictionary', status: '0' },
    async fill(dialog) { await dialog.getByPlaceholder('请输入字典名称').fill('失败后保留的模拟字典类型'); await dialog.getByPlaceholder('请输入字典类型').fill('mock_error_dictionary') } },
  { label: '菜单', path: '/system/menu', id: 'menuId', name: 'menuName', initial: { menuId: 100, parentId: 0, menuName: '模拟菜单', menuType: 'M', path: 'mock-menu', orderNum: 1, isFrame: '1', isCache: '0', visible: '0', status: '0', icon: 'system' }, tree: true,
    async fill(dialog) { await dialog.getByPlaceholder('请输入菜单名称').fill('失败后保留的模拟菜单'); await dialog.getByPlaceholder('请输入路由地址').fill('mock-error-menu'); await dialog.getByRole('spinbutton').fill('3') } },
  { label: 'API谷歌验证', path: '/system/apiAuth', id: 'id', name: 'apiUrl', initial: { id: 100, apiUrl: '/mock/initial', apiMethod: 'ALL', isNeedGoogle: 0, apiStatus: 1 },
    async fill(dialog) { await dialog.getByPlaceholder('请输入API请求路径').fill('/mock/error-retained'); await dialog.locator('.el-select__wrapper').click(); await dialog.page().getByRole('option', { name: 'POST', exact: true }).click() } }
]

for (const form of forms) {
  for (const action of ['新增', '修改', '详情']) {
    test(form.label + action + '接口拒绝能够提示、保留状态并重试，且无未处理异常', async ({ page }) => {
      const errors = []; page.on('pageerror', error => errors.push(error.message))
      const propWarnings = []; page.on('console', message => { if (message.type() === 'warning' && message.text().includes('Invalid prop')) propWarnings.push(message.text()) })
      await authenticated(page)
      const api = form.api || form.path, records = [{ ...form.initial }], writes = []
      let failed = true
      const failure = form.label + '接口暂不可用'
      await page.route('**/dev-api' + api + '/list**', route => route.fulfill({ json: form.tree
        ? { code: 200, data: records } : { code: 200, rows: records, total: records.length } }))
      if (form.label === '部门') await page.route('**/dev-api/system/dept/list/exclude/100', route => route.fulfill({ json: { code: 200, data: [] } }))
      await page.route('**/dev-api' + api + '/100', route => route.fulfill({ json: action === '详情' && failed
        ? { code: 500, msg: failure } : { code: 200, data: records[0] } }))
      await page.route('**/dev-api' + api, route => {
        const request = route.request(), data = request.postDataJSON()
        writes.push({ method: request.method(), data })
        if (failed) return route.fulfill({ json: { code: 500, msg: failure } })
        if (action === '新增') records.push({ ...data, [form.id]: 101 })
        else records[0] = { ...data }
        return route.fulfill({ json: { code: 200, msg: '操作成功' } })
      })
      const loaded = page.waitForResponse(response => response.url().includes('/dev-api' + api + '/list'))
      await page.goto(form.path, { waitUntil: 'domcontentloaded' })
      await loaded
      const row = page.getByRole('row').filter({ hasText: form.initial[form.name] })
      await expect(row).toBeVisible()
      // 部门在父节点下新增，沿用真实按钮传入的 parentId，不直接修改组件状态。
      if (action === '新增') await (form.label === '部门' ? row : page).getByRole('button', { name: '新增', exact: true }).first().click()
      else await row.getByRole('button', { name: '修改', exact: true }).click()
      const dialog = page.getByRole('dialog', { name: (action === '新增' ? '添加' : '修改') + form.label, exact: true })
      if (action === '详情') {
        await expect(page.getByText(failure, { exact: true })).toBeVisible()
        await expect(dialog).toHaveCount(0)
        expect(errors).toEqual([])
        failed = false
        await row.getByRole('button', { name: '修改', exact: true }).click()
        await expect(dialog).toBeVisible()
        const placeholder = { '部门': '请输入部门名称', '字典类型': '请输入字典名称', '菜单': '请输入菜单名称', 'API谷歌验证': '请输入API请求路径' }[form.label]
        await expect(dialog.getByPlaceholder(placeholder)).toHaveValue(form.initial[form.name])
        await dialog.getByRole('button', { name: '取 消', exact: true }).click()
      } else {
        await form.fill(dialog)
        await dialog.getByRole('button', { name: '确 定', exact: true }).click()
        await expect(page.getByText(failure, { exact: true })).toBeVisible()
        await expect(page.getByText(failure, { exact: true })).toHaveCount(1)
        await expect(dialog).toBeVisible()
        expect(records).toEqual([form.initial])
        expect(writes).toHaveLength(1)
        expect(errors).toEqual([])
        failed = false
        // 实际防重复提交的一秒窗口保持不变，重试沿用失败前的输入。
        await page.waitForTimeout(1100)
        await dialog.getByRole('button', { name: '确 定', exact: true }).click()
        await expect(dialog).toBeHidden()
        await expect(page.getByRole('row').filter({ hasText: writes[0].data[form.name] })).toBeVisible()
        expect(writes).toHaveLength(2)
        expect(writes[0]).toEqual(writes[1])
        expect(writes[0].method).toBe(action === '新增' ? 'POST' : 'PUT')
        if (form.label === '部门') expect(writes[0].data).toMatchObject({ parentId: action === '新增' ? 100 : 0, orderNum: 3 })
        if (form.label === 'API谷歌验证') expect(writes[0].data).toMatchObject({ apiMethod: 'POST', isNeedGoogle: 0, apiStatus: 1 })
      }
      expect(errors).toEqual([])
      expect(propWarnings).toEqual([])
    })
  }
}

for (const form of forms.filter(form => form.tree)) {
  for (const action of ['新增', '修改']) {
    test(form.label + action + '上级树加载失败不打开残缺表单，重试后回显正确', async ({ page }) => {
      const errors = []; page.on('pageerror', error => errors.push(error.message))
      await authenticated(page)
      let rejectTree = false
      let openedPage = false, treeRequests = 0
      const failure = form.label + '上级树暂不可用'
      const treeResponse = async (route, data) => {
        if (openedPage) treeRequests++
        await new Promise(resolve => setTimeout(resolve, 200))
        return route.fulfill({ json: rejectTree ? { code: 500, msg: failure } : { code: 200, data } })
      }
      await page.route('**/dev-api' + form.path + '/list**', route => treeResponse(route, [{ ...form.initial }]))
      if (form.label === '部门') await page.route('**/dev-api/system/dept/list/exclude/100', route => treeResponse(route, []))
      await page.route('**/dev-api' + form.path + '/100', route => route.fulfill({ json: { code: 200, data: form.initial } }))
      await page.goto(form.path, { waitUntil: 'domcontentloaded' })
      const row = page.getByRole('row').filter({ hasText: form.initial[form.name] })
      await expect(row).toBeVisible()
      openedPage = true
      rejectTree = true
      const button = action === '新增' ? (form.label === '部门' ? row : page).getByRole('button', { name: '新增', exact: true }).first()
        : row.getByRole('button', { name: '修改', exact: true })
      await button.click()
      await expect(page.getByText(failure, { exact: true })).toBeVisible()
      await expect(page.getByRole('dialog')).toHaveCount(0)
      expect(errors).toEqual([])
      rejectTree = false
      await button.dblclick()
      const dialog = page.getByRole('dialog', { name: (action === '新增' ? '添加' : '修改') + form.label, exact: true })
      await expect(dialog).toBeVisible()
      const placeholder = form.label === '部门' ? '请输入部门名称' : '请输入菜单名称'
      await expect(dialog.getByPlaceholder(placeholder)).toHaveValue(action === '新增' ? '' : form.initial[form.name])
      if (action === '新增' || form.label === '菜单') await expect(dialog.locator('.zero-tree-select .el-select__placeholder')).toContainText(form.label === '部门' ? '模拟部门' : '主类目')
      expect(treeRequests).toBe(2)
      await dialog.getByRole('button', { name: '取 消', exact: true }).click()
      expect(errors).toEqual([])
    })
  }
}
