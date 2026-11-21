<template>
  <div>
    <svg-icon icon-class="github" @click="goto" />
  </div>
</template>

<script>
/**
 * 项目源码仓库入口按钮；按既有地址打开外部仓库。
 */
export default {
  name: 'ZeroGit',
  data() {
    return {
      url: 'https://gitee.com/y_project/Zero-Vue'
    }
  },
  methods: {
    goto() {
      window.open(this.url)
    }
  }
}
</script>