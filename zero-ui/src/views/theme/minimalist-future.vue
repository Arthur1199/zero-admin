<template>
  <div class="theme-preview-page minimalist-future-theme">
    <!-- 顶部标题栏 -->
    <div class="preview-header">
      <div class="header-content">
        <div class="title-section">
          <AppIcon icon-class="el-icon-magic-stick" class="theme-icon"></AppIcon>
          <div class="title-text">
            <h1>极简未来风</h1>
            <p class="subtitle">Minimalist Future Theme</p>
          </div>
        </div>
        <el-button type="primary" size="default" @click="goBack" class="back-btn">
          <AppIcon icon-class="el-icon-back" class=""></AppIcon>
          返回首页
        </el-button>
      </div>
    </div>

    <!-- 主预览区域 -->
    <div class="preview-layout">
      <!-- 左侧：模拟侧边栏 -->
      <div class="sidebar-preview">
        <div class="sidebar-logo">
          <img src="@/assets/logo/zero.ico" width="24" height="24" alt="">
          <span>零号管理系统</span>
        </div>
        <div class="sidebar-menu">
          <div class="menu-item active">
            <AppIcon icon-class="el-icon-s-home" class=""></AppIcon>
            <span>系统首页</span>
          </div>
          <div class="menu-item has-submenu">
            <AppIcon icon-class="el-icon-user" class=""></AppIcon>
            <span>系统管理</span>
            <AppIcon icon-class="el-icon-arrow-down" class="submenu-arrow"></AppIcon>
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
            <AppIcon icon-class="el-icon-setting" class=""></AppIcon>
            <span>系统监控</span>
          </div>
          <div class="menu-item">
            <AppIcon icon-class="el-icon-s-tools" class=""></AppIcon>
            <span>系统工具</span>
          </div>
          <div class="menu-item">
            <AppIcon icon-class="el-icon-s-order" class=""></AppIcon>
            <span>日志管理</span>
          </div>
        </div>
      </div>

      <!-- 右侧：主题效果展示 -->
      <div class="content-preview">
        <div class="theme-description">
          <h2>主题特色</h2>
          <p>冷白空间搭配青蓝强调色，轻量边框与柔和光影，让后台界面保持清爽、明亮和科技感</p>
        </div>

        <div class="demo-section">
          <el-row :gutter="20">
            <el-col :span="8">
              <el-card shadow="hover" class="demo-card">
                <template #header><div  class="card-header">
                  <AppIcon icon-class="el-icon-magic-stick" class=""></AppIcon>
                  <span>轻量卡片</span>
                </div></template>
                <div class="card-body">
                  <p>冷白背景降低视觉负担</p>
                  <p>青蓝边框强化当前焦点</p>
                  <p>柔和阴影保留层次感</p>
                </div>
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card shadow="hover" class="demo-card">
                <template #header><div  class="card-header">
                  <AppIcon icon-class="el-icon-data-analysis" class=""></AppIcon>
                  <span>标签展示</span>
                </div></template>
                <div class="card-body">
                  <el-tag type="info" effect="plain">冷白</el-tag>
                  <el-tag type="info" effect="plain">青蓝</el-tag>
                  <el-tag type="info" effect="plain">轻盈</el-tag>
                </div>
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card shadow="hover" class="demo-card">
                <template #header><div  class="card-header">
                  <AppIcon icon-class="el-icon-setting" class=""></AppIcon>
                  <span>操作按钮</span>
                </div></template>
                <div class="card-body">
                  <el-button type="primary" size="small">主要操作</el-button>
                  <el-button type="info" size="small" plain>次要操作</el-button>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>

        <div class="color-palette">
          <h3>配色方案</h3>
          <div class="palette-grid">
            <div class="color-item">
              <div class="color-box" style="background: #eaf6ff;"></div>
              <span>主色调</span>
              <code>#eaf6ff</code>
            </div>
            <div class="color-item">
              <div class="color-box" style="background: #00bcd4;"></div>
              <span>强调色</span>
              <code>#00bcd4</code>
            </div>
            <div class="color-item">
              <div class="color-box" style="background: #f5fbff;"></div>
              <span>背景色</span>
              <code>#f5fbff</code>
            </div>
            <div class="color-item">
              <div class="color-box" style="background: #172033;"></div>
              <span>文字色</span>
              <code>#172033</code>
            </div>
          </div>
        </div>

        <div class="features-grid">
          <div class="feature-box">
            <AppIcon icon-class="el-icon-aim" class=""></AppIcon>
            <h4>清爽留白</h4>
            <p>浅色空间让菜单、表格和表单更易扫描</p>
          </div>
          <div class="feature-box">
            <AppIcon icon-class="el-icon-data-board" class=""></AppIcon>
            <h4>青蓝焦点</h4>
            <p>按钮、选中态和边框用青蓝提示操作层级</p>
          </div>
          <div class="feature-box">
            <AppIcon icon-class="el-icon-view" class=""></AppIcon>
            <h4>轻柔层次</h4>
            <p>半透明卡片和弱阴影避免界面过重</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'MinimalistFuturePreview',
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
.theme-preview-page {
  min-height: 100vh;
  
  &.minimalist-future-theme {
    background: linear-gradient(180deg, #f5fbff 0%, #eef8ff 100%);
    color: #172033;
  }
  
  // 顶部标题栏
  .preview-header {
    background: rgba(234, 246, 255, 0.96);
    padding: 30px 40px;
    border-bottom: 1px solid rgba(0, 188, 212, 0.18);
    box-shadow: 0 12px 32px rgba(22, 64, 95, 0.08);
    
    .header-content {
      max-width: 1400px;
      margin: 0 auto;
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .title-section {
        display: flex;
        align-items: center;
        gap: 20px;
        
        .theme-icon {
          font-size: 48px;
          color: #00bcd4;
        }
        
        .title-text {
          h1 {
            font-size: 32px;
            color: #172033;
            margin: 0;
            font-weight: 700;
            line-height: 1.2;
            letter-spacing: 0;
          }
          
          .subtitle {
            font-size: 14px;
            color: #5c6b82;
            margin: 5px 0 0 0;
            letter-spacing: 0;
            text-transform: uppercase;
          }
        }
      }
      
      .back-btn {
        background: #00bcd4;
        border: 1px solid #00bcd4;
        color: #ffffff;
        font-weight: 600;
        padding: 12px 30px;
        border-radius: 6px;
        box-shadow: 0 8px 20px rgba(0, 188, 212, 0.18);
        
        &:hover {
          background: #006c9a;
          border-color: #006c9a;
          color: #ffffff;
          box-shadow: 0 10px 24px rgba(0, 108, 154, 0.2);
          transform: translateY(-1px);
        }
      }
    }
  }
  
  // 主预览区域
  .preview-layout {
    max-width: 1400px;
    margin: 0 auto;
    padding: 40px;
    display: flex;
    gap: 30px;
    
    // 左侧侧边栏预览
    .sidebar-preview {
      width: 240px;
      flex-shrink: 0;
      background: rgba(255, 255, 255, 0.88);
      border: 1px solid rgba(0, 188, 212, 0.18);
      border-radius: 8px;
      box-shadow: 0 16px 36px rgba(22, 64, 95, 0.12);
      overflow: hidden;
      
      .sidebar-logo {
        background: #eaf6ff;
        padding: 20px;
        display: flex;
        align-items: center;
        gap: 12px;
        color: #172033;
        font-weight: 700;
        font-size: 16px;
        border-bottom: 1px solid rgba(0, 188, 212, 0.18);
      }
      
      .sidebar-menu {
        padding: 15px 0;
        
        .menu-item {
          display: flex;
          align-items: center;
          gap: 12px;
          padding: 14px 20px;
          color: #5c6b82;
          cursor: pointer;
          transition: all 0.2s;
          position: relative;
          border-left: 3px solid transparent;
          
          i:first-child {
            font-size: 18px;
          }
          
          span {
            flex: 1;
            font-size: 14px;
            font-weight: 500;
          }
          
          .submenu-arrow {
            font-size: 12px;
            transition: transform 0.3s;
          }
          
          &.has-submenu .submenu-arrow {
            transform: rotate(180deg);
          }
          
          &:hover {
            background: rgba(0, 188, 212, 0.1);
            color: #00bcd4;
            border-left-color: #00bcd4;
          }
          
          &.active {
            background: rgba(0, 188, 212, 0.12);
            color: #006c9a;
            font-weight: 700;
            border-left-color: #00bcd4;
          }
        }
        
        .submenu-items {
          background: #f5fbff;
          
          .submenu-item {
            padding: 12px 20px 12px 50px;
            color: #5c6b82;
            font-size: 13px;
            cursor: pointer;
            transition: all 0.2s;
            font-weight: 500;
            border-left: 3px solid transparent;
            
            &:hover {
              background: rgba(0, 188, 212, 0.1);
              color: #00bcd4;
              border-left-color: #00bcd4;
            }
            
            &.active {
              background: rgba(0, 188, 212, 0.12);
              color: #006c9a;
              font-weight: 700;
              border-left-color: #00bcd4;
            }
          }
        }
      }
    }
    
    // 右侧内容预览
    .content-preview {
      flex: 1;
      
      .theme-description {
        margin-bottom: 30px;
        padding: 25px;
        background: rgba(255, 255, 255, 0.88);
        border: 1px solid rgba(0, 188, 212, 0.18);
        border-radius: 8px;
        border-left: 4px solid #00bcd4;
        box-shadow: 0 14px 34px rgba(22, 64, 95, 0.1);
        
        h2 {
          font-size: 24px;
          color: #172033;
          margin: 0 0 10px 0;
          font-weight: 700;
          letter-spacing: 0;
        }
        
        p {
          margin: 0;
          color: #5c6b82;
          line-height: 1.6;
          font-weight: 400;
        }
      }
      
      .demo-section {
        margin-bottom: 30px;
        
        .demo-card {
          background: rgba(255, 255, 255, 0.88);
          border: 1px solid rgba(0, 188, 212, 0.18);
          color: #172033;
          border-radius: 8px;
          box-shadow: 0 10px 26px rgba(22, 64, 95, 0.08);
          transition: all 0.2s;
          
          &:hover {
            border-color: rgba(0, 188, 212, 0.45);
            box-shadow: 0 16px 34px rgba(22, 64, 95, 0.12);
            transform: translateY(-2px);
          }
          
          .card-header {
            display: flex;
            align-items: center;
            color: #172033;
            font-weight: 700;
            
            i {
              margin-right: 8px;
              font-size: 18px;
              color: #00bcd4;
            }
          }
          
          .card-body {
            p {
              margin: 8px 0;
              color: #5c6b82;
              font-weight: 400;
            }
            
            .el-tag {
              margin: 5px 8px 5px 0;
              background: rgba(0, 188, 212, 0.08);
              border: 1px solid rgba(0, 188, 212, 0.35);
              color: #006c9a;
              border-radius: 4px;
              font-weight: 600;
            }
            
            .el-button {
              margin: 5px 8px 5px 0;
              background: #00bcd4;
              border: 1px solid #00bcd4;
              color: #ffffff;
              border-radius: 4px;
              font-weight: 600;
              
              &:hover {
                background: #006c9a;
                border-color: #006c9a;
                color: #ffffff;
                box-shadow: 0 8px 18px rgba(0, 108, 154, 0.18);
              }
            }
          }
        }
      }
      
      .color-palette {
        margin-bottom: 30px;
        padding: 25px;
        background: rgba(255, 255, 255, 0.88);
        border: 1px solid rgba(0, 188, 212, 0.18);
        border-radius: 8px;
        box-shadow: 0 14px 32px rgba(22, 64, 95, 0.08);
        
        h3 {
          font-size: 18px;
          color: #172033;
          margin: 0 0 20px 0;
          font-weight: 700;
        }
        
        .palette-grid {
          display: grid;
          grid-template-columns: repeat(4, 1fr);
          gap: 15px;
          
          .color-item {
            text-align: center;
            
            .color-box {
              width: 100%;
              height: 80px;
              border-radius: 6px;
              margin-bottom: 10px;
              border: 1px solid rgba(0, 188, 212, 0.16);
              box-shadow: 0 8px 18px rgba(22, 64, 95, 0.1);
            }
            
            span {
              display: block;
              color: #5c6b82;
              font-size: 13px;
              margin-bottom: 5px;
              font-weight: 600;
            }
            
            code {
              display: block;
              color: #006c9a;
              font-size: 12px;
              background: #f5fbff;
              padding: 4px 8px;
              border-radius: 4px;
              font-weight: 700;
              border: 1px solid rgba(0, 188, 212, 0.18);
            }
          }
        }
      }
      
      .features-grid {
        display: grid;
        grid-template-columns: repeat(3, 1fr);
        gap: 20px;
        
        .feature-box {
          padding: 25px;
          background: rgba(255, 255, 255, 0.88);
          border: 1px solid rgba(0, 188, 212, 0.18);
          border-radius: 8px;
          text-align: center;
          transition: all 0.2s;
          box-shadow: 0 10px 26px rgba(22, 64, 95, 0.08);
          
          &:hover {
            border-color: rgba(0, 188, 212, 0.45);
            box-shadow: 0 16px 34px rgba(22, 64, 95, 0.12);
            transform: translateY(-2px);
          }
          
          i {
            font-size: 36px;
            color: #00bcd4;
            margin-bottom: 15px;
          }
          
          h4 {
            font-size: 16px;
            color: #172033;
            margin: 0 0 10px 0;
            font-weight: 700;
          }
          
          p {
            font-size: 13px;
            color: #5c6b82;
            margin: 0;
            line-height: 1.6;
            font-weight: 400;
          }
        }
      }
    }
  }
}

@media (max-width: 1200px) {
  .preview-layout {
    flex-direction: column;
    
    .sidebar-preview {
      width: 100% !important;
    }
  }
}
</style>
