<template>
  <div>
    <svg-icon icon-class="question" @click="goto" />
  </div>
</template>

<script>
/**
 * 项目文档入口按钮；按既有地址打开外部文档。
 */
export default {
  name: 'ZeroDoc',
  data() {
    return {
      url: 'http://doc.zero.vip/zero-vue'
    }
  },
  methods: {
    goto() {
      window.open(this.url)
    }
  }
}
</script>