import { describe, expect, it, vi } from 'vitest'
import { createWebSocketPresenter } from '@/utils/websocketPresenter'

function setup() {
  const handles = []
  const show = vi.fn(options => {
    const handle = { options, close: vi.fn(() => options.onClose()) }
    handles.push(handle)
    return handle
  })
  return { presenter: createWebSocketPresenter({ notify: show, toast: show, dialog: show }), show, handles }
}

const frame = (id, displayType = 'NOTIFICATION', messageLevel = 'NORMAL') => ({
  id, msgType: { messageType: 2, desc: '通知', displayType, messageLevel }, msgData: { title: '标题', content: '<b>纯文本</b>' }
})

describe('WebSocket消息展示', () => {
  it.each([['NORMAL', 'info'], ['WARNING', 'warning'], ['ERROR', 'error']])('消息级别%s映射%s', (level, type) => {
    const { presenter, show } = setup()
    presenter.show(frame('one', 'NOTIFICATION', level))
    expect(show).toHaveBeenCalledWith(expect.objectContaining({ title: '标题', message: '<b>纯文本</b>', type, dangerouslyUseHTMLString: false, position: 'top-right' }))
  })
  it('不展示未知形式、控制消息及无文本的业务对象', () => {
    const { presenter, show } = setup()
    presenter.show(frame('one', 'NONE'))
    presenter.show(frame('two', 'UNTRUSTED'))
    presenter.show({ ...frame('three'), msgData: { orderId: 1 } })
    expect(show).not.toHaveBeenCalled()
  })
  it('相同消息只展示一次，顶部提示支持文本载荷', () => {
    const { presenter, show } = setup()
    const message = { ...frame('one', 'MESSAGE'), msgData: '消息文本' }
    presenter.show(message)
    presenter.show(message)
    expect(show).toHaveBeenCalledTimes(1)
    expect(show).toHaveBeenCalledWith(expect.objectContaining({ message: '消息文本', showClose: true }))
  })
  it('右侧提示同时最多3条，只关闭自身创建的实例', () => {
    const { presenter, handles } = setup()
    for (let i = 0; i < 4; i++) presenter.show(frame(String(i)))
    expect(handles[0].close).toHaveBeenCalledTimes(1)
    expect(handles[1].close).not.toHaveBeenCalled()
    presenter.clear()
    expect(handles.every(handle => handle.close.mock.calls.length === 1)).toBe(true)
  })
  it('居中弹窗按顺序展示，清理后迟到关闭不能重新打开队列', () => {
    const { presenter, show, handles } = setup()
    presenter.show(frame('one', 'MESSAGE_BOX'))
    presenter.show(frame('two', 'MESSAGE_BOX'))
    expect(show).toHaveBeenCalledTimes(1)
    handles[0].close()
    expect(show).toHaveBeenCalledTimes(2)
    presenter.show(frame('three', 'MESSAGE_BOX'))
    presenter.clear()
    handles[1].options.onClose()
    expect(show).toHaveBeenCalledTimes(2)
  })
})
