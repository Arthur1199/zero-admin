# zero-ui

管理后台前端使用 Vue、Vite、Element Plus、Vue Router 和 Vuex，沿用 Options API。依赖版本以 [package.json](package.json) 与 [package-lock.json](package-lock.json) 为准。

## 开发

从仓库根目录执行。使用 [.nvmrc](.nvmrc) 指定的 Node 24.21.0 和 npm 11。

```powershell
cd zero-ui
node --version
npm --version
npm ci
npm run dev
```

默认在 [本地 80 端口](http://localhost:80) 打开页面，`/dev-api` 代理到 `http://localhost:9099`。需要调整时，使用 `VITE_DEV_PORT` 和 `VITE_DEV_PROXY_TARGET`。代理和 WebSocket 升级请求设置维护在 [vite.config.mjs](vite.config.mjs)。

## 发布

```powershell
# 构建测试环境
npm run build:stage

# 构建生产环境
npm run build:prod
```

`build:stage` 使用 `/stage-api`，`build:prod` 使用 `/prod-api`。两者写入同一个 `dist`，发布前核对最后一次构建所用环境。`npm run preview` 在本机 9526 端口预览现有构建产物。

当前脚本使用 Vite。历史 `vue.config.js`、`babel.config.js` 和 `build/index.js` 保留供迁移对照。静态部署检查见[发布与回退方案](../前段项目升级方案/03-发布与回退方案.md)。

## 验证

运行完整浏览器套件前，按[代码生成复验顺序](../前段项目升级方案/04-代码生成模板集成与验证报告.md#证据及复验)准备后端测试产物。

```powershell
npm run check:migration
npm test
npx playwright install chromium firefox webkit
npm run test:e2e
```

默认浏览器测试使用模拟业务接口，独立开发服务监听 5178 端口，报告写入 `output/playwright/report`，失败截图与 trace 写入 `output/playwright/results`。Vite 忽略 `output` 的热更新事件，源码仍正常监听。具体配置见 [playwright.config.js](playwright.config.js)。

当前源码的迁移检查会将首页的 `../../package.json` 导入报为大小写不一致。导入目标存在且名称一致，检查脚本逐段校验路径时将 `..` 当成了目录项。此项待修复，历史检查通过的数据对应当时的源码。见[首页导入](src/views/index.vue)与[迁移检查脚本](scripts/check-migration.mjs)。

真实 Java、Nacos、Redis、数据库和存储链路单独验收，见[功能与主题验收清单](../前段项目升级方案/02-功能与主题验收清单.md)。
