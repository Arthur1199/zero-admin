<template>
  <div id="tags-view-container" class="tags-view-container">
    <scroll-pane ref="scrollPane" class="tags-view-wrapper" @scroll="handleScroll">
      <router-link
        v-for="tag in visitedViews"
        ref="tag"
        :key="tag.path"
        :class="{ 'active': isActive(tag), 'has-icon': tagsIcon }"
        :to="{ path: tag.path, query: tag.query, fullPath: tag.fullPath }"
        tag="span"
        class="tags-view-item"
        :style="activeStyle(tag)"
@click.middle="!isAffix(tag)?closeSelectedTag(tag):''"
@click="handleTabClick(tag)"
@contextmenu.prevent="openMenu(tag,$event)"
@dblclick="locateMenuByTag(tag, $event)"
      >
        <svg-icon v-if="tagsIcon && tag.meta && tag.meta.icon && tag.meta.icon !== '#'" :icon-class="tag.meta.icon" />
        {{ tag.title }}
        <AppIcon v-if="!isAffix(tag)" icon-class="el-icon-close" class="" @click.prevent.stop="closeSelectedTag(tag)" />
      </router-link>
    </scroll-pane>
  </div>
</template>

<script>
import ScrollPane from './ScrollPane'
import path from '@/utils/path'
import { playSound, SoundType } from '@/utils/audio'

