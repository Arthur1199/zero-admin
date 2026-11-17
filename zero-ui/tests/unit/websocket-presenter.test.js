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
  it.each([['NORMAL', 'success'], ['WARNING', 'warning'], ['ERROR', 'error']])('消息级别%s映射%s', (level, type) => {
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

const notice = (id, displayType = 'NOTIFICATION', duration = 0) => ({
  id, msgType: { messageType: 10, displayType: 'NONE', messageLevel: 'NORMAL' },
  msgData: { title: '系统通知', content: '<script>纯文本</script>' },
  displayOptions: { displayType, messageLevel: 'ERROR', duration }
})
it('系统通知使用单条级别和时长，不修改枚举默认值', () => {
  const { presenter, show } = setup()
  const message = notice('dynamic', 'NOTIFICATION', 12000)
  presenter.show(message)
  expect(show).toHaveBeenCalledWith(expect.objectContaining({ type: 'error', duration: 12000, dangerouslyUseHTMLString: false, showClose: true }))
  expect(message.msgType.displayType).toBe('NONE')
})
it('常驻通知不被新通知挤掉，手工关闭后按队列展示', () => {
  const { presenter, show, handles } = setup()
  for (let i=0;i<6;i++) presenter.show(notice('permanent-' + i))
  expect(show).toHaveBeenCalledTimes(5)
  expect(handles.every(handle => !handle.close.mock.calls.length)).toBe(true)
  handles[0].close()
  expect(show).toHaveBeenCalledTimes(6)
  presenter.clear()
})
it('居中自动关闭从实际展示开始，退出清理后不再打开队列', () => {
  vi.useFakeTimers()
  try {
    const { presenter, show, handles } = setup()
    presenter.show(notice('dialog-1', 'MESSAGE_BOX', 2000))
    presenter.show(notice('dialog-2', 'MESSAGE_BOX', 2000))
    vi.advanceTimersByTime(2000)
    expect(handles[0].close).toHaveBeenCalledOnce()
    expect(show).toHaveBeenCalledTimes(2)
    vi.advanceTimersByTime(1000)
    expect(handles[1].close).not.toHaveBeenCalled()
    presenter.clear()
    expect(vi.getTimerCount()).toBe(0)
  } finally { vi.useRealTimers() }
})
it.each([-1, 1, 3600001, null])('非法展示时间%s不弹窗', duration => {
  const { presenter, show } = setup()
  presenter.show(notice('bad-duration', 'MESSAGE', duration))
  expect(show).not.toHaveBeenCalled()
})
it('其它业务消息不能通过displayOptions改变展示形式', () => {
  const { presenter, show } = setup()
  presenter.show({ ...frame('other', 'NONE'), displayOptions: notice('unused').displayOptions })
  expect(show).not.toHaveBeenCalled()
})

it.each(['MESSAGE', 'NOTIFICATION'])('系统通知%s展示纯文本摘要，详情只打开一次并关闭原提示', displayType => {
  const { presenter, show, handles } = setup()
  const message = notice('summary', displayType, 6000)
  message.msgData.content = '<script>完整正文</script>'.repeat(30)
  presenter.show(message)
  const options = handles[0].options
  expect(options.customClass).toBe(displayType === 'MESSAGE' ? 'admin-ws-notice-message' : 'admin-ws-notice-card')
  if (displayType === 'NOTIFICATION') expect(options.message.props.showTitle).toBe(false)
  expect(options.message.type.name).toBe('WebSocketNoticeSummary')
  expect(options.message.props.title).toBe('系统通知')
  expect(options.message.props.content).toBe(message.msgData.content)
  options.message.props.onDetails()
  expect(handles[0].close).toHaveBeenCalledOnce()
  expect(handles[1].options).toEqual(expect.objectContaining({ message: message.msgData.content, duration: 0 }))
  options.message.props.onDetails()
  expect(show).toHaveBeenCalledTimes(2)
  presenter.clear()
  options.message.props.onDetails()
  expect(show).toHaveBeenCalledTimes(2)
})

it.each(['MESSAGE', 'NOTIFICATION'])('通知%s自动关闭后，旧详情回调不能重新打开弹窗', displayType => {
  const { presenter, show, handles } = setup()
  presenter.show(notice('expired-summary', displayType, 1000))
  const oldDetails = handles[0].options.message.props.onDetails
  handles[0].close()
  expect(oldDetails()).toBe(false)
  expect(show).toHaveBeenCalledTimes(1)
  presenter.clear()
})

it('顶部和右侧分别最多5条，排队及关闭互不占用名额', () => {
  const { presenter, show, handles } = setup()
  for (let i = 0; i < 7; i++) presenter.show(notice('card-' + i))
  for (let i = 0; i < 7; i++) presenter.show(notice('top-' + i, 'MESSAGE'))
  expect(show).toHaveBeenCalledTimes(10)
  expect(handles.filter(item => item.options.position === 'top-right')).toHaveLength(5)
  expect(handles.every(item => !item.close.mock.calls.length)).toBe(true)
  handles[0].close()
  expect(handles[10].options.position).toBe('top-right')
  handles[5].close()
  expect(handles[11].options.position).toBeUndefined()
  presenter.clear()
  handles[1].options.onClose()
  expect(show).toHaveBeenCalledTimes(12)
})
it('居中待展示超过50条仍按接收顺序逐条弹出', () => {
  const { presenter, show, handles } = setup()
  for (let i = 0; i < 65; i++) {
    const message = notice('modal-' + i, 'MESSAGE_BOX')
    message.msgData.title = '通知' + i
    presenter.show(message)
  }
  expect(show).toHaveBeenCalledTimes(1)
  for (let i = 0; i < 65; i++) {
    expect(handles[i].options.title).toBe('通知' + i)
    handles[i].close()
  }
  expect(show).toHaveBeenCalledTimes(65)
  presenter.clear()
})

it('卡片布局完成后重新排列位置，关闭或退出后的旧回调不再操作通知', () => {
  const { presenter, show, handles } = setup()
  show.updateOffsets = vi.fn()
  presenter.show(notice('layout-1'))
  const layout = handles[0].options.message.props.onLayout
  layout()
  expect(show.updateOffsets).toHaveBeenCalledWith('top-right')
  handles[0].close()
  layout()
  expect(show.updateOffsets).toHaveBeenCalledTimes(1)
  presenter.show(notice('layout-2'))
  const previous = handles[1].options.message.props.onLayout
  presenter.clear()
  previous()
  expect(show.updateOffsets).toHaveBeenCalledTimes(1)
})

it('系统通知枚举展示字段为null时使用单条配置，缺少配置不得弹出', () => {
  const { presenter, show } = setup()
  const message = notice('null-default')
  message.msgType.displayType = null
  message.msgType.messageLevel = null
  presenter.show(message)
  expect(show).toHaveBeenCalledWith(expect.objectContaining({ type: 'error', duration: 0 }))
  presenter.show({ ...message, id: 'missing-options', displayOptions: null })
  expect(show).toHaveBeenCalledTimes(1)
  presenter.clear()
})
