<template>
  <div class="app-container statistics-page">
    <div class="statistics-heading">
      <div><h2>运行统计</h2><p>管理后台的访问、登录与接口表现</p></div>
      <div class="refresh-controls">
        <span>30秒自动刷新</span><el-switch v-model="autoRefresh" aria-label="自动刷新" />
        <el-button :loading="loading" @click="refresh">刷新</el-button>
      </div>
    </div>
    <el-alert v-if="healthMessage" :title="healthMessage" :type="healthType" :closable="false" show-icon class="health-alert" />
    <div class="collection-meta">
      <span>首个请求分钟：{{ formatTime(overview.health?.collectedSince) }}</span>
      <span>最近入库：{{ formatTime(overview.health?.lastConsumedAt) }}</span>
      <span>页面更新：{{ updatedAt || '—' }}</span>
      <span>数据范围：zero-admin自身及代理接口</span>
    </div>
    <div class="metric-grid">
      <el-card v-for="card in cards" :key="card.label" shadow="never" class="metric-card">
        <div class="metric-label">{{ card.label }}</div><div class="metric-value">{{ card.value }}<small>{{ card.unit }}</small></div>
        <div class="metric-note">{{ card.note }}</div>
      </el-card>
    </div>
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" @submit.prevent="handleQuery">
        <el-form-item label="历史范围"><el-radio-group v-model="period" :disabled="loading || tableLoading" @change="changePeriod"><el-radio-button value="today">今日</el-radio-button><el-radio-button value="yesterday">昨日</el-radio-button><el-radio-button value="7">近7天</el-radio-button><el-radio-button value="30">近30天</el-radio-button></el-radio-group></el-form-item>
        <el-form-item><el-date-picker v-model="dateRange" :disabled="loading || tableLoading" type="daterange" value-format="YYYY-MM-DD" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" :clearable="false" @change="period = ''" /></el-form-item>
        <el-form-item><el-button type="primary" :loading="loading" @click="handleQuery">查询</el-button></el-form-item>
      </el-form>
      <div class="range-note">历史筛选影响下方图表及接口明细；上方为今日指标和当前在线。在线曲线是采样值，缺口不代表零。</div>
    </el-card>
    <div class="chart-grid" v-loading="loading">
      <el-card shadow="never"><template #header>API调用趋势 <span class="header-note">{{ minute ? '按分钟' : '按日' }}</span></template><statistics-chart :options="apiChart" :empty="!apiRows.length" label="API成功和失败调用趋势" /></el-card>
      <el-card shadow="never"><template #header>在线人数趋势 <span class="header-note">采样均值与峰值</span></template><statistics-chart :options="onlineChart" :empty="!onlineRows.length" label="在线与活跃人数采样趋势" /></el-card>
      <el-card shadow="never"><template #header>登录趋势 <span class="header-note">按日</span></template><statistics-chart :options="loginChart" :empty="!loginRows.length" label="登录成功与失败趋势" /></el-card>
      <el-card shadow="never"><template #header>模块调用占比</template><statistics-chart :options="moduleChart" :empty="!distribution.length" label="模块调用占比" /></el-card>
      <el-card shadow="never"><template #header>失败分类</template><statistics-chart :options="failureChart" :empty="!failureItems.length" label="API失败分类分布" /></el-card>
      <el-card shadow="never"><template #header><div class="ranking-header"><span>接口排行 TOP10</span><el-radio-group v-model="rankingType" size="small"><el-radio-button value="calls">调用量</el-radio-button><el-radio-button value="slow">平均耗时</el-radio-button><el-radio-button value="failed">失败数</el-radio-button></el-radio-group></div></template>
        <p class="range-note">{{ rankingType === 'slow' ? '至少10次调用；单位ms' : '单位：次' }}</p>
        <statistics-chart :options="rankingChart" :empty="!currentRanking.length" label="接口排行前十" />
      </el-card>
    </div>
    <el-card shadow="never" class="detail-card">
      <template #header>接口统计明细</template>
      <el-form :inline="true" @submit.prevent="searchList"><el-form-item label="接口"><el-input v-model="keyword" placeholder="接口路径或名称" clearable maxlength="100" @keyup.enter="searchList" /></el-form-item><el-form-item><el-button type="primary" :disabled="loading || tableLoading" @click="searchList">查询</el-button><el-button :disabled="loading || tableLoading" @click="resetList">重置</el-button></el-form-item></el-form>
      <el-table v-loading="tableLoading" :data="rows" empty-text="所选时段暂无采集数据">
        <el-table-column label="接口名称" prop="name" min-width="230" :show-overflow-tooltip="true" />
        <el-table-column label="请求方式" prop="method" width="100" />
        <el-table-column label="接口路径" prop="route" min-width="260" :show-overflow-tooltip="true" />
        <el-table-column label="模块" prop="module" width="120" />
        <el-table-column label="调用次数" prop="total" width="110" align="right" />
        <el-table-column label="成功次数" prop="success" width="110" align="right" />
        <el-table-column label="失败次数" prop="failed" width="110" align="right" />
        <el-table-column label="成功率" width="110" align="right"><template #default="scope">{{ number(scope.row.successRate) }}%</template></el-table-column>
        <el-table-column label="平均耗时(ms)" width="140" align="right"><template #default="scope">{{ number(scope.row.avgMs) }}</template></el-table-column>
        <el-table-column label="最大耗时(ms)" width="140" align="right"><template #default="scope">{{ number(scope.row.maxMs) }}</template></el-table-column>
        <el-table-column label="慢请求数" prop="slow" width="110" align="right" />
      </el-table>
      <pagination v-show="total > 0" :total="total" v-model:page="pageNum" v-model:limit="pageSize" :auto-scroll="false" @pagination="loadList" />
    </el-card>
  </div>
