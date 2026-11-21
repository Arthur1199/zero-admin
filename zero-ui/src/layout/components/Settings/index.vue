<template>
  <el-drawer size="280px" :model-value="showSettings" modal-class="zero-settings-overlay" :with-header="false" :append-to-body="true" :modal="false" :before-close="closeSetting" :lock-scroll="false">
    <div ref="settingsContent" class="drawer-container">
      <div>
        <div class="setting-drawer-content">
          <div class="setting-drawer-title">
            <h3 class="drawer-title">主题风格设置</h3>
            <button type="button" class="settings-close" aria-label="关闭设置" @click="closeSetting">
              <el-icon><component :is="resolveIcon('el-icon-close')" /></el-icon>
            </button>
          </div>
          
          <!-- 主题风格选择 -->
          <div class="drawer-item">
            <span>主题风格</span>
            <el-select v-model="themeStyle" placeholder="请选择" size="small" class="drawer-select" popper-class="zero-settings-popper">
              <el-option label="毛玻璃拟态风" value="glassmorphism" />
              <el-option label="轻奢商务风" value="luxury-business" />
              <el-option label="活力科技风" value="vibrant-tech" />
              <el-option label="极简未来风" value="minimalist-future" />
              <el-option label="温馨自然风" value="warm-nature" />
              <el-option label="质感设计风" value="material-design" />
              <el-option label="高端暗黑风" value="dark-premium" />
              <el-option label="原始风格" value="default" />
            </el-select>
          </div>

          <!-- 毛玻璃主题背景风格选择 -->
          <div v-if="themeStyle === 'glassmorphism'" class="drawer-item">
            <span>背景风格</span>
            <el-select v-model="glassmorphismBgStyle" placeholder="请选择" size="small" class="drawer-select-wide" popper-class="zero-settings-popper">
              <el-option
                v-for="option in backgroundOptions"
                :key="option.value"
                :label="option.label"
                :value="option.value">
                <span style="float: left">{{ option.label }}</span>
                <span style="float: right; color: #8492a6; font-size: 11px">{{ option.description.substring(0, 12) }}</span>
              </el-option>
            </el-select>
          </div>
          
          <!-- 只有选择默认风格时才显示以下选项 -->
          <div v-if="themeStyle === 'default'">
            <div class="setting-drawer-block-checbox">
              <div class="setting-drawer-block-checbox-item" @click="handleTheme('theme-dark')">
                <img src="@/assets/images/dark.svg" alt="dark">
                <div v-if="sideTheme === 'theme-dark'" class="setting-drawer-block-checbox-selectIcon" style="display: block;">
                  <i aria-label="图标: check" class="anticon anticon-check">
                    <svg viewBox="64 64 896 896" data-icon="check" width="1em" height="1em" :fill="theme" aria-hidden="true" focusable="false" class="">
                      <path d="M912 190h-69.9c-9.8 0-19.1 4.5-25.1 12.2L404.7 724.5 207 474a32 32 0 0 0-25.1-12.2H112c-6.7 0-10.4 7.7-6.3 12.9l273.9 347c12.8 16.2 37.4 16.2 50.3 0l488.4-618.9c4.1-5.1.4-12.8-6.3-12.8z"/>
                    </svg>
                  </i>
                </div>
              </div>
              <div class="setting-drawer-block-checbox-item" @click="handleTheme('theme-light')">
                <img src="@/assets/images/light.svg" alt="light">
                <div v-if="sideTheme === 'theme-light'" class="setting-drawer-block-checbox-selectIcon" style="display: block;">
                  <i aria-label="图标: check" class="anticon anticon-check">
                    <svg viewBox="64 64 896 896" data-icon="check" width="1em" height="1em" :fill="theme" aria-hidden="true" focusable="false" class="">
                      <path d="M912 190h-69.9c-9.8 0-19.1 4.5-25.1 12.2L404.7 724.5 207 474a32 32 0 0 0-25.1-12.2H112c-6.7 0-10.4 7.7-6.3 12.9l273.9 347c12.8 16.2 37.4 16.2 50.3 0l488.4-618.9c4.1-5.1.4-12.8-6.3-12.8z"/>
                    </svg>
                  </i>
                </div>
              </div>
            </div>

            <div class="drawer-item">
              <span>主题颜色</span>
              <theme-picker popper-class="theme-picker-dropdown zero-settings-popper" style="float: right;height: 26px;margin: -3px 8px 0 0;" @change="themeChange" />
            </div>
          </div>
        </div>

        <el-divider/>

        <h3 class="drawer-title">系统布局配置</h3>

        <div class="drawer-item">
          <span>开启 TopNav</span>
          <el-switch v-model="topNav" class="drawer-switch" />
        </div>

        <div class="drawer-item">
          <span>开启 Tags-Views</span>
          <el-switch v-model="tagsView" class="drawer-switch" />
        </div>

        <div class="drawer-item">
          <span>显示页签图标</span>
          <el-switch v-model="tagsIcon" :disabled="!tagsView" class="drawer-switch" />
        </div>

        <div class="drawer-item">
          <span>固定 Header</span>
          <el-switch v-model="fixedHeader" class="drawer-switch" />
        </div>

        <div class="drawer-item">
          <span>显示 Logo</span>
          <el-switch v-model="sidebarLogo" class="drawer-switch" />
        </div>

        <div class="drawer-item">
          <span>动态标题</span>
          <el-switch v-model="dynamicTitle" class="drawer-switch" />
        </div>

        <div class="drawer-item">
          <span>菜单点击特效</span>
          <el-select v-model="menuClickEffect" placeholder="请选择" size="small" class="drawer-select" popper-class="zero-settings-popper">
            <el-option label="绚丽流光" value="gradient-gorgeous" />
            <el-option label="绚丽水波纹" value="ripple-gorgeous" />
            <el-option label="绚丽脉冲" value="pulse-gorgeous" />
            <el-option label="无特效" value="none" />
          </el-select>
        </div>

        <div class="drawer-item">
          <span>菜单点击音效</span>
          <el-switch v-model="menuSoundEnabled" class="drawer-switch" />
        </div>

        <div class="drawer-item">
          <span>底部版权</span>
          <el-switch v-model="footerVisible" class="drawer-switch" />
        </div>

        <el-divider/>

        <div class="drawer-footer-buttons">
          <el-button size="small" type="primary" :icon="resolveIcon('el-icon-document-add')" @click="saveSetting">保存配置</el-button>
          <el-button size="small" :icon="resolveIcon('el-icon-refresh')" @click="resetSetting">重置配置</el-button>
        </div>
      </div>
    </div>
  </el-drawer>
