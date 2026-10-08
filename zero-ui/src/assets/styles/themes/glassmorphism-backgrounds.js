/**
 * 毛玻璃主题背景风格配置
 * 包含9种不同的渐变方案供用户选择
 */

export const glassmorphismBackgrounds = {
  // 方案2：海洋日落渐变(默认) - 放在最前面
  'ocean-sunset': {
    name: '海洋日落',
    gradient: 'linear-gradient(135deg, #1e3a8a 0%, #7c3aed 50%, #ec4899 100%)',
    description: '深蓝 → 紫罗兰 → 玫瑰金，优雅浪漫'
  },
  
  // 方案1：梦幻极光渐变
  'aurora-dream': {
    name: '梦幻极光',
    gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 50%, #f093fb 100%)',
    description: '紫蓝 → 粉紫 → 粉橙，色彩最丰富，梦幻感强'
  },
  
  // 方案3：彩虹糖果渐变
  'rainbow-candy': {
    name: '彩虹糖果',
    gradient: 'linear-gradient(135deg, #667eea 0%, #06b6d4 33%, #ec4899 66%, #fb923c 100%)',
    description: '紫蓝 → 青色 → 粉色 → 橙色，活泼鲜艳'
  },
  
  // 方案4：霓虹科技渐变
  'neon-tech': {
    name: '霓虹科技',
    gradient: 'linear-gradient(135deg, #5b21b6 0%, #db2777 50%, #06b6d4 100%)',
    description: '深紫 → 品红 → 青色，现代科技感'
  },
  
  // 方案5：紫罗兰梦境渐变
  'violet-dream': {
    name: '紫罗兰梦境',
    gradient: 'linear-gradient(135deg, #4c1d95 0%, #a78bfa 50%, #fbcfe8 100%)',
    description: '深紫 → 薰衣草紫 → 粉红，柔和梦幻'
  },
  
  // 方案6：火焰夕阳渐变
  'fire-sunset': {
    name: '火焰夕阳',
    gradient: 'linear-gradient(135deg, #991b1b 0%, #ea580c 50%, #fbbf24 100%)',
    description: '深红 → 橙红 → 金黄，热情奔放'
  },
  
  // 方案7：翡翠森林渐变
  'emerald-forest': {
    name: '翡翠森林',
    gradient: 'linear-gradient(135deg, #064e3b 0%, #10b981 50%, #06b6d4 100%)',
    description: '深绿 → 翡翠绿 → 青色，清新自然'
  },
  
  // 方案8：银河星空渐变
  'galaxy-starry': {
    name: '银河星空',
    gradient: 'linear-gradient(135deg, #1e293b 0%, #3730a3 50%, #1e40af 100%)',
    description: '深蓝 → 紫蓝 → 星蓝，深邃神秘'
  },
  
  // 方案9：玫瑰金奢华渐变
  'rose-luxury': {
    name: '玫瑰金奢华',
    gradient: 'linear-gradient(135deg, #9d174d 0%, #b45309 50%, #d97706 100%)',
    description: '玫瑰金 → 香槟金 → 浅金，奢华高贵'
  }
}

/**
 * 获取背景渐变CSS值
 * @param {string} styleKey - 背景风格key
 * @returns {string} CSS gradient值
 */
export function getGlassmorphismBackground(styleKey) {
  const bg = glassmorphismBackgrounds[styleKey] || glassmorphismBackgrounds['ocean-sunset']
  return bg.gradient
}

/**
 * 获取所有背景风格选项(用于下拉选择器)
 * @returns {Array} 选项数组
 */
export function getBackgroundOptions() {
  return Object.keys(glassmorphismBackgrounds).map(key => ({
    value: key,
    label: glassmorphismBackgrounds[key].name,
    description: glassmorphismBackgrounds[key].description
  }))
}
