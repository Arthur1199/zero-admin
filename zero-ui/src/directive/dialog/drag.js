/**
 * 历史 v-dialogDrag 入口：复用公共实现拖动标题栏，不在此重复维护定位或外观规则。
 */
import { createDialogInteraction } from './interaction'

// v-dialogDrag：拖动弹窗标题栏，公共实现负责视口边界和事件释放。
export default createDialogInteraction('move')
