<template>
  <div class="app-container">
    <h4 class="form-header h4">基本信息</h4>
    <el-form ref="form" :model="form" label-width="80px">
      <el-row>
        <el-col :span="8" :offset="2">
          <el-form-item label="用户昵称" prop="nickName">
            <el-input v-model="form.nickName" disabled />
          </el-form-item>
        </el-col>
        <el-col :span="8" :offset="2">
          <el-form-item label="登录账号" prop="userName">
            <el-input v-model="form.userName" disabled />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <h4 class="form-header h4">角色信息</h4>
    <el-table v-loading="loading" :row-key="getRowKey" @row-click="clickRow" ref="table" @selection-change="handleSelectionChange" :data="roles.slice((pageNum-1)*pageSize,pageNum*pageSize)" border>
      <el-table-column label="序号" type="index" align="center">
        <template #default="scope">
          <span>{{ (pageNum - 1) * pageSize + scope.$index + 1 }}</span>
        </template>
      </el-table-column>
      <el-table-column type="selection" :reserve-selection="true" :selectable="checkSelectable" width="55" />
      <el-table-column label="角色编号" align="center" prop="roleId" />
      <el-table-column label="角色名称" align="center" prop="roleName" />
      <el-table-column label="角色英文名" align="center" prop="roleKey" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" v-model:page="pageNum" v-model:limit="pageSize" />

    <div class="auth-role-actions">
      <el-button type="primary" :disabled="loading || !form.userId" :loading="submitting" @click="submitForm()">提交</el-button>
      <el-button @click="close()">返回</el-button>
    </div>
  </div>
</template>

<script>
/**
 * 为指定账号分配角色；等待账号及角色列表就绪后允许选择，提交仍由对应后台接口校验。
 */
import { getAuthRole, updateAuthRole } from "@/api/system/user"

export default {
  name: "AuthRole",
  data() {
    return {
      // 遮罩层
      loading: true,
      submitting: false,
      // 分页信息
      total: 0,
      pageNum: 1,
      pageSize: 10,
      // 选中角色编号
      roleIds: [],
      // 角色信息
      roles: [],
      // 用户信息
      form: {}
    }
  },
  created() {
    const userId = this.$route.params && this.$route.params.userId
    if (userId) {
      this.loading = true
      getAuthRole(userId).then(async (response) => {
        this.form = response.user || {}
        this.roles = Array.isArray(response.roles) ? response.roles : []
        this.total = this.roles.length
        await this.$nextTick()
        this.roles.forEach((row) => {
          if (row.flag) {
            this.$refs.table?.toggleRowSelection(row)
          }
        })
      }).catch(() => {
        // 未取得完整用户数据时保留空表单，禁止发出缺少用户ID的授权请求。
        this.form = {}
        this.roles = []
        this.roleIds = []
        this.total = 0
      }).finally(() => {
        this.loading = false
      })
    } else {
      this.loading = false
    }
  },
  methods: {
    /** 单击选中行数据 */
    clickRow(row) {
      if (this.checkSelectable(row)) {
        this.$refs.table.toggleRowSelection(row)
      }
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.roleIds = selection.map((item) => item.roleId)
    },
    // 保存选中的数据编号
    getRowKey(row) {
      return row.roleId
    },
    // 检查角色状态
    checkSelectable(row) {
      return row.status === "0" ? true : false
    },
    /** 提交按钮 */
    submitForm() {
      if (this.loading || this.submitting || !this.form.userId) return
      const userId = this.form.userId
      const roleIds = this.roleIds.join(",")
      this.submitting = true
      updateAuthRole({ userId: userId, roleIds: roleIds }).then((response) => {
        this.$modal.msgSuccess("授权成功")
        this.close()
      }).catch(() => {
        // 保存失败保留用户和勾选结果，供原页面重试。
      }).finally(() => {
        this.submitting = false
      })
    },
    /** 关闭按钮 */
    close() {
      const obj = { path: "/system/user" }
      this.$tab.closeOpenPage(obj)
    }
  }
}
</script>

<style scoped>
/* 组件局部布局与展示样式；跨页面主题及 body 弹层规则在 assets/styles 中统一维护。 */
.auth-role-actions {
  display: flex;
  justify-content: center;
  margin-top: 24px;
  padding-bottom: 12px;
}
</style>
