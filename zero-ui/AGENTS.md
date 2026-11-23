# zero-ui 模块规则

本目录为若依系管理后台前端，使用 Vue 3 和 Element Plus，保留 Options API。通用规则见[仓库 AGENTS.md](../AGENTS.md)，本文件补充前端约束。本文 `zero-ui/` 开头的代码路径相对仓库根目录，其余前端代码路径相对本目录。菜单与接口共同契约见根规则，跨模块修改同时读取对应后端模块规则。安装、启动和构建见[前端 README](README.md)。

## 职责与源码入口

| 事项 | 当前入口 |
| --- | --- |
| 依赖、脚本与代理 | [package.json](package.json)、[vite.config.mjs](vite.config.mjs) |
| 请求、登录初始化与在线通信 | [request.js](src/utils/request.js)、[permission.js](src/permission.js)、[websocket.js](src/utils/websocket.js) |
| 页面与动态路由 | [index.js](src/router/index.js)、[permission.js](src/store/modules/permission.js) |
| 主题和缓存切换 | [App.vue](src/App.vue)、[AppMain.vue](src/layout/components/AppMain.vue)、[settings.js](src/store/modules/settings.js) |
| 样式职责、语义变量和回归入口 | [README.md](src/assets/styles/README.md) |

## 技术栈与版本来源

- 提交锁文件，以 `npm ci` 安装，不使用 `force`/`legacy-peer-deps`；Node 版本按下一条技术栈基线。
- 前端模块：`zero-ui`，Vue 3.5.43 + Vite 8.3.3 + Vue Router 4.6.4 + Vuex 4.1.0 + Element Plus 2.14.7，保留 Options API。Node 开发基线为 24.21.0，`package.json` 声明的引擎范围为 `>=24.21.0 <25`。不擅自引入 Pinia、TypeScript 或批量改为 Composition API。
- 前端请求：Axios 封装在 `zero-ui/src/utils/request.js`，开发代理在 `zero-ui/vite.config.mjs`，浏览器变量使用 `VITE_APP_*`。Vite 开发代理由 `VITE_DEV_PROXY_TARGET` 覆盖，默认指向 `http://localhost:9099`。
- Vue3升级后的构建、缓存、主题和固定列规则以 `zero-ui/AGENTS.md`、[前端升级说明](../前段项目升级方案/) 为准。当前固定列采用 Element Plus sticky 单元格，不能按旧镜像表结构新增覆盖。

## 前端结构规则

