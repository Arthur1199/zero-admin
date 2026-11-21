# 样式维护

项目全局样式从 index.scss 加载。Element Plus 自带 CSS 先由 main.js 加载，项目覆盖随后生效。页签、顶部导航和业务页面的独立样式随组件按需加载。

## 职责

| 位置 | 内容 |
| --- | --- |
| base | 文档基础样式、间距工具、Sass 工具、公共尺寸及层级 |
| layout | 侧栏、顶部导航、页签及内容区的公共结构与状态 |
| components | Element Plus、富文本和共享组件的结构适配 |
| themes | 七种增强主题、原始风格补充及主题取值 |
| themes/components | 同一组件在各主题中的配色和必要的特殊表面效果 |
| themes/layout | 菜单、页脚和布局的主题取值 |
| themes/pages | 业务字典、表单构建器等独立页面的主题差异 |
| motion | 菜单点击、选中抖动、页签反馈和主题独有动画 |

## 修改入口

- 调整所有页面共用的输入框、弹窗或表格结构时，修改 components 中对应组件。element-plus-compat.scss 汇总当前 Element Plus DOM 的适配，表单、表格、弹层和菜单分别维护。
- 调整主题取值时，先看 themes/_tokens.scss 的语义变量。--zero-surface、--zero-text、--zero-border 等供组件和 body 弹层取色。--zero-fixed-table-* 是表格兼容别名，组件读取 Element Plus 变量时直接映射语义变量。
- 修改侧栏交互时，公共状态在 layout/sidebar-states.scss，主题配色在 themes/layout/_sidebar.scss，动画在 motion/sidebar-effects.scss。当前分支由 Sidebar 递归计算，不按菜单层数添加选择器。
- 页面特有的尺寸和布局留在 Vue 的 scoped 样式中。页面主题取值放到 themes/pages，由页面显式导入，避免在全局入口提前加载整个业务页面的覆盖。
- 挂载到 body 的菜单、日期框和消息卡片使用全局组件类与 body 主题类限定范围。主题特有效果应以对应主题类作为边界。
- JavaScript 维护弹窗位置、拖动尺寸、动画触发和状态切换。静态背景、边框、字体及悬浮状态交给 SCSS 和主题变量。

## 加载与兼容

index.scss 的加载顺序维护级联关系。已清理 Vue2 树选择器、旧镜像固定列、旧 Dialog 外壳和 mini 按钮选择器；固定列只维护 Element Plus 的 sticky 单元格。`.el-button--text` 等仍被历史业务模板使用的兼容类继续保留。

- Sass 变量、混入使用 `@use`；base/_index.scss 用 `@forward` 提供不产生 CSS 的工具入口。`variables.scss` 只声明 Sass 常量，只有 `variables.module.scss` 向 JavaScript 导出菜单参数。
- 输出 CSS 的模块使用 `meta.load-css`，保留原加载位置和级联顺序。不要把文件中段的覆盖直接移成文件头部的 `@use`，否则可能改变规则先后关系。
- 主题调亮、调暗统一使用 `_theme-colors.scss` 的 `shift-lightness`，保留原颜色的 RGB 裁剪边界。动画定义集中在 motion，避免不同模块再次定义同名关键帧。

form-builder-page 为表单构建器提供独立的主题边界。theme-nordic-minimal 是历史样式，设置面板的八个正式选项由 Settings 组件维护。

构建器标识只匹配 `/tool/build` 和其子路径，不因其它地址含有 build 就启用。其白色输入表面仍使用深色文字，不能简单套用毛玻璃页面的白字；规则编辑区使用独立的 `--form-build-rule-*` 变量配对背景和文字。

需要 !important 时，就近说明其覆盖对象，例如 Element Plus 固定列绘制、输入框内层旧装饰或主题分区边框。新增规则放入所属组件，避免继续增加“最终覆盖”文件。

## 尺寸、弹层及运行时职责

- base/metrics.scss 定义 large/default/small 三档控件尺寸，对应 40/32/24px。全局选择仍由 App 的 ConfigProvider 传递，局部显式 size 保留；历史 mini 输入值由现有 normalizeSize 兼容为 small，不增加第四档。
- header、移动侧栏遮罩、调整手柄、右键菜单和通知使用 `--zero-layer-*`。Dialog、MessageBox、日期选择层和图片预览沿用 Element Plus 的动态层级；菜单内部禁止再声明 99999 一类强制置顶值。
- 毛玻璃背景只由 App.vue 维护类名与根变量；设置面板只提交 Vuex 状态，避免异步导入的旧背景覆盖新主题。
- 图表通过 `utils/chartTheme.js` 和 `mixins/chartAppearance.js` 读取语义变量，统一文字、轴线、图例、提示背景及公共调色板。组件继续维护数据、坐标范围和有业务含义的系列样式；Canvas 在主题切换时更新选项。
- motion/preferences.scss 维护键盘 focus-visible 和系统减少动态效果。减少动态效果只停止动画与过渡，保留菜单选中和悬浮标记；图表同步响应该偏好并在卸载时释放监听。
- 三个历史弹窗指令共用 directive/dialog/interaction.js，SCSS 负责手柄外观，JavaScript 只计算位置和尺寸。禁用、卸载及窗口失焦释放事件，拖动和缩放受视口限制，并处理 CSS zoom。新弹窗优先使用 Plus 自带 draggable；不要将 DOM 指令直接绑定到没有原生单根节点的组件。

## 后续维护边界

主题专有的渐变、阴影、圆角和装饰继续留在对应主题中；不为减少行数而把八种主题的视觉差异强行合并。剩余必要的优先级覆盖需结合真实 DOM 和构建 CSS 逐步核对，不能批量去掉 !important。

## 验证

纯文件迁移先比较构建后的 CSS。规则合并和变量调整后，运行 npm run check:migration、相关测试与 npm run build:prod。明显视觉改动检查八种主题的切换、刷新及组件交互，保留页面截图作为核对依据。

专项用例为 tests/e2e/style-details.spec.js，覆盖拖拽上下限、卸载释放、键盘焦点、800×480 视口、125% CSS 缩放、五类图表八主题切换、减少动态效果，以及通知/确认框/图片预览的叠层关系。菜单及表单构建器继续使用 sidebar-collapse.spec.js、theme-controls.spec.js 等现有回归。

同一工作区的 Playwright 任务应串行启动；并发运行两次命令会清理共享 output/playwright/results，并可能关闭另一任务复用的开发服务。测试中的 HTTP/WebSocket 均使用 mock，不代表真实后端联调。
