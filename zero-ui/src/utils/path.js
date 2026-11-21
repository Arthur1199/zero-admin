/**
 * 浏览器路由路径的合并与归一化；保留 POSIX 路径语义，避免向前端引入 Node path 依赖。
 */
/** 浏览器路由按POSIX路径合并，避免引入Node专用path模块。 */
export function resolvePath(...segments) {
  let combined = ''
  for (const segment of segments) {
    if (!segment) continue
    combined = segment.startsWith('/') ? segment : combined + '/' + segment
  }
  const result = []
  for (const part of combined.split('/')) {
    if (!part || part === '.') continue
    if (part === '..') result.pop()
    else result.push(part)
  }
  return '/' + result.join('/')
}
export default { resolve: resolvePath }
