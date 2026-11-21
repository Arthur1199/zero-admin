import { test, expect } from '@playwright/test'
import fs from 'node:fs/promises'
import { authenticated, mockAdminWebSocket } from './mock-api'

test('头像选图校验、旋转缩放、PNG裁剪Blob上传及失败重试', async ({ page }) => {
  const errors = []; page.on('pageerror', error => errors.push(error.message))
  await mockAdminWebSocket(page)
  await authenticated(page)
  const uploads = [], payloads = []
  await page.exposeFunction('recordAvatarPayload', payload => payloads.push(payload))
  await page.addInitScript(() => {
    const send = XMLHttpRequest.prototype.send
    XMLHttpRequest.prototype.send = function(data) {
      if (data instanceof FormData && data.has('avatarfile')) {
        const file = data.get('avatarfile')
        file.arrayBuffer().then(buffer => {
          const bytes = new Uint8Array(buffer), view = new DataView(buffer)
          window.recordAvatarPayload({ field: 'avatarfile', filename: file.name, type: file.type, size: file.size,
            png: [137, 80, 78, 71, 13, 10, 26, 10].every((value, index) => bytes[index] === value),
            width: view.getUint32(16), height: view.getUint32(20) })
        })
      }
      return send.call(this, data)
    }
  })
  const jpeg = await fs.readFile('src/assets/images/profile.jpg')
  await page.route('**/dev-api/system/user/profile/avatar', async route => {
    const request = route.request(), bytes = request.postDataBuffer()
    const signature = Buffer.from([137, 80, 78, 71, 13, 10, 26, 10])
    const offset = bytes.indexOf(signature)
    uploads.push({
      authorization: request.headers().authorization, contentType: request.headers()['content-type'],
      field: bytes.subarray(0, offset < 0 ? Math.min(300, bytes.length) : offset).toString('utf8'), bodyLength: bytes.length,
      width: offset < 0 ? 0 : bytes.readUInt32BE(offset + 16), height: offset < 0 ? 0 : bytes.readUInt32BE(offset + 20)
    })
    return route.fulfill({ json: uploads.length === 1 ? { code: 500, msg: '模拟头像保存失败' } : { code: 200, imgUrl: '/profile/mock-avatar.png' } })
  })
  await page.route('**/dev-api/profile/mock-avatar.png', route => route.fulfill({ contentType: 'image/jpeg', body: jpeg }))
  await page.goto('/user/profile')
  await page.locator('.user-info-head').click()
  const dialog = page.getByRole('region', { name: '修改头像' }), canvas = dialog.locator('.cropper-box-canvas')
  await expect(canvas).toBeVisible()
  await dialog.locator('input[type=file]').setInputFiles({ name: 'invalid.txt', mimeType: 'text/plain', buffer: Buffer.from('mock') })
  await expect(page.getByText('文件格式错误，请上传图片类型,如：JPG，PNG后缀的文件。', { exact: true })).toBeVisible()
  expect(uploads).toHaveLength(0)
  const previousImage = await dialog.locator('.cropper-box-canvas img').getAttribute('src')
  await dialog.locator('input[type=file]').setInputFiles({ name: 'mock-avatar.jpg', mimeType: 'image/jpeg', buffer: jpeg })
  // Vue3裁剪器将输入图片转换为Blob地址；验证新图片已载入，不绑定内部编码形式。
  await expect(dialog.locator('.cropper-box-canvas img')).not.toHaveAttribute('src', previousImage)
  await expect.poll(() => dialog.locator('.cropper-box-canvas img').evaluate(img => img.complete && img.naturalWidth > 0)).toBe(true)
  await expect(dialog.locator('.cropper-crop-box')).toBeVisible()
  await dialog.getByRole('button', { name: '向左旋转', exact: true }).click()
  await expect(canvas).toHaveAttribute('style', /rotateZ\(-90deg\)/)
  await dialog.getByRole('button', { name: '向右旋转', exact: true }).click()
  await expect(canvas).toHaveAttribute('style', /rotateZ\(0deg\)/)
  const initial = await canvas.getAttribute('style')
  await dialog.getByRole('button', { name: '放大', exact: true }).click()
  await expect(canvas).not.toHaveAttribute('style', initial)
  const zoomed = await canvas.getAttribute('style')
  await dialog.getByRole('button', { name: '缩小', exact: true }).click()
  await expect(canvas).not.toHaveAttribute('style', zoomed)
  await expect.poll(() => dialog.locator('.avatar-upload-preview img').evaluate(img => img.complete && img.naturalWidth > 0)).toBe(true)
  await dialog.getByRole('button', { name: /提\s*交/ }).click()
  await expect(page.getByText('模拟头像保存失败', { exact: true })).toBeVisible()
  await expect(dialog).toBeVisible()
  // 保留一秒防重复提交限制，失败后按原规则重试。
  await page.waitForTimeout(1100)
  const preview = dialog.locator('.avatar-upload-preview img')
  const source = await canvas.locator('img').getAttribute('src')
  const previewStyle = await preview.getAttribute('style')
  const previewSource = await preview.getAttribute('src')
  await dialog.getByRole('button', { name: /提\s*交/ }).click()
  await expect(dialog).toBeVisible()
  await expect(page.locator('.user-info-head img')).toHaveAttribute('src', '/dev-api/profile/mock-avatar.png')
  await expect(canvas.locator('img')).toHaveAttribute('src', source)
  await expect(preview).toHaveAttribute('src', previewSource)
  await expect(preview).toHaveAttribute('style', previewStyle)
  expect(uploads).toHaveLength(2)
  await expect.poll(() => payloads.length).toBe(2)
  await test.info().attach('avatar-payload-metadata', { body: JSON.stringify({ payloads, transport: uploads.map(({ contentType, bodyLength, width, height }) => ({ contentType, bodyLength, width, height })) }), contentType: 'application/json' })
  for (const upload of uploads) {
    expect(upload.authorization).toBe('Bearer mock-token')
    expect(upload.contentType).toContain('multipart/form-data; boundary=')
  }
  // WebKit协议可能省略multipart文件字节；从实际XHR提交的FormData验证Blob，不伪造上传对象。
  for (const payload of payloads) {
    expect(payload).toMatchObject({ field: 'avatarfile', filename: 'mock-avatar.jpg', type: 'image/png', png: true })
    expect(payload.size).toBeGreaterThan(0)
    expect(payload.width).toBeGreaterThan(0)
    expect(payload.height).toBeGreaterThan(0)
  }
  expect(errors).toEqual([])
})