export default {
  components: { ScrollPane },
  data() {
    return {
      visible: false,
      top: 0,
      left: 0,
      selectedTag: {},
      affixTags: [],
      isClosingTab: false,  // 标志位：是否正在关闭Tab
      isTabClick: false     // 标志位：是否由Tab点击触发的路由变化
    }
  },
  inject: ['getSidebar'],
  computed: {
    visitedViews() {
      return this.$store.state.tagsView.visitedViews
    },
    routes() {
      return this.$store.state.permission.routes
    },
    theme() {
      return this.$store.state.settings.theme
    },
    tagsIcon() {
      return this.$store.state.settings.tagsIcon
    }
  },
  watch: {
    $route() {
      this.addTags()
      this.moveToCurrentTag()

      // 注意：音效已经在 handleTabClick 中播放，这里不需要重复播放
      // 只需要重置标志位

      // 重置标志位
      this.isClosingTab = false
      this.isTabClick = false
    },
    visible(value) {
      if (value) {
        document.body.addEventListener('click', this.closeMenu)
      } else {
        document.body.removeEventListener('click', this.closeMenu)
      }
    }
  },
  mounted() {
    this.initTags()
    this.addTags()
    // 确保从localStorage恢复的标签能够正确显示
    this.$nextTick(() => {
      this.moveToCurrentTag()
    })

    // 创建右键菜单并插入到body，避免被毛玻璃主题的backdrop-filter遮罩
    this.createContextMenu()
  },
  beforeUnmount() {
    // 组件销毁时移除菜单
    this.destroyContextMenu()
  },
  methods: {
    isActive(route) {
      return route.path === this.$route.path
    },
    activeStyle(tag) {
      if (!this.isActive(tag)) return {}
      return {
        "background-color": this.theme,
        "border-color": this.theme
      }
    },
    isAffix(tag) {
      return tag.meta && tag.meta.affix
    },
    isFirstView() {
      try {
        return this.selectedTag.fullPath === '/index' || this.selectedTag.fullPath === this.visitedViews[1].fullPath
      } catch (err) {
        return false
      }
    },
    isLastView() {
      try {
        return this.selectedTag.fullPath === this.visitedViews[this.visitedViews.length - 1].fullPath
      } catch (err) {
        return false
      }
    },
    filterAffixTags(routes, basePath = '/') {
      let tags = []
      routes.forEach(route => {
        if (route.meta && route.meta.affix) {
          const tagPath = path.resolve(basePath, route.path)
          tags.push({
            fullPath: tagPath,
            path: tagPath,
            name: route.name,
            meta: { ...route.meta }
          })
        }
        if (route.children) {
          const tempTags = this.filterAffixTags(route.children, route.path)
          if (tempTags.length >= 1) {
            tags = [...tags, ...tempTags]
          }
        }
      })
      return tags
    },
    initTags() {
      const affixTags = this.affixTags = this.filterAffixTags(this.routes)
      // 如果 visitedViews 为空(首次访问或localStorage被清空),才添加 affix 标签
      if (this.visitedViews.length === 0) {
        for (const tag of affixTags) {
          // Must have tag name
          if (tag.name) {
            this.$store.dispatch('tagsView/addVisitedView', tag)
          }
        }
      } else {
        // 如果 localStorage 中有数据,检查是否包含 affix 标签,没有则添加
        for (const tag of affixTags) {
          if (tag.name) {
            const exists = this.visitedViews.some(v => v.path === tag.path)
            if (!exists) {
              this.$store.dispatch('tagsView/addVisitedView', tag)
            }
          }
        }
      }
    },
    addTags() {
      const { name } = this.$route
      if (name) {
        this.$store.dispatch('tagsView/addView', this.$route)
      }
    },
    moveToCurrentTag() {
      const tags = this.$refs.tag
      this.$nextTick(() => {
        for (const tag of tags) {
          if (tag.to.path === this.$route.path) {
            this.$refs.scrollPane.moveToTarget(tag)
            // when query is different then update
            if (tag.to.fullPath !== this.$route.fullPath) {
              this.$store.dispatch('tagsView/updateVisitedView', this.$route)
            }
            break
          }
        }
      })
    },
    refreshSelectedTag(view) {
      this.$tab.refreshPage(view)
      if (this.$route.meta.link) {
        this.$store.dispatch('tagsView/delIframeView', this.$route)
      }
    },
    closeSelectedTag(view) {
      // 设置关闭Tab标志，防止播放切换音效
      this.isClosingTab = true

      // 播放Tab关闭音效
      playSound(SoundType.TAB_CLOSE, this.$store)

      this.$tab.closePage(view).then(({ visitedViews }) => {
        if (this.isActive(view)) {
          this.toLastView(visitedViews, view)
        }
      })
    },
    closeRightTags() {
      this.$tab.closeRightPage(this.selectedTag).then(visitedViews => {
        if (!visitedViews.find(i => i.fullPath === this.$route.fullPath)) {
          this.toLastView(visitedViews)
        }
      })
    },
    closeLeftTags() {
      this.$tab.closeLeftPage(this.selectedTag).then(visitedViews => {
        if (!visitedViews.find(i => i.fullPath === this.$route.fullPath)) {
          this.toLastView(visitedViews)
        }
      })
    },
    closeOthersTags() {
      this.$router.push(this.selectedTag.fullPath).catch(()=>{})
      this.$tab.closeOtherPage(this.selectedTag).then(() => {
        this.moveToCurrentTag()
      })
    },
    closeAllTags(view) {
      this.$tab.closeAllPage().then(({ visitedViews }) => {
        if (this.affixTags.some(tag => tag.path === this.$route.path)) {
          return
        }
        this.toLastView(visitedViews, view)
      })
    },
    toLastView(visitedViews, view) {
      const latestView = visitedViews.slice(-1)[0]
      if (latestView) {
        this.$router.push(latestView.fullPath)
      } else {
        // now the default is to redirect to the home page if there is no tags-view,
        // you can adjust it according to your needs.
        if (view.name === 'Dashboard') {
          // to reload home page
          this.$router.replace({ path: '/redirect' + view.fullPath })
        } else {
          this.$router.push('/')
        }
      }
    },
    openMenu(tag, e) {
      // 更新选中的tag
      this.selectedTag = tag

      // 更新菜单项（根据当前状态显示/隐藏某些选项）
      this.updateContextMenuItems()

      // 计算菜单位置（使用clientX/clientY相对于视口）
      this.left = e.clientX
      this.top = e.clientY

      // 显示菜单
      if (this.menuEl) {
        this.menuEl.style.left = this.left + 'px'
        this.menuEl.style.top = this.top + 'px'
        this.menuEl.style.display = 'block'
        this.visible = true
      }
    },
    closeMenu() {
      if (this.menuEl) {
        this.menuEl.style.display = 'none'
      }
      this.visible = false
    },
    handleScroll() {
      this.closeMenu()
    },
    /**
     * 处理Tab点击事件
     */
    handleTabClick(tag) {
      // 无论是否当前Tab，都播放音效（增强交互反馈）
      this.isTabClick = true

      // 使用nextTick确保在下一个事件循环中播放音效
      this.$nextTick(() => {
        playSound(SoundType.TAB_CHANGE, this.$store)
      })
    },
    /**
     * 双击Tab标签，联动左侧菜单定位
     * @param {Object} tag - 双击的标签对象
     */
    locateMenuByTag(tag) {
      // 添加双击视觉反馈效果
      const targetTag = event.currentTarget
      if (targetTag) {
        targetTag.classList.add('tag-dblclick-flash')
        setTimeout(() => {
          targetTag.classList.remove('tag-dblclick-flash')
        }, 600)
      }

      const sidebar = this.getSidebar()
      if (!sidebar) return
      let menu
      menu = sidebar.$refs.menuRef
      if (!menu) {
        return
      }

      // 检查菜单是否被折叠，如果折叠则先展开
      if (!this.$store.getters.sidebar.opened) {
        // 切换侧边栏展开/折叠状态
        this.$store.dispatch('app/toggleSideBar')

        // 等待菜单展开动画完成
        setTimeout(() => {
          this.doLocateMenu(sidebar, menu, tag)
        }, 350)
        return
      }

      // 直接执行定位
      this.doLocateMenu(sidebar, menu, tag)
    },

    /**
     * 执行菜单定位逻辑（基于menuId查找父级）
     */
    doLocateMenu(sidebar, menu, tag) {
      // 1. 从路由树中查找目标路由对象（通过path匹配）
      const targetRoute = this.findRouteByPath(tag.path)

      if (!targetRoute || !targetRoute.menuId) {
        return
      }

      // 2. 通过menuId和parentId查找所有需要展开的父级菜单
      const menusToOpen = this.findMenusToOpenByMenuId(targetRoute)

      if (menusToOpen.length === 0) {
        return
      }

      // 3. 逐级展开菜单
      this.expandMenusByMenuId(sidebar, menu, menusToOpen)
    },

    /**
     * 通过menuId和parentId查找所有需要展开的父级菜单
     */
    findMenusToOpenByMenuId(targetRoute) {
      if (!targetRoute || !targetRoute.parentId) {
        return []
      }

      const routes = this.$store.getters.sidebarRouters
      const menusToOpen = []

      // 递归查找menuId等于指定值的路由
      const findRouteByMenuId = (menuId) => {
        let foundRoute = null

        const traverse = (routes, currentPath = '') => {
          for (let route of routes) {
            const fullPath = currentPath ? `${currentPath}/${route.path}` : `/${route.path}`
            const normalizedPath = fullPath.replace(/\/+/g, '/')

            if (!route._fullPath) {
              route._fullPath = normalizedPath
            }

            if (route.menuId === menuId) {
              foundRoute = route
              return true
            }

            if (route.children && route.children.length > 0) {
              if (traverse(route.children, normalizedPath)) {
                return true
              }
            }
          }
          return false
        }

        traverse(routes)
        return foundRoute
      }

      // 从目标路由的parentId开始，逐级向上查找
      let currentParentId = targetRoute.parentId

      while (currentParentId && currentParentId !== 0) {
        const parentRoute = findRouteByMenuId(currentParentId)

        if (!parentRoute) {
          console.warn('[Tab双击] 未找到menuId为', currentParentId, '的父级路由')
          break
        }

        menusToOpen.unshift(parentRoute)
        currentParentId = parentRoute.parentId
      }

      return menusToOpen
    },

    /**
     * 逐级展开菜单（使用递归确保顺序展开）
     * 修复版本：支持无限层级的菜单展开，使用 menuId 而非路径
     */
    expandMenusByMenuId(sidebar, menu, menusToOpen) {
      if (menusToOpen.length === 0) {
        return
      }

      // console.log('[Tab双击] 准备展开的菜单:', menusToOpen.map(m => `menuId:${m.menuId}, path:${m._fullPath}`))

      // 递归展开，每次只处理一个
      const expandNext = (index) => {
        if (index >= menusToOpen.length) {
          // console.log('[Tab双击] 所有菜单展开完成')
          return
        }

        const menuRoute = menusToOpen[index]
        const menuId = menuRoute.menuId

        // console.log(`[Tab双击] 正在展开第 ${index + 1}/${menusToOpen.length} 级菜单 - menuId:`, menuId)

        try {
          // 调用 Sidebar 的 openMenuItem 方法展开当前菜单
          if (sidebar.openMenuItem) {
            sidebar.openMenuItem(menuId, menuRoute).then(didOpen => {
              // console.log(`[Tab双击] 菜单 menuId:${menuId} 展开结果:`, didOpen)

              // 无论是否成功，都继续展开下一级（300ms后）
              // 增加延迟确保DOM完全渲染
              setTimeout(() => {
                expandNext(index + 1)
              }, 300)
            }).catch(err => {
              // console.error('[Tab双击] 展开菜单失败:', err)
              // 出错也继续下一级
              setTimeout(() => {
                expandNext(index + 1)
              }, 300)
            })
          } else {
            // 降级方案：直接使用 Element UI 的 open 方法
            // console.log('[Tab双击] 使用降级方案展开菜单')
            const menuIndex = menuRoute._fullPath
            if (menu._originalOpen) {
              menu._originalOpen(menuIndex)
            } else {
              menu.open(menuIndex)
            }

            // 继续下一级
            setTimeout(() => {
              expandNext(index + 1)
            }, 300)
          }
        } catch (err) {
          // console.error('[Tab双击] 展开菜单异常:', err)
          // 出错也继续下一级
          setTimeout(() => {
            expandNext(index + 1)
          }, 300)
        }
      }

      // 从第一级开始展开
      expandNext(0)
    },

    /**
     * 通过path在路由树中查找目标路由对象
     */
    findRouteByPath(targetPath) {
      const routes = this.$store.getters.sidebarRouters
      let foundRoute = null

      const traverse = (routes, currentPath = '') => {
        for (let route of routes) {
          // 构建完整路径
          const fullPath = currentPath ? `${currentPath}/${route.path}` : `/${route.path}`
          const normalizedPath = fullPath.replace(/\/+/g, '/')

          // 给每个路由对象存储完整路径（用于菜单展开的index）
          if (!route._fullPath) {
            route._fullPath = normalizedPath
          }

          // 如果找到目标
          if (normalizedPath === targetPath) {
            foundRoute = route
            return true
          }

          // 递归查找子路由
          if (route.children && route.children.length > 0) {
            if (traverse(route.children, normalizedPath)) {
              return true
            }
          }
        }
        return false
      }

      traverse(routes)
      return foundRoute
    },

    /**
     * 创建右键菜单并插入到body
     */
    createContextMenu() {
      // 创建ul元素
      const menuEl = document.createElement('ul')
      menuEl.className = 'tags-view-contextmenu'
      menuEl.style.cssText = `
        margin: 0;
        background: #fff;
        z-index: 9999;
        position: fixed;
        list-style-type: none;
        padding: 5px 0;
        border-radius: 4px;
        font-size: 12px;
        font-weight: 400;
        color: #333;
        box-shadow: 2px 2px 3px 0 rgba(0, 0, 0, .3);
        display: none;
      `

      // 创建菜单项
      const menuItems = [
        { label: '刷新页面', icon: 'el-icon-refresh-right', action: 'refresh' },
        { label: '关闭当前', icon: 'el-icon-close', action: 'closeCurrent', showIf: () => !this.isAffix(this.selectedTag) },
        { label: '关闭其他', icon: 'el-icon-circle-close', action: 'closeOthers' },
        { label: '关闭左侧', icon: 'el-icon-back', action: 'closeLeft', showIf: () => !this.isFirstView() },
        { label: '关闭右侧', icon: 'el-icon-right', action: 'closeRight', showIf: () => !this.isLastView() },
        { label: '全部关闭', icon: 'el-icon-circle-close', action: 'closeAll' }
      ]

      this.menuItems = menuItems
      this.menuEl = menuEl

      // 插入到body
      document.body.appendChild(menuEl)
    },

    /**
     * 更新菜单项
     */
    updateContextMenuItems() {
      if (!this.menuEl) return

      // 清空菜单
      this.menuEl.innerHTML = ''

      // 添加菜单项
      this.menuItems.forEach(item => {
        // 检查是否显示
        if (item.showIf && !item.showIf()) {
          return
        }

        const li = document.createElement('li')
        li.style.cssText = `
          margin: 0;
          padding: 7px 16px;
          cursor: pointer;
        `
        li.innerHTML = `<i class="${item.icon}"></i> ${item.label}`

        // 添加悬停效果
        li.addEventListener('mouseenter', () => {
          li.style.background = '#eee'
        })
        li.addEventListener('mouseleave', () => {
          li.style.background = 'transparent'
        })

        // 添加点击事件
        li.addEventListener('click', () => {
          this.handleMenuAction(item.action)
        })

        this.menuEl.appendChild(li)
      })
    },

    /**
     * 处理菜单操作
     */
    handleMenuAction(action) {
      switch (action) {
        case 'refresh':
          this.refreshSelectedTag(this.selectedTag)
          break
        case 'closeCurrent':
          this.closeSelectedTag(this.selectedTag)
          break
        case 'closeOthers':
          this.closeOthersTags()
          break
        case 'closeLeft':
          this.closeLeftTags()
          break
        case 'closeRight':
          this.closeRightTags()
          break
        case 'closeAll':
          this.closeAllTags(this.selectedTag)
          break
      }
      this.closeMenu()
    },

    /**
     * 销毁右键菜单
     */
    destroyContextMenu() {
      if (this.menuEl && this.menuEl.parentNode) {
        this.menuEl.parentNode.removeChild(this.menuEl)
        this.menuEl = null
      }
    }

  }
}
</script>

