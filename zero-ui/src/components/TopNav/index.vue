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
@import "@/assets/styles/layout/top-nav.scss";
</style>
