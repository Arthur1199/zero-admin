<template>
  <div class="app-container">
    <el-form ref="queryForm" :model="queryParams" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="标题" prop="title">
        <el-input v-model="queryParams.title" placeholder="请输入标题" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="域名URL" prop="resourceUrl">
        <el-input v-model="queryParams.resourceUrl" placeholder="请输入域名或地址" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="可见范围" prop="visibility">
        <el-select v-model="queryParams.visibility" placeholder="全部可见数据" clearable style="width: 180px">
          <el-option label="私有" value="0" />
          <el-option label="公开" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :icon="resolveIcon('el-icon-search')" @click="handleQuery">搜索</el-button>
        <el-button :icon="resolveIcon('el-icon-refresh')" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button v-hasPermi="['systemTools:memo:add']" type="primary" plain :disabled="detailLoading"
          :icon="resolveIcon('el-icon-plus')" @click="handleAdd">新增</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="memoList" border>
      <el-table-column label="标题" prop="title" min-width="180" :show-overflow-tooltip="true" />
      <el-table-column label="域名URL" prop="resourceUrl" min-width="240" :show-overflow-tooltip="true">
        <template #default="scope">
          <span>{{ scope.row.resourceUrl }}</span>
          <el-button v-if="getOpenableUrl(scope.row.resourceUrl)" class="memo-open-button" link type="primary"
            @click.stop="openResourceUrl(scope.row.resourceUrl)">打开</el-button>
        </template>
      </el-table-column>
      <el-table-column label="备注说明" prop="remark" min-width="200">
        <template #default="scope">
          <el-tooltip :content="scope.row.remark || ''" :disabled="!scope.row.remark"
            placement="top" :show-after="200">
            <span class="memo-remark">{{ scope.row.remark }}</span>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column label="可见范围" align="center" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.visibility === '1' ? 'warning' : 'info'">
            {{ scope.row.visibility === '1' ? '公开' : '私有' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建人" prop="createBy" min-width="120" :show-overflow-tooltip="true" />
      <el-table-column label="创建时间" prop="createTime" width="180" />
      <el-table-column label="更新人" prop="updateBy" min-width="120" :show-overflow-tooltip="true" />
      <el-table-column label="更新时间" prop="updateTime" width="180" />
      <el-table-column label="操作" align="center" width="210">
        <template #default="scope">
          <el-button link type="primary" :disabled="detailLoading" @click="handleOpen(scope.row, true)">查看</el-button>
          <el-button v-if="canManage(scope.row)" v-hasPermi="['systemTools:memo:edit']" link type="primary"
            :disabled="detailLoading" @click="handleOpen(scope.row, false)">修改</el-button>
          <el-button v-if="canManage(scope.row)" v-hasPermi="['systemTools:memo:remove']" link type="danger"
            :disabled="deleting" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="dialogTitle" v-model="open" width="min(760px, 94vw)" append-to-body
      :close-on-click-modal="false" :close-on-press-escape="!submitting" :show-close="!submitting"
      :before-close="beforeClose" destroy-on-close @close="clearContent" @closed="handleDialogClosed">
      <el-form ref="form" :model="form" :rules="rules" :disabled="submitting" label-width="100px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" :readonly="readOnly" placeholder="例如：公司Git仓库" />
        </el-form-item>
        <el-form-item label="域名URL" prop="resourceUrl">
          <el-input v-model="form.resourceUrl" :readonly="readOnly" placeholder="域名或访问地址，请勿在地址中填写密码、Token" />
        </el-form-item>
        <el-form-item label="可见范围" prop="visibility">
          <el-radio-group v-model="form.visibility" :disabled="readOnly">
            <el-radio value="0">私有</el-radio>
            <el-radio value="1">公开</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.visibility === '1'">
          <el-alert title="公开后，所有拥有备忘录菜单权限的账号均可查看完整内容，包括账号密码。"
            type="warning" :closable="false" show-icon />
        </el-form-item>
        <el-form-item v-else>
          <el-alert title="私有备忘录仅自己可以查看和维护。" type="info" :closable="false" show-icon />
        </el-form-item>
        <el-form-item label="备忘录内容" prop="memoContent">
          <el-input v-model="form.memoContent" type="textarea" :autosize="{ minRows: 8, maxRows: 18 }"
            :readonly="readOnly" autocomplete="off" placeholder="可填写用户名、密码及其它备忘信息，支持多行文本" />
        </el-form-item>
        <el-form-item label="备注说明" prop="remark">
          <el-input v-model="form.remark" type="textarea" :autosize="{ minRows: 2, maxRows: 5 }"
            :readonly="readOnly" placeholder="普通说明，请将密码等敏感信息填写到备忘录内容中" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button v-if="!readOnly" type="primary" :loading="submitting" @click="submitForm">保存</el-button>
        <el-button :disabled="submitting" @click="open = false">{{ readOnly ? '关闭' : '取消' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { listMemo, getMemo, addMemo, updateMemo, delMemo } from '@/api/systemTools/memo'

const emptyForm = () => ({
  memoId: undefined, title: '', resourceUrl: '', memoContent: '', remark: '',
  visibility: '0', version: undefined
})

export default {
  name: 'Memo',
  data() {
    return {
      loading: false, showSearch: true, memoList: [], total: 0,
      open: false, readOnly: false, dialogTitle: '', detailLoading: false,
      submitting: false, deleting: false, listSequence: 0, detailSequence: 0,
      pageActive: true, pageSequence: 0,
      queryParams: { pageNum: 1, pageSize: 10, title: undefined, resourceUrl: undefined, visibility: undefined },
      form: emptyForm(),
      rules: {
        title: [{ required: true, whitespace: true, message: '备忘录标题不能为空', trigger: 'blur' },
          { max: 200, message: '备忘录标题不能超过200个字符', trigger: 'blur' }],
        resourceUrl: [{ max: 2000, message: '资源地址不能超过2000个字符', trigger: 'blur' }],
        memoContent: [{ max: 100000, message: '备忘录内容不能超过100000个字符', trigger: 'blur' }],
        remark: [{ max: 1000, message: '备注说明不能超过1000个字符', trigger: 'blur' }],
        visibility: [{ required: true, message: '请选择可见范围', trigger: 'change' }]
      }
    }
  },
  created() {
    this.getList()
  },
  activated() {
    if (!this.pageActive) {
      this.pageActive = true
      this.getList()
    }
  },
  deactivated() {
    this.releaseContent()
  },
  beforeUnmount() {
    this.releaseContent()
  },
  methods: {
    /** 仅允许完整HTTP/HTTPS地址，拒绝脚本协议、内嵌凭证及浏览器会自动纠正的异常字符。 */
    getOpenableUrl(value) {
      if (typeof value !== 'string') return ''
      const text = value.trim()
      if (!/^https?:\/\/[^/]/i.test(text) || /[\s\\\u0000-\u001f\u007f]/u.test(text)) return ''
      try {
        const url = new URL(text)
        return url.hostname && !url.username && !url.password ? url.href : ''
      } catch {
        return ''
      }
    },
    /** 在用户点击时再次校验，新页面不持有当前后台窗口引用，也不发送来源地址。 */
    openResourceUrl(value) {
      const url = this.getOpenableUrl(value)
      if (url) window.open(url, '_blank', 'noopener,noreferrer')
    },
    /** 后端按创建人或超级管理员计算操作资格，按钮权限及SQL校验继续生效。 */
    canManage(row) {
      return row != null && row.canManage === true
    },
    async getList() {
      if (!this.pageActive) return
      const sequence = ++this.listSequence
      this.loading = true
      try {
        const response = await listMemo({ ...this.queryParams })
        if (sequence !== this.listSequence) return
        this.memoList = response.rows || []
        this.total = response.total || 0
      } catch {
        if (sequence === this.listSequence) {
          this.memoList = []
          this.total = 0
        }
      } finally {
        if (sequence === this.listSequence) this.loading = false
      }
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm('queryForm')
      this.handleQuery()
    },
    handleAdd() {
      if (!this.pageActive || this.submitting || this.detailLoading) return
      this.form = emptyForm()
      this.readOnly = false
      this.dialogTitle = '新增备忘录'
      this.open = true
    },
    /** 详情成功后才开窗；请求过期或页面离开后不再保留正文。 */
    async handleOpen(row, readOnly) {
      if (!this.pageActive || this.submitting || this.detailLoading || !row) return
      if (!readOnly && !this.canManage(row)) return
      const sequence = ++this.detailSequence
      this.detailLoading = true
      this.form = emptyForm()
      try {
        const { data } = await getMemo(row.memoId)
        if (sequence !== this.detailSequence) return
        if (!data || String(data.memoId) !== String(row.memoId)) {
          this.$modal.msgError('备忘录详情无效，请刷新后重试')
          return
        }
        if (!readOnly && !this.canManage(data)) {
          this.$modal.msgError('仅创建人或超级管理员可以修改备忘录')
          return
        }
        this.form = {
          memoId: data.memoId, title: data.title, resourceUrl: data.resourceUrl || '',
          memoContent: data.memoContent || '', remark: data.remark || '',
          visibility: data.visibility, version: data.version
        }
        this.readOnly = readOnly
        this.dialogTitle = readOnly ? '查看备忘录' : '修改备忘录'
        this.open = true
      } catch {
        // 请求拦截器统一提示错误，不打开空白详情。
      } finally {
        if (sequence === this.detailSequence) this.detailLoading = false
      }
    },
    async submitForm() {
      if (!this.pageActive || !this.open || this.submitting || this.readOnly) return
      const sequence = this.detailSequence
      this.submitting = true
      try {
        const valid = await this.$refs.form.validate().catch(() => false)
        if (!valid || !this.open || sequence !== this.detailSequence) return
        const payload = { ...this.form }
        if (this.form.visibility === '1') {
          await this.$modal.confirm('公开后，所有拥有备忘录菜单权限的账号都能查看完整内容，包括账号密码。是否确认保存？')
        }
        if (!this.open || sequence !== this.detailSequence) return
        const save = payload.memoId == null ? addMemo : updateMemo
        await save(payload)
        if (!this.pageActive || sequence !== this.detailSequence) return
        this.$modal.msgSuccess('保存成功')
        this.open = false
        this.clearContent()
        this.getList()
      } catch {
        // 取消确认或保存失败保留输入，错误提示复用请求拦截器。
      } finally {
        this.submitting = false
      }
    },
    async handleDelete(row) {
      if (!this.pageActive || this.submitting || this.deleting || !this.canManage(row)) return
      const sequence = this.pageSequence
      const { memoId, version } = row
      this.deleting = true
      try {
        await this.$modal.confirm('是否确认删除该备忘录？删除后无法恢复。')
        if (!this.pageActive || sequence !== this.pageSequence) return
        await delMemo(memoId, version)
        if (!this.pageActive || sequence !== this.pageSequence) return
        this.$modal.msgSuccess('删除成功')
        if (this.memoList.length === 1 && this.queryParams.pageNum > 1) this.queryParams.pageNum--
        this.getList()
      } catch {
        // 取消不调用删除接口，失败由请求拦截器提示。
      } finally {
        this.deleting = false
      }
    },
    beforeClose(done) {
      if (!this.submitting) done()
    },
    clearContent() {
      this.form = emptyForm()
    },
    /** 旧弹窗退出动画结束时，不能清空已经重新打开的新表单。 */
    handleDialogClosed() {
      if (!this.open) this.clearContent()
    },
    /** keep-alive切换及卸载都清空正文，废弃尚未完成的详情请求。 */
    releaseContent() {
      this.pageActive = false
      this.pageSequence++
      this.listSequence++
      this.loading = false
      this.detailSequence++
      this.detailLoading = false
      this.open = false
      this.clearContent()
    }
  }
}
</script>

<style scoped>
.memo-open-button {
  margin-left: 8px;
}

.memo-remark {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>

