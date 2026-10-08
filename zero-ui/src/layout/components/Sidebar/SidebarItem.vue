<template>
  <div v-if="!item.hidden">
    <template v-if="hasOneShowingChild(item.children,item) && (!onlyOneChild.children||onlyOneChild.noShowingChildren)&&!item.alwaysShow">
      <app-link v-if="onlyOneChild.meta" :to="resolvePath(onlyOneChild.path, onlyOneChild.query)">
        <el-menu-item 
          :index="resolvePathString(onlyOneChild.path)" 
          :class="{'submenu-title-noDropdown':!isNest}"
          @click.native="handleMenuClick">
          <item :icon="onlyOneChild.meta.icon||(item.meta&&item.meta.icon)" :title="onlyOneChild.meta.title" />
        </el-menu-item>
      </app-link>
    </template>

    <el-submenu 
      v-else 
      ref="subMenu" 
      :index="resolvePathString(item.path)" 
      :data-menu-id="item.menuId"
      popper-append-to-body>
      <template slot="title">
        <item 
          v-if="item.meta" 
          :icon="item.meta && item.meta.icon" 
          :title="item.meta.title" 
          @click.native="handleSubmenuClick" />
      </template>
      <sidebar-item
        v-for="(child, index) in item.children"
        :key="child.path + index"
        :is-nest="true"
        :item="child"
        :base-path="resolvePath(child.path)"
        class="nest-menu"
      />
    </el-submenu>
  </div>
</template>

<script>
import path from 'path'
import { isExternal } from '@/utils/validate'
import Item from './Item'
import AppLink from './Link'
import FixiOSBug from './FixiOSBug'
import { playSound, SoundType } from '@/utils/audio'

export default {
  name: 'SidebarItem',
  components: { Item, AppLink },
  mixins: [FixiOSBug],
  props: {
    // route object
    item: {
      type: Object,
      required: true
    },
    isNest: {
      type: Boolean,
      default: false
    },
    basePath: {
      type: String,
      default: ''
    }
  },
  data() {
    this.onlyOneChild = null
    return {
      clickEffectTimer: null
    }
  },
  computed: {
    menuClickEffect() {
      return this.$store.state.settings.menuClickEffect || 'ripple'
    }
  },
  methods: {
    hasOneShowingChild(children = [], parent) {
      if (!children) {
        children = []
      }
      const showingChildren = children.filter(item => {
        if (item.hidden) {
          return false
        }
        // Temp set(will be used if only has one showing child)
        this.onlyOneChild = item
        return true
      })

      // When there is only one child router, the child router is displayed by default
      if (showingChildren.length === 1) {
        return true
      }

      // Show parent if there are no child router to display
      if (showingChildren.length === 0) {
        this.onlyOneChild = { ... parent, path: '', noShowingChildren: true }
        return true
      }

      return false
    },
    resolvePath(routePath, routeQuery) {
      if (isExternal(routePath)) {
        return routePath
      }
      if (isExternal(this.basePath)) {
        return this.basePath
      }
      if (routeQuery) {
        let query = JSON.parse(routeQuery)
        return { path: path.resolve(this.basePath, routePath), query: query }
      }
      return path.resolve(this.basePath, routePath)
    },
    
    /**
     * 解析路径（仅返回字符串路径，用于index属性）
     */
    resolvePathString(routePath) {
      if (isExternal(routePath)) {
        return routePath
      }
      if (isExternal(this.basePath)) {
        return this.basePath
      }
      return path.resolve(this.basePath, routePath)
    },

    /**
     * 处理菜单项点击事件，触发点击特效
     */
    handleMenuClick(event) {
      // 播放子菜单点击音效
      playSound(SoundType.MENU_CLICK, this.$store)
      
      if (this.menuClickEffect === 'none') return
      
      const target = event.currentTarget
      this.triggerClickEffect(target)
    },

    /**
     * 处理子菜单标题点击事件
     */
    handleSubmenuClick(event) {
      // 检查是否点击的是父菜单标题，而非子项
      // 如果点击的是子项，则不播放父菜单音效，避免音效重叠
      const isClickingSubmenuTitle = event.target.closest('.el-submenu__title') && 
                                    !event.target.closest('.nest-menu')
      
      if (!isClickingSubmenuTitle) {
        // 点击的是子项，不触发特效，也不播放音效
        return
      }
      
      // 注意：这里不播放音效，因为音效已经在 Sidebar/index.vue 的 handleMenuOpen/handleMenuClose 中处理
      // 如果在这里也播放，会导致音效重复
      
      if (this.menuClickEffect === 'none') return
      
      // 查找父级 el-submenu__title 元素
      let target = event.target
      while (target && !target.classList.contains('el-submenu__title')) {
        target = target.parentElement
      }
      
      if (target) {
        this.triggerClickEffect(target)
      }
    },

    /**
     * 触发点击特效动画
     */
    triggerClickEffect(element) {
      if (!element || this.menuClickEffect === 'none') return

      // 清除之前的动画
      element.classList.remove('menu-clicking')
      
      // 强制重绘以重启动画
      void element.offsetWidth
      
      // 添加点击样式
      element.classList.add('menu-clicking')
      
      // 清除之前的定时器
      if (this.clickEffectTimer) {
        clearTimeout(this.clickEffectTimer)
      }
      
      // 根据绚丽版特效设置动画时间
      let duration = 800 // 绚丽水波纹和绚丽脉冲默认 800ms
      if (this.menuClickEffect === 'gradient-gorgeous') {
        duration = 1000 // 绚丽流光 1000ms
      }
      
      this.clickEffectTimer = setTimeout(() => {
        element.classList.remove('menu-clicking')
      }, duration)
    }
  },

  beforeDestroy() {
    // 组件销毁时清理定时器
    if (this.clickEffectTimer) {
      clearTimeout(this.clickEffectTimer)
    }
  }
}
</script>
