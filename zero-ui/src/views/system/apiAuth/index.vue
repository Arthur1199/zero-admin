<template>
  <div class="app-container">
    <el-form
      class="api-auth-query-form"
      :model="queryParams"
      ref="queryForm"
      size="small"
      :inline="true"
      v-show="showSearch"
      label-width="68px"
    >
      <el-form-item label="请求路径" prop="apiUrl">
        <el-input
          v-model="queryParams.apiUrl"
          placeholder="请输入API请求路径"
          clearable
          style="width: 240px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="权限标识" prop="apiPermission">
        <el-input
          v-model="queryParams.apiPermission"
          placeholder="请输入API权限标识"
          clearable
          style="width: 220px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="请求方式" prop="apiMethod">
        <el-select v-model="queryParams.apiMethod" placeholder="请求方式" clearable style="width: 120px">
          <el-option
            v-for="item in methodOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="是否开启谷歌验证" prop="isNeedGoogle" label-width="128px">
        <el-select v-model="queryParams.isNeedGoogle" placeholder="是否开启谷歌验证" clearable style="width: 160px">
          <el-option
            v-for="item in googleOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="apiStatus">
        <el-select v-model="queryParams.apiStatus" placeholder="API状态" clearable style="width: 120px">
          <el-option
            v-for="item in statusOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="创建时间">
        <el-date-picker
          v-model="dateRange"
          style="width: 240px"
          value-format="yyyy-MM-dd"
          type="daterange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
        ></el-date-picker>
      </el-form-item>
      <el-form-item class="api-auth-query-buttons">
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['system:apiAuth:add']"
        >新增</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="apiAuthList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="id" width="80" />
      <el-table-column label="请求路径" align="center" prop="apiUrl" min-width="240" :show-overflow-tooltip="true" />
      <el-table-column label="权限标识" align="center" prop="apiPermission" min-width="180" :show-overflow-tooltip="true" />
      <el-table-column label="请求方式" align="center" prop="apiMethod" width="100" />
      <el-table-column label="是否开启谷歌验证" align="center" prop="isNeedGoogle" width="150">
        <template slot-scope="scope">
          <el-tag :type="scope.row.isNeedGoogle === 1 ? 'warning' : 'info'">
            {{ formatGoogle(scope.row.isNeedGoogle) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="apiStatus" width="90">
        <template slot-scope="scope">
          <el-tag :type="formatStatusTagType(scope.row.apiStatus)">
            {{ formatStatus(scope.row.apiStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="描述" align="center" prop="apiDesc" min-width="180" :show-overflow-tooltip="true" />
      <el-table-column label="备注" align="center" prop="remark" min-width="160" :show-overflow-tooltip="true" />
      <el-table-column label="创建人" align="center" prop="createBy" width="100" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="最后更新人" align="center" prop="updateBy" width="100" />
      <el-table-column label="最后更新日期" align="center" prop="updateTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.updateTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="150" fixed="right">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['system:apiAuth:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            :disabled="scope.row.apiStatus === -1"
            @click="handleDelete(scope.row)"
            v-hasPermi="['system:apiAuth:remove']"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改API谷歌验证配置对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="720px" append-to-body :close-on-click-modal="false">
      <el-form ref="form" :model="form" :rules="rules" label-width="108px">
        <el-form-item label="请求路径" prop="apiUrl">
          <el-input v-model="form.apiUrl" placeholder="请输入API请求路径" maxlength="500" />
        </el-form-item>
        <el-form-item label="权限标识" prop="apiPermission">
          <el-input v-model="form.apiPermission" placeholder="请输入API权限标识" maxlength="255" />
        </el-form-item>
        <el-form-item label="请求方式" prop="apiMethod">
          <el-select v-model="form.apiMethod" placeholder="请选择请求方式" style="width: 100%">
            <el-option
              v-for="item in methodOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="是否开启谷歌验证" prop="isNeedGoogle">
          <el-radio-group v-model="form.isNeedGoogle">
            <el-radio
              v-for="item in googleOptions"
              :key="item.value"
              :label="item.value"
            >{{ item.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态" prop="apiStatus">
          <el-radio-group v-model="form.apiStatus">
            <el-radio
              v-for="item in formStatusOptions"
              :key="item.value"
              :label="item.value"
            >{{ item.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="描述" prop="apiDesc">
          <el-input v-model="form.apiDesc" type="textarea" :rows="3" placeholder="请输入API描述信息" maxlength="500" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入内容" maxlength="500" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listApiAuth, getApiAuth, delApiAuth, addApiAuth, updateApiAuth, getGoogleAuthApis } from "@/api/system/apiAuth"

const googleAuthApisKey = "google_auth_apis"

export default {
  name: "ApiAuth",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // API谷歌验证配置表格数据
      apiAuthList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 日期范围
      dateRange: [],
      // 是否开启谷歌验证选项
      googleOptions: [
        { label: "否", value: 0 },
        { label: "是", value: 1 }
      ],
      // 请求方式选项
      methodOptions: [
        { label: "全部", value: "ALL" },
        { label: "GET", value: "GET" },
        { label: "POST", value: "POST" },
        { label: "PUT", value: "PUT" },
        { label: "DELETE", value: "DELETE" },
        { label: "PATCH", value: "PATCH" }
      ],
      // 状态选项
      statusOptions: [
        { label: "正常", value: 1 },
        { label: "禁用", value: 0 },
        { label: "删除", value: -1 }
      ],
      // 表单状态选项，删除状态仅用于列表展示和查询筛选
      formStatusOptions: [
        { label: "正常", value: 1 },
        { label: "禁用", value: 0 }
      ],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        apiPermission: undefined,
        apiUrl: undefined,
        apiMethod: undefined,
        isNeedGoogle: undefined,
        apiStatus: undefined
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        apiUrl: [
          { required: true, message: "API请求路径不能为空", trigger: "blur" }
        ],
        apiMethod: [
          { required: true, message: "请选择请求方式", trigger: "change" }
        ],
        isNeedGoogle: [
          { required: true, message: "请选择是否开启谷歌验证", trigger: "change" }
        ],
        apiStatus: [
          { required: true, message: "请选择API状态", trigger: "change" }
        ]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询API谷歌验证配置列表 */
    getList() {
      this.loading = true
      listApiAuth(this.addDateRange(this.queryParams, this.dateRange)).then(response => {
        this.apiAuthList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    // 取消按钮
    cancel() {
      this.open = false
      this.reset()
    },
    // 表单重置
    reset() {
      this.form = {
        id: undefined,
        apiPermission: undefined,
        apiUrl: undefined,
        apiMethod: "ALL",
        isNeedGoogle: 0,
        apiDesc: undefined,
        apiStatus: 1,
        remark: undefined
      }
      this.resetForm("form")
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.dateRange = []
      this.resetForm("queryForm")
      this.handleQuery()
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "添加API谷歌验证"
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const id = row.id || this.ids
      getApiAuth(id).then(response => {
        this.form = response.data
        if (!this.form.apiMethod) {
          this.form.apiMethod = "ALL"
        }
        this.open = true
        this.title = "修改API谷歌验证"
      })
    },
    /** 提交按钮 */
    submitForm: function() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id !== undefined) {
            updateApiAuth(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
              this.refreshGoogleAuthApisCache()
            })
          } else {
            addApiAuth(this.form).then(response => {
              this.$modal.msgSuccess("新增成功")
              this.open = false
              this.getList()
              this.refreshGoogleAuthApisCache()
            })
          }
        }
      })
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const ids = row.id || this.ids
      this.$modal.confirm('是否确认删除API谷歌验证编号为"' + ids + '"的数据项？').then(function() {
        return delApiAuth(ids)
      }).then(() => {
        this.getList()
        this.refreshGoogleAuthApisCache()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 刷新接口谷歌验证规则缓存，避免管理页调整后当前会话仍按旧规则判断 */
    refreshGoogleAuthApisCache() {
      getGoogleAuthApis().then(response => {
        const data = response.data || {}
        if (data.isNeedGoogleAuth && data.authApis && data.authApis.length > 0) {
          localStorage.setItem(googleAuthApisKey, JSON.stringify(data.authApis))
        } else {
          localStorage.removeItem(googleAuthApisKey)
        }
      }).catch(() => {
        localStorage.removeItem(googleAuthApisKey)
      })
    },
    /** 格式化是否开启谷歌验证 */
    formatGoogle(isNeedGoogle) {
      const option = this.googleOptions.find(item => item.value === isNeedGoogle)
      return option ? option.label : isNeedGoogle
    },
    /** 格式化API状态 */
    formatStatus(apiStatus) {
      const option = this.statusOptions.find(item => item.value === apiStatus)
      return option ? option.label : apiStatus
    },
    /** 格式化API状态标签颜色 */
    formatStatusTagType(apiStatus) {
      if (apiStatus === 1) {
        return "success"
      }
      if (apiStatus === -1) {
        return "danger"
      }
      return "info"
    }
  }
}
</script>

<style scoped>
::v-deep .api-auth-query-form {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
}

::v-deep .api-auth-query-form .el-form-item {
  margin-right: 24px;
  margin-bottom: 12px;
}

::v-deep .api-auth-query-buttons {
  margin-left: 0;
}
</style>
