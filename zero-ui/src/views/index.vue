<template>
  <main class="zero-home">
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
          <AppIcon icon-class="el-icon-info" class=""></AppIcon>
          <span>系统简介</span>
        </div>
        <p class="intro-text">
          Zero 管理系统是一套面向企业内部管理和运营支撑的前后端分离平台，可作为 CRM、OA、ERP 等 Web 后台的开发基础。
          后端采用 Java 17、Spring Boot 3.5 和 Maven 多模块架构，使用 Nacos 注册发现与配置、Spring Security 和 JWT 认证，
          结合 MySQL、MyBatis、MyBatis-Plus、PageHelper、Druid 和 Redis 处理数据与缓存。
          前端采用 Vue 3、Element Plus 和 Vite，通过 Vue Router、Vuex 和 Axios 管理路由、状态及接口请求，支持多主题切换。
        </p>
        <p class="intro-text">
          <strong>组织、权限与业务管理</strong>涵盖用户、角色、菜单、部门和岗位维护，支持按钮权限、数据范围、
          登录 IP 白名单、谷歌动态码绑定与登录验证、按 API 配置二次验证，以及登录日志和操作日志。
          管理后台统一提供系统字典、后台参数和业务参数入口，报表服务负责业务字典与所属业务数据的查询和维护。
          个人中心支持维护资料、更换头像和修改密码。
        </p>
        <p class="intro-text">
          <strong>协作与文件管理</strong>支持通知公告维护、通过 WebSocket 向在线用户发送系统通知，
          以及可设为公开或私有的加密备忘录。独立文件服务管理文件及其记录，可接入本地磁盘和已适配的云存储平台。
        </p>
        <p class="intro-text">
          <strong>任务、监控与开发工具</strong>提供后台 Quartz 定时任务及独立的 XXL-JOB 调度中心和执行服务。
          运维功能覆盖运行统计、在线会话、实时日志、服务器资源、Redis 缓存与 Druid 连接池，并通过 Spring Boot Admin
          和 Actuator 查看微服务状态。开发工具包含拖拽式表单构建、单表／树表／主子表代码生成、Swagger 接口调试，
          以及任务模块的 MyBatis-Plus 开发期代码生成器。
        </p>
        <div class="intro-author">软件作者：Arthur</div>
      </el-card>
    </div>

    <div class="home-content">
      <section id="home-capabilities" aria-labelledby="home-capabilities-title">
        <h2 id="home-capabilities-title">项目亮点与功能</h2>
        <div class="home-grid">
          <article v-for="feature in features" :key="feature.title" class="home-panel home-feature">
            <span class="home-icon" aria-hidden="true"><AppIcon :icon-class="feature.icon" /></span>
            <div>
              <h3>{{ feature.title }}</h3>
              <p>{{ feature.description }}</p>
            </div>
          </article>
        </div>
      </section>

      <section aria-labelledby="home-flow-title">
        <h2 id="home-flow-title">项目架构</h2>
        <dl class="home-flow home-panel">
          <div v-for="flow in flows" :key="flow.label">
            <dt><AppIcon :icon-class="flow.icon" aria-hidden="true" />{{ flow.label }}</dt>
            <dd>
              <template v-for="(node, index) in flow.nodes" :key="node">
                <span v-if="index" class="home-flow-arrow">→</span>
                <code>{{ node }}</code>
              </template>
            </dd>
          </div>
        </dl>
        <h3 class="home-modules-title">项目模块</h3>
        <dl class="home-grid home-module-grid home-details">
          <div v-for="module in modules" :key="module.name" class="home-panel">
            <dt><code>{{ module.name }}</code></dt>
            <dd>{{ module.description }}</dd>
          </div>
        </dl>
      </section>

      <section id="home-stack" aria-labelledby="home-stack-title">
        <h2 id="home-stack-title">技术框架</h2>
        <dl class="home-grid home-details home-stack home-panel">
          <div v-for="item in stack" :key="item.title">
            <dt>{{ item.title }}</dt>
            <dd>{{ item.description }}</dd>
          </div>
        </dl>
      </section>

      <section aria-labelledby="home-themes-title">
        <h2 id="home-themes-title">主题风格预览</h2>
        <div class="home-themes home-panel">
          <a
            v-for="theme in themeLinks"
            :key="theme.name"
            class="home-panel home-theme-card"
            :href="$router.resolve(theme.path).href"
            target="_blank"
            rel="noopener noreferrer"
            :aria-label="'在新标签页预览' + theme.name"
          >
            <span class="home-theme-sample" :style="theme.palette" aria-hidden="true">
              <span/><span/><span/>
            </span>
            <span class="home-theme-caption">
              <AppIcon :icon-class="theme.icon" aria-hidden="true" />
              {{ theme.name }}
            </span>
          </a>
        </div>
      </section>

    </div>
  </main>
