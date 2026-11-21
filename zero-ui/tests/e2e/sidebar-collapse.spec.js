import { test, expect } from '@playwright/test'
import { authenticated, mockAdminWebSocket } from './mock-api'

const themes = [
  ['glassmorphism', '毛玻璃拟态风'], ['dark-premium', '高端暗黑风'],
  ['minimalist-future', '极简未来风'], ['default', '原始风格'],
  ['luxury-business', '轻奢商务风'], ['vibrant-tech', '活力科技风'],
  ['warm-nature', '温馨自然风'], ['material-design', '质感设计风']
]
const menus = [{ path: '/system', component: 'Layout', alwaysShow: true, meta: { title: '系统管理', icon: 'system' }, children: [
  { path: 'config', name: 'Config', component: 'system/config/index', meta: { title: '后台参数设置', icon: 'list' } },
  { path: 'branch', component: 'ParentView', alwaysShow: true, meta: { title: '嵌套菜单', icon: 'tree' }, children: [
    { path: 'post', name: 'Post', component: 'system/post/index', meta: { title: '岗位管理', icon: 'post' } }
  ] }
] }, { path: 'https://example.com', component: 'Layout', meta: { title: '外部链接', icon: 'link' } }]

async function switchTheme(page, label) {
  await page.locator('.navbar .setting').click()
  const settings = page.locator('.el-drawer')
  await settings.locator('.drawer-select').first().click()
  await page.getByRole('option', { name: label, exact: true }).click()
  await page.getByRole('button', { name: '关闭设置', exact: true }).click()
  await expect(settings).toBeHidden()
}

async function waitForSidebarPopup(page, item) {
  const popup = page.locator('.el-popper.sidebar-menu-popper').filter({ has: item })
  await expect(popup).toBeVisible()
  await expect(popup).not.toHaveClass(/el-zoom-in-left-(enter|leave)-/)
}

for (const [theme, label] of themes) {
  test(label + '折叠侧栏的二三级选中菜单使用同一高亮和抖动', async ({ page }) => {
    await mockAdminWebSocket(page)
    await authenticated(page, { menus })
    await page.addInitScript(themeStyle => {
      localStorage.setItem('layout-setting', JSON.stringify({ themeStyle, topNav: false }))
    }, theme)
    await page.goto('/system/config')
    await page.locator('#hamburger-container').click()
    const root = page.locator('.sidebar-container .el-menu--collapse > div > .el-sub-menu > .el-sub-menu__title')
    const parent = page.locator('.el-menu--popup .el-sub-menu__title').filter({ hasText: '嵌套菜单' })
    const secondLevel = page.locator('.el-menu--popup a[href="/system/config"] .el-menu-item')
    const thirdLevel = page.locator('.el-menu--popup a[href="/system/branch/post"] .el-menu-item')

    await root.hover()
    await expect(secondLevel).toHaveCSS('background-image', /linear-gradient/)
    await expect(secondLevel).toHaveCSS('animation-name', 'menu-shake-breath')
    const activeStyle = await secondLevel.evaluate(element => {
      const { backgroundImage, color } = getComputedStyle(element)
      return { backgroundImage, color }
    })

    await page.goto('/system/branch/post')
    await root.hover()
    await expect(parent).toHaveCSS('background-image', activeStyle.backgroundImage)
    await expect(parent).toHaveCSS('color', activeStyle.color)
    await expect(parent).toHaveCSS('animation-name', 'menu-shake-breath')
    await waitForSidebarPopup(page, parent)
    await parent.hover()
    await expect(thirdLevel).toBeVisible()
    await expect(thirdLevel).toHaveCSS('background-image', activeStyle.backgroundImage)
    await expect(thirdLevel).toHaveCSS('color', activeStyle.color)
    await expect(thirdLevel).toHaveCSS('animation-name', 'menu-shake-breath')
    await expect.poll(() => thirdLevel.evaluate(element => getComputedStyle(element, '::before').content)).toBe('""')
    if (theme === 'material-design') {
      await expect(parent).toHaveCSS('border-radius', '0px')
      await expect(thirdLevel).toHaveCSS('border-radius', '0px')
    }
  })
}

