<template>
  <div />
</template>

<script>
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
