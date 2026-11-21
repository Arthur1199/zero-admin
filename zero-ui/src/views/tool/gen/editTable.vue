<template>
  <el-card v-loading="loading">
    <el-tabs v-model="activeName">
      <el-tab-pane label="基本信息" name="basic">
        <basic-info-form ref="basicInfo" :info="info" />
      </el-tab-pane>
      <el-tab-pane label="字段信息" name="columnInfo">
        <el-table ref="dragTable" :data="columns" row-key="columnId" :max-height="tableHeight" border>
          <el-table-column label="序号" type="index" min-width="5%" class-name="allowDrag"/>
          <el-table-column label="字段列名" prop="columnName" min-width="10%" :show-overflow-tooltip="true" class-name="allowDrag"/>
          <el-table-column label="字段描述" min-width="10%">
            <template #default="scope">
              <el-input v-model="scope.row.columnComment"></el-input>
            </template>
          </el-table-column>
          <el-table-column
            label="物理类型"
            prop="columnType"
            min-width="10%"
            :show-overflow-tooltip="true"
          />
          <el-table-column label="Java类型" min-width="11%">
            <template #default="scope">
              <el-select v-model="scope.row.javaType">
                <el-option label="Long" value="Long" />
                <el-option label="String" value="String" />
                <el-option label="Integer" value="Integer" />
                <el-option label="Double" value="Double" />
                <el-option label="BigDecimal" value="BigDecimal" />
                <el-option label="Date" value="Date" />
                <el-option label="Boolean" value="Boolean" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="java属性" min-width="10%">
            <template #default="scope">
              <el-input v-model="scope.row.javaField"></el-input>
            </template>
          </el-table-column>

          <el-table-column label="插入" min-width="5%">
            <template #default="scope">
              <el-checkbox true-value="1" false-value="0" v-model="scope.row.isInsert"></el-checkbox>
            </template>
          </el-table-column>
          <el-table-column label="编辑" min-width="5%">
            <template #default="scope">
              <el-checkbox true-value="1" false-value="0" v-model="scope.row.isEdit"></el-checkbox>
            </template>
          </el-table-column>
          <el-table-column label="列表" min-width="5%">
            <template #default="scope">
              <el-checkbox true-value="1" false-value="0" v-model="scope.row.isList"></el-checkbox>
            </template>
          </el-table-column>
          <el-table-column label="查询" min-width="5%">
            <template #default="scope">
              <el-checkbox true-value="1" false-value="0" v-model="scope.row.isQuery"></el-checkbox>
            </template>
          </el-table-column>
          <el-table-column label="查询方式" min-width="10%">
            <template #default="scope">
              <el-select v-model="scope.row.queryType">
                <el-option label="=" value="EQ" />
                <el-option label="!=" value="NE" />
                <el-option label=">" value="GT" />
                <el-option label=">=" value="GTE" />
                <el-option label="<" value="LT" />
                <el-option label="<=" value="LTE" />
                <el-option label="LIKE" value="LIKE" />
                <el-option label="BETWEEN" value="BETWEEN" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="必填" min-width="5%">
            <template #default="scope">
              <el-checkbox true-value="1" false-value="0" v-model="scope.row.isRequired"></el-checkbox>
            </template>
          </el-table-column>
          <el-table-column label="显示类型" min-width="12%">
            <template #default="scope">
              <el-select v-model="scope.row.htmlType">
                <el-option label="文本框" value="input" />
                <el-option label="文本域" value="textarea" />
                <el-option label="下拉框" value="select" />
                <el-option label="单选框" value="radio" />
                <el-option label="复选框" value="checkbox" />
                <el-option label="日期控件" value="datetime" />
                <el-option label="图片上传" value="imageUpload" />
                <el-option label="文件上传" value="fileUpload" />
                <el-option label="富文本控件" value="editor" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="字典类型" min-width="12%">
            <template #default="scope">
              <el-select v-model="scope.row.dictType" clearable filterable placeholder="请选择">
                <el-option
                  v-for="dict in dictOptions"
                  :key="dict.dictType"
                  :label="dict.dictName"
                  :value="dict.dictType">
                  <span style="float: left">{{ dict.dictName }}</span>
                  <span style="float: right; color: #8492a6; font-size: 13px">{{ dict.dictType }}</span>
              </el-option>
              </el-select>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="生成信息" name="genInfo">
        <gen-info-form ref="genInfo" :info="info" :tables="tables" :menus="menus"/>
      </el-tab-pane>
    </el-tabs>
    <el-form label-width="100px">
      <el-form-item style="text-align: center;margin-left:-100px;margin-top:10px;">
        <el-button type="primary" :disabled="loading || !loaded" :loading="submitting" @click="submitForm()">提交</el-button>
        <el-button @click="close()">返回</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script>
