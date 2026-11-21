import { createDialogInteraction } from './interaction'

// v-dialogDragHeight：拖动右下角调整宽高，公共实现负责视口边界和事件释放。
export default createDialogInteraction('height')
