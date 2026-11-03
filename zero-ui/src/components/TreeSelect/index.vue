<template>
  <el-tree-select class="zero-tree-select" v-bind="$attrs" :model-value="modelValue" :data="treeOptions" :props="treeProps" node-key="id" check-strictly filterable :clearable="clearable" :teleported="appendToBody" :render-after-expand="false" @update:model-value="updateValue">
    <template #default="{ data }"><span>{{ data.label }}<span v-if="showCount && data.children?.length"> ({{ countNodes(data.children) }})</span></span></template>
  </el-tree-select>
</template>
<script>
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
    normalizeNodes(nodes) {
      return (nodes || []).filter(Boolean).map(node => {
        const copy = { ...node, children: node.children ? [...node.children] : undefined }
        const normalized = this.normalizer ? this.normalizer(copy) : copy
        return { ...normalized, disabled: node.isDisabled === true || node.disabled === true, children: this.normalizeNodes(normalized.children) }
      })
    },
    countNodes(nodes) { return nodes.reduce((total, node) => total + 1 + this.countNodes(node.children || []), 0) },
    updateValue(value) { this.$emit('update:modelValue', value === null || value === '' ? undefined : value) }
  }
}
</script>