</template>

<script>
/**
 * 布局与主题设置面板；只修改 Vuex 设置并按需保存到本地，主题类名和背景变量由 App 统一同步。
 */
import ThemePicker from '@/components/ThemePicker'
import { getBackgroundOptions } from '@/assets/styles/themes/glassmorphism-backgrounds'

export default {
  components: { ThemePicker },
  expose: ['openSetting'],
  data() {
    return {
      theme: this.$store.state.settings.theme,
      sideTheme: this.$store.state.settings.sideTheme,
      showSettings: false,
      backgroundOptions: getBackgroundOptions()
    }
  },
  computed: {
    fixedHeader: {
      get() {
        return this.$store.state.settings.fixedHeader
      },
      set(val) {
        this.$store.dispatch('settings/changeSetting', {
          key: 'fixedHeader',
          value: val
        })
      }
    },
    topNav: {
      get() {
        return this.$store.state.settings.topNav
      },
      set(val) {
        this.$store.dispatch('settings/changeSetting', {
          key: 'topNav',
          value: val
        })
        if (!val) {
          this.$store.dispatch('app/toggleSideBarHide', false)
          this.$store.commit("SET_SIDEBAR_ROUTERS", this.$store.state.permission.defaultRoutes)
        }
      }
    },
    tagsView: {
      get() {
        return this.$store.state.settings.tagsView
      },
      set(val) {
        this.$store.dispatch('settings/changeSetting', {
          key: 'tagsView',
          value: val
        })
      }
    },
    tagsIcon: {
      get() {
        return this.$store.state.settings.tagsIcon
      },
      set(val) {
        this.$store.dispatch('settings/changeSetting', {
          key: 'tagsIcon',
          value: val
        })
      }
    },
    sidebarLogo: {
      get() {
        return this.$store.state.settings.sidebarLogo
      },
      set(val) {
        this.$store.dispatch('settings/changeSetting', {
          key: 'sidebarLogo',
          value: val
        })
      }
    },
    dynamicTitle: {
      get() {
        return this.$store.state.settings.dynamicTitle
      },
      set(val) {
        this.$store.dispatch('settings/changeSetting', {
          key: 'dynamicTitle',
          value: val
        })
        this.$store.dispatch('settings/setTitle', this.$store.state.settings.title)
      }
    },
    footerVisible: {
      get() {
        return this.$store.state.settings.footerVisible
      },
      set(val) {
        this.$store.dispatch('settings/changeSetting', {
          key: 'footerVisible',
          value: val
        })
      }
    },
    menuClickEffect: {
      get() {
        return this.$store.state.settings.menuClickEffect
      },
      set(val) {
        this.$store.dispatch('settings/changeSetting', {
          key: 'menuClickEffect',
          value: val
        })
      }
    },
    menuSoundEnabled: {
      get() {
        return this.$store.state.settings.menuSoundEnabled
      },
      set(val) {
        this.$store.dispatch('settings/changeSetting', {
          key: 'menuSoundEnabled',
          value: val
        })
      }
    },
    themeStyle: {
      get() {
        return this.$store.state.settings.themeStyle || 'default'
      },
      set(val) {
        this.$store.dispatch('settings/changeSetting', {
          key: 'themeStyle',
          value: val
        })
      }
    },
    glassmorphismBgStyle: {
      get() {
        return this.$store.state.settings.glassmorphismBgStyle || 'ocean-sunset'
      },
      set(val) {
        this.$store.dispatch('settings/changeSetting', {
          key: 'glassmorphismBgStyle',
          value: val
        })
      }
    }
  },
  mounted() {
    document.addEventListener('pointerdown', this.handleClickOutside, true)
  },
  beforeUnmount() {
    document.removeEventListener('pointerdown', this.handleClickOutside, true)
  },
  methods: {
    themeChange(val) {
      this.$store.dispatch('settings/changeSetting', {
        key: 'theme',
        value: val
      })
      this.theme = val
    },
    handleTheme(val) {
      this.$store.dispatch('settings/changeSetting', {
        key: 'sideTheme',
        value: val
      })
      this.sideTheme = val
    },
    openSetting() {
      this.showSettings = true
    },
    closeSetting(done) {
      this.showSettings = false
      if (typeof done === 'function') done()
    },
    handleClickOutside(event) {
      if (!this.showSettings || !(event.target instanceof Element)) return
      const drawer = this.$refs.settingsContent?.closest('.el-drawer')
      // 设置下拉层挂在 body 下，选择主题或颜色时仍属于设置栏内部操作。
      if (drawer?.contains(event.target) || event.target.closest('.zero-settings-popper, .navbar .setting')) return
      this.closeSetting()
    },
    saveSetting() {
      this.$modal.loading("正在保存到本地，请稍候...")
      try {
        this.$cache.local.setJSON("layout-setting", {
          topNav: this.topNav,
          tagsView: this.tagsView,
          tagsIcon: this.tagsIcon,
          fixedHeader: this.fixedHeader,
          sidebarLogo: this.sidebarLogo,
          dynamicTitle: this.dynamicTitle,
          footerVisible: this.footerVisible,
          sideTheme: this.sideTheme,
          theme: this.theme,
          menuClickEffect: this.menuClickEffect,
          menuSoundEnabled: this.menuSoundEnabled,
          themeStyle: this.themeStyle,
          glassmorphismBgStyle: this.glassmorphismBgStyle
        })
        this.closeSetting()
      } catch {
        this.$modal.msgError("配置保存失败，请检查浏览器本地存储是否可用")
      } finally {
        this.$modal.closeLoading()
      }
    },
    resetSetting() {
      this.$modal.loading("正在清除设置缓存并刷新，请稍候...")
      this.$cache.local.remove("layout-setting")
      setTimeout("window.location.reload()", 1000)
    }
  }
}
</script>

