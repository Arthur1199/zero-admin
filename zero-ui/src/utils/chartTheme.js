/** 图表只读取已生效的主题变量；不改变业务数据、系列名称和坐标范围。 */
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
