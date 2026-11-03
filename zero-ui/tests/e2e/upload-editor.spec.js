import { test, expect } from '@playwright/test'
import { authenticated } from './mock-api'

const png = Buffer.from('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+aV1cAAAAASUVORK5CYII=', 'base64')
const file = (name, mimeType = 'text/plain', buffer = Buffer.from('mock-file')) => ({ name, mimeType, buffer })

async function setup(page, responses) {
  await authenticated(page)
  const requests = []
  await page.route('**/dev-api/common/upload', async route => {
    const request = route.request(), bytes = request.postDataBuffer().toString('utf8')
    const name = bytes.match(/filename="([^"]+)"/)?.[1]
    requests.push({ name, authorization: request.headers().authorization, contentType: request.headers()['content-type'] })
    const config = responses(name)
    if (config.delay) await new Promise(resolve => setTimeout(resolve, config.delay))
    if (config.networkError) return route.abort('failed')
    return route.fulfill({ json: config.json })
  })
  await page.route('**/dev-api/profile/mock/**', route => route.fulfill({ contentType: 'image/png', body: png }))
  const errors = []; page.on('pageerror', error => errors.push(error.message))
  await page.goto('/tests/fixtures/upload-editor.html')
  await expect(page.locator('.ql-editor')).toBeVisible()
  return { requests, errors }
}

async function pasteImage(page, name, mime, bytes) {
  await page.locator('.ql-editor').click()
  // 通过浏览器ClipboardEvent输入测试文件，覆盖实际paste处理链；不直接调用Vue/Quill方法。
  const result = await page.locator('.ql-editor').evaluate((element, data) => {
    const clipboard = new DataTransfer()
    clipboard.items.add(new File([new Uint8Array(data.bytes)], data.name, { type: data.mime }))
    const event = new ClipboardEvent('paste', { bubbles: true, cancelable: true, clipboardData: clipboard })
    const provided = [...clipboard.items].map(item => ({ type: item.type, size: item.getAsFile()?.size }))
    const received = [...(event.clipboardData?.items || [])].map(item => ({ type: item.type, size: item.getAsFile()?.size }))
    // 本机Firefox会丢弃非受信ClipboardEvent构造参数中的文件；显式补齐测试事件的数据。
    // 这里只验证公开paste事件处理链，不替代操作系统真实剪贴板验证。
    if (received.length !== provided.length) Object.defineProperty(event, 'clipboardData', { value: clipboard })
    const dispatched = [...event.clipboardData.items].map(item => ({ type: item.type, size: item.getAsFile()?.size }))
    element.dispatchEvent(event)
    return { provided, received, dispatched }
  }, { name, mime, bytes: [...bytes] })
  await test.info().attach('paste-fixture-' + name, { body: JSON.stringify(result), contentType: 'application/json' })
  expect(result.dispatched).toEqual([{ type: mime, size: bytes.length }])
}

test('富文本格式、工具栏选图、粘贴图及上传响应格式保持HTML存储', async ({ page }) => {
  const { requests, errors } = await setup(page, name => ({ json: name === 'toolbar.png'
    ? { code: 200, fileName: '/profile/mock/toolbar.png' }
    : { code: 200, data: { url: '/profile/mock/paste.png' } } }))
  await page.locator('.ql-editor').click()
  await page.locator('.ql-bold').click()
  // 按正常逐字输入节奏验证完整HTML；零延迟Firefox/Quill光标问题保留独立诊断，不计为修复。
  await page.locator('.ql-editor').pressSequentially('富文本验收', { delay: 100 })
  await expect(page.getByTestId('editor-model')).toContainText('<strong>富文本验收</strong>')
  const chooser = page.waitForEvent('filechooser')
  await page.locator('.ql-image').click()
  await (await chooser).setFiles(file('toolbar.png', 'image/png', png))
  await expect(page.locator('.ql-editor img[src="/dev-api/profile/mock/toolbar.png"]')).toBeVisible()
  await pasteImage(page, 'paste.png', 'image/png', png)
  await expect(page.locator('.ql-editor img[src="/dev-api/profile/mock/paste.png"]')).toBeVisible()
  await expect(page.getByTestId('editor-model')).toContainText('<img')
  expect(requests).toHaveLength(2)
  expect(requests.every(request => request.authorization === 'Bearer mock-token' && request.contentType.includes('multipart/form-data; boundary='))).toBe(true)
  await page.goto('/index')
  expect(errors).toEqual([])
})

