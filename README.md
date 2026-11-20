<p align="center">
	<img alt="logo" src="https://oscimg.oschina.net/oscnet/up-d3d0a9303e11d522a06cd263f3079027715.png">
</p>
<h1 align="center" style="margin: 30px 0 30px; font-weight: bold;">Zero v3.9.0</h1>
<h4 align="center">基于SpringBoot+Vue前后端分离的Java快速开发框架</h4>
<p align="center">
	<a href="https://gitee.com/y_project/Zero-Vue/stargazers"><img src="https://gitee.com/y_project/Zero-Vue/badge/star.svg?theme=dark"></a>
	<a href="https://gitee.com/y_project/Zero-Vue"><img src="https://img.shields.io/badge/Zero-v3.9.0-brightgreen.svg"></a>
	<a href="https://gitee.com/y_project/Zero-Vue/blob/master/LICENSE"><img src="https://img.shields.io/github/license/mashape/apistatus.svg"></a>
</p>

## 平台简介

零号是一套全部开源的快速开发平台，毫无保留给个人及企业免费使用。

* 当前 `zero-ui` 采用 Vue 3、Vite 和 Element Plus，保留 Options API 和 Vuex 4。开发、构建与测试见[前端说明](zero-ui/README.md)，迁移过程见 [Vue 3 升级方案](前段项目升级方案/01-Vue3升级实施方案.md)。
* 后端采用Spring Boot、Spring Security、Redis & Jwt。
* 权限认证使用Jwt，支持多终端认证系统。
* 支持加载动态权限菜单，多方式轻松权限控制。
* 高效率开发，使用代码生成器可以一键生成前后端代码。
* 提供了技术栈（[Vue3](https://v3.cn.vuejs.org) [Element Plus](https://element-plus.org/zh-CN) [Vite](https://cn.vitejs.dev)）版本[Zero-Vue3](https://gitcode.com/yangzongzhuan/Zero-Vue3)，保持同步更新。
* 提供了单应用版本[Zero-Vue-fast](https://gitcode.com/yangzongzhuan/Zero-Vue-fast)，Oracle版本[Zero-Vue-Oracle](https://gitcode.com/yangzongzhuan/Zero-Vue-Oracle)，保持同步更新。
* 不分离版本，请移步[Zero](https://gitee.com/y_project/Zero)，微服务版本，请移步[Zero-Cloud](https://gitee.com/y_project/Zero-Cloud)
* 阿里云折扣场：[点我进入](http://aly.zero.vip)，腾讯云秒杀场：[点我进入](http://txy.zero.vip)&nbsp;&nbsp;

## 项目整体架构

| 模块目录 | 职责 |
| --- | --- |
| `zero-tools` | Java 基础工具包，无独立进程。`zero-report`、`zero-file` 和 `zero-task` 复用其常量、返回对象和通用工具，`zero-admin` 仅复用 SQL 日志格式化工具并排除传递依赖。 |
| `zero-admin` | 管理后台后端和统一 API 入口，负责登录、权限、操作审计和后台自身业务。 |
| `zero-report` | 报表和业务数据服务，供 `zero-admin` 调用，负责所属业务数据的查询、管理和持久化。 |
| `zero-jobAdmin` | 服务名为 `zero-job-admin`，负责 XXL-JOB 任务管理、执行器注册、任务调度和结果收集。 |
| `zero-task` | XXL-JOB 执行服务，向调度中心注册，执行 `@XxlJob` 任务并回传结果。 |
| `zero-bootAdmin` | 默认服务名为 `zero-boot-admin`，通过服务发现和 Actuator 独立监控服务运行状态。 |
| `zero-file` | 独立文件存储服务，负责本地磁盘和已适配的云存储平台，并维护 `biz_file_detail` 文件记录。 |
| `zero-ui` | Vue 管理后台前端，负责页面交互和数据展示。 |

- **后台业务**由 `zero-ui` 统一请求 `zero-admin`。后台自身业务由 admin 处理，报表和业务数据通过 `ReportRestService` 调用 `zero-report`，文件管理通过 `FileRestService` 调用 `zero-file`。代理入口负责管理后台权限和操作审计，业务规则和持久化留在数据所属模块。
- **报表探活**由 `zero-admin` 通过 OpenFeign 客户端 `RemoteReportHealthService` 验证到 `zero-report` 的服务调用。
- **任务调度**由 `zero-task` 向 `zero-job-admin` 注册，调度中心派发任务，执行器运行任务并回传结果。XXL-JOB 注册和派发与 Nacos 服务发现分别配置。`zero-task` 主配置的 HTTP 端口为 9104，执行器配置源的默认端口为 9998。
- **运行监控**由 `zero-boot-admin` 发现服务实例并访问各服务开放的 Actuator 端点，独立于核心业务调用链路。部署时核对服务名、Nacos 命名空间、管理端点的可达性和访问权限。

## 技术栈

| 范围 | 主要技术 |
| --- | --- |
| 后端基线 | Java 17、Maven 多模块、Spring Boot 3.5.16、Spring Framework 6.2.19、Spring Cloud 2025.0.2、Spring Cloud Alibaba 2025.0.0.0。Spring Boot 核心、自动配置和打包插件统一沿用官方父 POM 的版本。 |
| 服务发现与调用 | Nacos 注册和配置、Spring Cloud LoadBalancer、OpenFeign。后台对报表和文件服务的代理分别使用带 `@LoadBalanced` 的 `reportRestTemplate` 和 `fileRestTemplate`，统一追加管理员上下文请求头。 |
| 后台管理 | Spring Security、JWT、MyBatis XML、PageHelper、Druid、Redis、Quartz、SpringDoc 2.8.17。 |
| 报表、文件与任务数据访问 | MySQL、MyBatis-Plus 3.5.16、dynamic-datasource 4.6.0。报表的复杂 XML 查询保留 PageHelper，报表与任务服务通过 Redisson 4.6.1 接入 Redis。 |
| 调度与监控 | XXL-JOB 3.5.0 负责调度和任务执行，Spring Boot Admin 3.5.8 与 Actuator 负责运行监控。 |
| 管理后台前端 | Node 开发基线为 24.21.0，引擎范围为 `>=24.21.0 <25`。使用 Vue 3.5.43、Vite 8.3.3、Vue Router 4.6.4、Vuex 4.1.0 和 Element Plus 2.14.7，沿用 Options API 和 Axios 请求封装。 |

后端版本以 [父 POM](pom.xml) 和各模块 POM 的声明为准，排查依赖差异时通过有效 POM 或依赖树确认实际解析版本。前端版本见 [package.json](zero-ui/package.json)。Vite 开发代理默认指向 `http://localhost:9099`，可通过 `VITE_DEV_PROXY_TARGET` 覆盖。

## 新建 Java 项目规范

- 先确定模块职责、数据归属和调用方，再参考 [zero-task 模块说明](zero-task/README.md) 和 [Maven 配置](zero-task/pom.xml)。仓库内新模块沿用父工程版本管理并加入 Maven 聚合，核对组件及 Mapper 扫描范围。按业务需要接入 Nacos、Actuator、MySQL 和 Redis，仅在承担任务执行职责时接入 XXL-JOB。
- 日志参考 [zero-task 的 Logback 配置](zero-task/src/main/resources/logback-spring.xml)。控制台和应用文件沿用现有服务的日志格式，保留毫秒时间、线程、级别、Logger、方法、行号和消息。每个服务使用独立日志目录，沿用 `sys-info.log`、`sys-error.log` 的分流和滚动方式。接入 Boot Admin 日志查看时，确认 Logback 写入的 `application.log` 与 Actuator logfile 读取的是同一文件。
- 所有新建 `.yml` 和 `.yaml` 配置项都要就近添加中文注释。参考 zero-task 的 [主配置](zero-task/src/main/resources/application.yml)、[开发环境](zero-task/src/main/resources/application-dev.yml)、[测试环境](zero-task/src/main/resources/application-test.yml)、[生产环境](zero-task/src/main/resources/application-prod.yml)及 [Nacos 配置源](zero-task/src/main/resources/nacosConfig)。注明配置用途、数值单位、开关范围、默认行为和生效时机，涉及外部配置时说明来源和覆盖关系。
- 每个新 Java 项目都要集成 [MyBatis-Plus 代码生成器](zero-task/src/test/java/com/zero/generator/TaskCodeGenerator.java)。MyBatis-Plus Generator 和 Velocity 使用测试作用域，从本地环境变量读取数据库连接信息，显式指定表名后生成实体、Mapper、Mapper XML、Service 接口和实现。生成器由开发人员显式运行，默认跳过已有文件。新模块要核对包名、输出路径和 Mapper XML 扫描配置。沿用 `zero-task` 的单层目录时，将 XML 写入 `src/main/resources/mapper`，文件名以 `Mapper.xml` 结尾。持久层按开关装配时，生成的 Service 模板也应遵循同一开关。在模块 README 中写清生成命令、输入和输出目录，用隔离测试验证生成产物及重复执行时跳过已有文件，并验证模块可构建。

Java 类和方法沿用作者、时间和说明的 JavaDoc 模板，写清职责、参数和返回语义。YAML 在配置项上方用中文说明用途、默认行为和生效时机，凭据示例使用空值或安全占位。Vue、MyBatis XML 和 SQL 注释聚焦兼容行为、异步顺序、数据边界等关键逻辑。详见 [Java 注释规范](AGENTS.md#java-注释规范)、[YAML 配置注释规范](AGENTS.md#yaml-配置注释规范)和[编码风格](AGENTS.md#编码风格)。

详细开发约束见 [AGENTS.md](AGENTS.md)，模块落位和代理权限见 [管理后台业务开发模式](AGENTS.md#管理后台业务开发模式规则)。

## 后端配置入口

每个服务从自身 `src/main/resources/application.yml` 读取基础配置。通过 `--spring.profiles.active=dev`、`test` 或 `prod` 选择对应的 `application-{profile}.yml`，环境文件负责 Nacos 连接和配置导入。下面的目录以仓库根目录为基准。

| Maven 模块 | 配置文件或模板 | 主要内容 |
| --- | --- | --- |
| `zero-admin` | [nacosConfig](zero-admin/src/main/resources/nacosConfig) | 管理库、Redis、后台通用配置和 WebSocket |
| `zero-bootAdmin` | [application.yml](zero-bootAdmin/src/main/resources/application.yml) | Spring Boot Admin 监控、服务发现和界面设置 |
| `zero-report` | [nacosConfig](zero-report/src/main/resources/nacosConfig) | 业务数据源、Redis 和报表服务通用配置 |
| `zero-file` | [nacosConfig](zero-file/src/main/resources/nacosConfig) | 文件元数据的数据源和本地或对象存储平台 |
| `zero-jobAdmin` | [nacosConfig](zero-jobAdmin/src/main/resources/nacosConfig) | 调度数据库、SMTP 邮件和 XXL-JOB 调度参数 |
| `zero-task` | [nacosConfig](zero-task/src/main/resources/nacosConfig) | XXL-JOB 执行器、任务数据源和 Redis |

- 发布配置前，按环境文件的 `spring.config.import` 核对 Data ID、配置 Group 和 Namespace ID。配置分组用于加载配置，服务注册分组由 `spring.cloud.nacos.discovery` 单独决定。
- `nacosConfig` 保存模板，应用读取的是配置中心已发布的内容。带 `optional:` 的导入允许对应资源缺失，依赖其中配置的功能仍需要有效值。
- `zero-admin/src/main/resources/localYml` 保存本地配置模板，当前主配置中的导入已注释。Boot Admin 使用自己的 `application*.yml`，通过服务发现获取监控实例。
- 修改已发布配置后，按服务的刷新开关和组件初始化方式判断生效时机。任务执行器或调度中心启动时读取的参数按各模块的启动说明处理。连接凭据通过部署环境或受控的 Nacos 配置维护。

## 本地开发

后端使用 JDK 17。在仓库根目录打包所需服务，例如管理后台：

```powershell
java -version
mvn -version
mvn -pl zero-admin -am package -DskipTests
java -jar zero-admin/target/zero-admin-1.0.0.jar --spring.profiles.active=dev
```

启动前准备对应环境的 MySQL、Redis 和 Nacos 配置，并核对服务端口与应用名。首次初始化时，先核对[各数据库脚本的归属和执行边界](AGENTS.md#技术栈与版本来源)。初始化脚本包含重建表语句，需确认目标为可重建的库。

另开终端，在 `zero-ui` 用 Node 24.21.0 和 npm 11 执行 `npm ci`、`npm run dev`。默认开发端口为 80，API 代理指向管理后台的 9099 端口。

## 模块与部署文档

| 主题 | 文档 |
| --- | --- |
| 调度中心与执行器 | [ZERO-JOB](zero-jobAdmin/README.md)、[ZERO-TASK](zero-task/README.md) |
| 报表代码生成 | [MyBatis-Plus 代码生成器](zero-report/src/test/resources/generator/README.md) |
| 后台功能 | [备忘录](docs/备忘录功能部署.md)、[实时日志](docs/实时日志部署与验证.md)、[运行统计](docs/运行统计部署与验证.md)、[WebSocket 与系统通知](docs/WebSocket集成与部署.md) |
| 前端构建和发布 | [开发与验证](zero-ui/README.md)、[发布与回退](前段项目升级方案/03-发布与回退方案.md) |

## 内置功能

1.  用户管理：用户是系统操作者，该功能主要完成系统用户配置。
2.  部门管理：配置系统组织机构（公司、部门、小组），树结构展现支持数据权限。
3.  岗位管理：配置系统用户所属担任职务。
4.  菜单管理：配置系统菜单，操作权限，按钮权限标识等。
5.  角色管理：角色菜单权限分配、设置角色按机构进行数据范围权限划分。
6.  字典管理：对系统中经常使用的一些较为固定的数据进行维护。
7.  参数管理：对系统动态配置常用参数。
8.  通知公告：系统通知公告信息发布维护。
9.  操作日志：系统正常操作日志记录和查询；系统异常信息日志记录和查询。
10. 登录日志：系统登录日志记录查询包含登录异常。
11. 在线用户：当前系统中活跃用户状态监控；会话编号后展示完整授权令牌（含Bearer前缀），支持复制和表格底部横向滚动。具有`monitor:online:list`菜单权限的账号可查看所有会话令牌，包含其它账号；分配该菜单即允许读取这些会话凭证。
12. 定时任务：在线（添加、修改、删除)任务调度包含执行结果日志。
13. 代码生成：13个模板已适配Java17/Spring Boot3及Vue3，支持单表、树表、主子表预览与CRUD代码下载；前端类型为空时默认Vue3，显式Vue2保留。验证及边界见“前段项目升级方案/04-代码生成模板集成与验证报告.md”。
14. 系统接口：根据业务代码自动生成相关的api接口文档。
15. 服务监控：监视当前系统CPU、内存、磁盘、堆栈等相关信息。
16. 缓存监控：对系统的缓存信息查询，命令统计等。
17. 在线构建器：拖动表单元素生成相应的HTML代码。
18. 连接池监视：监视当前系统数据库连接池状态，可进行分析SQL找出系统性能瓶颈。

系统接口文档使用 SpringDoc 2.8.17，与当前 Spring Boot 3.5 / Spring Framework 6.2 配套。后台菜单为“系统工具 → 系统接口”，文档 JSON 为 `/v3/api-docs`，Swagger UI 的配置入口 `/v3/api-docs/swagger-config` 只对 GET 放行。业务接口仍按登录和权限规则校验。调整 Maven 依赖后重新加载项目并重启 `zero-admin`。检查接口文档和安全规则时，从仓库根目录执行以下隔离测试：

```powershell
mvn -pl zero-admin -am test '-Dtest=SpringDocCompatibilityTest' '-Dsurefire.failIfNoSpecifiedTests=false'
```

后台页面通过Axios追加Bearer请求头，Swagger调试不会自动继承后台登录Cookie。调试业务接口前，在Swagger右上角Authorize填写当前会话的原始Token（不带`Bearer `前缀）；可在浏览器开发者工具中从正常业务请求的Authorization头取得，去掉前缀后仅粘贴到本机Swagger授权框，不发送到聊天或保存到文档。401表示认证未通过，授权后仍按原业务权限规则校验。

## 在线体验

- admin/admin123  
- 陆陆续续收到一些打赏，为了更好的体验已用于演示服务器升级。谢谢各位小伙伴。

演示地址：http://vue.zero.vip
文档地址：http://doc.zero.vip

## 演示图

<table>
    <tr>
        <td><img src="https://oscimg.oschina.net/oscnet/cd1f90be5f2684f4560c9519c0f2a232ee8.jpg"/></td>
        <td><img src="https://oscimg.oschina.net/oscnet/1cbcf0e6f257c7d3a063c0e3f2ff989e4b3.jpg"/></td>
    </tr>
    <tr>
        <td><img src="https://oscimg.oschina.net/oscnet/up-8074972883b5ba0622e13246738ebba237a.png"/></td>
        <td><img src="https://oscimg.oschina.net/oscnet/up-9f88719cdfca9af2e58b352a20e23d43b12.png"/></td>
    </tr>
    <tr>
        <td><img src="https://oscimg.oschina.net/oscnet/up-39bf2584ec3a529b0d5a3b70d15c9b37646.png"/></td>
        <td><img src="https://oscimg.oschina.net/oscnet/up-936ec82d1f4872e1bc980927654b6007307.png"/></td>
    </tr>
	<tr>
        <td><img src="https://oscimg.oschina.net/oscnet/up-b2d62ceb95d2dd9b3fbe157bb70d26001e9.png"/></td>
        <td><img src="https://oscimg.oschina.net/oscnet/up-d67451d308b7a79ad6819723396f7c3d77a.png"/></td>
    </tr>	 
    <tr>
        <td><img src="https://oscimg.oschina.net/oscnet/5e8c387724954459291aafd5eb52b456f53.jpg"/></td>
        <td><img src="https://oscimg.oschina.net/oscnet/644e78da53c2e92a95dfda4f76e6d117c4b.jpg"/></td>
    </tr>
	<tr>
        <td><img src="https://oscimg.oschina.net/oscnet/up-8370a0d02977eebf6dbf854c8450293c937.png"/></td>
        <td><img src="https://oscimg.oschina.net/oscnet/up-49003ed83f60f633e7153609a53a2b644f7.png"/></td>
    </tr>
	<tr>
        <td><img src="https://oscimg.oschina.net/oscnet/up-d4fe726319ece268d4746602c39cffc0621.png"/></td>
        <td><img src="https://oscimg.oschina.net/oscnet/up-c195234bbcd30be6927f037a6755e6ab69c.png"/></td>
    </tr>
    <tr>
        <td><img src="https://oscimg.oschina.net/oscnet/b6115bc8c31de52951982e509930b20684a.jpg"/></td>
        <td><img src="https://oscimg.oschina.net/oscnet/up-5e4daac0bb59612c5038448acbcef235e3a.png"/></td>
    </tr>
</table>


## 零号前后端分离交流群

QQ群： [![加入QQ群](https://img.shields.io/badge/已满-937441-blue.svg)](https://jq.qq.com/?_wv=1027&k=5bVB1og) [![加入QQ群](https://img.shields.io/badge/已满-887144332-blue.svg)](https://jq.qq.com/?_wv=1027&k=5eiA4DH) [![加入QQ群](https://img.shields.io/badge/已满-180251782-blue.svg)](https://jq.qq.com/?_wv=1027&k=5AxMKlC) [![加入QQ群](https://img.shields.io/badge/已满-104180207-blue.svg)](https://jq.qq.com/?_wv=1027&k=51G72yr) [![加入QQ群](https://img.shields.io/badge/已满-186866453-blue.svg)](https://jq.qq.com/?_wv=1027&k=VvjN2nvu) [![加入QQ群](https://img.shields.io/badge/已满-201396349-blue.svg)](https://jq.qq.com/?_wv=1027&k=5vYAqA05) [![加入QQ群](https://img.shields.io/badge/已满-101456076-blue.svg)](https://jq.qq.com/?_wv=1027&k=kOIINEb5) [![加入QQ群](https://img.shields.io/badge/已满-101539465-blue.svg)](https://jq.qq.com/?_wv=1027&k=UKtX5jhs) [![加入QQ群](https://img.shields.io/badge/已满-264312783-blue.svg)](https://jq.qq.com/?_wv=1027&k=EI9an8lJ) [![加入QQ群](https://img.shields.io/badge/已满-167385320-blue.svg)](https://jq.qq.com/?_wv=1027&k=SWCtLnMz) [![加入QQ群](https://img.shields.io/badge/已满-104748341-blue.svg)](https://jq.qq.com/?_wv=1027&k=96Dkdq0k) [![加入QQ群](https://img.shields.io/badge/已满-160110482-blue.svg)](https://jq.qq.com/?_wv=1027&k=0fsNiYZt) [![加入QQ群](https://img.shields.io/badge/已满-170801498-blue.svg)](https://jq.qq.com/?_wv=1027&k=7xw4xUG1) [![加入QQ群](https://img.shields.io/badge/已满-108482800-blue.svg)](https://jq.qq.com/?_wv=1027&k=eCx8eyoJ) [![加入QQ群](https://img.shields.io/badge/已满-101046199-blue.svg)](https://jq.qq.com/?_wv=1027&k=SpyH2875) [![加入QQ群](https://img.shields.io/badge/已满-136919097-blue.svg)](https://jq.qq.com/?_wv=1027&k=tKEt51dz) [![加入QQ群](https://img.shields.io/badge/已满-143961921-blue.svg)](http://qm.qq.com/cgi-bin/qm/qr?_wv=1027&k=0vBbSb0ztbBgVtn3kJS-Q4HUNYwip89G&authKey=8irq5PhutrZmWIvsUsklBxhj57l%2F1nOZqjzigkXZVoZE451GG4JHPOqW7AW6cf0T&noverify=0&group_code=143961921) [![加入QQ群](https://img.shields.io/badge/已满-174951577-blue.svg)](http://qm.qq.com/cgi-bin/qm/qr?_wv=1027&k=ZFAPAbp09S2ltvwrJzp7wGlbopsc0rwi&authKey=HB2cxpxP2yspk%2Bo3WKTBfktRCccVkU26cgi5B16u0KcAYrVu7sBaE7XSEqmMdFQp&noverify=0&group_code=174951577) [![加入QQ群](https://img.shields.io/badge/已满-161281055-blue.svg)](http://qm.qq.com/cgi-bin/qm/qr?_wv=1027&k=Fn2aF5IHpwsy8j6VlalNJK6qbwFLFHat&authKey=uyIT%2B97x2AXj3odyXpsSpVaPMC%2Bidw0LxG5MAtEqlrcBcWJUA%2FeS43rsF1Tg7IRJ&noverify=0&group_code=161281055) [![加入QQ群](https://img.shields.io/badge/已满-138988063-blue.svg)](http://qm.qq.com/cgi-bin/qm/qr?_wv=1027&k=XIzkm_mV2xTsUtFxo63bmicYoDBA6Ifm&authKey=dDW%2F4qsmw3x9govoZY9w%2FoWAoC4wbHqGal%2BbqLzoS6VBarU8EBptIgPKN%2FviyC8j&noverify=0&group_code=138988063) [![加入QQ群](https://img.shields.io/badge/已满-151450850-blue.svg)](http://qm.qq.com/cgi-bin/qm/qr?_wv=1027&k=DkugnCg68PevlycJSKSwjhFqfIgrWWwR&authKey=pR1Pa5lPIeGF%2FFtIk6d%2FGB5qFi0EdvyErtpQXULzo03zbhopBHLWcuqdpwY241R%2F&noverify=0&group_code=151450850) [![加入QQ群](https://img.shields.io/badge/已满-224622315-blue.svg)](http://qm.qq.com/cgi-bin/qm/qr?_wv=1027&k=F58bgRa-Dp-rsQJThiJqIYv8t4-lWfXh&authKey=UmUs4CVG5OPA1whvsa4uSespOvyd8%2FAr9olEGaWAfdLmfKQk%2FVBp2YU3u2xXXt76&noverify=0&group_code=224622315) [![加入QQ群](https://img.shields.io/badge/已满-287842588-blue.svg)](http://qm.qq.com/cgi-bin/qm/qr?_wv=1027&k=Nxb2EQ5qozWa218Wbs7zgBnjLSNk_tVT&authKey=obBKXj6SBKgrFTJZx0AqQnIYbNOvBB2kmgwWvGhzxR67RoRr84%2Bus5OadzMcdJl5&noverify=0&group_code=287842588) [![加入QQ群](https://img.shields.io/badge/已满-187944233-blue.svg)](http://qm.qq.com/cgi-bin/qm/qr?_wv=1027&k=numtK1M_I4eVd2Gvg8qtbuL8JgX42qNh&authKey=giV9XWMaFZTY%2FqPlmWbkB9g3fi0Ev5CwEtT9Tgei0oUlFFCQLDp4ozWRiVIzubIm&noverify=0&group_code=187944233) [![加入QQ群](https://img.shields.io/badge/228578329-blue.svg)](http://qm.qq.com/cgi-bin/qm/qr?_wv=1027&k=G6r5KGCaa3pqdbUSXNIgYloyb8e0_L0D&authKey=4w8tF1eGW7%2FedWn%2FHAypQksdrML%2BDHolQSx7094Agm7Luakj9EbfPnSTxSi2T1LQ&noverify=0&group_code=228578329) 点击按钮入群。
