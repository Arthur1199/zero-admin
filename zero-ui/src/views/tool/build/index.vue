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


body.theme-glassmorphism.form-builder-page .form-build-container {
  --form-build-primary: #93c5fd;
  --form-build-bg: var(--glassmorphism-bg-gradient, linear-gradient(135deg, #1e3a8a 0%, #7c3aed 50%, #ec4899 100%));
  --form-build-panel-bg: rgba(255, 255, 255, 0.12);
  --form-build-header-bg: rgba(255, 255, 255, 0.16);
  --form-build-canvas-bg: rgba(255, 255, 255, 0.08);
  --form-build-input-bg: rgba(255, 255, 255, 0.92);
  --form-build-text: #ffffff;
  --form-build-muted: rgba(255, 255, 255, 0.76);
  --form-build-border: rgba(255, 255, 255, 0.24);
  --form-build-component-bg: rgba(255, 255, 255, 0.14);
  --form-build-component-border: rgba(255, 255, 255, 0.2);
  --form-build-active-bg: rgba(255, 255, 255, 0.16);
  --form-build-empty-text: rgba(255, 255, 255, 0.58);
  --form-build-rule-bg: rgba(255, 255, 255, 0.16);
  --form-build-rule-input-bg: rgba(255, 255, 255, 0.9);
  --form-build-rule-input-text: #303133;
  --form-build-rule-text: #ffffff;
  --form-build-rule-placeholder: #909399;
  --form-build-rule-border: rgba(255, 255, 255, 0.26);
  --form-build-rule-shadow: rgba(0, 0, 0, 0.16);
  --form-build-divider-bg: rgba(255, 255, 255, 0.16);
  --form-build-divider-text: #ffffff;

  .left-board,
  .right-board,
  .logo-wrapper,
  .action-bar {
    backdrop-filter: blur(16px);
    -webkit-backdrop-filter: blur(16px);
  }

  .action-bar {
    .el-button--text {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      min-width: 88px;
      height: 30px;
      padding: 0 12px;
      border: 1px solid rgba(255, 255, 255, 0.28);
      border-radius: 8px;
      background: rgba(255, 255, 255, 0.16);
      color: #ffffff;
      font-weight: 600;
      text-shadow: 0 1px 4px rgba(0, 0, 0, 0.22);
      box-shadow: 0 8px 18px rgba(0, 0, 0, 0.12), inset 0 1px 0 rgba(255, 255, 255, 0.18);

      i {
        position: static;
        top: auto;
        margin-right: 6px;
        line-height: 1;
        color: #ffffff;
      }

      &:hover,
      &:focus {
        border-color: rgba(255, 255, 255, 0.48);
        background: rgba(255, 255, 255, 0.24);
        color: #ffffff;
      }
    }

    .delete-btn {
      border-color: rgba(255, 120, 120, 0.38);
      background: rgba(245, 108, 108, 0.18);
      color: #ff6b6b;

      i {
        color: #ff6b6b;
      }

      &:hover,
      &:focus {
        border-color: rgba(255, 120, 120, 0.62);
        background: rgba(245, 108, 108, 0.26);
        color: #ff8585;

        i {
          color: #ff8585;
        }
      }
    }
  }

  .el-input__inner,
  .el-textarea__inner {
    color: #303133;
  }
}


body.theme-luxury-business.form-builder-page .form-build-container {
  --form-build-primary: #d4af37;
  --form-build-bg: #0f1419;
  --form-build-panel-bg: #1a1f2e;
  --form-build-header-bg: #1a1f2e;
  --form-build-canvas-bg: #111827;
  --form-build-input-bg: #242b3b;
  --form-build-text: #e8eaf0;
  --form-build-muted: #b8bcc8;
  --form-build-border: #2a3142;
  --form-build-component-bg: rgba(212, 175, 55, 0.08);
  --form-build-component-border: rgba(212, 175, 55, 0.18);
  --form-build-active-bg: rgba(212, 175, 55, 0.12);
  --form-build-rule-bg: rgba(212, 175, 55, 0.08);
  --form-build-rule-input-bg: #242b3b;
  --form-build-rule-input-text: #e8eaf0;
  --form-build-rule-text: #e8eaf0;
  --form-build-rule-placeholder: #8f96a8;
  --form-build-rule-border: rgba(212, 175, 55, 0.22);
  --form-build-rule-shadow: rgba(0, 0, 0, 0.22);
}


body.theme-vibrant-tech.form-builder-page .form-build-container {
  --form-build-primary: #00f2fe;
  --form-build-bg: #0a0e27;
  --form-build-panel-bg: #141a35;
  --form-build-header-bg: #141a35;
  --form-build-canvas-bg: #0e1530;
  --form-build-input-bg: #1b2446;
  --form-build-text: #ffffff;
  --form-build-muted: #c0c9ff;
  --form-build-border: #2a3552;
  --form-build-component-bg: rgba(0, 242, 254, 0.08);
  --form-build-component-border: rgba(0, 242, 254, 0.18);
  --form-build-active-bg: rgba(0, 242, 254, 0.12);
  --form-build-rule-bg: rgba(0, 242, 254, 0.08);
  --form-build-rule-input-bg: #1b2446;
  --form-build-rule-input-text: #ffffff;
  --form-build-rule-text: #ffffff;
  --form-build-rule-placeholder: #8fa3d9;
  --form-build-rule-border: rgba(0, 242, 254, 0.22);
  --form-build-rule-shadow: rgba(0, 0, 0, 0.22);
}


body.theme-minimalist-future.form-builder-page .form-build-container {
  --form-build-primary: #00bcd4;
  --form-build-bg: linear-gradient(135deg, #f7fcff 0%, #eef8ff 52%, #f5f8ff 100%);
  --form-build-panel-bg: rgba(246, 252, 255, 0.96);
  --form-build-header-bg: rgba(255, 255, 255, 0.88);
  --form-build-canvas-bg: rgba(255, 255, 255, 0.72);
  --form-build-input-bg: rgba(255, 255, 255, 0.92);
  --form-build-text: #172033;
  --form-build-muted: #5c6b82;
  --form-build-border: rgba(0, 188, 212, 0.18);
  --form-build-component-bg: rgba(255, 255, 255, 0.82);
  --form-build-component-border: rgba(0, 188, 212, 0.22);
  --form-build-active-bg: rgba(0, 188, 212, 0.10);
  --form-build-empty-text: rgba(92, 107, 130, 0.52);
  --form-build-rule-bg: rgba(255, 255, 255, 0.82);
  --form-build-rule-input-bg: rgba(255, 255, 255, 0.94);
  --form-build-rule-input-text: #172033;
  --form-build-rule-text: #172033;
  --form-build-rule-placeholder: #7c91a6;
  --form-build-rule-border: rgba(0, 188, 212, 0.24);
  --form-build-rule-shadow: rgba(26, 73, 105, 0.10);
  --form-build-divider-bg: rgba(246, 252, 255, 0.96);
  --form-build-divider-text: #365065;

  .left-board,
  .right-board {
    border-color: var(--form-build-border) !important;
    box-shadow: 10px 0 28px rgba(26, 73, 105, 0.08);
    backdrop-filter: blur(14px);
    -webkit-backdrop-filter: blur(14px);
  }

  .right-board {
    box-shadow: -10px 0 28px rgba(26, 73, 105, 0.08);
  }

  .logo-wrapper,
  .action-bar {
    background: var(--form-build-header-bg) !important;
    border-color: var(--form-build-border) !important;
    backdrop-filter: blur(14px);
    -webkit-backdrop-filter: blur(14px);
  }

  .logo {
    color: #006c9a;
    text-shadow: none;
  }

  .components-title {
    color: #365065;
    font-weight: 700;

    .svg-icon {
      color: #00a7bd;
    }
  }

  .components-body {
    border-style: solid;
    border-radius: 8px;
    box-shadow: 0 8px 18px rgba(26, 73, 105, 0.06);

    &:hover {
      background: rgba(0, 188, 212, 0.08);
      box-shadow: 0 10px 22px rgba(0, 188, 212, 0.12);
    }
  }

  .center-scrollbar,
  .center-board {
    background:
      linear-gradient(rgba(0, 188, 212, 0.045) 1px, transparent 1px),
      linear-gradient(90deg, rgba(0, 188, 212, 0.045) 1px, transparent 1px),
      var(--form-build-canvas-bg) !important;
    background-size: 24px 24px, 24px 24px, auto;
  }

  .center-board-row > .el-form {
    background: rgba(255, 255, 255, 0.66);
    border: 1px solid rgba(0, 188, 212, 0.14);
    border-radius: 10px;
    box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.7);
  }

  .action-bar .el-button--text {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    min-width: 88px;
    height: 30px;
    padding: 0 12px;
    border: 1px solid rgba(0, 188, 212, 0.22);
    border-radius: 8px;
    background: rgba(255, 255, 255, 0.78);
    color: #365065;
    font-weight: 600;
    box-shadow: none;

    i {
      position: static;
      top: auto;
      margin-right: 6px;
      line-height: 1;
      color: #00a7bd;
    }

    &:hover,
    &:focus {
      background: rgba(0, 188, 212, 0.10);
      border-color: rgba(0, 188, 212, 0.45);
      color: #006c9a;
    }

    &.delete-btn {
      border-color: rgba(239, 68, 68, 0.32);
      color: #dc2626;

      i {
        color: #dc2626;
      }
    }
  }

  .center-tabs {
    background: var(--form-build-header-bg);

    .el-tabs__item {
      color: #5c6b82;
      font-weight: 600;

      &.is-active {
        color: #006c9a;
      }
    }
  }

  .el-input__inner,
  .el-textarea__inner,
  .el-input-group__append,
  .el-input-number__decrease,
  .el-input-number__increase {
    background: var(--form-build-input-bg) !important;
    border-color: rgba(0, 188, 212, 0.24) !important;
    color: #172033 !important;
  }

  .el-input__inner::placeholder,
  .el-textarea__inner::placeholder {
    color: #7c91a6 !important;
  }

  .el-slider__runway {
    background-color: rgba(0, 188, 212, 0.14);
  }

  .el-switch__core {
    background: rgba(138, 161, 180, 0.24) !important;
    border-color: rgba(138, 161, 180, 0.30) !important;
  }

  .drawing-item.unfocus-bordered:not(.activeFromItem) > div:first-child,
  .drawing-row-item {
    border-color: rgba(0, 188, 212, 0.18) !important;
  }

  .drawing-item.active-from-item > .el-form-item,
  .drawing-item:hover > .el-form-item,
  .drawing-row-item.active-from-item {
    background: rgba(0, 188, 212, 0.08) !important;
    border-color: rgba(0, 188, 212, 0.36) !important;
  }
}

body.theme-minimalist-future.form-builder-page #app {
  .sidebar-container {
    background: rgba(246, 252, 255, 0.96) !important;
    border-right: 1px solid rgba(0, 188, 212, 0.18) !important;
    box-shadow: 10px 0 30px rgba(26, 73, 105, 0.08) !important;

    .sidebar-logo-container {
      background: rgba(248, 253, 255, 0.98) !important;
      border-bottom: 1px solid rgba(0, 188, 212, 0.16) !important;

      .sidebar-title {
        color: #00a7bd !important;
      }
    }

    .el-scrollbar,
    .el-scrollbar__view,
    .el-menu,
    .el-menu--inline,
    .el-sub-menu,
    .el-menu-item,
    .el-sub-menu__title,
    .el-menu .el-sub-menu > .el-sub-menu__title,
    .nest-menu .el-menu-item,
    .nest-menu .el-sub-menu > .el-sub-menu__title {
      background: transparent !important;
      background-color: transparent !important;
      color: #5c6b82 !important;
    }

    .el-menu-item,
    .el-sub-menu__title,
    .nest-menu .el-menu-item,
    .nest-menu .el-sub-menu > .el-sub-menu__title {
      border-radius: 8px !important;
      margin: 3px 10px !important;
      border-left: 3px solid transparent !important;
      box-shadow: none !important;
    }

    .el-menu-item:hover,
    .el-sub-menu__title:hover,
    .nest-menu .el-menu-item:hover,
    .nest-menu .el-sub-menu > .el-sub-menu__title:hover {
      background: rgba(0, 188, 212, 0.09) !important;
      color: #006c9a !important;
      border-left-color: rgba(0, 188, 212, 0.55) !important;
    }

    .el-menu-item.is-active,
    .el-sub-menu.is-active > .el-sub-menu__title,
    .el-sub-menu.is-opened > .el-sub-menu__title {
      background: linear-gradient(90deg, rgba(0, 188, 212, 0.16) 0%, rgba(108, 99, 255, 0.08) 100%) !important;
      color: #006c9a !important;
      border-left-color: #00bcd4 !important;
      box-shadow: 0 8px 18px rgba(0, 188, 212, 0.10) !important;
    }

    .el-menu-item i,
    .el-sub-menu__title i,
    .el-menu-item span,
    .el-sub-menu__title span,
    .svg-icon,
    .el-sub-menu__icon-arrow {
      color: #7a8aa0 !important;
      fill: #7a8aa0 !important;
    }

    .el-menu-item.is-active i,
    .el-sub-menu.is-active > .el-sub-menu__title i,
    .el-menu-item.is-active .svg-icon,
    .el-sub-menu.is-active > .el-sub-menu__title .svg-icon,
    .el-sub-menu.is-active > .el-sub-menu__title .el-sub-menu__icon-arrow,
    .el-menu-item.is-active span,
    .el-sub-menu.is-active > .el-sub-menu__title span {
      color: #00a7bd !important;
      fill: #00a7bd !important;
    }
  }

  .navbar {
    background: rgba(255, 255, 255, 0.86) !important;
    border-bottom: 1px solid rgba(0, 188, 212, 0.16) !important;
    box-shadow: 0 10px 28px rgba(26, 73, 105, 0.08) !important;
    backdrop-filter: blur(16px);
    -webkit-backdrop-filter: blur(16px);
  }

  .tags-view-container {
    background: rgba(255, 255, 255, 0.78) !important;
    border-bottom: 1px solid rgba(0, 188, 212, 0.14) !important;
    backdrop-filter: blur(14px);
    -webkit-backdrop-filter: blur(14px);

    .tags-view-item {
      background: rgba(255, 255, 255, 0.78) !important;
      border-color: rgba(0, 188, 212, 0.18) !important;
      color: #5c6b82 !important;

      &.active {
        background: linear-gradient(135deg, rgba(0, 188, 212, 0.16) 0%, rgba(108, 99, 255, 0.12) 100%) !important;
        border-color: rgba(0, 188, 212, 0.46) !important;
        color: #006c9a !important;
      }
    }
  }
}

body.theme-minimalist-future.form-builder-page #app .sidebar-container .el-menu .el-menu-item,
body.theme-minimalist-future.form-builder-page #app .sidebar-container .el-menu .el-sub-menu > .el-sub-menu__title,
body.theme-minimalist-future.form-builder-page #app .sidebar-container .el-menu .el-menu-item span,
body.theme-minimalist-future.form-builder-page #app .sidebar-container .el-menu .el-sub-menu > .el-sub-menu__title span {
  color: #5c6b82 !important;
}

