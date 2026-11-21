import { isWebSocketBusinessType, WebSocketMessageType } from '@/utils/websocketMessageType'
import { h, render } from 'vue'
import { ElNotification, ElMessage, ElDialog, ElButton } from 'element-plus'
import '@/assets/styles/components/notice.scss'
import { noticeContent, noticeDetailsHtml } from '@/utils/noticeRichContent'

// 按实际排版判断摘要是否溢出，短消息不显示详情入口；窗口变化时重新测量。
const NoticeSummary = {
  name: 'WebSocketNoticeSummary',
  props: {
    title: String, content: String, onDetails: Function, onLayout: Function,
    showTitle: { type: Boolean, default: true },
    forceDetails: { type: Boolean, default: false }
  },
  data() { return { overflow: false, queueFull: false } },
  mounted() {
    this.$nextTick(this.measure)
    if (typeof ResizeObserver !== 'undefined') {
      this._observer = new ResizeObserver(this.measure)
      this._observer.observe(this.$refs.summary)
    } else window.addEventListener('resize', this.measure)
  },
  beforeUnmount() {
    this._observer?.disconnect()
    window.removeEventListener('resize', this.measure)
  },
  methods: {
    measure() {
      const node = this.$refs.summary
      if (node) this.overflow = node.scrollHeight > node.clientHeight || node.scrollWidth > node.clientWidth
      // 详情入口会改变卡片高度，等本轮DOM更新后再排列连续到达的通知。
      if (this.onLayout) this.$nextTick(this.onLayout)
    }
  },
  render() {
    return h('div', { class: 'admin-ws-notice-summary' }, [
      this.showTitle ? h('strong', { class: 'admin-ws-notice-summary__title', title: this.title }, this.title) : null,
      h('div', { ref: 'summary', class: 'admin-ws-notice-summary__text' }, this.content),
      this.overflow || this.forceDetails ? h(ElButton, {
        link: true, type: 'primary',
        onClick: () => { this.queueFull = this.onDetails() === false }
      }, () => this.queueFull ? '通知较多，请稍后查看' : '查看详情') : null
    ])
  }
}

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
    width: 'min(1500px, 92vw)',
    alignCenter: true,
    appendToBody: true,
    modalClass: 'admin-ws-notice-dialog-overlay',
    closeOnClickModal: false,
    'onUpdate:modelValue': value => { if (!value) close() }
  }, {
    default: () => h('div', { style: { whiteSpace: 'pre-wrap', overflowWrap: 'anywhere', maxHeight: '70vh', overflowY: 'auto' } }, [
      options.richHtml
        ? h('div', { class: 'admin-ws-rich-content', innerHTML: noticeDetailsHtml(options.richHtml) })
        : h('p', { style: { margin: '0', lineHeight: '1.7' } }, options.message)
    ]),
    footer: () => h(ElButton, { type: 'primary', onClick: close }, () => '我知道了')
  }), host)
  return { close }
}

