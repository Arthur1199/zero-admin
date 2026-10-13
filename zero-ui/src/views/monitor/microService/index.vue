<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" label-width="76px">
      <el-form-item label="服务名称" prop="serviceName">
        <el-input
          v-model="queryParams.serviceName"
          placeholder="请输入服务名称"
          clearable
@keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择运行状态" clearable>
          <el-option label="在线" value="UP" />
          <el-option label="已下线" value="DOWN" />
          <el-option label="状态未知" value="UNKNOWN" />
        </el-select>
      </el-form-item>
      <el-form-item label="自动刷新">
        <el-select v-model="autoRefreshInterval" class="refresh-select" placeholder="请选择刷新频率" @change="handleRefreshChange">
          <el-option label="5秒" :value="5" />
          <el-option label="10秒" :value="10" />
          <el-option label="30秒" :value="30" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :icon="resolveIcon('el-icon-search')" size="small" @click="handleQuery">搜索</el-button>
        <el-button :icon="resolveIcon('el-icon-refresh')" size="small" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <div v-loading="loading" class="service-card-grid">
      <el-empty v-if="!loading && list.length === 0" description="暂无微服务实例" />
      <el-card v-for="item in list" :key="item.instanceId" shadow="hover" class="service-card">
        <template #header><div  class="service-card-header">
          <div class="service-title">
            <AppIcon icon-class="el-icon-monitor" class=""></AppIcon>
            <span>{{ item.serviceName }}</span>
          </div>
          <el-tag :type="statusType(item.status)" size="small" class="status-tag">{{ statusText(item.status) }}</el-tag>
        </div></template>
        <div class="service-address">
          <div class="address-label">注册地址</div>
          <div class="address-main">{{ item.ip }}:{{ item.port }}</div>
          <div class="address-sub">检测时间：{{ item.checkTime || '--' }}</div>
        </div>
        <el-row :gutter="12" class="metric-row">
          <el-col :span="6">
            <div class="metric-box">
              <div class="metric-label">进程CPU</div>
              <div class="metric-value">{{ formatPercent(item.processCpuUsage) }}</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="metric-box">
              <div class="metric-label">系统CPU</div>
              <div class="metric-value">{{ formatPercent(item.systemCpuUsage) }}</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="metric-box">
              <div class="metric-label">CPU核心数</div>
              <div class="metric-value">{{ formatNumber(item.cpuCount) }}</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="metric-box">
              <div class="metric-label">运行时长</div>
              <div class="metric-value">{{ formatUptime(item.uptimeSeconds) }}</div>
            </div>
          </el-col>
        </el-row>
        <div class="metric-section">
          <div class="section-title">堆内存</div>
          <div class="section-grid">
            <el-tooltip content="服务当前已经用掉的堆内存" placement="top">
              <div class="section-item">
                <span>已用内存</span>
                <strong>{{ formatMb(item.heapUsedMb) }}</strong>
              </div>
            </el-tooltip>
            <el-tooltip content="JVM已经从系统申请到手、当前可以直接使用的堆内存" placement="top">
              <div class="section-item">
                <span>当前可用内存</span>
                <strong>{{ formatMb(item.heapCommittedMb) }}</strong>
              </div>
            </el-tooltip>
            <el-tooltip content="这个服务最多允许使用的堆内存上限" placement="top">
              <div class="section-item">
                <span>最大内存</span>
                <strong>{{ formatMb(item.heapMaxMb) }}</strong>
              </div>
            </el-tooltip>
          </div>
        </div>
        <div class="metric-section">
          <div class="section-title">线程</div>
          <div class="section-grid">
            <el-tooltip content="当前正在运行或等待的线程总数" placement="top">
              <div class="section-item">
                <span>活动线程</span>
                <strong>{{ formatNumber(item.threadLive) }}</strong>
              </div>
            </el-tooltip>
            <el-tooltip content="在后台做辅助工作的线程数量，一般不阻止服务退出" placement="top">
              <div class="section-item">
                <span>守护线程</span>
                <strong>{{ formatNumber(item.threadDaemon) }}</strong>
              </div>
            </el-tooltip>
            <el-tooltip content="服务启动后同时存在过的最高线程数" placement="top">
              <div class="section-item">
                <span>线程峰值</span>
                <strong>{{ formatNumber(item.threadPeak) }}</strong>
              </div>
            </el-tooltip>
          </div>
        </div>
        <div class="metric-section">
          <div class="section-title">垃圾回收</div>
          <div class="section-grid">
            <el-tooltip content="服务启动后触发垃圾回收的总次数" placement="top">
              <div class="section-item">
                <span>回收总次数</span>
                <strong>{{ formatNumber(item.gcCount) }}</strong>
              </div>
            </el-tooltip>
            <el-tooltip content="所有垃圾回收加起来一共花了多少秒" placement="top">
              <div class="section-item">
                <span>回收总耗时</span>
                <strong>{{ formatSeconds(item.gcTotalTimeSeconds) }}</strong>
              </div>
            </el-tooltip>
            <el-tooltip content="单次垃圾回收耗时最长的一次用了多少秒" placement="top">
              <div class="section-item">
                <span>单次最长耗时</span>
                <strong>{{ formatSeconds(item.gcMaxTimeSeconds) }}</strong>
              </div>
            </el-tooltip>
          </div>
        </div>
        <div class="load-section" :class="loadClass(item.loadStatus)">
          <div class="load-title">
            <span>微服务负载状态</span>
            <strong>{{ item.loadStatusName || '--' }}</strong>
          </div>
          <div class="load-desc">{{ item.loadStatusDesc || '--' }}</div>
          <div class="load-reason">{{ item.loadStatusReason || '--' }}</div>
        </div>
        <div class="detail-list">
          <div v-if="item.errorMsg" class="detail-item error-item">
            <span class="detail-label">异常</span>
            <el-tooltip :content="item.errorMsg" placement="top">
              <span class="detail-value">{{ item.errorMsg }}</span>
            </el-tooltip>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script>
