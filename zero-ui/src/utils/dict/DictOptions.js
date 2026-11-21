/**
 * 字典插件的默认数据源、字段候选名和响应转换器；具体业务通过 mergeOptions 追加配置。
 */
import { mergeRecursive } from "@/utils/zero"
import dictConverter from './DictConverter'

export const options = {
  metas: {
    '*': {
      /**
       * 字典请求，方法签名为function(dictMeta: DictMeta): Promise
       */
      request: (dictMeta) => {
        console.log(`load dict ${dictMeta.type}`)
        return Promise.resolve([])
      },
      /**
       * 字典响应数据转换器，方法签名为 function(response: Object, dictMeta: DictMeta): DictData[]
       */
      responseConverter,
      labelField: 'label',
      valueField: 'value',
    },
  },
  /**
   * 默认标签字段
   */
  DEFAULT_LABEL_FIELDS: ['label', 'name', 'title'],
  /**
   * 默认值字段
   */
  DEFAULT_VALUE_FIELDS: ['value', 'id', 'uid', 'key'],
}

/**
 * 映射字典
 * @param {Object} response 字典数据
 * @param {DictMeta} dictMeta 字典元数据
 * @returns {DictData[]} 转换后的字典选项数组；没有数据时返回空数组
 */
function responseConverter(response, dictMeta) {
  const dicts = response.content instanceof Array ? response.content : response
  if (dicts === undefined) {
    console.warn(`no dict data of "${dictMeta.type}" found in the response`)
    return []
  }
  return dicts.map(d => dictConverter(d, dictMeta))
}

// 合并到插件级默认选项，影响后续创建的字典实例，不在此直接发起数据请求。
export function mergeOptions(src) {
  mergeRecursive(options, src)
}

export default options
