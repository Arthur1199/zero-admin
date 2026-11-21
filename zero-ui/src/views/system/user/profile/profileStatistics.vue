<template>
  <div class="profile-statistics">
    <el-card class="operation-card" role="region" aria-label="我的操作概览">
      <template #header>
        <div class="statistics-heading">
          <h2>我的操作概览</h2>
          <span>近 7 天</span>
        </div>
      </template>
      <profile-session />
      <div v-loading="operationLoading" class="statistics-body operation-body">
        <div v-if="operationFailed" class="statistics-state">
          <span>操作统计读取失败</span>
          <el-button link type="primary" @click="loadOperations">重新加载</el-button>
        </div>
        <template v-else-if="operations">
          <dl class="statistics-metrics">
            <div v-for="metric in operationMetrics" :key="metric.label">
              <dt>{{ metric.label }}</dt>
              <dd>{{ metric.value }}</dd>
            </div>
          </dl>
          <h3>每日操作量</h3>
          <ul class="operation-trend">
            <li v-for="day in operations.days" :key="day.day">
              <span>{{ day.total }}</span>
              <span class="operation-track" aria-hidden="true">
                <span class="operation-bar" :style="{ height: (day.total / maxDailyOperations * 100) + '%' }"></span>
              </span>
              <time :datetime="day.day">{{ day.day.slice(5) }}</time>
            </li>
          </ul>
          <div class="recent-operations">
            <h3>最近操作</h3>
            <p v-if="!operations.recent.length" class="statistics-state">近 7 天暂无操作记录</p>
            <ul v-else class="statistics-list operation-list">
              <li v-for="(operation, index) in operationRows" :key="index">
                <span class="operation-text" :title="operation.module">{{ operation.module }}</span>
                <span class="operation-text statistics-meta" :title="operation.action">{{ operation.action }}</span>
                <el-tag class="operation-result" size="small" :type="operation.status === 0 ? 'success' : operation.status === 1 ? 'danger' : 'info'">
                  {{ operation.status === 0 ? "成功" : operation.status === 1 ? "失败" : "未知" }}
                </el-tag>
                <time class="statistics-meta" :datetime="operation.operTime">{{ formatDateTime(operation.operTime) }}</time>
              </li>
            </ul>
          </div>
        </template>
      </div>
    </el-card>

    <el-card v-if="canReadMemos === true || memoFailed" class="memo-card" role="region" aria-label="我的备忘录">
      <template #header>
        <div class="statistics-heading">
          <h2>我的备忘录</h2>
          <router-link v-if="canReadMemos && $router.hasRoute('Memo')" :to="{ name: 'Memo' }">
            查看备忘录
          </router-link>
        </div>
      </template>
      <div v-loading="memoLoading" class="statistics-body memo-body">
        <div v-if="memoFailed" class="statistics-state">
          <span>备忘录统计读取失败</span>
          <el-button link type="primary" @click="loadMemos">重新加载</el-button>
        </div>
        <template v-else-if="memos">
          <dl class="statistics-metrics">
            <div v-for="metric in memoMetrics" :key="metric.label">
              <dt>{{ metric.label }}</dt>
              <dd>{{ metric.value }}</dd>
            </div>
          </dl>
          <h3>最近更新</h3>
          <p v-if="!memos.recent.length" class="statistics-state">暂无备忘录</p>
          <ul v-else class="statistics-list memo-list">
            <li v-for="(memo, index) in memos.recent" :key="index">
              <span class="memo-title" :title="memo.title">{{ memo.title }}</span>
              <time class="statistics-meta" :datetime="memo.updatedAt">{{ formatDateTime(memo.updatedAt) }}</time>
            </li>
          </ul>
        </template>
      </div>
    </el-card>
  </div>
</template>

<script>
/**
 * 个人操作与备忘录统计面板；分别读取各类统计，失败和加载状态各自维护。
 */
import { getProfileMemoStatistics, getProfileOperationStatistics } from "@/api/system/user"
import profileSession from "./profileSession"

export default {
  name: "ProfileStatistics",
  components: { profileSession },
  dicts: ["sys_oper_type"],
  data() {
    return {
      operations: null,
      operationLoading: false,
      operationFailed: false,
      memos: null,
      canReadMemos: null,
      memoLoading: false,
      memoFailed: false
    }
  },
  computed: {
    maxDailyOperations() {
      return Math.max(1, ...this.operations.days.map(day => day.total))
    },
    operationMetrics() {
      return [
        { label: "操作总数", value: this.operations.total },
        { label: "成功次数", value: this.operations.succeeded },
        { label: "失败次数", value: this.operations.failed }
      ]
    },
    operationRows() {
      return this.operations.recent.map(operation => ({
        ...operation,
        module: operation.title || "未命名模块",
        action: this.selectDictLabel(this.dict.type.sys_oper_type ?? [], operation.businessType)
      }))
    },
    memoMetrics() {
      const { counts } = this.memos
      return [
        { label: "备忘录总数", value: counts.total },
        { label: "私有数量", value: counts.privateCount },
        { label: "公开数量", value: counts.publicCount },
        { label: "近 7 天新增", value: counts.newCount }
      ]
    }
  },
  created() {
    this.loadOperations()
    this.loadMemos()
  },
  methods: {
    async loadOperations() {
      if (this.operationLoading) return
      this.operationLoading = true
      this.operationFailed = false
      try {
        const { data } = await getProfileOperationStatistics()
        this.operationFailed = !Array.isArray(data?.days) || !Array.isArray(data?.recent)
        this.operations = this.operationFailed ? null : data
      } catch {
        this.operationFailed = true
      } finally {
        this.operationLoading = false
      }
    },
    async loadMemos() {
      if (this.memoLoading) return
      this.memoLoading = true
      this.memoFailed = false
      this.canReadMemos = null
      this.memos = null
      try {
        const { data } = await getProfileMemoStatistics()
        if (data?.canReadMemos === false) {
          this.canReadMemos = false
        } else if (data?.canReadMemos === true && data.statistics?.counts && Array.isArray(data.statistics.recent)) {
          this.canReadMemos = true
          this.memos = data.statistics
        } else {
          this.memoFailed = true
        }
      } catch {
        this.memoFailed = true
      } finally {
        this.memoLoading = false
      }
    },
    formatDateTime(value) {
      return value?.replace("T", " ").slice(0, 19) || "—"
    }
  }
}
</script>

