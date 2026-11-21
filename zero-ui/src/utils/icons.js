/**
 * 将历史 Element 图标标识和控件尺寸转换为当前 Plus 可用值，保留菜单和设计器存储的原始配置。
 */
import * as icons from '@element-plus/icons-vue'
import { markRaw } from 'vue'

// 旧菜单和设计器配置继续保存原图标字符串，仅在展示边界转换。
export const legacyIconAliases = {
  'mobile': 'Iphone', 'time': 'Clock', 'receiving': 'Box', 'trophy-1': 'Trophy',
  'medal-1': 'Medal', 'thumb': 'Pointer', 'circle-plus-outline': 'CirclePlus',
  's-custom': 'UserFilled', 's-tools': 'Tools', 's-goods': 'GoodsFilled', 's-help': 'HelpFilled',
  's-platform': 'Platform', 's-order': 'List', 's-data': 'Histogram', 's-operation': 'Operation',
  's-management': 'Management', 's-home': 'HomeFilled', 's-claim': 'Claim', 's-finance': 'Coin',
  's-comment': 'Comment', 's-flag': 'Flag', 's-marketing': 'TrendCharts', 's-shop': 'Shop',
  's-open': 'Menu', 's-grid': 'Grid', 's-check': 'CircleCheckFilled', 's-promotion': 'Promotion',
  's-unfold': 'Expand', 's-fold': 'Fold', 's-cooperation': 'Suitcase', 'picture-outline': 'Picture',
  'picture-outline-round': 'PictureRounded', 'star-off': 'Star', 'star-on': 'StarFilled',
  'circle-close': 'CircleClose', 'circle-close-outline': 'CircleClose', 'circle-check': 'CircleCheck',
  'circle-check-outline': 'CircleCheck', 'warning-outline': 'Warning', 'error': 'CircleCloseFilled',
  'success': 'CircleCheckFilled', 'warning': 'WarningFilled', 'info': 'InfoFilled',
  'upload2': 'Upload', 'download': 'Download', 'delete-solid': 'DeleteFilled',
  'more-outline': 'More', 'notebook-1': 'Notebook', 'notebook-2': 'Notebook',
  'copy-document': 'CopyDocument', 'document-copy': 'DocumentCopy', 'platform-eleme': 'ElemeFilled',
  'date': 'Calendar', 'close-notification': 'Bell', 'user-solid': 'UserFilled', 'help': 'HelpFilled',
  'video-camera-solid': 'VideoCameraFilled', 'message-solid': 'Message', 'edit-outline': 'EditPen',
  'phone-outline': 'Phone', 'question': 'QuestionFilled', 'remove-outline': 'Remove',
  'd-arrow-left': 'DArrowLeft', 'd-arrow-right': 'DArrowRight', 'top-left': 'TopLeft',
  'bottom-left': 'BottomLeft', 'bottom-right': 'BottomRight', 'top-right': 'TopRight'
}
export function resolveIcon(name) {
  if (!name || typeof name !== 'string') return name || undefined
  const legacy = name.replace(/^el-icon-/, '')
  const key = legacyIconAliases[legacy] || legacy.replace(/(^|-)([a-z])/g, (_, __, char) => char.toUpperCase())
  return icons[key] ? markRaw(icons[key]) : undefined
}
export function normalizeSize(size) {
  return size === 'mini' || size === 'small' ? 'small' : size === 'large' ? 'large' : 'default'
}