</template>

<script>
import StatisticsChart from './StatisticsChart.vue'
import { getStatisticsOverview, getStatisticsTrend, getStatisticsDistribution, getStatisticsRanking, listStatistics } from '@/api/monitor/statistics'

// 日历运算使用UTC作为无时区日期容器，标签始终表达北京时间。
function dateText(date) { return date.toISOString().slice(0, 10) }
function beijingToday(now = Date.now()) { return new Intl.DateTimeFormat('en-CA', { timeZone: 'Asia/Shanghai', year: 'numeric', month: '2-digit', day: '2-digit' }).format(new Date(now)) }
function presetRange(period, now) {
  const end = new Date(beijingToday(now) + 'T00:00:00Z'), start = new Date(end)
  if (period === 'yesterday') { end.setUTCDate(end.getUTCDate() - 1); start.setUTCDate(start.getUTCDate() - 1) }
  else if (period !== 'today') start.setUTCDate(start.getUTCDate() - Number(period) + 1)
  return [dateText(start), dateText(end)]
}
function buckets(range, minute) {
  if (!range || range.length !== 2) return []
  const start = Date.parse(range[0] + 'T00:00:00Z'), end = Date.parse(range[1] + 'T00:00:00Z') + 86400000
  const result = []
  for (let at = start; at < end; at += minute ? 60000 : 86400000) result.push(new Date(at).toISOString().slice(0, minute ? 16 : 10).replace('T', ' '))
  return result
}
function lines(labels, series) { return { xAxis: { type: 'category', data: labels, boundaryGap: false, axisLabel: { showMinLabel: false, showMaxLabel: false, formatter: value => value.slice(5) } }, yAxis: { type: 'value', min: 0 }, dataZoom: labels.length > 120 ? [{ type: 'inside' }] : [], series: series.map(item => ({ type: 'line', smooth: .2, showSymbol: true, symbolSize: 4, connectNulls: false, ...item })) } }
function donut(items) { return { tooltip: { trigger: 'item', renderMode: 'richText' }, legend: { top: undefined, bottom: 0, icon: 'circle', itemWidth: 8, itemHeight: 8 }, series: [{ type: 'pie', radius: ['45%', '70%'], center: ['50%', '48%'], label: { show: false }, data: items }] } }

