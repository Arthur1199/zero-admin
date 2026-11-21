import { test, expect } from '@playwright/test'
import { authenticated } from './mock-api'
import { WebSocketMessageType } from '../../src/utils/websocketMessageType'

test.beforeEach(async ({ page }) => {
  await page.routeWebSocket('**/ws/admin', socket => {
    socket.onMessage(() => socket.send(JSON.stringify({
      msgType: { messageType: WebSocketMessageType.CREATE_CONNECTION },
      msgData: { heartbeatInterval: 30000, idleTimeout: 60000 }
    })))
  })
})

async function openBuilder(page, expression = '') {
  const errors = [], warnings = []
  page.on('pageerror', error => errors.push(error.message))
  page.on('console', message => { if (message.type() === 'warning' && message.text().includes('Invalid prop')) warnings.push(message.text()) })
  const requests = await authenticated(page)
  await page.goto('/monitor/job', { waitUntil: 'domcontentloaded' })
  await page.getByRole('button', { name: '新增', exact: true }).click()
  const task = page.getByRole('dialog', { name: '添加任务', exact: true })
  if (expression) await task.getByPlaceholder('请输入cron执行表达式').fill(expression)
  await task.getByRole('button', { name: '生成表达式', exact: true }).click()
  const builder = page.getByRole('dialog', { name: 'Cron表达式生成器', exact: true })
  await expect(builder.getByRole('tab', { name: '秒', exact: true })).toBeVisible()
  const result = builder.locator('.popup-result tbody tr td').last()
  return { task, builder, result, errors, warnings, requests }
}

async function assertNoJobWrite(state) {
  expect(state.errors).toEqual([])
  expect(state.warnings).toEqual([])
  expect(state.requests.filter(request => request.path.startsWith('/monitor/job') && request.method !== 'GET')).toEqual([])
}

test('Cron空草稿打开七个页签、预览、编辑间隔、回填与取消，不保存任务', async ({ page }) => {
  const state = await openBuilder(page)
  for (const label of ['秒', '分钟', '小时', '日', '月', '周', '年']) await expect(state.builder.getByRole('tab', { name: label, exact: true })).toBeVisible()
  await expect(state.result).toHaveText('* * * * * ?')
  await expect(state.builder.locator('.popup-result-scroll li')).toHaveCount(5)
  const seconds = state.builder.getByRole('tabpanel', { name: '秒', exact: true })
  await seconds.locator('.el-radio').filter({ hasText: '秒开始，每' }).locator('.el-radio__input').click()
  await seconds.getByRole('spinbutton').nth(3).fill('10')
  await seconds.getByRole('spinbutton').nth(3).press('Tab')
  await expect(state.result).toHaveText('0/10 * * * * ?')
  const times = await state.builder.locator('.popup-result-scroll li').allTextContents()
  expect(times.every(time => Number(time.slice(-2)) % 10 === 0)).toBeTruthy()
  await state.builder.getByRole('button', { name: '确定', exact: true }).click()
  await expect(state.builder).toBeHidden()
  await expect(state.task.getByPlaceholder('请输入cron执行表达式')).toHaveValue('0/10 * * * * ?')
  await state.task.getByRole('button', { name: '生成表达式', exact: true }).click()
  await state.builder.getByRole('button', { name: '重置', exact: true }).click()
  await expect(state.result).toHaveText('* * * * * ?')
  await state.builder.getByRole('button', { name: '取消', exact: true }).click()
  await expect(state.task.getByPlaceholder('请输入cron执行表达式')).toHaveValue('0/10 * * * * ?')
  await state.task.getByRole('button', { name: '取 消', exact: true }).click()
  await assertNoJobWrite(state)
})

for (const [label, expression, tab, expectedValues] of [
  ['周期', '1-5 0/10 * * * ?', '秒', ['1', '5']],
  ['第几周', '0 0 9 ? * 2#1', '周', ['1']],
  ['年份周期', '0 0 9 * * ? ' + (new Date().getFullYear() + 2) + '-' + (new Date().getFullYear() + 3), '年', [String(new Date().getFullYear() + 2), String(new Date().getFullYear() + 3)]]
]) {
  test('Cron已有' + label + '表达式打开不改值，回显与确定回填一致', async ({ page }) => {
    const state = await openBuilder(page, expression)
    await expect(state.result).toHaveText(expression)
    await state.builder.getByRole('tab', { name: tab, exact: true }).click()
    const panel = state.builder.getByRole('tabpanel', { name: tab, exact: true })
    for (let index = 0; index < expectedValues.length; index++) await expect(panel.getByRole('spinbutton').nth(index)).toHaveValue(expectedValues[index])
    if (label === '第几周') await expect(panel.locator('.el-select__placeholder').nth(2)).toContainText('星期一')
    await expect(state.builder.locator('.popup-result-scroll li')).toHaveCount(5)
    const times = await state.builder.locator('.popup-result-scroll li').allTextContents()
    if (label === '第几周') expect(times.every(time => { const date = new Date(time.replace(' ', 'T')); return date.getDay() === 1 && date.getDate() <= 7 && date.getHours() === 9 })).toBeTruthy()
    if (label === '年份周期') expect(times.every(time => time.startsWith(expectedValues[0]) || time.startsWith(expectedValues[1]))).toBeTruthy()
    await state.builder.getByRole('button', { name: '确定', exact: true }).click()
    await expect(state.task.getByPlaceholder('请输入cron执行表达式')).toHaveValue(expression)
    await state.task.getByRole('button', { name: '取 消', exact: true }).click()
    await assertNoJobWrite(state)
  })
}