</template>

<script>
import { version } from '../../package.json'

export default {
  name: "Index",
  data() {
    return {
      version,
      flows: [
        { label: '后台业务', icon: 'el-icon-menu', nodes: ['zero-ui', 'zero-admin', 'zero-report / zero-file'] },
        { label: '任务调度', icon: 'el-icon-alarm-clock', nodes: ['zero-job-admin', 'zero-task'] },
        { label: '运行监控', icon: 'el-icon-monitor', nodes: ['zero-boot-admin', 'Actuator'] }
      ],
      features: [
        { icon: 'el-icon-lock', title: '组织权限与安全审计', description: '管理组织和角色权限，支持用户导入导出、登录白名单、谷歌验证与操作审计。' },
        { icon: 'el-icon-files', title: '报表与参数管理', description: '统一维护系统字典、后台参数和业务字典，业务数据由报表服务管理。' },
        { icon: 'el-icon-upload', title: '多平台文件存储', description: '统一上传、查询与删除文件，支持本地磁盘和已适配的云存储平台。' },
        { icon: 'el-icon-bell', title: '公告、实时通知与备忘录', description: '维护公告，向指定或全部在线账号发送通知，按可见范围管理加密备忘录。' },
        { icon: 'el-icon-alarm-clock', title: '任务调度与运行监控', description: '管理定时任务和执行记录，查看运行统计、实时日志、资源和服务实例。' },
        { icon: 'el-icon-cpu', title: '表单构建与开发工具', description: '提供拖拽表单、单表／树表／主子表代码生成和 Swagger 接口调试。' }
      ],
      stack: [
        { title: '管理后台前端', description: 'Vue 3、Element Plus、Vue Router、Vuex、Axios、Vite 与 ECharts。' },
        { title: 'Java 后端', description: 'Java 17、Spring Boot 3.5、Spring Cloud 与 Maven 多模块。' },
        { title: '服务发现与调用', description: 'Nacos 提供注册发现与配置。业务代理使用负载均衡 RestTemplate，报表探活使用 OpenFeign。' },
        { title: '认证与实时通信', description: 'Spring Security、JWT、WebSocket 与 Redis Pub/Sub。' },
        { title: '数据访问与缓存', description: 'MySQL、Druid、MyBatis、PageHelper、MyBatis-Plus、动态数据源、Redis 与 Redisson。' },
        { title: '调度、监控与接口文档', description: 'Quartz、XXL-JOB、Spring Boot Admin、Actuator、Logback 与 SpringDoc。' }
      ],
      modules: [
        { name: 'zero-ui', description: '管理界面与交互，业务请求统一发给 zero-admin。' },
        { name: 'zero-admin', description: '管理后台统一入口，负责认证、权限、审计和后台自身业务。' },
        { name: 'zero-report', description: '报表及所属业务数据的查询、管理和持久化。' },
        { name: 'zero-file', description: '独立文件服务，管理存储对象和文件记录。' },
        { name: 'zero-jobAdmin', description: 'XXL-JOB 调度中心，管理任务、执行器及执行结果。' },
        { name: 'zero-task', description: '接收调度并运行任务处理器，回传执行结果。' },
        { name: 'zero-bootAdmin', description: '独立监控服务，通过 Actuator 查看服务运行状态。' },
        { name: 'zero-tools', description: '跨服务复用的常量、统一返回对象和通用工具。' }
      ],
      themeLinks: [
        { path: '/theme-preview-glass', name: '毛玻璃拟态风', icon: 'el-icon-picture-outline', palette: { '--sample-bg': 'linear-gradient(135deg, #343e8d, #833cdb)', '--sample-side': '#514e9e', '--sample-accent': '#d4c2ff' } },
        { path: '/theme-preview-luxury', name: '轻奢商务风', icon: 'el-icon-trophy', palette: { '--sample-bg': '#1a1f2e', '--sample-side': '#292f40', '--sample-accent': '#d4af37' } },
        { path: '/theme-preview-tech', name: '活力科技风', icon: 'el-icon-cpu', palette: { '--sample-bg': '#141a35', '--sample-side': '#222958', '--sample-accent': '#00f2fe' } },
        { path: '/theme-preview-minimal', name: '极简未来风', icon: 'el-icon-magic-stick', palette: { '--sample-bg': '#f5fbfd', '--sample-side': '#d9eef3', '--sample-accent': '#00a6ba' } },
        { path: '/theme-preview-warm', name: '温馨自然风', icon: 'el-icon-sunny', palette: { '--sample-bg': '#faf7ee', '--sample-side': '#e9ddc8', '--sample-accent': '#8b6f47' } },
        { path: '/theme-preview-material', name: '质感设计风', icon: 'el-icon-setting', palette: { '--sample-bg': '#f6f1fc', '--sample-side': '#e7dbf7', '--sample-accent': '#7e57c2' } },
        { path: '/theme-preview-darkpremium', name: '高端暗黑风', icon: 'el-icon-moon', palette: { '--sample-bg': '#18181b', '--sample-side': '#27272a', '--sample-accent': '#d4af37' } },
        { path: '/theme-preview-default', name: '原始风格', icon: 'el-icon-monitor', palette: { '--sample-bg': '#f0f2f5', '--sample-side': '#304156', '--sample-accent': '#409eff' } }
      ]
    }
  }
}
</script>

