<template>
    <div :class="{'has-logo':showLogo}" :style="{ backgroundColor: settings.sideTheme === 'theme-dark' ? variables.menuBackground : variables.menuLightBackground }">
        <logo v-if="showLogo" :collapse="isCollapse" />
        <el-scrollbar :class="[settings.sideTheme, menuEffectClass]" wrap-class="scrollbar-wrapper">
            <el-menu
                ref="menuRef"
                :default-active="activeMenu"
                :collapse="isCollapse"
                :background-color="menuBackgroundColor"
                :text-color="menuTextColor"
                :active-text-color="menuActiveTextColor"
                :unique-opened="false"
                :default-openeds="savedOpenedKeys"
                :collapse-transition="false"
                mode="vertical"
                @open="handleMenuOpen"
                @close="handleMenuClose"
            >
                <sidebar-item
                    v-for="(route, index) in sidebarRouters"
                    :key="route.path  + index"
                    :item="route"
                    :base-path="route.path"
                />
            </el-menu>
        </el-scrollbar>
    </div>
</template>

<script>
import { mapGetters, mapState } from "vuex"
import Logo from "./Logo"
import SidebarItem from "./SidebarItem"
import variables from '@/assets/styles/variables.module.scss'
import { playSound, SoundType } from '@/utils/audio'

export default {
    components: { SidebarItem, Logo },
    computed: {
        ...mapState(["settings"]),
        ...mapGetters(["sidebarRouters", "sidebar", "menuOpenedKeys"]),
        activeMenu() {
            const route = this.$route
            const { meta, path } = route
            // if set path, the sidebar will highlight the path you set
            if (meta.activeMenu) {
                return meta.activeMenu
            }
            return path
        },
        showLogo() {
            return this.$store.state.settings.sidebarLogo
        },
        variables() {
            return variables
        },
        isCollapse() {
            return !this.sidebar.opened
        },
        // 刷新后默认返回空数组，不保持展开状态
        savedOpenedKeys() {
            return []
        },
        // 菜单特效样式类
        menuEffectClass() {
            const effect = this.$store.state.settings.menuClickEffect || 'gradient-gorgeous'
            if (effect === 'none') return ''
            return `menu-effect-${effect}`
        },
        // 菜单背景颜色 - 根据主题动态设置
        menuBackgroundColor() {
            const themeStyle = this.$store.state.settings.themeStyle

            // 毛玻璃拟态风:直接返回透明,阻止Element UI注入内联样式
            if (themeStyle === 'glassmorphism') {
                return 'transparent'
            }

            // 温馨自然风和质感设计风:返回undefined,让CSS接管
            if (themeStyle === 'warm-nature' || themeStyle === 'material-design') {
                return undefined
            }

            // 其他主题使用变量定义的背景色
            return this.settings.sideTheme === 'theme-dark'
                ? this.variables.menuBackground
                : this.variables.menuLightBackground
        },
        // 菜单文字颜色 - 根据主题动态设置
        menuTextColor() {
            const themeStyle = this.$store.state.settings.themeStyle

            // 毛玻璃拟态风、温馨自然风和质感设计风:返回undefined,让CSS接管(移除内联样式)
            if (themeStyle === 'glassmorphism' || themeStyle === 'warm-nature' || themeStyle === 'material-design') {
                return undefined
            }

            // 其他主题根据sideTheme设置
            return this.settings.sideTheme === 'theme-dark'
                ? this.variables.menuColor
                : this.variables.menuLightColor
        },
        // 激活菜单项文字颜色
        menuActiveTextColor() {
            const themeStyle = this.$store.state.settings.themeStyle

            // 毛玻璃拟态风、温馨自然风和质感设计风:返回undefined,让CSS接管
            if (themeStyle === 'glassmorphism' || themeStyle === 'warm-nature' || themeStyle === 'material-design') {
                return undefined
            }

            // 其他主题使用设置的主题色
            return this.settings.theme
        }
    },
    data() {
        return {
            allowAutoExpand: false,  // 标志位：是否允许自动展开（默认false，只有双击Tab时为true）
            isRouteChanging: false,  // 标志位：是否正在路由切换中（用于阻止自动展开）
            isInitializing: false,   // 标志位：是否正在初始化中（初始为false，始终不允许自动展开）
        }
    },
    created() {
        // 在组件创建时就清空菜单展开状态
        this.$store.dispatch('app/saveMenuOpenedKeys', [])
    },
    mounted() {
        // 立即启用拦截，阻止 Element UI 的自动展开

        // 等待下一帧，强制关闭所有已展开的菜单
        this.$nextTick(() => {
            this.forceCloseAllMenus()
        })
    },
    methods: {
        forceCloseAllMenus() {
            const closeTree = (routes, prefix = '') => (routes || []).forEach(route => {
                const index = (prefix + '/' + route.path).replace(/\/+/g, '/')
                if (route.children?.length) { this.$refs.menuRef?.close(index); closeTree(route.children, index) }
            })
            closeTree(this.sidebarRouters)
        },
        /**
         * 阻止菜单自动展开
         */
        openMenuItem(menuId, routeData) {
            const menu = this.$refs.menuRef
            if (!menu || !routeData?._fullPath) return Promise.resolve(false)
            menu.open(routeData._fullPath)
            return this.$nextTick().then(() => {
                this.$el.querySelector('[data-menu-id="' + menuId + '"]')?.scrollIntoView({ block: 'nearest' })
                return true
            })
        },
        handleMenuOpen(key, keyPath) {
            // 播放展开音效
            playSound(SoundType.MENU_EXPAND, this.$store)

            // 更新 store 中的菜单展开状态
            const keys = new Set(this.$store.getters.menuOpenedKeys || [])
            keys.add(key)
            this.$store.dispatch('app/saveMenuOpenedKeys', [...keys])
        },

        /**
         * 处理菜单关闭事件
         */
        handleMenuClose(key, keyPath) {
            // 播放关闭音效
            playSound(SoundType.MENU_COLLAPSE, this.$store)

            // 更新 store 中的菜单展开状态
            this.$store.dispatch('app/saveMenuOpenedKeys', (this.$store.getters.menuOpenedKeys || []).filter(index => index !== key))
        },

        /**
         * 对外暴露的方法：关闭菜单项
         */
        closeMenuItem(index) {
            const menu = this.$refs.menuRef
            if (!menu) return

            menu.close(index)
        }
    }
}
</script>
