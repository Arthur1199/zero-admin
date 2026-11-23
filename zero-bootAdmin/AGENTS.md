# zero-bootAdmin 模块规则

本文件适用于 `zero-bootAdmin/`。同时遵守[仓库全局规则](../AGENTS.md)，通用 Java/YAML 注释规范及安全要求以根文件为准。下文带模块名前缀的代码路径相对仓库根目录，其余模块内部路径相对本目录；Markdown 链接相对本文件。跨模块修改同时读取相关模块规则。

## 职责与源码入口

| 事项 | 当前入口 |
| --- | --- |
| 启动、扫描及依赖 | [ZeroBootAdminApplication.java](src/main/java/com/zero/ZeroBootAdminApplication.java)、[PackageProperties.java](src/main/java/com/zero/packageconfig/PackageProperties.java)、[pom.xml](pom.xml) |
| HTTP 访问控制 | [SecuritySecureConfig.java](src/main/java/com/zero/config/SecuritySecureConfig.java) |
| Nacos 发现 | [NacosWatchAutoConfiguration.java](src/main/java/com/zero/config/NacosWatchAutoConfiguration.java)、[NacosWatch.java](src/main/java/com/zero/config/NacosWatch.java) |
| 状态通知 | [CustomNotifier.java](src/main/java/com/zero/listener/CustomNotifier.java)、[SlackRobotNotifier.java](src/main/java/com/zero/listener/SlackRobotNotifier.java) |
| 应用日志 | [logback-spring.xml](src/main/resources/logback-spring.xml) |

## 技术栈与版本来源

- `zero-bootAdmin` 使用 Spring Boot Admin 3.5.8。分析监控功能时需单独读取该模块和各服务 Actuator 暴露配置。

## 职责与监控边界

- 本模块是独立监控服务，不参与业务代理，不套用 zero-admin 的 JWT 或管理后台按钮权限。
- 主入口为 com.zero.ZeroBootAdminApplication，组件扫描由本模块 PackageProperties 维护；端口与服务名见根文件的服务配置表。
- 排查发现问题时，核对本服务与被监控服务的 Nacos 地址、命名空间、分组和实际注册名；实例被发现不代表 Actuator 已可访问。
- 监控菜单由被监控实例实际开放且可访问的 Actuator 端点决定，分别核对各服务配置、管理端口、访问地址和凭据。
- 访问控制以 SecuritySecureConfig 和本模块 Spring Security 配置为准；调整 Actuator 暴露或监控认证前核对访问边界，不直接复用 admin 的匿名路径规则。
- 查看应用日志时，核对实例 Actuator logfile 指向的文件与 Logback 实际写入文件一致；应用日志和 XXL-JOB 单次任务日志分别管理。
- 通知器位于 listener，通知配置和辅助工具位于本模块配置及 util。修改通知器时核对调用链；未经确认不发送真实通知，凭据不进入日志和验证记录。

- 当前 `SecuritySecureConfig` 放行监控上下文下的资源、登录页及 `/actuator/**`，其他请求要求认证，并配置表单登录和 HTTP Basic、禁用 CSRF。不能据“存在 Spring Security”就宣称所有 Actuator 端点受登录保护；调整前核对路径、管理端口及部署网络边界。
- 本模块使用自身 Nacos 发现配置，不直接套用其它服务的 profile 导入机制。对外暴露 `*` 只是端点暴露声明，最终菜单和访问结果仍取决于实例端点、地址、网络和认证。

## 源码依据

- 启动与扫描：`ZeroBootAdminApplication`、`PackageProperties`；安全：`SecuritySecureConfig`；发现与通知：`NacosWatch`、`CustomNotifier`、`SlackRobotNotifier`，以及各服务实际生效的 Actuator 配置。

## 常用验证命令

- 监控模块编译：`mvn -pl zero-bootAdmin -am compile -DskipTests`
