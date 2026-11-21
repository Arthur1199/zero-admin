<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :model="queryParams" :inline="true" label-width="68px">
      <el-form-item label="用户名" prop="userName">
        <el-input
          v-model="queryParams.userName"
          placeholder="请输入用户名"
          clearable
          size="small"
          style="width: 240px"
@keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :icon="resolveIcon('el-icon-search')" size="small" @click="handleQuery">搜索</el-button>
        <el-button :icon="resolveIcon('el-icon-refresh')" size="small" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="googleList" border>
      <el-table-column label="用户ID" align="center" prop="userId" width="100" />
      <el-table-column label="用户名" align="center" prop="userName" :show-overflow-tooltip="true" />
      <el-table-column label="用户昵称" align="center" prop="nickName" :show-overflow-tooltip="true" />
      <el-table-column label="绑定状态" align="center" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.bound ? 'success' : 'info'">{{ scope.row.bound ? '已绑定' : '未绑定' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="谷歌登录秘钥片段" align="center" prop="googleAuthKeyMask" :show-overflow-tooltip="true" />
      <el-table-column label="创建人" align="center" prop="createBy" width="120" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="160">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="更新人" align="center" prop="updateBy" width="120" />
      <el-table-column label="更新时间" align="center" prop="updateTime" width="160">
        <template #default="scope">
          <span>{{ parseTime(scope.row.updateTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="180">
        <template #default="scope">
          <el-button
            v-if="scope.row.bound"
size="small"
             link type="primary"
:icon="resolveIcon('el-icon-view')"
            @click="handleViewKey(scope.row)"
            v-hasPermi="['system:google:viewKey']"
          >查看明文</el-button>
          <el-button
            v-if="scope.row.bound"
size="small"
             link type="primary"
:icon="resolveIcon('el-icon-refresh-left')"
            @click="handleUnbind(scope.row)"
            v-hasPermi="['system:google:unbind']"
          >解绑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
v-model:page="queryParams.pageNum"
v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <el-dialog title="谷歌登录秘钥全文" class="google-key-dialog" v-model="keyOpen" width="520px" append-to-body :close-on-click-modal="false">
      <el-alert title="该信息属于敏感密钥，请勿截图、转发或记录到日志中。" type="warning" show-icon :closable="false" />
      <code ref="googleAuthKeyText" class="key-text">{{ googleAuthKey }}</code>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" :icon="resolveIcon('el-icon-document-copy')" @click="copyGoogleAuthKey">复制</el-button>
          <el-button @click="keyOpen = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
/**
 * 谷歌验证绑定记录维护；按操作入口读取绑定信息、复制或解绑，失败提示复用公共请求层。
 */
import { getGoogleAuthKey, listGoogle, unbindGoogle } from '@/api/system/google'
import { getToken } from '@/utils/auth'

export default {
  name: 'Google',
  data() {
    return {
      loading: true,
      showSearch: true,
      total: 0,
      googleList: [],
      keyOpen: false,
      googleAuthKey: '',
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        userName: ''
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listGoogle(this.queryParams).then(response => {
        this.googleList = response.rows
        this.total = response.total
      }).finally(() => {
        this.loading = false
      }).catch(() => {})
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm('queryForm')
      this.handleQuery()
    },
    handleViewKey(row) {
      this.$modal.confirm('查看谷歌Key明文存在泄露风险，请确认当前环境安全。').then(() => {
        return getGoogleAuthKey(row.userId)
      }).then(response => {
        this.googleAuthKey = response.data || response.msg || ''
        this.keyOpen = true
      })
    },
    handleUnbind(row) {
      let ownToken
      return this.$modal.confirm('确认解绑用户"' + row.userName + '"的谷歌动态码吗？解绑会强制退出该用户的所有在线会话。').then(() => {
        if (String(row.userId) === String(this.$store.getters.id)) ownToken = this.$store.getters.token
        return unbindGoogle({ userId: row.userId })
      }).then(async () => {
        this.$modal.msgSuccess('解绑成功')
        if (ownToken) {
          // 自己解绑时用接口成功作为退出兜底，只清理发起解绑的登录态。
          if (this.$store.getters.token === ownToken && getToken() === ownToken) await this.$store.dispatch('FedLogOut')
          if (!getToken() && this.$route.path !== '/login') await this.$router.replace('/login')
          return
        }
        return this.getList()
      })
    },
    copyGoogleAuthKey() {
      if (!this.googleAuthKey) {
        this.$modal.msgWarning('暂无可复制内容')
        return
      }
      if (navigator.clipboard && navigator.clipboard.writeText) {
        navigator.clipboard.writeText(this.googleAuthKey).then(() => {
          this.$modal.msgSuccess('复制成功')
        }).catch(() => {
          this.copyGoogleAuthKeyFallback()
        })
      } else {
        this.copyGoogleAuthKeyFallback()
      }
    },
    copyGoogleAuthKeyFallback() {
      const text = this.$refs.googleAuthKeyText
      const selection = window.getSelection()
      if (!text || !selection) {
        this.$modal.msgError('复制失败')
        return
      }
      // 剪贴板 API 不可用时，选中弹窗内的明文执行复制。
      const range = document.createRange()
      range.selectNodeContents(text)
      selection.removeAllRanges()
      selection.addRange(range)
      let success = false
      try {
        success = document.execCommand('copy')
      } catch {
        success = false
      } finally {
        selection.removeAllRanges()
      }
      success ? this.$modal.msgSuccess('复制成功') : this.$modal.msgError('复制失败')
    }
  }
}
</script>

<style scoped>
/* 组件局部布局与展示样式；跨页面主题及 body 弹层规则在 assets/styles 中统一维护。 */
.key-text {
  display: block;
  margin-top: 16px;
  padding: 12px 14px;
  border-radius: 8px;
  background-color: color-mix(in srgb, currentColor 7%, transparent);
  font-family: ui-monospace, Consolas, monospace;
  font-size: 14px;
  line-height: 1.65;
  overflow-wrap: anywhere;
  user-select: text;
}

:deep(.google-key-dialog) {
  min-height: 0 !important;
  max-height: none !important;

  .el-dialog__body {
    flex: none !important;
    padding: 18px 24px 10px;
    overflow: visible;
  }

  .el-dialog__footer {
    padding: 10px 24px 20px;
  }
}
</style>
