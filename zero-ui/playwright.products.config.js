import { defineConfig, devices } from '@playwright/test'
import base from './playwright.config.js'

const chromeExecutable = process.env.ZERO_CHROME_EXECUTABLE
if (!chromeExecutable) throw new Error('实际Chrome验收需设置ZERO_CHROME_EXECUTABLE为已核实的Chrome安装路径')

export default defineConfig({
  ...base,
  outputDir: 'output/playwright-products/results',
  reporter: [['list'], ['html', { outputFolder: 'output/playwright-products/report', open: 'never' }]],
  use: { ...base.use, baseURL: 'http://127.0.0.1:5179' },
  projects: [
    {
      name: 'actual-chrome',
      use: { ...devices['Desktop Chrome'], launchOptions: { executablePath: chromeExecutable } }
    },
    { name: 'actual-edge', use: { ...devices['Desktop Edge'], channel: 'msedge' } }
  ],
  webServer: {
    ...base.webServer,
    env: { ...base.webServer.env, ZERO_VITE_CACHE_DIR: 'node_modules/.vite-products' },
    command: 'npm run dev -- --host 127.0.0.1 --port 5179',
    url: 'http://127.0.0.1:5179'
  }
})
