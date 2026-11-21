import { createDialogInteraction } from './interaction'

// v-dialogDragWidth：拖动弹窗右边缘调整宽度，公共实现负责视口边界和事件释放。
export default createDialogInteraction('width')
