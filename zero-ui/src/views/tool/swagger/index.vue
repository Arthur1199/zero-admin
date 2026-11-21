<template>
  <i-frame :src="url" />
</template>
<script>
/**
 * 接口文档的内嵌入口；加载后台 Swagger 页面，接口调试的认证由文档页面自身处理。
 */
import iFrame from "@/components/iFrame/index"
export default {
  name: "Swagger",
  components: { iFrame },
  data() {
    return {
      url: import.meta.env.VITE_APP_BASE_API + "/swagger-ui/index.html"
    }
  }
}
</script>
