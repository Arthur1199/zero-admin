/**
 * 将当前 CSS 语义变量转换为 ECharts 外观选项；只补充已有坐标系，不修改系列数据、名称和取值范围。
 */
/**
 * @param {Element} element 已挂载并继承当前主题变量的图表容器
 * @param {Object} options 当前 ECharts 选项，用于确认已有坐标系和图例
 * @param {Boolean} reducedMotion 是否关闭动画
 * @returns {Object} 可合并的外观选项，不包含业务数据
 */
export function getChartAppearance(element, options = {}, reducedMotion = false) {
  const style = getComputedStyle(element)
  const read = (name, fallback) => style.getPropertyValue(name).trim() || fallback
  const color = read('--zero-text', style.color)
  const border = read('--zero-border', '#ebeef5')
  const result = {
    backgroundColor: 'transparent',
    animation: !reducedMotion,
    color: [read('--el-color-primary', '#409eff'), read('--el-color-success', '#67c23a'), read('--el-color-warning', '#e6a23c'), read('--el-color-danger', '#f56c6c'), '#909399', '#9b80ff'],
    textStyle: { color },
    tooltip: {
      confine: true,
      backgroundColor: read('--zero-surface-muted', '#ffffff'),
      borderColor: border,
      textStyle: { color }
    }
  }
  // 只更新已经存在的坐标系；饼图不应因为主题切换而被添加坐标轴。
  for (const axis of ['xAxis', 'yAxis']) {
    if (!options[axis]) continue
    const axes = Array.isArray(options[axis]) ? options[axis] : [options[axis]]
    result[axis] = axes.map(() => ({
      axisLabel: { color }, nameTextStyle: { color },
      axisLine: { lineStyle: { color: border } },
      splitLine: { lineStyle: { color: border } }
    }))
  }
  if (options.legend) result.legend = (Array.isArray(options.legend) ? options.legend : [options.legend]).map(() => ({ textStyle: { color } }))
  if (options.radar) result.radar = (Array.isArray(options.radar) ? options.radar : [options.radar]).map(() => ({
    axisName: { color }, axisLine: { lineStyle: { color: border } },
    splitLine: { lineStyle: { color: border } },
    splitArea: { areaStyle: { color: ['transparent', read('--zero-surface-hover', '#f5f7fa')], shadowBlur: 0 } }
  }))
  return result
}
