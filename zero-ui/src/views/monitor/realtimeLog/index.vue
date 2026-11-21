<template>
  <div class="app-container realtime-log">
    <div class="log-heading">
      <div><h2>实时日志</h2><p>{{ sourceMode === 'console-file' ? '完整进程控制台 · stdout / stderr / JVM原生输出 · 原始文本' : '当前管理后台进程 · Logback应用日志 · 最近日志与实时增量' }}</p></div>
      <el-tag :type="statusType" role="status">{{ statusText }}</el-tag>
    </div>
    <div class="log-toolbar">
      <el-select v-model="level" :disabled="sourceMode === 'console-file'" :placeholder="sourceMode === 'console-file' ? '全部原始输出' : '全部级别'" aria-label="日志级别" class="log-level">
        <el-option label="全部级别" value="" />
        <el-option v-for="item in levels" :key="item" :label="item" :value="item" />
      </el-select>
      <el-input v-model="keyword" placeholder="过滤当前保留日志" clearable class="log-keyword" aria-label="日志关键字" />
      <el-button @click="togglePause">{{ paused ? '继续' : '暂停' }}</el-button>
      <el-button @click="clearDisplay">清空当前显示</el-button>
      <el-button v-if="halted" type="primary" @click="retry">重新连接</el-button>
      <el-checkbox v-model="follow" @change="followChanged">自动滚动</el-checkbox>
      <el-button link type="primary" @click="goTop">回到顶部</el-button>
      <el-button link type="primary" @click="goBottom">回到底部</el-button>
    </div>
    <el-alert v-if="notice" :title="notice" type="warning" :closable="false" show-icon class="log-notice" />
    <div class="log-summary">保留 {{ events.length }} 条，筛选后 {{ filtered.length }} 条 · 页面最多保留 2000 条 / 5MB · 筛选仅作用于当前保留内容</div>
    <div ref="viewport" class="log-viewport" :class="{ 'has-notice': !!notice }" tabindex="0" aria-label="实时日志内容" @scroll="onScroll">
      <pre v-for="chunk in chunks" :key="chunk.key" class="log-chunk">{{ chunk.text }}</pre>
      <div v-if="!filtered.length" class="log-empty">{{ events.length ? '没有符合筛选条件的日志' : '暂无日志，等待服务输出' }}</div>
    </div>
    <p class="log-footnote">{{ sourceMode === 'console-file' ? '原始控制台不做脱敏或补换行，可能包含密码、Token等敏感内容，请严格控制查看权限。' : '明确的凭证字段已脱敏，日志仍可能包含业务信息，请谨慎分配查看权限。' }}清空显示不会删除服务端日志。</p>
  </div>
</template>

<script>
/**
 * 当前后台进程的实时日志查看器；维护游标轮询、可见性暂停、缓冲及跟随滚动，清屏只清理本地显示。
 */
import { listRealtimeLogs } from '@/api/monitor/realtimeLog'

const MAX_EVENTS = 2000
const MAX_BYTES = 5 * 1024 * 1024
const POLL_MS = 1000
const MAX_RETRY_MS = 30000
const encoder = new TextEncoder()

