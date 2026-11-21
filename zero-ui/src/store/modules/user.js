import router, { resetRouter } from '@/router'
import { confirm } from '@/plugins/modal'
import { login, logout, getInfo } from '@/api/login'
import { getToken, setToken, removeToken } from '@/utils/auth'
import { isHttp, isEmpty } from "@/utils/validate"
import defAva from '@/assets/images/profile.jpg'
import { adminWebSocket } from '@/utils/websocket'

const user = {
  state: {
    token: getToken(),
    id: '',
    name: '',
    nickName: '',
    avatar: '',
    roles: [],
    permissions: []
  },

  mutations: {
    SET_TOKEN: (state, token) => {
      state.token = token
    },
    SET_ID: (state, id) => {
      state.id = id
    },
    SET_NAME: (state, name) => {
      state.name = name
    },
    SET_NICK_NAME: (state, nickName) => {
      state.nickName = nickName
    },
    SET_AVATAR: (state, avatar) => {
      state.avatar = avatar
    },
    SET_ROLES: (state, roles) => {
      state.roles = roles
    },
    SET_PERMISSIONS: (state, permissions) => {
      state.permissions = permissions
    }
  },

  actions: {
    // 登录
    Login({ commit, dispatch }, userInfo) {
      const username = userInfo.username.trim()
      const password = userInfo.password
      const code = userInfo.code
      const uuid = userInfo.uuid
      const googleCode = userInfo.googleCode
      return new Promise((resolve, reject) => {
        login(username, password, code, uuid, googleCode).then(async res => {
          // Cookie过期后的同页登录也需清理旧身份、动态路由和页面缓存，再按新Token初始化。
          await dispatch('FedLogOut')
          setToken(res.token)
          commit('SET_TOKEN', res.token)
          resolve()
        }).catch(error => {
          reject(error)
        })
      })
    },

    // 获取用户信息
    GetInfo({ commit, state }) {
      return new Promise((resolve, reject) => {
        getInfo().then(res => {
          const user = res.user
          let avatar = user.avatar || ""
          if (!isHttp(avatar)) {
            avatar = (isEmpty(avatar)) ? defAva : import.meta.env.VITE_APP_BASE_API + avatar
          }
          if (res.roles && res.roles.length > 0) { // 验证返回的roles是否是一个非空数组
            commit('SET_ROLES', res.roles)
            commit('SET_PERMISSIONS', res.permissions)
          } else {
            commit('SET_ROLES', ['ROLE_DEFAULT'])
          }
          commit('SET_ID', user.userId)
          commit('SET_NAME', user.userName)
          commit('SET_NICK_NAME', user.nickName)
          commit('SET_AVATAR', avatar)
          /* 初始密码提示 */
          if(res.isDefaultModifyPwd) {
            confirm('您的密码还是初始密码，请修改密码！', '安全提示').then(() => {
              router.push({ name: 'Profile', query: { activeTab: 'resetPwd' } })
            }).catch(() => {})
          }
          /* 过期密码提示 */
          if(!res.isDefaultModifyPwd && res.isPasswordExpired) {
            confirm('您的密码已过期，请尽快修改密码！', '安全提示').then(() => {
              router.push({ name: 'Profile', query: { activeTab: 'resetPwd' } })
            }).catch(() => {})
          }
          resolve(res)
        }).catch(error => {
          reject(error)
        })
      })
    },

    // 退出系统
    LogOut({ commit, state, dispatch }) {
      const token = state.token
      // 退出失败仍停留当前页面，暂停期间保留订阅；成功后再彻底清理。
      adminWebSocket.pause()
      return new Promise((resolve, reject) => {
        logout(token).then(() => {
          // 旧请求返回期间可能已重新登录，只释放暂停，不清除新身份。
          if (state.token !== token || (getToken() && getToken() !== token)) {
            adminWebSocket.resume()
            resolve()
            return
          }
          adminWebSocket.stop()
          commit('SET_TOKEN', '')
          commit('SET_ROLES', [])
          commit('SET_PERMISSIONS', [])
          removeToken()
          resetRouter()
          dispatch('tagsView/delAllViews')
          // 清除 localStorage 中的 Tab 标签数据
          localStorage.removeItem('visitedViews')
          localStorage.removeItem('google_auth_apis')
          resolve()
        }).catch(error => {
          adminWebSocket.resume()
          reject(error)
        })
      })
    },

    // 被动退出：连接工具已停止重连；退出接口失败也清理本地，旧身份不能清除新登录态。
    async ForceLogOut({ state, dispatch }, token) {
      if (!token || state.token !== token || (getToken() && getToken() !== token)) return
      try { await logout(token) } catch { /* 服务端会话可能已撤销或网络不可达，仍需本地退出 */ }
      if (state.token === token && (!getToken() || getToken() === token)) await dispatch('FedLogOut')
    },

    // 前端 登出
    FedLogOut({ commit, dispatch }) {
      adminWebSocket.stop()
      return new Promise(resolve => {
        commit('SET_TOKEN', '')
        commit('SET_ROLES', [])
        commit('SET_PERMISSIONS', [])
        dispatch('tagsView/delAllViews')
        removeToken()
        resetRouter()
        // 清除 localStorage 中的 Tab 标签数据
        localStorage.removeItem('visitedViews')
        localStorage.removeItem('google_auth_apis')
        resolve()
      })
    }
  }
}

export default user