<style scoped lang="scss">
.zero-home {
  --home-panel: var(--zero-fixed-table-panel-bg);
  --home-text: var(--zero-fixed-table-text);
  --home-title: var(--zero-fixed-table-header-text, var(--home-text));
  --home-border: var(--zero-fixed-table-border);
  --home-tint: var(--zero-fixed-table-row-hover-bg);
  color: var(--home-text);
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
        color: var(--home-title);
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
        color: var(--home-text);
        text-align: start;
        overflow-wrap: anywhere;
        margin: 0 0 12px;

        strong {
          margin-inline-end: 0.35em;
          font-weight: 650;
        }
      }

      .intro-author {
        margin-top: 12px;
        text-align: right;
        font-size: 15px;
        font-weight: 600;
        color: var(--home-title);
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

.home-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px 32px;
  display: grid;
  gap: clamp(22px, 2.4vw, 34px);
  container-type: inline-size;
}

.home-content section {
  min-width: 0;
  scroll-margin-top: 20px;
}

.home-content :is(h1, h2, h3, dt) {
  color: var(--home-title);
}

.home-content h2 {
  margin: 0 0 12px;
  font-size: 19px;
  font-weight: 650;
  line-height: 1.4;
}

.home-content h2::before {
  content: '';
  display: inline-block;
  width: 4px;
  height: 1em;
  margin-inline-end: 9px;
  vertical-align: -0.13em;
  border-radius: 4px;
  background: currentColor;
}

