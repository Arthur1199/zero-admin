<template>
  <div class="container form-build-container">
    <div class="left-board">
      <div class="logo-wrapper">
        <div class="logo">
          <img :src="logo" alt="logo"> Form Generator
        </div>
      </div>
      <el-scrollbar class="left-scrollbar">
        <div class="components-list">
          <div class="components-title">
            <svg-icon icon-class="component" />输入型组件
          </div>
          <draggable
            class="components-draggable"
            :list="inputComponents"
            :group="{ name: 'componentsGroup', pull: 'clone', put: false }"
            :clone="cloneComponent"
            draggable=".components-item"
            :sort="false"
            @end="onEnd"
           item-key="tag"><template #item="{ element: element, index: index }">
            <div
                class="components-item"
              @click="addComponent(element)"
            >
              <div class="components-body">
                <svg-icon :icon-class="element.tagIcon" />
                {{ element.label }}
              </div>
            </div>
          </template></draggable>
          <div class="components-title">
            <svg-icon icon-class="component" />选择型组件
          </div>
          <draggable
            class="components-draggable"
            :list="selectComponents"
            :group="{ name: 'componentsGroup', pull: 'clone', put: false }"
            :clone="cloneComponent"
            draggable=".components-item"
            :sort="false"
            @end="onEnd"
           item-key="tag"><template #item="{ element: element, index: index }">
            <div
              class="components-item"
              @click="addComponent(element)"
            >
              <div class="components-body">
                <svg-icon :icon-class="element.tagIcon" />
                {{ element.label }}
              </div>
            </div>
          </template></draggable>
          <div class="components-title">
            <svg-icon icon-class="component" /> 布局型组件
          </div>
          <draggable
            class="components-draggable" :list="layoutComponents"
            :group="{ name: 'componentsGroup', pull: 'clone', put: false }" :clone="cloneComponent"
            draggable=".components-item" :sort="false" @end="onEnd"
           item-key="tag"><template #item="{ element: element, index: index }">
            <div
                class="components-item"
              @click="addComponent(element)"
            >
              <div class="components-body">
                <svg-icon :icon-class="element.tagIcon" />
                {{ element.label }}
              </div>
            </div>
          </template></draggable>
        </div>
      </el-scrollbar>
    </div>

    <div class="center-board">
      <div class="action-bar">
        <el-button :icon="resolveIcon('el-icon-download')"  link type="primary" @click="download">
          导出vue文件
        </el-button>
        <el-button class="copy-btn-main" :icon="resolveIcon('el-icon-document-copy')"  link type="primary" @click="copy">
          复制代码
        </el-button>
        <el-button class="delete-btn" :icon="resolveIcon('el-icon-delete')"  link type="primary" @click="empty">
          清空
        </el-button>
      </div>
      <el-scrollbar class="center-scrollbar">
        <el-row class="center-board-row" :gutter="formConf.gutter">
          <el-form
            :size="normalizeSize(formConf.size)"
            :label-position="formConf.labelPosition"
            :disabled="formConf.disabled"
            :label-width="formConf.labelWidth + 'px'"
          >
            <draggable class="drawing-board" :list="drawingList" :animation="340" group="componentsGroup" item-key="formId"><template #item="{ element: element, index: index }">
              <draggable-item
                :drawing-list="drawingList"
                :element="element"
                :index="index"
                :active-id="activeId"
                :form-conf="formConf"
                @activeItem="activeFormItem"
                @copyItem="drawingItemCopy"
                @deleteItem="drawingItemDelete"
              />
            </template></draggable>
            <div v-show="!drawingList.length" class="empty-info">
              从左侧拖入或点选组件进行表单设计
            </div>
          </el-form>
        </el-row>
      </el-scrollbar>
    </div>

    <right-panel
      :active-data="activeData"
      :form-conf="formConf"
      :show-field="!!drawingList.length"
      @tag-change="tagChange"
    />

    <code-type-dialog
      ref="codeTypeDialog"
v-model="dialogVisible"
      title="选择生成类型"
      :show-file-name="showFileName"
      @confirm="generate"
    />
    <input ref="copyNode" id="copyNode" type="hidden">
  </div>
</template>

<script>
/**
 * 可视化表单构建器入口；管理画布草稿、组件 ID 与拖动，导出模板和脚本由 generator 工具负责。
 */