test('极简未来风折叠菜单移到三级菜单后保留二级标题的对比度', async ({ page }) => {
  await mockAdminWebSocket(page)
  await authenticated(page, { menus })
  await page.addInitScript(() => {
    localStorage.setItem('layout-setting', JSON.stringify({ themeStyle: 'minimalist-future', topNav: false }))
  })
  await page.goto('/system/config')
  await page.locator('#hamburger-container').click()
  await page.locator('.sidebar-container .el-menu--collapse > div > .el-sub-menu > .el-sub-menu__title').hover()
  const parent = page.locator('.el-menu--popup .el-sub-menu__title').filter({ hasText: '嵌套菜单' })
  await parent.hover()
  await page.locator('.el-menu--popup a[href="/system/branch/post"]').hover()
  await expect(parent).toHaveCSS('color', 'rgb(0, 108, 154)')
  await expect(parent).toHaveCSS('background-color', 'rgba(0, 188, 212, 0.1)')
  await expect(parent.getByText('嵌套菜单', { exact: true })).toBeVisible()
})

test('毛玻璃主题展开和折叠侧栏的各级菜单悬停时突出显示', async ({ page }) => {
  await mockAdminWebSocket(page)
  await authenticated(page, { menus })
  await page.addInitScript(() => {
    localStorage.setItem('layout-setting', JSON.stringify({
      themeStyle: 'glassmorphism', glassmorphismBgStyle: 'ocean-sunset', topNav: false
    }))
  })
  await page.goto('/index')
  const sidebar = page.locator('.sidebar-container')
  const root = sidebar.locator('.el-sub-menu__title').filter({ hasText: '系统管理' })
  const secondLevel = page.locator('.sidebar-menu-popper a[href="/system/config"] .el-menu-item')
  const parent = page.locator('.sidebar-menu-popper .el-sub-menu__title').filter({ hasText: '嵌套菜单' })
  const thirdLevel = page.locator('.sidebar-menu-popper a[href="/system/branch/post"] .el-menu-item')

  async function expectHover(item) {
    await expect(item).toHaveCSS('background-color', 'rgba(0, 0, 0, 0)')
    await item.hover()
    await expect(item).toHaveCSS('background-color', 'rgba(186, 230, 253, 0.22)')
    await expect(item).toHaveCSS('color', 'rgb(207, 250, 254)')
    await expect(item.locator('.svg-icon')).toHaveCSS('color', 'rgb(207, 250, 254)')
  }

  await expectHover(root)
  await root.click()
  await expectHover(sidebar.locator('a[href="/system/config"] .el-menu-item'))
  await page.locator('#hamburger-container').click()
  await expect(sidebar.locator('.el-menu')).toHaveClass(/el-menu--collapse/)
  await expectHover(root)
  await waitForSidebarPopup(page, secondLevel)
  await expectHover(secondLevel)
  await expectHover(parent)
  await expect(secondLevel).toHaveCSS('background-color', 'rgba(0, 0, 0, 0)')
  await waitForSidebarPopup(page, thirdLevel)
  await expectHover(thirdLevel)
})

