# zero / zero-admin 项目规则

## 基本要求

- 默认使用简体中文回答，表达要直接、精炼、专业。
- 先按任务涉及的业务和数据定位模块。用[项目整体架构](#项目整体架构)定位模块，再按[管理后台业务开发模式规则](#管理后台业务开发模式规则)确认调用和权限边界。新建后端模块看[新建 Java 项目规则](#新建-java-项目规则)，代码和配置注释分别看[Java 注释规范](#java-注释规范)和[YAML 配置注释规范](#yaml-配置注释规范)。前端改动同时遵守 [zero-ui/AGENTS.md](zero-ui/AGENTS.md)。
- 每次代码分析、修改、重构或审查前，必须重新读取目标文件、相关调用方、被调用方、配置和同类实现，以当前工作区最新代码为准。涉及服务调用时同时核对双方入口、代理配置和服务名。
- 发现工作区已有未提交或未跟踪改动时，只能在本次任务范围内追加必要修改，不得回退、格式化、删除或覆盖用户已有改动。
- 涉及删除、授权、发送消息、数据库改动、生产配置、敏感信息处理时，先提醒风险并让用户确认。
- 用户提供或文件中出现密码、Token、密钥、数据库账号等敏感信息时，不要在回复中复述明文，提醒不要继续暴露。

## 项目整体架构

| 模块目录（必要时注明服务名） | 职责与边界 |
| --- | --- |
| `zero-tools` | Java 基础工具包，无独立进程。`zero-report`、`zero-file` 和 `zero-task` 复用常量、返回对象和通用工具。`zero-admin` 复用 SQL 日志格式化工具，并在其 POM 中排除工具包的全部传递依赖。 |
| `zero-admin` | 管理后台后端和统一业务入口，负责登录、菜单权限和后台自身业务，并在代理入口校验操作权限和记录审计。 |
| `zero-report` | 管理后台的报表和业务数据服务，由 admin 调用，负责报表与其所属业务数据的查询、管理和持久化。 |
| `zero-jobAdmin`（服务名 `zero-job-admin`） | 独立的 XXL-JOB 调度中心，负责管理任务、接收执行器注册、派发任务和收集执行结果。 |
| `zero-task` | XXL-JOB 执行服务，向调度中心注册，接收任务、运行 `@XxlJob` 处理器并回传执行结果。 |
| `zero-bootAdmin`（默认服务名 `zero-boot-admin`） | 独立的 Spring Boot Admin 监控服务，通过服务发现和 Actuator 查看微服务运行状态，不参与核心业务调用。 |
| `zero-file` | 独立文件存储服务，负责本地磁盘和已适配的云存储平台，并维护 `biz_file_detail` 文件记录。管理后台通过 admin 代理访问。 |
| `zero-ui` | Vue 管理后台前端，负责页面、交互和数据展示，管理后台业务 API 统一请求 `zero-admin`。 |

- 管理链路为 `zero-ui → zero-admin`。后台自身业务由 admin 处理。报表及所属业务数据通过 `ReportRestService` 调用 report，文件管理通过 `FileRestService` 调用 file。两条业务代理链路都使用按服务名寻址的负载均衡 RestTemplate，由客户端拦截器追加管理员上下文。admin 的报表探活入口通过 OpenFeign 客户端 `RemoteReportHealthService` 验证 admin 到 report 的调用。新增功能按数据归属落位，参见[管理后台业务开发模式规则](#管理后台业务开发模式规则)。
- 调度链路由 `zero-task` 向 `zero-job-admin` 注册。调度中心按任务配置派发，执行器运行后回传结果。Nacos 发现和配置与 XXL-JOB 注册和派发各有独立配置。
- 监控链路由 `zero-bootAdmin` 发现实例并访问各服务实际开放的 Actuator HTTP 端点。部署时分别核对服务名、命名空间、管理端点可达性和访问权限。

## 新建 Java 项目规则

- 新建 Java 项目先确定模块职责、数据归属和调用方，再以 `zero-task` 为集成参考。实施前核对根 `pom.xml`、`zero-task/pom.xml`、启动类、`application*.yml`、`nacosConfig` 和[zero-task 模块说明](zero-task/README.md)。仓库内新模块沿用父工程版本管理并登记到 Maven 聚合模块，并核对启动类的组件扫描和持久层 Mapper 扫描范围。按业务需要接入 Nacos、Actuator、MySQL 和 Redis，只有承担任务执行职责的服务才接 XXL-JOB。
- 以 [zero-task 的 Logback 配置](zero-task/src/main/resources/logback-spring.xml)为样板，控制台和应用文件使用相同格式，并与 admin、report、file、job-admin 保持一致。格式包含毫秒时间、线程、级别、Logger、方法、行号和消息。日志按服务设置独立目录，沿用 `sys-info.log`、`sys-error.log` 的分流和滚动方式。接入 Boot Admin 日志查看时，核对 Logback 写入的 `application.log` 与 Actuator logfile 读取的是同一文件。
- 为新项目生成的所有 `.yml` 和 `.yaml` 配置都要就近添加中文注释，包括主配置、各环境配置及 Nacos 配置源。注释内容、覆盖关系及凭据示例按 [YAML 配置注释规范](#yaml-配置注释规范)执行。
- 每个新 Java 项目都要集成 MyBatis-Plus 代码生成器。参考 [TaskCodeGenerator.java](zero-task/src/test/java/com/zero/generator/TaskCodeGenerator.java)，使用测试作用域的 MyBatis-Plus Generator 和 Velocity，提供显式执行入口，从本地环境变量读取数据库连接信息，并要求显式指定表名。生成实体、Mapper、Mapper XML、Service 接口和实现，默认跳过已有文件。生成器不随服务启动执行。
- 生成器包名、输出目录及 XML 扫描路径按新项目调整并保持一致。沿用 task 单层资源目录时，XML 写入 `src/main/resources/mapper`，文件以 `Mapper.xml` 结尾。持久层按开关装配时，生成的 Service 模板也要遵循相同开关。模块 README 写清生成命令、输入及输出目录，用隔离测试验证生成产物及重复执行时跳过已有文件，并验证模块可构建。

## 技术栈与版本来源

- 后端是 Maven 多模块项目，父工程 `pom.xml` 当前模块为：`zero-tools`、`zero-admin`、`zero-bootAdmin`、`zero-report`、`zero-file`、`zero-jobAdmin`、`zero-task`。
- 父 POM 声明的版本基线为 Java 17、Spring Boot 3.5.16、Spring Framework 6.2.19、Spring Cloud 2025.0.2、Spring Cloud Alibaba 2025.0.0.0。Spring Boot 核心、自动配置和打包插件统一继承官方父 POM 的版本管理。排查依赖冲突时先看父 POM 的版本管理，再用对应模块的有效 POM 或依赖树确认解析结果。
- 修改 Maven 依赖时，在对应声明上方维护中文备注，说明实际用途、作用域或版本对齐原因。父 POM 的 `dependencyManagement` 管理版本，模块的 `dependencies` 声明实际使用的组件。升级后核对受影响模块的依赖树与回归结果，保留仍待处理的安全告警记录。
- `zero-admin` 是管理后台服务，主入口为 `com.zero.ZeroAdminApplication`，启用 Nacos 注册/配置、OpenFeign、LoadBalancer，并排除默认数据源自动配置。主要技术包括 Spring Security、JWT、MyBatis XML、PageHelper、Druid、Redis、Quartz、Actuator、SpringDoc、Kaptcha、Fastjson2、POI。
- 系统接口文档使用 SpringDoc 2.8.17，与当前 Boot 3.5 / Spring 6.2 配套；文档子路径 `/v3/api-docs/**` 仅额外放行 GET，保证 Swagger UI 可读取配置和文档，不扩大业务接口的匿名访问。
- Swagger3Config声明HTTP Bearer/JWT方案；后台Axios登录请求头不会自动继承到Swagger，调试受保护接口须在Authorize输入原始Token，不带Bearer前缀。不要把Token写入文档、日志、URL或测试产物。
- `zero-report` 是报表/业务服务，主入口为 `com.zero.ZeroReportApplication`，启用 Nacos 注册/配置、OpenFeign、`@MapperScan`。主要技术包括 MyBatis-Plus、MyBatis-Plus 分页拦截器、dynamic-datasource、PageHelper（复杂 XML 报表 SQL 保留使用）、Redis、Actuator、Validation、Lombok。
- `zero-tools` 的 JWT 工具使用 `com.auth0:java-jwt`，当前为 4.6.2。`zero-admin` 的登录令牌使用 `io.jsonwebtoken:jjwt-*`，版本由父 POM 管理。维护依赖时分别核对调用方，不将只属于 admin 的 `common` 类移动到 `zero-tools`。
- `zero-bootAdmin` 使用 Spring Boot Admin 3.5.8。分析监控功能时需单独读取该模块和各服务 Actuator 暴露配置。
- `zero-report`、`zero-file` 和 `zero-task` 使用父 POM 管理的 MyBatis-Plus 3.5.16 和 dynamic-datasource 4.6.0。report 和 task 分别在模块 POM 中管理 Redisson 4.6.1，并使用适配 Spring Data 3.5 的模块。
- `zero-file` 是独立文件服务，入口为 `com.zero.ZeroFileApplication`，通过 `@EnableFileStorage` 启用存储能力，使用 MyBatis-Plus、dynamic-datasource 和 `zero-tools`，文件记录表为业务库的 `biz_file_detail`。
- 前端模块：`zero-ui`，Vue 3.5.43 + Vite 8.3.3 + Vue Router 4.6.4 + Vuex 4.1.0 + Element Plus 2.14.7，保留 Options API。Node 开发基线为 24.21.0，`package.json` 声明的引擎范围为 `>=24.21.0 <25`。
- 前端请求：Axios 封装在 `zero-ui/src/utils/request.js`，开发代理在 `zero-ui/vite.config.mjs`，浏览器变量使用 `VITE_APP_*`。Vite 开发代理由 `VITE_DEV_PROXY_TARGET` 覆盖，默认指向 `http://localhost:9099`。
- Vue3升级后的构建、缓存、主题和固定列规则以 `zero-ui/AGENTS.md`、`前段项目升级方案` 为准。当前固定列采用 Element Plus sticky 单元格，不能按旧镜像表结构新增覆盖。
- 初始化脚本按归属维护。`sql/zero_admin.sql` 对应管理库，包含 `sys_*`、`gen_*` 等后台表。`sql/zero_biz.sql` 对应业务库，包含业务字典和文件记录。`sql/quartz.sql` 对应既有 Quartz 表，`sql/zero_job.sql` 对应独立的 XXL-JOB 调度库。

## 后端结构规则

- 包路径以 `com.zero` 为根。
- `zero-admin` 公共能力集中在 `common`：注解、常量、基础返回、分页、Redis、工具类、异常、XSS。
- `zero-admin` 框架能力集中在 `framework`：Security、JWT 过滤器、数据源、AOP、全局异常、登录服务。
- `zero-admin` 系统业务集中在 `system`：`domain`、`mapper`、`service`、`service.impl`。
- `zero-admin` Web 入口集中在 `web.controller`：`system`、`monitor`、`common`、`tool`；admin 代理 report 的 Controller 单独放在 `web.reportController`。
- `zero-admin` 代码生成在 `generator`，定时任务在 `quartz`。Mapper 接口放在 `com.zero.**.mapper`，XML 放在 `zero-admin/src/main/resources/mapper/**`，由 `ApplicationConfig` 的 `@MapperScan` 和 `application.yml` 的 `mapperLocations` 扫描。
- admin 页面使用的代码生成模板与新项目使用的 MyBatis-Plus 开发期生成器各有入口。admin 的 13 个 classpath 模板位于`zero-admin/src/main/resources/vm/`，原件及来源在`前段项目升级方案/若依官方代码生成模板-v3.9.0/`，验证见`04-代码生成模板集成与验证报告.md`。新增导入表和历史空前端类型默认Vue3，显式Vue2保留；Vue3模板沿用Options API及项目公共组件，Controller输出到`web/controller/模块`。修改后先运行`GeneratorTemplatesTest`验证六类渲染、ZIP及生成Java/XML，再运行读取其产物的前端用例，不以项目自身编译代替生成结果验证；不自动执行生成SQL或开启源码覆盖。
- `zero-report` 结构以当前代码为准：`controller` 放 HTTP 接口，`service` / `service.impl` 放业务规则，`mapper` 放 MyBatis-Plus Mapper，`domain` 放实体，`queryDto` 放查询参数，`dto` 放服务内 DTO/返回结构，`base` 放分页和 Service 父类，`config` 放 MyBatis-Plus、异常、Feign、XML 加载等配置。
- `zero-report` 的 Mapper 扫描由 `ZeroReportApplication` 上的 `@MapperScan(PackageProperties.MAPPER_SCAN)` 和 `PackageProperties` 统一维护；新增包路径前必须确认 `PackageProperties` 是否需要同步。
- `zero-file` 使用自身的 `packageconfig`、`base.BaseQuery`、`config`、`controller`、`service`、`mapper`、`domain`、`queryDto`、`dto`；存储实现放 `platform`，记录回调放 `recorder`，存储操作和预处理放 `vo`，扩展链放 `aspect`。不要因包名相同就混用 report 的类。

## 服务配置与启动边界

下表记录主配置声明值。运行值需结合环境变量、启动参数及实际导入的 Nacos 配置核对。

| 模块 | 启动类（均在 `com.zero`） | 主配置 HTTP 端口 | 主配置应用名 |
| --- | --- | --- | --- |
| zero-admin | ZeroAdminApplication | 9099 | zero-admin |
| zero-bootAdmin | ZeroBootAdminApplication | 9100 | zero-boot-admin，可由 `zero.boot-admin.application-name` 覆盖 |
| zero-report | ZeroReportApplication | 9101 | zero-report |
| zero-file | ZeroFileApplication | 9103 | zero-file |
| zero-jobAdmin | ZeroJobAdminApplication | 9102 | zero-job-admin |
| zero-task | ZeroTaskApplication | 9104 | zero-task |
| zero-tools | 无独立服务入口 | 无 | 无 |

`zero-task` 的 9104 是应用 HTTP 端口。XXL-JOB 派发使用独立的执行器端口，配置源默认值为 9998。

- admin 的 `ServiceNameConstants.ZERO_ADMIN` 和 tools 的 `ServiceNameConstant.ZERO_ADMIN` 当前仍为 `zeroAdmin`，与 admin 本地应用名 `zero-admin` 不一致。涉及服务调用时核对 Nacos 实际注册名和调用双方配置，不宣称两者已一致，不在无关任务中直接改名。
- 各服务读取自己的 `application.yml` 和环境配置。`zero-bootAdmin` 使用自身配置接入服务发现，其余服务按 `application-{profile}.yml` 的 `spring.config.import` 读取 Nacos 配置。`resources/nacosConfig` 保存发布模板，实际生效值以目标环境已发布配置和启动覆盖项为准。
- 新增或修改 Spring 和 Nacos 配置按 [YAML 配置注释规范](#yaml-配置注释规范)执行。各服务的配置源见 [README.md](README.md#后端配置入口)。
- admin 的 `localYml` 导入在主配置中当前被注释，不把该目录的数据库、Redis、上传限制当作生效配置。分析配置问题要同时检查本地文件、Nacos 和启动覆盖参数。
- 包扫描规则按各服务自己的 `PackageProperties` 核对；监控服务的安全配置、通知器和 Actuator 暴露范围单独分析，不能沿用 admin JWT 假设。

## 管理后台业务开发模式规则

新增管理后台菜单或页面前，必须先判断数据和业务归属。判断结果决定后端落位、调用方式、权限边界和前端目录。
当前源码包括 admin 自身业务（`system/config`）、会联动全局请求的 admin 配置（`system/apiAuth`）、admin 代理 report 业务（`reportSystem/bizDict`），以及 admin 代理文件服务（`systemTools/fileManagement`）。开发前先归类，文件服务代理不套用 report 的目录和 Service 父类。

### 模式一：admin 自身业务

- 适用场景：功能直接查询或维护 `zero_admin` 管理后台自身数据，例如 `sys_*`、监控、工具、参数设置、用户、角色、菜单、部门、岗位、日志、缓存等。
- 后端按若依既有结构开发：Controller 放在 `zero-admin/src/main/java/com/zero/web/controller` 对应子包，Service 放在 `system/service` 或同类模块，Mapper 接口放在 `system/mapper`，XML 放在 `zero-admin/src/main/resources/mapper/**`。
- `zero-admin` Controller 默认继承 `BaseController`，列表接口使用 `startPage()` + Service 查询 + `getDataTable(list)`，普通增删改查使用 `AjaxResult`、`success()`、`error()`、`toAjax(rows)`。
- 不是所有 admin 自身列表都返回 `TableDataInfo`：树形菜单类接口参考 `SysMenuController.list`，返回 `AjaxResult.success(list)`；分页表格类接口参考 `SysConfigController.list`、`SysApiAuthController.list`。
- Service 层负责业务规则、唯一性校验、数据权限、事务和缓存维护；Controller 只做参数接收、权限注解、操作日志、调用 Service 和返回封装。
- 当前登录人、审计字段和权限上下文使用 admin 现有能力，例如 `getUsername()`、`SecurityUtils`、`@DataScope`、Redis 缓存等，不要绕到 report 服务。
- 页面辅助接口按现有风格处理：例如 `SysMenuController.treeselect`、`SysApiAuthController.googleAuthApis` 不配置按钮权限，但仍走登录态和全局安全链路；不能把“无按钮权限”误理解为匿名公开。
- 涉及前端全局行为的 admin 配置接口要同步看前端调用方。例如 `SysApiAuthController.googleAuthApis` 被 `zero-ui/src/permission.js` 登录后刷新缓存，`zero-ui/src/utils/request.js` 会按缓存规则触发谷歌验证码弹窗。
- 前端页面按原目录归类，例如 `zero-ui/src/views/system`、`zero-ui/src/views/monitor`、`zero-ui/src/views/tool`；请求文件放 `zero-ui/src/api/system`、`zero-ui/src/api/monitor`、`zero-ui/src/api/tool` 等同类目录。
- 典型参考：`SysConfigController` + `ISysConfigService` + `SysConfigMapper.xml` + `zero-ui/src/views/system/config/index.vue` + `zero-ui/src/api/system/config.js`；接口谷歌验证参考 `SysApiAuthController` + `zero-ui/src/views/system/apiAuth/index.vue`。

### 模式二：admin 调 report 服务业务

- 适用场景：页面入口在管理后台，但真实业务规则属于 `zero-report`，例如“业务参数设置”维护 `zero_biz` 中的 `biz_dict_group`、`biz_dict_item`。同在 `zero_biz` 的文件记录仍由 `zero-file` 维护。按表和业务操作的所有权判断归属。
- admin 对 report 业务只做统一入口，使用 `com.zero.feign.ReportRestService` 代理业务请求。业务库访问、Mapper 和 Service 逻辑由 report 维护。现有 `ReportHealthController` 使用独立的 Feign 探活链路。
- admin 代理 Controller 必须放在 `zero-admin/src/main/java/com/zero/web/reportController`。Controller 职责只包含：路由映射、`@RequiresPermissions`、`@Log`、参数透传、返回结构声明和 `ReportRestService` 调用。
- `ReportRestService` 使用名为 `reportRestTemplate` 的 `@LoadBalanced RestTemplate` 调用 `http://zero-report`；`ReportRestTemplateConfig` 会通过拦截器追加管理员上下文请求头，不要在每个业务 Controller 里手动拼这些请求头。
- `AdminForwardHeaderProvider` 负责从当前 admin 登录态和请求中提取 `Authorization`、客户端 IP、管理员ID、账号、昵称、公司名称、角色ID、是否超级管理员，并通过 `ForwardHeaderConstants` 统一命名。中文请求头值会 URL 编码，report 侧再解码。
- `ReportRestService` 会校验 report 路径必须以 `/` 开头且不能是完整 URL，避免把代理能力变成任意转发；新增代理接口只能传服务内路径，例如 `/reportSystem/bizDict/group/list`。
- `ReportRestService.exchange` 对 `AjaxResult` 和 `TableDataInfo` 有统一失败兜底：调用 report 异常时返回固定结构和通用错误提示，并只记录路径、方法和异常，不向前端暴露下游堆栈或敏感信息。
- admin 代理 Controller 不需要给每个 `ReportRestService` 调用再包一层 `try/catch`。下游不可达、路径非法、返回空体时由 `ReportRestService` 兜底；report 内部业务异常由 report 的统一异常处理转成固定响应结构。
- `ReportRestService.invokeGetByObj` 只适合查询对象，会用 `ObjectMapper` 转成 Map；单个 ID、字符串、枚举等基础值 GET 参数必须用 `invokeGetByMap` 或 `exchange` 显式指定参数名，避免丢失查询参数名。
- report 服务负责真实业务：Controller、Service、Mapper、实体、查询 DTO、MyBatis-Plus 查询、唯一性校验、软删除/恢复、审计字段、事务和异常处理都放在 `zero-report`。
- report Service 通过 `ReportBaseService` 使用 `ReportAdminContext` 已提供的上下文获取方法，例如 `getAdminId()`、`getAdminName()`、`getAdminRoleIds()`、`isAdmin()`、`getClientIp()`、`getAuthorization()`、`getSourceService()`；不要为了兼容不存在的字段自行拼请求头。
- report 分页查询参数优先继承 `BaseQuery`，使用 `query.toPage()` 生成 MyBatis-Plus `Page`，不要在每个 Service 实现里重复写最大 `pageSize` 常量。日期范围使用 `hasBeginTime()`、`getBeginDateTime()`、`hasEndTime()`、`getEndExclusiveDateTime()` 组成半开区间，避免手写 `" 00:00:00"`、`" 23:59:59"` 字符串。
- report 列表接口返回 `com.zero.dto.TableDataInfo`，普通详情、新增、修改、删除、恢复、下拉选项等返回 `com.zero.model.AjaxResult`。admin 代理调用时必须声明对应返回类型，列表用 `exchange(..., TableDataInfo.class)`，普通接口用 `AjaxResult`。注意 admin 侧同名类型来自 `com.zero.common.*`，report 侧普通返回来自 `zero-tools` 的 `com.zero.model.AjaxResult`。
- report 服务已有 `ReportExceptionHandler` 统一处理异常：列表接口转成 `TableDataInfo.error`，普通接口转成 `AjaxResult.error`。Service 中不需要给每个 MyBatis-Plus 查询都包一层 `try/catch`；业务可预期错误抛 `IllegalArgumentException` 并给清晰提示，未知异常交给全局异常处理记录日志。
- 按前述内部路径规则传参。`ReportRestService` 的 GET 查询用 `invokeGetByMap` / `exchange`，POST JSON 用 `invokePostJson`，PUT/DELETE 或需要指定返回类型时用 `exchange`。
- 写操作权限和操作日志放在 admin 代理 Controller 上，例如 `@RequiresPermissions("reportSystem:bizDict:add")`、`@Log(title = "...", businessType = BusinessType.INSERT)`；report Controller 不重复做管理后台按钮权限。
- 查询详情接口如果只用于列表页修改弹窗回显，通常不单独加 `query` 按钮权限，能进入列表并具备修改按钮权限即可完成流程；如果接口会独立展示敏感详情，再单独配置查询权限。
- 页面初始化用的普通下拉列表、枚举选项等接口，如果需求明确“不需要按钮权限”，admin 代理 Controller 不加 `@RequiresPermissions`，但它仍受登录态保护，不等同于匿名公开接口。
- report 侧这类接口如果被直接访问，当前并不会自动拥有 admin 按钮权限语义；安全边界应放在网关、内网访问、admin 代理层和必要的服务间鉴权上，不要把 report 接收到的自定义请求头当成公网可信身份。
- 删除和恢复如果产品要求共用权限，则恢复接口和恢复按钮复用删除权限，例如 `reportSystem:bizDict:remove`；按钮文案可随状态显示为“删除”或“恢复”。
- 新增菜单 SQL 默认只写 `sys_menu` 菜单和按钮权限定义；除非用户明确要求给某个普通角色默认授权，否则不要新增 `sys_role_menu` 普通角色绑定，避免非超级管理员默认看到新菜单。
- admin + report 联动功能的前端文件默认独立归类：页面放 `zero-ui/src/views/reportSystem`，请求文件放 `zero-ui/src/api/reportSystem`；后端 admin 代理路径和前端 API 路径保持一致，例如 `/reportSystem/bizDict/**`。
- 典型参考：`BizDictController` + `ReportRestService` + `zero-report` 的 `DictGroupController`、`DictItemController`、`DictGroupServiceImpl`、`DictItemServiceImpl` + `zero-ui/src/views/reportSystem/bizDict/index.vue` + `zero-ui/src/api/reportSystem/bizDict.js`。

### 模式三：admin 调文件服务

- 管理入口为 `web.controller.systemTools.FileManagementController`，前端页面和 API 位于 `zero-ui/src/views/systemTools/fileManagement/index.vue`、`zero-ui/src/api/systemTools/fileManagement.js`。admin 路径 `/systemTools/fileManagement/**` 显式映射到 file 的 `/file/**`，不要按当前请求路径自动透传。
- 文件调用统一复用 `FileRestService` 和名为 `fileRestTemplate` 的 `@LoadBalanced RestTemplate`，目标为 `http://zero-file`；管理员上下文仍由 `AdminForwardHeaderProvider` 拦截追加。multipart 上传复用 `uploadFile/uploadFiles`，不要改成 JSON 或额外手写 HTTP 客户端。
- 文件列表在 admin 使用 `com.zero.common.core.page.TableDataInfo`，file 使用自己的 `com.zero.dto.TableDataInfo`；普通 file 返回来自 tools 的 `com.zero.model.AjaxResult`。代理失败结构由 `FileRestService` 兜底，file 异常由 `FileExceptionHandler` 处理。
- `sys.file.storage.mode` 集中定义在 `IAdminFileStorageModeService`；当前只有 `local`（忽略大小写并 trim）走 admin 本地上传，其它值和空配置都走 `zero-file`。通用单/多文件上传和头像上传读取该配置；文件管理上传始终走文件服务，不受此开关切回本地。
- 上传流程复用 `AdminFileUploadServiceImpl`，保持扩展名、文件大小、原始文件名校验和历史响应字段。当前业务类型分别为 `admin-common`、`admin-avatar`、`file-management`，不能把不同来源文件混成同一种类型。
- 上传限制同时受 admin、file、反向代理及前端校验影响。文件管理页面读取 admin 的 `/upload/config`，不能只修改页面默认提示。file 本地单文件/整请求默认 30MB/300MB，admin Nacos common 源文件为 30MB/60MB；实际限制按运行配置核对。
- 文件记录使用 `biz_file_detail`，默认数据源 key 为 `zeroBiz`；`FileDetailServiceImpl` 继承 MyBatis-Plus `ServiceImpl` 并通过 `IFileDetailService` 实现 `FileRecorder`。不要套用 `ReportBaseService` 或报表业务的软删除规则。
- `FileStorageProperties` 配置前缀为 `spring.file-storage`；存储平台的启用、默认平台、域名、磁盘目录和云存储配置以当前配置为准。`DefaultFileRecorder` 仅为空实现兜底，不能宣称能持久化或查询文件记录。
- 文件删除通过 `FileStorageService` 删除实际存储对象，再由 `FileRecorder` 清理数据库记录；数据库事务不能回滚外部磁盘/对象存储。admin 多 ID 删除逐个调用，前面成功后后续失败可能部分完成，不能宣称批量原子性或套用字典恢复逻辑。
- 当前管理页面仅提供列表、详情、上传、删除；file 服务仍存在 `PUT /file/{id}` 修改记录接口。页面不提供编辑不等于下游没有编辑接口，不自行删除接口或新增编辑菜单权限。
- file/report 的内部接口不自动继承 admin 的 JWT 和按钮权限。透传请求头不等于完成服务间鉴权，公网访问边界须按实际部署核对。

### 菜单权限配置规则

- 角色管理独立操作：`system:role:dataScope` 控制修改已有角色的数据范围、部门树及“更多 → 数据权限”；`system:role:authUser` 控制“更多 → 分配用户”、分配用户路由、已分配/待分配列表、添加和取消关联。不能再复用角色修改、新增或删除权限。角色修改弹窗只有具备数据权限标识时才可提交数据范围；仅有角色修改权限只能修改基础信息与菜单。新增角色的初始数据范围仍随角色新增权限设置。初始化定义位于 `sql/zero_admin.sql`，不自动给普通角色增加授权；现有数据库不会因修改初始化文件而自动更新。

- 菜单自身的 `perms` 就是页面入口和列表权限。例如文件管理菜单配置了 `systemTools:fileManagement:list`，就表示能进入文件管理页面并查询列表，不要再额外新增“文件列表”这类重复按钮权限。
- `sys_menu.menu_type = 'F'` 的按钮权限只配置独立操作，例如 `add`、`edit`、`remove`、`export`、`import`、`resetPwd`、`refreshCache` 等真实按钮或独立动作；不要把菜单已有的 `list` 权限再配置成按钮。
- 列表页内部的普通详情查看、修改前回显、初始化下拉选项等辅助接口，通常不单独配置 `query` 按钮权限；能进入列表且不展示额外敏感信息即可访问。只有独立详情页或会展示敏感信息时，才单独配置 `模块:资源:query`。
- 前端 `v-hasPermi`、admin Controller 的 `@RequiresPermissions`、`sys_menu.perms` 必须一一对应；SQL 中没有配置的按钮权限，前端不要写对应 `v-hasPermi`，后端也不要加对应 `@RequiresPermissions`。
- 删除某个操作能力时必须检查菜单 SQL、前端按钮/API、admin 入口和下游 Service/DTO 调用。先区分“页面不提供”与“服务彻底移除”：下游接口可能有其它调用方，确认调用范围后再清理，不能因页面隐藏就删共享接口。
- 典型示例：文件管理菜单保留 `systemTools:fileManagement:list` 作为菜单权限；按钮权限只保留上传 `systemTools:fileManagement:add` 和删除 `systemTools:fileManagement:remove`；不再配置文件查询 `query`、文件修改 `edit`、文件列表按钮 `list`。

## zero-report 服务端开发规则

- `zero-report` 当前定位是业务库/report 服务，不是管理后台权限中心。管理后台按钮权限、菜单权限、操作日志放在 `zero-admin` 代理层；report Controller 专注真实业务接口。
- `zero-report` 的 Redis 客户端为 Redisson，连接信息仍从 `zero-report-redis.yml` 的 `spring.data.redis` 读取。Service 子类可通过 `getRedissonUtil()` 使用 Redisson 缓存工具，`RedisTemplate` 兼容入口也复用同一客户端。原生工具使用 JSON 编解码，新增缓存键按业务独立命名，避免与已有键的序列化格式混用。
- `DictConfigUtil` 使用 Redisson `RBucket` 缓存单项和分组查询。单项查无结果返回 `null`，列表查无结果返回空集合。缓存未命中时查询 `zeroBiz`，有结果时写入无有效期缓存，空结果不缓存。Redis 不可用时直接查库。
- 业务字典由 `zero-report` 写入。写入成功后只清理受影响的单项及其分组列表，改编码或移动分组时包含新旧缓存键。分组变更只清理该组列表及其下的单项缓存。有事务时在提交后执行，回滚时保留原缓存。Redis 清理失败时记录告警，受影响的旧缓存需要清理。
- 业务字典键使用原始编码，格式为 `zero:dict:config:item:<分组>:<字典项>` 和 `zero:dict:config:group:<分组>`。分组编码含冒号时，单项键使用 `zero:dict:config:item-colon:<分组长度>:<分组>:<字典项>`，避免键冲突。同环境的 `zero-report` 和 `zero-task` 连接同一 Redis 库，发布时同步升级。
- report 接口可以读取 admin 透传的上下文，但这些上下文只应作为内部链路信息使用。直接外部调用 report 时，不应仅凭 `adminId`、`isAdmin` 等自定义请求头做授权判断。
- 启动入口 `ZeroReportApplication` 已统一设置时区 `Asia/Shanghai`、应用 `PackageProperties`、启用服务发现、Feign、Mapper 扫描。新增包路径和 Mapper 前先确认 `PackageProperties`。
- report 的动态数据源以 `zero-report-mysql.yml` 为准，当前 primary 是 `zeroBiz`；`ReportDataSourceConstants.REPORT` 对应 `zeroBiz`，`ReportDataSourceConstants.ADMIN` 对应 `zeroAdmin`。业务表默认走 `zeroBiz`，只有明确需要读取后台基础数据时才切到 `zeroAdmin`。
- 简单 CRUD 和常规分页优先使用 MyBatis-Plus：实体使用 `@TableName`、主键用 `@TableId`、字段必要时用 `@TableField`；Mapper 继承 MyBatis-Plus `BaseMapper`；Service 实现优先继承项目 `ReportBaseService<M, T>`。
- 复杂报表 XML SQL 才使用 PageHelper。不要在同一个查询里同时用 MyBatis-Plus `Page` 和 PageHelper 分页，避免分页叠加。
- 分页查询 DTO 继承 `BaseQuery`，复用 `pageNum`、`pageSize`、`beginTime`、`endTime`、`toPage()`、日期半开区间方法。特殊默认条数或最大条数按 `DictItemQuery` 的方式重写 `defaultPageSize()`、`maxPageSize()`。
- MyBatis-Plus 全局分页兜底在 `MybatisPlusConfig`，当前最大单页限制是 10000；业务查询还应通过 `BaseQuery` 设定更小的业务上限，例如普通列表默认最大 500、展开明细可单独设 1000。
- 查询条件使用 `LambdaQueryWrapper` / `lambdaQuery()` / `lambdaUpdate()`，字符串条件先 `StringUtils.hasText` 再 trim，避免空字符串进入 SQL。排序要稳定，至少包含业务排序字段和主键兜底。
- 业务可预期失败使用 `IllegalArgumentException` 返回明确提示，例如编码重复、必填为空、记录不存在、父级已删除。不要在每个 Service 查询、更新、删除方法里重复 `try/catch`；数据库和未知异常交给 `ReportExceptionHandler`。
- `ReportExceptionHandler` 会根据 Controller 方法返回类型或 `/list` 路径返回 `TableDataInfo.error` 或 `AjaxResult.error`。新增列表接口如果返回结构不是 `TableDataInfo`，要确认异常结构是否仍符合前端预期。
- `ReportBaseService` 只按 `ReportAdminContext` 提供当前已有上下文方法：管理员 ID、账号、昵称、公司名称、角色 ID、是否管理员、客户端 IP、Authorization、来源服务。需要新增字段时，从 admin 侧常量、请求头透传、report 解析到父类获取方法同步维护。
- `ReportBaseService` 子类通过 `getBizDictValue("分组编码.字典项编码")` 取原始字符串，或通过 `getBizDictItem(...)` 取完整字典项。按分组读取用 `getBizDictItems("分组编码")`，结果按排序值和主键升序。查询显式访问 `zeroBiz`，只读取已提交、启用且未删除的字典项。单项无结果返回 `null`，列表无结果返回空集合。
- `ReportAdminContext` 会解码中文请求头、解析角色ID、只接受 `true` 或 `1` 表示管理员。业务上不要把空请求头当成超级管理员，也不要因为本地测试缺请求头就放宽权限。
- report 继续调用其它 Java 服务时使用 OpenFeign，并依赖 `FeignRequestInterceptorConfig` 继续透传操作人上下文；不要手写新的 Feign 拦截器或散落的请求头拼接。
- 写入审计字段时，创建人/更新人优先使用 `getAdminName()`；创建/更新时间使用服务端时间。日志中不要输出 Authorization、密码、验证码、数据库密码、密钥等敏感信息。
- 软删除字段按业务表语义处理。列表是否展示已删除记录要按页面需求明确；详情、修改、恢复等接口要分别判断是否只允许未删除记录。
- 如果页面新增“删除标志”查询项，必须同步检查前端 `queryParams`、API 参数、admin 代理注释、report Query DTO 和 Service Wrapper 是否全链路支持。只在页面显示 `delFlag` 不等于已经支持按 `delFlag` 查询。
- 前端去掉输入框 `maxlength`、`show-word-limit` 或计数器，只表示页面不再展示长度限制；后端实体 `@Size`、数据库字段长度和唯一索引仍是最终约束，不能在规范中误写成字段无限长。
- 使用 Lombok 是当前 report 模块已有做法，实体、DTO、查询对象可优先用 `@Getter`、`@Setter`、`@ToString`，但仍要补齐字段注释和校验注解。

## zero-jobAdmin 调度中心规则

- `zero-jobAdmin` 基于官方 XXL-JOB 3.5.0 源码，入口 `com.zero.ZeroJobAdminApplication`，调度业务包 `com.zero.jobadmin`；保持官方控制台、独立登录及原生响应/调度协议，不套用 admin JWT 或 report 的业务父类。
- 使用当前父工程 Java 17 / Boot 3.5 / Spring 6.2 基线，core 版本 3.5.0，MyBatis starter 使用 Boot 3 适配版本，单数据源 Hikari 连接独立 `zero_job` 库；不因上游Boot 4基线升级其它服务。
- Nacos 注册名 `zero-job-admin`、默认端口 9102；discovery默认DEFAULT_GROUP，配置默认按profile分组。Data ID 为 `zero-job-admin-mysql.yml`、`zero-job-admin-common.yml`，四个application配置文件直接写配置值、凭据暂留空，数据源整段仅在Nacos的zero-job-admin-mysql.yml直接配置，邮件整段和xxl.job调度参数仅在Nacos的zero-job-admin-common.yml直接配置，不使用环境变量占位符；配置修改后重启，不宣称动态生效。
- Nacos发现不替代XXL-JOB执行器注册协议，执行器仍需配置可访问的调度中心HTTP地址，并使用3.5.0及对应AppName/AccessToken。
- `sql/zero_job.sql` 为初始化重建脚本，每次执行均清空并重建 8 张调度表，再插入初始数据。执行需经用户确认。来源、许可证及适配差异见 `zero-jobAdmin/upstream/README.md`。既有Quartz不迁移、不替换。
- 默认验证：`mvn -pl zero-jobAdmin -am package`。官方有外部写操作的测试位于`src/integration-test/java`，仅显式`job-integration` profile加入，不在未经授权时运行。编译或隔离测试通过不代表真实Nacos、数据库及执行器闭环已验证。

## zero-task 执行服务规则

- `zero-task` 使用 XXL-JOB Core 3.5.0，主入口为 `com.zero.ZeroTaskApplication`。任务方法使用 `@XxlJob`，执行器端口默认 9998，仅运行 BEAN 任务。
- Boot Admin 从 `zero-task` 的 HTTP 端口读取只读 Actuator 监控数据，环境和配置属性值脱敏。应用日志沿用其他微服务的 Logback 格式，默认在 `logs/zero-task` 下生成 `sys-info.log`、`sys-error.log` 和供 Boot Admin 读取的 `application.log`。任务执行日志由 XXL-JOB 管理。
- MySQL 是启动必需依赖，动态数据源由框架自动配置，JDBC 参数从 Nacos 的 `zero-task-mysql.yml` 读取。启动时要求配置 `zeroBiz` 和 `zeroAdmin`，并验证 `zeroBiz` 可连接。配置 `spring.data.redis.host` 后装配 Redis，显式设置 `zero.task.redis.enabled=false` 可关闭 Redis。任务和报表的 Redis 模板使用相同的 `spring.data.redis` 连接参数和 `zero.redis` 连接池参数，分别按服务 Data ID 发布。修改连接配置后重启服务。MySQL 默认访问 `zeroBiz`，需要后台库时显式使用 `@DS("zeroAdmin")`。Mapper 在 `com.zero.mapper`，任务 Redis 键使用 `zero-task:<环境>:<业务>:<标识>`。
- `zero-task` 启用 Redis 后使用 Redisson，任务按需注入 `RedissonUtil`，已有 Spring Redis 模板复用 Redisson 连接工厂。`TaskBaseService` 提供 `getBizDictItem`、`getBizDictItems`、`getBizDictValue` 三个字典查询方法。字典查询访问 `zeroBiz`，与同环境 `zero-report` 共用 Redis 库和 `zero:dict:config:` 失效协议。有结果时写入无有效期缓存，空结果不缓存，Redis 不可用时回源数据库。
- 开发期通过 `com.zero.generator.TaskCodeGenerator` 和显式表名生成实体、Mapper、XML、Service 接口及实现，凭据从本地环境变量读取。生成器跳过已有文件，生成的 Service 由 Spring 组件扫描装配。
- `zero-task` 的 `PackageProperties` 统一维护组件、Mapper 和实体别名的包路径。Mapper 由启动类扫描，分页由 `MybatisPlusConfig` 装配，Redis 组件由 Redis 配置单独导入。Mapper XML 统一放在 `zero-task/src/main/resources/mapper` 根目录，文件名以 `Mapper.xml` 结尾。MyBatis 使用 `classpath*:mapper/*Mapper.xml` 只扫描这一层，`TaskCodeGenerator` 也直接输出到该目录。
- 服务以 `zero-task` 注册到 Nacos 的 `DEFAULT_GROUP`。Nacos 连接信息由 `application-dev.yml`、`application-test.yml`、`application-prod.yml` 分别维护，默认地址和命名空间与 `zero-job-admin` 对应环境一致。三个环境均从当前 profile 分组可选导入 `zero-task-common.yml`、`zero-task-mysql.yml` 和 `zero-task-redis.yml`，并监听配置变更。`xxl.job.executor.*` 在服务启动时装配，变更后仍需重启才能应用到执行器。执行器通过 XXL-JOB 原生协议向调度中心注册并接收任务，执行器组 AppName 和 AccessToken 须与对应配置一致。

## common 包归类规则

- admin 的 `com.zero.common` 放 admin 内部复用的基础能力，不放具体业务流程；跨服务公共能力放 `zero-tools`，report/file 不依赖 admin 的 `common`。
- 通用注解放 `common.annotation`；注解处理逻辑通常放 `framework`。
- 全局常量、配置 key、Redis key 前缀、通用错误码放 `common.constant`。
- 基础返回对象、基础实体、分页对象、登录模型、Redis 封装放 `common.core`。
- 跨模块枚举放 `common.enums`；单业务枚举放业务模块。
- 全局异常放 `common.exception`；业务主动中断优先使用 `ServiceException`。
- Servlet Filter、请求包装器、XSS 请求过滤放 `common.filter`。
- 无状态、可复用、不依赖具体业务 Mapper/Service 的工具类放 `common.utils` 或其子包。
- XSS 校验注解和校验器放 `common.xss`。
- 只服务单个业务的实体、DTO、Mapper、Service、Controller 不放 `common`，应放对应业务模块。
- 通用算法工具放 `common.utils`，例如谷歌动态码的 Base32、TOTP、二维码工具应放 `common.utils.google`；绑定、解绑、登录校验等业务流程仍放 `system` / `web.controller.system`。
- 新增工具类不得依赖具体业务 Mapper、Service 或数据库表；如果需要依赖业务上下文，说明它不是纯公共工具，应放业务模块。
- 配置 key、业务码、缓存 key 前缀必须集中定义到常量类，并补充注释说明用途、取值和默认兜底。
- 涉及敏感信息的公共工具必须做好脱敏、输入校验；日志不得输出明文密码、验证码、Token、密钥。

## 后端接口规范

- `zero-admin` Controller 默认继承 `BaseController`；`zero-report` Controller 按 report 当前实现保持轻量 Controller，不继承 admin 的 `BaseController`。
- `zero-admin` 分页列表接口使用 `startPage()` + Service 查询 + `getDataTable(list)`，返回 `TableDataInfo`，字段为 `code`、`msg`、`rows`、`total`。
- `zero-report` 分页列表接口使用 `BaseQuery.toPage()` + MyBatis-Plus `page(...)`，返回 `com.zero.dto.TableDataInfo.success(records, total)`。
- 普通增删改查返回 `AjaxResult`，admin 成功用 `success()` / `success(data)`、失败用 `error(message)`、写操作用 `toAjax(rows)`；report 使用 `com.zero.model.AjaxResult` 的 `success()` / `success(data)` / `error(message)`。
- `AjaxResult` 字段语义必须固定：`code` 表示状态码，`msg` 只表示响应描述或错误提示，`data` 承载业务数据主体；不得把地区、用户信息、配置值、Token、对象、列表等业务数据放入 `msg`。
- 返回业务数据时必须写入 `data` 字段，例如 `success(data)` 或 `AjaxResult.success("操作成功", data)`；即使只返回一个字符串，也必须作为 `data` 返回，禁止使用 `success("越南河内")` 这类把数据误放入 `msg` 的写法。
- 注意 `success(String)` 重载表示响应描述；返回字符串数据用明确的双参数重载，例如 `AjaxResult.success("操作成功", value)`，不要依赖变量名或字符串内容区分语义。
- 历史上传接口有兼容例外：通用上传保留顶层 `url/fileName/newFileName/originalFilename`（多文件对应复数字段），头像保留顶层 `imgUrl`；新业务仍使用 `data`，不能直接移除既有上传字段。
- 前端读取接口结果必须按结构取值：普通业务数据读取 `response.data`，分页列表读取 `response.rows` 和 `response.total`；不得从 `response.msg` 中读取业务数据。
- 新增接口前必须先确认返回类型：查询详情、解析结果、配置值等使用 `AjaxResult.data`；增删改使用 `AjaxResult.msg` 表达操作结果，必要时再用 `data` 返回业务对象；分页列表使用 `TableDataInfo`。
- 工具类查询接口也必须遵守统一返回语义，例如 IP地区解析、验证码状态、配置值查询等，单值结果也放入 `data`，`msg` 只保留“操作成功”或错误原因。
- 如果历史接口错误使用 `msg` 承载业务数据，新代码不得继续沿用；修改这类接口时必须同步调整前端取值逻辑。
- 管理后台权限使用项目自定义 `@RequiresPermissions("模块:资源:动作")`，不要替换成 Shiro 或其它权限注解；admin 代理 report 的权限也放在 admin 代理 Controller 上。
- 列表页内部使用的辅助详情接口，例如点击“修改”前按ID回显表单数据，原则上不单独配置 `模块:资源:query` 权限；能进入列表并具备对应按钮权限即可完成页面流程，避免额外 query 权限导致修改弹窗无法回显。若详情接口用于独立详情页、敏感信息查看或可被单独访问展示，则必须单独配置查询权限。
- 操作日志使用 `@Log(title = "...", businessType = BusinessType.XXX)`，写操作、导入导出、清缓存等应按同类 Controller 补齐；admin 代理 report 时操作日志记录在 admin 代理 Controller。
- 新接口路径和前端 API 文件保持一致：REST 风格常用 `GET /list`、`GET /{id}`、`POST`、`PUT`、`DELETE /{ids}`。
- 参数校验沿用 `@Validated`、实体字段校验、`StringUtils`、业务 Service 校验，不在 Controller 堆复杂业务。
- 登录、验证码、Token、权限、路由相关逻辑不要随意改动；必须先读取 `SecurityConfig`、`JwtAuthenticationTokenFilter`、`SysLoginService`、`TokenService`、`SysLoginController`。

## 后端安全与权限

- `zero-admin` Spring Security 是无状态 JWT 模式，Token 请求头为 `Authorization: Bearer <token>`。
- 在线用户列表按用户明确授权，向具备`monitor:online:list`菜单权限的所有账号返回每条会话的完整`authorizationToken`（含`Bearer `前缀），不遮挡、不另加超级管理员限制。该菜单权限包含读取其它账号会话凭证的能力；列表响应禁止缓存，不记录令牌日志，不写入数据库，不刷新被查看会话有效期。页面完整显示会话编号，并保留底部横向滚动条。
- `zero-admin` 匿名路径在 `SecurityConfig` 和 `@Anonymous` / `PermitAllUrlProperties` 相关逻辑中体现，新增匿名接口必须谨慎确认。
- 管理后台 WebSocket 为精确 `GET /ws/admin` 握手入口，首帧仅发送含token字符串的JSON，JWT认证成功后返回CREATE_CONNECTION才接收业务事件；认证失败或协议错误直接关闭，不发送错误类型；握手、心跳和会话检查不续期登录。频道必须显式配置，缺失时拒绝启动；消息统一经环境隔离的 Redis Pub/Sub 分发，调用 `AdminWebSocketService` 按用户ID发送，不提供公网通用推送接口。前端从现有 API 地址自动生成 ws/wss 地址；代理须保留 Host 并转发 Upgrade。同账号多会话、多标签页接收，单登录会话连接上限由 Redis 租约跨节点控制。在线消息不落库、不补发，部署和验证边界见 `docs/WebSocket集成与部署.md`。
- WebSocket认证、心跳、错误及业务消息统一使用 `common.enums.webSocket.WebSocketMessageType`；消息对象 `WebSocketMessageVo` 包含 `id/sendTime/senderIp/fromAdminId/toAdminId/msgType/msgData/forwardUrl`，不恢复版本号。枚举以对象输出事件编号、中文说明、展示形式displayType及级别messageLevel；前端使用 `websocketMessageType.js` 对应编号订阅。业务发送入口只允许业务事件，禁止伪造控制消息；仅保留统一枚举及消息对象发送接口；无openSendMsg开关，前端展示器支持卡片、顶部提示及独立居中弹窗，退出清理自身提示和队列。
- 用户部门或角色关系变更成功后通过 `system.service.AdminUserRefreshService` 在事务提交后定向发送REFRESH_BROWSER；全局前端整页刷新，/getInfo读取最新部门和角色并按需更新当前登录快照。角色定义及菜单授权修改不在此通知范围；离线不补发，推送失败不回滚业务。
- API谷歌验证修改保存成功后，在事务提交后通过 `AdminUserRefreshService.afterApiAuthChanged()` 广播REFRESH_BROWSER至所有在线连接，页面刷新后重新读取 `/system/apiAuth/googleAuthApis` 更新本地规则；查询接口不触发推送，原校验、权限和审计日志保留。
- `zero-admin` 登录验证码由 `/captchaImage`、`SysLoginService.validateCaptcha`、`ISysConfigService.selectCaptchaEnabled()` 和配置项 `sys.account.captchaEnabled` 控制。
- `zero-admin` 当前 XSS 配置在 `application.yml` 的 `xss` 节，匹配 `/system/*,/monitor/*,/tool/*`，不要认为所有 JSON 请求都天然安全。
- 数据权限通过 `@DataScope` 和 SQL 中 `${params.dataScope}` 拼接实现。涉及用户、角色、部门数据时必须先确认数据范围规则。
- `DataScopeAspect` 对“本部门”“本部门及以下”范围按当前认证用户ID读取最新归属部门，并仅在当前请求内复用；不能回退登录缓存里的旧部门。辅助读取前挂起、读取成功后恢复原PageHelper实例，异常时清理分页上下文。全部、自定义、本人范围及超级管理员沿用既有规则；该机制不刷新角色、菜单权限或跨请求登录缓存。
- 日志、异常、导出、登录日志中不得输出密码、验证码、Token、数据库密码、密钥等敏感信息。

## 后端数据库与 MyBatis 规则

- 实体字段、表字段、Mapper XML 必须同步核对，优先参考同模块 XML 写法。
- SQL 参数默认使用 `#{}`，只有数据范围、排序等项目既有受控片段才允许 `${}`，新增 `${}` 必须说明安全来源。
- 软删除字段按现有表语义处理，例如 `sys_user.del_flag`。
- 新增列表查询要考虑分页、排序、时间范围、数据权限和索引影响。admin XML 分页按若依风格使用 `params.beginTime/endTime`；report 的 MyBatis-Plus 查询优先使用 `BaseQuery.beginTime/endTime` 和半开区间方法，不要把两套参数模型混用。
- 写入用户、角色、菜单、参数等系统表时，维护 `createBy/createTime/updateBy/updateTime` 等审计字段。
- 修改 `sys_config` 后注意缓存：Service 中会更新 Redis；必要时调用或提醒刷新参数缓存。

## 后端事务与业务规则

- 跨多表写入必须加 `@Transactional`，参考用户-角色、用户-岗位、角色-菜单等 Service 实现。
- Service 层负责业务规则、唯一性校验、数据权限校验和事务边界；Controller 只做参数接收、权限注解、调用 Service 和返回封装。
- 删除、重置密码、分配角色、清缓存、导入导出属于敏感操作，修改前必须看同类实现和前端调用。
- 定时任务使用 Quartz 模块既有结构，不要套用外部 `@Scheduled` 规范。

## 前端结构规则

- 前端已按授权升级为 Vue3 + Vite，继续使用 Options API、Vuex4 和 Element Plus；不擅自引入 Pinia、TypeScript 或批量改为 Composition API。
- API 请求统一放在 `src/api/**`，通过 `@/utils/request` 发起。
- 页面放在 `src/views/**`，布局在 `src/layout/**`，公共组件在 `src/components/**`。
- 路由分两类：`router/index.js` 中的静态/动态路由，以及后端 `getRouters` 返回的菜单路由。后端菜单的 `component` 会按 `@/views/${view}` 动态加载，例如 report 页面组件应写成 `reportSystem/bizDict/index`。
- 登录后路由守卫在 `permission.js` 中先调用 `GetInfo`，再调用 `getGoogleAuthApis()` 刷新接口谷歌验证缓存，最后 `GenerateRoutes` 生成菜单路由。不要在业务页面里重复做这一套全局初始化。
- `main.js` 已全局注册 `Pagination`、`RightToolbar`、`DictTag`、上传、富文本、图片预览等组件，并挂载 `parseTime`、`resetForm`、`addDateRange`、`download` 等方法；页面优先复用这些能力。
- 权限按钮使用 `v-hasPermi` / `v-hasRole`，不要手写分散权限判断。
- 字典使用 `dicts: [...]`、`dict-tag` 和项目字典组件，不重复硬编码字典标签。

## 前端请求与页面规范

- 请求成功判断以 `request.js` 拦截器为准：普通 JSON 接口应明确返回 `code: 200`；现有拦截器用 `res.data.code || 200` 兼容缺失或假值状态码，不能据此设计新接口。`401` 触发重新登录，`500` 弹错误消息，接口谷歌验证错误码按全局逻辑处理；文件流走独立分支。
- `request.js` 会自动追加 `Authorization: Bearer <token>`，GET 参数会序列化到 URL，POST/PUT 默认做 5MB 内的一秒防重复提交。业务 API 文件不要重复实现这些逻辑。
- 接口谷歌验证由 `request.js` 根据 localStorage 中的 `google_auth_apis` 判断，支持精确路径、`{param}`、`*`、`**` 和请求方法匹配；命中后统一弹 6 位验证码输入框并添加 `X-Google-Code` 请求头。
- Element Plus `el-upload` 的 `action` 直传不经过 `request.js`。检查实际上传调用方式，显式维护登录请求头、响应状态和错误处理；配置接口谷歌验证时不能假定直传会自动弹验证码。
- 列表页沿用若依页面结构：`queryParams`、`dateRange`、`loading`、`total`、`getList()`、`handleQuery()`、`resetQuery()`、`pagination`。
- 查询接口用 `GET + params`，新增用 `POST + data`，修改用 `PUT + data`，删除用 `DELETE /ids`。
- 导出使用 `this.download(...)` 或现有下载封装，不自行拼接 blob 逻辑。
- 表单弹窗使用 `el-dialog` + `el-form` + `rules` + `this.$refs["form"].validate`，提交后刷新列表并关闭弹窗。
- UI 改动要遵循 Element Plus 和当前后台风格，避免引入新的 UI 库或重写整体布局。
- 该项目已有主题、菜单、Tab 等大量样式改动，修改样式前必须先读相关 SCSS、组件和现有说明文档，避免破坏多主题适配。

## 管理后台 UI 开发沉淀规则

- 先按业务归属确定前端目录：admin 自身业务继续放 `views/system`、`views/monitor`、`views/tool` 等既有目录；admin 调 report 的业务统一放 `zero-ui/src/views/reportSystem`，API 文件统一放 `zero-ui/src/api/reportSystem`。
- 页面结构优先沿用若依列表页：顶部查询表单、操作按钮行、`right-toolbar`、`el-table`、`pagination`、新增/修改 `el-dialog`。不要为了单个页面重做一套布局。
- API 文件只封装请求，不写业务判断；命名按动作拆清楚，例如 `listDictGroup`、`listDictGroupOptions`、`getDictGroup`、`addDictGroup`、`updateDictGroup`、`delDictGroup`、`restoreDictGroup`。
- admin 自身页面 API 走 `/system/**`、`/monitor/**` 等现有路径；admin 代理 report 的页面 API 走 `/reportSystem/**`，前端不要直接请求 `zero-report` 端口或服务名。
- 查询条件里需要下拉选项时，先判断数据来源是否独立于分页列表。如果是字典组编码这类全量选项，应新增独立 options 接口，进入页面 `created()` 时加载，用 Element Plus 的 `filterable` 做本地过滤；不要临时用当前页表格数据拼下拉，也不要无必要地做远程搜索。
- options 接口如果用于查询区全量选项，应明确是否过滤状态和删除标志。本项目业务字典组编码下拉按当前实现是不区分状态、删除标志全量返回，再由前端 `filterable` 本地过滤。
- 分页列表读取 `response.rows` 和 `response.total`；普通接口读取 `response.data`。不要从 `response.msg` 读取业务数据。
- 按钮权限统一用 `v-hasPermi`，权限标识必须和 `sys_menu.perms`、admin Controller 的 `@RequiresPermissions` 一致。删除/恢复共用权限时，前端两个状态按钮也必须使用同一个权限。
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

## 前端多主题适配规则

- 当前管理后台可选主题风格包括 7 个增强主题和 1 个原始风格：`luxury-business` 轻奢商务风、`glassmorphism` 毛玻璃拟态风、`vibrant-tech` 活力科技风、`minimalist-future` 极简未来风、`warm-nature` 温馨自然风、`material-design` 质感设计风、`dark-premium` 高端暗黑风、`default` 原始风格。
- 主题可选项以 `zero-ui/src/layout/components/Settings/index.vue` 下拉为准。当前 `index.scss` 导入 7 个增强主题文件，`App.vue` 的清理数组还包含历史/实验类 `theme-nordic-minimal`；不要仅因为文件存在就把未导入、未配置的主题当作正式可选主题。
- 如果新增、删除或重命名主题，必须同步检查 `Settings/index.vue` 下拉、`settings.js` 默认值、Vuex `settings` 持久化、`App.vue` 清理数组、`layout/index.vue` 主题类、`index.scss` 导入、主题 SCSS 文件、`table-fixed-column.scss` 变量和弹层全局样式，不能只改一个下拉选项。
- 主题状态保存在 Vuex `settings` 模块，并从 localStorage 的 `layout-setting` 恢复；主题切换由 `App.vue` 监听 `themeStyle`、`glassmorphismBgStyle` 后更新 `body` 类名和毛玻璃背景变量。
- 主题类同时存在于 `body` 和 `layout/index.vue` 的 `.app-wrapper` 上。写全局样式时优先用 `body.theme-*`；写页面局部样式时要考虑 `.theme-* .page-class` 也可能命中。
- `AppMain.vue` 的路由 key 包含 `themeStyle`，用于主题切换时重建 keep-alive 页面；遇到“切换主题后不正常、刷新后正常”的问题，优先检查旧主题类、运行时 style、Element 表格 hover 状态和 keep-alive 缓存。
- 修改页面样式、组件样式、表格展开行、弹窗、表单、按钮、分页、固定列、下拉层、菜单或 Tab 时，不得只按当前正在使用的主题修一份颜色；必须考虑上述全部主题在浅色、深色、毛玻璃、高对比和原始风格下的显示效果。
- 写样式前先确认主题入口和作用域：`zero-ui/src/settings.js`、`zero-ui/src/store/modules/settings.js`、`zero-ui/src/App.vue`、`zero-ui/src/layout/components/Settings/index.vue`、`zero-ui/src/layout/index.vue`、`zero-ui/src/assets/styles/index.scss`、`zero-ui/src/assets/styles/element-ui.scss`、`zero-ui/src/assets/styles/pagination-override.scss`、`zero-ui/src/assets/styles/glassmorphism-readable.scss`、`zero-ui/src/assets/styles/table-fixed-column.scss` 以及 `zero-ui/src/assets/styles/themes/theme-*.scss`。
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

- 保持当前文件风格，精准局部改动，不做无关重构。
- Java 代码使用现有工具类：`StringUtils`、`SecurityUtils`、`DateUtils`、`ExcelUtil`、`RedisCache` 等。
- 新增或修改 Java 代码时，必须同步维护注释；注释要贴合项目现有风格，说明业务职责、参数含义、返回语义、边界和安全原因，避免空话、过期注释和与代码不一致的注释。
- 前端保持 Vue3 Options API 写法、单引号/双引号风格按当前文件就近一致，不批量格式化。
- 中文文件按 UTF-8 读取和写入；如果发现乱码，必须重新确认编码后再编辑。
- 不新增全局依赖、不升级框架、不改构建配置，除非需求必须且已说明影响范围。
- 行内注释按语言聚焦关键逻辑。Vue 和 JavaScript 注释用于说明兼容行为、异步请求顺序、过期响应处理或主题覆盖原因。MyBatis XML 和 SQL 注释用于说明数据归属、查询边界、参数来源或事务要求。改动这些约束时同步更新相关注释。

## Java 注释规范

适用于仓库各 Java 模块的源码和测试代码。新增功能、修改逻辑、补充字段或调整接口时，必须同步补充和维护相关注释。

### 基本要求

- 类、interface、enum 必须有类头 JavaDoc，说明职责、业务范围、对应表或接口用途；类名不能完整表达业务时，必须在说明中补足上下文。
- Controller 类注释需说明接口职责；每个对外映射方法必须有方法 JavaDoc，写清用途、关键参数和返回结果。
- Service、Mapper、工具类的公开方法必须有方法 JavaDoc；重要的 `private` 辅助方法也要注释，说明输入来源、输出格式、异常兜底或副作用。
- 实体、DTO、请求参数对象的字段上方使用 `/** ... */`，按字段需要说明业务含义、单位、枚举取值、默认值或脱敏规则；不为简单 getter/setter 机械重复注释，除非当前文件已有统一模板。
- 静态常量、配置 key、Redis key、URL、路径、权限标识、业务状态码、开关字段必须在字段上方说明含义、取值范围、存储格式或业务用途。
- 涉及登录、权限、数据权限、JWT、Redis TTL、验证码、谷歌动态码、密钥、操作日志、XSS、事务、定时任务、文件上传、导入导出等特殊逻辑时，必须在就近位置注释说明为什么这样处理。
- 修改既有代码时，如果行为发生变化，必须同步更新原注释，避免注释仍描述旧逻辑。

### 注释内容写法

- 注释写业务含义，不写代码表面动作。例如写“生成令牌前校验谷歌动态码，避免未通过二次验证的用户拿到 token”，不要写“调用 validateGoogleCode 方法”。
- 方法 `@param` 先写业务含义，再按实际约束补充是否可选、默认值、上限、单位或枚举值，例如 `pageSize 每页条数，默认10`、`googleCode 谷歌动态码，6位数字`。
- 有返回值的方法用 `@return` 和模板中的返回值说明写清业务结果、结构及适用的空值或失败语义。`void` 方法沿用项目模板，重点写清副作用。模板中的空 `@return` 仅兼容 `void` 写法。
- 接口方法的注释写清调用所需登录和权限、返回 DTO，并按接口职责补充数据权限、分页口径和敏感字段处理。
- 对 MyBatis XML 对应的 Mapper 方法，注释需说明查询条件、返回对象及适用的数据权限、时间边界或软删除口径；新增 `${}` 拼接时必须在代码或 XML 附近说明安全来源。
- 行内注释只写业务原因、边界保护、兼容老数据、安全控制或性能考虑，不写“给变量赋值”“循环列表”这类显而易见的内容。
- 注释中允许保留表名、字段名、DTO、Redis key、URL、API、SQL、token、data 等技术词；中文说明要让业务读者能理解。
- 不保留空的 `说明:`、错误的返回值类型、与实现不一致的默认值、过期作者时间、重复模板或无意义生成注释。

### 作者、时间与风格

- 维护旧文件时沿用同类文件的排版。新增或本次修改的类和方法按本节模板补齐说明。
- 历史源码中的简洁 JavaDoc 和未注释方法随相关业务改动维护，开展 Java 注释专项整理时按本节统一检查。
- 新增、补齐或重写类和方法 JavaDoc 时，`作者:` 统一填写 `Arthur 亚瑟`。清理类头和方法注释中模板外的历史署名、上游署名、版权声明和旧版本签名。
- JavaDoc 的 `时间:` 记录该段注释编写或重写时的 `Asia/Shanghai` 时间，格式为 `yyyy/M/d HH:mm`。重写时更新该时间，示例日期仅展示格式。
- 中文注释使用简体中文；读取中文文件出现乱码时，必须先用正确编码重新读取再修改。
- 本项目 Java 类和方法默认使用现有模板注释，不允许只写一句简洁 JavaDoc。新增或修改类头必须包含空行、`作者:`、`时间:`、`说明:`；新增或修改方法必须包含空行、`作者:`、`时间:`、`说明:`，有参数必须写 `@param`，有返回值必须写 `@return`、返回值类型和返回值说明。
- 方法注释模板必须贴近当前仓库既有格式，示例：

```java
/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/16 07:16
 * 说明: 应用启动完成后加载XML配置
 * @param event 应用启动完成事件
 * @return
 * 返回值: void
 * 返回值说明: 加载resources/configuration.xml到XmlConfig缓存
 */
```
- 写注释前必须先看同目录或同类型文件的注释模板；如果模板和通用 JavaDoc 规范冲突，以本项目模板为准。

### 字段注释示例

类和方法按上面的作者、时间、说明模板维护；字段和常量使用业务含义注释，不套用方法模板。

```java
/**
 * 谷歌动态码登录开关配置 key，true-开启，false或未配置-关闭
 */
public static final String OPEN_GOOGLE_AUTH = "sys.account.openGoogleAuth";
```

## YAML 配置注释规范

- 新增或修改主配置、各 profile 配置和 Nacos 配置源时，用简体中文说明每个相关配置项的用途。注释放在对应配置项上方，并对齐 YAML 缩进。参考 [zero-task 主配置](zero-task/src/main/resources/application.yml)、[环境导入示例](zero-task/src/main/resources/application-dev.yml)和 [Nacos 配置源](zero-task/src/main/resources/nacosConfig)。
- 写清配置控制什么、由谁读取及何时生效。数值注明单位和特殊取值，开关注明启停范围、默认行为，列表和路径注明格式或匹配范围。修改默认值、单位或实际读取方时，同步更新注释。
- 外部导入说明 Data ID、分组、可选行为和涉及的配置覆盖关系。能否热刷新以实际绑定和组件装配方式为准，启动时读取的配置在模块说明中注明重启要求。凭据示例使用空值或安全占位。

## 规则维护与源码依据

- 本文件记录开发约束和源码事实。修改模块、服务名、配置、接口或主题后，重新核对对应源码并维护相关条目。验证结论以当前源码、配置或运行证据为准。
- 构建与模块依据：根 `pom.xml`、各模块 `pom.xml`、`Zero*Application` 和各服务 `application*.yml`。
- admin 自身业务依据：`SysConfigController`、`SysMenuController`、`SysApiAuthController` 及其 Service、Mapper XML、前端页面/API。
- report 代理依据：`BizDictController`、`ReportRestService`、`ReportRestTemplateConfig`、`AdminForwardHeaderProvider`、`ReportAdminContext`、字典 Controller/Service、`BaseQuery` 和前端 `reportSystem/bizDict`。
- 文件服务依据：`FileManagementController`、`FileRestService`、`AdminFileUploadServiceImpl`、`AdminFileStorageModeServiceImpl`、`FileController`、`FileDetailServiceImpl`、`FileStorageService`、`FileStorageAutoConfiguration`、`FileExceptionHandler`、`sql/zero_biz.sql` 和前端 `systemTools/fileManagement`。
- 前端请求和主题依据：`request.js`、`permission.js`、`App.vue`、`AppMain.vue`、`Settings/index.vue`、`settings` 状态及主题 SCSS。
- 修改规则时区分既有行为、已知差异和新增开发要求。未运行的服务或外部依赖明确标为未验证，不把静态阅读写成联调完成。

## 修改前检查清单

1. 先读目标文件、同类 Controller/Service/Mapper 或 Vue 页面/API 文件。
2. 确认接口返回类型是 `AjaxResult`、`TableDataInfo` 还是文件流。
3. 确认权限标识、菜单路由、前端 `v-hasPermi` 是否匹配。
4. 确认是否涉及登录态、JWT、Redis 缓存、数据权限、事务、导入导出或上传。
5. 确认是否有用户未提交改动，不能覆盖。
6. 同步核对注释与行为、配置默认值及生效方式。能验证时运行最小范围编译或前端构建，未验证的范围说明原因。

## 常用验证命令

- admin 编译：`mvn -pl zero-admin -am compile -DskipTests`
- report 编译：`mvn -pl zero-report -am compile -DskipTests`
- file 编译：`mvn -pl zero-file -am compile -DskipTests`
- 监控模块编译：`mvn -pl zero-bootAdmin -am compile -DskipTests`
- admin 运行：`mvn -pl zero-admin spring-boot:run`
- report 运行：`mvn -pl zero-report spring-boot:run`
- file 运行：`mvn -pl zero-file spring-boot:run`
- 前端开发：在 `zero-ui` 执行 `npm run dev`
- 前端生产构建：在 `zero-ui` 执行 `npm run build:prod`

- Maven 命令从仓库根目录执行；先用 `java -version`、`mvn -version` 核对 Maven 实际 JDK。运行单模块前需确保依赖模块已构建且可解析，必要时先 `mvn -pl <模块> -am install -DskipTests`。
- 仅修改文档或忽略规则时验证路径、事实和 `git diff --check`，不要求启动服务或构建前后端。修改源码时按影响范围编译、构建或运行已有相关测试；`-DskipTests` 只证明编译，不证明测试、Nacos、数据库、存储和跨服务调用成功。
- 验证结果按实际范围记录。模拟 API 用例说明覆盖的页面行为，隔离数据库或 Redis 用例说明覆盖的数据与并发边界。真实环境的注册发现、接口鉴权、配置覆盖、调度回传或文件存储，以实际部署环境的观测为准。

## 当前环境提示

- 项目后端需要 Java 17；如果本机 Maven 使用 JDK 8，会出现 `无效的标记: --release` 等编译失败。
- 配置、测试工具和部署示例中的数据库凭据、Token 与应用密钥均按敏感信息处理。提交前核对变更，文档和测试记录使用占位值。