body.theme-minimalist-future.form-builder-page #app .sidebar-container .el-menu .el-menu-item.is-active,
body.theme-minimalist-future.form-builder-page #app .sidebar-container .el-menu .el-sub-menu.is-active > .el-sub-menu__title,
body.theme-minimalist-future.form-builder-page #app .sidebar-container .el-menu .el-menu-item.is-active span,
body.theme-minimalist-future.form-builder-page #app .sidebar-container .el-menu .el-sub-menu.is-active > .el-sub-menu__title span {
  color: #006c9a !important;
}

body.theme-minimalist-future.form-builder-page .form-build-container .right-board .el-input__inner,
body.theme-minimalist-future.form-builder-page .form-build-container .right-board .el-textarea__inner,
body.theme-minimalist-future.form-builder-page .form-build-container .right-board .el-input-group__append {
  background: rgba(255, 255, 255, 0.94) !important;
  background-color: rgba(255, 255, 255, 0.94) !important;
  border-color: rgba(0, 188, 212, 0.24) !important;
  color: #172033 !important;
}


body.theme-dark-premium.form-builder-page .form-build-container {
  --form-build-primary: #d4af37;
  --form-build-bg: #000000;
  --form-build-panel-bg: #18181b;
  --form-build-header-bg: #18181b;
  --form-build-canvas-bg: #09090b;
  --form-build-input-bg: #202024;
  --form-build-text: #e4e4e7;
  --form-build-muted: #a1a1aa;
  --form-build-border: #27272a;
  --form-build-component-bg: rgba(212, 175, 55, 0.07);
  --form-build-component-border: rgba(212, 175, 55, 0.16);
  --form-build-active-bg: rgba(212, 175, 55, 0.1);
  --form-build-rule-bg: rgba(212, 175, 55, 0.07);
  --form-build-rule-input-bg: #202024;
  --form-build-rule-input-text: #e4e4e7;
  --form-build-rule-text: #e4e4e7;
  --form-build-rule-placeholder: #8a8a92;
  --form-build-rule-border: rgba(212, 175, 55, 0.18);
  --form-build-rule-shadow: rgba(0, 0, 0, 0.24);
}