import { listMicroService } from '@/api/monitor/microService'

export default {
  name: 'MicroService',
  data() {
    return {
      // 遮罩层
      loading: true,
      // 卡片数据
      list: [],
      // 自动刷新间隔，单位秒
      autoRefreshInterval: 5,
      // 自动刷新定时器
      refreshTimer: null,
      // 查询参数
      queryParams: {
        serviceName: undefined,
        status: undefined
      }
    }
  },
  created() {
    this.getList()
    this.handleRefreshChange()
  },
  beforeUnmount() {
    this.clearRefreshTimer()
  },
  methods: {
    /** 查询微服务状态列表 */
    getList(showLoading = true) {
      if (showLoading) {
        this.loading = true
      }
      listMicroService(this.queryParams).then(response => {
        this.list = response.rows
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.getList()
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm('queryForm')
      this.handleQuery()
    },
    /** 自动刷新频率变更 */
    handleRefreshChange() {
      this.clearRefreshTimer()
      if (!this.autoRefreshInterval) {
        return
      }
      this.refreshTimer = setInterval(() => {
        this.getList(false)
      }, this.autoRefreshInterval * 1000)
    },
    /** 清理自动刷新定时器 */
    clearRefreshTimer() {
      if (this.refreshTimer) {
        clearInterval(this.refreshTimer)
        this.refreshTimer = null
      }
    },
    /** 状态标签样式 */
    statusType(status) {
      if (status === 'UP') {
        return 'success'
      }
      if (status === 'DOWN') {
        return 'danger'
      }
      return 'info'
    },
    /** 状态文案 */
    statusText(status) {
      if (status === 'UP') {
        return '在线'
      }
      if (status === 'DOWN') {
        return '已下线'
      }
      return '状态未知'
    },
    /** 负载状态样式 */
    loadClass(loadStatus) {
      if (loadStatus === 'NORMAL') {
        return 'load-normal'
      }
      if (loadStatus === 'BUSY') {
        return 'load-busy'
      }
      return 'load-overload'
    },
    /** 格式化运行时长 */
    formatUptime(value) {
      if (value === null || value === undefined) {
        return '--'
      }
      const totalSeconds = Math.floor(value)
      const days = Math.floor(totalSeconds / 86400)
      const hours = Math.floor((totalSeconds % 86400) / 3600)
      const minutes = Math.floor((totalSeconds % 3600) / 60)
      return days + '天' + hours + '小时' + minutes + '分'
    },
    /** 格式化百分比 */
    formatPercent(value) {
      return value === null || value === undefined ? '--' : value + '%'
    },
    /** 格式化内存 */
    formatMb(value) {
      return value === null || value === undefined ? '--' : value + ' MB'
    },
    /** 格式化数字 */
    formatNumber(value) {
      return value === null || value === undefined ? '--' : value
    },
    /** 格式化秒 */
    formatSeconds(value) {
      return value === null || value === undefined ? '--' : value + ' 秒'
    }
  }
}
</script>

<style scoped>
.service-card-grid {
  min-height: 220px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.refresh-select {
  width: 160px;
}

.service-card {
  position: relative;
  border-radius: 8px;
  color: #172033;
  background: rgba(255, 255, 255, 0.94);
  border: 1px solid rgba(148, 163, 184, 0.28);
  box-shadow: 0 14px 32px rgba(15, 23, 42, 0.08);
}

.service-card :deep(.el-card__header) {
  display: block !important;
  color: #172033;
  background: rgba(246, 248, 252, 0.96);
  border-bottom: 1px solid rgba(148, 163, 184, 0.22);
}

.service-card :deep(.el-card__body) {
  color: #172033;
  background: rgba(255, 255, 255, 0.94);
}

.service-card-header {
  width: 100%;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 24px;
  padding-right: 90px;
}

.service-title {
  min-width: 0;
  flex: 1;
  display: flex;
  align-items: center;
  font-size: 15px;
  font-weight: 600;
}

.service-title i {
  margin-right: 8px;
}

.service-title span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.status-tag {
  position: absolute !important;
  top: 18px !important;
  right: 18px !important;
  transform: none !important;
  min-width: 48px;
  text-align: center;
  z-index: 1;
}

.service-address {
  margin-bottom: 14px;
}

.address-label {
  color: #5c6b82;
  font-size: 13px;
  line-height: 20px;
}

.address-main {
  color: #172033;
  font-size: 18px;
  font-weight: 600;
  line-height: 26px;
}

.address-sub {
  margin-top: 4px;
  color: #5c6b82;
  font-size: 12px;
}

.metric-row {
  margin-bottom: 12px;
}

.metric-box {
  min-height: 86px;
  padding: 12px 8px;
  border-radius: 8px;
  background: rgba(241, 245, 249, 0.92);
  text-align: center;
}

.metric-label {
  color: #5c6b82;
  font-size: 12px;
  line-height: 18px;
}

.metric-value {
  margin-top: 6px;
  color: #172033;
  font-size: 16px;
  font-weight: 600;
  line-height: 24px;
  word-break: break-word;
}

.detail-list {
  border-top: 1px solid rgba(148, 163, 184, 0.22);
}

.metric-section {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid rgba(148, 163, 184, 0.22);
}

.section-title {
  margin-bottom: 10px;
  color: #172033;
  font-size: 14px;
  font-weight: 600;
}

.section-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
}

.section-item {
  min-height: 68px;
  padding: 10px 8px;
  border-radius: 8px;
  background: rgba(241, 245, 249, 0.92);
}

.section-item span {
  display: block;
  color: #5c6b82;
  font-size: 12px;
  line-height: 18px;
}

.section-item strong {
  display: block;
  margin-top: 6px;
  color: #172033;
  font-size: 15px;
  line-height: 20px;
  word-break: break-word;
}

.load-section {
  margin-top: 12px;
  padding: 12px;
  border-radius: 8px;
  border: 1px solid rgba(148, 163, 184, 0.22);
}

.load-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #172033;
  font-size: 14px;
  font-weight: 600;
}

.load-title strong {
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 13px;
}

.load-desc {
  margin-top: 8px;
  color: #344054;
  font-size: 13px;
  line-height: 20px;
}

.load-reason {
  margin-top: 4px;
  color: #5c6b82;
  font-size: 12px;
  line-height: 18px;
}

.load-normal {
  background: rgba(46, 204, 113, 0.14);
  border-color: rgba(46, 204, 113, 0.28);
}

.load-normal .load-title strong {
  background: #2ecc71;
  color: #ffffff;
}

.load-busy {
  background: rgba(230, 162, 60, 0.16);
  border-color: rgba(230, 162, 60, 0.3);
}

.load-busy .load-title strong {
  background: #e6a23c;
  color: #ffffff;
}

.load-overload {
  background: rgba(245, 108, 108, 0.16);
  border-color: rgba(245, 108, 108, 0.3);
}

.load-overload .load-title strong {
  background: #f56c6c;
  color: #ffffff;
}

.detail-item {
  display: flex;
  align-items: center;
  min-height: 36px;
  line-height: 20px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.16);
}

.detail-item:last-child {
  border-bottom: 0;
}

.detail-label {
  flex: 0 0 72px;
  color: #5c6b82;
}

.detail-value {
  min-width: 0;
  flex: 1;
  color: #172033;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.link-text {
  color: #8fd3ff;
}

.error-item .detail-value {
  color: #f56c6c;
}

@media (max-width: 1400px) {
  .service-card-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 900px) {
  .service-card-grid {
    grid-template-columns: 1fr;
  }

  .metric-row .el-col {
    width: 50%;
  }

  .section-grid {
    grid-template-columns: 1fr;
  }
}
</style>
