<template>
  <div class="app-container biz-dict-page">
    <el-form
      ref="queryForm"
      :model="queryParams"
      size="small"
      :inline="true"
      v-show="showSearch"
      label-width="82px"
      class="biz-dict-query-form"
    >
      <el-form-item label="字典组编码" prop="groupCode">
        <el-select
          v-model="queryParams.groupCode"
          placeholder="请选择字典组编码"
          clearable
          filterable
          :loading="groupCodeOptionsLoading"
          style="width: 220px"
        >
          <el-option
            v-for="item in groupCodeOptions"
            :key="item.groupCode"
            :label="formatGroupCodeOption(item)"
            :value="item.groupCode"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="字典组名称" prop="groupName">
        <el-input
          v-model="queryParams.groupName"
          placeholder="请输入字典组名称"
          clearable
          style="width: 220px"
@keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select
          v-model="queryParams.status"
          placeholder="请选择状态"
          clearable
          style="width: 180px"
        >
          <el-option
            v-for="item in statusOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="删除标志" prop="delFlag">
        <el-select
          v-model="queryParams.delFlag"
          placeholder="请选择删除标志"
          clearable
          style="width: 180px"
        >
          <el-option
            v-for="item in deleteFlagOptions"
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
        />
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
          @click="handleAddGroup"
          v-hasPermi="['reportSystem:bizDict:add']"
        >新增字典组</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table
      ref="groupTable"
      v-loading="loading"
      :data="groupList"
      row-key="id"
      :expand-row-keys="expandedRowKeys"
      @expand-change="handleExpandChange"
    >
      <el-table-column type="expand" width="42">
        <template #default="scope">
          <div class="dict-item-panel">
            <div class="dict-item-toolbar">
              <div class="dict-item-actions">
                <el-button
                  type="primary"
                  plain
:icon="resolveIcon('el-icon-plus')"
size="small"
                  :disabled="isDeleted(scope.row)"
                  @click="handleAddItem(scope.row)"
                  v-hasPermi="['reportSystem:bizDict:add']"
                >新增字典项</el-button>
                <el-button
:icon="resolveIcon('el-icon-refresh')"
size="small"
                  @click="loadItemList(scope.row, true)"
                >刷新</el-button>
              </div>
            </div>
            <el-table
