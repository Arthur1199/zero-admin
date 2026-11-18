# zero-jobAdmin

基于 XXL-JOB 3.5.0 官方调度中心源码，使用当前工程的 Java 17、Spring Boot 3.5、Spring Cloud Alibaba 基线，保留官方独立控制台和登录。Maven 模块名为 `zero-jobAdmin`，Nacos 服务名为 `zero-job-admin`，默认端口 `9102`，控制台路径为 `/`。

## 初始化与启动

1. 经数据库管理员确认后，在独立测试环境执行仓库的 `sql/zero_job.sql`。脚本包含建库、建表和官方初始数据，仅用于首次初始化，不能作为已有数据库升级脚本重复执行。应用不会自动执行 SQL。
2. 初始化脚本保留官方示例账号、密码摘要和示例执行器配置。首次登录后修改密码；执行器管理中设置自己的 AccessToken，配置实际执行器地址，不使用官方示例凭据或示例任务进行生产调度。源码中的初始账号信息不应继续对外传播。
3. 按下表在 YAML 中直接填写运行配置，数据库、邮件及 Nacos 账号密码暂留空。真实凭据不要提交到仓库或输出到日志。
4. 如使用配置中心，将 `src/main/resources/nacosConfig/` 的两个 YAML 文件分别发布到对应 namespace 和配置分组。文件名即 Data ID；仅将文件保存在源码目录中不会自动发布。
5. 从仓库根目录运行：

```powershell
java -version
mvn -version
mvn -pl zero-jobAdmin -am package
java -jar zero-jobAdmin/target/zero-jobAdmin-1.0.0.jar
```

访问 [控制台](http://localhost:9102/)，Nacos 应显示 `zero-job-admin`。当前交付没有连接实际数据库或 Nacos，注册和真实任务闭环仍需使用已初始化的测试环境验证。

## 环境与配置

四个 `application*.yml` 直接写配置值，Nacos导入的group按 `${spring.profiles.active}` 引用当前环境，不使用自定义环境变量占位符；当前保留原默认值，不复制其它服务的账号密码。

| 配置位置 | 用途与默认值 |
| --- | --- |
| `application.yml` 的 `spring.profiles.active` | 默认 `dev`，可通过 `--spring.profiles.active=test/prod` 切换 |
| `application.yml` 的 `server.port` | 默认 `9102` |
| Nacos `zero-job-admin-mysql.yml` 的 `spring.datasource` | 本机MySQL的 `zero_job` 库、Hikari连接池，账号密码待填写 |
| Nacos `zero-job-admin-common.yml` 的 `spring.mail` | 默认 `localhost:25`，邮件账号、密码和发件人待填写 |
| 环境文件的 `spring.cloud.nacos.discovery` | 地址 `127.0.0.1:8848`、分组 `DEFAULT_GROUP`、启用注册；namespace及账号密码待按环境填写 |
| 环境文件的 `spring.cloud.nacos.config` | 地址 `127.0.0.1:8848`，dev/test/prod分别使用同名配置分组；namespace及账号密码待按环境填写 |
| 环境文件的 `spring.config.import` | 两个Data ID的group使用 `${spring.profiles.active}`，与zero-admin一致 |

namespace为空表示公共空间；需要和其它服务注册在同一空间时填写相同的namespace ID。配置导入的Data ID为 `zero-job-admin-mysql.yml` 和 `zero-job-admin-common.yml`。Nacos可覆盖本地值，启动参数可进一步覆盖。

数据库连接及Hikari参数仅在 `nacosConfig/zero-job-admin-mysql.yml` 定义，直接填写配置值，不使用占位符；须发布到Nacos才能加载，`application.yml` 不保留本地数据源兜底。邮件完整配置（包含SMTP认证、TLS和超时）以及 `xxl.job` 调度参数仅在 `nacosConfig/zero-job-admin-common.yml` 定义，直接填写配置值，不使用占位符；`application.yml` 不再重复定义，须发布到Nacos后加载。官方调度参数在启动时初始化，修改后统一重启生效。

本模块保留官方 Hikari 单数据源和 MyBatis XML，不使用 report 的动态数据源或 MyBatis-Plus。日志写入 `logs/zero-job-admin`（相对于启动目录）。与report/file一致，HTTP监控使用 `management.endpoints.web.exposure.include: "*"` 暴露全部已启用Actuator端点，健康检查显示组件详情。`/actuator` 和其子路径无需控制台登录，Boot Admin可读取指标、线程、日志级别、Bean、环境及映射等信息。部署时必须限制内部访问来源；全量暴露不等于启用默认关闭的端点（例如shutdown），也不自动生成未配置的信息贡献项。

## 执行器及集群

- 执行器使用 `xxl-job-core:3.5.0`，`xxl.job.admin.addresses` 填调度中心可访问的 HTTP 地址，例如 `http://调度中心主机:9102`。这不是 Nacos 服务名解析入口，不可直接填写 `http://zero-job-admin` 并假定官方执行器支持服务发现。
- 执行器 `appname` 与控制台执行器 AppName 一致，AccessToken 与该执行器在控制台保存的值一致。3.5.0 的凭据按执行器隔离；调度中心和执行器配套升级。
- Nacos 注册只用于服务发现，不替代 XXL-JOB 原生的注册、心跳、回调与任务执行协议。调度中心和执行器需要双向网络可达。
- 多调度中心实例共用同一个调度数据库，沿用官方数据库锁与集群调度逻辑。浏览器控制台保持官方数据库登录存储，不接入 zero-admin 的 JWT 或按钮权限。
- 保留官方接口响应格式与 `/api/{uri}` 通信入口；接口仍检查执行器 AppName、AccessToken。服务部署在受控网络，不将注册到 Nacos 等同于新增服务间鉴权。
- 现有 zero-admin Quartz 功能和其它服务未修改，不自动迁移历史任务或新增执行器。

## 验证

默认 `mvn -pl zero-jobAdmin -am package` 运行隔离兼容性测试，覆盖实际登录模板渲染、静态资源、未登录拦截、健康检查、通信参数校验以及全部 Mapper XML、模板解析，不连接数据库或 Nacos，不执行调度任务。

官方测试保存在 `src/integration-test/java`，默认不编译、不运行，其中有真实写库、任务触发和停止操作。`job-integration` profile 用于显式加入这些测试源码；必须先审阅对应测试、替换官方示例地址及凭据，并取得外部写操作授权，不能直接在生产环境运行。仅检查其编译可使用：

```powershell
mvn -pl zero-jobAdmin -am -Pjob-integration test-compile -DskipTests
```

部署联调验收：服务注册到正确 namespace 和 discovery group；健康检查正常；控制台登录及列表正常；测试执行器可注册；经授权触发专用测试任务并确认日志、回调；退出登录后受保护页面不可访问。此流程需单独准备数据库、Nacos、执行器，本次不自动执行。

官方来源、许可证和改造差异见 `upstream/README.md`；原始数据库脚本保存在 `upstream/tables_xxl_job-3.5.0.sql`。
