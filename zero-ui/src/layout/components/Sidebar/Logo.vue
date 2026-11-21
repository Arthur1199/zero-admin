<template>
  <div 
    class="sidebar-logo-container" 
    :class="{'collapse':collapse, 'glassmorphism-theme': isGlassmorphismTheme}" 
    :style="collapse ? Object.assign({}, logoBackgroundStyle, {display: 'flex', alignItems: 'center', justifyContent: 'center'}) : logoBackgroundStyle">
    <transition name="sidebarLogoFade">
      <router-link v-if="collapse" key="collapse" class="sidebar-logo-link" to="/" style="display: flex !important; align-items: center !important; justify-content: center !important; width: 100% !important; height: 100% !important; padding: 0 !important;">
        <img v-if="logo" :src="logo" class="sidebar-logo" style="margin: 0 auto !important; display: block !important; position: relative !important; left: 0 !important; right: 0 !important;" />
        <h1 v-else class="sidebar-title" :style="{ color: sideTheme === 'theme-dark' ? variables.logoTitleColor : variables.logoLightTitleColor, margin: '0 auto' }">{{ title}} </h1>
      </router-link>
      <router-link v-else key="expand" class="sidebar-logo-link" to="/">
        <img v-if="logo" :src="logo" class="sidebar-logo" />
        <h1 class="sidebar-title" :style="logoTitleStyle">{{ title }} </h1>
      </router-link>
    </transition>
  </div>
</template>

<script>
import logoImg from '@/assets/logo/zero.ico'
import variables from '@/assets/styles/variables.module.scss'

export default {
  name: 'SidebarLogo',
  props: {
    collapse: {
      type: Boolean,
      required: true
    }
  },
  computed: {
    variables() {
      return variables
    },
    sideTheme() {
      return this.$store.state.settings.sideTheme
    },
    // 检测是否为毛玻璃拟态风主题
    isGlassmorphismTheme() {
      return this.$store.state.settings.themeStyle === 'glassmorphism'
    },
    // Logo背景样式
    logoBackgroundStyle() {
      // 毛玻璃主题返回undefined，让CSS接管
      if (this.isGlassmorphismTheme) {
        return {}
      }
      // 其他主题使用原有逻辑
      return {
        backgroundColor: this.sideTheme === 'theme-dark' 
          ? this.variables.menuBackground 
          : this.variables.menuLightBackground
      }
    },
    // Logo标题样式
    logoTitleStyle() {
      // 毛玻璃主题返回白色
      if (this.isGlassmorphismTheme) {
        return { color: '#ffffff' }
      }
      // 其他主题使用原有逻辑
      return {
        color: this.sideTheme === 'theme-dark' 
          ? this.variables.logoTitleColor 
          : this.variables.logoLightTitleColor
      }
    }
  },
  data() {
    return {
      title: import.meta.env.VITE_APP_TITLE,
      logo: logoImg
    }
  }
}
</script>

<style lang="scss" scoped>
.sidebarLogoFade-enter-active {
  transition: opacity 1.5s;
}

.sidebarLogoFade-enter-from,
.sidebarLogoFade-leave-to {
  opacity: 0;
}

.sidebar-logo-container {
  position: relative;
  width: 100%;
  height: 50px;
  line-height: 50px;
  background: #2b2f3a;
  overflow: hidden;

  & .sidebar-logo-link {
    height: 100%;
    width: 100%;
    display: block;
    text-align: center;

    & .sidebar-logo {
      width: 32px;
      height: 32px;
      vertical-align: middle;
      margin-right: 12px;
      // 保留图标底色和字样，避免主题滤镜将两者染成同一颜色。
      filter: none !important;
    }

    & .sidebar-title {
      display: inline-block;
      margin: 0;
      color: #fff;
      font-weight: 600;
      line-height: 50px;
      font-size: 14px;
      font-family: Avenir, Helvetica Neue, Arial, Helvetica, sans-serif;
      vertical-align: middle;
    }
  }

  // 折叠状态下的样式由内联style控制，这里不再定义
}
</style>