export default {
  name: 'Statistics',
  components: { StatisticsChart },
  data() {
    return { overview: {}, trends: [], distribution: [], rankings: {}, rows: [], total: 0, pageNum: 1, pageSize: 20, keyword: '', appliedKeyword: '', period: 'today', appliedPeriod: 'today', dateRange: [beijingToday(), beijingToday()], appliedRange: [beijingToday(), beijingToday()], rankingType: 'calls', autoRefresh: true, loading: false, tableLoading: false, refreshFailed: false, listFailed: false, now: Date.now(), serverOffset: 0, updatedAt: '', timer: null, disposed: false, isActive: true, listVersion: 0 }
  },
  computed: {
    failed() { return this.refreshFailed || this.listFailed },
    currentTime() { return this.now + this.serverOffset },
    minute() { return (Date.parse(this.appliedRange[1]) - Date.parse(this.appliedRange[0])) / 86400000 < 7 },
    apiRows() { return this.trends.filter(row => row.kind === 'api') },
    onlineRows() { return this.trends.filter(row => row.kind === 'online') },
    loginRows() { return this.trends.filter(row => row.kind === 'login') },
    cards() {
      const m = this.overview.metrics || {}, h = this.overview.health || {}, s = this.overview.status || {}
      const available = this.overview.databaseAvailable === true
      const value = v => available ? this.number(v) : '—'
      const onlineFresh = s.activeIndexAvailable !== false && h.sampledAt && this.currentTime - this.asTimestamp(h.sampledAt) <= Math.max(90000, (s.sampleMillis || 30000) * 3)
      return [
        { label: '当前在线人数', value: onlineFresh ? value(h.onlineUsers) : '—', unit: '人', note: '有效会话去重 · 会话数 ' + (onlineFresh ? value(h.onlineSessions) : '—') + ' · 近5分钟活跃 ' + (onlineFresh ? value(h.recentActive) : '—') },
        { label: '今日成功登录', value: value(m.loginSuccess), unit: '次', note: '登录人数 ' + value(m.loginUsers) + ' · 失败 ' + value(m.loginFailed) + ' · 尝试 ' + value(m.loginAttempts) },
        { label: '今日API调用', value: value(m.total), unit: '次', note: '成功 ' + value(m.success) + ' · 失败 ' + value(m.failed) + ' · 活跃用户 ' + value(m.activeUsers) },
        { label: '今日API成功率', value: value(m.successRate), unit: '%', note: 'HTTP状态与业务结果同时判定' },
        { label: '今日平均耗时', value: value(m.avgMs), unit: 'ms', note: '最大耗时 ' + value(m.maxMs) + ' ms · 包含失败请求' },
        { label: '今日慢请求', value: value(m.slow), unit: '次', note: '阈值 ≥ ' + (s.slowMillis || 1000) + ' ms · 分钟峰值 ' + value(m.peakMinute) + '次' }
      ]
    },
    healthMessage() {
      if (this.failed) return '部分统计查询失败，当前保留上次成功结果，请重试。'
      const s = this.overview.status, h = this.overview.health || {}
      if (!s) return '正在读取采集状态'
      if (s.switchAvailable === false) return '统计开关读取失败，暂按本实例最近有效值运行，请检查后台参数缓存及数据库。'
      if (!s.enabled) return '统计采集未启用，请在后台参数设置中将sys.statistics.enabled设为true；首次使用前先执行统计SQL。'
      if (!this.overview.databaseAvailable) return '统计数据库不可用或尚未初始化，指标暂不可用。'
      if (s.lostSinceStart > 0) return '本实例启动后有 ' + s.lostSinceStart + ' 条事件未能持久化，统计存在缺口。'
      if (s.activeIndexAvailable === false) return '近期活跃人数读取失败，在线采样暂不可用；API汇总继续处理。'
      if (s.redisAvailable === false || s.consumerGroupAvailable === false || s.lastErrorAt > s.lastSuccessAt) return '统计采集或消费异常，后台正在重试；显示的数据可能延迟。'
      if (Number(h.inboxPending || 0) + Number(s.streamPending || 0) + Number(s.streamLag || 0) > 0) return '统计事件正在处理，数据有短暂延迟。'
      if (!h.sampledAt || this.currentTime - this.asTimestamp(h.sampledAt) > Math.max(90000, (s.sampleMillis || 30000) * 3)) return '在线人数采样缺失或已过期，当前在线值暂不可用。'
      return '采集运行中 · 异步汇总，积压时更新可能延迟 · 故障计数仅代表当前实例，不能证明历史无缺口'
    },
    healthType() { return this.failed || this.overview.status?.switchAvailable === false || !this.overview.status?.enabled || !this.overview.databaseAvailable || this.overview.status?.lostSinceStart > 0 || this.overview.status?.redisAvailable === false || this.overview.status?.consumerGroupAvailable === false || this.overview.status?.activeIndexAvailable === false || this.overview.status?.lastErrorAt > this.overview.status?.lastSuccessAt ? 'warning' : 'info' },
    apiChart() { return this.trendOptions(this.apiRows, [['success', '成功'], ['failed', '失败']]) },
    onlineChart() { return this.trendOptions(this.onlineRows, [['onlineUsers', '在线人数均值'], ['active', '近5分钟活跃均值'], ['peak', '在线采样峰值']]) },
    loginChart() {
      const labels = buckets(this.appliedRange, false), by = new Map(this.loginRows.map(row => [row.bucket, row]))
      return { xAxis: { type: 'category', data: labels }, yAxis: { type: 'value', min: 0 }, series: [['loginSuccess', '成功'], ['loginFailed', '失败']].map(([key, name]) => ({ type: 'bar', name, itemStyle: { color: key === 'loginFailed' ? '#f56c6c' : '#67c23a' }, data: labels.map(label => by.get(label)?.[key] ?? null), barMaxWidth: 32 })) }
    },
    moduleChart() { return donut(this.distribution.map(row => ({ name: row.module, value: row.total }))) },
    failureItems() { return [['business', '业务拒绝'], ['auth', '认证/权限拒绝'], ['system', '系统异常'], ['http', '其他HTTP失败'], ['unknown', '未知结果']].map(([key, name]) => ({ name, value: this.distribution.reduce((sum, row) => sum + Number(row[key] || 0), 0) })).filter(item => item.value > 0) },
    failureChart() { return donut(this.failureItems) },
    currentRanking() { return this.rankings[this.rankingType] || [] },
    rankingChart() {
      const rows = [...this.currentRanking].reverse(), key = this.rankingType === 'slow' ? 'avgMs' : this.rankingType === 'failed' ? 'failed' : 'total'
      return { grid: { left: 10, right: 25, top: 10, bottom: 25, containLabel: true }, legend: { show: false }, tooltip: { trigger: 'axis', renderMode: 'richText', confine: true }, xAxis: { type: 'value' }, yAxis: { type: 'category', data: rows.map(row => row.method + ' ' + row.route), axisLabel: { width: 160, overflow: 'truncate' } }, series: [{ name: this.rankingType === 'slow' ? '平均耗时(ms)' : '次数', type: 'bar', barMaxWidth: 18, data: rows.map(row => row[key]), itemStyle: { borderRadius: [0, 4, 4, 0] } }] }
    }
  },
  mounted() { this.refresh(); this.timer = setInterval(() => { this.now = Date.now(); if (this.autoRefresh && this.isActive && !document.hidden) this.refresh() }, 30000) },
  activated() { this.isActive = true; this.refresh() },
  deactivated() { this.isActive = false },
  beforeUnmount() { this.disposed = true; this.listVersion++; clearInterval(this.timer) },
  methods: {
    number(value) { return value === null || value === undefined ? '—' : Number(value).toLocaleString('zh-CN', { maximumFractionDigits: 2 }) },
    asTimestamp(value) { return typeof value === 'number' ? value : Date.parse(String(value).replace(' ', 'T') + (/Z$|[+-]\d{2}:\d{2}$/.test(String(value)) ? '' : '+08:00')) },
    formatTime(value) { if (!value) return '—'; const at = this.asTimestamp(value); return Number.isFinite(at) ? new Date(at).toLocaleString('zh-CN', { timeZone: 'Asia/Shanghai', hour12: false }) : String(value) },
    trendOptions(rows, fields) {
      const labels = buckets(this.appliedRange, this.minute), by = new Map(rows.map(row => [row.bucket, row]))
      return lines(labels, fields.map(([key, name]) => ({ name, itemStyle: { color: key === 'failed' ? '#f56c6c' : key === 'success' ? '#67c23a' : key === 'peak' ? '#e6a23c' : key === 'active' ? '#67c23a' : '#409eff' }, data: labels.map(label => by.get(label)?.[key] ?? null) })))
    },
    changePeriod(value) {
      this.dateRange = presetRange(value, this.currentTime)
      this.handleQuery()
    },
    // 保留正在编辑的查询草稿，只让已提交的“今日/近7天”等预设随北京时间推进。
    advancePreset() {
      if (!this.appliedPeriod) return
      const range = presetRange(this.appliedPeriod, this.currentTime)
      if (range.join() === this.appliedRange.join()) return
      const unchanged = this.period === this.appliedPeriod && this.dateRange?.join() === this.appliedRange.join()
      this.appliedRange = range
      if (unchanged) this.dateRange = [...range]
      this.pageNum = 1
      this.trends = []; this.distribution = []; this.rankings = {}; this.rows = []; this.total = 0
    },
    handleQuery() {
      if (this.loading || this.tableLoading) return
      const days = this.dateRange?.length === 2 ? (Date.parse(this.dateRange[1]) - Date.parse(this.dateRange[0])) / 86400000 + 1 : 0
      if (!Number.isFinite(days) || days < 1 || days > 366) { this.$modal.msgWarning('请选择1至366天的统计日期范围'); return }
      if (this.appliedRange.join() !== this.dateRange.join()) { this.trends = []; this.distribution = []; this.rankings = {}; this.rows = []; this.total = 0 }
      this.appliedRange = [...this.dateRange]; this.appliedPeriod = this.period; this.pageNum = 1; this.refresh()
    },
    async refresh() {
      if (this.loading || this.tableLoading || this.disposed) return
      this.now = Date.now(); this.advancePreset()
      this.loading = true; this.refreshFailed = false
      const requestedRange = this.appliedRange.join()
      const params = { beginTime: this.appliedRange[0], endTime: this.appliedRange[1] }
      let repeat = false
      try {
        const results = await Promise.allSettled([getStatisticsOverview(), getStatisticsTrend(params), getStatisticsDistribution(params), ...['calls', 'slow', 'failed'].map(ranking => getStatisticsRanking({ ...params, ranking }))])
        if (this.disposed) return
        const setters = [value => {
          this.overview = value || {}
          if (Number.isFinite(Number(value?.serverTime)) && Number(value.serverTime) > 0) this.serverOffset = Number(value.serverTime) - Date.now()
          this.now = Date.now()
        }, value => { this.trends = Array.isArray(value) ? value : [] }, value => { this.distribution = Array.isArray(value) ? value : [] }, ...['calls', 'slow', 'failed'].map(key => value => { this.rankings[key] = Array.isArray(value) ? value : [] })]
        results.forEach((result, index) => { if (result.status === 'fulfilled') setters[index](result.value.data); else this.refreshFailed = true })
        this.advancePreset()
        if (requestedRange !== this.appliedRange.join()) { repeat = true; return }
        const listUpdated = await this.loadList()
        if (!this.disposed && (listUpdated || results.some(result => result.status === 'fulfilled'))) this.updatedAt = this.formatTime(this.currentTime)
      } finally {
        if (!this.disposed) { this.loading = false; if (repeat) this.$nextTick(this.refresh) }
      }
    },
    async loadList() {
      if (this.disposed) return
      const version = ++this.listVersion
      this.tableLoading = true
      try {
        const response = await listStatistics({ beginTime: this.appliedRange[0], endTime: this.appliedRange[1], keyword: this.appliedKeyword, pageNum: this.pageNum, pageSize: this.pageSize })
        if (!this.disposed && version === this.listVersion) {
          const total = Number(response.total) || 0, maximum = Math.max(1, Math.ceil(total / this.pageSize))
          if (this.pageNum > maximum) { this.pageNum = maximum; return await this.loadList() }
          this.rows = Array.isArray(response.rows) ? response.rows : []; this.total = total; this.listFailed = false
          return true
        }
      } catch { if (!this.disposed && version === this.listVersion) this.listFailed = true }
      finally { if (!this.disposed && version === this.listVersion) this.tableLoading = false }
    },
    searchList() { if (this.loading || this.tableLoading) return; this.appliedKeyword = this.keyword.trim(); this.pageNum = 1; this.loadList() },
    resetList() { this.keyword = ''; this.searchList() }
  }
}
</script>

