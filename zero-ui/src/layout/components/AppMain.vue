<template>
  <section :class="{ 'has-copyright': footerVisible }" class="app-main">
    <router-view v-slot="{ Component }">
      <transition name="fade-transform" mode="out-in">
        <keep-alive :key="themeStyle" :include="cachedViews">
          <component :is="Component" v-if="!$route.meta.link" :key="key" />
        </keep-alive>
      </transition>
    </router-view>
    <iframe-toggle />
    <copyright />
  </section>
</template>

<script>
/**
 * 主路由内容与缓存入口；缓存键包含主题，另行维护内嵌路由及版权区。
 */
import copyright from "./Copyright/index"
import iframeToggle from "./IframeToggle/index"

export default {
  name: 'AppMain',
  components: { iframeToggle, copyright },
  computed: {
    cachedViews() {
      return this.$store.state.tagsView.cachedViews
    },
    themeStyle() {
      return this.$store.state.settings.themeStyle || 'default'
    },
    key() {
      return `${this.$route.path}:${this.themeStyle}`
    },
    footerVisible() {
      return this.$store.state.settings.footerVisible
    }
  },
  watch: {
    $route() {
      this.addIframe()
    }
  },
  mounted() {
    this.addIframe()
  },
  methods: {
    addIframe() {
      const { name } = this.$route
      if (name && this.$route.meta.link) {
        this.$store.dispatch('tagsView/addIframeView', this.$route)
      }
    }
  }
}
</script>

<style lang="scss" scoped>
// 组件局部布局与展示样式；跨页面主题及 body 弹层规则在 assets/styles 中统一维护。
.app-main {
  /* 50= navbar  50  */
  min-height: calc(100vh - 50px);
  width: 100%;
  position: relative;
  overflow: hidden;
}

.app-main.has-copyright {
  padding-bottom: 36px;
}

.fixed-header + .app-main {
  padding-top: 50px;
}

.hasTagsView {
  .app-main {
    /* 84 = navbar + tags-view = 50 + 34 */
    min-height: calc(100vh - 84px);
  }

  .fixed-header + .app-main {
    padding-top: 84px;
  }
}
</style>

<style lang="scss">
// fix css style bug in open el-dialog
.el-popup-parent--hidden {
  .fixed-header {
    padding-right: 6px;
  }
}

::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background-color: #f1f1f1;
}

::-webkit-scrollbar-thumb {
  background-color: #c0c0c0;
  border-radius: 3px;
}
</style>
