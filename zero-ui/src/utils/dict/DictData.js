/**
 * 标准字典项的数据结构；label 用于展示，value 用于绑定和提交，raw 保留原始响应。
 */
/**
 * @classdesc 字典数据
 * @property {String} label 标签
 * @property {*} value 绑定或提交的实际值，保留原始值类型
 * @property {Object} raw 原始数据
 */
export default class DictData {
  constructor(label, value, raw) {
    this.label = label
    this.value = value
    this.raw = raw
  }
}
