/**
 * 历史 v-dialogDragHeight 入口：右下角同时调整宽度与高度，边界和监听清理由公共实现负责。
 */
import { createDialogInteraction } from './interaction'

// v-dialogDragHeight：拖动右下角调整宽高，公共实现负责视口边界和事件释放。
export default createDialogInteraction('height')
