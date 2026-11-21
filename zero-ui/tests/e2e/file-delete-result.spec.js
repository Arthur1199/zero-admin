import { test, expect } from '@playwright/test'
import { authenticated } from './mock-api'
import { WebSocketMessageType } from '../../src/utils/websocketMessageType'

test('文件单删成功与批删云端部分失败分别提示，并刷新已删除记录', async ({ page }) => {
  await page.routeWebSocket('**/ws/admin', socket => {
    socket.onMessage(() => socket.send(JSON.stringify({
      msgType: { messageType: WebSocketMessageType.CREATE_CONNECTION },
      msgData: { heartbeatInterval: 30000, idleTimeout: 60000 }
    })))
  })
  await authenticated(page)
  const endpoint = '**/dev-api/systemTools/fileManagement'
  const warning = '删除成功，云平台记录删除失败'
  let rows = [1, 2, 3].map(id => ({ id, filename: `file-${id}.png`, originalFilename: `file-${id}.png` }))
  const deletions = []
  await page.route(endpoint + '/list?**', route => route.fulfill({ json: { code: 200, rows, total: rows.length } }))
  await page.route(endpoint + '/*', async route => {
    if (route.request().method() !== 'DELETE') return route.fallback()
    const ids = decodeURIComponent(new URL(route.request().url()).pathname.split('/').at(-1)).split(',').map(Number)
    deletions.push(ids)
    rows = rows.filter(row => !ids.includes(row.id))
    const allDeleted = ids.length === 1
    return route.fulfill({ json: { code: 200, data: allDeleted, msg: allDeleted ? '删除成功' : warning } })
  })

  await page.goto('/systemTools/fileManagement')
  const table = page.locator('.file-management-page .el-table')
  const tableRows = table.locator('.el-table__body tbody tr')
  await expect(tableRows).toHaveCount(3)
  await tableRows.first().getByRole('button', { name: '删除', exact: true }).click()
  const confirm = page.locator('.el-message-box').getByRole('button', { name: '确定', exact: true })
  await confirm.click()
  await expect(page.locator('.el-message--success')).toHaveText('删除成功')
  await expect(tableRows).toHaveCount(2)
  await table.locator('.el-table__header .el-checkbox').click()
  await page.getByRole('button', { name: '批量删除', exact: true }).click()
  await confirm.click()
  await expect(page.locator('.el-message--warning')).toHaveText(warning)
  await expect(tableRows).toHaveCount(0)
  expect(deletions).toEqual([[1], [2, 3]])
})
