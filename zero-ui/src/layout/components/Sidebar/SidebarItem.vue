<template>
  <div v-if="!item.hidden">
    <template v-if="hasOneShowingChild(item.children,item) && (!onlyOneChild.children||onlyOneChild.noShowingChildren)&&!item.alwaysShow">
      <app-link v-if="onlyOneChild.meta" :to="resolvePath(onlyOneChild.path, onlyOneChild.query)" @click="handleMenuClick">
        <el-menu-item 
          :index="resolvePathString(onlyOneChild.path)" 
          :class="{'submenu-title-noDropdown':!isNest}">
          <item :icon="onlyOneChild.meta.icon||(item.meta&&item.meta.icon)" />
          <template #title><item :title="onlyOneChild.meta.title" /></template>
        </el-menu-item>
      </app-link>
    </template>

    <el-sub-menu
      v-else 
      ref="subMenu" 
      :index="resolvePathString(item.path)" 
      :class="{ 'sidebar-current-branch': isCurrentBranch }"
      :data-menu-id="item.menuId"
      teleported @click="handleSubmenuClick">
      <template #title>
        <item 
          v-if="item.meta" 
          :icon="item.meta && item.meta.icon" 
          :title="item.meta.title" />
      </template>
      <sidebar-item
        v-for="(child, index) in item.children"
        :key="child.path + index"
        :is-nest="true"
        :item="child"
        :base-path="resolvePath(child.path)"
        class="nest-menu"
      />
    </el-sub-menu>
  </div>
</template>

<script>
import path from '@/utils/path'
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
      clickEffectTimer: null,
      clickEffectElement: null
    }
  },
  computed: {
    isCurrentBranch() {
      const activePath = this.$route.meta.activeMenu || this.$route.path
      return this.isNest && activePath.startsWith(this.basePath + '/')
    },
    menuClickEffect() {
      return this.$store.state.settings.menuClickEffect || 'gradient-gorgeous'
    }
  },
  watch: {
    menuClickEffect() { this.clearClickEffect() }
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
      
      // Plus 的菜单 click 回调传入菜单对象，通过链接原生事件取得真实菜单元素。
      const target = event?.target?.closest?.('.el-menu-item') || event?.currentTarget?.querySelector?.('.el-menu-item')
      this.triggerClickEffect(target)
    },

    /**
     * 处理子菜单标题点击事件
     */
    handleSubmenuClick(event) {
      // 检查是否点击的是父菜单标题，而非子项
      // 如果点击的是子项，则不播放父菜单音效，避免音效重叠
      const target = event?.target?.closest?.('.el-sub-menu__title')
      const isClickingSubmenuTitle = target?.parentElement === event?.currentTarget
      
      if (!isClickingSubmenuTitle) {
        // 点击的是子项，不触发特效，也不播放音效
        return
      }
      
      // 注意：这里不播放音效，因为音效已经在 Sidebar/index.vue 的 handleMenuOpen/handleMenuClose 中处理
      // 如果在这里也播放，会导致音效重复
      
      if (this.menuClickEffect === 'none') return
      
      if (target) {
        this.triggerClickEffect(target)
      }
    },

    /**
     * 触发点击特效动画
     */
    triggerClickEffect(element) {
      if (!element || this.menuClickEffect === 'none') return

      this.clearClickEffect()
      this.clickEffectElement = element
      element.dataset.zeroMenuEffect = this.menuClickEffect
      // 独立动画层避免复用主题的选中指示伪元素，折叠菜单弹层也能显示特效。
      if (['ripple-gorgeous', 'pulse-gorgeous'].includes(this.menuClickEffect)) {
        const layer = document.createElement('span')
        layer.className = 'zero-menu-click-effect'
        layer.setAttribute('aria-hidden', 'true')
        element.appendChild(layer)
      }
      
      // 强制重绘以重启动画
      void element.offsetWidth
      
      // 添加点击样式
      element.classList.add('menu-clicking')
      
      // 根据绚丽版特效设置动画时间
      let duration = 800 // 绚丽水波纹和绚丽脉冲默认 800ms
      if (this.menuClickEffect === 'gradient-gorgeous') {
        duration = 1000 // 绚丽流光 1000ms
      }
      
      this.clickEffectTimer = setTimeout(() => {
        this.clearClickEffect()
      }, duration)
    },
    clearClickEffect() {
      if (this.clickEffectTimer) clearTimeout(this.clickEffectTimer)
      const element = this.clickEffectElement
      element?.classList.remove('menu-clicking')
      element?.querySelector(':scope > .zero-menu-click-effect')?.remove()
      element?.removeAttribute('data-zero-menu-effect')
      this.clickEffectTimer = null
      this.clickEffectElement = null
    }
  },

  beforeUnmount() {
    // 组件销毁时清理定时器
    this.clearClickEffect()
  }
}
</script>
