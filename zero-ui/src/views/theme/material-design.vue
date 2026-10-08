<template>
  <div class="theme-preview-page material-theme">
    <!-- 顶部标题栏 -->
    <div class="preview-header">
      <div class="header-content">
        <div class="title-section">
          <i class="el-icon-setting theme-icon"></i>
          <div class="title-text">
            <h1>质感设计风</h1>
            <p class="subtitle">Material Design 3.0</p>
          </div>
        </div>
        <el-button type="primary" size="medium" @click="goBack" class="back-btn">
          <i class="el-icon-back"></i>
          返回首页
        </el-button>
      </div>
    </div>

    <!-- 主预览区域 -->
    <div class="preview-layout">
      <!-- 左侧：模拟侧边栏 -->
      <div class="sidebar-preview">
        <div class="sidebar-logo">
          <i class="el-icon-s-grid"></i>
          <span>零号管理系统</span>
        </div>
        <div class="sidebar-menu">
          <div class="menu-item active">
            <i class="el-icon-s-home"></i>
            <span>系统首页</span>
          </div>
          <div class="menu-item has-submenu">
            <i class="el-icon-user"></i>
            <span>系统管理</span>
            <i class="el-icon-arrow-down submenu-arrow"></i>
          </div>
          <div class="submenu-items">
            <div class="submenu-item">
              <span>用户管理</span>
            </div>
            <div class="submenu-item active">
              <span>角色管理</span>
            </div>
            <div class="submenu-item">
              <span>菜单管理</span>
            </div>
          </div>
          <div class="menu-item">
            <i class="el-icon-setting"></i>
            <span>系统监控</span>
          </div>
          <div class="menu-item">
            <i class="el-icon-s-tools"></i>
            <span>系统工具</span>
          </div>
          <div class="menu-item">
            <i class="el-icon-s-order"></i>
            <span>日志管理</span>
          </div>
        </div>
      </div>

      <!-- 右侧：主题效果展示 -->
      <div class="content-preview">
        <div class="theme-description">
          <h2>主题特色</h2>
          <p>Google最新设计规范，动态配色系统，丰富的微交互，明确的视觉层级</p>
        </div>

        <div class="demo-section">
          <el-row :gutter="20">
            <el-col :span="8">
              <el-card shadow="hover" class="demo-card">
                <div slot="header" class="card-header">
                  <i class="el-icon-setting"></i>
                  <span>质感卡片</span>
                </div>
                <div class="card-body">
                  <p>圆润的大圆角设计</p>
                  <p>动态配色系统</p>
                  <p>明确的层级关系</p>
                </div>
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card shadow="hover" class="demo-card">
                <div slot="header" class="card-header">
                  <i class="el-icon-data-analysis"></i>
                  <span>标签展示</span>
                </div>
                <div class="card-body">
                  <el-tag type="primary" class="md-tag">主要</el-tag>
                  <el-tag type="success" class="md-tag">成功</el-tag>
                  <el-tag type="warning" class="md-tag">警告</el-tag>
                </div>
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card shadow="hover" class="demo-card">
                <div slot="header" class="card-header">
                  <i class="el-icon-star-on"></i>
                  <span>操作按钮</span>
                </div>
                <div class="card-body">
                  <el-button type="primary" size="small">主要操作</el-button>
                  <el-button type="success" size="small" plain>次要操作</el-button>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>

        <div class="color-palette">
          <h3>配色方案</h3>
          <div class="palette-grid">
            <div class="color-item">
              <div class="color-box" style="background: #6750a4;"></div>
              <span>主色调</span>
              <code>#6750a4</code>
            </div>
            <div class="color-item">
              <div class="color-box" style="background: #625b71;"></div>
              <span>次要色</span>
              <code>#625b71</code>
            </div>
            <div class="color-item">
              <div class="color-box" style="background: #f5f5f5;"></div>
              <span>背景色</span>
              <code>#f5f5f5</code>
            </div>
            <div class="color-item">
              <div class="color-box" style="background: #1c1b1f;"></div>
              <span>文字色</span>
              <code>#1c1b1f</code>
            </div>
          </div>
        </div>

        <div class="features-grid">
          <div class="feature-box">
            <i class="el-icon-medal-1"></i>
            <h4>Material 3.0</h4>
            <p>遵循Google最新设计规范</p>
          </div>
          <div class="feature-box">
            <i class="el-icon-trophy"></i>
            <h4>动态配色</h4>
            <p>智能的颜色系统和层级</p>
          </div>
          <div class="feature-box">
            <i class="el-icon-star-on"></i>
            <h4>微交互</h4>
            <p>丰富细腻的动画效果</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'MaterialDesignPreview',
  methods: {
    goBack() {
      window.close();
      if (!window.closed) {
        this.$router.push('/index');
      }
    }
  }
}
</script>

