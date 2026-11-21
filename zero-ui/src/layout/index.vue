<template>
  <div :class="[classObj, themeClass]" class="app-wrapper" :style="layoutStyle">
    <div v-if="device==='mobile'&&sidebar.opened" class="drawer-bg" @click="handleClickOutside"/>
    <sidebar ref="sidebar" v-if="!sidebar.hide" class="sidebar-container"/>
    <div
      v-if="showSidebarResizer"
      class="sidebar-resizer"
      title="拖拽调整菜单宽度，双击恢复默认"
      @mousedown="startResizeSidebar"
      @dblclick="resetSidebarWidth"
    >
      <span></span>
    </div>
    <div :class="{hasTagsView:needTagsView,sidebarHide:sidebar.hide}" class="main-container">
      <div :class="{'fixed-header':fixedHeader}">
        <navbar @setLayout="setLayout"/>
        <tags-view v-if="needTagsView"/>
      </div>
      <app-main/>
      <settings ref="settingRef"/>
    </div>
  </div>
</template>

<script>
import { AppMain, Navbar, Settings, Sidebar, TagsView } from './components'
import ResizeMixin from './mixin/ResizeHandler'
import { mapState } from 'vuex'
import variables from '@/assets/styles/variables.module.scss'

export default {
  name: 'Layout',
  components: {
    AppMain,
    Navbar,
    Settings,
    Sidebar,
    TagsView
  },
  mixins: [ResizeMixin],
  provide() { return { getSidebar: () => this.$refs.sidebar } },
  data() {
    return {
      resizingSidebar: false,
      resizeStartX: 0,
      resizeStartWidth: 225
    }
  },
  computed: {
    ...mapState({
      theme: state => state.settings.theme,
      sideTheme: state => state.settings.sideTheme,
      sidebar: state => state.app.sidebar,
      device: state => state.app.device,
      needTagsView: state => state.settings.tagsView,
      fixedHeader: state => state.settings.fixedHeader,
      themeStyle: state => state.settings.themeStyle
    }),
    classObj() {
      return {
        hideSidebar: !this.sidebar.opened,
        openSidebar: this.sidebar.opened,
        withoutAnimation: this.sidebar.withoutAnimation,
        mobile: this.device === 'mobile',
        resizingSidebar: this.resizingSidebar
      }
    },
    themeClass() {
      // 当 themeStyle 不为 default 时，返回对应的主题类名
      if (this.themeStyle && this.themeStyle !== 'default') {
        return `theme-${this.themeStyle}`
      }
      return ''
    },
    variables() {
      return variables
    },
    sidebarWidth() {
      const width = Number(this.sidebar.width) || 225
      return Math.min(360, Math.max(225, width))
    },
    layoutStyle() {
      return {
        '--current-color': this.theme,
        '--sidebar-width': `${this.sidebarWidth}px`
      }
    },
    showSidebarResizer() {
      return this.device !== 'mobile' && !this.sidebar.hide && this.sidebar.opened
    }
  },
  beforeUnmount() {
    this.stopResizeSidebar()
  },
  methods: {
    handleClickOutside() {
      this.$store.dispatch('app/closeSideBar', { withoutAnimation: false })
    },
    setLayout() {
      this.$refs.settingRef.openSetting()
    },
    startResizeSidebar(e) {
      if (e.button !== 0) {
        return
      }
      e.preventDefault()
      this.resizingSidebar = true
      this.resizeStartX = e.clientX
      this.resizeStartWidth = this.sidebarWidth
      document.body.style.cursor = 'col-resize'
      document.body.style.userSelect = 'none'
      window.addEventListener('mousemove', this.handleResizeSidebar)
      window.addEventListener('mouseup', this.stopResizeSidebar)
    },
    handleResizeSidebar(e) {
      if (!this.resizingSidebar) {
        return
      }
      const width = this.resizeStartWidth + e.clientX - this.resizeStartX
      this.$store.dispatch('app/setSidebarWidth', width)
    },
    stopResizeSidebar() {
      if (!this.resizingSidebar) {
        return
      }
      this.resizingSidebar = false
      document.body.style.cursor = ''
      document.body.style.userSelect = ''
      window.removeEventListener('mousemove', this.handleResizeSidebar)
      window.removeEventListener('mouseup', this.stopResizeSidebar)
    },
    resetSidebarWidth() {
      this.$store.dispatch('app/setSidebarWidth', 225)
    }
  }
}
</script>

<style lang="scss" scoped>
  @import "@/assets/styles/mixin.scss";
  @import "@/assets/styles/variables.scss";

  .app-wrapper {
    @include clearfix;
    position: relative;
    height: 100%;
    width: 100%;

    &.mobile.openSidebar {
      position: fixed;
      top: 0;
    }
  }

  .drawer-bg {
    background: #000;
    opacity: 0.3;
    width: 100%;
    top: 0;
    height: 100%;
    position: absolute;
    z-index: 999;
  }

  .fixed-header {
    position: fixed;
    top: 0;
    right: 0;
    z-index: 9;
    width: calc(100% - var(--sidebar-width, #{$base-sidebar-width}));
    transition: width 0.28s;
  }

  .sidebar-resizer {
    position: fixed;
    top: 0;
    bottom: 0;
    left: calc(var(--sidebar-width, #{$base-sidebar-width}) - 4px);
    z-index: 1002;
    width: 8px;
    cursor: col-resize;
    display: flex;
    align-items: center;
    justify-content: center;

    span {
      width: 2px;
      height: 48px;
      border-radius: 2px;
      background: rgba(255, 255, 255, 0.22);
      box-shadow: 0 0 10px rgba(255, 255, 255, 0.16);
      opacity: 0;
      transition: opacity 0.2s, background-color 0.2s;
    }

    &:hover span {
      opacity: 1;
      background: rgba(255, 255, 255, 0.58);
    }
  }

  .resizingSidebar {
    .fixed-header,
    .main-container,
    .sidebar-container {
      transition: none !important;
    }

    .sidebar-resizer span {
      opacity: 1;
      background: rgba(255, 255, 255, 0.72);
    }
  }

  .hideSidebar .fixed-header {
    width: calc(100% - 54px);
  }

  .sidebarHide .fixed-header {
    width: 100%;
  }

  .mobile .fixed-header {
    width: 100%;
  }
</style>
