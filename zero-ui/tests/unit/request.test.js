import { beforeEach, describe, expect, it, vi } from 'vitest'
const mocks = vi.hoisted(() => ({ token: 'test-token', prompt: vi.fn(), message: vi.fn(), storage: new Map(), dispatch: vi.fn() }))
vi.mock('@/store', () => ({ default: { dispatch: mocks.dispatch } }))
vi.mock('@/utils/auth', () => ({ getToken: () => mocks.token }))
vi.mock('@/plugins/cache', () => ({ default: { session: { getJSON: key => mocks.storage.get(key), setJSON: (key, value) => mocks.storage.set(key, value) } } }))
vi.mock('element-plus', () => ({ ElNotification: { error: vi.fn() }, ElMessageBox: { prompt: mocks.prompt, confirm: vi.fn() }, ElMessage: mocks.message, ElLoading: { service: vi.fn() } }))
import service from '@/utils/request'

beforeEach(() => {
  mocks.storage.clear(); mocks.prompt.mockReset(); mocks.message.mockClear(); mocks.token = 'test-token'
  const values = new Map()
  vi.stubGlobal('localStorage', { getItem: key => values.get(key) || null, setItem: (key, value) => values.set(key, value), removeItem: key => values.delete(key) })
  vi.stubGlobal('document', { querySelector: () => null })
})
const adapter = data => async config => ({ data, status: 200, statusText: 'OK', config, headers: {}, request: { responseType: config.responseType } })
describe('接口与错误提示保持原协议', () => {
  it('GET参数与Token按原协议传递', async () => {
    let sent
    const result = await service.get('/system/config/list', { params: { pageNum: 2, pageSize: 10, name: '中文' }, adapter: async config => { sent = config; return adapter({ code: 200, rows: [], total: 0 })(config) } })
    expect(sent.headers.Authorization).toBe('Bearer test-token')
    expect(sent.url).toContain('pageNum=2')
    expect(sent.url).toContain(encodeURIComponent('中文'))
    expect(result).toEqual({ code: 200, rows: [], total: 0 })
  })
  it('普通data与上传兼容字段不被改写', async () => {
    const payload = { code: 200, data: { id: '12345678901234567890' }, url: '/mock.png', fileName: '/mock.png' }
    expect(await service.get('/common/upload', { adapter: adapter(payload) })).toEqual(payload)
  })
  it('谷歌保护接口按方法和动态路径匹配', async () => {
    localStorage.setItem('google_auth_apis', JSON.stringify([{ apiMethod: 'PUT', apiUrl: '/system/config/{id}' }]))
    mocks.prompt.mockResolvedValue({ value: '123456' })
    let headers
    await service.put('/system/config/3', {}, { adapter: async config => { headers = config.headers; return adapter({ code: 200 })(config) } })
    expect(headers['X-Google-Code']).toBe('123456')
    expect(mocks.prompt).toHaveBeenCalledTimes(1)
  })
  it('谷歌保护接口取消后不发送请求', async () => {
    localStorage.setItem('google_auth_apis', JSON.stringify(['/system/config/**']))
    mocks.prompt.mockRejectedValue('cancel')
    const send = vi.fn(adapter({ code: 200 }))
    await expect(service.post('/system/config/test', {}, { adapter: send })).rejects.toBe('cancel')
    expect(send).not.toHaveBeenCalled()
  })
  it('重复提交会被阻止', async () => {
    const config = { adapter: adapter({ code: 200 }) }
    await service.post('/system/config', { id: 1 }, config)
    await expect(service.post('/system/config', { id: 1 }, config)).rejects.toThrow('数据正在处理')
  })
  for (const code of [500, 601, -1001, -1002, -1003]) it('错误码' + code + '不能当成功返回', async () => {
    await expect(service.get('/system/config/test', { adapter: adapter({ code, msg: '测试错误' }) })).rejects.toBeDefined()
  })
  it('文件流不按JSON业务码解析', async () => {
    const file = new Blob(['mock-file'])
    expect(await service.get('/export', { responseType: 'blob', adapter: adapter(file) })).toBe(file)
  })
})
