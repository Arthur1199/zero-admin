<template>
  <div class="app-container user-page">
    <el-row :gutter="20" class="user-layout-row">
      <splitpanes :horizontal="this.$store.getters.device === 'mobile'" class="default-theme user-splitpanes">
        <!--部门数据-->
        <pane size="16" class="user-dept-pane">
          <el-col class="user-dept-panel" @wheel="handleDeptWheel">
            <div class="head-container user-dept-filter">
              <el-input v-model="deptName" placeholder="请输入部门名称" clearable size="small" :prefix-icon="resolveIcon('el-icon-search')" style="margin-bottom: 20px" />
            </div>
            <div ref="deptTreeBody" class="head-container user-dept-tree">
              <el-tree :data="deptOptions" :props="defaultProps" :expand-on-click-node="false" :filter-node-method="filterNode" ref="tree" node-key="id" default-expand-all highlight-current @node-click="handleNodeClick" />
            </div>
          </el-col>
        </pane>
        <!--用户数据-->
        <pane size="84">
          <el-col>
            <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
              <el-form-item label="用户名称" prop="userName">
                <el-input v-model="queryParams.userName" placeholder="请输入用户名称" clearable style="width: 240px" @keyup.enter="handleQuery" />
              </el-form-item>
              <el-form-item label="手机号码" prop="phonenumber">
                <el-input v-model="queryParams.phonenumber" placeholder="请输入手机号码" clearable style="width: 240px" @keyup.enter="handleQuery" />
              </el-form-item>
              <el-form-item label="账号状态" prop="status">
                <el-select v-model="queryParams.status" placeholder="账号状态" clearable style="width: 240px">
                  <el-option v-for="dict in dict.type.sys_normal_disable" :key="dict.value" :label="dict.label" :value="dict.value" />
                </el-select>
              </el-form-item>
              <el-form-item label="删除状态" prop="delFlag">
                <el-select v-model="queryParams.delFlag" placeholder="删除状态" clearable style="width: 240px">
                  <el-option label="未删除" value="0" />
                  <el-option label="已删除" value="2" />
                </el-select>
              </el-form-item>
              <el-form-item label="登录锁定" prop="loginLock">
                <el-select v-model="queryParams.loginLock" placeholder="登录锁定" clearable style="width: 240px">
                  <el-option label="已锁定" :value="1" />
                  <el-option label="未锁定" :value="0" />
                </el-select>
              </el-form-item>
              <el-form-item label="创建时间">
                <el-date-picker v-model="dateRange" style="width: 240px" value-format="YYYY-MM-DD" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期"></el-date-picker>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :icon="resolveIcon('el-icon-search')" size="small" @click="handleQuery">搜索</el-button>
                <el-button :icon="resolveIcon('el-icon-refresh')" size="small" @click="resetQuery">重置</el-button>
              </el-form-item>
            </el-form>

            <el-row :gutter="10" class="mb8">
              <el-col :span="1.5">
                <el-button type="primary" plain :icon="resolveIcon('el-icon-plus')" size="small" @click="handleAdd" v-hasPermi="['system:user:add']">新增</el-button>
              </el-col>
              <el-col :span="1.5">
                <el-button type="success" plain :icon="resolveIcon('el-icon-edit')" size="small" :disabled="single" @click="handleUpdate" v-hasPermi="['system:user:edit']">修改</el-button>
              </el-col>
              <el-col :span="1.5">
                <el-button type="danger" plain :icon="resolveIcon('el-icon-delete')" size="small" :disabled="multiple" @click="handleDelete" v-hasPermi="['system:user:remove']">删除</el-button>
              </el-col>
              <el-col :span="1.5">
                <el-button type="info" plain :icon="resolveIcon('el-icon-upload2')" size="small" @click="handleImport" v-hasPermi="['system:user:import']">导入</el-button>
              </el-col>
              <el-col :span="1.5">
                <el-button type="warning" plain :icon="resolveIcon('el-icon-download')" size="small" @click="handleExport" v-hasPermi="['system:user:export']">导出</el-button>
              </el-col>
              <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns"></right-toolbar>
              <el-col v-if="$auth.hasPermi('system:user:unlockLogin')" :span="1.5" class="login-unlock-help-col">
                <button type="button" class="login-unlock-help el-button--small" aria-label="查看登录解锁说明" title="查看登录解锁说明" @click="openUnlockHelp = true">
                  登录解锁
                  <AppIcon icon-class="el-icon-question" />
                </button>
              </el-col>
            </el-row>

            <el-table v-loading="loading" :data="userList" @selection-change="handleSelectionChange" border>
              <el-table-column type="selection" width="50" align="center" :selectable="row => !isDeleted(row)" />
              <el-table-column label="用户编号" align="center" key="userId" prop="userId" v-if="columns[0].visible" />
              <el-table-column label="用户名称" align="center" key="userName" prop="userName" v-if="columns[1].visible" :show-overflow-tooltip="true" />
              <el-table-column label="用户昵称" align="center" key="nickName" prop="nickName" v-if="columns[2].visible" min-width="160" class-name="user-long-text" />
              <el-table-column label="部门" align="center" key="deptName" prop="dept.deptName" v-if="columns[3].visible" min-width="180" class-name="user-long-text" />
              <el-table-column label="用户角色" align="center" key="roles" prop="roles" v-if="columns[4].visible" min-width="240" class-name="user-long-text" :formatter="formatUserRoles" />
              <el-table-column label="手机号码" align="center" key="phonenumber" prop="phonenumber" v-if="columns[5].visible" width="120" />
              <el-table-column label="账号状态" align="center" key="status" v-if="columns[6].visible">
                <template #default="scope">
                  <el-switch v-model="scope.row.status" active-value="0" inactive-value="1" :disabled="isDeleted(scope.row)" @change="handleStatusChange(scope.row)"></el-switch>
                </template>
              </el-table-column>
              <el-table-column label="删除状态" align="center" prop="delFlag" v-if="columns[7].visible" min-width="100">
                <template #default="scope">
                  <el-tag :type="isDeleted(scope.row) ? 'danger' : 'info'">{{ isDeleted(scope.row) ? '已删除' : '未删除' }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="登录锁定" align="center" prop="loginLock" v-if="columns[8].visible" min-width="100">
                <template #default="scope">
                  <el-tag :type="isLoginLocked(scope.row) ? 'danger' : 'info'">{{ isLoginLocked(scope.row) ? '已锁定' : '未锁定' }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="创建人" align="center" prop="createBy" v-if="columns[9].visible" min-width="120" :show-overflow-tooltip="true">
                <template #default="scope">
                  <span>{{ scope.row.createBy || '—' }}</span>
                </template>
              </el-table-column>
              <el-table-column label="创建时间" align="center" prop="createTime" v-if="columns[10].visible" width="160">
                <template #default="scope">
                  <span>{{ parseTime(scope.row.createTime) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="更新人" align="center" prop="updateBy" v-if="columns[11].visible" min-width="120" :show-overflow-tooltip="true">
                <template #default="scope">
                  <span>{{ scope.row.updateBy || '—' }}</span>
                </template>
              </el-table-column>
              <el-table-column label="更新时间" align="center" prop="updateTime" v-if="columns[12].visible" width="160">
                <template #default="scope">
                  <span>{{ parseTime(scope.row.updateTime) || '—' }}</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" align="center" width="160" class-name="small-padding fixed-width">
                <template #default="scope">
                  <el-button v-if="isDeleted(scope.row) && scope.row.userId !== 1" size="small" link type="primary" :icon="resolveIcon('el-icon-refresh')" :loading="restoringUserId === scope.row.userId" :disabled="restoringUserId !== null" @click="handleRestore(scope.row)" v-hasPermi="['system:user:remove']">恢复</el-button>
                  <template v-if="scope.row.userId !== 1 && !isDeleted(scope.row)">
                  <el-button size="small"  link type="primary" :icon="resolveIcon('el-icon-edit')" @click="handleUpdate(scope.row)" v-hasPermi="['system:user:edit']">修改</el-button>
                  <el-button size="small"  link type="primary" :icon="resolveIcon('el-icon-delete')" @click="handleDelete(scope.row)" v-hasPermi="['system:user:remove']">删除</el-button>
                  <el-dropdown v-if="showMoreMenu(scope.row)" size="small" @command="(command) => handleCommand(command, scope.row)">
                    <el-button size="small"  link type="primary" :icon="resolveIcon('el-icon-d-arrow-right')">更多</el-button>
                    <template #dropdown><el-dropdown-menu >
                      <el-dropdown-item v-if="isLoginLocked(scope.row) && $auth.hasPermi('system:user:unlockLogin')" command="handleUnlockLogin" :icon="resolveIcon('el-icon-unlock')">登录解锁</el-dropdown-item>
                      <el-dropdown-item v-if="$auth.hasPermi('system:user:resetPwd')" command="handleResetPwd" :icon="resolveIcon('el-icon-key')">重置密码</el-dropdown-item>
                      <el-dropdown-item v-if="$auth.hasPermi('system:user:edit')" command="handleAuthRole" :icon="resolveIcon('el-icon-circle-check')">分配角色</el-dropdown-item>
                    </el-dropdown-menu></template>
                  </el-dropdown>
                  </template>
                </template>
              </el-table-column>
            </el-table>

            <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
          </el-col>
        </pane>
      </splitpanes>
    </el-row>

    <!-- 添加或修改用户配置对话框 -->
    <el-dialog :title="title" v-model="open" width="50%" append-to-body :close-on-click-modal="false">
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="用户昵称" prop="nickName">
              <el-input v-model="form.nickName" placeholder="请输入用户昵称" maxlength="30" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="归属部门" prop="deptId">
              <treeselect v-model="form.deptId" :options="enabledDeptOptions" :show-count="true" placeholder="请选择归属部门" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="手机号码" prop="phonenumber">
              <el-input v-model="form.phonenumber" placeholder="请输入手机号码" maxlength="11" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱" maxlength="50" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item v-if="form.userId == undefined" label="用户名称" prop="userName">
              <el-input v-model="form.userName" placeholder="请输入用户名称（登录时账号名称）" maxlength="30" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item v-if="form.userId == undefined" label="用户密码" prop="password">
              <el-input v-model="form.password" placeholder="请输入用户密码" type="password" maxlength="20" show-password />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="用户性别">
              <el-select v-model="form.sex" placeholder="请选择性别">
                <el-option v-for="dict in dict.type.sys_user_sex" :key="dict.value" :label="dict.label" :value="dict.value"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-radio-group v-model="form.status">
                <el-radio v-for="dict in dict.type.sys_normal_disable" :key="dict.value" :value="dict.value">{{ dict.label }}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="岗位">
              <el-select v-model="form.postIds" multiple placeholder="请选择岗位">
                <el-option v-for="item in postOptions" :key="item.postId" :label="item.postName" :value="item.postId" :disabled="item.status == 1" ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="角色">
              <el-select v-model="form.roleIds" multiple placeholder="请选择角色">
                <el-option v-for="item in roleOptions" :key="item.roleId" :label="item.roleName" :value="item.roleId" :disabled="item.status == 1"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input v-model="form.remark" type="textarea" placeholder="请输入内容"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer><div  class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div></template>
    </el-dialog>

    <el-dialog v-if="$auth.hasPermi('system:user:unlockLogin')" title="登录解锁说明" v-model="openUnlockHelp" width="min(860px, 94vw)" append-to-body :close-on-click-modal="false">
      <div class="login-unlock-guide">
        <p>登录解锁用于解除因密码错误次数过多而产生的账号登录锁定。</p>
        <ul>
          <li>开启登录锁定保护后，账号在配置的密码错误统计时间内达到错误次数上限，会被锁定并阻止继续登录。保护开启期间，统计时间结束不会自动解除已经产生的账号锁定。</li>
          <li>关闭登录锁定保护后，已锁定账号仍可使用正确密码登录，登录成功后会自动解除锁定，并清空密码错误次数记录。</li>
          <li>登录解锁会解除该账号的登录锁定，并清空其密码错误次数记录；解锁后仍需使用正确密码登录。</li>
          <li>操作方法：找到已锁定的账号，在该行“更多 → 登录解锁”中确认操作。未锁定账号不显示此操作。</li>
          <li>此操作不会重置密码、启用已停用的账号、恢复已删除的账号，也不会变更角色或菜单权限。</li>
          <li>仅可对当前账号数据权限范围内的普通账号操作；超级管理员账号不提供此入口。</li>
        </ul>
      </div>
      <template #footer>
        <el-button type="primary" @click="openUnlockHelp = false">我知道了</el-button>
      </template>
    </el-dialog>

    <!-- 用户导入对话框 -->
    <el-dialog :title="upload.title" v-model="upload.open" width="50%" append-to-body :close-on-click-modal="false">
      <el-upload ref="upload" :limit="1" accept=".xlsx, .xls" :headers="upload.headers" :action="upload.url + '?updateSupport=' + upload.updateSupport" :disabled="upload.isUploading" :on-progress="handleFileUploadProgress" :on-success="handleFileSuccess" :auto-upload="false" drag>
        <AppIcon icon-class="el-icon-upload" class=""></AppIcon>
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        <template #tip><div class="el-upload__tip text-center" >
          <div class="el-upload__tip" >
            <el-checkbox v-model="upload.updateSupport" />是否更新已经存在的用户数据
          </div>
          <span>仅允许导入xls、xlsx格式文件。</span>
          <el-link type="primary" :underline="false" style="font-size: 12px; vertical-align: baseline" @click="importTemplate">下载模板</el-link>
          <button v-if="$auth.hasPermi('system:user:import')" type="button" class="user-import-help el-button--small" aria-label="查看用户导入说明" title="查看用户导入说明" @click="openImportHelp = true">
            导入说明
            <AppIcon icon-class="el-icon-question" />
          </button>
        </div></template>
      </el-upload>
      <template #footer><div  class="dialog-footer">
        <el-button type="primary" @click="submitFileForm">确 定</el-button>
        <el-button @click="upload.open = false">取 消</el-button>
      </div></template>
    </el-dialog>
    <el-dialog v-if="$auth.hasPermi('system:user:import')" title="用户导入说明" v-model="openImportHelp" width="min(860px, 94vw)" append-to-body :close-on-click-modal="false">
      <div class="user-import-guide">
        <p>请下载最新模板，前三列依次填写“部门编号、用户名称、用户昵称”。请保留模板表头，按以下规则填写各列。</p>
        <dl>
          <div>
            <dt>部门编号（非必填，建议填写）</dt>
            <dd>对应“部门管理”中显示的部门编号，用于指定用户所属部门。请填写编号数字，不是部门名称。所填部门应在当前账号的数据权限范围内；新增时留空不关联部门，更新时留空保留原部门。</dd>
          </div>
          <div>
            <dt>用户名称（必填）</dt>
            <dd>用户登录系统时使用的账号名称，最多30个字符。系统按此账号识别新增或更新的用户；账号已存在时，需勾选“是否更新已经存在的用户数据”才能更新。</dd>
          </div>
          <div>
            <dt>用户昵称（新增必填，更新可留空）</dt>
            <dd>用户在系统中显示的名称，可与登录账号不同，最多30个字符。更新时留空保留原昵称；用户名称和用户昵称均不能包含脚本字符。</dd>
          </div>
          <div>
            <dt>用户邮箱（非必填）</dt>
            <dd>填写有效邮箱地址，例如 user@example.com，最多50个字符。填写后会校验邮箱格式，留空表示不填写邮箱。</dd>
          </div>
          <div>
            <dt>手机号码（非必填）</dt>
            <dd>最多11个字符，按实际手机号码填写。建议将单元格设置为文本格式，避免科学计数法或数字格式改变号码。</dd>
          </div>
          <div>
            <dt>用户性别（非必填）</dt>
            <dd>填写中文“男”“女”或“未知”，不要填写数字编码。新增时留空使用数据库默认值（项目初始化默认“男”），更新时留空保留原性别；如需表示未知，请明确填写“未知”。</dd>
          </div>
          <div>
            <dt>账号状态（非必填）</dt>
            <dd>填写中文“正常”或“停用”，不要填写数字编码。“正常”表示账号可参与登录校验，“停用”表示禁止登录。新增时留空使用数据库默认值（项目初始化默认“正常”），更新时留空保留原状态。</dd>
          </div>
        </dl>
        <div class="user-import-note">
          <p><strong>更新已有用户时注意</strong></p>
          <p>使用完整模板更新时，用户邮箱或手机号码单元格留空会清空原值；部门编号、用户昵称、用户性别和账号状态留空则保留原值。请核对后再导入。</p>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="openImportHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
/**
 * 用户维护与部门树筛选；共享部门数据的在途读取，并区分删除、恢复、解锁、重置密码和角色分配操作。
 */
import { listUser, getUser, delUser, restoreUser, addUser, updateUser, resetUserPwd, unlockUserLogin, changeUserStatus, deptTreeSelect } from "@/api/system/user"
import { getToken } from "@/utils/auth"
import { checkPermi } from "@/utils/permission"
import Treeselect from '@/components/TreeSelect'
import { Splitpanes, Pane } from "splitpanes"
import "splitpanes/dist/splitpanes.css"

export default {
  name: "User",
  dicts: ['sys_normal_disable', 'sys_user_sex'],
  components: { Treeselect, Splitpanes, Pane },
  data() {
    return {
      // 遮罩层
      loading: true,
      restoringUserId: null,
      formLoading: false,
      deptTreeLoaded: false,
      deptTreeRequest: null,
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
      // 用户表格数据
      userList: null,
      // 弹出层标题
      title: "",
      // 所有部门树选项
      deptOptions: undefined,
      // 过滤掉已禁用部门树选项
      enabledDeptOptions: undefined,
      // 是否显示弹出层
      open: false,
      // 登录解锁说明独立于用户编辑弹窗，不读取或提交用户数据。
      openUnlockHelp: false,
      // 仅展示模板字段说明，关闭说明时保留导入弹窗及已选择的文件。
      openImportHelp: false,
      // 部门名称
      deptName: undefined,
      // 默认密码
      initPassword: undefined,
      // 日期范围
      dateRange: [],
      // 岗位选项
      postOptions: [],
      // 角色选项
      roleOptions: [],
      // 表单参数
      form: {},
      defaultProps: {
        children: "children",
        label: "label"
      },
      // 用户导入参数
      upload: {
        // 是否显示弹出层（用户导入）
        open: false,
        // 弹出层标题（用户导入）
        title: "",
        // 是否禁用上传
        isUploading: false,
        // 是否更新已经存在的用户数据
        updateSupport: 0,
        // 设置上传的请求头部
        headers: { Authorization: "Bearer " + getToken() },
        // 上传的地址
        url: import.meta.env.VITE_APP_BASE_API + "/system/user/importData"
      },
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        userName: undefined,
        phonenumber: undefined,
        status: undefined,
        delFlag: undefined,
        loginLock: undefined,
        deptId: undefined
      },
      // 列信息
      columns: [
        { key: 0, label: `用户编号`, visible: true },
        { key: 1, label: `用户名称`, visible: true },
        { key: 2, label: `用户昵称`, visible: true },
        { key: 3, label: `部门`, visible: true },
        { key: 4, label: `用户角色`, visible: true },
        { key: 5, label: `手机号码`, visible: true },
        { key: 6, label: `账号状态`, visible: true },
        { key: 7, label: `删除状态`, visible: true },
        { key: 8, label: `登录锁定`, visible: true },
        { key: 9, label: `创建人`, visible: true },
        { key: 10, label: `创建时间`, visible: true },
        { key: 11, label: `更新人`, visible: true },
        { key: 12, label: `更新时间`, visible: true }
      ],
      // 表单校验
      rules: {
        userName: [
          { required: true, message: "用户名称不能为空", trigger: "blur" },
          { min: 2, max: 20, message: '用户名称长度必须介于 2 和 20 之间', trigger: 'blur' }
        ],
        nickName: [
          { required: true, message: "用户昵称不能为空", trigger: "blur" }
        ],
        password: [
          { required: true, message: "用户密码不能为空", trigger: "blur" },
          { min: 5, max: 20, message: '用户密码长度必须介于 5 和 20 之间', trigger: 'blur' },
          { pattern: /^[^<>"'|\\]+$/, message: "不能包含非法字符：< > \" ' \\\ |", trigger: "blur" }
        ],
        email: [
          {
            type: "email",
            message: "请输入正确的邮箱地址",
            trigger: ["blur", "change"]
          }
        ],
        phonenumber: [
          {
            pattern: /^1[3|4|5|6|7|8|9][0-9]\d{8}$/,
            message: "请输入正确的手机号码",
            trigger: "blur"
          }
        ]
      }
    }
  },
  watch: {
    // 根据名称筛选部门树
    deptName(val) {
      this.$refs.tree.filter(val)
    }
  },
  created() {
    this.getList()
    this.getDeptTree().catch(() => {
      this.deptOptions = []
      this.enabledDeptOptions = []
    })
    this.getConfigKey("sys.user.initPassword").then(response => {
      this.initPassword = response.msg
    }).catch(() => {
      // 默认值读取失败时允许在新增表单手工填写，不输出默认密码。
    })
  },
  methods: {
    isDeleted(row) {
      return String(row?.delFlag) === '2'
    },
    /** 角色列表以英文逗号拼接，兼容无角色及旧接口缺省值。 */
    formatUserRoles(row) {
      const roles = Array.isArray(row.roles) ? row.roles : []
      return [...new Set(roles.map(role => role?.roleName).filter(Boolean))].join(',') || '—'
    },
    handleDeptWheel(event) {
      const treeBody = this.$refs.deptTreeBody
      if (!treeBody) {
        return
      }
      event.preventDefault()
      event.stopPropagation()
      treeBody.scrollTop += event.deltaY
    },
    /** 查询用户列表 */
    getList() {
      this.loading = true
      return listUser(this.addDateRange(this.queryParams, this.dateRange)).then(response => {
          this.userList = response.rows
          this.total = response.total
          this.loading = false
        }
      ).catch(() => {
        this.loading = false
      })
    },
    /** 查询部门下拉树结构 */
    getDeptTree() {
      if (this.deptTreeRequest) return this.deptTreeRequest
      this.deptTreeRequest = deptTreeSelect().then(response => {
        this.deptOptions = Array.isArray(response.data) ? response.data : []
        this.enabledDeptOptions = this.filterDisabledDept(JSON.parse(JSON.stringify(this.deptOptions)))
        this.deptTreeLoaded = true
      }).finally(() => {
        this.deptTreeRequest = null
      })
      return this.deptTreeRequest
    },
    // 过滤禁用的部门
    filterDisabledDept(deptList) {
      return deptList.filter(dept => {
        if (dept.disabled) {
          return false
        }
        if (dept.children && dept.children.length) {
          dept.children = this.filterDisabledDept(dept.children)
        }
        return true
      })
    },
    // 筛选节点
    filterNode(value, data) {
      if (!value) return true
      return data.label.indexOf(value) !== -1
    },
    // 节点单击事件
    handleNodeClick(data) {
      this.queryParams.deptId = data.id
      this.handleQuery()
    },
    // 用户状态修改
    handleStatusChange(row) {
      let text = row.status === "0" ? "启用" : "停用"
      this.$modal.confirm('确认要"' + text + '""' + row.userName + '"用户吗？').then(function() {
        return changeUserStatus(row.userId, row.status)
      }).then(() => {
        this.$modal.msgSuccess(text + "成功")
      }).catch(function() {
        row.status = row.status === "0" ? "1" : "0"
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
        userId: undefined,
        deptId: undefined,
        userName: undefined,
        nickName: undefined,
        password: undefined,
        phonenumber: undefined,
        email: undefined,
        sex: undefined,
        status: "0",
        remark: undefined,
        postIds: [],
        roleIds: []
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
      this.queryParams.deptId = undefined
      this.queryParams.delFlag = undefined
      this.queryParams.loginLock = undefined
      this.$refs.tree.setCurrentKey(null)
      this.handleQuery()
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      const selectable = selection.filter(item => !this.isDeleted(item))
      this.ids = selectable.map(item => item.userId)
      this.single = selectable.length != 1
      this.multiple = !selectable.length
    },
    // 更多操作触发
    handleCommand(command, row) {
      switch (command) {
        case "handleResetPwd":
          this.handleResetPwd(row)
          break
        case "handleUnlockLogin":
          this.handleUnlockLogin(row)
          break
        case "handleAuthRole":
          this.handleAuthRole(row)
          break
        default:
          break
      }
    },
    isLoginLocked(row) {
      return row.loginLock === 1 || row.loginLock === "1"
    },
    showMoreMenu(row) {
      return checkPermi(['system:user:resetPwd', 'system:user:edit'])
        || (this.isLoginLocked(row) && checkPermi(['system:user:unlockLogin']))
    },
    /** 新增按钮操作 */
    async handleAdd() {
      if (this.formLoading) return
      this.formLoading = true
      this.reset()
      try {
        const [response] = await Promise.all([getUser(), this.deptTreeLoaded ? null : this.getDeptTree()])
        this.postOptions = response.posts || []
        this.roleOptions = response.roles || []
        this.open = true
        this.title = "添加用户"
        this.form.password = this.initPassword
      } catch {
        this.postOptions = []
        this.roleOptions = []
      } finally {
        this.formLoading = false
      }
    },
    /** 修改按钮操作 */
    async handleUpdate(row) {
      if (this.formLoading) return
      this.formLoading = true
      this.reset()
      const userId = row.userId || this.ids
      try {
        const [response] = await Promise.all([getUser(userId), this.deptTreeLoaded ? null : this.getDeptTree()])
        this.form = response.data
        this.postOptions = response.posts || []
        this.roleOptions = response.roles || []
        this.form["postIds"] = response.postIds || []
        this.form["roleIds"] = response.roleIds || []
        this.open = true
        this.title = "修改用户"
        this.form.password = ""
      } catch {
        this.postOptions = []
        this.roleOptions = []
      } finally {
        this.formLoading = false
      }
    },
    /** 重置密码按钮操作 */
    handleResetPwd(row) {
      this.$prompt('请输入"' + row.userName + '"的新密码', "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        closeOnClickModal: false,
        inputPattern: /^.{5,20}$/,
        inputErrorMessage: "用户密码长度必须介于 5 和 20 之间",
        inputValidator: (value) => {
          if (/<|>|"|'|\||\\/.test(value)) {
            return "不能包含非法字符：< > \" ' \\\ |"
          }
        },
      }).then(({ value }) => {
          return resetUserPwd(row.userId, value).then(response => {
            this.$modal.msgSuccess("修改成功")
          })
        }).catch(() => {})
    },
    /** 登录解锁操作 */
    handleUnlockLogin(row) {
      this.$modal.confirm('确认要对用户"' + row.userName + '"进行登录解锁吗?').then(function() {
        return unlockUserLogin(row.userId)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("解锁成功")
      }).catch(() => {})
    },
    /** 分配角色操作 */
    handleAuthRole: function(row) {
      const userId = row.userId
      this.$router.push("/system/user-auth/role/" + userId)
    },
    /** 提交按钮 */
    submitForm: function() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.userId != undefined) {
            updateUser(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            }).catch(() => {
              // 保存失败保留用户草稿，错误提示由请求拦截器负责。
            })
          } else {
            addUser(this.form).then(response => {
              this.$modal.msgSuccess("新增成功")
              this.open = false
              this.getList()
            }).catch(() => {
              // 保存失败保留用户草稿，错误提示由请求拦截器负责。
            })
          }
        }
      })
    },
    /** 恢复账号，不补回删除时移除的角色和岗位授权 */
    async handleRestore(row) {
      if (!this.isDeleted(row) || !row.userId || this.restoringUserId !== null) return
      this.restoringUserId = row.userId
      try {
        await this.$modal.confirm('确认恢复用户“' + row.userName + '”？仅恢复账号，原角色和岗位授权不会自动恢复,需要重新设置')
        await restoreUser(row.userId)
        this.$modal.msgSuccess("恢复成功")
        if (this.userList.length === 1 && this.queryParams.pageNum > 1) this.queryParams.pageNum--
        await this.getList()
      } catch (_) {
        // 取消或接口失败时保留列表，接口错误由请求拦截器提示。
      } finally {
        this.restoringUserId = null
      }
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const userIds = row.userId || this.ids
      this.$modal.confirm('是否确认删除用户编号为"' + userIds + '"的数据项？').then(function() {
        return delUser(userIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 导出按钮操作 */
    handleExport() {
      // 导出时重新合并当前日期范围，避免日期变更后尚未搜索而沿用上一次查询范围。
      this.download('system/user/export', this.addDateRange({
        ...this.queryParams
      }, this.dateRange), `user_${new Date().getTime()}.xlsx`)
    },
    /** 导入按钮操作 */
    handleImport() {
      this.upload.title = "用户导入"
      this.upload.open = true
    },
    /** 下载模板操作 */
    importTemplate() {
      this.download('system/user/importTemplate', {
      }, `user_template_${new Date().getTime()}.xlsx`)
    },
    // 文件上传中处理
    handleFileUploadProgress(event, file, fileList) {
      this.upload.isUploading = true
    },
    // 文件上传成功处理
    handleFileSuccess(response, file, fileList) {
      this.upload.open = false
      this.upload.isUploading = false
      this.$refs.upload.clearFiles()
      this.$alert("<div style='overflow: auto;overflow-x: hidden;max-height: 70vh;padding: 10px 20px 0;'>" + response.msg + "</div>", "导入结果", { dangerouslyUseHTMLString: true })
      this.getList()
    },
    // 提交上传文件
    submitFileForm() {
      this.$refs.upload.submit()
    }
  }
}
</script>

<style lang="scss" scoped>
.login-unlock-help-col {
  display: flex;
  align-items: center;
}

.login-unlock-help,
.user-import-help {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 0;
  border: 0;
  background: transparent;
  color: inherit;
  // 字号复用 el-button--small，连同主题对小按钮的字号调整一起生效。
  font-family: inherit;
  cursor: pointer;
}

.user-import-help {
  margin-left: 8px;
  vertical-align: baseline;
}

.login-unlock-guide,
.user-import-guide {
  line-height: 1.7;
  overflow-wrap: anywhere;
  max-height: 60vh;
  overflow-y: auto;
  padding-right: 8px;

  p { margin: 0 0 12px; }
  ul { padding-left: 20px; margin: 0; }
  li + li { margin-top: 6px; }
}

// 字段标题、说明与底部注意事项共用左边缘，避免列表缩进导致最后一段错位。
.user-import-guide {
  dl { margin: 0; }
  dl > div { margin-bottom: 14px; }
  dt { font-weight: 600; }
  dd { margin: 0; }

  .user-import-note {
    padding-top: 12px;
    border-top: 1px solid var(--zero-border);

    p:first-child { margin-bottom: 4px; }
    p:last-child { margin-bottom: 0; }
  }
}

// 这三列直接完整换行展示，长英文也不能撑出单元格或被省略。
:deep(td.user-long-text .cell) {
  white-space: normal;
  overflow-wrap: anywhere;
  word-break: normal;
  text-overflow: clip;
}

.user-page {
  height: calc(100vh - 160px);
  overflow: hidden;
}

.user-layout-row,
.user-splitpanes {
  height: 100%;
}

.user-dept-pane {
  height: 100%;
}

.user-dept-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  overscroll-behavior: contain;
}

.user-dept-filter {
  flex: 0 0 auto;
}

.user-dept-tree {
  flex: 1;
  min-height: 0;
  overflow: auto;
  overscroll-behavior: contain;
}

@media (max-width: 768px) {
  .user-page,
  .user-layout-row,
  .user-splitpanes,
  .user-dept-pane,
  .user-dept-panel {
    height: auto;
  }

  .user-dept-tree {
    max-height: 320px;
  }
}
</style>
