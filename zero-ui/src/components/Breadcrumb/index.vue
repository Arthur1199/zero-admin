<template>
  <el-breadcrumb class="app-breadcrumb" separator="/">
    <transition-group name="breadcrumb">
      <el-breadcrumb-item v-for="(item, index) in levelList" :key="item.path">
        <span 
          v-if="item.redirect === 'noRedirect' || index == levelList.length - 1" 
          class="no-redirect"
          :class="{ 'breadcrumb-shake': shouldShake }"
        >{{ item.meta.title }}</span>
        <a v-else @click.prevent="handleLink(item)" :class="{ 'breadcrumb-shake': shouldShake }">{{ item.meta.title }}</a>
      </el-breadcrumb-item>
    </transition-group>
  </el-breadcrumb>
</template>

<script>
export default {
  data() {
    return {
      levelList: null,
      shouldShake: true
    }
  },
  watch: {
    $route(route) {
      // if you go to the redirect page, do not update the breadcrumbs
      if (route.path.startsWith('/redirect/')) {
        return
      }
      this.getBreadcrumb()
      // 重启面包屑抖动动画，与Tab标签同步
      this.restartShakeAnimation()
    }
  },
  created() {
    this.getBreadcrumb()
  },
  methods: {
    getBreadcrumb() {
      // only show routes with meta.title
      let matched = []
      const router = this.$route
      const pathNum = this.findPathNum(router.path)
      // multi-level menu
      if (pathNum > 2) {
        const reg = /\/\w+/gi
        const pathList = router.path.match(reg).map((item, index) => {
          if (index !== 0) item = item.slice(1)
          return item
        })
        this.getMatched(pathList, this.$store.getters.defaultRoutes, matched)
      } else {
        matched = router.matched.filter(item => item.meta && item.meta.title)
      }
      // 判断是否为首页
      if (!this.isDashboard(matched[0])) {
        matched = [{ path: "/index", meta: { title: "首页" } }].concat(matched)
      }
      this.levelList = matched.filter(item => item.meta && item.meta.title && item.meta.breadcrumb !== false)
    },
    // 重启抖动动画
    restartShakeAnimation() {
      // 移除class
      this.shouldShake = false
      // 下一帧重新添加class，触发动画重启
      this.$nextTick(() => {
        this.shouldShake = true
      })
    },
    findPathNum(str, char = "/") {
      let index = str.indexOf(char)
      let num = 0
      while (index !== -1) {
        num++
        index = str.indexOf(char, index + 1)
      }
      return num
    },
    getMatched(pathList, routeList, matched) {
      let data = routeList.find(item => item.path == pathList[0] || (item.name += '').toLowerCase() == pathList[0])
      if (data) {
        matched.push(data)
        if (data.children && pathList.length) {
          pathList.shift()
          this.getMatched(pathList, data.children, matched)
        }
      }
    },
    isDashboard(route) {
      const name = route && route.name
      if (!name) {
        return false
      }
      return name.trim() === 'Index'
    },
    handleLink(item) {
      const { redirect, path } = item
      if (redirect) {
        this.$router.push(redirect)
        return
      }
      this.$router.push(path)
    }
  }
}
</script>

<style lang="scss" scoped>
.app-breadcrumb.el-breadcrumb {
  display: inline-block;
  font-size: 14px;
  line-height: 50px;
  margin-left: 8px;
  // 移除固定颜色，让主题样式生效
  // .no-redirect {
  //   color: #97a8be;
  //   cursor: text;
  // }
  .no-redirect {
    cursor: text;
  }
}
</style>

<style lang="scss">
/* 面包屑所有层级抖动效果（与菜单栏一致） */
.breadcrumb-shake {
  display: inline-block;
  animation: breadcrumb-shake-breath 2.5s ease-in-out infinite !important;
}

/* 面包屑抖动动画定义 */
@keyframes breadcrumb-shake-breath {
  0% {
    transform: translateX(0);
  }
  1% {
    transform: translateX(1.5px);
  }
  2% {
    transform: translateX(3px);
  }
  3% {
    transform: translateX(1.5px);
  }
  4% {
    transform: translateX(0);
  }
  5% {
    transform: translateX(-1.5px);
  }
  6% {
    transform: translateX(-3px);
  }
  7% {
    transform: translateX(-1.5px);
  }
  8% {
    transform: translateX(0);
  }
  /* 8%-95% 保持静止 */
  95% {
    transform: translateX(0);
  }
  /* 95%-100% 保持静止，模拟呼吸感 */
  100% {
    transform: translateX(0);
  }
}
</style>
