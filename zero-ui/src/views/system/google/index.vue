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
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="googleList">
      <el-table-column label="用户ID" align="center" prop="userId" width="100" />
      <el-table-column label="用户名" align="center" prop="userName" :show-overflow-tooltip="true" />
      <el-table-column label="用户昵称" align="center" prop="nickName" :show-overflow-tooltip="true" />
      <el-table-column label="绑定状态" align="center" width="100">
        <template slot-scope="scope">
          <el-tag :type="scope.row.bound ? 'success' : 'info'">{{ scope.row.bound ? '已绑定' : '未绑定' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="谷歌登录秘钥片段" align="center" prop="googleAuthKeyMask" :show-overflow-tooltip="true" />
      <el-table-column label="创建者" align="center" prop="createBy" width="120" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="更新者" align="center" prop="updateBy" width="120" />
      <el-table-column label="更新时间" align="center" prop="updateTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.updateTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="180">
        <template slot-scope="scope">
          <el-button
            v-if="scope.row.bound"
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="handleViewKey(scope.row)"
            v-hasPermi="['system:google:viewKey']"
          >查看明文</el-button>
          <el-button
            v-if="scope.row.bound"
            size="mini"
            type="text"
            icon="el-icon-refresh-left"
            @click="handleUnbind(scope.row)"
            v-hasPermi="['system:google:unbind']"
          >解绑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <el-dialog title="谷歌登录秘钥全文" custom-class="google-key-dialog" :visible.sync="keyOpen" width="520px" append-to-body :close-on-click-modal="false">
      <el-alert title="该信息属于敏感密钥，请勿截图、转发或记录到日志中。" type="warning" show-icon :closable="false" />
      <div class="key-box">
        <el-input ref="googleAuthKeyInput" v-model="googleAuthKey" readonly class="key-input" />
        <el-button type="text" icon="el-icon-document-copy" class="copy-key-btn" @click="copyGoogleAuthKey">复制</el-button>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="keyOpen = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getGoogleAuthKey, listGoogle, unbindGoogle } from '@/api/system/google'

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
      })
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
      this.$modal.confirm('确认解绑用户"' + row.userName + '"的谷歌动态码吗？解绑后该用户下次登录需要重新绑定。').then(() => {
        return unbindGoogle({ userId: row.userId })
      }).then(() => {
        this.$modal.msgSuccess('解绑成功')
        this.getList()
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
      const input = this.$refs.googleAuthKeyInput && this.$refs.googleAuthKeyInput.$el.querySelector('input')
      if (!input) {
        this.$modal.msgError('复制失败')
        return
      }
      input.select()
      const success = document.execCommand('copy')
      input.blur()
      success ? this.$modal.msgSuccess('复制成功') : this.$modal.msgError('复制失败')
    }
  }
}
</script>

<style scoped>
.key-box {
  margin-top: 16px;
  text-align: right;
}

.key-input {
  display: block;
}

.copy-key-btn {
  margin-top: 16px;
  padding: 0;
}

::v-deep .google-key-dialog {
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