test('切换八种主题时，折叠侧栏的顶层图标行距保持一致', async ({ page }) => {
  test.setTimeout(60000)
  await mockAdminWebSocket(page)
  await authenticated(page, { menus })
  await page.addInitScript(() => {
    localStorage.setItem('layout-setting', JSON.stringify({ themeStyle: 'glassmorphism', topNav: false }))
  })
  await page.goto('/system/config')
  await page.locator('#hamburger-container').click()
  const sidebarMenu = page.locator('.sidebar-container .el-menu--collapse')
  const iconPositions = () => sidebarMenu.evaluate(element => {
    const top = element.closest('.sidebar-container').getBoundingClientRect().top
    return [...element.querySelectorAll(':scope > div > a > .el-menu-item .svg-icon, :scope > div > .el-sub-menu > .el-sub-menu__title > .svg-icon')]
      .map(icon => {
        const rect = icon.getBoundingClientRect()
        return Math.round(rect.top + rect.height / 2 - top)
      })
  })
  await expect.poll(async () => (await iconPositions()).length).toBe(3)
  const positions = {}
  for (const [theme, label] of themes) {
    if (theme !== 'glassmorphism') await switchTheme(page, label)
    await expect.poll(async () => (await iconPositions()).length).toBe(3)
    positions[theme] = await iconPositions()
  }
  expect(positions).toEqual(Object.fromEntries(themes.map(([theme]) => [theme, positions.glassmorphism])))
})

test('八主题折叠侧栏图标居中且可见，标题隐藏后无省略号', async ({ page }, testInfo) => {
  test.setTimeout(120000)
  await mockAdminWebSocket(page)
  await authenticated(page, { menus })
  const errors = [], measurements = []
  page.on('pageerror', error => errors.push(error.message))
  await page.goto('/system/config')
  const sidebar = page.locator('.sidebar-container')
  for (const [theme, label] of themes) {
    await switchTheme(page, label)
    await page.locator('#hamburger-container').click()
    await expect(page.locator('.app-wrapper')).toHaveClass(/hideSidebar/)
    await expect.poll(() => sidebar.evaluate(element => Math.round(element.getBoundingClientRect().width))).toBe(54)
    const rows = await sidebar.evaluate(element => {
      const side = element.getBoundingClientRect()
      return [...element.querySelectorAll('.submenu-title-noDropdown, .el-menu--collapse > div > .el-sub-menu > .el-sub-menu__title')].map(row => {
        const icon = row.querySelector('.svg-icon'), box = icon?.getBoundingClientRect()
        const visible = icon && [...function* () { for (let node = icon; node && node !== element; node = node.parentElement) yield node }()].every(node => {
          const style = getComputedStyle(node)
          return style.visibility !== 'hidden' && style.display !== 'none' && Number(style.opacity) > 0 && node.getBoundingClientRect().width > 0
        })
        return { title: row.textContent.trim(), visible: Boolean(visible), width: box?.width || 0, centerOffset: box ? Math.abs(box.x + box.width / 2 - side.x - side.width / 2) : 99, clipped: box ? box.x < side.x || box.right > side.right : true }
      })
    })
    measurements.push({ theme, rows })
    await page.screenshot({ path: testInfo.outputPath(theme + '-collapsed.png') })
    await page.locator('#hamburger-container').click()
    await expect(page.locator('.app-wrapper')).toHaveClass(/openSidebar/)
  }
  await testInfo.attach('collapsed-measurements', { body: JSON.stringify(measurements, null, 2), contentType: 'application/json' })
  for (const { theme, rows } of measurements) {
    expect(rows.length, theme).toBe(3)
    for (const row of rows) {
      expect(row.visible, theme + row.title).toBe(true)
      expect(row.width, theme + row.title).toBeGreaterThan(0)
      expect(row.clipped, theme + row.title).toBe(false)
      expect(row.centerOffset, theme + row.title).toBeLessThanOrEqual(2)
    }
  }
  expect(errors).toEqual([])
})

