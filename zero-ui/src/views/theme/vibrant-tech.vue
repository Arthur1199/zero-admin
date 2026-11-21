<template>
  <div class="theme-preview-page vibrant-tech-theme">
    <!-- 顶部标题栏 -->
    <div class="preview-header">
      <div class="header-content">
        <div class="title-section">
          <AppIcon icon-class="el-icon-cpu" class="theme-icon"></AppIcon>
          <div class="title-text">
            <h1>活力科技风</h1>
            <p class="subtitle">Vibrant Tech Theme</p>
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
          <p>炫酷的渐变色与动态光效，适合科技创新企业</p>
        </div>

        <div class="demo-section">
          <el-row :gutter="20">
            <el-col :span="8">
              <el-card shadow="hover" class="demo-card">
                <template #header><div  class="card-header">
                  <AppIcon icon-class="el-icon-cpu" class=""></AppIcon>
                  <span>科技卡片</span>
                </div></template>
                <div class="card-body">
                  <p>蓝紫渐变主色调</p>
                  <p>荧光色高亮点缀</p>
                  <p>动态光效边框</p>
                </div>
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card shadow="hover" class="demo-card">
                <template #header><div  class="card-header">
                  <AppIcon icon-class="el-icon-data-line" class=""></AppIcon>
                  <span>标签展示</span>
                </div></template>
                <div class="card-body">
                  <el-tag type="primary" effect="dark">科技</el-tag>
                  <el-tag type="success" effect="dark">创新</el-tag>
                  <el-tag type="info" effect="dark">未来</el-tag>
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
              <div class="color-box" style="background: #00f2fe;"></div>
              <span>强调色</span>
              <code>#00f2fe</code>
            </div>
            <div class="color-item">
              <div class="color-box" style="background: #141a35;"></div>
              <span>背景色</span>
              <code>#141a35</code>
            </div>
            <div class="color-item">
              <div class="color-box" style="background: #c0c9ff;"></div>
              <span>文字色</span>
              <code>#c0c9ff</code>
            </div>
          </div>
        </div>

        <div class="features-grid">
          <div class="feature-box">
            <AppIcon icon-class="el-icon-star-on" class=""></AppIcon>
            <h4>炫酷渐变</h4>
            <p>蓝紫渐变营造科技氛围</p>
          </div>
          <div class="feature-box">
            <AppIcon icon-class="el-icon-lightning" class=""></AppIcon>
            <h4>荧光高亮</h4>
            <p>荧光蓝提升视觉冲击力</p>
          </div>
          <div class="feature-box">
            <AppIcon icon-class="el-icon-magic-stick" class=""></AppIcon>
            <h4>动态光效</h4>
            <p>悬浮动画增强交互体验</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'VibrantTechPreview',
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
  
  &.vibrant-tech-theme {
    background: linear-gradient(135deg, #0a0e27 0%, #141a35 100%);
    color: #ffffff;
  }
  
  // 顶部标题栏
  .preview-header {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    padding: 30px 40px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
    position: relative;
    overflow: hidden;
    
    &::before {
      content: '';
      position: absolute;
      top: -50%;
      right: -10%;
      width: 300px;
      height: 300px;
      background: radial-gradient(circle, rgba(0, 242, 254, 0.3) 0%, transparent 70%);
      animation: glow 3s ease-in-out infinite;
    }
    
    .header-content {
      max-width: 1400px;
      margin: 0 auto;
      display: flex;
      justify-content: space-between;
      align-items: center;
      position: relative;
      z-index: 1;
      
      .title-section {
        display: flex;
        align-items: center;
        gap: 20px;
        
        .theme-icon {
          font-size: 48px;
          color: #00f2fe;
          animation: pulse 2s ease-in-out infinite;
        }
        
        .title-text {
          h1 {
            font-size: 32px;
            color: #ffffff;
            margin: 0;
            font-weight: 700;
            line-height: 1.2;
            text-shadow: 0 0 20px rgba(0, 242, 254, 0.5);
          }
          
          .subtitle {
            font-size: 14px;
            color: #c0c9ff;
            margin: 5px 0 0 0;
            letter-spacing: 2px;
          }
        }
      }
      
      .back-btn {
        background: linear-gradient(135deg, #00f2fe 0%, #00c4d4 100%);
        border: none;
        color: #0a0e27;
        font-weight: 600;
        padding: 12px 30px;
        box-shadow: 0 0 20px rgba(0, 242, 254, 0.3);
        
        &:hover {
          box-shadow: 0 0 30px rgba(0, 242, 254, 0.6);
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
      background: #141a35;
      border-radius: 12px;
      box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
      border: 1px solid rgba(0, 242, 254, 0.2);
      overflow: hidden;
      
      .sidebar-logo {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        padding: 20px;
        display: flex;
        align-items: center;
        gap: 12px;
        color: #00f2fe;
        font-weight: 600;
        font-size: 16px;
        border-bottom: 1px solid rgba(0, 242, 254, 0.2);
        box-shadow: 0 0 20px rgba(0, 242, 254, 0.3);
      }
      
      .sidebar-menu {
        padding: 15px 0;
        
        .menu-item {
          display: flex;
          align-items: center;
          gap: 12px;
          padding: 14px 20px;
          color: #c0c9ff;
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
            background: rgba(0, 242, 254, 0.1);
            color: #00f2fe;
            box-shadow: 0 0 15px rgba(0, 242, 254, 0.2);
          }
          
          &.active {
            background: linear-gradient(90deg, rgba(102, 126, 234, 0.3) 0%, transparent 100%);
            color: #00f2fe;
            font-weight: 600;
            box-shadow: 0 0 20px rgba(0, 242, 254, 0.3);
            
            &::before {
              content: '';
              position: absolute;
              left: 0;
              top: 0;
              bottom: 0;
              width: 3px;
              background: linear-gradient(180deg, #00f2fe 0%, #667eea 100%);
              box-shadow: 0 0 10px #00f2fe;
            }
          }
        }
        
        .submenu-items {
          background: rgba(10, 14, 39, 0.5);
          
          .submenu-item {
            padding: 12px 20px 12px 50px;
            color: #c0c9ff;
            font-size: 13px;
            cursor: pointer;
            transition: all 0.3s;
            
            &:hover {
              background: rgba(0, 242, 254, 0.08);
              color: #00f2fe;
            }
            
            &.active {
              background: rgba(0, 242, 254, 0.12);
              color: #00f2fe;
              font-weight: 600;
              border-left: 3px solid #00f2fe;
              box-shadow: 0 0 15px rgba(0, 242, 254, 0.2);
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
        background: linear-gradient(135deg, rgba(102, 126, 234, 0.2) 0%, rgba(118, 75, 162, 0.2) 100%);
        border: 1px solid rgba(0, 242, 254, 0.3);
        border-radius: 12px;
        border-left: 4px solid #00f2fe;
        box-shadow: 0 0 30px rgba(0, 242, 254, 0.2);
        
        h2 {
          font-size: 24px;
          color: #00f2fe;
          margin: 0 0 10px 0;
          font-weight: 600;
          text-shadow: 0 0 10px rgba(0, 242, 254, 0.5);
        }
        
        p {
          margin: 0;
          color: #c0c9ff;
          line-height: 1.6;
        }
      }
      
      .demo-section {
        margin-bottom: 30px;
        
        .demo-card {
          background: #141a35;
          border: 1px solid #2a3552;
          color: #ffffff;
          transition: all 0.3s;
          
          &:hover {
            border-color: #00f2fe;
            box-shadow: 0 8px 32px rgba(0, 242, 254, 0.3);
            transform: translateY(-2px);
          }
          
          .card-header {
            display: flex;
            align-items: center;
            color: #00f2fe;
            font-weight: 600;
            
            i {
              margin-right: 8px;
              font-size: 18px;
            }
          }
          
          .card-body {
            p {
              margin: 8px 0;
              color: #c0c9ff;
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
        background: #141a35;
        border: 1px solid #2a3552;
        border-radius: 12px;
        
        h3 {
          font-size: 18px;
          color: #00f2fe;
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
              box-shadow: 0 4px 20px rgba(0, 242, 254, 0.2);
            }
            
            span {
              display: block;
              color: #c0c9ff;
              font-size: 13px;
              margin-bottom: 5px;
            }
            
            code {
              display: block;
              color: #00f2fe;
              font-size: 12px;
              background: rgba(0, 242, 254, 0.1);
              padding: 4px 8px;
              border-radius: 4px;
              border: 1px solid rgba(0, 242, 254, 0.2);
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
          background: #141a35;
          border: 1px solid #2a3552;
          border-radius: 12px;
          text-align: center;
          transition: all 0.3s;
          
          &:hover {
            border-color: #00f2fe;
            box-shadow: 0 8px 32px rgba(0, 242, 254, 0.3);
            transform: translateY(-5px);
          }
          
          i {
            font-size: 36px;
            color: #00f2fe;
            margin-bottom: 15px;
          }
          
          h4 {
            font-size: 16px;
            color: #ffffff;
            margin: 0 0 10px 0;
          }
          
          p {
            font-size: 13px;
            color: #c0c9ff;
            margin: 0;
            line-height: 1.6;
          }
        }
      }
    }
  }
}

@keyframes glow {
  0%, 100% {
    opacity: 0.5;
    transform: scale(1);
  }
  50% {
    opacity: 0.8;
    transform: scale(1.1);
  }
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
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
