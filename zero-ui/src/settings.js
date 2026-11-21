/**
 * 布局与主题的默认配置；运行时以 Vuex settings 及其本地持久化值为准，不把默认值误当成用户当前选择。
 */
export default {
  /**
   * 网页标题
   */
  title: import.meta.env.VITE_APP_TITLE,

  /**
   * 侧边栏主题 深色主题theme-dark，浅色主题theme-light
   */
  sideTheme: 'theme-dark',

  /**
   * 系统布局配置
   */
  showSettings: true,

  /**
   * 是否显示顶部导航
   */
  topNav: false,

  /**
   * 是否显示 tagsView
   */
  tagsView: true,
  
  /**
   * 显示页签图标
   */
  tagsIcon: false,

  /**
   * 是否固定头部
   */
  fixedHeader: false,

  /**
   * 是否显示logo
   */
  sidebarLogo: true,

  /**
   * 是否显示动态标题
   */
  dynamicTitle: false,

  /**
   * 是否显示底部版权
   */
  footerVisible: false,

  /**
   * 底部版权文本内容
   */
  footerContent: 'Copyright © 2026 Zero. All Rights Reserved.',

  /**
   * 菜单点击特效类型
   * gradient-gorgeous: 绚丽流光(5重特效)
   * ripple-gorgeous: 绚丽水波纹(5重特效)
   * pulse-gorgeous: 绚丽脉冲(6重特效)
   * none: 无特效
   */
  menuClickEffect: 'gradient-gorgeous',

  /**
   * 菜单音效开关
   * true: 开启菜单音效
   * false: 关闭菜单音效
   */
  menuSoundEnabled: true,

  /**
   * 主题风格
   * default: 默认风格
   * luxury-business: 轻奢商务风
   * vibrant-tech: 活力科技风
   * minimalist-future: 极简未来风
   * warm-nature: 温馨自然风
   * glassmorphism: 毛玻璃拟态风
   * material-design: 质感设计风
   * dark-premium: 高端暗黑风
   */
  themeStyle: 'glassmorphism',

  /**
   * 毛玻璃主题背景风格
   * aurora-dream: 梦幻极光渐变
   * ocean-sunset: 海洋日落渐变(默认)
   * rainbow-candy: 彩虹糖果渐变
   * neon-tech: 霓虹科技渐变
   * violet-dream: 紫罗兰梦境渐变
   * fire-sunset: 火焰夕阳渐变
   * emerald-forest: 翡翠森林渐变
   * galaxy-starry: 银河星空渐变
   * rose-luxury: 玫瑰金奢华渐变
   */
  glassmorphismBgStyle: 'ocean-sunset'
}