.home-panel {
  background: var(--home-panel);
  border: 1px solid var(--home-border);
  border-radius: 12px;
}

.home-flow,
.home-details {
  margin: 0;
}

.home-flow {
  padding: 6px 18px;
}

.home-flow > div {
  display: grid;
  grid-template-columns: minmax(115px, 0.23fr) minmax(0, 1fr);
  align-items: center;
  gap: 10px 16px;
  padding: 12px 0;
}

.home-flow > div + div {
  border-top: 1px solid var(--home-border);
}

.home-flow dt,
.home-flow dd {
  display: flex;
  align-items: center;
  gap: 8px;
}

.home-flow dd {
  flex-wrap: wrap;
}

.home-flow code {
  max-width: 100%;
  padding: 4px 9px;
  border: 1px solid var(--home-border);
  border-radius: 7px;
  background: var(--home-tint);
  color: var(--home-title);
}

.home-flow-arrow {
  color: var(--home-title);
}

.home-modules-title {
  margin: 16px 0 9px;
  font-size: 15px;
}

.home-flow dt,
.home-details dt {
  font-weight: 650;
}

.home-flow dd,
.home-details dd {
  margin: 0;
  overflow-wrap: anywhere;
  line-height: 1.55;
}

.home-grid,
.home-themes {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.home-grid > .home-panel {
  padding: 15px;
}

.home-feature {
  display: flex;
  align-items: flex-start;
  gap: 14px;
}

.home-feature > div {
  min-width: 0;
  overflow-wrap: anywhere;
}

.home-icon {
  display: grid;
  place-items: center;
  flex: 0 0 40px;
  height: 40px;
  border-radius: 8px;
  background: var(--home-tint);
  color: var(--home-title);
  font-size: 20px;
}

.home-feature h3 {
  margin: 0 0 5px;
  font-size: 15px;
  line-height: 1.4;
}

.home-feature p,
.home-details dd {
  margin: 4px 0 0;
  font-size: 13px;
  line-height: 1.65;
}

.home-module-grid {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.home-stack {
  gap: 0;
  padding: 6px 10px;
}

.home-stack > div {
  min-width: 0;
  padding: 10px 12px;
}

.home-themes {
  grid-template-columns: repeat(4, minmax(0, 1fr));
  padding: 10px;
  gap: 8px;
}

.home-theme-card {
  min-width: 0;
  padding: 8px;
  background: var(--home-tint);
}

.home-theme-sample {
  display: grid;
  grid-template: 8px 1fr / 30% 1fr;
  height: 44px;
  overflow: hidden;
  border-radius: 6px;
  background: var(--sample-bg);

  span:first-child {
    grid-row: span 2;
    background: var(--sample-side);
  }

  span:nth-child(2) {
    background: var(--sample-accent);
  }

  span:last-child {
    width: 58%;
    height: 5px;
    margin: 9px;
    border-radius: 3px;
    background: var(--sample-accent);
    opacity: 0.7;
  }
}

.home-theme-caption {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 7px 2px 2px;
}

.home-content a {
  color: var(--home-title);
}

.home-content a:hover {
  text-decoration: underline;
}

.home-content a:focus-visible {
  outline: 2px solid var(--home-title);
  outline-offset: 3px;
}

@container (max-width: 780px) {
  .home-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@container (max-width: 490px) {
  .home-grid {
    grid-template-columns: minmax(0, 1fr);
  }

  .home-themes {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .home-flow > div {
    grid-template-columns: minmax(0, 1fr);
  }
}

@media (max-width: 768px) {
  .zero-home .intro-section .intro-card .intro-text {
    font-size: 15px;
    line-height: 1.75;
  }

  .zero-home .hero-section .hero-content {
    .main-title {
      font-size: 32px;
      line-height: 1.2;
    }

    .subtitle {
      font-size: 16px;
    }

    .version-tag {
      flex-wrap: wrap;
      gap: 8px;
    }
  }
}
</style>
