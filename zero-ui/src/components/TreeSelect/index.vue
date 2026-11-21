<template>
  <el-tree-select class="zero-tree-select" v-bind="$attrs" :model-value="modelValue" :data="treeOptions" :props="treeProps" node-key="id" check-strictly filterable :clearable="clearable" :teleported="appendToBody" :render-after-expand="false" @update:model-value="updateValue">
    <template #default="{ data }"><span>{{ data.label }}<span v-if="showCount && data.children?.length"> ({{ countNodes(data.children) }})</span></span></template>
  </el-tree-select>
</template>
<script>
/**
 * 基于 Plus TreeSelect 的历史接口适配；递归归一化节点、保留禁用语义，并统一清空值的回传格式。
 */
export default {
  name: 'Treeselect',
  inheritAttrs: false,
  emits: ['update:modelValue'],
  props: {
    modelValue: { type: [String, Number, Array], default: undefined },
    options: { type: Array, default: () => [] },
    normalizer: { type: Function, default: null },
    showCount: { type: Boolean, default: false },
    appendToBody: { type: Boolean, default: true },
    clearable: { type: Boolean, default: true }
  },
  data() { return { treeProps: { label: 'label', children: 'children', disabled: 'disabled' } } },
  computed: { treeOptions() { return this.normalizeNodes(this.options) } },
  methods: {
    // 复制后再执行页面 normalizer，避免归一化过程改写父页面共享的部门或菜单树。
    normalizeNodes(nodes) {
      return (nodes || []).filter(Boolean).map(node => {
        const copy = { ...node, children: node.children ? [...node.children] : undefined }
        const normalized = this.normalizer ? this.normalizer(copy) : copy
        return { ...normalized, disabled: node.isDisabled === true || node.disabled === true, children: this.normalizeNodes(normalized.children) }
      })
    },
    countNodes(nodes) { return nodes.reduce((total, node) => total + 1 + this.countNodes(node.children || []), 0) },
    // 清空值统一回传 undefined，保持旧页面的必填校验和查询参数语义。
    updateValue(value) { this.$emit('update:modelValue', value === null || value === '' ? undefined : value) }
  }
}
</script>