body.theme-warm-nature.form-builder-page .form-build-container {
  --form-build-primary: #8b6f47;
  --form-build-bg: #faf8f3;
  --form-build-panel-bg: #fffaf2;
  --form-build-header-bg: #f5f0e8;
  --form-build-canvas-bg: #fffdf8;
  --form-build-input-bg: #ffffff;
  --form-build-text: #4a3f35;
  --form-build-muted: #8b7355;
  --form-build-border: #d4c4b0;
  --form-build-component-bg: rgba(139, 111, 71, 0.08);
  --form-build-component-border: rgba(139, 111, 71, 0.16);
  --form-build-active-bg: rgba(139, 111, 71, 0.1);
  --form-build-rule-bg: #fffaf2;
  --form-build-rule-input-bg: #ffffff;
  --form-build-rule-input-text: #4a3f35;
  --form-build-rule-text: #4a3f35;
  --form-build-rule-placeholder: #9b8569;
  --form-build-rule-border: #d4c4b0;
  --form-build-rule-shadow: rgba(90, 70, 44, 0.08);
}


body.theme-material-design.form-builder-page .form-build-container {
  --form-build-primary: #6750a4;
  --form-build-bg: #f5f5f5;
  --form-build-panel-bg: #ffffff;
  --form-build-header-bg: #ffffff;
  --form-build-canvas-bg: #fafafa;
  --form-build-input-bg: #ffffff;
  --form-build-text: #1c1b1f;
  --form-build-muted: #49454f;
  --form-build-border: #e5e5e5;
  --form-build-component-bg: rgba(103, 80, 164, 0.08);
  --form-build-component-border: rgba(103, 80, 164, 0.16);
  --form-build-active-bg: rgba(103, 80, 164, 0.1);
  --form-build-rule-bg: #ffffff;
  --form-build-rule-input-bg: #ffffff;
  --form-build-rule-input-text: #1c1b1f;
  --form-build-rule-text: #1c1b1f;
  --form-build-rule-placeholder: #79747e;
  --form-build-rule-border: #e5e5e5;
  --form-build-rule-shadow: rgba(0, 0, 0, 0.08);
}


body.theme-nordic-minimal.form-builder-page .form-build-container {
  --form-build-primary: #5e81ac;
  --form-build-bg: #eceff4;
  --form-build-panel-bg: #ffffff;
  --form-build-header-bg: #e5e9f0;
  --form-build-canvas-bg: #f8fafc;
  --form-build-input-bg: #ffffff;
  --form-build-text: #2e3440;
  --form-build-muted: #4c566a;
  --form-build-border: #d8dee9;
  --form-build-component-bg: rgba(94, 129, 172, 0.08);
  --form-build-component-border: rgba(94, 129, 172, 0.16);
  --form-build-active-bg: rgba(94, 129, 172, 0.1);
  --form-build-rule-bg: #ffffff;
  --form-build-rule-input-bg: #ffffff;
  --form-build-rule-input-text: #2e3440;
  --form-build-rule-text: #2e3440;
  --form-build-rule-placeholder: #6b7280;
  --form-build-rule-border: #d8dee9;
  --form-build-rule-shadow: rgba(46, 52, 64, 0.08);
}


</style>
