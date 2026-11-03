<template><span>{{ formatted }}</span></template>
<script>
export default {
  name: 'CountTo',
  props: {
    startVal: { type: Number, default: 0 }, endVal: { type: Number, default: 0 },
    duration: { type: Number, default: 2000 }, decimals: { type: Number, default: 0 }, separator: { type: String, default: ',' }
  },
  data() { return { current: this.startVal, frame: null } },
  computed: { formatted() { const [integer, fraction] = this.current.toFixed(this.decimals).split('.'); return integer.replace(/\B(?=(\d{3})+(?!\d))/g, this.separator) + (fraction ? '.' + fraction : '') } },
  watch: { endVal() { this.start() } },
  mounted() { this.start() },
  beforeUnmount() { cancelAnimationFrame(this.frame) },
  methods: {
    start() {
      cancelAnimationFrame(this.frame)
      const begin = performance.now(), duration = Math.max(0, this.duration)
      const tick = now => { const progress = duration ? Math.min(1, (now - begin) / duration) : 1; this.current = this.startVal + (this.endVal - this.startVal) * (1 - (1 - progress) ** 3); if (progress < 1) this.frame = requestAnimationFrame(tick) }
      this.frame = requestAnimationFrame(tick)
    }
  }
}
</script>
