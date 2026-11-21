<template>
  <div class="theme-preview-page luxury-business-theme">
    <!-- 顶部标题栏 -->
    <div class="preview-header">
      <div class="header-content">
        <div class="title-section">
          <AppIcon icon-class="el-icon-trophy" class="theme-icon"></AppIcon>
          <div class="title-text">
            <h1>轻奢商务风</h1>
            <p class="subtitle">Luxury Business Theme</p>
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
          <p>深邃高端的商务配色，营造专业质感的管理界面</p>
        </div>

        <div class="demo-section">
          <el-row :gutter="20">
            <el-col :span="8">
              <el-card shadow="hover" class="demo-card">
                <template #header><div  class="card-header">
                  <AppIcon icon-class="el-icon-trophy" class=""></AppIcon>
                  <span>商务卡片</span>
                </div></template>
                <div class="card-body">
                  <p>深蓝紫色调搭配</p>
                  <p>金色点缀高亮</p>
                  <p>专业高端质感</p>
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
                  <el-tag type="warning" effect="dark">轻奢</el-tag>
                  <el-tag type="success" effect="dark">商务</el-tag>
                  <el-tag type="info" effect="dark">专业</el-tag>
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
                  <el-button type="warning" size="small" plain>次要操作</el-button>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>

        <div class="color-palette">
          <h3>配色方案</h3>
          <div class="palette-grid">
            <div class="color-item">
              <div class="color-box" style="background: #2c3e7a;"></div>
              <span>主色调</span>
              <code>#2c3e7a</code>
            </div>
            <div class="color-item">
              <div class="color-box" style="background: #d4af37;"></div>
              <span>强调色</span>
              <code>#d4af37</code>
            </div>
            <div class="color-item">
              <div class="color-box" style="background: #1a1f2e;"></div>
              <span>背景色</span>
              <code>#1a1f2e</code>
            </div>
            <div class="color-item">
              <div class="color-box" style="background: #e8eaf0;"></div>
              <span>文字色</span>
              <code>#e8eaf0</code>
            </div>
          </div>
        </div>

        <div class="features-grid">
          <div class="feature-box">
            <AppIcon icon-class="el-icon-medal" class=""></AppIcon>
            <h4>高端质感</h4>
            <p>深邃的商务色彩营造专业氛围</p>
          </div>
          <div class="feature-box">
            <AppIcon icon-class="el-icon-trophy-1" class=""></AppIcon>
            <h4>金色点缀</h4>
            <p>轻奢的金色高亮提升品质</p>
          </div>
          <div class="feature-box">
            <AppIcon icon-class="el-icon-star-off" class=""></AppIcon>
            <h4>玻璃态效果</h4>
            <p>现代化的视觉设计风格</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'LuxuryBusinessPreview',
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
  
  &.luxury-business-theme {
    background: linear-gradient(135deg, #0f1419 0%, #1a1f2e 100%);
    color: #e8eaf0;
  }
  
  // 顶部标题栏
  .preview-header {
    background: linear-gradient(135deg, #2c3e7a 0%, #1e2d5a 100%);
    padding: 30px 40px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
    
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
          color: #d4af37;
        }
        
        .title-text {
          h1 {
            font-size: 32px;
            color: #d4af37;
            margin: 0;
            font-weight: 700;
            line-height: 1.2;
          }
          
          .subtitle {
            font-size: 14px;
            color: #b8bcc8;
            margin: 5px 0 0 0;
            letter-spacing: 2px;
          }
        }
      }
      
      .back-btn {
        background: linear-gradient(135deg, #d4af37 0%, #c29d2e 100%);
        border: none;
        color: #2c3e7a;
        font-weight: 600;
        padding: 12px 30px;
        
        &:hover {
          box-shadow: 0 0 20px rgba(212, 175, 55, 0.4);
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
      background: #1a1f2e;
      border-radius: 12px;
      box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
      overflow: hidden;
      
      .sidebar-logo {
        background: linear-gradient(135deg, #2c3e7a 0%, #1e2d5a 100%);
        padding: 20px;
        display: flex;
        align-items: center;
        gap: 12px;
        color: #d4af37;
        font-weight: 600;
        font-size: 16px;
        border-bottom: 1px solid #2a3142;
      }
      
      .sidebar-menu {
        padding: 15px 0;
        
        .menu-item {
          display: flex;
          align-items: center;
          gap: 12px;
          padding: 14px 20px;
          color: #b8bcc8;
          cursor: pointer;
          transition: all 0.3s;
          position: relative;
          
          i:first-child {
            font-size: 18px;
          }
          
          span {
            flex: 1;
            font-size: 14px;
          }
          
          .submenu-arrow {
            font-size: 12px;
            transition: transform 0.3s;
          }
          
          &.has-submenu .submenu-arrow {
            transform: rotate(180deg);
          }
          
          &:hover {
            background: rgba(212, 175, 55, 0.1);
            color: #d4af37;
          }
          
          &.active {
            background: rgba(212, 175, 55, 0.15);
            color: #d4af37;
            font-weight: 600;
            
            &::before {
              content: '';
              position: absolute;
              left: 0;
              top: 0;
              bottom: 0;
              width: 3px;
              background: #d4af37;
            }
          }
        }
        
        .submenu-items {
          background: #151924;
          
          .submenu-item {
            padding: 12px 20px 12px 50px;
            color: #b8bcc8;
            font-size: 13px;
            cursor: pointer;
            transition: all 0.3s;
            
            &:hover {
              background: rgba(212, 175, 55, 0.08);
              color: #d4af37;
            }
            
            &.active {
              background: rgba(212, 175, 55, 0.12);
              color: #d4af37;
              font-weight: 600;
              border-left: 3px solid #d4af37;
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
        background: linear-gradient(135deg, rgba(44, 62, 122, 0.3) 0%, rgba(30, 45, 90, 0.3) 100%);
        border: 1px solid #2a3142;
        border-radius: 12px;
        border-left: 4px solid #d4af37;
        
        h2 {
          font-size: 24px;
          color: #d4af37;
          margin: 0 0 10px 0;
          font-weight: 600;
        }
        
        p {
          margin: 0;
          color: #b8bcc8;
          line-height: 1.6;
        }
      }
      
      .demo-section {
        margin-bottom: 30px;
        
        .demo-card {
          background: #1a1f2e;
          border: 1px solid #2a3142;
          color: #e8eaf0;
          transition: all 0.3s;
          
          &:hover {
            border-color: #d4af37;
            box-shadow: 0 8px 32px rgba(212, 175, 55, 0.15);
            transform: translateY(-2px);
          }
          
          .card-header {
            display: flex;
            align-items: center;
            color: #d4af37;
            font-weight: 600;
            
            i {
              margin-right: 8px;
              font-size: 18px;
            }
          }
          
          .card-body {
            p {
              margin: 8px 0;
              color: #b8bcc8;
            }
            
            .el-tag {
              margin: 5px 8px 5px 0;
            }
            
            .el-button {
              margin: 5px 8px 5px 0;
            }
          }
        }
      }
      
      .color-palette {
        margin-bottom: 30px;
        padding: 25px;
        background: #1a1f2e;
        border: 1px solid #2a3142;
        border-radius: 12px;
        
        h3 {
          font-size: 18px;
          color: #d4af37;
          margin: 0 0 20px 0;
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
              border-radius: 8px;
              margin-bottom: 10px;
              box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
            }
            
            span {
              display: block;
              color: #b8bcc8;
              font-size: 13px;
              margin-bottom: 5px;
            }
            
            code {
              display: block;
              color: #d4af37;
              font-size: 12px;
              background: rgba(212, 175, 55, 0.1);
              padding: 4px 8px;
              border-radius: 4px;
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
          background: #1a1f2e;
          border: 1px solid #2a3142;
          border-radius: 12px;
          text-align: center;
          transition: all 0.3s;
          
          &:hover {
            border-color: #d4af37;
            box-shadow: 0 8px 32px rgba(212, 175, 55, 0.15);
            transform: translateY(-5px);
          }
          
          i {
            font-size: 36px;
            color: #d4af37;
            margin-bottom: 15px;
          }
          
          h4 {
            font-size: 16px;
            color: #e8eaf0;
            margin: 0 0 10px 0;
          }
          
          p {
            font-size: 13px;
            color: #b8bcc8;
            margin: 0;
            line-height: 1.6;
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