export default {
  name: 'RealtimeLog',
  data() {
    return {
      events: [], level: '', keyword: '', levels: ['TRACE', 'DEBUG', 'INFO', 'WARN', 'ERROR'],
      sourceMode: '', processId: '', cursor: null, paused: false, follow: true, active: false,
      busy: false, halted: false, state: 'waiting', notice: '', timer: null,
      generation: 0, failures: 0, retainedBytes: 0
    }
  },
  computed: {
    filtered() {
      const keyword = this.keyword.toLowerCase()
      return this.events.filter(event => (!this.level || event.level === this.level) &&
        (!keyword || event.text.toLowerCase().includes(keyword)))
    },
    // 分块渲染纯文本，避免每行堆栈生成DOM；容量另受事件数和字节限制。
    chunks() {
      const result = []
      for (let index = 0; index < this.filtered.length; index += 40) {
        const group = this.filtered.slice(index, index + 40)
        result.push({ key: group[0].key, text: group.map(event => event.raw || event.text.endsWith('\n') ? event.text : event.text + '\n').join('') })
      }
      return result
    },
    statusText() {
      if (this.halted) return '已停止，请检查登录状态或权限'
      if (this.paused) return '已暂停'
      if (!this.active) return '页面已隐藏'
      return { waiting: '正在连接', connected: '实时读取中', retrying: '连接失败，等待重试' }[this.state]
    },
    statusType() { return this.halted || this.state === 'retrying' ? 'danger' : this.paused ? 'warning' : 'success' }
  },
  mounted() {
    document.addEventListener('visibilitychange', this.visibilityChanged)
    this.activate()
  },
  activated() { this.activate() },
  deactivated() { this.stop() },
  beforeUnmount() {
    this.stop()
    document.removeEventListener('visibilitychange', this.visibilityChanged)
  },
  watch: {
    level() { this.followChanged() },
    keyword() { this.followChanged() }
  },
  methods: {
    activate() {
      if (this.active || document.hidden) return
      this.active = true
      this.schedule(0)
    },
    stop() {
      this.active = false
      this.generation++
      clearTimeout(this.timer)
      this.timer = null
    },
    visibilityChanged() { document.hidden ? this.stop() : this.activate() },
    schedule(delay) {
      clearTimeout(this.timer)
      if (this.active && !this.paused && !this.halted && !this.busy) this.timer = setTimeout(this.poll, delay)
    },
    async poll() {
      if (!this.active || this.paused || this.halted || this.busy) return
      this.busy = true
      const generation = this.generation
      let delay = POLL_MS
      let denied = false
      try {
        const params = { limit: 500 }
        if (this.processId && this.cursor !== null) Object.assign(params, { processId: this.processId, after: this.cursor })
        const response = await listRealtimeLogs(params, {
          // 全局拦截器保留原提示；在其将业务错误转为字符串前观察状态，阻止认证失败后反复轮询。
          transformResponse: [raw => {
            const data = typeof raw === 'string' ? JSON.parse(raw) : raw
            denied = data?.code === 401 || data?.code === 403
            return data
          }]
        })
        if (generation !== this.generation) return
        const data = response.data
        if (!data || !Array.isArray(data.events) || !data.processId || !Number.isSafeInteger(data.nextCursor)) throw new Error('日志响应格式异常')
        this.sourceMode = data.sourceMode || 'logback'
        if (this.sourceMode === 'console-file') this.level = ''
        if (data.reset || (this.processId && this.processId !== data.processId)) {
          this.clearDisplay()
          this.notice = this.sourceMode === 'console-file' ? '控制台文件已切换或重置，已重新读取最近输出。' : '服务已重启，已切换到新进程最近日志。'
        } else if (data.gap) this.notice = this.sourceMode === 'console-file' ? '已按回看容量读取最近控制台输出，较早内容仍保留在控制台文件中。' : '部分日志已被服务端缓冲覆盖，当前显示可读取的剩余日志。'
        this.appendEvents(data.events, data.processId)
        this.processId = data.processId
        this.cursor = data.nextCursor
        this.state = 'connected'
        this.failures = 0
        delay = data.hasMore ? 100 : POLL_MS
        this.followChanged()
      } catch (error) {
        if (generation !== this.generation) return
        if (denied || [401, 403].includes(error?.response?.status) || error === 'cancel' || error === 'close' || error?.code < 0) {
          this.halted = true
        } else {
          this.state = 'retrying'
          this.failures++
          delay = Math.min(MAX_RETRY_MS, POLL_MS * 2 ** Math.min(this.failures, 5))
        }
      } finally {
        this.busy = false
        // 旧响应不写入已离开的页面；重新激活时仍等待原请求结束，保证不重叠。
        this.schedule(generation === this.generation ? delay : 0)
      }
    },
    appendEvents(incoming, processId) {
      const last = this.events.at(-1)?.sequence || 0
      for (const event of incoming) {
        if (event.sequence <= last) continue
        const entry = { ...event, key: `${processId}:${event.sequence}` }
        entry.bytes = encoder.encode(JSON.stringify(entry)).length
        this.events.push(entry)
        this.retainedBytes += entry.bytes
      }
      let removed = false
      while (this.events.length && (this.events.length > MAX_EVENTS || this.retainedBytes > MAX_BYTES)) {
        this.retainedBytes -= this.events.shift().bytes
        removed = true
      }
      if (removed && !this.notice) this.notice = '页面保留量已达上限，较早日志已移出当前显示。'
    },
    togglePause() {
      this.paused = !this.paused
      this.generation++
      clearTimeout(this.timer)
      if (!this.paused) this.schedule(0)
    },
    retry() { this.halted = false; this.failures = 0; this.state = 'waiting'; this.schedule(0) },
    clearDisplay() { this.events = []; this.retainedBytes = 0; this.notice = '' },
    onScroll() {
      const element = this.$refs.viewport
      if (element && element.scrollHeight - element.scrollTop - element.clientHeight > 32) this.follow = false
    },
    followChanged() {
      // 等待日志渲染后再次检查用户选择，防止已排队的自动跟随覆盖“回到顶部”。
      if (this.follow) this.$nextTick(() => { if (this.follow) this.scrollBottom() })
    },
    goTop() {
      this.follow = false
      this.$nextTick(() => {
        const element = this.$refs.viewport
        if (element && !this.follow) element.scrollTo({ top: 0, left: element.scrollLeft, behavior: 'auto' })
      })
    },
    goBottom() { this.follow = true; this.followChanged() },
    scrollBottom() {
      const element = this.$refs.viewport
      if (element) element.scrollTo({ top: Math.max(0, element.scrollHeight - element.clientHeight), left: element.scrollLeft, behavior: 'auto' })
    }
  }
}
</script>

