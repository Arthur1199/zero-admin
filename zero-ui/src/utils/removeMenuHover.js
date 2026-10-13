/**
 * 移除毛玻璃主题下菜单的hover背景色
 * 通过运行时注入CSS来强制覆盖所有可能的样式
 */
export function removeMenuHoverBackground() {
  // 检查是否为毛玻璃主题
  if (!document.body.classList.contains('theme-glassmorphism')) {
    return
  }

  // 创建一个style标签，使用最高优先级
  const styleId = 'glassmorphism-hover-override'
  
  // 如果已经存在，先移除
  const existingStyle = document.getElementById(styleId)
  if (existingStyle) {
    existingStyle.remove()
  }

  // 创建新的style标签
  const style = document.createElement('style')
  style.id = styleId
  // 使用最高优先级，添加多层选择器和html标签前缀
  style.textContent = `
    /* 毛玻璃主题菜单hover背景色移除 - 运行时强制覆盖 */
    html body.theme-glassmorphism #app .sidebar-container .submenu-title-noDropdown:hover,
    html body.theme-glassmorphism #app .sidebar-container .el-sub-menu__title:hover,
    html body.theme-glassmorphism #app .sidebar-container .nest-menu .el-sub-menu > .el-sub-menu__title:hover,
    html body.theme-glassmorphism #app .sidebar-container .el-sub-menu .el-menu-item:hover,
    html body.theme-glassmorphism #app .sidebar-container .el-menu--inline .el-menu-item:hover,
    html body.theme-glassmorphism #app .sidebar-container ul.el-menu--inline li.el-menu-item:hover,
    html body.theme-glassmorphism .el-menu--vertical .nest-menu .el-sub-menu > .el-sub-menu__title:hover,
    html body.theme-glassmorphism .el-menu--vertical .el-menu-item:hover {
      background-color: transparent !important;
      background: none !important;
      background-image: none !important;
    }
    
    /* 子菜单项特殊处理 - 使用多种选择器组合 */
    html body.theme-glassmorphism #app .sidebar-container .el-menu--inline > .el-menu-item:hover,
    html body.theme-glassmorphism #app .sidebar-container .el-sub-menu > .el-menu--inline > .el-menu-item:hover,
    html body.theme-glassmorphism .sidebar-container .el-menu .el-menu-item:hover {
      background-color: transparent !important;
      background: none !important;
      background-image: none !important;
    }
    
    /* 使用属性选择器再次强制覆盖 */
    html body[class*="theme-glassmorphism"] #app .sidebar-container [class*="el-menu-item"]:hover,
    html body[class*="theme-glassmorphism"] #app .sidebar-container [class*="el-sub-menu__title"]:hover {
      background-color: transparent !important;
      background: none !important;
      background-image: none !important;
    }
  `
  
  // 将style标签插入到head的最后，确保最高优先级
  document.head.appendChild(style)
}

/**
 * 监听主题变化
 */
export function watchThemeChange() {
  // 创建MutationObserver监听body的class变化
  const observer = new MutationObserver((mutations) => {
    mutations.forEach((mutation) => {
      if (mutation.type === 'attributes' && mutation.attributeName === 'class') {
        removeMenuHoverBackground()
      }
    })
  })

  // 开始监听
  observer.observe(document.body, {
    attributes: true,
    attributeFilter: ['class']
  })

  // 初始执行一次
  removeMenuHoverBackground()
  
  // 监听菜单项，强制移除内联样式
  watchMenuItems()
}

/**
 * 监听菜单项，强制移除hover内联样式
 */
function watchMenuItems() {
  // 等待DOM加载完成
  const setupWatcher = () => {
    const sidebar = document.querySelector('.sidebar-container')
    if (!sidebar) {
      setTimeout(setupWatcher, 500)
      return
    }

    let isProcessing = false  // 防止死循环的标志位

    // 监听侧边栏内的变化
    const menuObserver = new MutationObserver((mutations) => {
      if (!document.body.classList.contains('theme-glassmorphism')) {
        return
      }
      
      // 如果正在处理中，跳过本次回调，避免死循环
      if (isProcessing) {
        return
      }
      
      isProcessing = true
      
      // 查找所有菜单项（包括父菜单和子菜单）
      const menuItems = sidebar.querySelectorAll(
        '.el-menu-item, .el-sub-menu__title, .submenu-title-noDropdown'
      )
      
      menuItems.forEach(item => {
        // 只移除已有的内联样式，不再主动设置
        if (item.style.backgroundColor && item.style.backgroundColor !== 'transparent') {
          item.style.removeProperty('background-color')
        }
        if (item.style.background && item.style.background !== 'transparent') {
          item.style.removeProperty('background')
        }
        if (item.style.backgroundImage) {
          item.style.removeProperty('background-image')
        }
      })
      
      isProcessing = false
    })

    // 监听侧边栏的子树变化
    menuObserver.observe(sidebar, {
      childList: true,
      subtree: true,
      attributes: true,
      attributeFilter: ['style', 'class']
    })
  }

  setupWatcher()
}
