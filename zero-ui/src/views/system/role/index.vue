<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch">
      <el-form-item label="角色名称" prop="roleName">
        <el-input
          v-model="queryParams.roleName"
          placeholder="请输入角色名称"
          clearable
          style="width: 240px"
@keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="角色英文名称" prop="roleKey">
        <el-input
          v-model="queryParams.roleKey"
          placeholder="请输入角色英文名称"
          clearable
          style="width: 240px"
@keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select
          v-model="queryParams.status"
          placeholder="角色状态"
          clearable
          style="width: 240px"
        >
          <el-option
            v-for="dict in dict.type.sys_normal_disable"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
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
          v-hasPermi="['system:role:add']"
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
          v-hasPermi="['system:role:edit']"
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
          v-hasPermi="['system:role:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
:icon="resolveIcon('el-icon-download')"
size="small"
          @click="handleExport"
          v-hasPermi="['system:role:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="roleList" @selection-change="handleSelectionChange" border>
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="角色编号" prop="roleId" width="120" />
      <el-table-column label="角色名称" prop="roleName" :show-overflow-tooltip="true" width="150" />
      <el-table-column label="角色英文名称" prop="roleKey" :show-overflow-tooltip="true" width="150" />
      <el-table-column label="显示顺序" prop="roleSort" width="100" />
      <el-table-column label="数据权限" prop="dataScope" min-width="190" :formatter="formatDataScope">
        <template #header>
          <span class="data-scope-label">
            数据权限
            <button type="button" class="data-scope-help" aria-label="查看数据权限说明" title="查看数据权限说明" @click.stop="openScopeHelp = true">
              <AppIcon icon-class="el-icon-question" />
            </button>
          </span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" width="100">
        <template #default="scope">
          <el-switch
            v-model="scope.row.status"
            active-value="0"
            inactive-value="1"
            @change="handleStatusChange(scope.row)"
          ></el-switch>
        </template>
      </el-table-column>
      <el-table-column label="创建人" align="center" prop="createBy" min-width="120" :show-overflow-tooltip="true">
        <template #default="scope">
          <span>{{ scope.row.createBy || '—' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="更新人" align="center" prop="updateBy" min-width="120" :show-overflow-tooltip="true">
        <template #default="scope">
          <span>{{ scope.row.updateBy || '—' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="更新时间" align="center" prop="updateTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.updateTime) || '—' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <template v-if="scope.row.roleId !== 1">
          <el-button
size="small"
             link type="primary"
:icon="resolveIcon('el-icon-edit')"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['system:role:edit']"
          >修改</el-button>
          <el-button
size="small"
             link type="primary"
:icon="resolveIcon('el-icon-delete')"
            @click="handleDelete(scope.row)"
            v-hasPermi="['system:role:remove']"
          >删除</el-button>
          <el-dropdown size="small" @command="(command) => handleCommand(command, scope.row)" v-if="$auth.hasPermiOr(['system:role:dataScope', 'system:role:authUser'])">
            <el-button size="small"  link type="primary" :icon="resolveIcon('el-icon-d-arrow-right')">更多</el-button>
            <template #dropdown><el-dropdown-menu >
              <el-dropdown-item command="handleDataScope" :icon="resolveIcon('el-icon-circle-check')"
                v-if="$auth.hasPermi('system:role:dataScope')">数据权限</el-dropdown-item>
              <el-dropdown-item command="handleAuthUser" :icon="resolveIcon('el-icon-user')"
                v-if="$auth.hasPermi('system:role:authUser')">分配用户</el-dropdown-item>
            </el-dropdown-menu></template>
          </el-dropdown>
          </template>
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

    <!-- 添加或修改角色配置对话框 -->
    <el-dialog :title="title" v-model="open" width="50%" append-to-body :close-on-click-modal="false" :close-on-press-escape="!submitting" :show-close="!submitting">
      <el-form ref="form" :model="form" :rules="rules" label-width="130px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item prop="roleKey">
          <template #label><span >
            <el-tooltip content="角色英文名称，如：admin、common 等" placement="top">
              <AppIcon icon-class="el-icon-question" class=""></AppIcon>
            </el-tooltip>
            角色英文名称
          </span></template>
          <el-input v-model="form.roleKey" placeholder="请输入角色英文名称" />
        </el-form-item>
        <el-form-item label="角色顺序" prop="roleSort">
          <el-input-number v-model="form.roleSort" controls-position="right" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio
              v-for="dict in dict.type.sys_normal_disable"
              :key="dict.value"
:value="dict.value"
            >{{dict.label}}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="菜单权限" class="permission-tree-item">
          <el-checkbox v-model="menuExpand" @change="handleCheckedTreeExpand($event, 'menu')">展开/折叠</el-checkbox>
          <el-checkbox v-model="menuNodeAll" @change="handleCheckedTreeNodeAll($event, 'menu')">全选/全不选</el-checkbox>
          <el-checkbox v-model="form.menuCheckStrictly" @change="handleCheckedTreeConnect($event, 'menu')">父子联动</el-checkbox>
          <el-tree
            class="tree-border"
            :data="menuOptions"
            show-checkbox
            ref="menu"
            node-key="id"
            :check-strictly="!form.menuCheckStrictly"
            empty-text="加载中，请稍候"
            :props="defaultProps"
          ></el-tree>
        </el-form-item>
        <el-form-item v-if="form.roleId == undefined || $auth.hasPermi('system:role:dataScope')" label="数据权限" prop="dataScope">
          <template #label>
            <span class="data-scope-label">
              <button type="button" class="data-scope-help" aria-label="查看数据权限说明" title="查看数据权限说明" @click="openScopeHelp = true">
                <AppIcon icon-class="el-icon-question" />
              </button>
              数据权限
            </span>
          </template>
          <el-select v-model="form.dataScope" placeholder="请选择数据权限" @change="handleCreateDataScopeChange">
            <el-option v-for="item in dataScopeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.dataScope === '2' && (form.roleId == undefined || $auth.hasPermi('system:role:dataScope'))" label="部门范围" class="permission-tree-item" v-loading="createDeptLoading">
          <el-checkbox v-model="createDeptExpand" @change="handleCreateDeptExpand">展开/折叠</el-checkbox>
          <el-checkbox v-model="createDeptNodeAll" @change="value => $refs.createDept?.setCheckedNodes(value ? createDeptOptions : [])">全选/全不选</el-checkbox>
          <el-checkbox v-model="form.deptCheckStrictly">父子联动</el-checkbox>
          <el-tree ref="createDept" class="tree-border" :data="createDeptOptions" show-checkbox default-expand-all
            node-key="id" :check-strictly="!form.deptCheckStrictly" :props="defaultProps" empty-text="暂无可选部门" />
          <el-button v-if="!createDeptLoaded && !createDeptLoading" link type="primary" @click="loadCreateDeptTree">重新加载部门</el-button>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入内容"></el-input>
        </el-form-item>
      </el-form>
      <template #footer><div  class="dialog-footer">
        <el-button type="primary" :loading="submitting" :disabled="createDeptLoading" @click="submitForm">确 定</el-button>
        <el-button :disabled="submitting" @click="cancel">取 消</el-button>
      </div></template>
    </el-dialog>

    <!-- 分配角色数据权限对话框 -->
    <el-dialog :title="title" v-model="openDataScope" width="50%" append-to-body :close-on-click-modal="false">
      <el-form :model="form" label-width="130px">
        <el-form-item label="角色名称">
          <el-input v-model="form.roleName" :disabled="true" />
        </el-form-item>
        <el-form-item label="角色英文名称">
          <el-input v-model="form.roleKey" :disabled="true" />
        </el-form-item>
        <el-form-item label="权限范围">
          <template #label>
            <span class="data-scope-label">
              <button type="button" class="data-scope-help" aria-label="查看数据权限说明" title="查看数据权限说明" @click="openScopeHelp = true">
                <AppIcon icon-class="el-icon-question" />
              </button>
              权限范围
            </span>
          </template>
          <el-select v-model="form.dataScope" @change="dataScopeSelectChange">
            <el-option
              v-for="item in dataScopeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="数据权限" class="permission-tree-item" v-show="form.dataScope == 2">
          <el-checkbox v-model="deptExpand" @change="handleCheckedTreeExpand($event, 'dept')">展开/折叠</el-checkbox>
          <el-checkbox v-model="deptNodeAll" @change="handleCheckedTreeNodeAll($event, 'dept')">全选/全不选</el-checkbox>
          <el-checkbox v-model="form.deptCheckStrictly" @change="handleCheckedTreeConnect($event, 'dept')">父子联动</el-checkbox>
          <el-tree
            class="tree-border"
            :data="deptOptions"
            show-checkbox
            default-expand-all
            ref="dept"
            node-key="id"
            :check-strictly="!form.deptCheckStrictly"
            empty-text="加载中，请稍候"
            :props="defaultProps"
          ></el-tree>
        </el-form-item>
      </el-form>
      <template #footer><div  class="dialog-footer">
        <el-button type="primary" @click="submitDataScope">确 定</el-button>
        <el-button @click="cancelDataScope">取 消</el-button>
      </div></template>
    </el-dialog>

    <el-dialog title="数据权限说明" v-model="openScopeHelp" width="min(860px, 94vw)" append-to-body :close-on-click-modal="false">
      <div class="data-scope-guide">
        <p>菜单权限决定“能进入哪些页面、使用哪些操作”；数据权限决定“这些页面中能看到哪些数据”。两者需要同时满足。</p>
        <p><strong>统一示例：</strong>张三属于“研发部”，研发部下有“研发一组”；“测试部”与研发部平级。以下以已接入数据权限的用户列表为例。</p>
        <dl>
          <div v-for="item in dataScopeHelp" :key="item.name">
            <dt>{{ item.name }}</dt>
            <dd>{{ item.description }}</dd>
            <dd><strong>举例：</strong>{{ item.example }}</dd>
          </div>
        </dl>
        <p><strong>选择时注意：</strong></p>
        <ul>
          <li>“本部门”取实际登录人的所属部门。同一角色分配给不同部门的人，看到的部门数据会不同。</li>
          <li>自定义范围按最终勾选的部门生效，不会仅因勾选父部门就自动包含所有下级；开启父子联动时，会同步勾选当前树中的下级部门。</li>
          <li>多个正常角色中，参与当前功能权限判断的数据范围取并集；其中有“全部数据权限”时，该功能不再按部门或本人缩小范围。超级管理员不受此范围过滤。</li>
          <li>仅对已经接入数据权限过滤的功能生效，不代表系统所有页面都会自动过滤。页面本身的查询条件和其他业务限制仍然有效。</li>
          <li>“仅本人”按登录用户编号识别，不等于“我创建的所有记录”；没有接入本人识别的功能可能不显示数据。</li>
        </ul>
      </div>
      <template #footer>
        <el-button type="primary" @click="openScopeHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
/**
 * 角色基础信息、菜单及数据范围维护；区分新增时的初始数据范围和已有角色的独立数据权限操作。
 */
import { listRole, getRole, delRole, addRole, updateRole, dataScope, changeRoleStatus, deptTreeSelect, creationDeptTreeSelect, getNextRoleSort } from "@/api/system/role"
import { treeselect as menuTreeselect, roleMenuTreeselect } from "@/api/system/menu"

export default {
  name: "Role",
  dicts: ['sys_normal_disable'],
  data() {
    return {
      // 遮罩层
      loading: true,
      formLoading: false,
      submitting: false,
      openScopeHelp: false,
      dataScopeHelp: [
        { name: "全部数据权限", description: "不按部门或本人限制数据范围，仍需具备对应菜单和操作权限。", example: "张三可查看研发部、研发一组、测试部等全部部门的用户。" },
        { name: "自定义数据权限", description: "由管理员为该角色勾选可查看的部门，范围固定为所选部门。", example: "只勾选研发一组和测试部，张三可查看这两个部门的用户，不能查看未勾选的研发部用户。" },
        { name: "本部门数据权限", description: "只允许查看登录人所属部门的数据，不包含下级部门。", example: "张三可查看研发部用户，不能查看研发一组或测试部用户。" },
        { name: "本部门及以下数据权限", description: "允许查看登录人所属部门及所有下级部门的数据。", example: "张三可查看研发部、研发一组及其下级部门用户，不能查看平级测试部用户。" },
        { name: "仅本人数据权限", description: "只允许查看与当前登录用户编号对应的本人数据。", example: "在用户列表中，张三只能查看自己的用户记录，看不到同部门其他人的记录。" }
      ],
      createDeptLoading: false,
      createDeptLoaded: false,
      savedDeptKeys: [],
      createDeptOptions: [],
      createDeptExpand: true,
      createDeptNodeAll: false,
      createDeptRequestId: 0,
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
      // 角色表格数据
      roleList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 是否显示弹出层（数据权限）
      openDataScope: false,
      menuExpand: false,
      menuNodeAll: false,
      deptExpand: true,
      deptNodeAll: false,
      // 日期范围
      dateRange: [],
      // 数据范围选项
      dataScopeOptions: [
        {
          value: "1",
          label: "全部数据权限"
        },
        {
          value: "2",
          label: "自定义数据权限"
        },
        {
          value: "3",
          label: "本部门数据权限"
        },
        {
          value: "4",
          label: "本部门及以下数据权限"
        },
        {
          value: "5",
          label: "仅本人数据权限"
        }
      ],
      // 菜单列表
      menuOptions: [],
      // 部门列表
      deptOptions: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        roleName: undefined,
        roleKey: undefined,
        status: undefined
      },
      // 表单参数
      form: {},
      defaultProps: {
        children: "children",
        label: "label"
      },
      // 表单校验
      rules: {
        dataScope: [
          { required: true, message: "请选择数据权限", trigger: "change" }
        ],
        roleName: [
          { required: true, message: "角色名称不能为空", trigger: "blur" }
        ],
        roleKey: [
          { required: true, message: "角色英文名称不能为空", trigger: "blur" }
        ],
        roleSort: [
          { required: true, message: "角色顺序不能为空", trigger: "blur" }
        ]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 复用表单的范围名称，兼容数字和字符串编码，缺失值不推断为全部权限。 */
    formatDataScope(row, column, value) {
      if (value == null || value === '') return '未配置'
      return this.dataScopeOptions.find(item => item.value === String(value))?.label || '未知数据权限'
    },
    /** 查询角色列表 */
    getList() {
      this.loading = true
      listRole(this.addDateRange(this.queryParams, this.dateRange)).then(response => {
          this.roleList = response.rows
          this.total = response.total
          this.loading = false
        }
      ).catch(() => {
        this.loading = false
      })
    },
    /** 查询菜单树结构 */
    getMenuTreeselect() {
      return menuTreeselect()
    },
    // 所有菜单节点数据
    getMenuAllCheckedKeys() {
      // 目前被选中的菜单节点
      let checkedKeys = this.$refs.menu.getCheckedKeys()
      // 半选中的菜单节点
      let halfCheckedKeys = this.$refs.menu.getHalfCheckedKeys()
      checkedKeys.unshift.apply(checkedKeys, halfCheckedKeys)
      return checkedKeys
    },
    // 所有部门节点数据
    getDeptAllCheckedKeys() {
      // 目前被选中的部门节点
      let checkedKeys = this.$refs.dept.getCheckedKeys()
      // 半选中的部门节点
      let halfCheckedKeys = this.$refs.dept.getHalfCheckedKeys()
      checkedKeys.unshift.apply(checkedKeys, halfCheckedKeys)
      return checkedKeys
    },
    /** 根据角色ID查询菜单树结构 */
    getRoleMenuTreeselect(roleId) {
      return roleMenuTreeselect(roleId)
    },
    /** 根据角色ID查询部门树结构 */
    getDeptTree(roleId) {
      return deptTreeSelect(roleId)
    },
    // 角色状态修改
    handleStatusChange(row) {
      let text = row.status === "0" ? "启用" : "停用"
      this.$modal.confirm('确认要"' + text + '""' + row.roleName + '"角色吗？').then(function() {
        return changeRoleStatus(row.roleId, row.status)
      }).then(() => {
        this.$modal.msgSuccess(text + "成功")
      }).catch(function() {
        row.status = row.status === "0" ? "1" : "0"
      })
    },
    // 取消按钮
    cancel() {
      if (this.submitting) return
      this.open = false
      this.reset()
    },
    // 取消按钮（数据权限）
    cancelDataScope() {
      this.openDataScope = false
      this.reset()
    },
    // 表单重置
    reset() {
      this.createDeptRequestId++
      this.createDeptLoading = false
      this.createDeptLoaded = false
      this.createDeptOptions = []
      this.createDeptExpand = true
      this.createDeptNodeAll = false
      this.savedDeptKeys = []
      if (this.$refs.menu != undefined) {
        this.$refs.menu.setCheckedKeys([])
      }
      this.menuExpand = false,
      this.menuNodeAll = false,
      this.deptExpand = true,
      this.deptNodeAll = false,
      this.form = {
        roleId: undefined,
        roleName: undefined,
        roleKey: undefined,
        roleSort: 0,
        status: "0",
        menuIds: [],
        deptIds: [],
        dataScope: "1",
        menuCheckStrictly: true,
        deptCheckStrictly: true,
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
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.roleId)
      this.single = selection.length!=1
      this.multiple = !selection.length
    },
    // 更多操作触发
    handleCommand(command, row) {
      switch (command) {
        case "handleDataScope":
          this.handleDataScope(row)
          break
        case "handleAuthUser":
          this.handleAuthUser(row)
          break
        default:
          break
      }
    },
    // 树权限（展开/折叠）
    handleCheckedTreeExpand(value, type) {
      if (type == 'menu') {
        let treeList = this.menuOptions
        for (let i = 0; i < treeList.length; i++) {
          this.$refs.menu.store.nodesMap[treeList[i].id].expanded = value
        }
      } else if (type == 'dept') {
        let treeList = this.deptOptions
        for (let i = 0; i < treeList.length; i++) {
          this.$refs.dept.store.nodesMap[treeList[i].id].expanded = value
        }
      }
    },
    // 树权限（全选/全不选）
    handleCheckedTreeNodeAll(value, type) {
      if (type == 'menu') {
        this.$refs.menu.setCheckedNodes(value ? this.menuOptions: [])
      } else if (type == 'dept') {
        this.$refs.dept.setCheckedNodes(value ? this.deptOptions: [])
      }
    },
    // 树权限（父子联动）
    handleCheckedTreeConnect(value, type) {
      if (type == 'menu') {
        this.form.menuCheckStrictly = value ? true: false
      } else if (type == 'dept') {
        this.form.deptCheckStrictly = value ? true: false
      }
    },
    /** 新增按钮操作 */
    async handleAdd() {
      if (this.formLoading) return
      this.formLoading = true
      this.reset()
      this.form.dataScope = "4"
      this.menuOptions = []
      try {
        const [response, sortResponse] = await Promise.all([this.getMenuTreeselect(), getNextRoleSort()])
        if (!Array.isArray(response.data)) {
          this.$modal.msgError("菜单信息不完整，请重试")
          return
        }
        this.menuOptions = response.data
        if (!Number.isInteger(sortResponse.data) || sortResponse.data < 1) {
          this.$modal.msgError("角色顺序信息不完整，请重试")
          return
        }
        this.form.roleSort = sortResponse.data
        this.open = true
        this.title = "添加角色"
      } catch (error) {
        // 请求拦截器已经提示错误，初始化读取失败时不打开残缺表单。
        this.menuOptions = []
      } finally {
        this.formLoading = false
      }
    },
    /** 切换数据范围时清理旧部门选择。 */
    handleCreateDataScopeChange(value) {
      this.createDeptRequestId++
      this.createDeptLoading = false
      this.createDeptLoaded = false
      this.createDeptOptions = []
      this.createDeptNodeAll = false
      this.form.deptIds = []
      if (value === '2') this.loadCreateDeptTree()
    },
    /** 部门读取结果仅更新当前角色草稿，失败可重试。 */
    async loadCreateDeptTree() {
      if (this.createDeptLoading) return
      const requestId = ++this.createDeptRequestId
      this.createDeptLoading = true
      try {
        const response = this.form.roleId == undefined ? await creationDeptTreeSelect() : await deptTreeSelect(this.form.roleId)
        if (requestId !== this.createDeptRequestId || this.form.dataScope !== '2') return
        const options = this.form.roleId == undefined ? response.data : response.depts
        if (!Array.isArray(options)) {
          this.$modal.msgError("部门信息不完整，请重试")
          return
        }
        this.createDeptOptions = options
        // 修改时重新读取已保存的勾选，避免范围切换清空后无法回显。
        if (this.form.roleId != undefined) this.form.deptIds = response.checkedKeys || []
        await this.$nextTick()
        if (requestId !== this.createDeptRequestId) return
        this.$refs.createDept?.setCheckedKeys(this.form.deptIds || [])
        this.createDeptLoaded = true
      } catch {
        // 接口错误由拦截器提示，当前草稿保留供重试。
      } finally {
        if (requestId === this.createDeptRequestId) this.createDeptLoading = false
      }
    },
    handleCreateDeptExpand(value) {
      for (const node of this.createDeptOptions) {
        const treeNode = this.$refs.createDept?.getNode(node.id)
        if (treeNode) treeNode.expanded = value
      }
    },
    /** 修改按钮操作 */
    async handleUpdate(row) {
      if (this.formLoading) return
      this.formLoading = true
      this.reset()
      const roleId = row.roleId || this.ids
      this.menuOptions = []
      try {
        // 两个只读请求齐备后再回显，任一失败都不会使用上次的权限树。
        const [roleMenu, response] = await Promise.all([this.getRoleMenuTreeselect(roleId), getRole(roleId)])
        if (!response.data || !Array.isArray(roleMenu.menus)) {
          this.$modal.msgError("角色信息不完整，请重试")
          return
        }
        this.menuOptions = roleMenu.menus
        this.form = { ...response.data, dataScope: response.data.dataScope || "1" }
        this.title = "修改角色"
        let deptTree
        if (this.form.dataScope === '2' && this.$auth.hasPermi('system:role:dataScope')) {
          deptTree = await deptTreeSelect(roleId)
          if (!Array.isArray(deptTree.depts)) throw new Error("部门信息不完整")
          this.createDeptOptions = deptTree.depts
          this.form.deptIds = deptTree.checkedKeys || []
          this.createDeptLoaded = true
        }
        this.open = true
        await this.$nextTick()
        if (deptTree) this.$refs.createDept?.setCheckedKeys(this.form.deptIds)
        const checkedKeys = roleMenu.checkedKeys || []
        checkedKeys.forEach(v => {
          this.$refs.menu?.setChecked(v, true, false)
        })
      } catch (error) {
        this.menuOptions = []
        this.open = false
      } finally {
        this.formLoading = false
      }
    },
    /** 选择角色权限范围触发 */
    dataScopeSelectChange(value) {
      this.$refs.dept?.setCheckedKeys(value === '2' ? this.savedDeptKeys : [])
    },
    /** 分配数据权限操作 */
    async handleDataScope(row) {
      if (this.formLoading) return
      this.formLoading = true
      this.reset()
      this.deptOptions = []
      try {
        const [deptTree, response] = await Promise.all([this.getDeptTree(row.roleId), getRole(row.roleId)])
        if (!response.data || !Array.isArray(deptTree.depts)) {
          this.$modal.msgError("角色信息不完整，请重试")
          return
        }
        this.deptOptions = deptTree.depts
        this.savedDeptKeys = [...(deptTree.checkedKeys || [])]
        this.form = response.data
        this.title = "分配数据权限"
        this.openDataScope = true
        await this.$nextTick()
        this.$refs.dept?.setCheckedKeys(deptTree.checkedKeys || [])
      } catch (error) {
        this.deptOptions = []
        this.openDataScope = false
      } finally {
        this.formLoading = false
      }
    },
    /** 分配用户操作 */
    handleAuthUser: function(row) {
      const roleId = row.roleId
      this.$router.push("/system/role-auth/user/" + roleId)
    },
    /** 提交按钮 */
    submitForm: function() {
      if (this.submitting) return
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.submitting) return
          const editing = this.form.roleId != undefined
          const data = { ...this.form, menuIds: this.getMenuAllCheckedKeys() }
          data.deptIds = []
          if (data.dataScope === '2' && (!editing || this.$auth.hasPermi('system:role:dataScope'))) {
            if (!this.createDeptLoaded || this.createDeptLoading) {
              this.$modal.msgError("请先加载部门信息")
              return
            }
            // 仅提交真正勾选的部门，半选父节点不应扩大自定范围。
            data.deptIds = this.$refs.createDept?.getCheckedKeys() || []
            if (!data.deptIds.length) {
              this.$modal.msgError("请至少选择一个部门")
              return
            }
          }
          if (editing && !this.$auth.hasPermi('system:role:dataScope')) {
            delete data.dataScope
            delete data.deptIds
            delete data.deptCheckStrictly
          }
          this.submitting = true
          const request = editing ? updateRole(data) : addRole(data)
          request.then(() => {
            this.$modal.msgSuccess(editing ? "修改成功" : "新增成功")
            this.open = false
            this.getList()
          }).catch(() => {}).finally(() => { this.submitting = false })
        }
      }).catch(() => {})
    },
    /** 提交按钮（数据权限） */
    submitDataScope: function() {
      if (this.form.roleId != undefined) {
        this.form.deptIds = this.getDeptAllCheckedKeys()
        dataScope(this.form).then(response => {
          this.$modal.msgSuccess("修改成功")
          this.openDataScope = false
          this.getList()
        }).catch(() => {})
      }
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const roleIds = row.roleId || this.ids
      this.$modal.confirm('是否确认删除角色编号为"' + roleIds + '"的数据项？').then(function() {
        return delRole(roleIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('system/role/export', {
        ...this.queryParams
      }, `role_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>

<style lang="scss" scoped>
// 组件局部布局与展示样式；跨页面主题及 body 弹层规则在 assets/styles 中统一维护。
.data-scope-label {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.data-scope-help {
  display: inline-flex;
  align-items: center;
  padding: 0;
  border: 0;
  background: transparent;
  color: inherit;
  font: inherit;
  cursor: pointer;
}

.data-scope-guide {
  line-height: 1.7;
  overflow-wrap: anywhere;
  max-height: 60vh;
  overflow-y: auto;
  padding-right: 8px;

  p { margin: 0 0 12px; }
  dl { margin: 0; }
  dl > div { margin-bottom: 14px; }
  dt { font-weight: 600; }
  dd { margin-left: 0; }
  ul { padding-left: 20px; margin: 0; }
  li + li { margin-top: 6px; }
}

.permission-tree-item {
  :deep(.el-form-item__content) {
    display: block;
    min-width: 0;
  }

  .tree-border {
    width: 100%;
  }
}
</style>
