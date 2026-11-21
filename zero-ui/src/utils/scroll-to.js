/**
 * 文档纵向滚动动画工具；按当前页面的滚动位置插值，完成后通知调用方，不处理局部滚动容器。
 */
Math.easeInOutQuad = function(t, b, c, d) {
  t /= d / 2
  if (t < 1) {
    return c / 2 * t * t + b
  }
  t--
  return -c / 2 * (t * (t - 2) - 1) + b
}

// 优先使用浏览器动画帧；保留前缀及定时器回退以兼容历史调用环境。
var requestAnimFrame = (function() {
  return window.requestAnimationFrame || window.webkitRequestAnimationFrame || window.mozRequestAnimationFrame || function(callback) { window.setTimeout(callback, 1000 / 60) }
})()

/**
 * 同步写入常见的文档滚动节点，兼容不同浏览器的实际滚动根元素
 * @param {number} amount
 */
function move(amount) {
  document.documentElement.scrollTop = amount
  document.body.parentNode.scrollTop = amount
  document.body.scrollTop = amount
}

function position() {
  return document.documentElement.scrollTop || document.body.parentNode.scrollTop || document.body.scrollTop
}

/**
 * 将文档滚动到目标纵向位置；局部容器不在本工具范围内
 * @param {number} to 目标 scrollTop（像素）
 * @param {number} duration 动画时长（毫秒），未传时为 500
 * @param {Function} callback 动画结束后的可选回调
 */
export function scrollTo(to, duration, callback) {
  const start = position()
  const change = to - start
  const increment = 20
  let currentTime = 0
  duration = (typeof (duration) === 'undefined') ? 500 : duration
  var animateScroll = function() {
    // 累加本轮动画进度。
    currentTime += increment
    // 根据缓入缓出曲线计算当前滚动位置。
    var val = Math.easeInOutQuad(currentTime, start, change, duration)
    // 更新文档滚动根节点。
    move(val)
    // 未到时长继续排下一帧，结束后才执行回调。
    if (currentTime < duration) {
      requestAnimFrame(animateScroll)
    } else {
      if (callback && typeof (callback) === 'function') {
        // 回调由调用方提供，本工具只在结束时执行一次。
        callback()
      }
    }
  }
  animateScroll()
}
