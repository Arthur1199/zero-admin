<template>
  <div class="zero-home">
    <!-- 顶部横幅 -->
    <div class="hero-section">
      <div class="hero-content">
        <h1 class="main-title">
          Zero管理框架
        </h1>
        <p class="subtitle">现代化企业级后台管理系统解决方案</p>
        <div class="version-tag">
          <el-tag type="success" effect="dark">v{{ version }}</el-tag>
          <el-tag type="danger" effect="dark">免费开源</el-tag>
          <el-tag type="warning" effect="dark">持续更新</el-tag>
        </div>
      </div>
    </div>

    <!-- 系统简介 -->
    <div class="intro-section">
      <el-card shadow="hover" class="intro-card">
        <div class="section-title">
          <i class="el-icon-info"></i>
          <span>系统简介</span>
        </div>
        <p class="intro-text">
          Zero管理系统是一套基于 Java 17、Spring Boot 3.5、Vue 2 和 Element UI 构建的前后端分离管理平台。
          后端以 Spring Security、JWT、MyBatis、Redis、Quartz、Druid 和 Nacos 为核心支撑，前端通过 Vue Router、
          Vuex、Axios 和组件化页面完成业务交互。系统已集成用户、角色、菜单、部门、岗位、数据字典、参数配置、
          操作日志、登录日志、在线用户、系统监控、代码生成、定时任务、API谷歌验证、谷歌动态码验证、IP白名单等能力，
          适合快速搭建企业内部管理后台、运营支撑平台、CRM、OA、ERP 等 Web 管理系统。
        </p>
        <div class="intro-author">软件作者：Arthur</div>
      </el-card>
    </div>

    <!-- 主题风格预览 -->
    <div class="theme-preview-section">
      <el-card shadow="hover" class="theme-preview-card">
        <div class="section-title">
          <i class="el-icon-brush"></i>
          <span>主题风格预览</span>
        </div>
        <p class="theme-preview-desc">
          提供多种精心设计的主题风格方案，点击即可在新窗口中预览完整效果
        </p>
        <div class="theme-grid">
          <div
            v-for="theme in themeLinks"
            :key="theme.value"
            class="theme-card-item"
            @click="previewTheme(theme.value)"
          >
            <div class="theme-card-icon">
              <i :class="theme.icon"></i>
            </div>
            <div class="theme-card-content">
              <h4>{{ theme.name }}</h4>
              <el-tag :type="theme.type" size="mini" effect="plain">点击预览</el-tag>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 技术架构 -->
    <div class="tech-section">
      <div class="section-title-center">
        <i class="el-icon-cpu"></i>
        <span>技术架构</span>
      </div>
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="12" :lg="12">
          <el-card shadow="hover" class="tech-card backend-card">
            <div class="card-header">
              <i class="el-icon-guide"></i>
              <h3>后端技术栈</h3>
            </div>
            <div class="tech-list">
              <div class="tech-item">
                <el-tag effect="plain" size="medium">Spring Boot 3.5.12</el-tag>
                <span class="tech-desc">核心框架</span>
              </div>
              <div class="tech-item">
                <el-tag effect="plain" size="medium" type="success">Spring Security</el-tag>
                <span class="tech-desc">安全框架</span>
              </div>
              <div class="tech-item">
                <el-tag effect="plain" size="medium" type="warning">MyBatis + PageHelper</el-tag>
                <span class="tech-desc">数据访问与分页</span>
              </div>
              <div class="tech-item">
                <el-tag effect="plain" size="medium" type="danger">Redis</el-tag>
                <span class="tech-desc">缓存数据库</span>
              </div>
              <div class="tech-item">
                <el-tag effect="plain" size="medium" type="info">JWT</el-tag>
                <span class="tech-desc">Token认证</span>
              </div>
              <div class="tech-item">
                <el-tag effect="plain" size="medium">Druid 1.2.25</el-tag>
                <span class="tech-desc">数据库连接池</span>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="12" :lg="12">
          <el-card shadow="hover" class="tech-card frontend-card">
            <div class="card-header">
              <i class="el-icon-monitor"></i>
              <h3>前端技术栈</h3>
            </div>
            <div class="tech-list">
              <div class="tech-item">
                <el-tag effect="plain" size="medium">Vue 2.6.12</el-tag>
                <span class="tech-desc">前端框架</span>
              </div>
              <div class="tech-item">
                <el-tag effect="plain" size="medium" type="success">Element UI 2.15.x</el-tag>
                <span class="tech-desc">UI组件库</span>
              </div>
              <div class="tech-item">
                <el-tag effect="plain" size="medium" type="warning">Vuex</el-tag>
                <span class="tech-desc">状态管理</span>
              </div>
              <div class="tech-item">
                <el-tag effect="plain" size="medium" type="danger">Vue Router</el-tag>
                <span class="tech-desc">路由管理</span>
              </div>
              <div class="tech-item">
                <el-tag effect="plain" size="medium" type="info">Axios</el-tag>
                <span class="tech-desc">HTTP客户端</span>
              </div>
              <div class="tech-item">
                <el-tag effect="plain" size="medium">Sass/Webpack</el-tag>
                <span class="tech-desc">构建工具</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
        <!-- 核心特性 -->
    <div class="features-section">
      <div class="section-title-center">
        <i class="el-icon-trophy"></i>
        <span>核心特性</span>
      </div>
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="(feature, index) in features" :key="index">
          <el-card shadow="hover" class="feature-card">
            <div class="feature-icon" :style="{background: feature.color}">
              <i :class="feature.icon"></i>
            </div>
            <h4>{{ feature.title }}</h4>
            <p>{{ feature.desc }}</p>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 系统架构 -->
    <div class="architecture-section">
      <div class="section-title-center">
        <i class="el-icon-connection"></i>
        <span>系统架构</span>
      </div>
      <el-card shadow="hover" class="architecture-card">
        <div class="architecture-diagram">
          <div class="arch-layer presentation">
            <h4>表现层 (Presentation Layer)</h4>
            <div class="arch-components">
              <span class="arch-tag">Vue Components</span>
              <span class="arch-tag">Element UI</span>
              <span class="arch-tag">Responsive Design</span>
            </div>
          </div>
          <div class="arch-arrow">↓</div>
          <div class="arch-layer business">
            <h4>业务层 (Business Layer)</h4>
            <div class="arch-components">
              <span class="arch-tag">Spring Security</span>
              <span class="arch-tag">Service Logic</span>
              <span class="arch-tag">JWT Auth</span>
            </div>
          </div>
          <div class="arch-arrow">↓</div>
          <div class="arch-layer data">
            <h4>数据层 (Data Layer)</h4>
            <div class="arch-components">
              <span class="arch-tag">MyBatis</span>
              <span class="arch-tag">MySQL</span>
              <span class="arch-tag">Redis Cache</span>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 功能模块 -->
    <div class="modules-section">
      <div class="section-title-center">
        <i class="el-icon-menu"></i>
        <span>功能模块</span>
      </div>
      <el-row :gutter="15">
        <el-col :xs="12" :sm="8" :md="6" :lg="4" v-for="(module, index) in modules" :key="index">
          <div class="module-item">
            <i :class="module.icon" :style="{color: module.color}"></i>
            <span>{{ module.name }}</span>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 底部信息 -->
    <div class="footer-section">
      <el-card shadow="never" class="footer-card">
        <p class="footer-text">
          <i class="el-icon-star-off"></i>
          Zero管理框架 - 让开发更简单，让管理更高效
        </p>
        <p class="copyright">
          Copyright © 2026 Zero. All Rights Reserved.
        </p>
      </el-card>
    </div>
  </div>
