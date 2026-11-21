<template>
  <div class="theme-preview-page warm-nature-theme">
    <!-- 顶部标题栏 -->
    <div class="preview-header">
      <div class="header-content">
        <div class="title-section">
          <AppIcon icon-class="el-icon-sunny" class="theme-icon"></AppIcon>
          <div class="title-text">
            <h1>温馨自然风</h1>
            <p class="subtitle">Warm Nature Theme</p>
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
          <p>柔和的暖色调配色，圆角卡片设计，营造舒适温馨的视觉体验</p>
        </div>

        <div class="demo-section">
          <el-row :gutter="20">
            <el-col :span="8">
              <el-card shadow="hover" class="demo-card">
                <template #header><div  class="card-header">
                  <AppIcon icon-class="el-icon-sunny" class=""></AppIcon>
                  <span>温馨卡片</span>
                </div></template>
                <div class="card-body">
                  <p>柔和的暖色调配色</p>
                  <p>圆角卡片设计</p>
                  <p>舒适的视觉感受</p>
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
                  <el-tag type="warning">温馨</el-tag>
                  <el-tag type="success">自然</el-tag>
                  <el-tag type="info">舒适</el-tag>
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
              <div class="color-box" style="background: linear-gradient(135deg, #8b7355 0%, #a68968 100%);"></div>
              <span>主色调</span>
              <code>#8b7355</code>
            </div>
            <div class="color-item">
              <div class="color-box" style="background: linear-gradient(135deg, #e89c5c 0%, #f5b87d 100%);"></div>
              <span>强调色</span>
              <code>#e89c5c</code>
            </div>
            <div class="color-item">
              <div class="color-box" style="background: #faf8f5;"></div>
              <span>背景色</span>
              <code>#faf8f5</code>
            </div>
            <div class="color-item">
              <div class="color-box" style="background: #6d5c47;"></div>
              <span>文字色</span>
              <code>#6d5c47</code>
            </div>
          </div>
        </div>

        <div class="features-grid">
          <div class="feature-box">
            <AppIcon icon-class="el-icon-coffee-cup" class=""></AppIcon>
            <h4>暖色调配色</h4>
            <p>米色与暖橙营造温馨氛围</p>
          </div>
          <div class="feature-box">
            <AppIcon icon-class="el-icon-brush" class=""></AppIcon>
            <h4>圆角设计</h4>
            <p>柔和的圆角带来舒适感受</p>
          </div>
          <div class="feature-box">
            <AppIcon icon-class="el-icon-view" class=""></AppIcon>
            <h4>自然质感</h4>
            <p>接近自然的视觉体验</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'WarmNaturePreview',
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
  
  &.warm-nature-theme {
    background: linear-gradient(135deg, #faf8f5 0%, #f5f1ec 100%);
    color: #3e3629;
  }
  
  // 顶部标题栏
  .preview-header {
    background: linear-gradient(135deg, #8b7355 0%, #e89c5c 100%);
    padding: 30px 40px;
    box-shadow: 0 4px 20px rgba(139, 115, 85, 0.3);
    
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
          color: #ffffff;
          filter: drop-shadow(0 0 10px rgba(255, 255, 255, 0.5));
        }
        
        .title-text {
          h1 {
            font-size: 32px;
            color: #ffffff;
            margin: 0;
            font-weight: 700;
            line-height: 1.2;
          }
          
          .subtitle {
            font-size: 14px;
            color: rgba(255, 255, 255, 0.9);
            margin: 5px 0 0 0;
            letter-spacing: 2px;
          }
        }
      }
      
      .back-btn {
        background: #ffffff;
        border: none;
        color: #8b7355;
        font-weight: 600;
        padding: 12px 30px;
        border-radius: 24px;
        box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
        
        &:hover {
          background: #f5f1ec;
          box-shadow: 0 4px 16px rgba(232, 156, 92, 0.3);
          transform: translateY(-2px);
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
      background: #ffffff;
      border-radius: 16px;
      box-shadow: 0 4px 20px rgba(139, 115, 85, 0.1);
      overflow: hidden;
      border: 1px solid #e8e0d5;
      
      .sidebar-logo {
        background: linear-gradient(135deg, #8b7355 0%, #e89c5c 100%);
        padding: 20px;
        display: flex;
        align-items: center;
        gap: 12px;
        color: #ffffff;
        font-weight: 600;
        font-size: 16px;
      }
      
      .sidebar-menu {
        padding: 15px 0;
        
        .menu-item {
          display: flex;
          align-items: center;
          gap: 12px;
          padding: 14px 20px;
          color: #6d5c47;
          cursor: pointer;
          transition: all 0.3s;
          position: relative;
          border-radius: 0;
          
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
            background: linear-gradient(90deg, rgba(232, 156, 92, 0.1) 0%, transparent 100%);
            color: #e89c5c;
          }
          
          &.active {
            background: linear-gradient(90deg, rgba(139, 115, 85, 0.15) 0%, transparent 100%);
            color: #e89c5c;
            font-weight: 600;
            
            &::before {
              content: '';
              position: absolute;
              left: 0;
              top: 50%;
              transform: translateY(-50%);
              width: 4px;
              height: 60%;
              background: linear-gradient(180deg, #e89c5c 0%, #f5b87d 100%);
              border-radius: 0 4px 4px 0;
            }
          }
        }
        
        .submenu-items {
          background: #faf8f5;
          
          .submenu-item {
            padding: 12px 20px 12px 50px;
            color: #6d5c47;
            font-size: 13px;
            cursor: pointer;
            transition: all 0.3s;
            
            &:hover {
              background: rgba(232, 156, 92, 0.08);
              color: #e89c5c;
            }
            
            &.active {
              background: rgba(232, 156, 92, 0.15);
              color: #e89c5c;
              font-weight: 600;
              position: relative;
              
              &::before {
                content: '';
                position: absolute;
                left: 0;
                top: 50%;
                transform: translateY(-50%);
                width: 3px;
                height: 60%;
                background: #e89c5c;
                border-radius: 0 2px 2px 0;
              }
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
        background: #ffffff;
        border: 1px solid #e8e0d5;
        border-radius: 16px;
        border-left: 4px solid #e89c5c;
        box-shadow: 0 2px 12px rgba(139, 115, 85, 0.08);
        
        h2 {
          font-size: 24px;
          color: #e89c5c;
          margin: 0 0 10px 0;
          font-weight: 600;
        }
        
        p {
          margin: 0;
          color: #6d5c47;
          line-height: 1.6;
        }
      }
      
      .demo-section {
        margin-bottom: 30px;
        
        .demo-card {
          background: #ffffff;
          border: 1px solid #e8e0d5;
          color: #3e3629;
          border-radius: 16px;
          box-shadow: 0 2px 12px rgba(139, 115, 85, 0.08);
          transition: all 0.3s;
          
          &:hover {
            box-shadow: 0 6px 24px rgba(232, 156, 92, 0.2);
            border-color: #f5b87d;
            transform: translateY(-3px);
          }
          
          .card-header {
            display: flex;
            align-items: center;
            color: #e89c5c;
            font-weight: 600;
            
            i {
              margin-right: 8px;
              font-size: 18px;
            }
          }
          
          .card-body {
            p {
              margin: 8px 0;
              color: #6d5c47;
            }
            
            .el-tag {
              margin: 5px 8px 5px 0;
              background: #f5f1ec;
              border: 1px solid #f0e9de;
              color: #6d5c47;
              border-radius: 12px;
            }
            
            .el-button {
              margin: 5px 8px 5px 0;
              background: linear-gradient(135deg, #8b7355 0%, #e89c5c 100%);
              border: none;
              color: #ffffff;
              border-radius: 24px;
              
              &:hover {
                background: linear-gradient(135deg, #e89c5c 0%, #f5b87d 100%);
                box-shadow: 0 4px 16px rgba(232, 156, 92, 0.3);
                transform: translateY(-2px);
              }
            }
          }
        }
      }
      
      .color-palette {
        margin-bottom: 30px;
        padding: 25px;
        background: #ffffff;
        border: 1px solid #e8e0d5;
        border-radius: 16px;
        box-shadow: 0 2px 12px rgba(139, 115, 85, 0.08);
        
        h3 {
          font-size: 18px;
          color: #e89c5c;
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
              border-radius: 12px;
              margin-bottom: 10px;
              box-shadow: 0 2px 12px rgba(139, 115, 85, 0.15);
              border: 1px solid #e8e0d5;
            }
            
            span {
              display: block;
              color: #6d5c47;
              font-size: 13px;
              margin-bottom: 5px;
            }
            
            code {
              display: block;
              color: #e89c5c;
              font-size: 12px;
              background: #f5f1ec;
              padding: 4px 8px;
              border-radius: 8px;
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
          background: #ffffff;
          border: 1px solid #e8e0d5;
          border-radius: 16px;
          text-align: center;
          transition: all 0.3s;
          box-shadow: 0 2px 12px rgba(139, 115, 85, 0.08);
          
          &:hover {
            border-color: #f5b87d;
            box-shadow: 0 6px 24px rgba(232, 156, 92, 0.2);
            transform: translateY(-5px);
          }
          
          i {
            font-size: 36px;
            color: #e89c5c;
            margin-bottom: 15px;
          }
          
          h4 {
            font-size: 16px;
            color: #3e3629;
            margin: 0 0 10px 0;
          }
          
          p {
            font-size: 13px;
            color: #6d5c47;
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
