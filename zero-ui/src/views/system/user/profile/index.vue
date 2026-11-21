<template>
  <div class="app-container profile-page">
    <div class="profile-layout" :class="{ 'profile-layout--overview': !selectedPanel }" v-loading="loading">
      <el-card class="profile-summary">
        <div class="profile-identity">
          <button type="button" class="user-info-head" aria-label="编辑头像" @click="selectedPanel = 'avatar'">
            <img :src="$store.getters.avatar" class="img-circle img-lg" alt="" />
          </button>
          <h1>{{ user.nickName || user.userName || "个人中心" }}</h1>
          <p v-if="user.userName && user.nickName">{{ user.userName }}</p>
          <div class="profile-actions">
            <el-button v-for="panel in panels" :key="panel.name" size="small" plain
              :type="selectedPanel === panel.name ? 'primary' : 'default'"
              :aria-pressed="selectedPanel === panel.name"
              :icon="resolveIcon(panel.icon)" @click="selectedPanel = panel.name">{{ panel.label }}</el-button>
          </div>
        </div>

        <h2>个人信息</h2>
        <dl class="profile-details">
          <div v-for="detail in accountDetails" :key="detail.label">
            <dt><svg-icon :icon-class="detail.icon" />{{ detail.label }}</dt>
            <dd>{{ detail.value || "—" }}</dd>
          </div>
        </dl>
      </el-card>
      <profile-statistics v-if="!selectedPanel" />
      <el-card v-show="selectedPanel" class="profile-editor" role="region" :aria-label="activePanel?.title || activePanel?.label">
        <template #header>
          <h2 class="profile-card-title">{{ activePanel?.title || activePanel?.label }}</h2>
        </template>
        <div v-if="selectedPanel === 'avatar'" class="profile-avatar">
          <userAvatar @close="selectedPanel = ''" />
        </div>
        <section v-show="selectedPanel === 'userinfo'" v-loading="loading">
          <div v-if="loadFailed" class="profile-load-error">
            <span>个人资料读取失败</span>
            <el-button link type="primary" @click="getUser">重新加载</el-button>
          </div>
          <userInfo v-if="profileLoaded" :user="user" @saved="updateProfileSummary" @close="selectedPanel = ''" />
        </section>
        <div v-if="selectedPanel === 'resetPwd'" class="profile-password">
          <resetPwd @close="selectedPanel = ''" />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script>
/**
 * 当前账号的个人中心入口；汇总基本资料并切换资料、密码及统计面板。
 */
import userAvatar from "./userAvatar"
import userInfo from "./userInfo"
import resetPwd from "./resetPwd"
import profileStatistics from "./profileStatistics"
import { getUserProfile } from "@/api/system/user"

export default {
  name: "Profile",
  components: { userAvatar, userInfo, resetPwd, profileStatistics },
  data() {
    return {
      user: {},
      roleGroup: "",
      postGroup: "",
      loading: false,
      loadFailed: false,
      profileLoaded: false,
      selectedPanel: "",
      panels: [
        { name: "avatar", label: "更换头像", title: "修改头像", icon: "el-icon-picture" },
        { name: "userinfo", label: "修改个人信息", icon: "el-icon-edit" },
        { name: "resetPwd", label: "修改密码", icon: "el-icon-lock" }
      ]
    }
  },
  computed: {
    activePanel() {
      return this.panels.find(panel => panel.name === this.selectedPanel)
    },
    accountDetails() {
      return [
        { label: "所属部门", icon: "tree", value: this.user.dept?.deptName },
        { label: "所属岗位", icon: "post", value: this.postGroup },
        { label: "所属角色", icon: "peoples", value: this.roleGroup },
        { label: "账号创建", icon: "date", value: this.user.createTime },
        ...(this.user.loginDate ? [{ label: "最近登录", icon: "date", value: this.parseTime(this.user.loginDate) }] : [])
      ]
    }
  },
  created() {
    if (this.$route.query.activeTab === "resetPwd") {
      this.selectedPanel = "resetPwd"
    }
    this.getUser()
  },
  methods: {
    getUser() {
      if (this.loading) return
      this.loading = true
      this.loadFailed = false
      getUserProfile().then(response => {
        this.user = response.data || {}
        this.roleGroup = response.roleGroup
        this.postGroup = response.postGroup
        this.profileLoaded = Boolean(response.data)
        this.loadFailed = !this.profileLoaded
      }).catch(() => {
        this.loadFailed = true
      }).finally(() => {
        this.loading = false
      })
    },
    updateProfileSummary(profile) {
      this.user = { ...this.user, ...profile }
      this.$store.commit("SET_NICK_NAME", profile.nickName)
    }
  }
}
</script>

<style scoped lang="scss">
// 组件局部布局与展示样式；跨页面主题及 body 弹层规则在 assets/styles 中统一维护。
.profile-layout {
  display: grid;
  container: profile-layout / inline-size;
  grid-template-columns: minmax(280px, 340px) minmax(0, 1fr);
  align-items: start;
  gap: 20px;
}

.profile-layout > * {
  min-width: 0;
}

@media (width > 1050px) {
  @container profile-layout (min-width: 1000px) {
    .profile-layout--overview > .profile-summary:hover,
    .profile-layout--overview > :deep(.profile-statistics > .el-card:hover) {
      transform: none;
    }
  }
}

.profile-card-title {
  font-size: 16px;
  line-height: 24px;
  margin: 0;
}

.profile-avatar {
  max-width: 760px;
}

.profile-password {
  max-width: 520px;
  padding-top: 8px;
}

@media (max-width: 1050px) {
  .profile-layout {
    grid-template-columns: minmax(0, 1fr);
  }
}

.profile-identity {
  display: grid;
  justify-items: center;
  gap: 8px;
  text-align: center;
  overflow-wrap: anywhere;
  padding: 4px 0 22px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.profile-identity h1 {
  font-size: 22px;
  line-height: 1.35;
  margin: 4px 0 0;
}

.profile-identity p {
  margin: 0;
  opacity: .8;
}

.user-info-head {
  padding: 0;
  border: 0;
  border-radius: 50%;
  background: transparent;
  line-height: 0;
  cursor: pointer;
}

.profile-actions, .profile-load-error {
  display: flex;
  align-items: center;
  gap: 8px;
}

.profile-load-error {
  flex-wrap: wrap;
}

.profile-actions {
  width: 100%;
  flex-wrap: nowrap;
  justify-content: safe center;
  gap: 6px;
  margin-top: 4px;
  overflow-x: auto;
}

.profile-actions :deep(.el-button) {
  flex: 0 0 auto;
  padding-inline: 6px;
}

.profile-actions :deep(.el-button + .el-button) {
  margin-left: 0;
}

.profile-summary h2 {
  font-size: 16px;
  line-height: 24px;
  margin: 16px 0 6px;
}

.profile-details {
  margin: 0;
}

.profile-details > div {
  display: grid;
  grid-template-columns: max-content minmax(0, 1fr);
  gap: 12px;
  padding: 9px 0;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.profile-details > div:last-child {
  border-bottom: 0;
}

.profile-details dt {
  display: flex;
  align-items: center;
  gap: 5px;
  opacity: .85;
}

.profile-details dd {
  margin: 0;
  text-align: right;
  overflow-wrap: anywhere;
}
</style>
