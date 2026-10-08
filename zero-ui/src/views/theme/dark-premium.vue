<template>
  <div class="theme-preview-page dark-premium-theme">
    <!-- 顶部标题栏 -->
    <div class="preview-header">
      <div class="header-content">
        <div class="title-section">
          <i class="el-icon-moon-night theme-icon"></i>
          <div class="title-text">
            <h1>高端暗黑风</h1>
            <p class="subtitle">Dark Mode Premium - OLED Optimized</p>
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
      <!-- 左侧:模拟侧边栏 -->
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
          <div class="menu-item has-submenu expanded">
            <i class="el-icon-user"></i>
            <span>系统管理</span>
            <i class="el-icon-arrow-down submenu-arrow"></i>
          </div>
          <div class="submenu">
            <div class="submenu-item">
              <i class="el-icon-user-solid"></i>
              <span>用户管理</span>
            </div>
            <div class="submenu-item active">
              <i class="el-icon-s-custom"></i>
              <span>角色管理</span>
            </div>
            <div class="submenu-item">
              <i class="el-icon-menu"></i>
              <span>菜单管理</span>
            </div>
          </div>
          <div class="menu-item">
            <i class="el-icon-monitor"></i>
            <span>系统监控</span>
          </div>
          <div class="menu-item">
            <i class="el-icon-s-tools"></i>
            <span>系统工具</span>
          </div>
          <div class="menu-item">
            <i class="el-icon-document"></i>
            <span>日志管理</span>
          </div>
        </div>
      </div>

      <!-- 右侧:主题效果展示 -->
      <div class="content-preview">
        <!-- 主题描述 -->
        <div class="theme-description">
          <h2>
            <i class="el-icon-star-on"></i>
            主题特色
          </h2>
          <p>真黑背景,香槟金点缀,柔和发光效果,OLED友好的高端暗黑体验</p>
        </div>

        <!-- 组件演示 -->
        <div class="components-demo">
          <div class="demo-card">
            <h3>
              <i class="el-icon-trophy"></i>
              按钮组件
            </h3>
            <el-button type="primary" class="premium-btn">香槟金按钮</el-button>
            <el-button type="info" class="premium-btn">铂金银按钮</el-button>
          </div>

          <div class="demo-card">
            <h3>
              <i class="el-icon-collection-tag"></i>
              标签组件
            </h3>
            <el-tag class="premium-tag gold">金色标签</el-tag>
            <el-tag class="premium-tag silver">银色标签</el-tag>
            <el-tag class="premium-tag emerald">绿宝石</el-tag>
          </div>

          <div class="demo-card">
            <h3>
              <i class="el-icon-edit"></i>
              输入框
            </h3>
            <el-input placeholder="请输入内容" class="premium-input"></el-input>
          </div>
        </div>

        <!-- 配色方案 -->
        <div class="color-scheme">
          <h2>
            <i class="el-icon-brush"></i>
            配色方案
          </h2>
          <div class="color-palette">
            <div class="color-item">
              <div class="color-block" style="background: #000000;">
                <span style="color: #d4af37;">真黑</span>
              </div>
              <div class="color-name">主背景</div>
              <div class="color-value">#000000</div>
            </div>
            <div class="color-item">
              <div class="color-block" style="background: #18181b;">
                <span style="color: #d4af37;">炭灰</span>
              </div>
              <div class="color-name">卡片背景</div>
              <div class="color-value">#18181b</div>
            </div>
            <div class="color-item">
              <div class="color-block" style="background: #d4af37;">
                <span style="color: #000000;">香槟金</span>
              </div>
              <div class="color-name">主色调</div>
              <div class="color-value">#d4af37</div>
            </div>
            <div class="color-item">
              <div class="color-block" style="background: #c0c0c0;">
                <span style="color: #000000;">铂金银</span>
              </div>
              <div class="color-name">次要色</div>
              <div class="color-value">#c0c0c0</div>
            </div>
          </div>
        </div>

        <!-- 核心特性 -->
        <div class="features-grid">
          <div class="feature-card">
            <div class="feature-icon">
              <i class="el-icon-moon-night"></i>
            </div>
            <h3>OLED优化</h3>
            <p>真黑背景,省电护眼,完美适配OLED屏幕</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">
              <i class="el-icon-trophy"></i>
            </div>
            <h3>奢华质感</h3>
            <p>香槟金与铂金银点缀,彰显高端品质</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">
              <i class="el-icon-magic-stick"></i>
            </div>
            <h3>柔和发光</h3>
            <p>精致的光晕效果,营造沉浸式体验</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'DarkPremiumPreview',
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
/* 高端暗黑风 - Dark Mode Premium */
.dark-premium-theme {
  background: #000000;
  min-height: 100vh;
  padding: 0;
  margin: 0;
  font-family: 'SF Pro Display', 'Microsoft YaHei', sans-serif;

  /* 顶部标题栏 */
  .preview-header {
    background: linear-gradient(135deg, #18181b 0%, #000000 100%);
    padding: 0;
    margin: 0;
    border-bottom: 1px solid #27272a;
    box-shadow: 0 4px 20px rgba(212, 175, 55, 0.15);
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
        background: linear-gradient(135deg, #d4af37 0%, #f5d76e 100%);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        width: 64px;
        height: 64px;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: 12px;
        border: 1px solid #d4af37;
        box-shadow: 0 0 30px rgba(212, 175, 55, 0.3);
      }

      .title-text {
        h1 {
          margin: 0;
          font-size: 28px;
          font-weight: 600;
          background: linear-gradient(135deg, #d4af37 0%, #f5d76e 100%);
          -webkit-background-clip: text;
          -webkit-text-fill-color: transparent;
          letter-spacing: 1px;
        }

        .subtitle {
          margin: 4px 0 0 0;
          font-size: 13px;
          color: #a1a1aa;
          font-weight: 400;
          letter-spacing: 0.5px;
        }
      }
    }

    .back-btn {
      background: linear-gradient(135deg, #d4af37 0%, #c0a030 100%);
      border: 1px solid #d4af37;
      color: #000000;
      border-radius: 8px;
      padding: 10px 24px;
      font-size: 15px;
      font-weight: 600;
      box-shadow: 0 0 20px rgba(212, 175, 55, 0.4);
      transition: all 0.3s ease;

      &:hover {
        background: linear-gradient(135deg, #f5d76e 0%, #d4af37 100%);
        box-shadow: 0 0 35px rgba(212, 175, 55, 0.6);
        transform: translateY(-2px);
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
    background: #18181b;
    border-radius: 12px;
    padding: 16px 0;
    border: 1px solid #27272a;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.5);
    position: sticky;
    top: 120px;
    flex-shrink: 0;

    .sidebar-logo {
      display: flex;
      align-items: center;
      padding: 12px 20px 20px;
      margin-bottom: 12px;
      border-bottom: 1px solid #27272a;

      i {
        font-size: 28px;
        background: linear-gradient(135deg, #d4af37 0%, #f5d76e 100%);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        margin-right: 12px;
      }

      span {
        font-size: 17px;
        font-weight: 600;
        background: linear-gradient(135deg, #e4e4e7 0%, #a1a1aa 100%);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        letter-spacing: 0.5px;
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
        border-radius: 8px;
        transition: all 0.3s ease;
        color: #a1a1aa;
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
          transition: transform 0.3s ease;
        }

        &:hover:not(.active) {
          background: #27272a;
          color: #e4e4e7;
        }

        &.active {
          background: linear-gradient(135deg, rgba(212, 175, 55, 0.2) 0%, rgba(212, 175, 55, 0.1) 100%);
          color: #d4af37;
          border: 1px solid rgba(212, 175, 55, 0.3);
          box-shadow: 0 0 20px rgba(212, 175, 55, 0.2);

          i {
            color: #d4af37;
            filter: drop-shadow(0 0 8px rgba(212, 175, 55, 0.6));
          }
        }

        &.has-submenu.expanded {
          .submenu-arrow {
            transform: rotate(180deg);
          }
        }
      }

      .submenu {
        padding-left: 20px;
        margin-top: 4px;

        .submenu-item {
          display: flex;
          align-items: center;
          padding: 10px 20px 10px 32px;
          margin: 2px 12px;
          cursor: pointer;
          border-radius: 8px;
          transition: all 0.3s ease;
          color: #71717a;
          font-size: 13px;
          font-weight: 400;

          i {
            font-size: 16px;
            margin-right: 12px;
            width: 16px;
            text-align: center;
          }

          &:hover:not(.active) {
            background: #27272a;
            color: #a1a1aa;
          }

          &.active {
            background: rgba(192, 192, 192, 0.15);
            color: #c0c0c0;
            border: 1px solid rgba(192, 192, 192, 0.3);

            i {
              color: #c0c0c0;
            }
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
    background: #18181b;
    padding: 32px;
    border-radius: 12px;
    margin-bottom: 24px;
    border: 1px solid #27272a;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.5);

    h2 {
      font-size: 24px;
      font-weight: 600;
      background: linear-gradient(135deg, #d4af37 0%, #f5d76e 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      margin: 0 0 12px 0;
      display: flex;
      align-items: center;
      gap: 12px;

      i {
        background: linear-gradient(135deg, #d4af37 0%, #f5d76e 100%);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        font-size: 26px;
        filter: drop-shadow(0 0 10px rgba(212, 175, 55, 0.5));
      }
    }

    p {
      font-size: 15px;
      color: #a1a1aa;
      line-height: 1.6;
      margin: 0;
    }
  }

  /* 组件演示网格 */
  .components-demo {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 20px;
    margin-bottom: 24px;

    .demo-card {
      background: #18181b;
      padding: 24px;
      border-radius: 12px;
      border: 1px solid #27272a;
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.5);
      transition: all 0.3s ease;

      &:hover {
        border-color: #d4af37;
        box-shadow: 0 8px 30px rgba(212, 175, 55, 0.2);
        transform: translateY(-4px);
      }

      h3 {
        font-size: 16px;
        font-weight: 600;
        color: #e4e4e7;
        margin: 0 0 16px 0;
        display: flex;
        align-items: center;
        gap: 8px;

        i {
          color: #d4af37;
          filter: drop-shadow(0 0 8px rgba(212, 175, 55, 0.5));
        }
      }

      .premium-btn {
        border-radius: 8px;
        padding: 10px 24px;
        font-weight: 600;
        transition: all 0.3s ease;
        margin-right: 8px;
        margin-bottom: 8px;

        &.el-button--primary {
          background: linear-gradient(135deg, #d4af37 0%, #c0a030 100%);
          border: 1px solid #d4af37;
          color: #000000;
          box-shadow: 0 0 20px rgba(212, 175, 55, 0.4);

          &:hover {
            background: linear-gradient(135deg, #f5d76e 0%, #d4af37 100%);
            box-shadow: 0 0 30px rgba(212, 175, 55, 0.6);
          }
        }

        &.el-button--info {
          background: linear-gradient(135deg, #c0c0c0 0%, #a8a8a8 100%);
          border: 1px solid #c0c0c0;
          color: #000000;
          box-shadow: 0 0 20px rgba(192, 192, 192, 0.3);

          &:hover {
            background: linear-gradient(135deg, #e0e0e0 0%, #c0c0c0 100%);
            box-shadow: 0 0 30px rgba(192, 192, 192, 0.5);
          }
        }
      }

      .premium-tag {
        border-radius: 6px;
        padding: 6px 14px;
        margin-right: 8px;
        margin-bottom: 8px;
        font-weight: 500;
        border: none;

        &.gold {
          background: rgba(212, 175, 55, 0.15);
          color: #d4af37;
          border: 1px solid rgba(212, 175, 55, 0.3);
          box-shadow: 0 0 15px rgba(212, 175, 55, 0.2);
        }

        &.silver {
          background: rgba(192, 192, 192, 0.15);
          color: #c0c0c0;
          border: 1px solid rgba(192, 192, 192, 0.3);
          box-shadow: 0 0 15px rgba(192, 192, 192, 0.2);
        }

        &.emerald {
          background: rgba(80, 200, 120, 0.15);
          color: #50c878;
          border: 1px solid rgba(80, 200, 120, 0.3);
          box-shadow: 0 0 15px rgba(80, 200, 120, 0.2);
        }
      }

      .premium-input {
        ::v-deep .el-input__inner {
          background: #000000;
          border: 1px solid #3f3f46;
          color: #e4e4e7;
          border-radius: 8px;
          padding: 12px 16px;
          transition: all 0.3s ease;

          &:focus {
            border-color: #d4af37;
            box-shadow: 0 0 20px rgba(212, 175, 55, 0.3);
          }

          &::placeholder {
            color: #52525b;
          }
        }
      }
    }
  }

  /* 配色方案 */
  .color-scheme {
    background: #18181b;
    padding: 32px;
    border-radius: 12px;
    margin-bottom: 24px;
    border: 1px solid #27272a;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.5);

    h2 {
      font-size: 20px;
      font-weight: 600;
      background: linear-gradient(135deg, #d4af37 0%, #f5d76e 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      margin: 0 0 24px 0;
      display: flex;
      align-items: center;
      gap: 12px;

      i {
        background: linear-gradient(135deg, #d4af37 0%, #f5d76e 100%);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        filter: drop-shadow(0 0 10px rgba(212, 175, 55, 0.5));
      }
    }

    .color-palette {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 16px;

      .color-item {
        text-align: center;

        .color-block {
          width: 100%;
          height: 100px;
          border-radius: 12px;
          margin-bottom: 12px;
          border: 1px solid #3f3f46;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 16px;
          font-weight: 600;
          transition: all 0.3s ease;
          box-shadow: 0 4px 15px rgba(0, 0, 0, 0.3);

          &:hover {
            transform: scale(1.05);
            box-shadow: 0 8px 25px rgba(212, 175, 55, 0.3);
          }
        }

        .color-name {
          font-size: 14px;
          font-weight: 500;
          color: #e4e4e7;
          margin-bottom: 4px;
        }

        .color-value {
          font-size: 12px;
          color: #71717a;
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

    .feature-card {
      background: #18181b;
      padding: 28px;
      border-radius: 12px;
      text-align: center;
      border: 1px solid #27272a;
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.5);
      transition: all 0.3s ease;

      &:hover {
        border-color: #d4af37;
        box-shadow: 0 8px 30px rgba(212, 175, 55, 0.2);
        transform: translateY(-4px);
      }

      .feature-icon {
        width: 64px;
        height: 64px;
        background: linear-gradient(135deg, rgba(212, 175, 55, 0.2) 0%, rgba(212, 175, 55, 0.1) 100%);
        border-radius: 12px;
        border: 1px solid rgba(212, 175, 55, 0.3);
        display: flex;
        align-items: center;
        justify-content: center;
        margin: 0 auto 16px;
        box-shadow: 0 0 30px rgba(212, 175, 55, 0.3);

        i {
          font-size: 28px;
          background: linear-gradient(135deg, #d4af37 0%, #f5d76e 100%);
          -webkit-background-clip: text;
          -webkit-text-fill-color: transparent;
          filter: drop-shadow(0 0 10px rgba(212, 175, 55, 0.6));
        }
      }

      h3 {
        font-size: 16px;
        font-weight: 600;
        color: #e4e4e7;
        margin: 0 0 8px 0;
      }

      p {
        font-size: 13px;
        color: #a1a1aa;
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

    .components-demo,
    .features-grid {
      grid-template-columns: repeat(2, 1fr);
    }

    .color-palette {
      grid-template-columns: repeat(2, 1fr) !important;
    }
  }

  @media (max-width: 768px) {
    .preview-header .header-content {
      flex-direction: column;
      gap: 16px;
      text-align: center;
    }

    .components-demo,
    .features-grid,
    .color-palette {
      grid-template-columns: 1fr !important;
    }
  }
}
</style>