<style scoped lang="scss">
// 组件局部布局与展示样式；跨页面主题及 body 弹层规则在 assets/styles 中统一维护。
.realtime-log {
  color: var(--zero-fixed-table-text, #606266);
  .log-heading { display: flex; align-items: center; justify-content: space-between; gap: 16px; flex-wrap: wrap; }
  h2 { margin: 0 0 8px; color: var(--zero-fixed-table-header-text, #303133); }
  p { margin: 0 0 18px; font-size: 13px; line-height: 1.6; }
  .log-toolbar { display: flex; align-items: center; flex-wrap: wrap; gap: 12px; margin-bottom: 14px; }
  .log-toolbar .el-button + .el-button { margin-left: 0; }
  .log-level { width: 140px; }
  .log-keyword { width: 260px; max-width: 100%; }
  .log-notice { margin-bottom: 12px; }
  .log-summary { font-size: 12px; margin-bottom: 10px; line-height: 1.6; }
  .log-viewport {
    height: clamp(320px, calc(100dvh - 330px), 1200px); overflow: auto; overflow-anchor: none; scroll-behavior: auto;
    &.has-notice { height: clamp(320px, calc(100dvh - 350px), 1200px); }
    border: 1px solid var(--zero-fixed-table-border, #ebeef5); border-radius: 6px;
    background: var(--zero-fixed-table-panel-bg, #fff); padding: 14px;
    scrollbar-gutter: stable;
  }
  .log-chunk { display: inline; margin: 0; white-space: pre; font: 13px/1.65 Consolas, 'Courier New', monospace; color: inherit; }
  .log-empty { padding: 36px 0; text-align: center; }
  .log-footnote { margin-top: 12px; margin-bottom: 0; }
}
</style>
