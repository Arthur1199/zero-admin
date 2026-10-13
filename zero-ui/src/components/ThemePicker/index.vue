<template><el-color-picker v-if="showPicker" v-model="theme" :predefine="colors" class="theme-picker" :popper-class="popperClass" @change="setTheme" /></template>
<script>
export default {
  props: {
    showPicker: { type: Boolean, default: true },
    popperClass: { type: String, default: 'theme-picker-dropdown' }
  },
  emits: ['change'],
  data() { return { colors: ['#409EFF', '#1890ff', '#304156', '#212121', '#11a983', '#13c2c2', '#6959CD', '#f5222d'] } },
  computed: { theme: { get() { return this.$store.state.settings.theme }, set(value) { this.$store.dispatch('settings/changeSetting', { key: 'theme', value: value || '#1890ff' }) } } },
  watch: { theme: { immediate: true, handler(value) { this.applyTheme(value) } } },
  methods: {
    applyTheme(value) {
      if (!/^#[\da-f]{6}$/i.test(value || '')) return
      const root = document.documentElement
      root.style.setProperty('--el-color-primary', value)
      const channels = [1, 3, 5].map(offset => parseInt(value.slice(offset, offset + 2), 16))
      for (let amount = 1; amount <= 9; amount++) {
        const color = channels.map(channel => Math.round(channel + (255 - channel) * amount / 10).toString(16).padStart(2, '0')).join('')
        root.style.setProperty('--el-color-primary-light-' + amount, '#' + color)
      }
      root.style.setProperty('--el-color-primary-dark-2', '#' + channels.map(channel => Math.round(channel * 0.8).toString(16).padStart(2, '0')).join(''))
    },
    setTheme(value) { this.applyTheme(value); this.$emit('change', value) }
  }
}
</script>
