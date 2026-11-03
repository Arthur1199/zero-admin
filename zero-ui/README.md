## 开发

当前使用 Vue 3.5.43、Vite 8.3.3、Element Plus 2.14.7、Vue Router 4 和 Vuex 4，保留 Options API。
使用 Node 24.21.0 LTS（见 `.nvmrc`），锁文件需提交，以 `npm ci` 安装。

```bash
# 克隆项目
git clone https://gitee.com/y_project/Zero-Vue

# 进入项目目录
cd zero-ui

# 安装依赖
npm ci

# 建议不要直接使用 cnpm 安装依赖，会有各种诡异的 bug。可以通过如下操作解决 npm 下载速度慢的问题
npm ci --registry=https://registry.npmmirror.com

# 启动服务
npm run dev
```

浏览器访问 http://localhost:80

## 发布

```bash
# 构建测试环境
npm run build:stage

# 构建生产环境
npm run build:prod
```

`build:stage` 使用 `/stage-api`，`build:prod` 使用 `/prod-api`，两者覆盖同一 `dist`，发布时确认最后构建的环境。
开发代理见 `vite.config.mjs`，默认 admin 9099、开发端口80；可用 `VITE_DEV_PROXY_TARGET`、`VITE_DEV_PORT` 覆盖。
历史 `vue.config.js`、`babel.config.js`、`build/index.js` 保留供参考，当前脚本不调用这些文件。

## 验证

```powershell
npm run check:migration
npm test
npx playwright install chromium firefox webkit
npm run test:e2e
npm run preview
```

浏览器测试拦截业务请求，报告和截图保存到 `output/playwright`。mock不代替真实Java、Nacos、Redis、数据库及存储联调。
Vite排除`output`目录的文件监听，截图、trace和报告不参与热更新，避免自动化产物持续写入影响日常开发页面；源码热更新仍启用。
实施、验收边界和回退步骤见仓库根目录 `前段项目升级方案` 的三份文档。
