/**
 * 旧设计器配置的消费边界转换：归一化尺寸、日期格式、图标及时间禁选规则，避免直接改写保存的原对象。
 */
import { normalizeSize, resolveIcon } from '@/utils/icons'
import { timeRangeProps } from './timeRange'

/**
 * @param {Object} field 保存的组件配置，使用副本转换以保留历史字段
 * @returns {Object} 供当前 Vue3/Plus 渲染与导出使用的配置副本
 */
export function normalizeField(field) {
  const result = { ...field }
  if (result.size) result.size = normalizeSize(result.size)
  for (const key of ['format', 'value-format']) if (typeof result[key] === 'string') result[key] = result[key].replace(/yyyy/g, 'YYYY').replace(/dd/g, 'DD')
  for (const key of ['icon', 'prefix-icon', 'suffix-icon']) if (result[key]) result[key] = resolveIcon(result[key])
  if (result['picker-options']) {
    result.shortcuts = result['picker-options'].shortcuts
    Object.assign(result, timeRangeProps(result['picker-options'].selectableRange))
    delete result['picker-options']
  }
  return result
}
