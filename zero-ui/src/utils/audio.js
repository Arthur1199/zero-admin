/**
 * 音效管理工具
 * 统一管理系统中的音效播放
 */

// 音效类型枚举
export const SoundType = {
  MENU_EXPAND: 'menu-expand',          // 父菜单展开
  MENU_COLLAPSE: 'menu-collapse',      // 父菜单收起
  MENU_CLICK: 'menu-click',            // 子菜单点击
  TAB_CHANGE: 'menu-tab-change',       // Tab切换
  TAB_CLOSE: 'menu-tab-close'          // Tab关闭
}

// 音效文件路径映射
const SOUND_FILES = {
  [SoundType.MENU_EXPAND]: '/audio/menu/menu-expand.mp3',
  [SoundType.MENU_COLLAPSE]: '/audio/menu/menu-collapse.mp3',
  [SoundType.MENU_CLICK]: '/audio/menu/menu-click.mp3',
  [SoundType.TAB_CHANGE]: '/audio/menu/menu-tab-change.mp3',
  [SoundType.TAB_CLOSE]: '/audio/menu/menu-tab-close.mp3'
}

// 音频实例缓存
const audioCache = {}

// 音量控制（0.0 - 1.0）
const VOLUME_CONFIG = {
  [SoundType.MENU_EXPAND]: 0.3,
  [SoundType.MENU_COLLAPSE]: 0.3,
  [SoundType.MENU_CLICK]: 0.4,
  [SoundType.TAB_CHANGE]: 0.4,
  [SoundType.TAB_CLOSE]: 0.3
}

/**
 * 获取音频实例（带缓存）
 */
function getAudio(soundType) {
  if (!audioCache[soundType]) {
    const audio = new Audio(SOUND_FILES[soundType])
    audio.volume = VOLUME_CONFIG[soundType] || 0.5
    audioCache[soundType] = audio
  }
  return audioCache[soundType]
}

/**
 * 播放音效
 * @param {string} soundType - 音效类型（从SoundType枚举中选择）
 * @param {object} store - Vuex store实例（用于检查音效开关）
 */
export function playSound(soundType, store) {
  try {
    // 检查音效是否开启
    if (!store || !store.state.settings.menuSoundEnabled) {
      return
    }

    // 检查音效类型是否有效
    if (!SOUND_FILES[soundType]) {
      console.warn(`[音效] 未知的音效类型: ${soundType}`)
      return
    }

    // 获取音频实例
    const audio = getAudio(soundType)

    // 如果正在播放，重置到开始位置
    if (!audio.paused) {
      audio.currentTime = 0
    }

    // 播放音效
    audio.play().catch(err => {
      // 忽略用户未交互导致的播放失败
      if (err.name !== 'NotAllowedError') {
        console.warn(`[音效] 播放失败:`, err)
      }
    })
  } catch (err) {
    console.warn(`[音效] 播放音效时出错:`, err)
  }
}

/**
 * 停止音效播放
 * @param {string} soundType - 音效类型
 */
export function stopSound(soundType) {
  try {
    const audio = audioCache[soundType]
    if (audio && !audio.paused) {
      audio.pause()
      audio.currentTime = 0
    }
  } catch (err) {
    console.warn(`[音效] 停止音效时出错:`, err)
  }
}

/**
 * 清空音频缓存
 */
export function clearAudioCache() {
  Object.keys(audioCache).forEach(key => {
    const audio = audioCache[key]
    if (audio) {
      audio.pause()
      audio.src = ''
    }
  })
  Object.keys(audioCache).forEach(key => delete audioCache[key])
}

export default {
  SoundType,
  playSound,
  stopSound,
  clearAudioCache
}
