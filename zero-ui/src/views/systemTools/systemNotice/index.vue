<template>
  <div class="app-container">
    <p style="margin: 0; line-height: 1.6">通知仅发送给当前在线账号，离线账号不会收到，上线后也不会补发。系统不保存通知历史；拥有发送权限的管理员可向全部在线账号发送通知。</p>
    <el-form class="system-notice-form" ref="form" :model="form" :rules="rules" label-width="110px" :disabled="busy" style="margin-top: 20px">
      <el-form-item label="接收方式">
        <el-radio-group v-model="form.targetType" @change="changeTarget">
          <el-radio value="ALL">全部在线账号</el-radio><el-radio value="USERS">指定在线账号</el-radio>
          <el-radio value="DEPT">按部门</el-radio><el-radio value="ROLE">按角色</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="在线账号" v-if="form.targetType === 'ALL'">
        <span>{{ onlineCount === null ? '尚未获取' : onlineCount + ' 个账号' }}</span>
        <el-button link type="primary" @click="refreshOnlineCount" style="margin-left: 12px">刷新在线人数</el-button>
        <span style="margin-left: 12px">发送时会重新核对在线状态</span>
      </el-form-item>
      <el-form-item label="接收账号" v-if="form.targetType === 'USERS'">
        <el-button @click="openUsers">选择在线账号</el-button><span style="margin-left: 12px">已选择 {{ form.userIds.length }} 个账号</span>
      </el-form-item>
      <el-form-item label="接收部门" v-if="form.targetType === 'DEPT'">
        <el-tree-select v-model="form.deptIds" :data="departmentTree" multiple show-checkbox check-strictly filterable clearable style="width: 100%" />
        <el-checkbox v-model="form.includeChildren">包含下级部门</el-checkbox>
      </el-form-item>
      <el-form-item label="接收角色" v-if="form.targetType === 'ROLE'">
        <el-select v-model="form.roleIds" multiple filterable clearable style="width: 100%" placeholder="拥有任意所选角色即命中">
          <el-option v-for="role in roles" :key="role.roleId" :label="role.roleName" :value="role.roleId" />
        </el-select>
      </el-form-item>
      <el-form-item label="消息级别"><el-radio-group v-model="form.messageLevel"><el-radio v-for="level in messageLevels" :key="level" :value="level">{{ levelLabels[level] }}</el-radio></el-radio-group></el-form-item>
      <el-form-item label="弹窗形式"><el-select v-model="form.displayType"><el-option v-for="type in displayTypes" :key="type" :value="type" :label="displayLabels[type]" /></el-select></el-form-item>
      <el-form-item label="展示时间">
        <el-radio-group v-model="manualClose"><el-radio :value="true">手动关闭</el-radio><el-radio :value="false">自动关闭</el-radio></el-radio-group>
        <el-input-number v-if="!manualClose" v-model="seconds" :min="1" :max="3600" :precision="0" style="margin-left: 16px" /><span v-if="!manualClose"> 秒</span>
      </el-form-item>
      <el-form-item label="通知标题" prop="title"><el-input v-model="form.title" placeholder="请输入通知标题" maxlength="100" /></el-form-item>
      <el-form-item class="system-notice-content-field" label="通知内容" prop="content">
        <editor class="system-notice-editor" v-model="form.content" :min-height="550" :read-only="busy" :toolbar="noticeToolbar"
          :upload-request="uploadNoticeImage" :image-types="['image/jpeg', 'image/png', 'image/gif', 'image/bmp']"
          @uploading-change="imageUploads = $event" @on-change="$refs.form?.clearValidate('content')" />
        <div>正文最多2000字、10张图片；图片单独上传，每张不超过5MB。顶部和右侧显示摘要，详情展示完整图文。</div>
      </el-form-item>
      <el-form-item class="system-notice-actions">
        <el-button @click="localPreview">本地预览</el-button>
        <el-button type="primary" v-hasPermi="['systemTools:systemNotice:send']" :loading="busy" @click="submit">发送通知</el-button>
      </el-form-item>
    </el-form>
    <el-dialog title="选择在线账号" v-model="usersVisible" width="min(900px, 94vw)" append-to-body @close="cancelUserSearch">
      <el-input v-model="query.keyword" clearable placeholder="输入账号或昵称自动搜索" @input="scheduleUserSearch" @clear="searchUsers" @keyup.enter="searchUsers" style="width: 260px" />
      <el-button @click="searchUsers">搜索</el-button><el-button @click="loadUsers">刷新在线状态</el-button>
      <el-table ref="usersTable" style="margin-top: 8px" :data="users" row-key="userId" v-loading="loadingUsers" @selection-change="selectUsers">
        <el-table-column type="selection" :reserve-selection="true" width="50" />
        <el-table-column prop="userId" label="账号ID" width="100" />
        <el-table-column prop="userName" label="账号" min-width="150" :show-overflow-tooltip="true" />
        <el-table-column prop="nickName" label="昵称" min-width="150" :show-overflow-tooltip="true" />
        <el-table-column prop="deptName" label="部门" min-width="150" :show-overflow-tooltip="true" />
      </el-table>
      <pagination :total="total" v-model:page="query.pageNum" v-model:limit="query.pageSize" :page-sizes="[10,20,50,100]" @pagination="loadUsers" />
      <template #footer><el-button type="primary" @click="usersVisible = false">确定（{{ form.userIds.length }} 个账号）</el-button></template>
    </el-dialog>
  </div>
