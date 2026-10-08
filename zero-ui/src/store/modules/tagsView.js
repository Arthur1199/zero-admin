// 从 localStorage 恢复已访问的标签页
const getVisitedViewsFromStorage = () => {
  try {
    const stored = localStorage.getItem('visitedViews')
    if (stored) {
      const views = JSON.parse(stored)
      // 确保每个 view 都有必要的字段
      return views.map(view => ({
        fullPath: view.fullPath,
        path: view.path,
        name: view.name,
        title: view.title,
        query: view.query || {},
        params: view.params || {},
        meta: view.meta || {}
      }))
    }
    return []
  } catch (e) {
    return []
  }
}

// 保存已访问的标签页到 localStorage
const saveVisitedViewsToStorage = (views) => {
  try {
    // 只保存必要的字段，避免循环引用和不可序列化的对象
    const serializedViews = views.map(view => ({
      fullPath: view.fullPath,
      path: view.path,
      name: view.name,
      title: view.title,
      query: view.query || {},
      params: view.params || {},
      meta: {
        title: view.meta?.title,
        icon: view.meta?.icon,
        affix: view.meta?.affix,
        noCache: view.meta?.noCache,
        link: view.meta?.link
      }
    }))
    localStorage.setItem('visitedViews', JSON.stringify(serializedViews))
  } catch (e) {
    // 忽略存储错误
  }
}

const state = {
  visitedViews: getVisitedViewsFromStorage(),
  cachedViews: [],
  iframeViews: []
}

const mutations = {
  ADD_IFRAME_VIEW: (state, view) => {
    if (state.iframeViews.some(v => v.path === view.path)) return
    state.iframeViews.push(
      Object.assign({}, view, {
        title: view.meta.title || 'no-name'
      })
    )
  },
  ADD_VISITED_VIEW: (state, view) => {
    if (state.visitedViews.some(v => v.path === view.path)) return
    state.visitedViews.push(
      Object.assign({}, view, {
        title: view.meta.title || 'no-name'
      })
    )
    // 保存到 localStorage
    saveVisitedViewsToStorage(state.visitedViews)
  },
  ADD_CACHED_VIEW: (state, view) => {
    if (state.cachedViews.includes(view.name)) return
    if (view.meta && !view.meta.noCache) {
      state.cachedViews.push(view.name)
    }
  },
  DEL_VISITED_VIEW: (state, view) => {
    for (const [i, v] of state.visitedViews.entries()) {
      if (v.path === view.path) {
        state.visitedViews.splice(i, 1)
        break
      }
    }
    state.iframeViews = state.iframeViews.filter(item => item.path !== view.path)
    // 保存到 localStorage
    saveVisitedViewsToStorage(state.visitedViews)
  },
  DEL_IFRAME_VIEW: (state, view) => {
    state.iframeViews = state.iframeViews.filter(item => item.path !== view.path)
  },
  DEL_CACHED_VIEW: (state, view) => {
    const index = state.cachedViews.indexOf(view.name)
    index > -1 && state.cachedViews.splice(index, 1)
  },

  DEL_OTHERS_VISITED_VIEWS: (state, view) => {
    state.visitedViews = state.visitedViews.filter(v => {
      return v.meta.affix || v.path === view.path
    })
    state.iframeViews = state.iframeViews.filter(item => item.path === view.path)
    // 保存到 localStorage
    saveVisitedViewsToStorage(state.visitedViews)
  },
  DEL_OTHERS_CACHED_VIEWS: (state, view) => {
    const index = state.cachedViews.indexOf(view.name)
    if (index > -1) {
      state.cachedViews = state.cachedViews.slice(index, index + 1)
    } else {
      state.cachedViews = []
    }
  },
  DEL_ALL_VISITED_VIEWS: state => {
    // keep affix tags
    const affixTags = state.visitedViews.filter(tag => tag.meta.affix)
    state.visitedViews = affixTags
    state.iframeViews = []
    // 保存到 localStorage
    saveVisitedViewsToStorage(state.visitedViews)
  },
  DEL_ALL_CACHED_VIEWS: state => {
    state.cachedViews = []
  },
  UPDATE_VISITED_VIEW: (state, view) => {
    for (let v of state.visitedViews) {
      if (v.path === view.path) {
        v = Object.assign(v, view)
        break
      }
    }
    // 保存到 localStorage
    saveVisitedViewsToStorage(state.visitedViews)
  },
  DEL_RIGHT_VIEWS: (state, view) => {
    const index = state.visitedViews.findIndex(v => v.path === view.path)
    if (index === -1) {
      return
    }
    state.visitedViews = state.visitedViews.filter((item, idx) => {
      if (idx <= index || (item.meta && item.meta.affix)) {
        return true
      }
      const i = state.cachedViews.indexOf(item.name)
      if (i > -1) {
        state.cachedViews.splice(i, 1)
      }
      if(item.meta.link) {
        const fi = state.iframeViews.findIndex(v => v.path === item.path)
        state.iframeViews.splice(fi, 1)
      }
      return false
    })
    // 保存到 localStorage
    saveVisitedViewsToStorage(state.visitedViews)
  },
  DEL_LEFT_VIEWS: (state, view) => {
    const index = state.visitedViews.findIndex(v => v.path === view.path)
    if (index === -1) {
      return
    }
    state.visitedViews = state.visitedViews.filter((item, idx) => {
      if (idx >= index || (item.meta && item.meta.affix)) {
        return true
      }
      const i = state.cachedViews.indexOf(item.name)
      if (i > -1) {
        state.cachedViews.splice(i, 1)
      }
      if(item.meta.link) {
        const fi = state.iframeViews.findIndex(v => v.path === item.path)
        state.iframeViews.splice(fi, 1)
      }
      return false
    })
    // 保存到 localStorage
    saveVisitedViewsToStorage(state.visitedViews)
  }
}