/**
 * 代码生成配置编辑页；等待表、字典和菜单信息齐备后展示，并汇总子表单校验结果再提交。
 */
import { getGenTable, updateGenTable } from "@/api/tool/gen"
import { optionselect as getDictOptionselect } from "@/api/system/dict/type"
import { listMenu as getMenuTreeselect } from "@/api/system/menu"
import basicInfoForm from "./basicInfoForm"
import genInfoForm from "./genInfoForm"
import Sortable from 'sortablejs'
import { markRaw } from 'vue'

export default {
  name: "GenEdit",
  components: {
    basicInfoForm,
    genInfoForm
  },
  data() {
    return {
      loading: false,
      loaded: false,
      submitting: false,
      sortable: null,
      // 选中选项卡的 name
      activeName: "columnInfo",
      // 表格的高度
      tableHeight: document.documentElement.scrollHeight - 245 + "px",
      // 表信息
      tables: [],
      // 表列信息
      columns: [],
      // 字典信息
      dictOptions: [],
      // 菜单信息
      menus: [],
      // 表详细信息
      info: {}
    }
  },
  created() {
    this.loadTable()
  },
  methods: {
    /** 详情及选项齐备后允许提交，避免保存半份配置。 */
    async loadTable() {
      const tableId = this.$route.params && this.$route.params.tableId
      if (!tableId) return
      this.loading = true
      this.loaded = false
      try {
        const [table, dict, menu] = await Promise.all([
          getGenTable(tableId), getDictOptionselect(), getMenuTreeselect()
        ])
        this.columns = table.data.rows || []
        this.info = table.data.info || {}
        this.tables = table.data.tables || []
        this.dictOptions = dict.data || []
        this.menus = this.handleTree(menu.data || [], "menuId")
        this.loaded = Boolean(this.info.tableId)
      } catch {
        this.columns = []
        this.info = {}
        this.tables = []
        this.dictOptions = []
        this.menus = []
      } finally {
        this.loading = false
      }
    },
    /** 提交按钮 */
    async submitForm() {
      if (!this.loaded || this.loading || this.submitting) return
      this.submitting = true
      try {
        const basicForm = this.$refs.basicInfo.$refs.basicInfoForm
        const genForm = this.$refs.genInfo.$refs.genInfoForm
        const res = await Promise.all([basicForm, genForm].map(this.getFormPromise))
        const validateResult = res.every(item => !!item)
        if (validateResult) {
          const genTable = Object.assign({}, this.info)
          genTable.columns = this.columns
          genTable.params = {
            treeCode: genTable.treeCode,
            treeName: genTable.treeName,
            treeParentCode: genTable.treeParentCode,
            parentMenuId: genTable.parentMenuId
          }
          const response = await updateGenTable(genTable)
          this.$modal.msgSuccess(response.msg)
          if (response.code === 200) {
            this.close()
          }
        } else {
          this.$modal.msgError("表单校验未通过，请重新检查提交内容")
        }
      } catch {
        // 请求提示由拦截器处理，失败保留配置供重试。
      } finally {
        this.submitting = false
      }
    },
    getFormPromise(form) {
      return new Promise(resolve => {
        form.validate(res => {
          resolve(res)
        })
      })
    },
    /** 关闭按钮 */
    close() {
      const obj = { path: "/tool/gen", query: { t: Date.now(), pageNum: this.$route.query.pageNum } }
      this.$tab.closeOpenPage(obj)
    }
  },
  mounted() {
    const el = this.$refs.dragTable.$el.querySelector(".el-table__body-wrapper tbody")
    if (!el) return
    this.sortable = markRaw(Sortable.create(el, {
      handle: ".allowDrag",
      onEnd: evt => {
        if (!Number.isInteger(evt.oldIndex) || !Number.isInteger(evt.newIndex) || evt.oldIndex === evt.newIndex) return
        if (evt.oldIndex < 0 || evt.newIndex < 0 || evt.oldIndex >= this.columns.length || evt.newIndex >= this.columns.length) return
        // Sortable先移动真实节点，恢复原顺序后交给Vue按稳定key重排。
        const parent = evt.item.parentNode
        parent.removeChild(evt.item)
        parent.insertBefore(evt.item, parent.children[evt.oldIndex] || null)
        const targetRow = this.columns.splice(evt.oldIndex, 1)[0]
        this.columns.splice(evt.newIndex, 0, targetRow)
        for (let index in this.columns) {
          this.columns[index].sort = parseInt(index) + 1
        }
      }
    }))
  },
  beforeUnmount() {
    this.sortable?.destroy()
  }
}
</script>