- 按业务归属组织页面和 API：admin 自身业务分别放在 `src/views/system`、`src/views/monitor`、`src/views/tool` 及对应的 `src/api/system`、`src/api/monitor`、`src/api/tool`；admin 代理 report 的业务统一放在 `src/views/reportSystem` 和 `src/api/reportSystem`。前端 API 与 admin 路径保持一致，例如 `/reportSystem/bizDict/**`。
- API 请求统一放在 `src/api/**`，通过 `@/utils/request` 发起。
- 页面放在 `src/views/**`，布局在 `src/layout/**`，公共组件在 `src/components/**`。
- 路由分两类：`router/index.js` 中的静态/动态路由，以及后端 `getRouters` 返回的菜单路由。后端菜单的 `component` 会按 `@/views/${view}` 动态加载，例如 report 页面组件应写成 `reportSystem/bizDict/index`。
- 登录后路由守卫在 `permission.js` 中先调用 `GetInfo`，再调用 `getGoogleAuthApis()` 刷新接口谷歌验证缓存，再 `GenerateRoutes` 生成菜单路由并启动 WebSocket。并发导航复用同一个初始化 Promise，失败后允许重试；无 Token 或强退时清理连接与旧谷歌验证规则。不要在业务页面里重复做这一套全局初始化。
- `main.js` 已全局注册 `Pagination`、`RightToolbar`、`DictTag`、上传、富文本、图片预览等组件，并挂载 `parseTime`、`resetForm`、`addDateRange`、`download` 等方法；页面优先复用这些能力。
- 权限按钮使用 `v-hasPermi` / `v-hasRole`，不手写分散权限判断；与菜单及 admin 注解的对应关系见[根文件权限规则](../AGENTS.md#菜单权限配置规则)。删除/恢复共用权限时，两个状态按钮使用同一个权限；Element Plus 下拉菜单项的特殊处理见本文件优化约束。
- 字典使用 `dicts: [...]`、`dict-tag` 和项目字典组件，不重复硬编码字典标签。

## 前端请求与页面规范

- 请求成功判断以 `request.js` 拦截器为准：普通 JSON 接口应明确返回 `code: 200`；现有拦截器用 `res.data.code || 200` 兼容缺失或假值状态码，不能据此设计新接口。`401` 触发重新登录，`500` 弹错误消息，接口谷歌验证错误码按全局逻辑处理；文件流走独立分支。业务数据读取及历史兼容边界见[根文件接口规范](../AGENTS.md#后端接口规范)。
- `request.js` 会自动追加 `Authorization: Bearer <token>`，GET 参数会序列化到 URL，POST/PUT 对低于 5MB 阈值的请求做一秒内重复提交判断。该判断是客户端短时保护，不等同于服务端幂等或事务保证；业务 API 文件不要重复实现。
- 接口谷歌验证由 `request.js` 根据 localStorage 中的 `google_auth_apis` 判断，支持精确路径、`{param}`、`*`、`**` 和请求方法匹配；命中后统一弹 6 位验证码输入框并添加 `X-Google-Code` 请求头。
- Element Plus `el-upload` 的 `action` 直传不经过 `request.js`。检查实际上传调用方式，显式维护登录请求头、响应状态和错误处理；配置接口谷歌验证时不能假定直传会自动弹验证码。
- 列表页沿用若依结构：顶部查询表单、操作按钮行、`right-toolbar`、`el-table`、`pagination`、新增/修改 `el-dialog`；保留 `queryParams`、`dateRange`、`loading`、`total`、`getList()`、`handleQuery()`、`resetQuery()` 等既有页面模型，不为单个页面另建布局。
- 查询接口用 `GET + params`，新增用 `POST + data`，修改用 `PUT + data`，删除用 `DELETE /ids`。
- 导出使用 `this.download(...)` 或现有下载封装，不自行拼接 blob 逻辑。
- 表单弹窗使用 `el-dialog` + `el-form` + `rules` + `this.$refs["form"].validate`，提交后刷新列表并关闭弹窗。
- UI 改动要遵循 Element Plus 和当前后台风格，避免引入新的 UI 库或重写整体布局。
- 该项目已有主题、菜单、Tab 等大量样式改动，修改样式前必须先读相关 SCSS、组件和现有说明文档，避免破坏多主题适配。

## 管理后台 UI 开发沉淀规则

- API 文件只封装请求，不写业务判断；命名按动作拆清楚，例如 `listDictGroup`、`listDictGroupOptions`、`getDictGroup`、`addDictGroup`、`updateDictGroup`、`delDictGroup`、`restoreDictGroup`。
- admin 自身页面 API 走 `/system/**`、`/monitor/**` 等现有路径；admin 代理 report 的页面 API 走 `/reportSystem/**`，前端不要直接请求 `zero-report` 端口或服务名。
- 查询条件里需要下拉选项时，先判断数据来源是否独立于分页列表。如果是字典组编码这类全量选项，应新增独立 options 接口，进入页面 `created()` 时加载，用 Element Plus 的 `filterable` 做本地过滤；不要临时用当前页表格数据拼下拉，也不要无必要地做远程搜索。
- options 接口如果用于查询区全量选项，应明确是否过滤状态和删除标志。本项目业务字典组编码下拉按当前实现是不区分状态、删除标志全量返回，再由前端 `filterable` 本地过滤。
- 没有批量操作时，不要保留 `el-table-column type="selection"` 和顶部批量修改/删除按钮；避免全选框造成误解。
- 软删除数据如果需要展示，列表接口不要默认过滤 `del_flag`；页面要显示“删除标志”，并根据状态切换“删除/恢复”按钮。已删除记录通常禁止修改、禁止新增子项；恢复后再允许继续维护。若只要求父表支持删除标志查询，不要顺手给子表增加未经需求确认的筛选区。
- 展开行展示子表时，要处理父子状态联动：父级已删除时，子项新增、修改、删除/恢复等操作要按业务规则禁用或提示；子项恢复前要确认父级已恢复。
- 表格字段以数据库实体和用户需求为准，字段多时用 `min-width`、`width`、`:show-overflow-tooltip="true"` 和横向滚动处理；不要为了当前屏幕宽度私自隐藏关键字段。
- 操作列能不用 `fixed` 就不用 `fixed`。如果必须固定右侧操作列，要检查不同主题下固定列背景、表头、空数据行和滚动阴影是否协调。
- 弹窗宽度按表单复杂度设置，不要让字段挤压；多行文本使用 `textarea` 和 `autosize`。如果用户要求去掉页面长度限制或长度提示，不要保留 Element 输入框计数器；但后端字段长度、校验注解和数据库列长仍必须保留并作为最终保护。
- 页面文案要和数据库字段语义统一，例如 `createBy` 显示“创建人”、`updateBy` 显示“更新人”、`delFlag` 显示“删除标志”、`extraJson` 显示“拓展配置JSON”。状态标签文案不要默认拼接数据库值，除非用户明确要求展示原始值。
- 查询重置要同时清理 `dateRange` 和 `queryParams`，然后回到第一页重新查询。新增、修改、删除、恢复成功后，刷新主列表；如果影响下拉 options，也要同步刷新 options。
- 页面不要吞掉关键业务错误。写操作失败由后端 `AjaxResult` 和 `request.js` 统一弹出；前端只在必要的本地边界做提示，例如未选择记录、父级已删除不可新增子项。
- 管理后台页面应服务高频维护场景，优先清晰、稳定、可扫描：紧凑查询区、明确按钮、表格字段对齐、必要 tooltip、少用装饰性大块内容。

## 优化时的约束

### 样式与组件边界

- 没有原生元素根节点的 Element Plus 下拉菜单项使用现有 `$auth.hasPermi` 控制 `v-if`，权限标识不变；普通按钮遵守本文件前端结构规则。
- 菜单图标与标题保持独立根节点，叶子项标题放title插槽，折叠图标按el-menu-tooltip__trigger居中；折叠结构覆盖只作用顶层侧栏，不影响弹出子菜单和点击特效。各级子菜单用teleported挂到body，避免父层裁剪；二级标题使用主题文字变量。
- 输入框边框、背景放在Element Plus wrapper，内部input不重复装饰；表格link操作保持紧凑间距，下拉容器与普通按钮垂直居中。
- 数字框内部input不重复绘制底色，禁用控件使用项目主题变量。角色权限树保持操作项在上、树占满下一行；设置栏的body弹层视为内部操作，全局主色初始化不渲染隐藏的颜色控件。
- 系统弹窗、MessageBox确认框和输入提示框不叠加Plus外层留白、边框和阴影；保留内部主题分区及输入校验，覆盖规则需高于主题:not选择器。菜单组件的click参数不能当MouseEvent使用；特效使用原生DOM事件和独立图层，切换、重复触发及卸载后释放图层和定时器。
- 图片预览设置preview-teleported，避免表格和主题transform/backdrop-filter限制全屏范围；预览按钮不使用主题文字变量作背景，采用深色中性底和白色图标。
- 修改SVG后检查全部sprite图标ID和SVG命名空间，不能把DOMParser生成的parsererror或HTML节点当作有效图标；设计器按钮图标复用icon-button，不改后端图标名称。

### 异步请求、表单回显与清理

- 查询失败收尾由页面负责结束loading，错误提示复用request拦截器；设计器空画布需处理初始字段为空，重建后新ID不得与已有字段重复。
- 表单详情及保存拒绝需由消费方收尾，失败保留输入，不重复提示request已展示的错误；返回给调用方的Promise仍由调用链处理，不能用全局吞错掩盖异常。DictTag类型采用Element Plus支持的值，旧主色默认值使用primary。
- 部门、菜单编辑在详情及上级树齐备后打开，树加载失败不使用旧选项；加载期间防止重复点击覆盖回显，失败释放标记供重试。未选图标的菜单不渲染SvgIcon，不把undefined传给必填字符串属性。
- 角色表单同样等待详情与菜单/部门树齐备后打开；读取helper返回Promise，由消费方收尾。字典/任务日志初始化失败结束loading，搜索重试先恢复路由上下文，不能变成查询全部数据。
- 缓存详情用readonly属性，名称/键名请求分别忽略旧响应，失败不保留旧详情。刷新成功提示在请求成功后显示；测试记录实际出现过的消息，不能等到成功提示自动消失后判为未显示。
- 验证码读取失败清除旧图片、uuid和输入，提供重试入口，配置尚未取得时不提交登录；用户角色初始化失败释放loading且不提交空用户ID。白名单地区解析只填充当前草稿的当前IP，不能覆盖手填地区、切换后的IP或新打开的草稿。
- 用户表单复用已成功加载的部门树；首次加载失败时，在下次开窗重试并等待必要选项，共享在途读取且防止并发开窗。重置密码请求须返回原提示框Promise链消费拒绝，成功提示不展示密码。用户表单读取 `getConfigKey("sys.user.initPassword")` 的 `response.msg` 属于已有兼容；该入口返回结构变更时同步迁移，不作为新接口规范。

### Cron 与表单构建器

- Cron及其它组件直接读已声明props，不使用Vue2的$options.propsData。Cron回显区分数值与字符串，保持Quartz星期#参数顺序及年份，确认只回填草稿；预览使用主题变量。
- 生成配置从页面绑定的info提交，不依赖Element Plus表单实例公开model；详情、字典和菜单齐备后允许保存。Sortable挂载实际tbody，先恢复其移动的节点再交给Vue按key排序，卸载释放实例。首次动态路由重匹配显式保留query/hash。
- 设计器复制成功只能在复制执行成功后提示，校验实际剪贴板/粘贴结果，不能只看通知。兼容选区复制和标准Clipboard API；临时选区位于打开的弹窗内部，复制实例在重建/卸载时释放。复选限选数字框使用modelValue/update:modelValue，保留数值和0转undefined的既有语义。

### 样式层次与运行时职责

- 先查 `src/assets/styles/README.md`：公共结构放 base、layout、components，主题差异放 themes，动画放 motion，页面独立布局继续使用 scoped 样式。主题语义变量集中在 `themes/_tokens.scss`，固定列变量属于兼容映射；不要仅为减少行数合并八种主题的视觉差异。
- Sass 工具使用 `@use` / `@forward`，会输出 CSS 的模块沿用 `meta.load-css` 的加载位置和级联顺序；不能把文件中段的覆盖机械移动到顶部，或批量移除 `!important`。
- 图表主题读取 `utils/chartTheme.js` / `mixins/chartAppearance.js`；事件监听、图表实例、弹窗交互和动画资源按组件生命周期释放。修改共享行为时核对主题切换、CSS zoom、减少动态效果和 body 弹层。

## 提示说明弹窗规范

- 功能说明、操作说明及问号帮助弹窗，统一参考角色管理的“数据权限说明”（`src/views/system/role/index.vue`）和用户管理的“登录解锁说明”（`src/views/system/user/index.vue`），无需每次再询问用户尺寸。此规则不用于新增/修改表单、详情弹窗或 MessageBox 二次确认框。
- 使用 `el-dialog`，默认宽度为 `width="min(860px, 94vw)"`，设置 `append-to-body` 和 `:close-on-click-modal="false"`。保留标题、关闭按钮及底部“我知道了”按钮，复用全局弹窗和八种主题样式，不单独定义背景、边框或阴影。
- 内容高度随说明长度自适应；内容区域使用 `max-height: 60vh`、`overflow-y: auto` 和 `overflow-wrap: anywhere`，长说明在内容区域滚动，短说明不强行撑满高度。默认行高为 `1.7`，列表与段落间距参考上述页面。
- 工具栏的说明文字及问号图标应与相邻按钮的字号、垂直对齐保持一致。工具栏使用 `size="small"` 时，说明入口复用 `el-button--small` 的字号规则，并兼容各主题的尺寸调整；不能直接继承页面正文字号导致说明比按钮大，也不要重复硬编码字号。
- 说明文案以当前后端实现为准，写清功能作用、操作位置及必要边界。属于特定操作的说明入口和弹窗按当前登录账号的对应权限显示；打开说明只展示文字，不发起实际业务操作。修改后核对八种主题、权限显隐和窄屏适配。

## 前端多主题适配规则

- 当前管理后台可选主题风格包括 7 个增强主题和 1 个原始风格：`luxury-business` 轻奢商务风、`glassmorphism` 毛玻璃拟态风、`vibrant-tech` 活力科技风、`minimalist-future` 极简未来风、`warm-nature` 温馨自然风、`material-design` 质感设计风、`dark-premium` 高端暗黑风、`default` 原始风格。
- 主题可选项以 `zero-ui/src/layout/components/Settings/index.vue` 下拉为准。当前 `index.scss` 导入 7 个增强主题文件，`App.vue` 的清理数组还包含历史/实验类 `theme-nordic-minimal`；不要仅因为文件存在就把未导入、未配置的主题当作正式可选主题。
- 如果新增、删除或重命名主题，必须同步检查 `Settings/index.vue` 下拉、`settings.js` 默认值、Vuex `settings` 持久化、`App.vue` 清理数组、`layout/index.vue` 主题类、`index.scss` 导入、主题 SCSS 文件、`table-fixed-column.scss` 变量和弹层全局样式，不能只改一个下拉选项。
- 主题状态保存在 Vuex `settings` 模块，并从 localStorage 的 `layout-setting` 恢复；主题切换由 `App.vue` 监听 `themeStyle`、`glassmorphismBgStyle` 后更新 `body` 类名和毛玻璃背景变量。
- 主题类同时存在于 `body` 和 `layout/index.vue` 的 `.app-wrapper` 上。写全局样式时优先用 `body.theme-*`；写页面局部样式时要考虑 `.theme-* .page-class` 也可能命中。
- `AppMain.vue` 的路由 key 包含 `themeStyle`，用于主题切换时重建 keep-alive 页面；遇到“切换主题后不正常、刷新后正常”的问题，优先检查旧主题类、运行时 style、Element 表格 hover 状态和 keep-alive 缓存。
- 修改页面样式、组件样式、表格展开行、弹窗、表单、按钮、分页、固定列、下拉层、菜单或 Tab 时，不得只按当前正在使用的主题修一份颜色；必须考虑上述全部主题在浅色、深色、毛玻璃、高对比和原始风格下的显示效果。
- 写样式前先确认主题入口和作用域：`zero-ui/src/settings.js`、`zero-ui/src/store/modules/settings.js`、`zero-ui/src/App.vue`、`zero-ui/src/layout/components/Settings/index.vue`、`zero-ui/src/layout/index.vue`、`zero-ui/src/assets/styles/index.scss`、`zero-ui/src/assets/styles/components/element-plus.scss`、`zero-ui/src/assets/styles/components/pagination.scss`、`zero-ui/src/assets/styles/themes/glassmorphism-readable.scss`、`zero-ui/src/assets/styles/components/table.scss` 以及 `zero-ui/src/assets/styles/themes/theme-*.scss`。
- 通用页面样式优先使用 Element Plus 既有结构、项目已有 CSS 变量或主题类作用域；如果必须写颜色、背景、边框、阴影或透明度，要么使用能兼容明暗主题的中性色和透明度，要么分别在 `.theme-luxury-business`、`.theme-glassmorphism`、`.theme-vibrant-tech`、`.theme-minimalist-future`、`.theme-warm-nature`、`.theme-material-design`、`.theme-dark-premium` 和默认样式下做适配。
- 修复某个主题下的视觉问题时，也要检查其它主题是否会出现白底、黑字看不清、透明层叠、固定列背景错位、hover/active 状态异常、弹层被遮挡、边框割裂、按钮不可见等问题。
- 新增或调整业务页面时，页面本身不要强绑定某一种主题色；业务页面只负责结构和局部必要样式，整体背景、菜单、顶部栏、Tabs、分页、弹窗和表格基础观感尽量交给全局主题样式控制。
- 主题切换问题不能只看刷新后的效果。切换时要确认 `App.vue` 会清理 `body` 上全部旧主题类、毛玻璃背景类和运行时样式，`layout/index.vue` 的主题类也要同步，`AppMain.vue` 的缓存 key 要包含主题值，避免 keep-alive 页面沿用旧主题渲染结果。
- Element Plus 的弹层、下拉框、消息框、日期选择器等经常挂在 `body` 下，业务页的 `scoped` 样式管不到；这类样式必须写在全局样式或非 scoped 块里，并按主题类限定作用域。
- 调整表格展开行、固定列、空数据行、hover 行或选中行时，按 Element Plus 的 sticky 单元格结构核对表头、表体、扩展单元格、边框、阴影和交互状态。通用样式由 `main.js` 导入 `table-fixed-column.scss`，局部样式优先复用现有变量。
- 操作列确实需要固定时，优先复用 `table-fixed-column.scss` 的 `--zero-fixed-table-*` 变量；页面局部只能补必要结构样式，不能把固定列背景硬写成某一个主题的颜色。
- 毛玻璃主题有背景渐变和透明叠层，切出毛玻璃时必须清理 `glass-bg-*` 类、`--glassmorphism-bg-gradient` 变量和运行时样式，否则会出现“刷新后正常、切换后残留”的问题。
- 能验证前端时，至少运行 `npm run build:prod`；涉及明显视觉风险的改动，要优先用浏览器在多个代表主题下查看：`glassmorphism`、`dark-premium`、`minimalist-future`、`default`，必要时再补查其它主题。

## 编码风格

- 前端保持 Vue3 Options API 写法、单引号/双引号风格按当前文件就近一致，不批量格式化。

## 源码依据

- 前端请求和主题依据：`request.js`、`permission.js`、`App.vue`、`AppMain.vue`、`Settings/index.vue`、`settings` 状态及主题 SCSS。

## 验证

- 空前端类型默认 Vue 3，显式选择 Vue 2 时沿用历史模板。模板位于 [zero-admin/src/main/resources/vm](../zero-admin/src/main/resources/vm)。Vue 3 产物沿用 Options API、`dicts`、`TreeSelect` 和 `resolveIcon`。生成组件测试先运行 `GeneratorTemplatesTest`，再运行 `generated-templates.spec.js` 和 `generator-flows.spec.js`，顺序见[专项复验](../前段项目升级方案/04-代码生成模板集成与验证报告.md#证据及复验)。

- 源码与大小写检查：npm run check:migration；单元测试：npm test；浏览器回归：npm run test:e2e。
- Vite开发监听排除output目录，截图、trace和报告不能反复广播到测试及日常浏览器；保留源码热更新，不用延长业务请求超时掩盖产物监听干扰。
- 构建仍使用 npm run build:prod / npm run build:stage，真实联调与mock结果分别记录。

- 纯规则文档整理按根文件只检查事实、路径和 diff，不要求构建；前端源码或样式变更再按风险运行迁移检查、相关测试和构建。保持“源码验证”“模拟页面行为”“真实后端联调”三个结论的区别。
- 同一工作区的 Playwright 命令串行执行，避免共享 `output/playwright/results` 和开发服务互相干扰。样式专项入口及八主题、缩放、键盘焦点、动画偏好等覆盖以样式 README 为准，不把 Mock HTTP/WebSocket 当作真实后端验收。

### 常用验证命令

- 前端开发：在 `zero-ui` 执行 `npm run dev`
- 前端生产构建：在 `zero-ui` 执行 `npm run build:prod`
