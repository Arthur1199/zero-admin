<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="IP地址" prop="ipAddr">
        <el-input
          v-model="queryParams.ipAddr"
          placeholder="请输入IP地址"
          clearable
@keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="IP地区" prop="ipRegion">
        <el-input
          v-model="queryParams.ipRegion"
          placeholder="请输入IP地区"
          clearable
@keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="白名单状态" clearable>
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
value-format="YYYY-MM-DD"
          type="daterange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
        ></el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :icon="resolveIcon('el-icon-search')" size="small" @click="handleQuery">搜索</el-button>
        <el-button :icon="resolveIcon('el-icon-refresh')" size="small" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
:icon="resolveIcon('el-icon-plus')"
size="small"
          @click="handleAdd"
          v-hasPermi="['system:ipWhite:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
:icon="resolveIcon('el-icon-edit')"
size="small"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['system:ipWhite:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
:icon="resolveIcon('el-icon-delete')"
size="small"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['system:ipWhite:remove']"
        >删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="ipWhiteList" @selection-change="handleSelectionChange" border>
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="id" width="80" />
      <el-table-column label="IP地址" align="center" prop="ipAddr" min-width="160" :show-overflow-tooltip="true" />
      <el-table-column label="IP地区" align="center" prop="ipRegion" min-width="180" :show-overflow-tooltip="true" />
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.status === '1' ? 'success' : 'info'">
            {{ formatStatus(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建者" align="center" prop="createBy" width="100" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="160">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" :show-overflow-tooltip="true" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="150">
        <template #default="scope">
          <el-button
size="small"
             link type="primary"
:icon="resolveIcon('el-icon-edit')"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['system:ipWhite:edit']"
          >修改</el-button>
          <el-button
size="small"
             link type="primary"
:icon="resolveIcon('el-icon-delete')"
            @click="handleDelete(scope.row)"
            v-hasPermi="['system:ipWhite:remove']"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
v-model:page="queryParams.pageNum"
v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改登录白名单对话框 -->
    <el-dialog
      :title="title"
v-model="open"
      width="520px"
      append-to-body
class="ip-white-dialog"
      :close-on-click-modal="false"
    >
      <el-form ref="form" :model="form" :rules="rules" label-width="86px">
        <el-form-item label="IP地址" prop="ipAddr">
          <el-input
            v-model="form.ipAddr"
            placeholder="请输入IP地址"
            :disabled="form.id !== undefined"
            @blur="handleIpAddrBlur"
          />
        </el-form-item>
        <el-form-item label="IP地区" prop="ipRegion">
          <el-input v-model="form.ipRegion" :placeholder="regionLoading ? '正在解析IP地区' : '不填写时由后台自动解析'" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio
              v-for="item in statusOptions"
              :key="item.value"
:value="item.value"
            >{{ item.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入内容" />
        </el-form-item>
      </el-form>
      <template #footer><div  class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div></template>
    </el-dialog>
  </div>
</template>

<script>
/**
 * 登录 IP 白名单维护；地区解析仅补充当前草稿的当前 IP，不覆盖后续手填内容或新弹窗。
 */
import { listIpWhite, getIpWhite, getIpWhiteRegion, delIpWhite, addIpWhite, updateIpWhite } from "@/api/system/ipWhite"

export default {
  name: "IpWhite",
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
      // 白名单表格数据
      ipWhiteList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // IP地区解析状态
      regionLoading: false,
      regionRequestId: 0,
      // 日期范围
      dateRange: [],
      // 状态选项
      statusOptions: [
        { label: "启用", value: "1" },
        { label: "关闭", value: "0" }
      ],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        ipAddr: undefined,
        ipRegion: undefined,
        status: undefined
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        ipAddr: [
          { required: true, message: "IP地址不能为空", trigger: "blur" }
        ],
        status: [
          { required: true, message: "状态不能为空", trigger: "change" }
        ]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询登录白名单列表 */
    getList() {
      this.loading = true
      listIpWhite(this.addDateRange(this.queryParams, this.dateRange)).then(response => {
        this.ipWhiteList = response.rows
        this.total = response.total
        this.loading = false
      }).catch(() => {
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
      this.regionRequestId++
      this.form = {
        id: undefined,
        ipAddr: undefined,
        ipRegion: undefined,
        status: "1",
        remark: undefined
      }
      this.regionLoading = false
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
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "添加登录白名单"
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const id = row.id || this.ids
      getIpWhite(id).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改登录白名单"
      }).catch(() => {
        // 读取失败不展示空白编辑框，接口错误由拦截器提示。
      })
    },
    /** IP输入完成后自动解析地区 */
    handleIpAddrBlur() {
      const ipAddr = (this.form.ipAddr || '').trim()
      if (!ipAddr || this.form.ipRegion) {
        return
      }
      const form = this.form
      const requestId = ++this.regionRequestId
      this.regionLoading = true
      getIpWhiteRegion(ipAddr).then(response => {
        // 只填充当前草稿的当前IP，不能覆盖用户后续手工填写的地区。
        if (requestId === this.regionRequestId && form === this.form &&
            (form.ipAddr || '').trim() === ipAddr && !form.ipRegion && response.data) {
          form.ipRegion = response.data
        }
      }).catch(() => {
        // 保留输入供失焦重试，错误提示复用请求拦截器。
      }).finally(() => {
        if (requestId === this.regionRequestId) this.regionLoading = false
      })
    },
    /** 提交按钮 */
    submitForm: function() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id !== undefined) {
            updateIpWhite(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            }).catch(() => {
              // 保存失败保留草稿，避免未处理的请求拒绝。
            })
          } else {
            addIpWhite(this.form).then(response => {
              this.$modal.msgSuccess("新增成功")
              this.open = false
              this.getList()
            }).catch(() => {
              // 保存失败保留草稿，避免未处理的请求拒绝。
            })
          }
        }
      })
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const ids = row.id || this.ids
      this.$modal.confirm('是否确认删除登录白名单编号为"' + ids + '"的数据项？').then(function() {
        return delIpWhite(ids)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 格式化状态 */
    formatStatus(status) {
      const option = this.statusOptions.find(item => item.value === status)
      return option ? option.label : status
    }
  }
}
</script>

<style scoped>
/* 组件局部布局与展示样式；跨页面主题及 body 弹层规则在 assets/styles 中统一维护。 */
:deep(.ip-white-dialog) {
  min-height: 0 !important;
  max-height: none !important;

  .el-dialog__body {
    flex: none !important;
    padding: 20px 26px 4px;
    overflow: visible;
  }

  .el-dialog__footer {
    padding: 10px 26px 18px;
  }

  .el-form-item {
    margin-bottom: 18px;
  }
}
</style>
