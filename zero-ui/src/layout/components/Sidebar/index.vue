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
import variables from "@/assets/styles/variables.scss"
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
        this.blockMenuAutoExpand()

        // 等待下一帧，强制关闭所有已展开的菜单
        this.$nextTick(() => {
            this.forceCloseAllMenus()
        })
    },
    methods: {
        /**
         * 阻止菜单自动展开
         */
        blockMenuAutoExpand() {
            if (!this.$refs.menuRef) return

            const menu = this.$refs.menuRef
            const vm = this

            // 保存原始open方法
            if (!menu._originalOpen) {
                menu._originalOpen = menu.open
            }

            // 保存原始openMenu方法（Element UI内部使用）
            if (!menu._originalOpenMenu) {
                menu._originalOpenMenu = menu.openMenu
            }

            // 重写openMenu方法，只允许双击Tab和用户点击时展开，始终阻止初始化时的自动展开
            menu.openMenu = function(index, indexPath) {
                // 如果菜单处于折叠状态，允许悬浮弹出（这是Element UI的默认行为）
                if (vm.isCollapse) {
                    return menu._originalOpenMenu.call(this, index, indexPath)
                }

                // 如果允许自动展开（双击Tab触发），则调用原始方法
                if (vm.allowAutoExpand) {
                    return menu._originalOpenMenu.call(this, index, indexPath)
                }

                // 检查调用堆栈，判断是否为用户点击submenu
                const stack = new Error().stack
                const isUserClick = stack && stack.includes('handleSubmenuClick')

                if (isUserClick) {
                    return menu._originalOpenMenu.call(this, index, indexPath)
                }

                // 其他情况（路由切换、初始化触发）全部阻止展开
            }
        },

        /**
         * 对外暴露的方法：展开菜单项（绕过拦截）
         * 修复版本：通过 menuId 查找 DOM，支持无限层级的菜单展开
         * @param {number} menuId - 菜单ID
         * @param {object} routeData - 路由对象数据（包含 _fullPath 等信息）
         */
        openMenuItem(menuId, routeData) {
            const menu = this.$refs.menuRef
            if (!menu || !menuId) {
                return Promise.resolve(false)
            }

            const menuIndex = routeData._fullPath || ''

            return new Promise((resolve) => {
                try {
                    this.allowAutoExpand = false

                    this.$nextTick(() => {

                        // 通过 menuId 查找对应的 DOM 元素
                        let submenuEl = null
                        const allSubmenus = this.$el.querySelectorAll('li.el-submenu')

                        submenuEl = Array.from(allSubmenus).find(el => {
                            // 方法1: 通过 Vue 实例的 data 属性
                            const vueInstance = el.__vue__
                            if (vueInstance) {
                                const item = vueInstance.$attrs.item || vueInstance.item
                                if (item && item.menuId === menuId) {
                                    // console.log('[Sidebar.openMenuItem] 通过 Vue 实例找到元素')
                                    return true
                                }
                            }

                            // 方法2: 通过自定义 data 属性
                            const dataMenuId = el.getAttribute('data-menu-id')
                            if (dataMenuId && parseInt(dataMenuId) === menuId) {
                                // console.log('[Sidebar.openMenuItem] 通过 data-menu-id 找到元素')
                                return true
                            }

                            return false
                        })

                        if (submenuEl) {
                            // console.log('[Sidebar.openMenuItem] 找到菜单元素 - menuId:', menuId)

                            // 查找子菜单容器
                            const submenuWrapper = submenuEl.querySelector('.el-menu--inline')
                            if (submenuWrapper) {
                                // console.log('[Sidebar.openMenuItem] 找到子菜单容器，强制展开')

                                // 1. 更新 Element UI 的 openedMenus 状态
                                if (!menu.openedMenus.includes(menuIndex)) {
                                    // 临时允许自动展开
                                    this.allowAutoExpand = true
                                    menu.openedMenus.push(menuIndex)
                                    this.allowAutoExpand = false
                                }

                                // 2. 直接设置 CSS，强制展开
                                submenuWrapper.style.setProperty('display', 'block', 'important')
                                submenuWrapper.style.setProperty('opacity', '1', 'important')
                                submenuWrapper.style.setProperty('visibility', 'visible', 'important')
                                submenuWrapper.style.setProperty('height', 'auto', 'important')
                                submenuWrapper.style.setProperty('overflow', 'visible', 'important')
                                submenuWrapper.style.setProperty('max-height', 'none', 'important')

                                // 3. 给 submenu 添加 is-opened class
                                submenuEl.classList.add('is-opened')
                                submenuEl.classList.add('is-active')

                                // 标记这是通过双击Tab强制展开的
                                submenuEl.setAttribute('data-force-opened', 'true')

                                // 监听用户手动点击收起事件
                                const titleEl = submenuEl.querySelector('.el-submenu__title')
                                if (titleEl) {
                                    // 移除之前可能添加的监听器
                                    if (titleEl._clickHandler) {
                                        titleEl.removeEventListener('click', titleEl._clickHandler, true)
                                    }

                                    // 添加新的点击监听器（捕获阶段）
                                    titleEl._clickHandler = (event) => {
                                        const isForceOpened = submenuEl.getAttribute('data-force-opened') === 'true'

                                        if (isForceOpened) {
                                            event.stopPropagation()
                                            event.preventDefault()

                                            submenuEl.removeAttribute('data-force-opened')

                                            // 移除强制设置的 CSS
                                            submenuWrapper.style.removeProperty('display')
                                            submenuWrapper.style.removeProperty('opacity')
                                            submenuWrapper.style.removeProperty('visibility')
                                            submenuWrapper.style.removeProperty('height')
                                            submenuWrapper.style.removeProperty('overflow')
                                            submenuWrapper.style.removeProperty('max-height')

                                            submenuEl.classList.remove('is-opened')
                                            submenuEl.classList.remove('is-active')

                                            // 从 openedMenus 数组中移除
                                            const menu = this.$refs.menuRef
                                            if (menu && menu.openedMenus) {
                                                const idx = menu.openedMenus.indexOf(menuIndex)
                                                if (idx !== -1) {
                                                    menu.openedMenus.splice(idx, 1)
                                                }
                                            }

                                            submenuWrapper.style.display = 'none'
                                        }
                                    }

                                    titleEl.addEventListener('click', titleEl._clickHandler, true)
                                }

                                // 成功展开
                                resolve(true)
                            } else {
                                // console.log('[Sidebar.openMenuItem] 未找到子菜单容器（可能是叶子节点）')
                                // 可能是叶子节点，不需要展开
                                resolve(false)
                            }
                        } else {
                            // console.warn('[Sidebar.openMenuItem] 未找到菜单元素 - menuId:', menuId)
                            // 可能是叶子节点
                            resolve(false)
                        }
                    })

                } catch (err) {
                    console.error('[Sidebar.openMenuItem] 异常:', err)
                    this.allowAutoExpand = false
                    resolve(false)
                }
            })
        },

        /**
         * 强制关闭所有已展开的菜单
         */
        forceCloseAllMenus() {
            const menu = this.$refs.menuRef
            if (!menu) return

            // 1. 清空 openedMenus 数组
            if (menu.openedMenus && menu.openedMenus.length > 0) {
                const openedList = [...menu.openedMenus]
                menu.openedMenus = []

                // 2. 通过 DOM 操作强制关闭
                openedList.forEach(menuIndex => {
                    const submenuEl = this.$el.querySelector(`li.el-submenu[index="${menuIndex}"]`)
                    if (submenuEl) {
                        submenuEl.classList.remove('is-opened')
                        submenuEl.classList.remove('is-active')

                        const submenuWrapper = submenuEl.querySelector('.el-menu--inline')
                        if (submenuWrapper) {
                            submenuWrapper.style.display = 'none'
                        }
                    }
                })
            }
        },

        /**
         * 处理菜单展开事件
         */
        handleMenuOpen(key, keyPath) {
            // 播放展开音效
            playSound(SoundType.MENU_EXPAND, this.$store)

            // 更新 store 中的菜单展开状态
            const menu = this.$refs.menuRef
            if (menu && menu.openedMenus) {
                this.$store.dispatch('app/saveMenuOpenedKeys', [...menu.openedMenus])
            }
        },

        /**
         * 处理菜单关闭事件
         */
        handleMenuClose(key, keyPath) {
            // 播放关闭音效
            playSound(SoundType.MENU_COLLAPSE, this.$store)

            // 更新 store 中的菜单展开状态
            const menu = this.$refs.menuRef
            if (menu && menu.openedMenus) {
                this.$store.dispatch('app/saveMenuOpenedKeys', [...menu.openedMenus])
            }
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