<style scoped>
.statistics-page { color: inherit; }
.statistics-heading { display: flex; align-items: center; justify-content: space-between; gap: 16px; margin-bottom: 16px; }
.statistics-heading h2 { margin: 0 0 8px; font-size: 24px; font-weight: 600; color: inherit; }
.statistics-heading p, .range-note { margin: 0; font-size: 12px; opacity: .72; line-height: 1.6; }
.refresh-controls, .ranking-header { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.refresh-controls { font-size: 12px; }
.health-alert { margin-bottom: 12px; }
.collection-meta { display: flex; gap: 8px 24px; flex-wrap: wrap; margin-bottom: 16px; font-size: 12px; opacity: .72; }
.metric-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 16px; margin-bottom: 20px; }
.metric-card { border-top: 3px solid var(--el-color-primary); }
.metric-label { font-size: 13px; opacity: .78; }
.metric-value { margin: 12px 0; font-size: 32px; font-weight: 600; font-variant-numeric: tabular-nums; letter-spacing: -.6px; }
.metric-value small { margin-left: 8px; font-size: 13px; font-weight: 400; opacity: .7; }
.metric-note { font-size: 12px; opacity: .72; line-height: 1.7; }
.filter-card { margin-bottom: 20px; }
.filter-card :deep(.el-form-item) { margin-bottom: 12px; }
.chart-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 20px; }
.chart-grid > *, .metric-grid > * { min-width: 0; }
.header-note { margin-left: 8px; font-size: 12px; opacity: .65; font-weight: 400; }
.ranking-header { justify-content: space-between; }
.detail-card { margin-top: 20px; }
@media (max-width: 1200px) { .metric-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
@media (max-width: 800px) { .chart-grid, .metric-grid { grid-template-columns: minmax(0, 1fr); } .statistics-heading { align-items: flex-start; flex-direction: column; } }
</style>