<style lang="scss" scoped>
.tags-view-container {
  height: 34px;
  width: 100%;
  border-bottom: 1px solid #d8dce5;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, .12), 0 0 3px 0 rgba(0, 0, 0, .04);
  .tags-view-wrapper {
    .tags-view-item {
      display: inline-block;
      position: relative;
      cursor: pointer;
      height: 26px;
      line-height: 26px;
      border: 1px solid #d8dce5;
      color: #495060;
      background: #fff;
      padding: 0 8px;
      font-size: 12px;
      margin-left: 5px;
      margin-top: 4px;
      /* 禁止双击选中文字 */
      user-select: none;
      -webkit-user-select: none;
      -moz-user-select: none;
      -ms-user-select: none;
      &:first-of-type {
        margin-left: 15px;
      }
      &:last-of-type {
        margin-right: 15px;
      }
      &.active {
        background-color: #42b983;
        color: #fff;
        border-color: #42b983;
        &::before {
          content: '';
          background: #fff;
          display: inline-block;
          width: 8px;
          height: 8px;
          border-radius: 50%;
          position: relative;
          margin-right: 2px;
        }
      }
    }
  }

  .tags-view-item.active.has-icon::before {
    content: none !important;
  }
}
</style>

<style lang="scss">
//reset element css of el-icon-close
.tags-view-wrapper {
  .tags-view-item {
    .el-icon-close {
      width: 16px;
      height: 16px;
      vertical-align: 2px;
      border-radius: 50%;
      text-align: center;
      transition: all .3s cubic-bezier(.645, .045, .355, 1);
      transform-origin: 100% 50%;
      &:before {
        transform: scale(.6);
        display: inline-block;
        vertical-align: -3px;
      }
      &:hover {
        background-color: #b4bccc;
        color: #fff;
      }
    }
  }
}

