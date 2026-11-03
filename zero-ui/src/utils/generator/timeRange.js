/** 将旧时间范围转换为Element Plus公开禁用回调；不改变HH:mm:ss提交格式。 */
export function timeRangeProps(selectableRange) {
  if (!selectableRange) return {}
  const seconds = value => value.split(':').reduce((result, part) => result * 60 + Number(part), 0)
  const ranges = (Array.isArray(selectableRange) ? selectableRange : [selectableRange])
    .map(range => String(range).split('-').map(value => value.trim()))
    .filter(range => range.length === 2 && range.every(value => /^\d{2}:\d{2}:\d{2}$/.test(value)))
    .map(range => range.map(seconds))
  if (!ranges.length) return {}
  const excluded = (count, start, width) => Array.from({ length: count }, (_, index) => index)
    .filter(index => !ranges.some(([begin, end]) => start + index * width <= end && start + (index + 1) * width - 1 >= begin))
  return {
    disabledHours: () => excluded(24, 0, 3600),
    disabledMinutes: hour => excluded(60, hour * 3600, 60),
    disabledSeconds: (hour, minute) => excluded(60, hour * 3600 + minute * 60, 1)
  }
}
