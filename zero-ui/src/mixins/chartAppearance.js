/**
 * 图表外观生命周期：主题或系统动态偏好变化后更新 Canvas 展示选项，卸载时释放媒体查询监听。
 */
import { getChartAppearance } from '@/utils/chartTheme'

// Canvas 不会自动响应 CSS 变量变化；主题及系统动态偏好变化后更新展示选项。
export default {
  computed: { chartThemeStyle() { return this.$store.state.settings.themeStyle } },
  watch: { chartThemeStyle() { this.$nextTick(this.applyChartAppearance) } },
  mounted() {
    this._chartMotionQuery = window.matchMedia('(prefers-reduced-motion: reduce)')
    this._chartMotionListener = () => this.applyChartAppearance()
    this._chartMotionQuery.addEventListener('change', this._chartMotionListener)
  },
  beforeUnmount() {
    this._chartMotionQuery?.removeEventListener('change', this._chartMotionListener)
  },
  methods: {
    // 主题变更可能晚于组件卸载进入 nextTick，先检查实例有效性再写入选项。
    applyChartAppearance() {
      if (!this.chart || this.chart.isDisposed()) return
      this.chart.setOption(getChartAppearance(this.$el, this.chart.getOption(), this._chartMotionQuery?.matches))
    }
  }
}
