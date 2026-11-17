import { isWebSocketBusinessType } from '@/utils/websocketMessageType'
import { h, render } from 'vue'
import { ElNotification, ElMessage, ElDialog, ElButton, ElAlert } from 'element-plus'

// 使用独立Dialog实例，退出时只关闭WebSocket弹窗，不干扰HTTP确认框或验证码输入框。
function openDialog(options) {
  const host = document.createElement('div')
  document.body.appendChild(host)
  let closed = false
  const close = () => {
    if (closed) return
    closed = true
    render(null, host)
    host.remove()
    options.onClose()
  }
  render(h(ElDialog, {
    modelValue: true,
    title: options.title,
    width: 'min(560px, 92vw)',
    alignCenter: true,
    appendToBody: true,
    closeOnClickModal: false,
    'onUpdate:modelValue': value => { if (!value) close() }
  }, {
    default: () => h('div', { style: { whiteSpace: 'pre-wrap', overflowWrap: 'anywhere' } }, [
      h(ElAlert, { title: options.levelLabel, type: options.type, closable: false, showIcon: true }),
      h('p', options.message)
    ]),
    footer: () => h(ElButton, { type: 'primary', onClick: close }, () => '我知道了')
  }), host)
  return { close }
}

// 仅展示后端明确声明形式的业务消息；所有文本按纯文本渲染，不执行HTML或自动跳转。
export function createWebSocketPresenter({ notify = ElNotification, toast = ElMessage, dialog = openDialog } = {}) {
  const handles = new Set()
  const pending = []
  const seen = new Set()
  let activeDialog
  let generation = 0
  const levels = { NORMAL: ['info', '普通'], WARNING: ['warning', '警告'], ERROR: ['error', '错误'] }

  function pump() {
    if (activeDialog || !pending.length) return
    const current = generation
    const marker = {}
    activeDialog = marker
    marker.handle = dialog({ ...pending.shift(), onClose: () => {
      if (generation !== current || activeDialog !== marker) return
      activeDialog = undefined
      pump()
    } })
  }

  return {
    show(message) {
      const info = message?.msgType
      if (!isWebSocketBusinessType(info?.messageType)) return
      const level = levels[info?.messageLevel]
      if (!level || !['NOTIFICATION', 'MESSAGE', 'MESSAGE_BOX'].includes(info?.displayType)) return
      if (message.id && seen.has(message.id)) return
      const data = message.msgData
      const content = typeof data === 'string' ? data : typeof data?.content === 'string' ? data.content : ''
      if (!content.trim()) return
      if (message.id) {
        seen.add(message.id)
        if (seen.size > 200) seen.delete(seen.values().next().value)
      }
      const options = {
        title: typeof data?.title === 'string' && data.title.trim() ? data.title : info.desc || '消息通知',
        message: content,
        type: level[0],
        levelLabel: level[1],
        dangerouslyUseHTMLString: false
      }
      if (info.displayType === 'MESSAGE_BOX') {
        // 居中消息逐条展示，限制队列大小，避免消息风暴耗尽浏览器内存。
        if (pending.length >= 50) pending.shift()
        pending.push(options)
        pump()
        return
      }
      // 同时最多保留3条非模态提示，只管理本通信模块创建的实例。
      if (handles.size >= 3) {
        const oldest = handles.values().next().value
        handles.delete(oldest)
        oldest.close()
      }
      let handle
      const onClose = () => handles.delete(handle)
      handle = info.displayType === 'NOTIFICATION'
        ? notify({ ...options, position: 'top-right', offset: 90, duration: 6000, onClose })
        : toast({ ...options, showClose: true, duration: 4000, onClose })
      handles.add(handle)
    },
    clear() {
      generation++
      pending.length = 0
      const previous = activeDialog
      activeDialog = undefined
      previous?.handle?.close()
      for (const handle of handles) handle.close()
      handles.clear()
      seen.clear()
    }
  }
}
