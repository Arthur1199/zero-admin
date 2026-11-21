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
    applyChartAppearance() {
      if (!this.chart || this.chart.isDisposed()) return
      this.chart.setOption(getChartAppearance(this.$el, this.chart.getOption(), this._chartMotionQuery?.matches))
    }
  }
}
