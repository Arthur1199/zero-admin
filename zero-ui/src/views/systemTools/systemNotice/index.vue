<template>
  <div class="app-container">
    <p style="margin: 0; line-height: 1.6">通知仅发送给当前在线账号，离线账号不会收到，上线后也不会补发。系统不保存通知历史；拥有发送权限的管理员可向全部在线账号发送通知。</p>
    <el-form class="system-notice-form" ref="form" :model="form" :rules="rules" label-width="110px" :disabled="busy" style="margin-top: 20px">
      <el-form-item label="接收方式">
        <el-radio-group v-model="form.targetType" @change="changeTarget">
          <el-radio value="ALL">全部在线账号</el-radio><el-radio value="USERS">指定在线账号</el-radio>
          <el-radio value="DEPT">
            <span class="system-notice-department-label">
              按部门
              <button type="button" class="system-notice-department-help" aria-label="查看部门接收范围说明" title="查看部门接收范围说明" @click.stop.prevent="departmentHelpVisible = true">
                <AppIcon icon-class="el-icon-question" />
              </button>
            </span>
          </el-radio><el-radio value="ROLE">按角色</el-radio>
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
        <div class="system-notice-department-picker">
          <div class="system-notice-department-options">
            <el-checkbox v-model="departmentExpanded" :disabled="!departments.length" @change="expandDepartments">展开/折叠</el-checkbox>
            <el-checkbox :model-value="allDepartmentsChecked" :disabled="!departments.length" @change="checkAllDepartments">全选/全不选</el-checkbox>
            <el-checkbox v-model="departmentLinked" @change="refreshDepartmentChecks">父子联动</el-checkbox>
            <span v-if="form.deptIds.length" class="system-notice-department-count">已选 {{ form.deptIds.length }} 个部门</span>
          </div>
          <el-input v-model="departmentKeyword" placeholder="搜索部门名称" clearable :disabled="!departments.length"
            @input="$refs.departmentTree?.filter($event.trim().toLowerCase())" />
          <el-tree ref="departmentTree" class="tree-border system-notice-department-tree" :data="departmentTree"
            show-checkbox default-expand-all node-key="value" :check-strictly="!departmentLinked"
            :filter-node-method="filterDepartment" empty-text="暂无可选部门" @check="updateDepartmentChecks" />
        </div>
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
    <el-dialog title="部门接收范围说明" v-model="departmentHelpVisible" width="min(860px, 94vw)" append-to-body :close-on-click-modal="false">
      <div class="system-notice-department-guide">
        <p>勾选「包含下级部门」后，通知会匹配所选部门及其所有层级下级部门的在线账号，下级部门即使没有在树上勾选，也会纳入接收范围。</p>
        <p>取消「包含下级部门」后，只匹配树上实际勾选部门的在线账号。已单独勾选的下级部门仍会收到通知。</p>
        <p>「父子联动」控制树上的勾选操作。开启后，勾选或取消父部门会同步勾选或取消下级部门；取消某个下级后，父部门可能显示为半选，半选部门不作为接收目标。「包含下级部门」控制发送时是否自动扩展接收范围。</p>
        <p>例如，只勾选某个父部门时，开启「包含下级部门」会同时通知该部门及其所有层级下级部门的在线账号；关闭后只通知直接归属该部门的在线账号。若只想通知某个子部门直属的在线账号，应仅勾选该子部门，并取消「包含下级部门」。</p>
        <p>选择多个部门时合并接收范围，同一账号只匹配一次。仅向发送时仍在线的有效账号发送，离线账号上线后不会补发。</p>
      </div>
      <template #footer>
        <el-button type="primary" @click="departmentHelpVisible = false">我知道了</el-button>
      </template>
    </el-dialog>
    <el-dialog title="选择在线账号" v-model="usersVisible" width="min(900px, 94vw)" append-to-body @close="cancelUserSearch">
      <el-input v-model="query.keyword" clearable placeholder="输入账号或昵称自动搜索" @input="scheduleUserSearch" @clear="searchUsers" @keyup.enter="searchUsers" style="width: 260px" />
      <el-button @click="searchUsers">搜索</el-button><el-button @click="loadUsers">刷新在线状态</el-button>
      <el-table ref="usersTable" style="margin-top: 8px" :data="users" row-key="userId" v-loading="loadingUsers" @selection-change="selectUsers" border>
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
/**
 * 在线系统通知发送页；区分发送目标、显示形式、本地预览和实际发送，处理在线账号搜索的过期响应。
 */
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
      manualClose: true, seconds: 8, busy: false, usersVisible: false, loadingUsers: false, departmentHelpVisible: false,
      users: [], total: 0, onlineCount: null, query: { keyword: '', pageNum: 1, pageSize: 20 }, requestId: 0, userSearchTimer: null,
      departments: [], departmentKeyword: '', departmentExpanded: true, departmentLinked: true,
      roles: [], displayTypes: [], messageLevels: [],
      displayLabels: { NOTIFICATION: '右侧通知卡片', MESSAGE: '顶部提示', MESSAGE_BOX: '居中弹窗' },
      levelLabels: { NORMAL: '普通', WARNING: '警告', ERROR: '错误' }
    }
  },
  computed: {
    allDepartmentsChecked() {
      return this.departments.length > 0 && this.form.deptIds.length === this.departments.length
    },
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
    this.departments = data.departments ?? []
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
      this.departmentKeyword = ''
      this.departmentExpanded = true
      this.$refs.usersTable?.clearSelection()
    },
    filterDepartment(keyword, department) {
      return department.label.toLowerCase().includes(keyword)
    },
    expandDepartments(expanded) {
      const tree = this.$refs.departmentTree
      for (const department of this.departments) {
        const node = tree?.getNode(department.deptId)
        if (node && !node.isLeaf) node.expanded = expanded
      }
    },
    checkAllDepartments(checked) {
      this.$refs.departmentTree?.setCheckedKeys(checked ? this.departments.map(department => department.deptId) : [])
      this.updateDepartmentChecks()
    },
    async refreshDepartmentChecks() {
      // 等父子联动设置生效，再按当前选择重算勾选及半选状态。
      await this.$nextTick()
      this.$refs.departmentTree?.setCheckedKeys(this.form.deptIds)
      this.updateDepartmentChecks()
    },
    updateDepartmentChecks() {
      // 半选父部门不能加入发送目标，否则「包含下级部门」会匹配未勾选的分支。
      this.form.deptIds = this.$refs.departmentTree?.getCheckedKeys() ?? []
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
/* 组件局部布局与展示样式；跨页面主题及 body 弹层规则在 assets/styles 中统一维护。 */
.system-notice-form {
  max-width: 1400px;
}
.system-notice-form > .el-form-item:not(.system-notice-content-field):not(.system-notice-actions) {
  max-width: 900px;
}
.system-notice-department-picker {
  width: 100%;
  min-width: 0;
}
.system-notice-department-label,
.system-notice-department-help {
  display: inline-flex;
  align-items: center;
}
.system-notice-department-label {
  gap: 4px;
}
.system-notice-department-help {
  padding: 0;
  border: 0;
  background: transparent;
  color: inherit;
  cursor: pointer;
}
.system-notice-department-guide p {
  margin: 0 0 12px;
  line-height: 1.7;
}
.system-notice-department-options {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  column-gap: 24px;
}
.system-notice-department-options > .el-checkbox {
  margin-right: 0;
}
.system-notice-department-count {
  line-height: 1;
  white-space: nowrap;
}
.system-notice-department-tree {
  max-height: 300px;
  overflow: auto;
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