import draggable from 'vuedraggable'
import { markRaw } from 'vue'
import beautifier from 'js-beautify'
import ClipboardJS from 'clipboard'
import render from '@/utils/generator/render.jsx'
import RightPanel from './RightPanel'
import { inputComponents, selectComponents, layoutComponents, formConf } from '@/utils/generator/config'
import { beautifierConf, titleCase } from '@/utils/index'
import { makeUpHtml, vueTemplate, vueScript, cssStyle } from '@/utils/generator/html'
import { makeUpJs } from '@/utils/generator/js'
import { makeUpCss } from '@/utils/generator/css'
import drawingDefault from '@/utils/generator/drawingDefault'
import logo from '@/assets/logo/zero.ico'
import CodeTypeDialog from './CodeTypeDialog'
import DraggableItem from './DraggableItem'

let oldActiveId
let tempActiveData

export default {
  components: {
    draggable,
    render,
    RightPanel,
    CodeTypeDialog,
    DraggableItem
  },
  data() {
    return {
      logo,
      clipboard: null,
      idGlobal: 100,
      formConf,
      inputComponents,
      selectComponents,
      layoutComponents,
      labelWidth: 100,
      drawingList: drawingDefault,
      drawingData: {},
      activeId: drawingDefault[0]?.formId || 0,
      drawerVisible: false,
      formData: {},
      dialogVisible: false,
      generateConf: null,
      showFileName: false,
      activeData: drawingDefault[0] || {}
    }
  },
  created() {
    // 主题切换重建页面时保留旧画布，并从现有字段最大 ID 继续生成，避免克隆键冲突。
    this.idGlobal = Math.max(this.idGlobal, this.getMaxFormId(this.drawingList))
    // 防止 firefox 下拖拽会新打开一个选项卡
    document.body.addEventListener('drop', this.preventBrowserDrop)
  },
  watch: {
    'activeData.label': function (val, oldVal) {
      if (
        this.activeData.placeholder === undefined
        || !this.activeData.tag
        || oldActiveId !== this.activeId
      ) {
        return
      }
      this.activeData.placeholder = this.activeData.placeholder.replace(oldVal, '') + val
    },
    activeId: {
      handler(val) {
        oldActiveId = val
      },
      immediate: true
    }
  },
  beforeUnmount() {
    this.clipboard?.destroy()
    document.body.removeEventListener('drop', this.preventBrowserDrop)
  },
  methods: {
    preventBrowserDrop(event) {
      // 仅阻止浏览器打开拖入文件，保留事件传播让 Sortable 的 document drop 完成收尾。
      event.preventDefault()
    },
    getMaxFormId(fields) {
      if (!Array.isArray(fields)) return 0
      return fields.reduce((maximum, field) => {
        const id = Number(field?.formId)
        return Math.max(maximum, Number.isFinite(id) ? id : 0, this.getMaxFormId(field?.children))
      }, 0)
    },
    activeFormItem(element) {
      this.activeData = element
      this.activeId = element.formId
    },
    onEnd(obj, a) {
      if (obj.from !== obj.to) {
        this.activeData = tempActiveData
        this.activeId = this.idGlobal
      }
    },
    addComponent(item) {
      const clone = this.cloneComponent(item)
      this.drawingList.push(clone)
      this.activeFormItem(clone)
    },
    cloneComponent(origin) {
      const clone = JSON.parse(JSON.stringify(origin))
      clone.formId = ++this.idGlobal
      clone.span = formConf.span
      clone.renderKey = +new Date() // 改变 renderKey 后可以实现强制更新组件
      if (!clone.layout) clone.layout = 'colFormItem'
      if (clone.layout === 'colFormItem') {
        clone.vModel = `field${this.idGlobal}`
        clone.placeholder !== undefined && (clone.placeholder += clone.label)
        tempActiveData = clone
      } else if (clone.layout === 'rowFormItem') {
        delete clone.label
        clone.componentName = `row${this.idGlobal}`
        clone.gutter = this.formConf.gutter
        tempActiveData = clone
      }
      return tempActiveData
    },
    AssembleFormData() {
      this.formData = {
        fields: JSON.parse(JSON.stringify(this.drawingList)),
        ...this.formConf
      }
    },
    generate(data) {
      const func = this[`exec${titleCase(this.operationType)}`]
      this.generateConf = data
      func && func(data)
    },
    execRun(data) {
      this.AssembleFormData()
      this.drawerVisible = true
    },
    execDownload(data) {
      const codeStr = this.generateCode()
      const blob = new Blob([codeStr], { type: 'text/plain;charset=utf-8' })
      this.$download.saveAs(blob, data.fileName)
    },
    execCopy(data) {
      try {
        const codeStr = this.generateCode()
        if (!codeStr.trim()) throw new Error('生成代码为空')
        // 先更新选区复制缓冲，再写标准API；兼容两类浏览器剪贴板通道，内容保持一致。
        const copied = this.copyCodeLegacy(codeStr)
        if (window.isSecureContext && navigator.clipboard?.writeText) {
          navigator.clipboard.writeText(codeStr).then(() => {
            this.$notify({ title: '成功', message: '代码已复制到剪贴板，可粘贴。', type: 'success' })
          }).catch(() => {
            if (copied) this.$notify({ title: '成功', message: '代码已复制到剪贴板，可粘贴。', type: 'success' })
            else this.$message.error('代码复制失败')
          })
        } else if (copied) {
          this.$notify({ title: '成功', message: '代码已复制到剪贴板，可粘贴。', type: 'success' })
        } else {
          this.$message.error('代码复制失败')
        }
      } catch {
        this.$message.error('代码复制失败')
      }
    },
    copyCodeLegacy(codeStr) {
      const trigger = this.$refs.copyNode
      const dialog = this.$refs.codeTypeDialog?.$el?.querySelector('.el-dialog')
      const container = dialog?.getClientRects().length ? dialog : this.$el
      if (!trigger || !container) return false
      let copied = false
      try {
        this.clipboard?.destroy()
        // 临时选区必须位于弹窗内部，避免 Element Plus 焦点约束夺走复制选区。
        this.clipboard = markRaw(new ClipboardJS(trigger, { container, text: () => codeStr }))
        this.clipboard.on('success', event => {
          copied = true
          event.clearSelection()
        })
        this.clipboard.on('error', () => { copied = false })
        trigger.click()
        return copied
      } catch {
        this.clipboard?.destroy()
        return false
      }
    },
    empty() {
      this.$confirm('确定要清空所有组件吗？', '提示', { type: 'warning' }).then(
        () => {
          this.drawingList = []
        }
      )
    },
    drawingItemCopy(item, parent) {
      let clone = JSON.parse(JSON.stringify(item))
      clone = this.createIdAndKey(clone)
      parent.push(clone)
      this.activeFormItem(clone)
    },
    createIdAndKey(item) {
      item.formId = ++this.idGlobal
      item.renderKey = +new Date()
      if (item.layout === 'colFormItem') {
        item.vModel = `field${this.idGlobal}`
      } else if (item.layout === 'rowFormItem') {
        item.componentName = `row${this.idGlobal}`
      }
      if (Array.isArray(item.children)) {
        item.children = item.children.map(childItem => this.createIdAndKey(childItem))
      }
      return item
    },
    drawingItemDelete(index, parent) {
      parent.splice(index, 1)
      this.$nextTick(() => {
        const len = this.drawingList.length
        if (len) {
          this.activeFormItem(this.drawingList[len - 1])
        }
      })
    },
    generateCode() {
      const { type } = this.generateConf
      this.AssembleFormData()
      const script = vueScript(makeUpJs(this.formData, type))
      const html = vueTemplate(makeUpHtml(this.formData, type))
      const css = cssStyle(makeUpCss(this.formData))
      return beautifier.html(html + script + css, beautifierConf.html)
    },
    download() {
      this.dialogVisible = true
      this.showFileName = true
      this.operationType = 'download'
    },
    run() {
      this.dialogVisible = true
      this.showFileName = false
      this.operationType = 'run'
    },
    copy() {
      this.dialogVisible = true
      this.showFileName = false
      this.operationType = 'copy'
    },
    tagChange(newTag) {
      newTag = this.cloneComponent(newTag)
      newTag.vModel = this.activeData.vModel
      newTag.formId = this.activeId
      newTag.span = this.activeData.span
      delete this.activeData.tag
      delete this.activeData.tagIcon
      delete this.activeData.document
      Object.keys(newTag).forEach(key => {
        if (this.activeData[key] !== undefined
          && typeof this.activeData[key] === typeof newTag[key]) {
          newTag[key] = this.activeData[key]
        }
      })
      this.activeData = newTag
      this.updateDrawingList(newTag, this.drawingList)
    },
    updateDrawingList(newTag, list) {
      const index = list.findIndex(item => item.formId === this.activeId)
      if (index > -1) {
        list.splice(index, 1, newTag)
      } else {
        list.forEach(item => {
          if (Array.isArray(item.children)) this.updateDrawingList(newTag, item.children)
        })
      }
    }
  }
}
</script>

