<template>
  <div class="app-container">
    <el-row :gutter="10">
      <el-col :span="8">
        <el-card style="height: calc(100vh - 125px)">
          <template #header><div >
            <span><AppIcon icon-class="el-icon-collection" class=""></AppIcon> 缓存列表</span>
            <el-button
              style="float: right; padding: 3px 0"
               link type="primary"
:icon="resolveIcon('el-icon-refresh-right')"
              @click="refreshCacheNames()"
            ></el-button>
          </div></template>
          <el-table
            border
            v-loading="loading"
            :data="cacheNames"
            :height="tableHeight"
            highlight-current-row
            @row-click="getCacheKeys"
            style="width: 100%"
          >
            <el-table-column
              label="序号"
              width="60"
              type="index"
            ></el-table-column>

            <el-table-column
              label="缓存名称"
              align="center"
              prop="cacheName"
              :show-overflow-tooltip="true"
              :formatter="nameFormatter"
            ></el-table-column>

            <el-table-column
              label="备注"
              align="center"
              prop="remark"
              :show-overflow-tooltip="true"
            />
            <el-table-column
              label="操作"
              width="60"
              align="center"
              class-name="small-padding fixed-width"
            >
              <template #default="scope">
                <el-button
size="small"
                   link type="primary"
:icon="resolveIcon('el-icon-delete')"
                  @click="handleClearCacheName(scope.row)"
                ></el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card style="height: calc(100vh - 125px)">
          <template #header><div >
            <span><AppIcon icon-class="el-icon-key" class=""></AppIcon> 键名列表</span>
            <el-button
              style="float: right; padding: 3px 0"
               link type="primary"
:icon="resolveIcon('el-icon-refresh-right')"
              @click="refreshCacheKeys()"
            ></el-button>
          </div></template>
          <el-table
            border
            v-loading="subLoading"
            :data="cacheKeys"
            :height="tableHeight"
            highlight-current-row
            @row-click="handleCacheValue"
            style="width: 100%"
          >
            <el-table-column
              label="序号"
              width="60"
              type="index"
            ></el-table-column>
            <el-table-column
              label="缓存键名"
              align="center"
              :show-overflow-tooltip="true"
              :formatter="keyFormatter"
            >
            </el-table-column>
            <el-table-column
              label="操作"
              width="60"
              align="center"
              class-name="small-padding fixed-width"
            >
              <template #default="scope">
                <el-button
size="small"
                   link type="primary"
:icon="resolveIcon('el-icon-delete')"
                  @click="handleClearCacheKey(scope.row)"
                ></el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card :bordered="false" style="height: calc(100vh - 125px)">
          <template #header><div >
            <span><AppIcon icon-class="el-icon-document" class=""></AppIcon> 缓存内容</span>
            <el-button
              style="float: right; padding: 3px 0"
               link type="primary"
:icon="resolveIcon('el-icon-refresh-right')"
              @click="handleClearCacheAll()"
              >清理全部</el-button
            >
          </div></template>
          <el-form :model="cacheForm">
            <el-row :gutter="32">
              <el-col :offset="1" :span="22">
                <el-form-item label="缓存名称:" prop="cacheName">
                  <el-input v-model="cacheForm.cacheName" readonly />
                </el-form-item>
              </el-col>
              <el-col :offset="1" :span="22">
                <el-form-item label="缓存键名:" prop="cacheKey">
                  <el-input v-model="cacheForm.cacheKey" readonly />
                </el-form-item>
              </el-col>
              <el-col :offset="1" :span="22">
                <el-form-item label="缓存内容:" prop="cacheValue">
                  <el-input
                    v-model="cacheForm.cacheValue"
                    type="textarea"
                    :rows="8"
                    readonly
                  />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { listCacheName, listCacheKey, getCacheValue, clearCacheName, clearCacheKey, clearCacheAll } from "@/api/monitor/cache"

export default {
  name: "CacheList",
  data() {
    return {
      cacheNames: [],
      cacheKeys: [],
      cacheForm: {},
      loading: true,
      subLoading: false,
      nowCacheName: "",
      keysRequestId: 0,
      valueRequestId: 0,
      tableHeight: window.innerHeight - 200
    }
  },
  created() {
    this.getCacheNames()
  },
  methods: {
    /** 查询缓存名称列表 */
    getCacheNames(showMessage = false) {
      this.loading = true
      listCacheName().then(response => {
        this.cacheNames = response.data
        this.loading = false
        if (showMessage) this.$modal.msgSuccess("刷新缓存列表成功")
      }).catch(() => {
        this.loading = false
      })
    },
    /** 刷新缓存名称列表 */
    refreshCacheNames() {
      this.getCacheNames(true)
    },
    /** 清理指定名称缓存 */
    handleClearCacheName(row) {
      clearCacheName(row.cacheName).then(response => {
        this.$modal.msgSuccess("清理缓存名称[" + row.cacheName + "]成功")
        this.getCacheKeys()
      }).catch(() => {})
    },
    /** 查询缓存键名列表 */
    getCacheKeys(row, showMessage = false) {
      const cacheName = row?.cacheName ?? this.nowCacheName
      if (!cacheName) {
        return
      }
      const requestId = ++this.keysRequestId
      this.nowCacheName = cacheName
      this.cacheKeys = []
      this.cacheForm = {}
      this.valueRequestId++
      this.subLoading = true
      listCacheKey(cacheName).then(response => {
        if (requestId === this.keysRequestId) {
          this.cacheKeys = response.data || []
          if (showMessage === true) this.$modal.msgSuccess("刷新键名列表成功")
        }
      }).catch(() => {}).finally(() => {
        if (requestId === this.keysRequestId) this.subLoading = false
      })
    },
    /** 刷新缓存键名列表 */
    refreshCacheKeys() {
      this.getCacheKeys(undefined, true)
    },
    /** 清理指定键名缓存 */
    handleClearCacheKey(cacheKey) {
      clearCacheKey(cacheKey).then(response => {
        this.$modal.msgSuccess("清理缓存键名[" + cacheKey + "]成功")
        this.getCacheKeys()
      }).catch(() => {})
    },
    /** 列表前缀去除 */
    nameFormatter(row) {
      return row.cacheName.replace(":", "")
    },
    /** 键名前缀去除 */
    keyFormatter(cacheKey) {
      return cacheKey.replace(this.nowCacheName, "")
    },
    /** 查询缓存内容详细 */
    handleCacheValue(cacheKey) {
      if (!this.nowCacheName || !cacheKey) return
      const requestId = ++this.valueRequestId
      this.cacheForm = {}
      getCacheValue(this.nowCacheName, cacheKey).then(response => {
        // 忽略之前选择的慢响应，切换名称或键名后不显示旧内容。
        if (requestId === this.valueRequestId) this.cacheForm = response.data || {}
      }).catch(() => {})
    },
    /** 清理全部缓存 */
    handleClearCacheAll() {
      clearCacheAll().then(response => {
        this.$modal.msgSuccess("清理全部缓存成功")
      }).catch(() => {})
    }
  }
}
</script>
