# zero-jobAdmin 模块规则

本文件适用于 `zero-jobAdmin/`。同时遵守[仓库全局规则](../AGENTS.md)，通用 Java/YAML 注释规范及安全要求以根文件为准。下文带模块名前缀的代码路径相对仓库根目录，其余模块内部路径相对本目录；Markdown 链接相对本文件。跨模块修改同时读取相关模块规则。

## 职责与源码入口

| 事项 | 当前入口 |
| --- | --- |
| 启动与调度装配 | [ZeroJobAdminApplication.java](src/main/java/com/zero/ZeroJobAdminApplication.java)、[XxlJobAdminBootstrap.java](src/main/java/com/zero/jobadmin/business/scheduler/config/XxlJobAdminBootstrap.java) |
| 调度业务与执行器协议 | [OpenApiController.java](src/main/java/com/zero/jobadmin/business/scheduler/openapi/OpenApiController.java)、[AdminBizImpl.java](src/main/java/com/zero/jobadmin/business/scheduler/openapi/impl/AdminBizImpl.java)、[AdminJobBizImpl.java](src/main/java/com/zero/jobadmin/business/scheduler/openapi/impl/AdminJobBizImpl.java) |
| 后台页面入口 | [JobInfoController.java](src/main/java/com/zero/jobadmin/business/controller/JobInfoController.java)、[JobLogController.java](src/main/java/com/zero/jobadmin/business/controller/JobLogController.java)、[JobGroupController.java](src/main/java/com/zero/jobadmin/business/controller/JobGroupController.java)、[LoginController.java](src/main/java/com/zero/jobadmin/framework/controller/LoginController.java) |
| 数据库映射 | [XxlJobGroupMapper.java](src/main/java/com/zero/jobadmin/business/mapper/XxlJobGroupMapper.java)、[XxlJobGroupMapper.xml](src/main/resources/mapper/business/XxlJobGroupMapper.xml) |
| 测试与来源 | [pom.xml](pom.xml)、[README.md](upstream/README.md) |

## 职责与原生协议

- `zero-jobAdmin` 基于官方 XXL-JOB 3.5.0 源码，入口 `com.zero.ZeroJobAdminApplication`，调度业务包 `com.zero.jobadmin`；保持官方控制台、独立登录及原生响应/调度协议，不套用 admin JWT 或 report 的业务父类。
- 使用当前父工程 Java 21 / Boot 4.0 / Spring 7.0 基线，core 版本 3.5.0，MyBatis starter 使用 Boot 4 适配版本，单数据源 Hikari 连接独立 `zero_job` 库。
- Nacos发现不替代XXL-JOB执行器注册协议，执行器仍需配置可访问的调度中心HTTP地址，并使用3.5.0及对应AppName/AccessToken。

- 当前代码按 `com.zero.jobadmin.business` 与 `com.zero.jobadmin.framework` 分层；业务 Controller、调度引擎及 OpenAPI 在 business，登录和框架入口在 framework，Mapper XML 对应 `mapper/business` 与 `mapper/framework`。不要按上游旧目录查找或新建重复入口。
- `/api/{uri}` 是原生调度 API，不依赖控制台登录。入口要求 POST 和有效请求体，并按请求携带的 AppName 加载当前执行器组、校验 AccessToken，将组范围传给下游处理；不能将“无需控制台登录”误写为“无需鉴权”。修改注册、回调或任务操作时同时检查组范围与原生响应兼容。

## 注册与配置生效

- Nacos 注册名 `zero-job-admin`、默认端口 9102；discovery默认DEFAULT_GROUP，配置默认按profile分组。Data ID 为 `zero-job-admin-mysql.yml`、`zero-job-admin-common.yml`，四个application配置文件直接写配置值、凭据暂留空，数据源整段仅在Nacos的zero-job-admin-mysql.yml直接配置，邮件整段和xxl.job调度参数仅在Nacos的zero-job-admin-common.yml直接配置，不使用环境变量占位符。三个环境均监听这两份配置，发布变更后由调度中心重启并重新装配。

## 初始化与兼容边界

- [调度库初始化脚本](../sql/zero_job.sql) 为初始化重建脚本，每次执行均清空并重建 8 张调度表，再插入初始数据。执行需经用户确认。来源、许可证及适配差异见 [上游来源说明](upstream/README.md)。既有Quartz不迁移、不替换。

## 验证

- 默认验证：`mvn -pl zero-jobAdmin -am package`。官方有外部写操作的测试位于`src/integration-test/java`，仅显式`job-integration` profile加入，不在未经授权时运行。编译或隔离测试通过不代表真实Nacos、数据库及执行器闭环已验证。

- Java 兼容回归入口为 `JobAdminCompatibilityTest`；页面行为测试位于 `src/test/js`。`job-integration` 中的用例可能真实写库、派发或停止任务，不能因路径带 test 就认定只读。

## 关联规则

- 注册和任务执行侧见 [zero-task 规则](../zero-task/AGENTS.md)，初始化与启动说明见[模块 README](README.md)。
