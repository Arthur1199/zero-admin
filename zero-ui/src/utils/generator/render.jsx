import { h, resolveComponent } from 'vue'
import { normalizeField } from './normalize'

const metadata = new Set(['tag', 'layout', 'tagIcon', 'label', 'labelWidth', 'vModel', 'defaultValue', 'span', 'formId', 'renderKey', 'regList', 'required', 'changeTag', 'document', 'options', 'children', 'optionType', 'buttonText', 'showTip', 'fileSize', 'sizeUnit', 'prepend', 'append'])
const element = (tag, props, slots) => h(resolveComponent(tag), props, slots)

export default {
  name: 'FormRender',
  props: { conf: { type: Object, required: true } },
  emits: ['update:modelValue'],
  render() {
    const conf = normalizeField(this.conf)
    const props = Object.fromEntries(Object.entries(conf).filter(([key]) => !metadata.has(key)))
    if (conf.vModel !== undefined) {
      props.modelValue = conf.defaultValue
      props['onUpdate:modelValue'] = value => this.$emit('update:modelValue', value)
    }
    const slots = {}
    if (conf.tag === 'el-input') for (const slot of ['prepend', 'append']) if (conf[slot]) slots[slot] = () => conf[slot]
    if (conf.tag === 'el-button') slots.default = () => conf.buttonText || conf.label
    if (['el-select', 'el-radio-group', 'el-checkbox-group'].includes(conf.tag)) {
      const tag = conf.tag === 'el-select' ? 'el-option' : conf.tag.replace('-group', '') + (conf.optionType === 'button' ? '-button' : '')
      slots.default = () => (conf.options || []).map(option => element(tag, { value: option.value, label: option.label, disabled: option.disabled, border: conf.border }, { default: () => option.label }))
    }
    if (conf.tag === 'el-upload') {
      slots.default = () => conf['list-type'] === 'picture-card' ? element('AppIcon', { iconClass: 'el-icon-plus' }) : element('el-button', { type: 'primary' }, { default: () => conf.buttonText })
      if (conf.showTip) slots.tip = () => h('div', { class: 'el-upload__tip' }, `只能上传不超过 ${conf.fileSize}${conf.sizeUnit} 的${conf.accept}文件`)
    }
    return element(conf.tag, props, slots)
  }
}
