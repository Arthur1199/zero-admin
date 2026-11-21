<template>
  <div v-loading="loading" :style="'height:' + height">
    <iframe
      :src="src"
      frameborder="no"
      style="width: 100%; height: 100%"
      scrolling="auto"
    />
  </div>
</template>
<script>
/**
 * 通用内嵌页面容器；接收调用方提供的地址，负责加载展示，不把内嵌页面当成后台 API 调用。
 */
export default {
  props: {
    src: {
      type: String,
      required: true
    },
  },
  data() {
    return {
      height: document.documentElement.clientHeight - 94.5 + "px;",
      loading: true,
      url: this.src
    }
  },
  mounted: function () {
    setTimeout(() => {
      this.loading = false
    }, 300)
    const that = this
    window.onresize = function temp() {
      that.height = document.documentElement.clientHeight - 94.5 + "px;"
    }
  }
}
</script>
