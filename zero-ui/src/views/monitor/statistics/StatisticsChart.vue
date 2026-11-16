<template>
  <div class="statistics-chart" role="img" :aria-label="label">
    <div ref="canvas" class="chart-canvas" />
    <div v-if="empty" class="chart-empty">所选时段暂无采集数据</div>
  </div>
</template>

<script>
import { markRaw } from 'vue'
import * as echarts from 'echarts'

export default {
  name: 'StatisticsChart',
  props: {
    options: { type: Object, required: true },
    label: { type: String, required: true },
    empty: { type: Boolean, default: false }
  },
  data() { return { chart: null, observer: null } },
  computed: { themeStyle() { return this.$store.state.settings.themeStyle } },
  watch: {
    options: { deep: true, handler() { this.renderChart() } },
    themeStyle() { this.$nextTick(this.renderChart) }
  },
  mounted() {
    this.chart = markRaw(echarts.init(this.$refs.canvas))
    this.observer = markRaw(new ResizeObserver(() => this.chart?.resize()))
    this.observer.observe(this.$el)
    this.renderChart()
  },
  beforeUnmount() {
    this.observer?.disconnect()
    this.chart?.dispose()
    this.chart = null
  },
  methods: {
    renderChart() {
      if (!this.chart) return
      const color = getComputedStyle(this.$el).color
      this.chart.setOption({
        backgroundColor: 'transparent',
        color: ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399', '#9b80ff'],
        textStyle: { color },
        tooltip: { trigger: 'axis', confine: true, renderMode: 'richText' },
        grid: { left: 16, right: 20, bottom: 28, top: 42, containLabel: true },
        ...this.options,
        legend: { top: 2, ...this.options.legend, textStyle: { color } },
        xAxis: this.options.xAxis ? { ...this.options.xAxis, axisLabel: { ...this.options.xAxis.axisLabel, color, hideOverlap: true }, splitLine: { lineStyle: { color, opacity: 0.12 } } } : undefined,
        yAxis: this.options.yAxis ? { ...this.options.yAxis, axisLabel: { ...this.options.yAxis.axisLabel, color }, splitLine: { lineStyle: { color, opacity: 0.12 } } } : undefined
      }, true)
    }
  }
}
</script>

<style scoped>
.statistics-chart { position: relative; width: 100%; height: 300px; color: inherit; }
.chart-canvas { height: 100%; width: 100%; }
.chart-empty { position: absolute; inset: 45% 0 auto; text-align: center; color: inherit; opacity: .65; pointer-events: none; }
</style>
