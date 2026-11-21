/**
 * 弹窗拖动和缩放的公共事件管理。保留三种指令入口，卸载或禁用时释放本指令的监听。
 * 仅改变弹窗几何尺寸；不覆盖 document 的事件属性，也不接管关闭、确认等按钮。
 */
/**
 * @param {'move'|'width'|'height'} mode 拖动方式；height 为右下角，宽高一起变化
 * @returns {Object} 带 mounted/updated/unmounted 的 Vue 指令定义
 */
export function createDialogInteraction(mode) {
  const instances = new WeakMap()

  function detach(el) {
    instances.get(el)?.()
    instances.delete(el)
  }

  function attach(el, binding) {
    if (binding.value === false || instances.has(el)) return
    const dialog = el.matches?.('.el-dialog') ? el : el.querySelector?.('.el-dialog')
    if (!dialog) return
    const handle = mode === 'move' ? dialog.querySelector('.el-dialog__header') : document.createElement('div')
    if (!handle) return
    // 只保存本指令会修改的定位属性及其优先级，禁用时恢复，避免覆盖其它内联样式。
    const original = {}
    const properties = ['position', 'margin-top', 'margin-right', 'margin-bottom', 'margin-left', 'left', 'top', 'width', 'height', 'transform']
    properties.forEach(key => { original[key] = [dialog.style.getPropertyValue(key), dialog.style.getPropertyPriority(key)] })
    if (mode === 'move') dialog.classList.add('zero-dialog-draggable')
    else {
      handle.className = `zero-dialog-resize-handle zero-dialog-resize-handle--${mode}`
      handle.setAttribute('aria-hidden', 'true')
      dialog.appendChild(handle)
    }
    let stop = () => {}
    const start = event => {
      if (event.button !== 0 || dialog.classList.contains('is-fullscreen')) return
      if (mode === 'move' && event.target.closest('button, a, input, textarea, select, [role="button"]')) return
      stop()
      const rect = dialog.getBoundingClientRect()
      if (!rect.width || !rect.height) return
      // CSS zoom 下鼠标和 rect 使用视口像素，style 使用布局像素，写入时需要换算。
      const scale = dialog.offsetWidth ? rect.width / dialog.offsetWidth : 1
      const pixels = value => `${value / scale}px`
      const style = getComputedStyle(dialog)
      const minWidth = (parseFloat(style.getPropertyValue('--zero-dialog-min-width')) || rect.width / scale) * scale
      const minHeight = (parseFloat(style.getPropertyValue('--zero-dialog-min-height')) || rect.height / scale) * scale
      const originX = event.clientX
      const originY = event.clientY
      const clamp = (value, min, max) => Math.min(Math.max(value, min), Math.max(min, max))
      // 统一用视口坐标，避免百分比居中和页面滚动造成首次拖动跳变。
      dialog.classList.add('zero-dialog-interacting')
      Object.assign(dialog.style, {
        position: 'fixed', margin: '0px', transform: 'none',
        left: pixels(rect.left), top: pixels(rect.top), width: pixels(rect.width)
      })
      const move = current => {
        current.preventDefault()
        const dx = current.clientX - originX
        const dy = current.clientY - originY
        if (mode === 'move') {
          dialog.style.left = pixels(clamp(rect.left + dx, 0, window.innerWidth - rect.width))
          dialog.style.top = pixels(clamp(rect.top + dy, 0, window.innerHeight - rect.height))
        } else {
          const availableWidth = Math.max(0, window.innerWidth - rect.left)
          dialog.style.width = pixels(clamp(rect.width + dx, Math.min(minWidth, availableWidth), availableWidth))
          if (mode === 'height') {
            const availableHeight = Math.max(0, window.innerHeight - rect.top)
            dialog.style.height = pixels(clamp(rect.height + dy, Math.min(minHeight, availableHeight), availableHeight))
          }
        }
      }
      stop = () => {
        document.removeEventListener('mousemove', move)
        document.removeEventListener('mouseup', stop)
        window.removeEventListener('blur', stop)
      }
      document.addEventListener('mousemove', move)
      document.addEventListener('mouseup', stop)
      window.addEventListener('blur', stop)
      event.preventDefault()
    }
    handle.addEventListener('mousedown', start)
    // 一处清理入口同时服务卸载和动态禁用；窗口失焦仅停止当前拖动，不移除指令。
    instances.set(el, () => {
      stop()
      dialog.classList.remove('zero-dialog-interacting')
      handle.removeEventListener('mousedown', start)
      if (mode === 'move') dialog.classList.remove('zero-dialog-draggable')
      else handle.remove()
      properties.forEach(key => { dialog.style.setProperty(key, ...original[key]) })
    })
  }

  return {
    mounted: attach,
    updated(el, binding) { binding.value === false ? detach(el) : attach(el, binding) },
    unmounted: detach
  }
}
