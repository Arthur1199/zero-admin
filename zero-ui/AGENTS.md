# zero-ui 前端 Agent 说明

本目录为若依系管理后台前端（Vue 2 + Element UI）。在 Cursor 中编辑本目录下文件时，项目规则会自动附加 Agency 中与前端优化相关的角色（见仓库根目录 `.cursor/rules` 中带 `zero-ui` 路径匹配的规则）。

## 技术栈（勿擅自升级）

- Vue 2.6、Vue Router 3、Vuex 3、Element UI 2、Vue CLI 4
- 请求封装：`src/utils/request.js`；开发代理：`vue.config.js`

## 优化时的约束

- 以**最小改动**修复问题；不做无关重构、不批量格式化 unrelated 文件。
- 列表页沿用既有若依结构（`queryParams`、`getList`、分页等）。
- 样式改动前先阅读 `src/assets/styles` 与主题相关 SCSS，避免破坏多主题。
- 不新增全局依赖、不升级框架，除非单独评审并说明影响。
