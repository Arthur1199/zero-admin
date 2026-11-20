# zero-ui 前端 Agent 说明

本目录为若依系管理后台前端，使用 Vue 3 和 Element Plus，保留 Options API。通用规则见[仓库 AGENTS.md](../AGENTS.md)，本文件补充前端约束。安装、启动和构建见[前端 README](README.md)。

## 技术栈（勿擅自升级）

- Vue 3.5、Vue Router 4、Vuex 4、Element Plus 2、Vite 8；不批量改为 Composition API、Pinia 或 TypeScript。
- 请求封装：`src/utils/request.js`；开发代理：`vite.config.mjs`
- 使用 Node 24.21.0，提交锁文件，以 npm ci 安装，不使用 force/legacy-peer-deps。

## 优化时的约束

- 以最小改动修复问题，格式与文件现有写法一致。
- 列表页沿用既有若依结构（`queryParams`、`getList`、分页等）。
- 样式改动前先阅读 `src/assets/styles` 与主题相关 SCSS，避免破坏多主题。
- 不新增全局依赖、不升级框架，除非单独评审并说明影响。
- 普通权限按钮沿用v-hasPermi；没有原生元素根节点的Element Plus下拉菜单项使用现有$auth.hasPermi控制v-if，权限标识不变。
- 菜单图标与标题保持独立根节点，叶子项标题放title插槽，折叠图标按el-menu-tooltip__trigger居中；折叠结构覆盖只作用顶层侧栏，不影响弹出子菜单和点击特效。各级子菜单用teleported挂到body，避免父层裁剪；二级标题使用主题文字变量。
- Cron及其它组件直接读已声明props，不使用Vue2的$options.propsData。Cron回显区分数值与字符串，保持Quartz星期#参数顺序及年份，确认只回填草稿；预览使用主题变量。
- 输入框边框、背景放在Element Plus wrapper，内部input不重复装饰；表格link操作保持紧凑间距，下拉容器与普通按钮垂直居中。
- 数字框内部input不重复绘制底色，禁用控件使用项目主题变量。角色权限树保持操作项在上、树占满下一行；设置栏的body弹层视为内部操作，全局主色初始化不渲染隐藏的颜色控件。
- 系统弹窗、MessageBox确认框和输入提示框不叠加Plus外层留白、边框和阴影；保留内部主题分区及输入校验，覆盖规则需高于主题:not选择器。菜单组件的click参数不能当MouseEvent使用；特效使用原生DOM事件和独立图层，切换、重复触发及卸载后释放图层和定时器。
- 图片预览设置preview-teleported，避免表格和主题transform/backdrop-filter限制全屏范围；预览按钮不使用主题文字变量作背景，采用深色中性底和白色图标。
- 修改SVG后检查全部sprite图标ID和SVG命名空间，不能把DOMParser生成的parsererror或HTML节点当作有效图标；设计器按钮图标复用icon-button，不改后端图标名称。
- 查询失败收尾由页面负责结束loading，错误提示复用request拦截器；设计器空画布需处理初始字段为空，重建后新ID不得与已有字段重复。
- 生成配置从页面绑定的info提交，不依赖Element Plus表单实例公开model；详情、字典和菜单齐备后允许保存。Sortable挂载实际tbody，先恢复其移动的节点再交给Vue按key排序，卸载释放实例。首次动态路由重匹配显式保留query/hash。
- 表单详情及保存拒绝需由消费方收尾，失败保留输入，不重复提示request已展示的错误；返回给调用方的Promise仍由调用链处理，不能用全局吞错掩盖异常。DictTag类型采用Element Plus支持的值，旧主色默认值使用primary。
- 部门、菜单编辑在详情及上级树齐备后打开，树加载失败不使用旧选项；加载期间防止重复点击覆盖回显，失败释放标记供重试。未选图标的菜单不渲染SvgIcon，不把undefined传给必填字符串属性。
- 角色表单同样等待详情与菜单/部门树齐备后打开；读取helper返回Promise，由消费方收尾。字典/任务日志初始化失败结束loading，搜索重试先恢复路由上下文，不能变成查询全部数据。
- 缓存详情用readonly属性，名称/键名请求分别忽略旧响应，失败不保留旧详情。刷新成功提示在请求成功后显示；测试记录实际出现过的消息，不能等到成功提示自动消失后判为未显示。
- 验证码读取失败清除旧图片、uuid和输入，提供重试入口，配置尚未取得时不提交登录；用户角色初始化失败释放loading且不提交空用户ID。白名单地区解析只填充当前草稿的当前IP，不能覆盖手填地区、切换后的IP或新打开的草稿。
- 用户表单复用已成功加载的部门树；首次加载失败时，在下次开窗重试并等待必要选项，共享在途读取且防止并发开窗。重置密码请求须返回原提示框Promise链消费拒绝，成功提示不展示密码。现有默认值接口的msg读取属于历史兼容，不作为新接口返回规范。
- 设计器复制成功只能在复制执行成功后提示，校验实际剪贴板/粘贴结果，不能只看通知。兼容选区复制和标准Clipboard API；临时选区位于打开的弹窗内部，复制实例在重建/卸载时释放。复选限选数字框使用modelValue/update:modelValue，保留数值和0转undefined的既有语义。

## 验证

- 空前端类型默认 Vue 3，显式选择 Vue 2 时沿用历史模板。模板位于 [zero-admin/src/main/resources/vm](../zero-admin/src/main/resources/vm)。Vue 3 产物沿用 Options API、`dicts`、`TreeSelect` 和 `resolveIcon`。生成组件测试先运行 `GeneratorTemplatesTest`，再运行 `generated-templates.spec.js` 和 `generator-flows.spec.js`，顺序见[专项复验](../前段项目升级方案/04-代码生成模板集成与验证报告.md#证据及复验)。

- 源码与大小写检查：npm run check:migration；单元测试：npm test；浏览器回归：npm run test:e2e。
- Vite开发监听排除output目录，截图、trace和报告不能反复广播到测试及日常浏览器；保留源码热更新，不用延长业务请求超时掩盖产物监听干扰。
- 构建仍使用 npm run build:prod / npm run build:stage，真实联调与mock结果分别记录。
- 主题切换清理旧缓存和body类；固定列按Element Plus sticky结构及项目CSS变量处理。