test('八主题折叠菜单提示、二级悬停、子页跳转、刷新与展开恢复正常', async ({ page }, testInfo) => {
  test.setTimeout(180000)
  await mockAdminWebSocket(page)
  const requests = await authenticated(page, { menus }), errors = []
  page.on('pageerror', error => errors.push(error.message))
  await page.goto('/system/config')
  const sidebar = page.locator('.sidebar-container')
  for (const [theme, label] of themes) {
    await switchTheme(page, label)
    await page.locator('#hamburger-container').click()
    await expect(sidebar.locator('.el-menu')).toHaveClass(/el-menu--collapse/)
    const home = sidebar.locator('a[href="/index"] .el-menu-item')
    await home.hover()
    await expect(page.getByRole('tooltip').filter({ hasText: /^首页$/ })).toBeVisible()
    await expect(sidebar.locator('a[href="https://example.com"]')).toHaveAttribute('target', '_blank')
    const root = sidebar.locator('.el-menu--collapse > div > .el-sub-menu > .el-sub-menu__title')
    await expect(root.locator('.sidebar-menu-title')).toBeHidden()
    await expect(root.locator('.el-sub-menu__icon-arrow')).toBeHidden()
    await root.hover()
    const firstPopup = page.locator('.el-menu--popup').filter({ has: page.locator('a[href="/system/config"]') })
    await expect(firstPopup).toBeVisible()
    await expect.poll(() => firstPopup.evaluate(element => {
      let opacity = 1
      for (let node = element; node && node !== document.body; node = node.parentElement) opacity *= Number(getComputedStyle(node).opacity)
      return opacity
    })).toBeGreaterThanOrEqual(0.99)
    const nestedTitle = firstPopup.locator('.el-sub-menu__title').filter({ hasText: '嵌套菜单' })
    if (theme === 'glassmorphism') {
      const color = await nestedTitle.evaluate(element => ({ actual: getComputedStyle(element).color, expected: getComputedStyle(document.body).getPropertyValue('--el-text-color-primary').trim() }))
      expect(color.actual).toBe(color.expected)
    }
    if (theme === 'default') {
      const color = await nestedTitle.evaluate(element => {
        const expected = getComputedStyle(element.closest('.el-menu')).getPropertyValue('--el-menu-text-color').trim()
        const hex = /^#([\da-f]{6})$/i.exec(expected)
        return {
          actual: getComputedStyle(element).color.match(/[\d.]+/g).slice(0, 3).map(Number),
          expected: hex ? [0, 2, 4].map(index => parseInt(hex[1].slice(index, index + 2), 16)) : expected.match(/[\d.]+/g)?.slice(0, 3).map(Number)
        }
      })
      expect(color.actual).toEqual(color.expected)
    }
    await page.screenshot({ path: testInfo.outputPath(theme + '-popup.png') })
    await nestedTitle.hover()
    const nestedLink = page.locator('.el-menu--popup a[href="/system/branch/post"]')
    await expect(nestedLink).toBeVisible()
    expect(await nestedLink.evaluate(element => Boolean(element.closest('.el-menu--popup').parentElement.closest('.el-menu--popup')))).toBe(false)
    await nestedLink.click({ timeout: 5000 })
    await expect(page).toHaveURL(/\/system\/branch\/post$/)
    await expect(page.locator('.app-main').getByRole('button', { name: '新增', exact: true })).toBeVisible()
    await page.reload()
    await expect(sidebar.locator('.el-menu')).toHaveClass(/el-menu--collapse/)
    await expect(sidebar.locator('a[href="/index"] .svg-icon')).toBeVisible()
    await sidebar.locator('a[href="/index"]').click()
    await expect(page).toHaveURL(/\/index$/)
    await expect(page.locator('.zero-home')).toBeVisible()
    await page.locator('#hamburger-container').click()
    await expect(page.locator('.app-wrapper')).toHaveClass(/openSidebar/)
    await expect(sidebar.getByText('系统管理', { exact: true })).toBeVisible()
    await expect(sidebar.getByText('首页', { exact: true })).toBeVisible()
    const title = sidebar.locator('.el-sub-menu__title').filter({ hasText: '系统管理' })
    const link = sidebar.locator('a[href="/system/config"]')
    if (!await link.isVisible()) await title.click()
    await link.click()
    await expect(page).toHaveURL(/\/system\/config$/)
    await page.screenshot({ path: testInfo.outputPath(theme + '-expanded.png') })
  }
  expect(errors).toEqual([])
  expect(requests.filter(request => request.method !== 'GET')).toEqual([])
})
