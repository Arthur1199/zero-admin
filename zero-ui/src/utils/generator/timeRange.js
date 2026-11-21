/**
 * 将历史 selectableRange 转为 Element Plus 禁选时间回调；保持时分秒提交格式和范围边界。
 */
/**
 * @param {string|string[]} selectableRange 一个或多个 HH:mm:ss-HH:mm:ss 范围
 * @returns {Object} 禁选小时、分钟及秒的回调；未配置或无法识别时返回空对象
 */
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
