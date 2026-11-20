# 若依官方代码生成模板原件

查询及下载日期：2026-11-16。此目录保留官方参考原件，不在应用classpath中。后续已按用户授权将适配副本接入`zero-admin/src/main/resources/vm/`并完成专项验证，见[集成与验证报告](../04-代码生成模板集成与验证报告.md)；下文差异说明保留作为原件与适配版的对照。

## 官方来源

- [若依官方文档与源码入口](https://doc.ruoyi.vip/ruoyi-vue/)
- [官方GitHub仓库的v3.9.0模板目录](https://github.com/yangzongzhuan/RuoYi-Vue/tree/v3.9.0/ruoyi-generator/src/main/resources/vm)
- [官方Gitee仓库](https://gitee.com/y_project/RuoYi-Vue)
- 固定提交：`fa88922637fa674026ef0a6f3db5f46a3d946945`，标签：`v3.9.0`。选择它作为参考是因为本项目README标注Zero v3.9.0，且模板路径集合与当前VelocityUtils一致；不据此认定本项目未做其它定制。
- 官方原件共13个`.vm`，未替换包名、权限注解或前端代码。每个文件已按GitHub树记录校验Git blob SHA-1，并记录SHA-256，见`来源与校验.json`；保留上游MIT许可`LICENSE`。

## 文件清单

```text
vm/
├─ java/
│  ├─ controller.java.vm
│  ├─ domain.java.vm
│  ├─ mapper.java.vm
│  ├─ service.java.vm
│  ├─ serviceImpl.java.vm
│  └─ sub-domain.java.vm
├─ js/api.js.vm
├─ sql/sql.vm
├─ xml/mapper.xml.vm
└─ vue/
   ├─ index.vue.vm
   ├─ index-tree.vue.vm
   └─ v3/
      ├─ index.vue.vm
      └─ index-tree.vue.vm
```

对应菜单：“系统工具 → 代码生成”，地址`/tool/gen`。当前项目的`VelocityUtils.getTemplateList`按单表、树表、主子表和前端类型选择以上模板；`GenTableServiceImpl`在预览、压缩包下载和生成到目录时通过Velocity加载。`VelocityInitializer`使用classpath资源加载器，因此正式接入的位置应是`zero-admin/src/main/resources/vm/`，不是前端的`src/views`。

## 与zero-admin的差异

这些原件可以作为恢复来源，但直接复制并不等于生成结果可编译、可运行：

1. Java模板使用`com.ruoyi.common.*`，本项目实际为`com.zero.common.*`。
2. 此标签的Controller模板使用`javax.servlet.http.HttpServletResponse`，本项目Spring Boot 3应使用`jakarta.servlet`；官方当前`springboot3`分支可作为对照，不能将不同分支模板随意混用。
3. 权限使用`@PreAuthorize`，本项目约定使用`@RequiresPermissions`。列表辅助详情和生成菜单按钮权限也须按本项目现有规则核对，不能直接执行原版菜单SQL。
4. 官方Vue3模板使用`script setup`、`proxy.useDict`等写法，本项目保留Options API，且`main.js`没有注册`useDict`。须适配字典、导入、图标及组件调用，不能只替换Vue版本字符串。
5. 本项目Java注释有固定作者、时间、说明模板，生成的类和方法也需要同步适配。

后续维护适配模板时，复验单表、树表、主子表渲染，并编译生成的 Java 和 Vue 3 组件。预览与下载的复验顺序见[专项验证报告](../04-代码生成模板集成与验证报告.md#证据及复验)。验收使用隔离产物，数据库操作和业务源码覆盖按项目规则另行确认。
