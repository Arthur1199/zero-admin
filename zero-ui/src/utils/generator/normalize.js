import { normalizeSize, resolveIcon } from '@/utils/icons'
import { timeRangeProps } from './timeRange'

/** 旧设计器JSON仅在组件消费边界转换，不改变保存字段和业务值类型。 */
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
