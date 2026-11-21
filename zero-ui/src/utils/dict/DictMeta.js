/**
 * 字典类型、数据请求和字段转换的元数据；支持类型字符串或配置对象，并合并全局默认项。
 */
import { mergeRecursive } from "@/utils/zero"
import DictOptions from './DictOptions'

/**
 * @classdesc 字典元数据
 * @property {String} type 类型
 * @property {Function} request 请求
 * @property {Function} responseConverter 将接口响应转换为 DictData 数组
 * @property {String} labelField 标签字段名
 * @property {String} valueField 绑定值字段名
 * @property {Boolean} lazy 是否跳过初始化加载，后续由调用方显式加载
 */
export default class DictMeta {
  constructor(options) {
    this.type = options.type
    this.request = options.request
    this.responseConverter = options.responseConverter
    this.labelField = options.labelField
    this.valueField = options.valueField
    this.lazy = options.lazy === true
  }
}


/**
 * 解析字典元数据
 * @param {Object} options
 * @returns {DictMeta}
 */
DictMeta.parse= function(options) {
  let opts = null
  if (typeof options === 'string') {
    opts = DictOptions.metas[options] || {}
    opts.type = options
  } else if (typeof options === 'object') {
    opts = options
  }
  opts = mergeRecursive(DictOptions.metas['*'], opts)
  return new DictMeta(opts)
}
