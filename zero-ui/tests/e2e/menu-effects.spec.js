import { test, expect } from '@playwright/test'
import { authenticated } from './mock-api'

test('八主题水波纹、脉冲和流光点击动画可见，关闭特效和重复点击会清理', async ({ page }) => {
  test.setTimeout(180000)
  const errors = []; page.on('pageerror', error => errors.push(error.message))
  await authenticated(page, { menus: [{ path: '/system', component: 'Layout', alwaysShow: true, meta: { title: '系统管理' }, children: [
    { path: 'config', name: 'Config', component: 'system/config/index', meta: { title: '后台参数设置' } },
    { path: 'branch', component: 'ParentView', alwaysShow: true, meta: { title: '嵌套菜单' }, children: [
      { path: 'post', name: 'Post', component: 'system/post/index', meta: { title: '岗位管理' } }
    ] }
  ] }] })
  await page.goto('/system/config')
  const sidebar = page.locator('.sidebar-container')
  const menu = sidebar.locator('a[href="/index"] .el-menu-item')
  const settings = page.locator('.el-drawer')
  for (const theme of ['毛玻璃拟态风', '高端暗黑风', '极简未来风', '原始风格', '轻奢商务风', '活力科技风', '温馨自然风', '质感设计风']) {
    await page.locator('.navbar .setting').click()
    await settings.locator('.drawer-select').first().click()
    await page.getByRole('option', { name: theme, exact: true }).click()
    await page.getByRole('button', { name: '关闭设置', exact: true }).click()
    await expect(settings).toBeHidden()
    for (const [label, mode, animation] of [
      ['绚丽水波纹', 'ripple-gorgeous', 'gorgeous-ripple-scale'],
      ['绚丽脉冲', 'pulse-gorgeous', 'gorgeous-pulse-main'],
      ['绚丽流光', 'gradient-gorgeous', 'gorgeous-gradient-scale']
    ]) {
      await page.locator('.navbar .setting').click()
      await settings.locator('.drawer-select').last().click()
      await page.getByRole('option', { name: label, exact: true }).click()
      await page.getByRole('button', { name: '关闭设置', exact: true }).click()
      await expect(settings).toBeHidden()
      await menu.click()
      await expect(menu).toHaveAttribute('data-zero-menu-effect', mode)
      expect(await menu.evaluate(element => getComputedStyle(element).animationName), theme + label).toBe(animation)
      if (mode !== 'gradient-gorgeous') {
        const layer = menu.locator('.zero-menu-click-effect')
        await expect(layer).toBeAttached()
        await expect.poll(() => layer.evaluate(element => element.getBoundingClientRect().width)).toBeGreaterThan(0)
        const appearance = await layer.evaluate(element => {
          const style = getComputedStyle(element)
          return { gradient: style.backgroundImage.startsWith('radial-gradient('), pointerEvents: style.pointerEvents, width: element.getBoundingClientRect().width, animation: style.animationName }
        })
        expect(appearance.gradient, theme + label).toBe(true)
        expect(appearance.pointerEvents).toBe('none')
        expect(appearance.animation).toBe(mode === 'ripple-gorgeous' ? 'zero-menu-wave' : 'particle-burst')
        await menu.click()
        await expect(menu.locator('.zero-menu-click-effect')).toHaveCount(1)
      }
      await expect(menu).not.toHaveClass(/menu-clicking/)
      await expect(menu.locator('.zero-menu-click-effect')).toHaveCount(0)
    }
  }
  await page.locator('.navbar .setting').click()
  await settings.locator('.drawer-select').last().click()
  await page.getByRole('option', { name: '绚丽脉冲', exact: true }).click()
  await page.getByRole('button', { name: '关闭设置', exact: true }).click()
  const rootTitle = sidebar.locator('.el-sub-menu__title').filter({ hasText: '系统管理' })
  await rootTitle.click({ timeout: 5000 })
  await expect(rootTitle).toHaveClass(/menu-clicking/)
  const nestedTitle = sidebar.locator('.el-sub-menu__title').filter({ hasText: '嵌套菜单' })
  await nestedTitle.click({ timeout: 5000 })
  await expect(nestedTitle).toHaveClass(/menu-clicking/)
  await page.locator('.navbar .setting').click()
  await settings.locator('.drawer-select').last().click()
  await page.getByRole('option', { name: '无特效', exact: true }).click()
  await page.getByRole('button', { name: '关闭设置', exact: true }).click()
  await menu.click()
  await expect(menu).not.toHaveClass(/menu-clicking/)
  await expect(sidebar.locator('.zero-menu-click-effect')).toHaveCount(0)
  expect(errors).toEqual([])
})
