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
                popper-class="sidebar-menu-popper"
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
                    :active-branches="activeBranches"
                />
            </el-menu>
        </el-scrollbar>
    </div>
</template>

<script>
/**
 * 侧栏菜单根节点：根据路由树计算任意深度的当前分支，并协调折叠、手动展开及页签定位。
 */
import { mapGetters, mapState } from "vuex"
import Logo from "./Logo"
import SidebarItem from "./SidebarItem"
import variables from '@/assets/styles/base/variables.module.scss'
import { playSound, SoundType } from '@/utils/audio'
import { resolvePath } from '@/utils/path'
import { isExternal } from '@/utils/validate'

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
        // 返回当前叶子路由的祖先节点集合，递归组件直接复用，不依赖弹层是否已经挂载。
        activeBranches() {
            const activePath = resolvePath(this.activeMenu)
            const branches = new Set()
            // 弹出菜单可能尚未挂载，直接从可见菜单树确定当前路由的各级父项。
            const findActive = (routes, basePath) => routes.some(route => {
                if (route.hidden || isExternal(route.path)) return false
                const menuPath = resolvePath(basePath, route.path)
                if (route.children?.length && findActive(route.children, menuPath)) {
                    branches.add(route)
                    return true
                }
                return menuPath === activePath
            })
            findActive(this.sidebarRouters, '/')
            return branches
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

            // 显式向 Plus 菜单传入透明底，避免默认背景盖住主题表面。
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
        // 挂载后先关闭已有展开项；后续是否允许展开由 openMenuItem 与状态标记共同决定。

        // 等待下一帧，强制关闭所有已展开的菜单
        this.$nextTick(() => {
            this.forceCloseAllMenus()
        })
    },
    methods: {
        // 遍历整棵路由树关闭各级子菜单，不以二级或三级作为固定上限。
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
