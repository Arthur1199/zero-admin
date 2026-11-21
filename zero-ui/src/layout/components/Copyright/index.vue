<template>
  <teleport to="body">
    <footer v-if="visible" class="copyright">
      <span>{{ content }}</span>
    </footer>
  </teleport>
</template>

<script>
/**
 * 读取全局开关与版权文本并展示；位置由公共布局样式负责，不随业务表格高度单独定位。
 */
export default {
  computed: {
    visible() {
      return this.$store.state.settings.footerVisible
    },
    content() {
      return this.$store.state.settings.footerContent
    }
  }
}
</script>

<style scoped>
/* 组件局部布局与展示样式；跨页面主题及 body 弹层规则在 assets/styles 中统一维护。 */
.copyright {
  position: fixed;
  bottom: 0;
  right: 0;
  max-width: 100%;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 0 22px;
  text-align: right;
  background: transparent;
  color: var(--copyright-color, #64748b);
  font-size: 13px;
  line-height: 1;
  border-top: none;
  box-shadow: none;
  z-index: 8;
  pointer-events: none;
}

.copyright::before {
  display: none;
}

.copyright span {
  pointer-events: auto;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  text-shadow: var(--copyright-text-shadow, 0 1px 2px rgba(15, 23, 42, 0.16));
}
</style>
