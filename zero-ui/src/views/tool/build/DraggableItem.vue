<script>
/**
 * 递归渲染构建器的行布局与普通控件；拖动调整配置结构，值变更回传给画布草稿。
 */
import { h, resolveComponent } from 'vue'
import draggable from 'vuedraggable'
import FormRender from '@/utils/generator/render.jsx'
const element = (tag, props, slots) => h(resolveComponent(tag), props, slots)
export default {
  name: 'DraggableItem',
  props: ['element', 'index', 'drawingList', 'activeId', 'formConf'],
  emits: ['activeItem', 'copyItem', 'deleteItem'],
  methods: {
    buttons(item, index, parent) {
      return ['copy', 'delete'].map(action => h('span', {
        class: 'drawing-item-' + action, title: action === 'copy' ? '复制' : '删除',
        onClick: event => { event.stopPropagation(); this.$emit(action === 'copy' ? 'copyItem' : 'deleteItem', action === 'copy' ? item : index, parent) }
      }, [element('AppIcon', { iconClass: action === 'copy' ? 'el-icon-copy-document' : 'el-icon-delete' })]))
    },
    renderItem(item, index, parent) {
      const activate = event => { event.stopPropagation(); this.$emit('activeItem', item) }
      if (item.layout === 'colFormItem') {
        let className = this.activeId === item.formId ? 'drawing-item active-from-item' : 'drawing-item'
        if (this.formConf.unFocusedComponentBorder) className += ' unfocus-bordered'
        return element('el-col', { span: item.span, class: className, onClick: activate }, {
          default: () => [element('el-form-item', { labelWidth: item.labelWidth ? item.labelWidth + 'px' : undefined, label: item.label, required: item.required }, {
            default: () => h(FormRender, { key: item.renderKey, conf: item, 'onUpdate:modelValue': value => { item.defaultValue = value } })
          }), ...this.buttons(item, index, parent)]
        })
      }
      if (item.layout === 'rowFormItem') {
        return element('el-col', { span: item.span }, { default: () => element('el-row', {
          gutter: item.gutter, justify: item.justify, align: item.align, class: this.activeId === item.formId ? 'drawing-row-item active-from-item' : 'drawing-row-item', onClick: activate
        }, { default: () => [h('span', { class: 'component-name' }, item.componentName), h(draggable, {
          list: item.children || [], itemKey: 'formId', animation: 340, group: 'componentsGroup', class: 'drag-wrapper'
        }, { item: ({ element: child, index: childIndex }) => this.renderItem(child, childIndex, item.children) }), ...this.buttons(item, index, parent)] }) })
      }
      throw new Error('没有与' + item.layout + '匹配的layout')
    }
  },
  render() { return this.renderItem(this.element, this.index, this.drawingList) }
}
</script>