<style scoped lang="scss">
// 组件局部布局与展示样式；跨页面主题及 body 弹层规则在 assets/styles 中统一维护。
.profile-statistics {
  --statistics-gap: 20px;
  display: grid;
  // 宽屏最多两列，备忘录隐藏时保留原列宽。
  grid-template-columns: repeat(auto-fill, minmax(min(100%, max(310px, (100% - var(--statistics-gap)) / 2)), 1fr));
  align-items: start;
  gap: var(--statistics-gap);
}

.profile-statistics > * {
  min-width: 0;
}

.operation-card :deep(.el-card__body) {
  padding-block: 12px;
}

@media (width > 1050px) {
  @container profile-layout (min-width: 1000px) {
    .profile-statistics {
      // 由左侧卡片决定整行高度，统计内容较多时在卡片内部滚动。
      contain: size;
      align-self: stretch;
      align-items: stretch;
      grid-template-rows: minmax(0, 1fr);
    }

    .profile-statistics > .el-card {
      display: flex;
      flex-direction: column;
      min-height: 0;
    }

    .profile-statistics :deep(.el-card__header) {
      flex-shrink: 0;
    }

    .profile-statistics :deep(.el-card__body) {
      min-height: 0;
      overflow: auto;
    }

    .operation-card :deep(.el-card__body),
    .memo-card :deep(.el-card__body),
    .operation-body,
    .memo-body,
    .memo-list {
      display: flex;
      flex-direction: column;
    }

    .operation-card :deep(.el-card__body) {
      // 让最后一条的中心与左侧底行接近，保留各主题的卡片内边距。
      padding-bottom: calc(var(--el-card-padding) + 6px);
    }

    .operation-body,
    .operation-trend,
    .memo-body,
    .memo-list,
    .memo-list > li {
      flex: 1 0 auto;
    }
  }
}

.statistics-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 6px 12px;
}

h2 {
  margin: 0;
  font-size: 16px;
  line-height: 24px;
}

h3 {
  margin: 22px 0 8px;
  font-size: 14px;
}

.statistics-heading a {
  text-decoration: underline;
  text-underline-offset: 3px;
}

.statistics-body {
  min-height: 120px;
}

.operation-body h3 {
  margin: 7px 0 4px;
}

.memo-body h3 {
  margin: 12px 0 4px;
}

.recent-operations {
  padding-top: 13px;
}

.operation-body .recent-operations h3 {
  margin-top: 0;
}

.operation-body .statistics-metrics > div {
  padding-block: 4px;
}

.statistics-metrics {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(min(100%, 76px), 1fr));
  gap: 12px;
  margin: 0;
}

.statistics-metrics > div {
  padding: 8px 0;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.statistics-metrics dd {
  margin: 6px 0 0;
  font-size: 24px;
  font-weight: 600;
  overflow-wrap: anywhere;
}

.operation-trend,
.statistics-list {
  padding: 0;
  margin: 0;
  list-style: none;
}

.operation-trend {
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
  gap: 4px;
  font-size: 12px;
  text-align: center;
}

.operation-trend li {
  display: grid;
  grid-template-rows: auto minmax(32px, 1fr) auto;
  justify-items: center;
  gap: 4px;
  overflow-wrap: anywhere;
}

.operation-track {
  display: flex;
  align-items: end;
  justify-content: center;
  width: 100%;
  min-height: 32px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.operation-bar {
  width: min(18px, 80%);
  background: currentColor;
  opacity: .65;
  border-radius: 3px 3px 0 0;
}

.statistics-list li {
  padding: 9px 0;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.statistics-list li:last-child {
  border-bottom: 0;
}

.memo-list > li {
  display: grid;
  grid-template-columns: minmax(0, 1fr) max-content;
  align-items: center;
  gap: 8px;
  padding: 5px 0;
  white-space: nowrap;
}

.memo-title {
  overflow: hidden;
  text-overflow: ellipsis;
}

.statistics-meta {
  font-size: 12px;
}

.operation-list > li {
  display: grid;
  grid-template-columns: minmax(0, 1fr) fit-content(24%) max-content max-content;
  align-items: center;
  gap: 6px;
  padding: 2px 0;
  white-space: nowrap;
}

.operation-text {
  overflow: hidden;
  text-overflow: ellipsis;
}

.operation-list .operation-result {
  margin: 0;
  padding-inline: 6px;
}

.statistics-state {
  margin: 12px 0;
}
</style>
