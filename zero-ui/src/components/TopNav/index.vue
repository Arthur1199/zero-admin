<template>
  <div ref="container" class="top-nav">
    <el-menu
      id="topmenu-container"
      ref="menu"
      class="topmenu-container"
      :style="{ '--theme': theme }"
      :default-active="activeMenu"
      :ellipsis="false"
      mode="horizontal"
      @select="handleSelect"
    >
      <el-menu-item v-for="item in visibleMenus" :key="item.path" :index="item.path">
        <svg-icon v-if="item.meta.icon && item.meta.icon !== '#'" :icon-class="item.meta.icon" />
        {{ item.meta.title }}
      </el-menu-item>

      <el-sub-menu v-if="overflowMenus.length" class="top-nav-more" index="top-nav-more" popper-class="top-nav-overflow-popper">
        <template #title>更多</template>
        <el-menu-item v-for="item in overflowMenus" :key="item.path" :index="item.path">
          <svg-icon v-if="item.meta.icon && item.meta.icon !== '#'" :icon-class="item.meta.icon" />
          {{ item.meta.title }}
        </el-menu-item>
      </el-sub-menu>
    </el-menu>

    <ul ref="measure" class="topmenu-container top-nav-measure el-menu el-menu--horizontal" aria-hidden="true">
      <li v-for="item in topMenus" :key="item.path" class="el-menu-item" :class="{ 'is-active': item.path === activeMenu }">
        <svg-icon v-if="item.meta.icon && item.meta.icon !== '#'" :icon-class="item.meta.icon" />
        {{ item.meta.title }}
      </li>
      <li class="el-sub-menu top-nav-more">
        <div class="el-sub-menu__title">更多<el-icon class="el-sub-menu__icon-arrow"><ArrowDown /></el-icon></div>
      </li>
    </ul>
  </div>
</template>

<script>
import { ArrowDown } from '@element-plus/icons-vue'
import { resolvePath } from '@/utils/path'
import { isExternal } from '@/utils/validate'

export default {
  components: { ArrowDown },
  data() {
    return {
      visibleNumber: Infinity,
      activeMenu: ''
    }
  },
  computed: {
    theme() {
      return this.$store.state.settings.theme
    },
    topMenus() {
      return this.$store.state.permission.topbarRouters
        .filter(menu => !menu.hidden)
        .map(menu => {
          const child = menu.children?.find(item => !item.hidden)
          // 顶级单页和内链使用根布局包装，顶部直接展示子页面。
          if (menu.path === '/' && child) {
            return { ...child, path: isExternal(child.path) ? child.path : resolvePath(menu.path, child.path) }
          }
          return menu
        })
        .filter(menu => menu.meta && (!menu.children || menu.children.some(child => !child.hidden)))
    },
    visibleMenus() {
      return this.topMenus.slice(0, this.visibleNumber)
    },
    overflowMenus() {
      return this.topMenus.slice(this.visibleNumber)
    }
  },
  watch: {
    '$route.path': {
      immediate: true,
      handler: 'syncRoute'
    },
    topMenus() {
      this.syncRoute()
      this.$nextTick(this.updateVisibleMenus)
    },
    activeMenu() {
      this.$nextTick(this.updateVisibleMenus)
    }
  },
  mounted() {
    this.resizeObserver = new ResizeObserver(this.updateVisibleMenus)
    this.resizeObserver.observe(this.$refs.container)
    this.resizeObserver.observe(this.$refs.measure)
    this.updateVisibleMenus()
  },
  beforeUnmount() {
    this.resizeObserver?.disconnect()
  },
  methods: {
    updateVisibleMenus() {
      const { container, measure } = this.$refs
      if (!container || !measure) return
      const widths = Array.from(measure.children, item => {
        const style = getComputedStyle(item)
        return item.getBoundingClientRect().width + parseFloat(style.marginLeft) + parseFloat(style.marginRight)
      })
      const moreWidth = widths.pop()
      const available = container.clientWidth
      if (widths.reduce((total, width) => total + width, 0) <= available) {
        this.visibleNumber = widths.length
        return
      }
      let used = moreWidth
      const firstOverflow = widths.findIndex(width => {
        used += width
        return used > available
      })
      this.visibleNumber = firstOverflow < 0 ? widths.length : firstOverflow
    },
    syncRoute() {
      const path = this.$route.meta.activeMenu || this.$route.path
      const menu = this.topMenus.reduce((selected, item) => {
        if (path === item.path || (item.children?.length && path.startsWith(item.path + '/'))) {
          return !selected || item.path.length > selected.path.length ? item : selected
        }
        return selected
      }, null)
      this.selectMenu(menu)
    },
    selectMenu(menu) {
      this.activeMenu = menu?.path || ''
      const children = menu?.children?.filter(child => !child.hidden) || []
      const routes = children.map(child => ({
        ...child,
        path: isExternal(child.path) ? child.path : resolvePath(menu.path, child.path)
      }))
      this.$store.commit('SET_SIDEBAR_ROUTERS', routes)
      this.$store.dispatch('app/toggleSideBarHide', routes.length === 0)
    },
    async handleSelect(key) {
      const menu = this.topMenus.find(item => item.path === key)
      if (!menu) return
      if (isExternal(key)) {
        window.open(key, '_blank', 'noopener,noreferrer')
        this.$refs.menu?.updateActiveIndex(this.activeMenu)
      } else if (menu.children?.length) {
        this.selectMenu(menu)
      } else {
        const query = menu.query ? JSON.parse(menu.query) : undefined
        await this.$router.push({ path: key, query })
        this.syncRoute()
      }
    }
  }
}
</script>