<style lang='scss'>
// 构建器画布、组件面板及属性区布局；末尾加载独立主题覆盖，避免影响其它业务页面。
@use 'sass:meta';
.editor-tabs{
  background: #121315;
  .el-tabs__header{
    margin: 0;
    border-bottom-color: #121315;
    .el-tabs__nav{
      border-color: #121315;
    }
  }
  .el-tabs__item{
    height: 32px;
    line-height: 32px;
    color: #888a8e;
    border-left: 1px solid #121315 !important;
    background: #363636;
    margin-right: 5px;
    user-select: none;
  }
  .el-tabs__item.is-active{
    background: #1e1e1e;
    border-bottom-color: #1e1e1e!important;
    color: #fff;
  }
  .el-icon-edit{
    color: #f1fa8c;
  }
  .el-icon-document{
    color: #a95812;
  }
}

// home
.right-scrollbar {
  .el-scrollbar__view {
    padding: 12px 18px 15px 15px;
  }
}
.left-scrollbar .el-scrollbar__wrap {
  box-sizing: border-box;
  overflow-x: hidden !important;
  margin-bottom: 0 !important;
}
.center-tabs{
  .el-tabs__header{
    margin-bottom: 0!important;
  }
  .el-tabs__item{
    width: 50%;
    text-align: center;
  }
  .el-tabs__nav{
    width: 100%;
  }
}
.reg-item{
  padding: 12px 8px;
  background: var(--form-build-rule-bg, #f8f8f8);
  position: relative;
  border: 1px solid var(--form-build-rule-border, #ebeef5);
  border-radius: 6px;
  box-shadow: 0 8px 18px var(--form-build-rule-shadow, rgba(0, 0, 0, 0.06));
  .close-btn{
    position: absolute;
    right: -7px;
    top: -7px;
    display: block;
    width: 16px;
    height: 16px;
    line-height: 16px;
    background: #f56c6c;
    border-radius: 50%;
    color: #fff;
    text-align: center;
    z-index: 1;
    cursor: pointer;
    font-size: 12px;
    &:hover{
      background: #f78989;
    }
  }
  & + .reg-item{
    margin-top: 18px;
  }
}
.action-bar{
  & .el-button+.el-button {
    margin-left: 15px;
  }
  & i {
    font-size: 20px;
    vertical-align: middle;
    position: relative;
    top: -1px;
  }
}

.custom-tree-node{
  width: 100%;
  font-size: 14px;
  .node-operation{
    float: right;
  }
  i[class*="el-icon"] + i[class*="el-icon"]{
    margin-left: 6px;
  }
  .el-icon-plus{
    color: #409EFF;
  }
  .el-icon-delete{
    color: #157a0c;
  }
}

.left-scrollbar .el-scrollbar__view{
  overflow-x: hidden;
}

.el-rate{
  display: inline-block;
  vertical-align: text-top;
}
.el-upload__tip{
  line-height: 1.2;
}

$selectedColor: #f6f7ff;
$lighterBlue: #409EFF;

.container {
  position: relative;
  width: 100%;
  height: 100%;
  background: var(--form-build-bg, #fff);
  color: var(--form-build-text, #303133);
}

.components-list {
  padding: 8px;
  box-sizing: border-box;
  height: 100%;
  .components-item {
    display: inline-block;
    width: 48%;
    margin: 1%;
    transition: transform 0ms !important;
  }
}
.components-draggable{
  padding-bottom: 20px;
}
.components-title{
  font-size: 14px;
  color: var(--form-build-text, #222);
  margin: 6px 2px;
  .svg-icon{
    color: var(--form-build-muted, #666);
    font-size: 18px;
  }
}

.components-body {
  padding: 8px 10px;
  background: var(--form-build-component-bg, #f6f7ff);
  font-size: 12px;
  cursor: move;
  border: 1px dashed var(--form-build-component-border, #f6f7ff);
  border-radius: 3px;
  color: var(--form-build-text, #303133);
  .svg-icon{
    color: var(--form-build-muted, #777);
    font-size: 15px;
  }
  &:hover {
    border: 1px dashed var(--form-build-primary, #787be8);
    color: var(--form-build-primary, #787be8);
    .svg-icon {
      color: var(--form-build-primary, #787be8);
    }
  }
}

.left-board {
  width: 260px;
  position: absolute;
  left: 0;
  top: 0;
  height: 100vh;
  background: var(--form-build-panel-bg, #fff);
}
.left-scrollbar{
  height: calc(100vh - 42px);
  overflow: hidden;
}
.center-scrollbar {
  height: calc(100vh - 42px);
  overflow: hidden;
  border-left: 1px solid var(--form-build-border, #f1e8e8);
  border-right: 1px solid var(--form-build-border, #f1e8e8);
  box-sizing: border-box;
  background: var(--form-build-canvas-bg, #fff);
}
.center-board {
  height: 100vh;
  width: auto;
  margin: 0 350px 0 260px;
  box-sizing: border-box;
  background: var(--form-build-canvas-bg, #fff);
}
.empty-info{
  pointer-events: none;
  position: absolute;
  top: 46%;
  left: 0;
  right: 0;
  text-align: center;
  font-size: 18px;
  color: var(--form-build-empty-text, #ccb1ea);
  letter-spacing: 4px;
}
.action-bar{
  position: relative;
  height: 42px;
  text-align: right;
  padding: 0 15px;
  box-sizing: border-box;;
  border: 1px solid var(--form-build-border, #f1e8e8);
  border-top: none;
  border-left: none;
  background: var(--form-build-header-bg, #fff);
  .delete-btn{
    color: #F56C6C;
  }
}
.logo-wrapper{
  position: relative;
  height: 42px;
  background: var(--form-build-header-bg, #fff);
  border-bottom: 1px solid var(--form-build-border, #f1e8e8);
  box-sizing: border-box;
}
.logo{
  position: absolute;
  left: 12px;
  top: 6px;
  line-height: 30px;
  color: var(--form-build-primary, #00afff);
  font-weight: 600;
  font-size: 17px;
  white-space: nowrap;
  > img{
    width: 30px;
    height: 30px;
    vertical-align: top;
  }
  .github{
    display: inline-block;
    vertical-align: sub;
    margin-left: 15px;
    > img{
      height: 22px;
    }
  }
}

.center-board-row {
  padding: 12px 12px 15px 12px;
  box-sizing: border-box;
  & > .el-form {
    width: 100%;
    // 69 = 12+15+42
    height: calc(100vh - 69px);
  }
}
.drawing-board {
  height: 100%;
  position: relative;
  .components-body {
    padding: 0;
    margin: 0;
    font-size: 0;
  }
  .sortable-ghost {
    position: relative;
    display: block;
    overflow: hidden;
    &::before {
      content: " ";
      position: absolute;
      left: 0;
      right: 0;
      top: 0;
      height: 3px;
      background: rgb(89, 89, 223);
      z-index: 2;
    }
  }
  .components-item.sortable-ghost {
    width: 100%;
    height: 60px;
    background-color: $selectedColor;
  }
  .active-from-item {
    & > .el-form-item{
      background: var(--form-build-active-bg, #f6f7ff);
      border-radius: 6px;
    }
    & > .drawing-item-copy, & > .drawing-item-delete{
      display: initial;
    }
    & > .component-name{
      color: var(--form-build-primary, #409EFF);
    }
  }
  .el-form-item{
    margin-bottom: 15px;
  }
}
.drawing-item{
  position: relative;
  cursor: move;
  &.unfocus-bordered:not(.activeFromItem) > div:first-child  {
    border: 1px dashed #ccc;
  }
  .el-form-item{
    padding: 12px 10px;
  }
}
.drawing-row-item{
  position: relative;
  cursor: move;
  box-sizing: border-box;
  border: 1px dashed #ccc;
  border-radius: 3px;
  padding: 0 2px;
  margin-bottom: 15px;
  .drawing-row-item {
    margin-bottom: 2px;
  }
  .el-col{
    margin-top: 22px;
  }
  .el-form-item{
    margin-bottom: 0;
  }
  .drag-wrapper{
    width: 100%;
    min-height: 80px;
  }
  &.active-from-item{
    border: 1px dashed $lighterBlue;
  }
  .component-name{
    position: absolute;
    top: 0;
    left: 0;
    font-size: 12px;
    color: #bbb;
    display: inline-block;
    padding: 0 6px;
  }
}
.drawing-item, .drawing-row-item{
  &:hover {
    & > .el-form-item{
      background: var(--form-build-active-bg, #f6f7ff);
      border-radius: 6px;
    }
    & > .drawing-item-copy, & > .drawing-item-delete{
      display: initial;
    }
  }
  & > .drawing-item-copy, & > .drawing-item-delete{
    display: none;
    position: absolute;
    top: -10px;
    width: 22px;
    height: 22px;
    line-height: 22px;
    text-align: center;
    border-radius: 50%;
    font-size: 12px;
    border: 1px solid;
    cursor: pointer;
    z-index: 1;
  }
  & > .drawing-item-copy{
    right: 56px;
    border-color: $lighterBlue;
    color: $lighterBlue;
    background: #fff;
    &:hover{
      background: $lighterBlue;
      color: #fff;
    }
  }
  & > .drawing-item-delete{
    right: 24px;
    border-color: #F56C6C;
    color: #F56C6C;
    background: #fff;
    &:hover{
      background: #F56C6C;
      color: #fff;
    }
  }
}

.form-build-container {
  --form-build-primary: #409EFF;
  --form-build-bg: #fff;
  --form-build-panel-bg: #fff;
  --form-build-header-bg: #fff;
  --form-build-canvas-bg: #fff;
  --form-build-text: #303133;
  --form-build-muted: #606266;
  --form-build-border: #ebeef5;
  --form-build-component-bg: #f6f7ff;
  --form-build-component-border: #f6f7ff;
  --form-build-active-bg: #f6f7ff;
  --form-build-empty-text: #ccb1ea;
  --form-build-rule-bg: #ffffff;
  --form-build-rule-input-bg: #ffffff;
  --form-build-rule-input-text: #303133;
  --form-build-rule-text: #303133;
  --form-build-rule-placeholder: #909399;
  --form-build-rule-border: #dcdfe6;
  --form-build-rule-shadow: rgba(0, 0, 0, 0.06);
  --form-build-divider-bg: #ffffff;
  --form-build-divider-text: #303133;

  .action-bar .el-button--text {
    color: var(--form-build-primary);

    &.delete-btn {
      color: #F56C6C;
    }
  }

  .right-board {
    background: var(--form-build-panel-bg);
    color: var(--form-build-text);
  }

  .right-board .field-box {
    background: var(--form-build-panel-bg);
    border-left: 1px solid var(--form-build-border);
  }

  .center-tabs {
    background: var(--form-build-header-bg);

    .el-tabs__header {
      border-bottom-color: var(--form-build-border);
    }

    .el-tabs__item {
      color: var(--form-build-muted);

      &.is-active {
        color: var(--form-build-primary);
      }
    }

    .el-tabs__active-bar {
      background-color: var(--form-build-primary);
    }
  }

  .el-form-item__label,
  .el-radio,
  .el-checkbox,
  .el-input-number__decrease,
  .el-input-number__increase {
    color: var(--form-build-text);
  }

  .el-input__inner,
  .el-textarea__inner,
  .el-input-group__append,
  .el-input-number__decrease,
  .el-input-number__increase {
    background-color: var(--form-build-input-bg, #fff);
    border-color: var(--form-build-border);
    color: var(--form-build-text);
  }

  .el-input__inner,
  .el-textarea__inner {
    &:focus,
    &:hover {
      border-color: var(--form-build-primary);
    }
  }

  .el-slider__bar,
  .el-switch.is-checked .el-switch__core {
    background-color: var(--form-build-primary);
    border-color: var(--form-build-primary);
  }

  .drawing-item.unfocus-bordered:not(.activeFromItem) > div:first-child,
  .drawing-row-item {
    border-color: var(--form-build-border);
  }
}


@include meta.load-css("@/assets/styles/themes/pages/_form-builder.scss");
</style>
