<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" label-width="68px">
      <el-form-item label="登录地址" prop="ipaddr">
        <el-input
          v-model="queryParams.ipaddr"
          placeholder="请输入登录地址"
          clearable
@keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="用户名称" prop="userName">
        <el-input
          v-model="queryParams.userName"
          placeholder="请输入用户名称"
          clearable
@keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :icon="resolveIcon('el-icon-search')" size="small" @click="handleQuery">搜索</el-button>
        <el-button :icon="resolveIcon('el-icon-refresh')" size="small" @click="resetQuery">重置</el-button>
      </el-form-item>

    </el-form>
    <el-table
      border
      v-loading="loading"
      :data="list.slice((pageNum-1)*pageSize,pageNum*pageSize)"
      style="width: 100%;"
      scrollbar-always-on
    >
      <el-table-column label="序号" type="index" align="center" width="65">
        <template #default="scope">
          <span>{{(pageNum - 1) * pageSize + scope.$index + 1}}</span>
        </template>
      </el-table-column>
      <el-table-column label="会话编号" align="center" prop="tokenId" min-width="320">
        <template #default="scope">
          <span class="online-session-id">{{ scope.row.tokenId }}</span>
        </template>
      </el-table-column>
      <el-table-column label="授权令牌" header-align="center" min-width="420">
        <template #default="scope">
          <div v-if="scope.row.authorizationToken" class="online-authorization">
            <span class="authorization-token">{{ scope.row.authorizationToken }}</span>
            <el-button link type="primary" size="small"
              v-clipboard:copy="scope.row.authorizationToken"
              v-clipboard:success="handleCopySuccess"
              v-clipboard:error="handleCopyError"
            >复制令牌</el-button>
          </div>
          <span v-else>暂无令牌</span>
        </template>
      </el-table-column>
      <el-table-column label="登录名称" align="center" prop="userName" min-width="130" :show-overflow-tooltip="true" />
      <el-table-column label="部门名称" align="center" prop="deptName" min-width="140" />
      <el-table-column label="主机" align="center" prop="ipaddr" min-width="150" :show-overflow-tooltip="true" />
      <el-table-column label="登录地点" align="center" prop="loginLocation" min-width="150" :show-overflow-tooltip="true" />
      <el-table-column label="浏览器" align="center" prop="browser" min-width="130" />
      <el-table-column label="操作系统" align="center" prop="os" min-width="130" />
      <el-table-column label="登录时间" align="center" prop="loginTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.loginTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" min-width="100" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button
size="small"
             link type="primary"
:icon="resolveIcon('el-icon-delete')"
            @click="handleForceLogout(scope.row)"
            v-hasPermi="['monitor:online:forceLogout']"
          >强退</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" v-model:page="pageNum" v-model:limit="pageSize" />
  </div>
</template>

<script>
/**
 * 在线会话维护页；按既有授权展示完整会话凭证，并提供复制与强制退出操作，页面不输出令牌日志。
 */
import { list, forceLogout } from "@/api/monitor/online"

export default {
  name: "Online",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 总条数
      total: 0,
      // 表格数据
      list: [],
      pageNum: 1,
      pageSize: 10,
      // 查询参数
      queryParams: {
        ipaddr: undefined,
        userName: undefined
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    handleCopySuccess() {
      this.$modal.msgSuccess("令牌复制成功")
    },
    handleCopyError() {
      this.$modal.msgError("复制失败，请手动选择令牌复制")
    },
    /** 查询登录日志列表 */
    getList() {
      this.loading = true
      list(this.queryParams).then(response => {
        this.list = response.rows
        this.total = response.total
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.pageNum = 1
      this.getList()
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm")
      this.handleQuery()
    },
    /** 强退按钮操作 */
    handleForceLogout(row) {
      this.$modal.confirm('是否确认强退名称为"' + row.userName + '"的用户？').then(function() {
        return forceLogout(row.tokenId)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("强退成功")
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
/* 组件局部布局与展示样式；跨页面主题及 body 弹层规则在 assets/styles 中统一维护。 */
.online-authorization {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
}
.authorization-token,
.online-session-id {
  display: block;
  width: 100%;
  white-space: normal;
  overflow-wrap: anywhere;
  word-break: break-all;
  user-select: text;
}
</style>
