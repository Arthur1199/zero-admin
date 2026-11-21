/**
 * 基于 Clipboard 的复制指令；随元素创建和销毁实例，并把复制结果交给绑定的回调。
 */
/**
* v-clipboard 文字复制剪贴
* Copyright (c) 2021 zero
*/

import Clipboard from 'clipboard'
export default {
  mounted(el, binding, vnode) {
    switch (binding.arg) {
      case 'success':
        el._vClipBoard_success = binding.value
        break
      case 'error':
        el._vClipBoard_error = binding.value
        break
      default: {
        const clipboard = new Clipboard(el, {
          text: () => binding.value,
          action: () => binding.arg === 'cut' ? 'cut' : 'copy'
        })
        clipboard.on('success', e => {
          const callback = el._vClipBoard_success
          callback && callback(e)
        })
        clipboard.on('error', e => {
          const callback = el._vClipBoard_error
          callback && callback(e)
        })
        el._vClipBoard = clipboard
      }
    }
  },
  updated(el, binding) {
    if (binding.arg === 'success') {
      el._vClipBoard_success = binding.value
    } else if (binding.arg === 'error') {
      el._vClipBoard_error = binding.value
    } else {
      el._vClipBoard.text = function () { return binding.value }
      el._vClipBoard.action = () => binding.arg === 'cut' ? 'cut' : 'copy'
    }
  },
  unmounted(el, binding) {
    if (!el._vClipBoard && !el._vClipBoard_success && !el._vClipBoard_error) return
    if (binding.arg === 'success') {
      delete el._vClipBoard_success
    } else if (binding.arg === 'error') {
      delete el._vClipBoard_error
    } else {
      el._vClipBoard?.destroy()
      delete el._vClipBoard
    }
  }
}