<style lang="scss">
.top-nav {
  position: relative;

  > #topmenu-container {
    height: 100%;
    border-bottom: 0;
  }
}

// 使用与实际菜单相同的字体和间距测量，不参与导航布局或交互。
.top-nav-measure.el-menu--horizontal {
  position: absolute;
  top: 0;
  left: 0;
  width: max-content;
  visibility: hidden;
  // 子菜单箭头有全局可见性覆盖，整组透明才能保留测量且不露出图标。
  opacity: 0;
  pointer-events: none;
}

.topmenu-container.el-menu--horizontal > .el-menu-item {
  float: left;
  height: 50px !important;
  line-height: 50px !important;
  color: rgba(255, 255, 255, 0.8) !important;  /* 默认白色半透明 */
  padding: 0 5px !important;
  margin: 0 10px !important;
  font-weight: 500 !important;
  transition: all 0.3s ease;
}

.topmenu-container.el-menu--horizontal > .el-menu-item.is-active {
  border-bottom: 4px solid #{'var(--theme)'} !important;
  color: rgba(255, 255, 255, 1) !important;  /* 激活时全白 */
  font-weight: 600 !important;
}

.topmenu-container.el-menu--horizontal > .el-sub-menu.is-active .el-sub-menu__title {
  border-bottom: 4px solid #{'var(--theme)'} !important;
  color: rgba(255, 255, 255, 1) !important;  /* 激活时全白 */
  font-weight: 600 !important;
}

/* submenu item */
.topmenu-container.el-menu--horizontal > .el-sub-menu .el-sub-menu__title {
  float: left;
  height: 50px !important;
  line-height: 50px !important;
  color: rgba(255, 255, 255, 0.8) !important;  /* 默认白色半透明 */
  padding: 0 5px !important;
  margin: 0 10px !important;
  font-weight: 500 !important;
  transition: all 0.3s ease;
}

.topmenu-container.el-menu--horizontal > .top-nav-more .el-sub-menu__title {
  font-weight: 600 !important;
}

/* hover状态 */
.topmenu-container.el-menu--horizontal > .el-menu-item:hover,
.topmenu-container.el-menu--horizontal > .el-sub-menu .el-sub-menu__title:hover {
  color: rgba(255, 255, 255, 1) !important;
}

/* 移除自定义主题下的顶部菜单栏白色背景 */
body[class*="theme-"] .topmenu-container.el-menu--horizontal {
  background-color: transparent !important;
}

/* 移除自定义主题下的激活菜单项白色背景 */
body[class*="theme-"] .topmenu-container.el-menu--horizontal > .el-menu-item.is-active {
  background-color: transparent !important;
}

body[class*="theme-"] .topmenu-container.el-menu--horizontal > .el-sub-menu.is-active .el-sub-menu__title {
  background-color: transparent !important;
}

/* 外链打开新标签页后，原菜单可能保留焦点。 */
body[class*="theme-"] .topmenu-container.el-menu--horizontal > .el-menu-item:is(:hover, :focus),
body[class*="theme-"] .top-nav-overflow-popper .el-menu-item:focus:not(:hover, .is-active) {
  background-color: transparent !important;
}

body[class*="theme-"] .topmenu-container.el-menu--horizontal > .el-menu-item:focus-visible,
body[class*="theme-"] .top-nav-overflow-popper .el-menu-item:focus-visible {
  outline: 2px solid currentColor;
  outline-offset: -3px;
}

body[class*="theme-"] .el-menu--horizontal > .el-sub-menu .el-sub-menu__title:hover {
  background-color: transparent !important;
}
</style>