</template>
<script>
import { getNoticeOptions, getNoticeOnlineUsers, previewNotice, sendNotice, uploadNoticeImage } from '@/api/systemTools/systemNotice'
import { createWebSocketPresenter } from '@/utils/websocketPresenter'
import { WebSocketMessageType } from '@/utils/websocketMessageType'
import Editor from '@/components/Editor'
import { noticeContent } from '@/utils/noticeRichContent'

// 连续输入结束后再查询，避免每个字符都刷新在线账号列表。
const USER_SEARCH_DELAY = 300
export default {
  name: 'SystemNotice',
  components: { Editor },
  data() {
    return {
      form: { targetType: 'ALL', userIds: [], deptIds: [], roleIds: [], includeChildren: true, title: '', content: '', contentFormat: 'HTML', displayType: 'NOTIFICATION', messageLevel: 'NORMAL' },
      rules: { title: [{ required: true, whitespace: true, message: '请输入通知标题', trigger: 'blur' }, { max: 100, message: '标题最多100字符' }], content: [{ validator: (rule, value, callback) => {
        const parsed = noticeContent(value, 'HTML')
        const visibleText = parsed.plainText
        if (!visibleText && !parsed.hasImages) return callback(new Error('请输入通知正文或插入有效图片'))
        if (value.length > 20000 || new TextEncoder().encode(value).length > 32768) return callback(new Error('图文内容过大，请减少格式或图片链接'))
        if (visibleText.length > 2000) return callback(new Error('通知正文最多2000字'))
        if (parsed.imageCount > 10) return callback(new Error('每条通知最多10张图片'))
        callback()
      }, trigger: 'blur' }] },
      noticeToolbar: [['bold', 'italic', 'underline', 'strike'], ['blockquote', 'code-block'], [{ list: 'ordered' }, { list: 'bullet' }], [{ indent: '-1' }, { indent: '+1' }], [{ size: ['small', false, 'large', 'huge'] }], [{ header: [1, 2, 3, false] }], [{ color: [] }, { background: [] }], [{ align: [] }], ['clean'], ['link', 'image']],
      imageUploads: 0,
      manualClose: true, seconds: 8, busy: false, usersVisible: false, loadingUsers: false,
      users: [], total: 0, onlineCount: null, query: { keyword: '', pageNum: 1, pageSize: 20 }, requestId: 0, userSearchTimer: null,
      departments: [], roles: [], displayTypes: [], messageLevels: [],
      displayLabels: { NOTIFICATION: '右侧通知卡片', MESSAGE: '顶部提示', MESSAGE_BOX: '居中弹窗' },
      levelLabels: { NORMAL: '普通', WARNING: '警告', ERROR: '错误' }
    }
  },
  computed: {
    departmentTree() {
      const nodes = new Map(this.departments.map(dept => [dept.deptId, { value: dept.deptId, label: dept.deptName, children: [] }]))
      const roots = []
      for (const dept of this.departments) {
        const node = nodes.get(dept.deptId)
        const parent = nodes.get(dept.parentId)
        if (parent && parent !== node) parent.children.push(node)
        else roots.push(node)
      }
      return roots
    }
  },
  async created() {
    this.noticePreview = createWebSocketPresenter()
    const { data } = await getNoticeOptions()
    this.departments = data.departments
    this.roles = data.roles
    this.displayTypes = data.displayTypes
    this.messageLevels = data.messageLevels
    await this.refreshOnlineCount()
  },
  deactivated() { this.cancelUserSearch(); this.noticePreview?.clear() },
  beforeUnmount() { this.cancelUserSearch(); this.noticePreview?.clear() },
  methods: {
    uploadNoticeImage,
    async refreshOnlineCount() {
      const response = await getNoticeOnlineUsers({ pageNum: 1, pageSize: 1 })
      this.onlineCount = response.total
    },
    changeTarget() {
      this.form.userIds = []; this.form.deptIds = []; this.form.roleIds = []
      this.$refs.usersTable?.clearSelection()
    },
    openUsers() { this.usersVisible = true; this.searchUsers() },
    cancelUserSearch() {
      clearTimeout(this.userSearchTimer)
      this.userSearchTimer = null
      this.requestId++
      this.loadingUsers = false
    },
    scheduleUserSearch() {
      this.cancelUserSearch()
      this.userSearchTimer = setTimeout(() => {
        // 请求失败由统一拦截器提示，这里只消费定时器发起请求的拒绝。
        this.searchUsers()?.catch(() => {})
      }, USER_SEARCH_DELAY)
    },
    searchUsers() { this.query.pageNum = 1; return this.loadUsers() },
    async loadUsers() {
      clearTimeout(this.userSearchTimer)
      this.userSearchTimer = null
      if (!this.usersVisible) return
      const current = ++this.requestId
      this.loadingUsers = true
      try {
        const response = await getNoticeOnlineUsers({ ...this.query, keyword: this.query.keyword.trim() })
        if (current === this.requestId) { this.users = response.rows; this.total = response.total }
      } finally { if (current === this.requestId) this.loadingUsers = false }
    },
    selectUsers(rows) { this.form.userIds = rows.map(row => row.userId) },
    async payload() {
      if (this.imageUploads) { this.$modal.msgWarning('图片上传中，请稍候再预览或发送'); return }
      const valid = await this.$refs.form.validate().catch(() => false)
      if (!valid) return
      const key = { USERS: 'userIds', DEPT: 'deptIds', ROLE: 'roleIds' }[this.form.targetType]
      if (key && !this.form[key].length) { this.$modal.msgWarning('请先选择接收目标'); return }
      if (key && this.form[key].length > 1000) { this.$modal.msgWarning('单次最多选择1000个目标'); return }
      if (!this.manualClose && (!Number.isInteger(this.seconds) || this.seconds < 1 || this.seconds > 3600)) { this.$modal.msgWarning('展示时间应为1～3600秒'); return }
      return { ...this.form, userIds: [...this.form.userIds], deptIds: [...this.form.deptIds], roleIds: [...this.form.roleIds], duration: this.manualClose ? 0 : this.seconds * 1000 }
    },
    async localPreview() {
      const data = await this.payload()
      if (!data) return
      this.noticePreview.clear()
      this.noticePreview.show({ id: 'preview-' + Date.now(), msgType: { messageType: WebSocketMessageType.SYSTEM_NOTICE }, msgData: { title: data.title, content: data.content, contentFormat: data.contentFormat }, displayOptions: { displayType: data.displayType, messageLevel: data.messageLevel, duration: data.duration } })
    },
    async submit() {
      if (this.busy) return
      const data = await this.payload()
      if (!data || this.busy) return
      this.busy = true
      try {
        const preview = await previewNotice(data)
        try { await this.$modal.confirm('当前匹配 ' + preview.data.matchedCount + ' 个在线账号，确认发送通知？实际发送时将重新核对在线状态。') } catch { return }
        const response = await sendNotice(data)
        if (response.data.status === 'SUBMITTED') this.$modal.msgSuccess(response.msg)
        else this.$modal.msgWarning(response.msg)
      } finally { this.busy = false }
    }
  }
}
</script>

<style scoped>
.system-notice-form {
  max-width: 1400px;
}
.system-notice-form > .el-form-item:not(.system-notice-content-field):not(.system-notice-actions) {
  max-width: 900px;
}
.system-notice-actions {
  margin-top: -8px;
}
.system-notice-actions :deep(.el-form-item__content) {
  justify-content: flex-end;
}
.system-notice-editor {
  width: 100%;
  min-width: 0;
}
</style>
