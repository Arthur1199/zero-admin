import { test, expect } from '@playwright/test'
import { authenticated } from './mock-api'

const role = { roleId: 2, roleName: '测试角色', roleKey: 'mock-role', roleSort: 1, status: '0', dataScope: '2', menuCheckStrictly: true, deptCheckStrictly: true }
const nodes = [{ id: 100, label: '系统管理', children: [{ id: 101, label: '用户管理' }, { id: 102, label: '角色管理' }] }]

test('八主题角色菜单与数据权限树铺满下一行，展开全选和提交字段保留', async ({ page }) => {
  test.setTimeout(120000)
  const errors = []
  page.on('pageerror', error => errors.push(error.message))
  const requests = await authenticated(page)
  await page.route('**/dev-api/system/role/list?**', route => route.fulfill({ json: { code: 200, rows: [role], total: 1 } }))
  await page.route('**/dev-api/system/role/2', route => route.fulfill({ json: { code: 200, data: role } }))
  await page.route('**/dev-api/system/menu/roleMenuTreeselect/2', route => route.fulfill({ json: { code: 200, menus: nodes, checkedKeys: [101] } }))
  await page.route('**/dev-api/system/role/deptTree/2', route => route.fulfill({ json: { code: 200, depts: nodes, checkedKeys: [101] } }))
  await page.goto('/system/role')
  const dialog = page.locator('.el-dialog:visible')
  const assertLayout = async label => {
    const group = dialog.locator('.permission-tree-item:visible')
    await expect(group.getByRole('tree')).toBeVisible()
    const geometry = await group.evaluate(element => {
      const content = element.querySelector('.el-form-item__content').getBoundingClientRect()
      const tree = element.querySelector('.el-tree').getBoundingClientRect()
      const options = [...element.querySelectorAll('.el-form-item__content > .el-checkbox')].map(node => node.getBoundingClientRect())
      return { widthGap: Math.abs(tree.width - content.width), leftGap: Math.abs(tree.left - content.left), below: tree.top >= Math.max(...options.map(rect => rect.bottom)) }
    })
    expect(geometry.widthGap, label).toBeLessThanOrEqual(1)
    expect(geometry.leftGap, label).toBeLessThanOrEqual(1)
    expect(geometry.below, label).toBe(true)
  }
  for (const label of ['毛玻璃拟态风', '高端暗黑风', '极简未来风', '原始风格', '轻奢商务风', '活力科技风', '温馨自然风', '质感设计风']) {
    await page.locator('.navbar .setting').click()
    await page.locator('.drawer-select').first().click()
    await page.getByRole('option', { name: label, exact: true }).click()
    await page.getByRole('button', { name: '关闭设置', exact: true }).click()
    await expect(page.locator('.el-drawer')).toBeHidden()
    await expect(page.locator('.app-main .fade-transform-leave-active, .app-main .fade-transform-enter-active')).toHaveCount(0)
    await page.locator('.el-table__row').getByRole('button', { name: '修改', exact: true }).click()
    await assertLayout(label + '菜单')
    const number = dialog.getByRole('spinbutton')
    await number.fill('2')
    await number.press('Tab')
    await expect.poll(() => number.evaluate(element => {
      const style = getComputedStyle(element)
      return { background: style.backgroundColor, border: style.borderTopWidth, shadow: style.boxShadow }
    }), { message: label + '数字输入框' }).toEqual({ background: 'rgba(0, 0, 0, 0)', border: '0px', shadow: 'none' })
    await dialog.getByRole('button', { name: '增加数值', exact: true }).click()
    await expect(number).toHaveValue('3')
    await dialog.getByRole('button', { name: '减少数值', exact: true }).click()
    await expect(number).toHaveValue('2')
    await dialog.getByText('展开/折叠', { exact: true }).click()
    await expect(dialog.getByRole('treeitem', { name: '用户管理', exact: true })).toBeVisible()
    await dialog.getByText('全选/全不选', { exact: true }).click()
    for (const checkbox of await dialog.getByRole('tree').getByRole('checkbox').all()) await expect(checkbox).toBeChecked()
    await dialog.getByRole('button', { name: '取 消', exact: true }).click()
    await expect(dialog).toHaveCount(0)
    await page.getByRole('button', { name: '更多', exact: true }).click()
    await page.getByRole('menuitem', { name: '数据权限', exact: true }).click()
    await assertLayout(label + '部门')
    for (const input of await dialog.locator('.el-input.is-disabled').all()) {
      const decoration = await input.evaluate(root => {
        const wrapper = root.querySelector('.el-input__wrapper')
        const probe = document.createElement('div')
        probe.style.backgroundColor = 'var(--el-disabled-bg-color)'
        root.appendChild(probe)
        const expected = getComputedStyle(probe).backgroundColor
        probe.remove()
        return { actual: getComputedStyle(wrapper).backgroundColor, expected }
      })
      expect(decoration.actual).toBe(decoration.expected)
      await expect(input.getByRole('textbox')).toBeDisabled()
    }
    await dialog.getByRole('button', { name: '取 消', exact: true }).click()
    await expect(dialog).toHaveCount(0)
  }
  // 模拟提交校验字段，真实服务只打开并取消弹窗，不修改权限。
  await page.locator('.el-table__row').getByRole('button', { name: '修改', exact: true }).click()
  await dialog.getByText('全选/全不选', { exact: true }).click()
  await dialog.getByRole('button', { name: '确 定', exact: true }).click()
  await expect(dialog).toHaveCount(0)
  const submission = requests.find(request => request.method === 'PUT' && request.path === '/system/role')
  expect(JSON.parse(submission.body).menuIds.sort()).toEqual([100, 101, 102])
  expect(errors).toEqual([])
})
