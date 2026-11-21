/**
 * 根据表单组件配置收集并去重所需的导出 CSS，不影响构建器页面自身样式。
 */
const styles = {
  'el-rate': '.el-rate{display: inline-block; vertical-align: text-top;}',
  'el-upload': '.el-upload__tip{line-height: 1.2;}'
}

function addCss(cssList, el) {
  const css = styles[el.tag]
  css && cssList.indexOf(css) === -1 && cssList.push(css)
  if (el.children) {
    el.children.forEach(el2 => addCss(cssList, el2))
  }
}

export function makeUpCss(conf) {
  const cssList = []
  conf.fields.forEach(el => addCss(cssList, el))
  return cssList.join('\n')
}
