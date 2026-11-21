<template>
  <i-frame :src="url" />
</template>
<script>
/**
 * 数据源监控的内嵌页面入口；通过公共 iFrame 组件显示监控地址。
 */
import iFrame from "@/components/iFrame/index"
export default {
  name: "Druid",
  components: { iFrame },
  data() {
    return {
      url: import.meta.env.VITE_APP_BASE_API + "/druid/login.html"
    }
  }
}
</script>