</template>
<script>
export default {
  name: "Index",
  data() {
    return {
      version: "3.9.0",
      currentYear: new Date().getFullYear(),
      themeLinks: [
        {
          value: 'luxury-business',
          name: '轻奢商务风',
          type: 'primary',
          icon: 'el-icon-trophy'
        },
        {
          value: 'vibrant-tech',
          name: '活力科技风',
          type: 'primary',
          icon: 'el-icon-cpu'
        },
        {
          value: 'minimalist-future',
          name: '极简未来风',
          type: 'primary',
          icon: 'el-icon-magic-stick'
        },
        {
          value: 'warm-nature',
          name: '温馨自然风',
          type: 'primary',
          icon: 'el-icon-sunny'
        },
        {
          value: 'glassmorphism',
          name: '毛玻璃拟态风',
          type: 'primary',
          icon: 'el-icon-picture-outline'
        },
        {
          value: 'material-design',
          name: '质感设计风',
          type: 'primary',
          icon: 'el-icon-setting'
        },
        {
          value: 'dark-premium',
          name: '高端暗黑风',
          type: 'primary',
          icon: 'el-icon-moon'
        }
      ],
      features: [
        {
          icon: "el-icon-lock",
          title: "权限管理",
          desc: "基于RBAC的细粒度权限控制，支持按钮级权限",
          color: "linear-gradient(135deg, #667eea 0%, #764ba2 100%)"
        },
        {
          icon: "el-icon-document",
          title: "代码生成",
          desc: "一键生成前后端代码，支持主子表、树表结构",
          color: "linear-gradient(135deg, #f093fb 0%, #f5576c 100%)"
        },
        {
          icon: "el-icon-time",
          title: "定时任务",
          desc: "灵活的任务调度，支持Cron表达式配置",
          color: "linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)"
        },
        {
          icon: "el-icon-data-analysis",
          title: "系统监控",
          desc: "实时监控服务器状态、缓存信息、在线用户",
          color: "linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)"
        },
        {
          icon: "el-icon-files",
          title: "数据字典",
          desc: "灵活的字典管理，支持多级联动",
          color: "linear-gradient(135deg, #fa709a 0%, #fee140 100%)"
        },
        {
          icon: "el-icon-notebook-2",
          title: "操作日志",
          desc: "详细记录用户操作，支持异常日志追踪",
          color: "linear-gradient(135deg, #30cfd0 0%, #330867 100%)"
        },
        {
          icon: "el-icon-upload",
          title: "文件管理",
          desc: "支持本地存储、云存储，文件上传下载",
          color: "linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)"
        },
        {
          icon: "el-icon-setting",
          title: "参数配置",
          desc: "系统参数统一管理，支持缓存刷新",
          color: "linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%)"
        }
      ],
      modules: [
        { name: "用户管理", icon: "el-icon-user", color: "#409EFF" },
        { name: "角色管理", icon: "el-icon-s-custom", color: "#67C23A" },
        { name: "菜单管理", icon: "el-icon-menu", color: "#E6A23C" },
        { name: "部门管理", icon: "el-icon-office-building", color: "#F56C6C" },
        { name: "岗位管理", icon: "el-icon-suitcase", color: "#909399" },
        { name: "字典管理", icon: "el-icon-notebook-1", color: "#409EFF" },
        { name: "参数设置", icon: "el-icon-setting", color: "#67C23A" },
        { name: "通知公告", icon: "el-icon-bell", color: "#E6A23C" },
        { name: "操作日志", icon: "el-icon-document", color: "#F56C6C" },
        { name: "登录日志", icon: "el-icon-key", color: "#909399" },
        { name: "在线用户", icon: "el-icon-user-solid", color: "#409EFF" },
        { name: "定时任务", icon: "el-icon-alarm-clock", color: "#67C23A" },
        { name: "代码生成", icon: "el-icon-cpu", color: "#E6A23C" },
        { name: "系统接口", icon: "el-icon-link", color: "#F56C6C" },
        { name: "服务监控", icon: "el-icon-monitor", color: "#909399" },
        { name: "缓存监控", icon: "el-icon-coin", color: "#409EFF" },
        { name: "在线构建", icon: "el-icon-edit", color: "#67C23A" },
        { name: "连接池监视", icon: "el-icon-data-line", color: "#E6A23C" }
      ]
    }
  },
  methods: {
    previewTheme(themeValue) {
      // 根据主题值打开对应的预览页面
      const themeRouteMap = {
        'luxury-business': '/theme-preview-luxury',
        'vibrant-tech': '/theme-preview-tech',
        'minimalist-future': '/theme-preview-minimal',
        'warm-nature': '/theme-preview-warm',
        'glassmorphism': '/theme-preview-glass',
        'material-design': '/theme-preview-material',
        'dark-premium': '/theme-preview-darkpremium'
      };

      const route = themeRouteMap[themeValue];
      if (route) {
        // 在新标签页中打开预览页面
        const routeUrl = this.$router.resolve({ path: route });
        window.open(routeUrl.href, '_blank');
      }
    }
  }
}
</script>
<style scoped lang="scss">
.zero-home {
  padding: 0;
  background: #f0f2f5;
  min-height: 100vh;

  .hero-section {
    padding: 60px 20px;
    text-align: center;
    color: #fff;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);

    .hero-content {
      max-width: 1200px;
      margin: 0 auto;

      .main-title {
        font-size: 48px;
        font-weight: 700;
        margin-bottom: 20px;
        text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
        animation: fadeInDown 1s;
        line-height: 1;
      }

      .subtitle {
        font-size: 20px;
        margin: 0 0 30px 0;
        opacity: 0.95;
        animation: fadeInUp 1s;
        line-height: 1.8;
      }

      .version-tag {
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 20px;
        animation: fadeIn 1.5s;

        .el-tag {
          font-size: 14px;
          padding: 10px 20px;
          height: 36px;
          line-height: 16px;
          display: inline-flex;
          align-items: center;
          justify-content: center;
        }
      }
    }
  }

  .intro-section {
    padding: 40px 20px;
    max-width: 1200px;
    margin: 0 auto;

    .intro-card {
      border-radius: 12px;

      .section-title {
        display: flex;
        align-items: center;
        font-size: 24px;
        font-weight: 600;
        color: #303133;
        margin-bottom: 20px;
        line-height: 1;

        i {
          margin-right: 10px;
          color: #409EFF;
          font-size: 24px;
          line-height: 1;
          display: flex;
          align-items: center;
        }

        span {
          line-height: 1;
          display: flex;
          align-items: center;
        }
      }

      .intro-text {
        font-size: 16px;
        line-height: 2;
        color: #606266;
        text-align: justify;
      }

      .intro-author {
        margin-top: 12px;
        text-align: right;
        font-size: 15px;
        font-weight: 600;
        color: #fff;
      }
    }
  }

  .theme-preview-section {
    padding: 40px 20px;
    max-width: 1200px;
    margin: 0 auto;

    .theme-preview-card {
      border-radius: 12px;

      .section-title {
        display: flex;
        align-items: center;
        font-size: 24px;
        font-weight: 600;
        color: #303133;
        margin-bottom: 15px;
        line-height: 1;

        i {
          margin-right: 10px;
          color: #409EFF;
          font-size: 24px;
          line-height: 1;
          display: flex;
          align-items: center;
        }

        span {
          line-height: 1;
          display: flex;
          align-items: center;
        }
      }

      .theme-preview-desc {
        font-size: 14px;
        color: #909399;
        margin-bottom: 25px;
        line-height: 1.8;
      }

      .theme-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
        gap: 20px;

        .theme-card-item {
          background: #fff;
          border: 2px solid #EBEEF5;
          border-radius: 12px;
          padding: 20px;
          cursor: pointer;
          transition: all 0.3s ease;
          display: flex;
          align-items: center;
          gap: 15px;

          &:hover {
            border-color: #409EFF;
            transform: translateY(-5px);
            box-shadow: 0 8px 20px rgba(64, 158, 255, 0.15);

            .theme-card-icon {
              transform: scale(1.1);
              background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            }
          }

          .theme-card-icon {
            width: 50px;
            height: 50px;
            border-radius: 10px;
            background: linear-gradient(135deg, #E6F7FF 0%, #BAE7FF 100%);
            display: flex;
            align-items: center;
            justify-content: center;
            flex-shrink: 0;
            transition: all 0.3s ease;

            i {
              font-size: 24px;
              color: #409EFF;
            }
          }

          .theme-card-content {
            flex: 1;

            h4 {
              font-size: 15px;
              color: #303133;
              margin: 0 0 8px 0;
              font-weight: 600;
            }

            .el-tag {
              font-size: 12px;
            }
          }
        }
      }
    }
  }

  .tech-section {
    padding: 40px 20px;
    max-width: 1200px;
    margin: 0 auto;

    .tech-card {
      border-radius: 12px;
      height: 100%;
      transition: all 0.3s;

      &:hover {
        transform: translateY(-5px);
      }

      .card-header {
        display: flex;
        align-items: center;
        margin-bottom: 20px;
        padding-bottom: 15px;
        border-bottom: 2px solid #EBEEF5;

        i {
          font-size: 28px;
          margin-right: 12px;
          line-height: 1;
          display: flex;
          align-items: center;
        }

        h3 {
          margin: 0;
          font-size: 20px;
          color: #303133;
          line-height: 1;
          display: flex;
          align-items: center;
        }
      }

      &.backend-card .card-header i {
        color: #67C23A;
      }

      &.frontend-card .card-header i {
        color: #409EFF;
      }

      .tech-list {
        .tech-item {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 12px 0;
          border-bottom: 1px dashed #EBEEF5;

          &:last-child {
            border-bottom: none;
          }

          .tech-desc {
            color: #909399;
            font-size: 13px;
          }
        }
      }
    }
  }

  .features-section {
    padding: 40px 20px;
    max-width: 1200px;
    margin: 0 auto;

    .feature-card {
      border-radius: 12px;
      text-align: center;
      padding: 20px 10px;
      height: 200px;
      transition: all 0.3s;

      &:hover {
        transform: translateY(-5px);
        box-shadow: 0 12px 24px rgba(0, 0, 0, 0.15);
      }

      .feature-icon {
        width: 60px;
        height: 60px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        margin: 0 auto 15px;

        i {
          font-size: 28px;
          color: #fff;
        }
      }

      h4 {
        font-size: 16px;
        color: #303133;
        margin: 10px 0;
        line-height: 1.5;
      }

      p {
        font-size: 13px;
        color: #909399;
        line-height: 1.6;
      }
    }
  }

  .architecture-section {
    padding: 40px 20px;
    max-width: 1000px;
    margin: 0 auto;

    .architecture-card {
      border-radius: 12px;

      .architecture-diagram {
        .arch-layer {
          padding: 25px;
          border-radius: 8px;
          margin: 10px 0;

          h4 {
            margin: 0 0 15px 0;
            font-size: 18px;
            color: #fff;
            text-align: center;
          }

          .arch-components {
            display: flex;
            justify-content: center;
            gap: 15px;
            flex-wrap: wrap;

            .arch-tag {
              background: rgba(255, 255, 255, 0.2);
              padding: 6px 15px;
              border-radius: 20px;
              color: #fff;
              font-size: 13px;
              backdrop-filter: blur(10px);
            }
          }

          &.presentation {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          }

          &.business {
            background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
          }

          &.data {
            background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
          }
        }

        .arch-arrow {
          text-align: center;
          font-size: 30px;
          color: #909399;
          margin: 5px 0;
        }
      }
    }
  }

  .modules-section {
    padding: 40px 20px;
    max-width: 1200px;
    margin: 0 auto;

    .module-item {
      background: #fff;
      border-radius: 8px;
      padding: 20px;
      text-align: center;
      box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
      transition: all 0.3s;
      cursor: default;

      &:hover {
        transform: translateY(-3px);
        box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.15);
      }

      i {
        font-size: 24px;
        display: block;
        margin-bottom: 8px;
        line-height: 1;
      }

      span {
        font-size: 13px;
        color: #606266;
        line-height: 1.5;
        display: block;
      }
    }
  }

  .section-title-center {
    display: flex;
    align-items: center;
    justify-content: center;
    text-align: center;
    font-size: 32px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 40px;
    line-height: 1.5;

    i {
      margin-right: 10px;
      color: #409EFF;
      font-size: 32px;
      line-height: 1;
    }

    span {
      line-height: 1.5;
    }
  }

  .footer-section {
    padding: 40px 20px 20px;
    max-width: 1200px;
    margin: 0 auto;

    .footer-card {
      background: transparent;
      border: none;

      .footer-text {
        display: flex;
        align-items: center;
        justify-content: center;
        text-align: center;
        font-size: 16px;
        color: #303133;
        margin-bottom: 10px;
        line-height: 1.5;

        i {
          color: #ffd700;
          margin-right: 8px;
          font-size: 16px;
          line-height: 1;
        }
      }

      .copyright {
        text-align: center;
        font-size: 13px;
        color: #909399;
        margin: 0;
      }
    }
  }

  @keyframes fadeInDown {
    from {
      opacity: 0;
      transform: translateY(-20px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }

  @keyframes fadeInUp {
    from {
      opacity: 0;
      transform: translateY(20px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }

  @keyframes fadeIn {
    from {
      opacity: 0;
    }
    to {
      opacity: 1;
    }
  }
}

@media (max-width: 768px) {
  .zero-home {
    .hero-section .hero-content .main-title {
      font-size: 32px;
    }

    .section-title-center {
      font-size: 24px;
    }

    .feature-card {
      height: auto !important;
      margin-bottom: 15px;
    }
  }
}
</style>