// 仅展示后端明确声明形式的业务消息；所有文本按纯文本渲染，不执行HTML或自动跳转。
export function createWebSocketPresenter({ notify = ElNotification, toast = ElMessage, dialog = openDialog } = {}) {
  const handles = new Set()
  const pending = []
  const cards = []
  const seen = new Set()
  let activeDialog
  let generation = 0
  const levels = { NORMAL: ['success', '普通'], WARNING: ['warning', '警告'], ERROR: ['error', '错误'] }

  function pump() {
    if (activeDialog || !pending.length) return
    const current = generation
    const marker = {}
    activeDialog = marker
    const options = pending.shift()
    marker.handle = dialog({ ...options, onClose: () => {
      clearTimeout(marker.timer)
      if (generation !== current || activeDialog !== marker) return
      activeDialog = undefined
      pump()
    } })
    if (options.duration > 0) marker.timer = setTimeout(() => marker.handle.close(), options.duration)
  }

  function pumpCards() {
    while (cards.length) {
      // 顶部提示与右侧卡片各有5个名额，某一类排满不阻塞另一类。
      const index = cards.findIndex(card => [...handles].filter(item => item.displayType === card.displayType).length < 5)
      if (index < 0) return
      const { options, displayType, summary } = cards.splice(index, 1)[0]
      const marker = { displayType }
      const current = generation
      handles.add(marker)
      const onClose = () => {
        handles.delete(marker)
        if (generation === current) pumpCards()
      }
      const onDetails = () => {
        if (generation !== current || !handles.has(marker) || marker.detailsOpened) return false
        marker.detailsOpened = true
        // 详情保留完整正文，手动关闭；仍纳入统一队列与退出清理，不另起失控弹层。
        marker.handle?.close()
        if (generation !== current) return false
        pending.unshift({ ...options, duration: 0 })
        pump()
        return true
      }
      const { richHtml, ...displayOptions } = options
      marker.handle = displayType === 'NOTIFICATION'
        ? notify({ ...displayOptions, showClose: true, position: 'top-right', offset: 90, onClose, ...(summary ? {
          customClass: 'admin-ws-notice-card',
          message: h(NoticeSummary, { title: options.title, content: options.message, onDetails, showTitle: false, forceDetails: !!richHtml,
            onLayout: () => { if (generation === current && handles.has(marker)) notify.updateOffsets?.('top-right') }
          })
        } : {}) })
        : toast({ ...displayOptions, showClose: true, onClose, ...(summary ? {
          customClass: 'admin-ws-notice-message',
          message: h(NoticeSummary, { title: options.title, content: options.message, onDetails, forceDetails: !!richHtml })
        } : {}) })
    }
  }

  return {
    show(message) {
      const base = message?.msgType
      const dynamic = base?.messageType === WebSocketMessageType.SYSTEM_NOTICE
      if (dynamic && message.displayOptions == null) return
      const info = dynamic ? { ...base, ...message.displayOptions } : base
      const duration = message?.displayOptions?.duration
      if (dynamic && (!Number.isInteger(duration) || duration < 0 || duration > 3600000 || duration % 1000)) return
      if (!isWebSocketBusinessType(info?.messageType)) return
      const level = levels[info?.messageLevel]
      if (!level || !['NOTIFICATION', 'MESSAGE', 'MESSAGE_BOX'].includes(info?.displayType)) return
      if (message.id && seen.has(message.id)) return
      const data = message.msgData
      const rawContent = typeof data === 'string' ? data : typeof data?.content === 'string' ? data.content : ''
      const rich = noticeContent(rawContent, base.messageType === WebSocketMessageType.SYSTEM_NOTICE ? data?.contentFormat : 'TEXT')
      const content = rich.text
      if (!content.trim()) return
      if (info.displayType !== 'MESSAGE_BOX' && cards.filter(card => card.displayType === info.displayType).length >= 50) return
      if (message.id) {
        seen.add(message.id)
        if (seen.size > 200) seen.delete(seen.values().next().value)
      }
      const options = {
        title: typeof data?.title === 'string' && data.title.trim() ? data.title : info.desc || '消息通知',
        message: content,
        richHtml: rich.html,
        type: level[0],
        levelLabel: level[1],
        dangerouslyUseHTMLString: false,
        duration: dynamic ? duration : info.displayType === 'MESSAGE_BOX' ? 0 : info.displayType === 'MESSAGE' ? 4000 : 6000
      }
      if (info.displayType === 'MESSAGE_BOX') {
        // 居中消息不限制待展示条数，关闭当前弹窗后按接收顺序展示下一条。
        pending.push(options)
        pump()
        return
      }
      // 超出当前形式的5个显示名额后排队，不挤掉已有通知。
      cards.push({ options, displayType: info.displayType, summary: base.messageType === WebSocketMessageType.SYSTEM_NOTICE })
      pumpCards()
    },
    clear() {
      generation++
      pending.length = 0
      cards.length = 0
      const previous = activeDialog
      activeDialog = undefined
      clearTimeout(previous?.timer)
      previous?.handle?.close()
      for (const marker of [...handles]) marker.handle?.close()
      handles.clear()
      seen.clear()
    }
  }
}
