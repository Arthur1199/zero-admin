<template>
  <div class="theme-preview-page glassmorphism-theme">
    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="blob blob-1"></div>
      <div class="blob blob-2"></div>
      <div class="blob blob-3"></div>
    </div>

    <!-- 顶部标题栏 -->
    <div class="preview-header">
      <div class="header-content">
        <div class="title-section">
          <AppIcon icon-class="el-icon-picture-outline" class="theme-icon"></AppIcon>
          <div class="title-text">
            <h1>毛玻璃拟态风</h1>
            <p class="subtitle">Glassmorphism Theme</p>
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
          <AppIcon icon-class="el-icon-s-grid" class=""></AppIcon>
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
          <p>半透明毛玻璃效果，柔和的背景模糊，打造高级质感的视觉体验</p>
        </div>

        <div class="demo-section">
          <el-row :gutter="20">
            <el-col :span="8">
              <el-card shadow="hover" class="demo-card">
                <template #header><div  class="card-header">
                  <AppIcon icon-class="el-icon-picture-outline" class=""></AppIcon>
                  <span>毛玻璃卡片</span>
                </div></template>
                <div class="card-body">
                  <p>半透明背景效果</p>
                  <p>柔和的背景模糊</p>
                  <p>细腻的光影边框</p>
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
                  <el-tag type="primary">现代</el-tag>
                  <el-tag type="success">时尚</el-tag>
                  <el-tag type="warning">高级</el-tag>
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
              <div class="color-box" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);"></div>
              <span>主色调</span>
              <code>渐变紫蓝</code>
            </div>
            <div class="color-item">
              <div class="color-box" style="background: rgba(255, 255, 255, 0.25); backdrop-filter: blur(20px); border: 1px solid rgba(255,255,255,0.3);"></div>
              <span>强调色</span>
              <code>半透明白</code>
            </div>
            <div class="color-item">
              <div class="color-box" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);"></div>
              <span>装饰色</span>
              <code>粉红渐变</code>
            </div>
            <div class="color-item">
              <div class="color-box" style="background: #ffffff;"></div>
              <span>文字色</span>
              <code>#ffffff</code>
            </div>
          </div>
        </div>

        <div class="features-grid">
          <div class="feature-box">
            <AppIcon icon-class="el-icon-view" class=""></AppIcon>
            <h4>毛玻璃效果</h4>
            <p>backdrop-filter模糊打造高级质感</p>
          </div>
          <div class="feature-box">
            <AppIcon icon-class="el-icon-star-on" class=""></AppIcon>
            <h4>浮动色块</h4>
            <p>动态渐变背景增强视觉层次</p>
          </div>
          <div class="feature-box">
            <AppIcon icon-class="el-icon-sunrise" class=""></AppIcon>
            <h4>半透明设计</h4>
            <p>柔和的透明度营造梦幻氛围</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'GlassmorphismPreview',
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
  position: relative;
  overflow: hidden;
  
  &.glassmorphism-theme {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 50%, #f093fb 100%);
    color: #ffffff;
    
    .bg-decoration {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      overflow: hidden;
      z-index: 0;
      
      .blob {
        position: absolute;
        border-radius: 50%;
        filter: blur(60px);
        opacity: 0.6;
        animation: float 8s infinite ease-in-out;
        
        &.blob-1 {
          width: 400px;
          height: 400px;
          background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
          top: 10%;
          left: 10%;
          animation-delay: 0s;
        }
        
        &.blob-2 {
          width: 500px;
          height: 500px;
          background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
          top: 50%;
          right: 10%;
          animation-delay: 2s;
        }
        
        &.blob-3 {
          width: 350px;
          height: 350px;
          background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
          bottom: 10%;
          left: 50%;
          animation-delay: 4s;
        }
      }
    }
  }
  
  // 顶部标题栏
  .preview-header {
    background: rgba(255, 255, 255, 0.1);
    backdrop-filter: blur(20px);
    border-bottom: 1px solid rgba(255, 255, 255, 0.2);
    padding: 30px 40px;
    position: relative;
    z-index: 1;
    
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
            text-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
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
        background: rgba(255, 255, 255, 0.2);
        border: 1px solid rgba(255, 255, 255, 0.3);
        color: #ffffff;
        font-weight: 600;
        padding: 12px 30px;
        border-radius: 12px;
        backdrop-filter: blur(10px);
        
        &:hover {
          background: rgba(255, 255, 255, 0.3);
          border-color: rgba(255, 255, 255, 0.5);
          box-shadow: 0 4px 16px rgba(255, 255, 255, 0.2);
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
    position: relative;
    z-index: 1;
    
    // 左侧侧边栏预览
    .sidebar-preview {
      width: 240px;
      flex-shrink: 0;
      background: rgba(0, 0, 0, 0.3);
      backdrop-filter: blur(20px);
      border-radius: 16px;
      border: 1px solid rgba(255, 255, 255, 0.2);
      box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
      overflow: hidden;
      
      .sidebar-logo {
        background: rgba(255, 255, 255, 0.15);
        backdrop-filter: blur(10px);
        padding: 20px;
        display: flex;
        align-items: center;
        gap: 12px;
        color: #ffffff;
        font-weight: 600;
        font-size: 16px;
        border-bottom: 1px solid rgba(255, 255, 255, 0.2);
        
        i {
          font-size: 24px;
        }
      }
      
      .sidebar-menu {
        padding: 15px 0;
        
        .menu-item {
          display: flex;
          align-items: center;
          gap: 12px;
          padding: 14px 20px;
          color: rgba(255, 255, 255, 0.9);
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
            background: rgba(255, 255, 255, 0.15);
            color: #ffffff;
          }
          
          &.active {
            background: rgba(255, 255, 255, 0.2);
            color: #ffffff;
            font-weight: 600;
            
            &::before {
              content: '';
              position: absolute;
              left: 0;
              top: 0;
              bottom: 0;
              width: 3px;
              background: #ffffff;
              box-shadow: 0 0 10px rgba(255, 255, 255, 0.5);
            }
          }
        }
        
        .submenu-items {
          background: rgba(0, 0, 0, 0.2);
          
          .submenu-item {
            padding: 12px 20px 12px 50px;
            color: rgba(255, 255, 255, 0.8);
            font-size: 13px;
            cursor: pointer;
            transition: all 0.3s;
            
            &:hover {
              background: rgba(255, 255, 255, 0.1);
              color: #ffffff;
            }
            
            &.active {
              background: rgba(255, 255, 255, 0.15);
              color: #ffffff;
              font-weight: 600;
              position: relative;
              
              &::before {
                content: '';
                position: absolute;
                left: 0;
                top: 0;
                bottom: 0;
                width: 3px;
                background: #ffffff;
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
        background: rgba(0, 0, 0, 0.3);
        backdrop-filter: blur(20px);
        border: 1px solid rgba(255, 255, 255, 0.2);
        border-radius: 16px;
        border-left: 4px solid #ffffff;
        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
        
        h2 {
          font-size: 24px;
          color: #ffffff;
          margin: 0 0 10px 0;
          font-weight: 600;
          text-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
        }
        
        p {
          margin: 0;
          color: rgba(255, 255, 255, 0.9);
          line-height: 1.6;
        }
      }
      
      .demo-section {
        margin-bottom: 30px;
        
        .demo-card {
          background: rgba(255, 255, 255, 0.15);
          backdrop-filter: blur(20px);
          border: 1px solid rgba(255, 255, 255, 0.2);
          color: #ffffff;
          border-radius: 16px;
          box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
          transition: all 0.3s;
          
          &:hover {
            background: rgba(255, 255, 255, 0.2);
            border-color: rgba(255, 255, 255, 0.3);
            box-shadow: 0 12px 48px rgba(0, 0, 0, 0.15);
            transform: translateY(-4px);
          }
          
          .card-header {
            display: flex;
            align-items: center;
            color: #ffffff;
            font-weight: 600;
            
            i {
              margin-right: 8px;
              font-size: 18px;
            }
          }
          
          .card-body {
            p {
              margin: 8px 0;
              color: rgba(255, 255, 255, 0.9);
            }
            
            .el-tag {
              margin: 5px 8px 5px 0;
              background: rgba(255, 255, 255, 0.2);
              border: 1px solid rgba(255, 255, 255, 0.3);
              color: #ffffff;
              backdrop-filter: blur(10px);
            }
            
            .el-button {
              margin: 5px 8px 5px 0;
              background: rgba(255, 255, 255, 0.2);
              border: 1px solid rgba(255, 255, 255, 0.3);
              color: #ffffff;
              backdrop-filter: blur(10px);
              
              &:hover {
                background: rgba(255, 255, 255, 0.3);
                border-color: rgba(255, 255, 255, 0.5);
              }
            }
          }
        }
      }
      
      .color-palette {
        margin-bottom: 30px;
        padding: 25px;
        background: rgba(0, 0, 0, 0.3);
        backdrop-filter: blur(20px);
        border: 1px solid rgba(255, 255, 255, 0.2);
        border-radius: 16px;
        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
        
        h3 {
          font-size: 18px;
          color: #ffffff;
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
              box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
            }
            
            span {
              display: block;
              color: rgba(255, 255, 255, 0.9);
              font-size: 13px;
              margin-bottom: 5px;
            }
            
            code {
              display: block;
              color: #ffffff;
              font-size: 12px;
              background: rgba(255, 255, 255, 0.1);
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
          background: rgba(255, 255, 255, 0.15);
          backdrop-filter: blur(20px);
          border: 1px solid rgba(255, 255, 255, 0.2);
          border-radius: 16px;
          text-align: center;
          transition: all 0.3s;
          box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
          
          &:hover {
            background: rgba(255, 255, 255, 0.2);
            border-color: rgba(255, 255, 255, 0.3);
            box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
            transform: translateY(-5px);
          }
          
          i {
            font-size: 36px;
            color: #ffffff;
            margin-bottom: 15px;
            filter: drop-shadow(0 0 10px rgba(255, 255, 255, 0.3));
          }
          
          h4 {
            font-size: 16px;
            color: #ffffff;
            margin: 0 0 10px 0;
          }
          
          p {
            font-size: 13px;
            color: rgba(255, 255, 255, 0.9);
            margin: 0;
            line-height: 1.6;
          }
        }
      }
    }
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
  }
  50% {
    transform: translateY(-20px) rotate(5deg);
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
