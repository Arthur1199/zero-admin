<template>
  <el-icon :class="{ 'is-loading': iconClass === 'el-icon-loading' }"><component :is="component" v-if="component" /></el-icon>
</template>
<script>
import { resolveIcon } from '@/utils/icons'
export default {
  name: 'AppIcon',
  props: { iconClass: { type: String, default: '' } },
  computed: { component() { return resolveIcon(this.iconClass) } }
}
</script>
