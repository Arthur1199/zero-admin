<template>
  <div class="statistics-chart" role="img" :aria-label="label">
    <div ref="canvas" class="chart-canvas" />
    <div v-if="empty" class="chart-empty">所选时段暂无采集数据</div>
  </div>
</template>

<script>
/**
 * 运行统计的通用 ECharts 容器；页面传入业务选项，组件维护尺寸监听和空态，外观由公共图表 mixin 适配。
 */
import { markRaw } from 'vue'
import * as echarts from 'echarts'
import chartAppearance from '@/mixins/chartAppearance'

export default {
  name: 'StatisticsChart',
  mixins: [chartAppearance],
  props: {
    options: { type: Object, required: true },
    label: { type: String, required: true },
    empty: { type: Boolean, default: false }
  },
  data() { return { chart: null, observer: null } },
  watch: {
    options: { deep: true, handler() { this.renderChart() } }
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
      this.chart.setOption({
        tooltip: { trigger: 'axis', confine: true, renderMode: 'richText' },
        grid: { left: 16, right: 20, bottom: 28, top: 42, containLabel: true },
        ...this.options,
        legend: { top: 2, ...this.options.legend },
        xAxis: this.options.xAxis ? { ...this.options.xAxis, axisLabel: { ...this.options.xAxis.axisLabel, hideOverlap: true } } : undefined
      }, true)
      this.applyChartAppearance()
    }
  }
}
</script>

<style scoped>
/* 组件局部布局与展示样式；跨页面主题及 body 弹层规则在 assets/styles 中统一维护。 */
.statistics-chart { position: relative; width: 100%; height: 300px; color: inherit; }
.chart-canvas { height: 100%; width: 100%; }
.chart-empty { position: absolute; inset: 45% 0 auto; text-align: center; color: inherit; opacity: .65; pointer-events: none; }
</style>
