<template>
  <div id="app">
    <el-config-provider :size="elementSize" :table="tableConfig"><router-view /></el-config-provider>
    <theme-picker :show-picker="false" />
  </div>
</template>

<script>
import ThemePicker from "@/components/ThemePicker"
import { mapState } from 'vuex'
import { glassmorphismBackgrounds, getGlassmorphismBackground } from '@/assets/styles/themes/glassmorphism-backgrounds'

const THEME_CLASSES = [
  'theme-luxury-business',
  'theme-vibrant-tech',
  'theme-minimalist-future',
  'theme-warm-nature',
  'theme-glassmorphism',
  'theme-material-design',
  'theme-dark-premium',
  'theme-nordic-minimal'
]

export default {
  name: "App",
  components: { ThemePicker },
  computed: {
    elementSize() { return this.$store.getters.size },
    tableConfig() {
      // 毛玻璃表格的模糊滤镜会影响浮层定位，将溢出提示挂到 body。
      return this.themeStyle === 'glassmorphism' ? { tooltipOptions: { appendTo: 'body' } } : undefined
    },
    ...mapState({
      themeStyle: state => state.settings.themeStyle,
      glassmorphismBgStyle: state => state.settings.glassmorphismBgStyle
    }),
    // 判断是否在登录页
    isLoginPage() {
      return this.$route.path === '/login'
    }
  },
  watch: {
    themeStyle: {
      handler(newVal) {
        this.applyTheme(newVal)
      },
      immediate: true
    },
    glassmorphismBgStyle: {
      handler(newVal) {
        // 当毛玻璃背景风格变化时，动态更新CSS变量
        if (this.themeStyle === 'glassmorphism') {
          this.updateGlassmorphismBackground(newVal)
        }
      },
      immediate: true
    },
    // 监听路由变化，确保在登录页时移除主题
    '$route'(to, from) {
      // 更新表单构建页面标识
      this.updateFormBuilderFlag(to.path)
      
      if (to.path === '/login') {
        // 进入登录页，移除所有主题
        this.removeAllThemes()
      } else if (from.path === '/login') {
        // 离开登录页，重新应用当前主题
        this.applyTheme(this.themeStyle)
      }
    }
  },
  methods: {
    applyTheme(newVal) {
      // 如果在登录页，不应用任何主题
      if (this.isLoginPage) {
        this.removeAllThemes()
        return
      }

      // 切换主题时先清理全部旧主题，避免旧值不准导致毛玻璃样式残留
      this.removeAllThemes()

      // 添加新主题类名
      if (newVal && newVal !== 'default') {
        document.body.classList.add(`theme-${newVal}`)
        if (newVal === 'glassmorphism') {
          this.updateGlassmorphismBackground(this.glassmorphismBgStyle)
        }
      }
      this.refreshThemeRuntime(newVal)
    },
    removeAllThemes() {
      THEME_CLASSES.forEach(className => {
        document.body.classList.remove(className)
      })
      this.removeLayoutThemeClasses()
      this.removeGlassmorphismBackgroundClasses()
      this.removeGlassmorphismRuntimeStyles()
    },
    removeLayoutThemeClasses() {
      const layout = document.querySelector('#app .app-wrapper')
      if (!layout) {
        return
      }
      THEME_CLASSES.forEach(className => {
        layout.classList.remove(className)
      })
    },
    syncLayoutThemeClass(themeStyle) {
      const layout = document.querySelector('#app .app-wrapper')
      if (!layout) {
        return
      }
      this.removeLayoutThemeClasses()
      if (themeStyle && themeStyle !== 'default') {
        layout.classList.add(`theme-${themeStyle}`)
      }
    },
    refreshThemeRuntime(themeStyle) {
      this.$nextTick(() => {
        this.syncLayoutThemeClass(themeStyle)
        this.clearElementTableHoverState()
        window.dispatchEvent(new Event('resize'))
      })
    },
    clearElementTableHoverState() {
      document.querySelectorAll('.el-table__body tr.hover-row').forEach(row => {
        row.classList.remove('hover-row')
      })
    },
    // 更新表单构建页面标识
    updateFormBuilderFlag(path) {
      // 判断是否是表单构建页面
      if (path === '/tool/build' || path.startsWith('/tool/build/') || path.includes('build')) {
        document.body.classList.add('form-builder-page')
      } else {
        document.body.classList.remove('form-builder-page')
      }
    },
    // 更新毛玻璃主题背景渐变
    updateGlassmorphismBackground(styleKey) {
      const backgroundKey = styleKey || 'ocean-sunset'
      const gradient = getGlassmorphismBackground(backgroundKey)
      const root = document.documentElement
      this.removeGlassmorphismBackgroundClasses()
      root.style.setProperty('--glassmorphism-bg-gradient', gradient)
      document.body.classList.add(`glass-bg-${backgroundKey}`)
    },
    // 移除毛玻璃背景风格类名，避免切换背景后旧样式残留
    removeGlassmorphismBackgroundClasses() {
      Object.keys(glassmorphismBackgrounds).forEach(key => {
        document.body.classList.remove(`glass-bg-${key}`)
      })
      document.documentElement.style.removeProperty('--glassmorphism-bg-gradient')
    },
    removeGlassmorphismRuntimeStyles() {
      const style = document.getElementById('glassmorphism-hover-override')
      if (style) {
        style.remove()
      }
    }
  },
  mounted() {
    // 组件挂载后，检查当前路由
    this.updateFormBuilderFlag(this.$route.path)
    // 初始化毛玻璃背景渐变
    if (this.themeStyle === 'glassmorphism') {
      this.updateGlassmorphismBackground(this.glassmorphismBgStyle)
    }
  }
}
</script>
