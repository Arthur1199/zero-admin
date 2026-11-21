import { createDialogInteraction } from './interaction'

// v-dialogDrag：拖动弹窗标题栏，公共实现负责视口边界和事件释放。
export default createDialogInteraction('move')