test('Cron指定值选项正确选中，取消已有值不会生成重复项', async ({ page }) => {
  const state = await openBuilder(page, '1,5 2,7 3,9 4,12 6,8 ?')
  await expect(state.result).toHaveText('1,5 2,7 3,9 4,12 6,8 ?')
  const seconds = state.builder.getByRole('tabpanel', { name: '秒', exact: true })
  await seconds.getByRole('combobox').press('ArrowDown')
  const five = page.getByRole('option', { name: '5', exact: true })
  await expect(five).toHaveAttribute('aria-selected', 'true')
  await five.click()
  await seconds.getByRole('combobox').press('Escape')
  await expect(state.result).toHaveText('1 2,7 3,9 4,12 6,8 ?')
  await state.builder.getByRole('button', { name: '取消', exact: true }).click()
  await expect(state.task.getByPlaceholder('请输入cron执行表达式')).toHaveValue('1,5 2,7 3,9 4,12 6,8 ?')
  await state.task.getByRole('button', { name: '取 消', exact: true }).click()
  await assertNoJobWrite(state)
})

test('Cron八主题预览文本可读、表格完整，关闭草稿不写任务', async ({ page }, testInfo) => {
  test.setTimeout(120000)
  const requests = await authenticated(page), errors = []
  page.on('pageerror', error => errors.push(error.message))
  await page.goto('/monitor/job', { waitUntil: 'domcontentloaded' })
  for (const [theme, label] of [
    ['glassmorphism', '毛玻璃拟态风'], ['dark-premium', '高端暗黑风'],
    ['minimalist-future', '极简未来风'], ['default', '原始风格'],
    ['luxury-business', '轻奢商务风'], ['vibrant-tech', '活力科技风'],
    ['warm-nature', '温馨自然风'], ['material-design', '质感设计风']
  ]) {
    await page.locator('.navbar .setting').click()
    await page.locator('.drawer-select').first().click()
    await page.getByRole('option', { name: label, exact: true }).click()
    await expect(page.locator('.app-main .fade-transform-leave-active, .app-main .fade-transform-enter-active')).toHaveCount(0)
    await page.getByRole('button', { name: '关闭设置', exact: true }).click()
    await page.getByRole('button', { name: '新增', exact: true }).click()
    const task = page.getByRole('dialog', { name: '添加任务', exact: true })
    await task.getByRole('button', { name: '生成表达式', exact: true }).click()
    const builder = page.getByRole('dialog', { name: 'Cron表达式生成器', exact: true })
    await expect(builder.locator('.popup-result-scroll li')).toHaveCount(5)
    await expect(page.locator('.dialog-fade-enter-active')).toHaveCount(0)
    const visual = await builder.locator('.popup-main').evaluate(element => {
      const style = getComputedStyle(element), color = style.color.match(/[\d.]+/g).slice(0, 3).map(Number)
      const background = style.backgroundColor.match(/[\d.]+/g).slice(0, 3).map(Number)
      const panel = element.closest('.el-dialog').getBoundingClientRect(), table = element.querySelector('table').getBoundingClientRect()
      return { separation: Math.abs(color.reduce((sum, value) => sum + value, 0) - background.reduce((sum, value) => sum + value, 0)) / 3,
        panelWidth: panel.width, tableWidth: table.width, viewport: document.documentElement.clientWidth }
    })
    expect(visual.separation, label).toBeGreaterThan(80)
    expect(visual.tableWidth, label).toBeLessThan(visual.panelWidth)
    expect(visual.panelWidth, label).toBeLessThanOrEqual(visual.viewport)
    await builder.screenshot({ path: testInfo.outputPath('cron-' + theme + '.png') })
    await builder.getByRole('button', { name: '取消', exact: true }).click()
    await task.getByRole('button', { name: '取 消', exact: true }).click()
    await expect(task).toBeHidden()
  }
  expect(errors).toEqual([])
  expect(requests.filter(request => request.path.startsWith('/monitor/job') && request.method !== 'GET')).toEqual([])
})
