<template>
  <div />
</template>

<script>
/**
 * 页签刷新使用的中间路由；挂载后将目标路径和查询参数重新交给 Router 解析。
 */
export default {
  mounted() {
    const { params, query } = this.$route
    const { path } = params
    // 等待刷新中转页挂载，避免KeepAlive/Transition访问尚未建立的组件树。
    const target = Array.isArray(path) ? path.join('/') : path
    this.$router.replace({ path: '/' + (target || ''), query })
  }
}
</script>
