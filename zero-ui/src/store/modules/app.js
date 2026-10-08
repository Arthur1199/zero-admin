import Cookies from 'js-cookie'

const DEFAULT_SIDEBAR_WIDTH = 225
const MIN_SIDEBAR_WIDTH = 225
const MAX_SIDEBAR_WIDTH = 360

function normalizeSidebarWidth(width) {
  const value = Number(width)
  if (!Number.isFinite(value)) {
    return DEFAULT_SIDEBAR_WIDTH
  }
  return Math.min(MAX_SIDEBAR_WIDTH, Math.max(MIN_SIDEBAR_WIDTH, Math.round(value)))
}

const state = {
  sidebar: {
    opened: Cookies.get('sidebarStatus') ? !!+Cookies.get('sidebarStatus') : true,
    withoutAnimation: false,
    hide: false,
    width: normalizeSidebarWidth(Cookies.get('sidebarWidth'))
  },
  device: 'desktop',
  size: Cookies.get('size') || 'medium',
  // 保存菜单展开状态
  menuOpenedKeys: []
}

const mutations = {
  TOGGLE_SIDEBAR: state => {
    if (state.sidebar.hide) {
      return false
    }
    state.sidebar.opened = !state.sidebar.opened
    state.sidebar.withoutAnimation = false
    if (state.sidebar.opened) {
      Cookies.set('sidebarStatus', 1)
    } else {
      Cookies.set('sidebarStatus', 0)
    }
  },
  CLOSE_SIDEBAR: (state, withoutAnimation) => {
    Cookies.set('sidebarStatus', 0)
    state.sidebar.opened = false
    state.sidebar.withoutAnimation = withoutAnimation
  },
  TOGGLE_DEVICE: (state, device) => {
    state.device = device
  },
  SET_SIZE: (state, size) => {
    state.size = size
    Cookies.set('size', size)
  },
  SET_SIDEBAR_HIDE: (state, status) => {
    state.sidebar.hide = status
  },
  SET_SIDEBAR_WIDTH: (state, width) => {
    state.sidebar.width = normalizeSidebarWidth(width)
    Cookies.set('sidebarWidth', state.sidebar.width)
  },
  // 设置菜单展开的keys
  SET_MENU_OPENED_KEYS: (state, keys) => {
    state.menuOpenedKeys = keys || []
    // 保存到 localStorage，刷新后仍然保持
    try {
      localStorage.setItem('menuOpenedKeys', JSON.stringify(state.menuOpenedKeys))
    } catch (e) {
      console.error('保存菜单展开状态失败:', e)
    }
  },
  // 初始化菜单展开状态（从 localStorage 读取）
  INIT_MENU_OPENED_KEYS: (state) => {
    try {
      const savedKeys = localStorage.getItem('menuOpenedKeys')
      if (savedKeys) {
        state.menuOpenedKeys = JSON.parse(savedKeys)
      }
    } catch (e) {
      console.error('读取菜单展开状态失败:', e)
      state.menuOpenedKeys = []
    }
  }
}

const actions = {
  toggleSideBar({ commit }) {
    commit('TOGGLE_SIDEBAR')
  },
  closeSideBar({ commit }, { withoutAnimation }) {
    commit('CLOSE_SIDEBAR', withoutAnimation)
  },
  toggleDevice({ commit }, device) {
    commit('TOGGLE_DEVICE', device)
  },
  setSize({ commit }, size) {
    commit('SET_SIZE', size)
  },
  toggleSideBarHide({ commit }, status) {
    commit('SET_SIDEBAR_HIDE', status)
  },
  setSidebarWidth({ commit }, width) {
    commit('SET_SIDEBAR_WIDTH', width)
  },
  // 保存菜单展开状态
  saveMenuOpenedKeys({ commit }, keys) {
    commit('SET_MENU_OPENED_KEYS', keys)
  },
  // 初始化菜单展开状态
  initMenuOpenedKeys({ commit }) {
    commit('INIT_MENU_OPENED_KEYS')
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}