<style scoped lang="scss">
/* Material Design 3.0 质感设计风格 */
.material-theme {
  background: #f5f5f5;
  min-height: 100vh;
  padding: 0;
  margin: 0;
  font-family: 'Roboto', 'Microsoft YaHei', sans-serif;

  /* 顶部标题栏 */
  .preview-header {
    background: linear-gradient(135deg, #6750a4 0%, #7e57c2 100%);
    padding: 0;
    margin: 0;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1),
                0 4px 8px rgba(103, 80, 164, 0.2);
    position: sticky;
    top: 0;
    z-index: 100;

    .header-content {
      max-width: 1400px;
      margin: 0 auto;
      padding: 24px 32px;
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .title-section {
      display: flex;
      align-items: center;
      gap: 16px;

      .theme-icon {
        font-size: 42px;
        color: #ffffff;
        background: rgba(255, 255, 255, 0.15);
        width: 64px;
        height: 64px;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: 16px;
        box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
      }

      .title-text {
        h1 {
          margin: 0;
          font-size: 28px;
          font-weight: 500;
          color: #ffffff;
          letter-spacing: 0.5px;
        }

        .subtitle {
          margin: 4px 0 0 0;
          font-size: 14px;
          color: rgba(255, 255, 255, 0.85);
          font-weight: 300;
        }
      }
    }

    .back-btn {
      background: rgba(255, 255, 255, 0.2);
      border: 1px solid rgba(255, 255, 255, 0.3);
      color: #ffffff;
      border-radius: 20px;
      padding: 10px 24px;
      font-size: 15px;
      font-weight: 500;
      backdrop-filter: blur(10px);
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

      &:hover {
        background: rgba(255, 255, 255, 0.3);
        border-color: rgba(255, 255, 255, 0.5);
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
      }

      i {
        margin-right: 6px;
      }
    }
  }

  /* 主预览布局 */
  .preview-layout {
    max-width: 1400px;
    margin: 32px auto;
    padding: 0 32px;
    display: flex;
    gap: 24px;
    align-items: flex-start;
  }

  /* 左侧模拟侧边栏 */
  .sidebar-preview {
    width: 240px;
    background: #ffffff;
    border-radius: 16px;
    padding: 16px 0;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.3),
                0 1px 3px 1px rgba(0, 0, 0, 0.15);
    position: sticky;
    top: 120px;
    flex-shrink: 0;

    .sidebar-logo {
      display: flex;
      align-items: center;
      padding: 12px 20px 20px;
      margin-bottom: 8px;
      border-bottom: 1px solid #e5e5e5;

      i {
        font-size: 28px;
        color: #6750a4;
        margin-right: 12px;
      }

      span {
        font-size: 17px;
        font-weight: 500;
        color: #1c1b1f;
        letter-spacing: 0.3px;
      }
    }

    .sidebar-menu {
      padding: 8px 0;

      .menu-item {
        display: flex;
        align-items: center;
        padding: 12px 20px;
        margin: 4px 12px;
        cursor: pointer;
        border-radius: 28px;
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        color: #49454f;
        font-size: 14px;
        font-weight: 500;

        i {
          font-size: 18px;
          margin-right: 12px;
          width: 20px;
          text-align: center;
        }

        .submenu-arrow {
          margin-left: auto;
          margin-right: 0;
          font-size: 12px;
          transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        }

        &:hover:not(.active) {
          background: #f5f5f5;
        }

        &.active {
          background: #eaddff;
          color: #21005d;
          box-shadow: 0 1px 3px rgba(103, 80, 164, 0.2);

          i {
            color: #6750a4;
          }
        }
      }

      .submenu-items {
        padding-left: 20px;
        margin-top: 4px;

        .submenu-item {
          padding: 10px 20px 10px 32px;
          margin: 2px 12px;
          cursor: pointer;
          border-radius: 28px;
          transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
          color: #49454f;
          font-size: 13px;
          font-weight: 400;

          &:hover:not(.active) {
            background: #f5f5f5;
          }

          &.active {
            background: #d0bcff;
            color: #21005d;
          }
        }
      }
    }
  }

  /* 右侧内容预览 */
  .content-preview {
    flex: 1;
    min-width: 0;
  }

  /* 主题描述卡片 */
  .theme-description {
    background: #ffffff;
    padding: 32px;
    border-radius: 20px;
    margin-bottom: 24px;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.3),
                0 1px 3px 1px rgba(0, 0, 0, 0.15);

    h2 {
      font-size: 24px;
      font-weight: 500;
      color: #1c1b1f;
      margin: 0 0 12px 0;
    }

    p {
      font-size: 15px;
      color: #49454f;
      line-height: 1.6;
      margin: 0;
    }
  }

  /* 组件演示区域 */
  .demo-section {
    margin-bottom: 24px;

    .demo-card {
      background: #ffffff;
      border-radius: 16px;
      box-shadow: 0 1px 2px rgba(0, 0, 0, 0.3),
                  0 1px 3px 1px rgba(0, 0, 0, 0.15);
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      border: none;

      &:hover {
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.3),
                    0 4px 8px 3px rgba(0, 0, 0, 0.15);
        transform: translateY(-4px);
      }

      .card-header {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 16px;
        font-weight: 500;
        color: #1c1b1f;

        i {
          color: #6750a4;
          font-size: 18px;
        }
      }

      .card-body {
        p {
          margin: 8px 0;
          color: #49454f;
          font-size: 14px;
        }

        .el-button {
          border-radius: 20px;
          padding: 10px 24px;
          font-weight: 500;
          transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
          margin-right: 8px;
          margin-bottom: 8px;
        }

        .el-tag {
          border-radius: 8px;
          padding: 6px 12px;
          border: none;
          margin-right: 8px;
        }
      }
    }
  }

  /* 配色方案 */
  .color-palette {
    background: #ffffff;
    padding: 32px;
    border-radius: 20px;
    margin-bottom: 24px;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.3),
                0 1px 3px 1px rgba(0, 0, 0, 0.15);

    h3 {
      font-size: 20px;
      font-weight: 500;
      color: #1c1b1f;
      margin: 0 0 24px 0;
    }

    .palette-grid {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 16px;

      .color-item {
        text-align: center;

        .color-box {
          width: 100%;
          height: 100px;
          border-radius: 16px;
          margin-bottom: 12px;
          box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
          transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

          &:hover {
            transform: scale(1.05);
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.25);
          }
        }

        span {
          display: block;
          font-size: 14px;
          font-weight: 500;
          color: #1c1b1f;
          margin-bottom: 4px;
        }

        code {
          font-size: 12px;
          color: #79747e;
          font-family: 'Consolas', monospace;
        }
      }
    }
  }

  /* 核心特性网格 */
  .features-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 20px;

    .feature-box {
      background: #ffffff;
      padding: 28px;
      border-radius: 16px;
      text-align: center;
      box-shadow: 0 1px 2px rgba(0, 0, 0, 0.3),
                  0 1px 3px 1px rgba(0, 0, 0, 0.15);
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

      &:hover {
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.3),
                    0 4px 8px 3px rgba(0, 0, 0, 0.15);
        transform: translateY(-4px);
      }

      i {
        font-size: 48px;
        color: #6750a4;
        margin-bottom: 16px;
        display: block;
      }

      h4 {
        font-size: 16px;
        font-weight: 500;
        color: #1c1b1f;
        margin: 0 0 8px 0;
      }

      p {
        font-size: 13px;
        color: #49454f;
        line-height: 1.5;
        margin: 0;
      }
    }
  }

  /* 响应式设计 */
  @media (max-width: 1200px) {
    .preview-layout {
      flex-direction: column;
    }

    .sidebar-preview {
      width: 100% !important;
      position: relative;
      top: 0;
    }

    .demo-section .el-row {
      .el-col {
        width: 100% !important;
        margin-bottom: 16px;
      }
    }

    .features-grid {
      grid-template-columns: repeat(2, 1fr);
    }

    .color-palette .palette-grid {
      grid-template-columns: repeat(2, 1fr);
    }
  }

  @media (max-width: 768px) {
    .preview-header .header-content {
      flex-direction: column;
      gap: 16px;
      text-align: center;

      .title-section {
        flex-direction: column;
        text-align: center;
      }
    }

    .features-grid,
    .color-palette .palette-grid {
      grid-template-columns: 1fr;
    }
  }
}
</style>