<style lang="scss" scoped>
// 组件局部布局与展示样式；跨页面主题及 body 弹层规则在 assets/styles 中统一维护。
  .setting-drawer-content {
    .setting-drawer-title {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 12px;
      color: rgba(0, 0, 0, .85);
      font-size: 14px;
      line-height: 22px;
      font-weight: bold;

      .drawer-title { margin: 0; }
      .settings-close {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        width: 28px;
        height: 28px;
        padding: 0;
        border: 0;
        background: transparent;
        color: inherit;
        cursor: pointer;
        font-size: 18px;
      }
    }

    .setting-drawer-block-checbox {
      display: flex;
      justify-content: flex-start;
      align-items: center;
      margin-top: 10px;
      margin-bottom: 20px;

      .setting-drawer-block-checbox-item {
        position: relative;
        margin-right: 16px;
        border-radius: 2px;
        cursor: pointer;

        img {
          width: 48px;
          height: 48px;
        }

        .setting-drawer-block-checbox-selectIcon {
          position: absolute;
          top: 0;
          right: 0;
          width: 100%;
          height: 100%;
          padding-top: 15px;
          padding-left: 24px;
          color: #1890ff;
          font-weight: 700;
          font-size: 14px;
        }
      }
    }
  }

  .drawer-container {
    padding: 20px;
    font-size: 14px;
    line-height: 1.5;
    word-wrap: break-word;

    .drawer-title {
      margin-bottom: 12px;
      color: rgba(0, 0, 0, .85);
      font-size: 14px;
      line-height: 22px;
    }

    .drawer-item {
      color: rgba(0, 0, 0, .65);
      font-size: 14px;
      padding: 12px 0;
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .drawer-switch {
      flex-shrink: 0;
    }
    
    .drawer-select {
      width: 110px;
      flex-shrink: 0;
      font-size: 12px;
      
      :deep(.el-input__inner) {
        font-size: 12px;
        padding-left: 8px;
        padding-right: 24px;
      }
      
      :deep(.el-input__icon) {
        width: 20px;
      }
    }

    .drawer-select-wide {
      width: 150px;
      flex-shrink: 0;
      font-size: 12px;
      
      :deep(.el-input__inner) {
        font-size: 12px;
        padding-left: 8px;
        padding-right: 24px;
      }
      
      :deep(.el-input__icon) {
        width: 20px;
      }
    }
    
    .drawer-footer-buttons {
      display: flex;
      gap: 10px;
      margin-top: 20px;
      
      .el-button {
        flex: 1;
        font-weight: 600;
      }
    }
  }
</style>