/* Tab标签双击视觉反馈效果 */
.tags-view-wrapper {
  .tags-view-item.tag-dblclick-flash {
    animation: tag-dblclick-pulse 0.6s ease-out;
  }
}

/* Tab双击脉冲动画 */
@keyframes tag-dblclick-pulse {
  0% {
    transform: scale(1);
    box-shadow: 0 0 0 rgba(64, 158, 255, 0);
  }
  30% {
    transform: scale(1.05);
    box-shadow: 0 0 15px rgba(64, 158, 255, 0.5);
  }
  60% {
    transform: scale(0.98);
    box-shadow: 0 0 8px rgba(64, 158, 255, 0.3);
  }
  100% {
    transform: scale(1);
    box-shadow: 0 0 0 rgba(64, 158, 255, 0);
  }
}

/* ========================================
   Tab标签激活项抖动效果（全局默认）
   注意：各主题的火焰发光效果会覆盖此动画
   默认主题下，Tab标签拥有独立的抖动效果，与菜单不同步
======================================== */

/* 激活的Tab标签抖动（仅用于默认主题） */
body:not(.theme-glassmorphism):not(.theme-dark-premium):not(.theme-luxury-business):not(.theme-vibrant-tech):not(.theme-minimalist-future):not(.theme-warm-nature):not(.theme-material-design) {
  .tags-view-wrapper .tags-view-item.active {
    animation: tab-shake-independent 3.5s ease-in-out infinite !important;
  }
}

/* Tab标签独立抖动动画（与菜单不同步） */
@keyframes tab-shake-independent {
  0% {
    transform: translateX(0);
  }
  1% {
    transform: translateX(1.5px);
  }
  2% {
    transform: translateX(3px);
  }
  3% {
    transform: translateX(1.5px);
  }
  8% {
    transform: translateX(0);
  }
  5% {
    transform: translateX(-1.5px);
  }
  6% {
    transform: translateX(-3px);
  }
  7% {
    transform: translateX(-1.5px);
  }
  8% {
    transform: translateX(0);
  }
  95% {
    transform: translateX(0);
  }
  100% {
    transform: translateX(0);
  }
}


</style>