size="small"
              :data="itemMap[scope.row.id] || []"
              v-loading="itemLoadingMap[scope.row.id]"
              class="dict-item-table"
              empty-text="暂无字典项"
            >
              <el-table-column label="字典项ID" prop="id" width="90" align="center" />
              <el-table-column label="字典组ID" prop="groupId" width="90" align="center" />
              <el-table-column label="字典编码" prop="itemCode" min-width="140" :show-overflow-tooltip="true" />
              <el-table-column label="字典名称" prop="itemName" min-width="140" :show-overflow-tooltip="true" />
              <el-table-column label="字典值" prop="itemValue" min-width="100" :show-overflow-tooltip="true" />
              <el-table-column label="排序" prop="itemSort" width="70" align="center" />
              <el-table-column label="状态" prop="status" width="80" align="center" class-name="dict-tag-cell">
                <template #default="itemScope">
                  <el-tag :type="statusTagType(itemScope.row.status)" size="small">
                    {{ statusText(itemScope.row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="删除标志" prop="delFlag" width="90" align="center" class-name="dict-tag-cell">
                <template #default="itemScope">
                  <el-tag :type="deleteStatusTagType(itemScope.row.delFlag)" size="small">
                    {{ deleteStatusText(itemScope.row.delFlag) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="拓展配置JSON" prop="extraJson" min-width="130" :show-overflow-tooltip="true" />
              <el-table-column label="备注" prop="remark" min-width="130" :show-overflow-tooltip="true" />
              <el-table-column label="创建人" prop="createBy" min-width="110" :show-overflow-tooltip="true" />
              <el-table-column label="创建时间" prop="createTime" width="170" align="center">
                <template #default="itemScope">
                  <span>{{ parseTime(itemScope.row.createTime) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="更新人" prop="updateBy" min-width="110" :show-overflow-tooltip="true" />
              <el-table-column label="更新时间" prop="updateTime" width="170" align="center">
                <template #default="itemScope">
                  <span>{{ parseTime(itemScope.row.updateTime) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="120" align="center" fixed="right" class-name="small-padding fixed-width">
                <template #default="itemScope">
                  <el-button
size="small"
                     link type="primary"
:icon="resolveIcon('el-icon-edit')"
                    :disabled="isDeleted(itemScope.row) || isDeleted(scope.row)"
                    @click="handleUpdateItem(itemScope.row, scope.row)"
                    v-hasPermi="['reportSystem:bizDict:edit']"
                  >修改</el-button>
                  <el-button
size="small"
                     link type="primary"
                    :icon="removeOrRestoreIcon(itemScope.row)"
                    :disabled="isDeleted(scope.row)"
                    @click="handleRemoveOrRestoreItem(itemScope.row, scope.row)"
                    v-hasPermi="['reportSystem:bizDict:remove']"
                  >{{ removeOrRestoreText(itemScope.row) }}</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="字典组ID" prop="id" width="90" align="center" />
      <el-table-column label="字典组编码" prop="groupCode" min-width="180" :show-overflow-tooltip="true" />
      <el-table-column label="字典组名称" prop="groupName" min-width="160" :show-overflow-tooltip="true" />
      <el-table-column label="状态" prop="status" width="100" align="center" class-name="dict-tag-cell">
        <template #default="scope">
          <el-tag :type="statusTagType(scope.row.status)" size="small">
            {{ statusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="删除标志" prop="delFlag" width="100" align="center" class-name="dict-tag-cell">
        <template #default="scope">
          <el-tag :type="deleteStatusTagType(scope.row.delFlag)" size="small">
            {{ deleteStatusText(scope.row.delFlag) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="备注" prop="remark" min-width="180" :show-overflow-tooltip="true" />
      <el-table-column label="创建人" prop="createBy" min-width="120" :show-overflow-tooltip="true" />
      <el-table-column label="创建时间" prop="createTime" width="170" align="center">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="更新人" prop="updateBy" min-width="120" :show-overflow-tooltip="true" />
      <el-table-column label="更新时间" prop="updateTime" width="170" align="center">
        <template #default="scope">
          <span>{{ parseTime(scope.row.updateTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="230" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button
size="small"
             link type="primary"
:icon="resolveIcon('el-icon-plus')"
            :disabled="isDeleted(scope.row)"
            @click="handleAddItem(scope.row)"
            v-hasPermi="['reportSystem:bizDict:add']"
          >新增项</el-button>
          <el-button
size="small"
             link type="primary"
:icon="resolveIcon('el-icon-edit')"
            :disabled="isDeleted(scope.row)"
            @click="handleUpdateGroup(scope.row)"
            v-hasPermi="['reportSystem:bizDict:edit']"
          >修改</el-button>
          <el-button
size="small"
             link type="primary"
            :icon="removeOrRestoreIcon(scope.row)"
            @click="handleRemoveOrRestoreGroup(scope.row)"
            v-hasPermi="['reportSystem:bizDict:remove']"
          >{{ removeOrRestoreText(scope.row) }}</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
v-model:page="queryParams.pageNum"
v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <el-dialog :title="groupTitle" v-model="groupOpen" width="720px" append-to-body :close-on-click-modal="false">
      <el-form ref="groupForm" :model="groupForm" :rules="groupRules" label-width="96px">
        <el-form-item label="字典组编码" prop="groupCode">
          <el-input v-model="groupForm.groupCode" placeholder="请输入字典组编码" />
        </el-form-item>
        <el-form-item label="字典组名称" prop="groupName">
          <el-input v-model="groupForm.groupName" placeholder="请输入字典组名称" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="groupForm.status">
            <el-radio v-for="item in statusOptions" :key="item.value" :value="item.value">{{ item.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="groupForm.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer><div  class="dialog-footer">
        <el-button type="primary" @click="submitGroupForm">确 定</el-button>
        <el-button @click="cancelGroup">取 消</el-button>
      </div></template>
    </el-dialog>

    <el-dialog :title="itemTitle" v-model="itemOpen" width="860px" append-to-body :close-on-click-modal="false">
      <el-form ref="itemForm" :model="itemForm" :rules="itemRules" label-width="96px">
        <el-form-item label="所属字典组">
          <el-input :value="currentGroupName" disabled />
        </el-form-item>
        <el-form-item label="字典编码" prop="itemCode">
          <el-input v-model="itemForm.itemCode" placeholder="请输入字典编码" />
        </el-form-item>
        <el-form-item label="字典名称" prop="itemName">
          <el-input v-model="itemForm.itemName" placeholder="请输入字典名称" />
        </el-form-item>
        <el-form-item label="字典值" prop="itemValue">
          <el-input v-model="itemForm.itemValue" placeholder="请输入字典值" />
        </el-form-item>
        <el-form-item label="排序" prop="itemSort">
          <el-input-number v-model="itemForm.itemSort" :min="0" :max="999999" controls-position="right" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="itemForm.status">
            <el-radio v-for="item in statusOptions" :key="item.value" :value="item.value">{{ item.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="拓展配置JSON" prop="extraJson">
          <el-input
            v-model="itemForm.extraJson"
            type="textarea"
            :autosize="{ minRows: 3, maxRows: 6 }"
            placeholder="请输入拓展配置JSON"
          />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="itemForm.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer><div  class="dialog-footer">
        <el-button type="primary" @click="submitItemForm">确 定</el-button>
        <el-button @click="cancelItem">取 消</el-button>
      </div></template>
    </el-dialog>
  </div>
</template>

<script>
import {
  addDictGroup,
  addDictItem,
  delDictGroup,
  delDictItem,
  getDictGroup,
  getDictItem,
  listDictGroup,
  listDictGroupOptions,
  listDictItem,
  restoreDictGroup,
  restoreDictItem,
  updateDictGroup,
  updateDictItem
} from '@/api/reportSystem/bizDict'

export default {
  name: 'BizDict',
  data() {
    return {
      loading: false,
      showSearch: true,
      total: 0,
      groupList: [],
      groupCodeOptions: [],
      groupCodeOptionsLoading: false,
      expandedRowKeys: [],
      itemMap: {},
      itemLoadingMap: {},
      dateRange: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        groupCode: undefined,
        groupName: undefined,
        status: undefined,
        delFlag: undefined
      },
      statusOptions: [
        { label: '正常', value: '1', type: 'success' },
        { label: '停用', value: '0', type: 'info' }
      ],
      deleteFlagOptions: [
        { label: '未删除', value: '0' },
        { label: '已删除', value: '1' }
      ],
      groupOpen: false,
      groupTitle: '',
      groupForm: {},
      itemOpen: false,
      itemTitle: '',
      itemForm: {},
      currentGroup: null,
      groupRules: {
        groupCode: [
          { required: true, message: '字典组编码不能为空', trigger: 'blur' }
        ],
        groupName: [
          { required: true, message: '字典组名称不能为空', trigger: 'blur' }
        ]
      },
      itemRules: {
        itemCode: [
          { required: true, message: '字典编码不能为空', trigger: 'blur' }
        ],
        itemName: [
          { required: true, message: '字典名称不能为空', trigger: 'blur' }
        ],
        itemValue: [
          { required: true, message: '字典值不能为空', trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    currentGroupName() {
      if (!this.currentGroup) {
        return ''
      }
      return this.currentGroup.groupName + '（' + this.currentGroup.groupCode + '）'
    }
  },
  created() {
    this.getList()
    this.loadGroupCodeOptions()
  },
  methods: {
    getList() {
      this.loading = true
      listDictGroup(this.buildGroupQuery()).then(response => {
        this.groupList = response.rows || []
        this.total = response.total || 0
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    loadGroupCodeOptions() {
      this.groupCodeOptionsLoading = true
      listDictGroupOptions().then(response => {
        this.groupCodeOptions = this.uniqueGroupCodeOptions(response.data || [])
        this.groupCodeOptionsLoading = false
      }).catch(() => {
        this.groupCodeOptionsLoading = false
      })
    },
    uniqueGroupCodeOptions(rows) {
      const groupCodeMap = new Map()
      rows.forEach(item => {
        if (item && item.groupCode && !groupCodeMap.has(item.groupCode)) {
          groupCodeMap.set(item.groupCode, item)
        }
      })
      return Array.from(groupCodeMap.values())
    },
    formatGroupCodeOption(item) {
      if (!item) {
        return ''
      }
      return item.groupName ? item.groupCode + '（' + item.groupName + '）' : item.groupCode
    },
    buildGroupQuery() {
      const query = { ...this.queryParams }
      if (this.dateRange && this.dateRange.length === 2) {
        query.beginTime = this.dateRange[0]
        query.endTime = this.dateRange[1]
      }
      return query
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.dateRange = []
      this.resetForm('queryForm')
      this.handleQuery()
    },
    handleExpandChange(row, expandedRows) {
      this.expandedRowKeys = expandedRows.map(item => item.id)
      if (this.expandedRowKeys.indexOf(row.id) !== -1) {
        this.loadItemList(row)
      }
    },
    loadItemList(group, force) {
      if (!group || !group.id) {
        return
      }
      if (!force && this.itemMap[group.id]) {
        return
      }
      this.itemLoadingMap[group.id] = true
      listDictItem({
        groupId: group.id,
        pageNum: 1,
        pageSize: 500
      }).then(response => {
        this.itemMap[group.id] = response.rows || []
        this.itemLoadingMap[group.id] = false
      }).catch(() => {
        this.itemLoadingMap[group.id] = false
      })
    },
    resetGroup() {
      this.groupForm = {
        id: undefined,
        groupCode: undefined,
        groupName: undefined,
        status: '1',
        remark: undefined
      }
      this.resetForm('groupForm')
    },
    handleAddGroup() {
      this.resetGroup()
      this.groupOpen = true
      this.groupTitle = '新增字典组'
    },
    handleUpdateGroup(row) {
      const target = row
      if (!target || !target.id) {
        this.$modal.msgWarning('请选择要修改的字典组')
        return
      }
      if (this.isDeleted(target)) {
        this.$modal.msgWarning('已删除的字典组不能修改')
        return
      }
      this.resetGroup()
      getDictGroup(target.id).then(response => {
        this.groupForm = response.data || {}
        this.groupOpen = true
        this.groupTitle = '修改字典组'
      }).catch(() => {}) // 详情失败由请求拦截器提示，不打开空表单。
    },
    submitGroupForm() {
      this.$refs.groupForm.validate(valid => {
        if (!valid) {
          return
        }
        const request = this.groupForm.id ? updateDictGroup(this.groupForm) : addDictGroup(this.groupForm)
        request.then(() => {
          this.$modal.msgSuccess(this.groupForm.id ? '修改成功' : '新增成功')
          this.groupOpen = false
          this.getList()
          this.loadGroupCodeOptions()
        }).catch(() => {}) // 请求失败或安全验证取消时保留表单，允许重试。
      })
    },
    cancelGroup() {
      this.groupOpen = false
      this.resetGroup()
    },
    handleRemoveOrRestoreGroup(row) {
      if (!row || !row.id) {
        this.$modal.msgWarning('请选择要操作的字典组')
        return
      }
      const isRestore = this.isDeleted(row)
      const actionName = isRestore ? '恢复' : '删除'
      this.$modal.confirm('是否确认' + actionName + '字典组编号为"' + row.id + '"的数据项？').then(() => {
        return isRestore ? restoreDictGroup(row.id) : delDictGroup(row.id)
      }).then(() => {
        this.$modal.msgSuccess(actionName + '成功')
        this.getList()
        this.loadGroupCodeOptions()
      }).catch(() => {})
    },
    resetItem(group) {
      this.currentGroup = group || this.currentGroup
      this.itemForm = {
        id: undefined,
        groupId: this.currentGroup ? this.currentGroup.id : undefined,
        itemCode: undefined,
        itemName: undefined,
        itemValue: undefined,
        extraJson: undefined,
        itemSort: 0,
        status: '1',
        remark: undefined
      }
      this.resetForm('itemForm')
    },
    handleAddItem(group) {
      if (this.isDeleted(group)) {
        this.$modal.msgWarning('已删除的字典组不能新增字典项')
        return
      }
      this.resetItem(group)
      this.itemOpen = true
      this.itemTitle = '新增字典项'
    },
    handleUpdateItem(row, group) {
      if (this.isDeleted(row) || this.isDeleted(group)) {
        this.$modal.msgWarning('已删除的数据不能修改')
        return
      }
      this.resetItem(group)
      getDictItem(row.id).then(response => {
        this.itemForm = response.data || {}
        this.currentGroup = group || this.findGroup(this.itemForm.groupId)
        this.itemOpen = true
        this.itemTitle = '修改字典项'
      }).catch(() => {})
    },
    submitItemForm() {
      this.$refs.itemForm.validate(valid => {
        if (!valid) {
          return
        }
        const request = this.itemForm.id ? updateDictItem(this.itemForm) : addDictItem(this.itemForm)
        request.then(() => {
          this.$modal.msgSuccess(this.itemForm.id ? '修改成功' : '新增成功')
          this.itemOpen = false
          const group = this.currentGroup || this.findGroup(this.itemForm.groupId)
          this.loadItemList(group, true)
        }).catch(() => {})
      })
    },
    cancelItem() {
      this.itemOpen = false
      this.resetItem()
    },
    handleRemoveOrRestoreItem(row, group) {
      if (this.isDeleted(group)) {
        this.$modal.msgWarning('所属字典组已删除，请先恢复字典组')
        return
      }
      const isRestore = this.isDeleted(row)
      const actionName = isRestore ? '恢复' : '删除'
      this.$modal.confirm('是否确认' + actionName + '字典项编号为"' + row.id + '"的数据项？').then(() => {
        return isRestore ? restoreDictItem(row.id) : delDictItem(row.id)
      }).then(() => {
        this.$modal.msgSuccess(actionName + '成功')
        this.loadItemList(group, true)
        this.getList()
      }).catch(() => {})
    },
    findGroup(groupId) {
      return this.groupList.find(item => item.id === groupId) || null
    },
    statusLabel(status) {
      const item = this.statusOptions.find(option => option.value === status)
      return item ? item.label : status
    },
    statusText(status) {
      return status === undefined || status === null || status === '' ? '' : this.statusLabel(status)
    },
    statusTagType(status) {
      const item = this.statusOptions.find(option => option.value === status)
      return item ? item.type : ''
    },
    isDeleted(row) {
      return row && String(row.delFlag) === '1'
    },
    removeOrRestoreText(row) {
      return this.isDeleted(row) ? '恢复' : '删除'
    },
    removeOrRestoreIcon(row) {
      return this.isDeleted(row) ? 'el-icon-refresh' : 'el-icon-delete'
    },
    deleteStatusLabel(delFlag) {
      return String(delFlag) === '1' ? '已删除' : '未删除'
    },
    deleteStatusText(delFlag) {
      return this.deleteStatusLabel(delFlag)
    },
    deleteStatusTagType(delFlag) {
      return String(delFlag) === '1' ? 'danger' : 'success'
    }
  }
}
</script>

<style scoped>
.biz-dict-page {
  --dict-border: #e5e7eb;
  --dict-soft-bg: #f8fafc;
  --dict-title-color: #303133;
  --dict-header-bg: #f5f7fa;
  --dict-row-bg: #ffffff;
  --dict-row-hover-bg: #f5f7fa;
  --dict-cell-color: #303133;
  --dict-header-color: #606266;
  --dict-tag-bg: #ffffff;
  --dict-tag-color: #606266;
}

.dict-item-panel {
  padding: 12px 14px 16px 46px;
  background: var(--dict-soft-bg);
  border-top: 1px solid var(--dict-border);
  border-bottom: 1px solid var(--dict-border);
}

.dict-item-toolbar {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
  margin-bottom: 10px;
}

.dict-item-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.dict-item-table {
  border: 1px solid var(--dict-border);
  background: transparent;
}

:deep(.el-table__expanded-cell),
:deep(.el-table__expanded-cell[class*="cell"]) {
  padding: 0 !important;
  background: transparent !important;
}

:deep(.dict-item-table),
:deep(.dict-item-table tr),
:deep(.dict-item-table th),
:deep(.dict-item-table td) {
  background: transparent !important;
}

:deep(.dict-item-table th) {
  color: var(--dict-header-color);
  border-bottom-color: var(--dict-border);
}

:deep(.dict-item-table td) {
  color: var(--dict-cell-color);
  border-bottom-color: var(--dict-border);
}

:deep(.dict-item-table .el-table__row) {
  background: var(--dict-row-bg) !important;
}

:deep(.dict-item-table .el-table__row:hover > td) {
  background: var(--dict-row-hover-bg) !important;
}

:deep(.dict-item-table::before),
:deep(.dict-item-table::after) {
  background-color: var(--dict-border);
}

:deep(.biz-dict-query-form .el-form-item) {
  margin-bottom: 14px;
}
</style>

<style>
.biz-dict-page {
  --dict-expanded-bg: #f8fafc;
  --dict-border: #e5e7eb;
  --dict-soft-bg: #f8fafc;
  --dict-title-color: #303133;
  --dict-header-bg: #f5f7fa;
  --dict-row-bg: #ffffff;
  --dict-row-hover-bg: #f5f7fa;
  --dict-cell-color: #303133;
  --dict-header-color: #606266;
  --dict-tag-bg: #ffffff;
  --dict-tag-color: #606266;
}

body.theme-luxury-business .biz-dict-page,
.theme-luxury-business .biz-dict-page {
  --dict-expanded-bg: #141925;
  --dict-border: #2a3142;
  --dict-soft-bg: #1a1f2e;
  --dict-title-color: #e8eaf0;
  --dict-header-bg: #20273a;
  --dict-row-bg: #171d2b;
  --dict-row-hover-bg: rgba(212, 175, 55, 0.12);
  --dict-cell-color: #e8eaf0;
  --dict-header-color: #d4af37;
  --dict-tag-bg: rgba(212, 175, 55, 0.10);
  --dict-tag-color: #d4af37;
}

body.theme-glassmorphism .biz-dict-page,
.theme-glassmorphism .biz-dict-page {
  --dict-expanded-bg: rgba(48, 30, 88, 0.82);
  --dict-border: rgba(255, 255, 255, 0.24);
  --dict-soft-bg: rgba(48, 30, 88, 0.66);
  --dict-title-color: #ffffff;
  --dict-header-bg: rgba(93, 68, 145, 0.86);
  --dict-row-bg: rgba(82, 56, 142, 0.74);
  --dict-row-hover-bg: rgba(105, 72, 170, 0.86);
  --dict-cell-color: #ffffff;
  --dict-header-color: rgba(255, 255, 255, 0.92);
  --dict-tag-bg: rgba(255, 255, 255, 0.12);
  --dict-tag-color: rgba(255, 255, 255, 0.94);
}

body.theme-vibrant-tech .biz-dict-page,
.theme-vibrant-tech .biz-dict-page {
  --dict-expanded-bg: #101730;
  --dict-border: #2a3552;
  --dict-soft-bg: #141a35;
  --dict-title-color: #ffffff;
  --dict-header-bg: rgba(102, 126, 234, 0.18);
  --dict-row-bg: rgba(20, 26, 53, 0.96);
  --dict-row-hover-bg: rgba(0, 242, 254, 0.10);
  --dict-cell-color: #ffffff;
  --dict-header-color: #00f2fe;
  --dict-tag-bg: rgba(0, 242, 254, 0.10);
  --dict-tag-color: #00f2fe;
}

body.theme-minimalist-future .biz-dict-page,
.theme-minimalist-future .biz-dict-page {
  --dict-expanded-bg: rgba(244, 251, 255, 0.96);
  --dict-border: rgba(0, 188, 212, 0.18);
  --dict-soft-bg: rgba(255, 255, 255, 0.90);
  --dict-title-color: #172033;
  --dict-header-bg: rgba(232, 250, 255, 0.96);
  --dict-row-bg: #ffffff;
  --dict-row-hover-bg: rgba(0, 188, 212, 0.07);
  --dict-cell-color: #172033;
  --dict-header-color: #365065;
  --dict-tag-bg: rgba(0, 188, 212, 0.08);
  --dict-tag-color: #006c9a;
}

body.theme-warm-nature .biz-dict-page,
.theme-warm-nature .biz-dict-page {
  --dict-expanded-bg: #fbf7f0;
  --dict-border: #d4c4b0;
  --dict-soft-bg: #fffaf3;
  --dict-title-color: #4a3f35;
  --dict-header-bg: #f6ecdd;
  --dict-row-bg: #fffaf3;
  --dict-row-hover-bg: #f9f4ed;
  --dict-cell-color: #4a3f35;
  --dict-header-color: #8b6f47;
  --dict-tag-bg: #faf7f0;
  --dict-tag-color: #8b6f47;
}

body.theme-material-design .biz-dict-page,
.theme-material-design .biz-dict-page {
  --dict-expanded-bg: #f7f2ff;
  --dict-border: #e7e0ec;
  --dict-soft-bg: #ffffff;
  --dict-title-color: #1c1b1f;
  --dict-header-bg: #f3edf7;
  --dict-row-bg: #ffffff;
  --dict-row-hover-bg: rgba(103, 80, 164, 0.08);
  --dict-cell-color: #1c1b1f;
  --dict-header-color: #49454f;
  --dict-tag-bg: rgba(103, 80, 164, 0.08);
  --dict-tag-color: #6750a4;
}

body.theme-dark-premium .biz-dict-page,
.theme-dark-premium .biz-dict-page {
  --dict-expanded-bg: #0f0f12;
  --dict-border: #27272a;
  --dict-soft-bg: #18181b;
  --dict-title-color: #e4e4e7;
  --dict-header-bg: #202024;
  --dict-row-bg: #151518;
  --dict-row-hover-bg: rgba(212, 175, 55, 0.12);
  --dict-cell-color: #e4e4e7;
  --dict-header-color: #d4af37;
  --dict-tag-bg: rgba(212, 175, 55, 0.10);
  --dict-tag-color: #d4af37;
}

.biz-dict-page .el-table__body-wrapper td.el-table__expanded-cell.el-table__cell {
  padding: 0 !important;
  background: var(--dict-expanded-bg) !important;
  background-color: var(--dict-expanded-bg) !important;
}

.biz-dict-page .dict-item-table.el-table,
.biz-dict-page .dict-item-table .el-table__header-wrapper,
.biz-dict-page .dict-item-table .el-table__body-wrapper {
  background: transparent !important;
  background-color: transparent !important;
}

.biz-dict-page .dict-item-table .el-table__header-wrapper tr,
.biz-dict-page .dict-item-table .el-table__header-wrapper th.el-table__cell {
  background: var(--dict-header-bg) !important;
  background-color: var(--dict-header-bg) !important;
  color: var(--dict-header-color) !important;
  border-color: var(--dict-border) !important;
}

.biz-dict-page .dict-item-table .el-table__body-wrapper tr,
.biz-dict-page .dict-item-table .el-table__body-wrapper td.el-table__cell {
  background: var(--dict-row-bg) !important;
  background-color: var(--dict-row-bg) !important;
  color: var(--dict-cell-color) !important;
  border-color: var(--dict-border) !important;
}

.biz-dict-page .dict-item-table.el-table--enable-row-hover .el-table__body tr:hover > td.el-table__cell {
  background: var(--dict-row-hover-bg) !important;
  background-color: var(--dict-row-hover-bg) !important;
}

.biz-dict-page .dict-item-table::before,
.biz-dict-page .dict-item-table::after {
  background-color: var(--dict-border) !important;
}

.biz-dict-page .el-table td.dict-tag-cell,
.biz-dict-page .el-table th.dict-tag-cell {
  border-left-color: transparent !important;
  border-right-color: transparent !important;
}

.biz-dict-page .el-table td.dict-tag-cell .cell,
.biz-dict-page .el-table th.dict-tag-cell .cell {
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.biz-dict-page .el-table td.dict-tag-cell .cell::before,
.biz-dict-page .el-table td.dict-tag-cell .cell::after,
.biz-dict-page .el-table th.dict-tag-cell .cell::before,
.biz-dict-page .el-table th.dict-tag-cell .cell::after {
  display: none !important;
  content: none !important;
}
</style>
