<template>
  <footer v-if="visible" class="copyright">
    <span>{{ content }}</span>
  </footer>
</template>

<script>
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
.copyright {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
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
}

.copyright::before {
  display: none;
}

.copyright span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  text-shadow: var(--copyright-text-shadow, 0 1px 2px rgba(15, 23, 42, 0.16));
}
</style>
