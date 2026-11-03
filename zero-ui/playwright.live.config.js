import { defineConfig, devices } from '@playwright/test'

if (!process.env.ZERO_LIVE_USERNAME || !process.env.ZERO_LIVE_PASSWORD) {
  throw new Error('真实服务验证需通过临时环境变量提供登录信息，不写入测试源码或报告')
}
if (!process.env.ZERO_CHROME_EXECUTABLE) throw new Error('需提供已核实的实际Chrome安装路径')

export default defineConfig({
  testDir: './tests/live',
  timeout: 120000,
  workers: 1,
  fullyParallel: false,
  reporter: [['list'], ['html', { outputFolder: 'output/playwright/live-readonly-report', open: 'never' }]],
  outputDir: 'output/playwright/live-readonly-results',
  use: { baseURL: 'http://127.0.0.1', trace: 'off', screenshot: 'off', video: 'off' },
  projects: [
    { name: 'actual-chrome', use: { ...devices['Desktop Chrome'], launchOptions: { executablePath: process.env.ZERO_CHROME_EXECUTABLE } } },
    { name: 'actual-edge', use: { ...devices['Desktop Edge'], channel: 'msedge' } }
  ]
})
