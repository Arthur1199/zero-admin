<template>
  <section class="profile-session" aria-label="当前会话">
    <div class="session-heading">
      <h3>当前会话</h3>
      <el-button link type="primary" :disabled="loading" @click="loadSession">
        {{ failed ? "重试" : "刷新" }}
      </el-button>
    </div>
    <p v-if="failed" class="session-message" role="status">会话信息读取失败</p>
    <dl class="session-fields" :aria-busy="loading">
      <div v-for="field in fields" :key="field.label">
        <dt>{{ field.label }}</dt>
        <dd :title="field.title || field.value">{{ field.value }}</dd>
      </div>
    </dl>
  </section>
</template>

<script>
/**
 * 当前账号会话统计；维护会话时长刷新与停止会话操作，卸载时释放计时器。
 */
import { getProfileSessionStatistics } from "@/api/system/user"

export default {
  name: "ProfileSession",
  data() {
    return {
      session: null,
      loading: false,
      failed: false,
      requestId: 0,
      receivedAt: 0,
      onlineSeconds: null,
      timer: null
    }
  },
  computed: {
    duration() {
      if (this.onlineSeconds === null) return "—"
      const days = Math.floor(this.onlineSeconds / 86400)
      const hours = Math.floor(this.onlineSeconds / 3600) % 24
      const minutes = Math.floor(this.onlineSeconds / 60) % 60
      const seconds = this.onlineSeconds % 60
      const time = [hours, minutes, seconds].map(value => String(value).padStart(2, "0")).join(":")
      return days ? `${days} 天 ${time}` : time
    },
    fields() {
      return [
        { label: "当前在线时长", value: this.duration, title: this.session && this.onlineSeconds === null ? "重新登录后开始统计" : "" },
        { label: "当前操作IP", value: this.session?.ip || "—" },
        { label: "操作地址", value: this.session?.location || "—" },
        { label: "浏览器", value: this.session?.browser || "—" },
        { label: "操作系统", value: this.session?.os || "—" }
      ]
    }
  },
  mounted() {
    this.loadSession()
  },
  activated() {
    this.startTimer()
    this.loadSession()
  },
  deactivated() {
    this.stopSession()
  },
  beforeUnmount() {
    this.stopSession()
  },
  methods: {
    async loadSession() {
      if (this.loading) return
      const requestId = ++this.requestId
      this.loading = true
      this.failed = false
      try {
        const { data } = await getProfileSessionStatistics()
        if (requestId !== this.requestId) return
        if (!data || typeof data !== "object" || (!("onlineSeconds" in data) && !("ip" in data))) {
          this.failed = true
          return
        }
        this.session = data
        this.receivedAt = performance.now()
        this.startTimer()
      } catch {
        if (requestId === this.requestId) this.failed = true
      } finally {
        if (requestId === this.requestId) this.loading = false
      }
    },
    updateDuration() {
      const seconds = this.session?.onlineSeconds
      this.onlineSeconds = Number.isSafeInteger(seconds) && seconds >= 0
        ? seconds + Math.floor((performance.now() - this.receivedAt) / 1000)
        : null
    },
    startTimer() {
      this.updateDuration()
      if (this.onlineSeconds === null) {
        clearInterval(this.timer)
        this.timer = null
      } else if (this.timer === null) {
        this.timer = setInterval(this.updateDuration, 1000)
      }
    },
    stopSession() {
      // 忽略离开个人中心后返回的旧响应，避免覆盖重新进入时取得的信息。
      this.requestId++
      this.loading = false
      clearInterval(this.timer)
      this.timer = null
    }
  }
}
</script>

<style scoped lang="scss">
// 组件局部布局与展示样式；跨页面主题及 body 弹层规则在 assets/styles 中统一维护。
.profile-session {
  padding-bottom: 6px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.session-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.session-heading h3 {
  margin: 0;
  font-size: 14px;
}

.session-fields {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(min(100%, 125px), 1fr));
  gap: 4px 10px;
  margin: 4px 0 0;
  font-size: 12px;
  line-height: 1.35;
}

.session-fields dd {
  margin: 2px 0 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-variant-numeric: tabular-nums;
}

.session-message {
  margin: 4px 0;
  font-size: 12px;
}
</style>