const actions = {
  addView({ dispatch }, view) {
    dispatch('addVisitedView', view)
    dispatch('addCachedView', view)
  },
  addIframeView({ commit }, view) {
    commit('ADD_IFRAME_VIEW', view)
  },
  addVisitedView({ commit }, view) {
    commit('ADD_VISITED_VIEW', view)
  },
  addCachedView({ commit }, view) {
    commit('ADD_CACHED_VIEW', view)
  },
  delView({ dispatch, state }, view) {
    return new Promise(resolve => {
      dispatch('delVisitedView', view)
      dispatch('delCachedView', view)
      resolve({
        visitedViews: [...state.visitedViews],
        cachedViews: [...state.cachedViews]
      })
    })
  },
  delVisitedView({ commit, state }, view) {
    return new Promise(resolve => {
      commit('DEL_VISITED_VIEW', view)
      resolve([...state.visitedViews])
    })
  },
  delIframeView({ commit, state }, view) {
    return new Promise(resolve => {
      commit('DEL_IFRAME_VIEW', view)
      resolve([...state.iframeViews])
    })
  },
  delCachedView({ commit, state }, view) {
    return new Promise(resolve => {
      commit('DEL_CACHED_VIEW', view)
      resolve([...state.cachedViews])
    })
  },
  delOthersViews({ dispatch, state }, view) {
    return new Promise(resolve => {
      dispatch('delOthersVisitedViews', view)
      dispatch('delOthersCachedViews', view)
      resolve({
        visitedViews: [...state.visitedViews],
        cachedViews: [...state.cachedViews]
      })
    })
  },
  delOthersVisitedViews({ commit, state }, view) {
    return new Promise(resolve => {
      commit('DEL_OTHERS_VISITED_VIEWS', view)
      resolve([...state.visitedViews])
    })
  },
  delOthersCachedViews({ commit, state }, view) {
    return new Promise(resolve => {
      commit('DEL_OTHERS_CACHED_VIEWS', view)
      resolve([...state.cachedViews])
    })
  },
  delAllViews({ dispatch, state }, view) {
    return new Promise(resolve => {
      dispatch('delAllVisitedViews', view)
      dispatch('delAllCachedViews', view)
      resolve({
        visitedViews: [...state.visitedViews],
        cachedViews: [...state.cachedViews]
      })
    })
  },
  delAllVisitedViews({ commit, state }) {
    return new Promise(resolve => {
      commit('DEL_ALL_VISITED_VIEWS')
      resolve([...state.visitedViews])
    })
  },
  delAllCachedViews({ commit, state }) {
    return new Promise(resolve => {
      commit('DEL_ALL_CACHED_VIEWS')
      resolve([...state.cachedViews])
    })
  },
  updateVisitedView({ commit }, view) {
    commit('UPDATE_VISITED_VIEW', view)
  },
  delRightTags({ commit }, view) {
    return new Promise(resolve => {
      commit('DEL_RIGHT_VIEWS', view)
      resolve([...state.visitedViews])
    })
  },
  delLeftTags({ commit }, view) {
    return new Promise(resolve => {
      commit('DEL_LEFT_VIEWS', view)
      resolve([...state.visitedViews])
    })
  },
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}
