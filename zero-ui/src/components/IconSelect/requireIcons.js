/**
 * 从构建时已知的 SVG 路径生成图标选择列表，列表名称与后端保存的图标标识保持一致。
 */
const sources = import.meta.glob('../../assets/icons/svg/*.svg')
export default Object.keys(sources).map(file => file.split('/').pop().replace(/\.svg$/, ''))
