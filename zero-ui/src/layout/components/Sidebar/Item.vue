<template>
  <svg-icon v-if="icon" :icon-class="icon" />
  <span v-if="title" class="sidebar-menu-title" :title="title.length > 5 ? title : undefined">{{ title }}</span>
</template>
<script>
/**
 * 侧栏图标和标题的最小展示单元；叶子项标题放入 title 插槽，以兼容折叠提示。
 */
export default { name: 'MenuItem', props: { icon: { type: String, default: '' }, title: { type: String, default: '' } } }
</script>
