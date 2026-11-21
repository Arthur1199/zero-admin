<template>
  <el-icon :class="{ 'is-loading': iconClass === 'el-icon-loading' }"><component :is="component" v-if="component" /></el-icon>
</template>
<script>
/**
 * 脚本和模板中的 Element 图标桥接组件；接收历史图标标识，再交给统一映射解析。
 */
import { resolveIcon } from '@/utils/icons'
export default {
  name: 'AppIcon',
  props: { iconClass: { type: String, default: '' } },
  computed: { component() { return resolveIcon(this.iconClass) } }
}
</script>
