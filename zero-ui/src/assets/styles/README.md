# 样式维护

项目全局样式从 index.scss 加载。Element Plus 自带 CSS 先由 main.js 加载，项目覆盖随后生效。页签、顶部导航和业务页面的独立样式随组件按需加载。

## 职责

| 位置 | 内容 |
| --- | --- |
| base | 文档基础样式、间距工具、Sass 变量和混入 |
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

index.scss 的导入顺序维护级联关系。现有主题还包含 Element UI 迁移时期的选择器，调整时同时核对实际 DOM、页面 scoped 样式和后续覆盖。保留的兼容规则需要写明组件和原因。

form-builder-page 为表单构建器提供独立的主题边界。theme-nordic-minimal 是历史样式，设置面板的八个正式选项由 Settings 组件维护。

需要 !important 时，就近说明其覆盖对象，例如 Element Plus 固定列绘制、输入框内层旧装饰或主题分区边框。新增规则放入所属组件，避免继续增加“最终覆盖”文件。

## 验证

纯文件迁移先比较构建后的 CSS。规则合并和变量调整后，运行 npm run check:migration、相关测试与 npm run build:prod。明显视觉改动检查八种主题的切换、刷新及组件交互，保留页面截图作为核对依据。