test('富文本选图与粘贴执行同样的格式和大小校验，不发送非法文件', async ({ page }) => {
  const { requests, errors } = await setup(page, () => ({ json: { code: 200, url: '/profile/mock/valid.png' } }))
  const input = page.locator('#editor-section input[type=file]').first()
  await input.setInputFiles(file('invalid.gif', 'image/gif', png))
  await expect(page.getByText('图片格式错误!', { exact: true })).toBeVisible()
  await pasteImage(page, 'invalid.gif', 'image/gif', png)
  await pasteImage(page, 'large.png', 'image/png', Buffer.alloc(1024 * 1024))
  await expect(page.getByText('上传文件大小不能超过 1 MB!', { exact: true })).toBeVisible()
  expect(requests).toHaveLength(0)
  expect(errors).toEqual([])
})

test('富文本上传返回前卸载组件，不报图片插入失败或更新已销毁实例', async ({ page }) => {
  const { requests, errors } = await setup(page, () => ({ delay: 600, json: { code: 200, url: '/profile/mock/late.png' } }))
  let finishUpload
  const settled = new Promise(resolve => { finishUpload = resolve })
  await page.exposeFunction('recordUploadEnd', () => finishUpload())
  await page.evaluate(() => {
    const open = XMLHttpRequest.prototype.open
    XMLHttpRequest.prototype.open = function(...args) {
      if (String(args[1]).endsWith('/common/upload')) this.addEventListener('loadend', () => {
        // 等网络结束后的Promise及Vue渲染完成，避免“消息尚未出现”造成假通过。
        setTimeout(() => window.recordUploadEnd(), 0)
      }, { once: true })
      return open.apply(this, args)
    }
  })
  const response = page.waitForResponse(result => result.url().endsWith('/common/upload'))
  await page.locator('#editor-section input[type=file]').first().setInputFiles(file('late.png', 'image/png', png))
  await expect.poll(() => requests.length).toBe(1)
  await page.getByRole('button', { name: '卸载富文本', exact: true }).click()
  await expect(page.locator('.ql-editor')).toHaveCount(0)
  await (await response).finished()
  await settled
  await page.getByRole('button', { name: '恢复富文本', exact: true }).click()
  await expect(page.locator('.ql-editor')).toBeVisible()
  await expect(page.getByText('图片插入失败', { exact: true })).not.toBeVisible()
  await expect(page.locator('.ql-editor img')).toHaveCount(0)
  expect(errors).toEqual([])
})

for (const kind of ['file', 'image']) {
  for (const failure of ['business-first', 'network-last']) {
  test(kind + '并行上传部分失败后成功项保留，计数归零且可继续上传 ' + failure, async ({ page }) => {
    const { requests, errors } = await setup(page, name => name.startsWith('bad')
      ? { delay: failure === 'business-first' ? 80 : 180, networkError: failure === 'network-last', json: { code: 500, msg: '模拟上传业务失败' } }
      : { delay: failure === 'business-first' ? 180 : 80, json: name.startsWith('next') ? { code: 200, data: { url: '/profile/mock/' + name } } : { code: 200, fileName: '/profile/mock/' + name } })
    const scope = page.locator('#' + kind + '-section'), input = scope.locator('input[type=file]')
    const extension = kind === 'file' ? 'txt' : 'png'
    const payload = name => file(name + '.' + extension, kind === 'file' ? 'text/plain' : 'image/png', kind === 'file' ? Buffer.from('mock-file') : png)
    await input.setInputFiles([payload('bad'), payload('good')])
    await expect(page.getByText(failure === 'business-first' ? '模拟上传业务失败' : kind === 'file' ? '上传文件失败，请重试' : '上传图片失败，请重试', { exact: true })).toBeVisible()
    await expect(page.getByTestId(kind + '-model')).toContainText('/profile/mock/good.' + extension)
    await expect(page.getByTestId(kind + '-model')).not.toContainText('bad')
    await expect(page.locator('.el-loading-mask:visible')).toHaveCount(0)
    await input.setInputFiles(payload('next'))
    await expect(scope.locator('.el-upload-list__item')).toHaveCount(2)
    await expect(page.getByTestId(kind + '-model')).toContainText('/profile/mock/next.' + extension)
    expect(requests).toHaveLength(3)
    expect(errors).toEqual([])
  })
  }
}
