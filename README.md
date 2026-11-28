# 零号（Zero）管理平台

零号是一个基于 Vue 3 和 Spring Boot 的前后端分离管理平台。管理后台提供组织与权限管理、业务参数和文件管理、运维监控及开发工具；任务调度、任务执行和跨服务监控由独立服务承担。

## 主要功能

| 类别 | 当前仓库提供的功能 |
| --- | --- |
| 系统管理与权限 | 登录认证和动态菜单；用户、部门、岗位、角色、菜单、系统字典和参数管理；角色菜单权限、按钮权限和数据范围管理。 |
| 业务数据与文件 | 业务参数设置维护业务字典分组和字典项，由报表服务处理；文件管理支持查询文件记录、上传和删除，由文件服务处理。系统字典与业务字典分别维护。 |
| 监控与运维 | 登录日志、操作日志、在线会话、服务和缓存监控、运行统计、实时日志；独立的 Spring Boot Admin 用于查看各服务开放的 Actuator 信息。 |
| 任务调度 | 管理后台保留 Quartz 定时任务及执行日志；独立的 XXL-JOB 调度中心负责管理调度任务、接收执行器注册、派发任务和收集结果。 |
| 开发工具与协作 | 表单构建、代码生成、系统接口文档，以及通知公告、WebSocket 系统通知和备忘录。 |

菜单是否可见、操作是否可用，取决于实际部署的菜单数据、账号权限和功能配置。系统接口文档入口为「系统工具 → 系统接口」，OpenAPI JSON 路径为 `/v3/api-docs`；查看文档不等于获得业务接口权限。

## 模块与调用关系

后端由 [Maven 父工程](pom.xml) 聚合八个模块，前端 `zero-ui` 独立构建。

| 模块 | 服务名 | 主配置 HTTP 端口 | 职责 |
| --- | --- | --- | --- |
| `zero-ui` | — | — | Vue 管理后台，负责页面、交互和数据展示。 |
| `zero-admin` | `zero-admin` | 9099 | 管理后台 API 入口，负责登录、权限、操作审计、后台自身业务及下游业务代理。 |
| `zero-report` | `zero-report` | 9101 | 报表与所属业务数据服务，例如业务字典的查询、管理和持久化。 |
| `zero-file` | `zero-file` | 9103 | 保存文件记录，支持本地磁盘和已适配的对象存储。 |
| `zero-jobAdmin` | `zero-job-admin` | 9102 | 独立的 XXL-JOB 调度中心。 |
| `zero-task` | `zero-task` | 9104 | XXL-JOB 执行服务，运行 `@XxlJob` 任务并回传结果。 |
| `zero-ai` | `zero-ai` | 9105 | 独立服务，保留同名 XXL-JOB 执行器。 |
| `zero-bootAdmin` | `zero-boot-admin` | 9100 | 发现服务实例并通过 Actuator 监控运行状态，不参与后台业务请求。 |
| `zero-tools` | — | — | Java 公共工具库，提供常量、返回对象和通用工具，不独立运行。 |

- **管理请求：** `zero-ui → zero-admin`。后台自身业务由 `zero-admin` 处理；业务字典和文件管理分别经后台代理调用 `zero-report` 和 `zero-file`。代理负责后台权限校验和操作审计，下游内部接口不会自动继承同等鉴权，部署时应限制其访问范围。
- **任务调度：** `zero-task` 和 `zero-ai` 分别向 `zero-job-admin` 注册并接收派发。执行器端口默认分别为 `9998` 和 `9999`，不同于应用 HTTP 端口；XXL-JOB 注册与 Nacos 服务发现分别配置。
- **运行监控：** `zero-bootAdmin` 通过服务发现定位实例，再访问该实例实际开放的 Actuator 端点。

表中是主配置声明值，不是实际部署结果。启动时还需核对环境配置、Nacos 注册名和启动参数。

## 技术基线

- 后端：Java 21、Spring Boot 4.0、Spring Cloud、Nacos、Spring Security、MyBatis / MyBatis-Plus、MySQL、Redis。具体版本以 [父 POM](pom.xml) 和各模块 POM 为准。
- 前端：Vue 3、Vite、Element Plus、Vue Router、Vuex。开发环境需要 Node `>=24.21.0 <25`、npm `>=11 <12`，以 [前端包清单](zero-ui/package.json) 为准。

## 本地开发

### 1. 准备依赖和数据库

安装 JDK 21、Maven、Node 和 npm。按所选服务准备 MySQL、Redis、Nacos 等依赖，并根据实际配置核对数据库名、连接参数及服务地址。

| 初始化脚本 | 用途 |
| --- | --- |
| [zero_admin.sql](sql/zero_admin.sql) | 管理后台数据：系统与权限表、初始菜单、代码生成等。 |
| [zero_biz.sql](sql/zero_biz.sql) | 业务字典、文件记录等业务库表。 |
| [quartz.sql](sql/quartz.sql) | 管理后台既有 Quartz 调度表。 |
| [zero_job.sql](sql/zero_job.sql) | 独立 XXL-JOB 调度库表。 |

初始化脚本可能包含删表或重建语句。执行前审阅脚本，确认连接的是可重建的开发库，不直接对已有业务数据的库执行。

### 2. 核对后端配置

按下面的[后端配置入口](#后端配置入口)检查所选环境的基础配置和外部依赖。若要使用业务参数设置或文件管理，还需启动对应下游服务，并确保管理后台能根据服务发现配置访问它们。

### 3. 启动所需后端服务

以下命令在仓库根目录执行，只构建并启动管理后台，不会启动全套服务：

```powershell
java -version
mvn -version
mvn -pl zero-admin -am package -DskipTests
java -jar zero-admin/target/zero-admin-1.0.0.jar --spring.profiles.active=dev
```

### 4. 启动前端

```powershell
cd zero-ui
npm ci
npm run dev
```

Vite 开发服务器默认端口为 `80`，可用 `VITE_DEV_PORT` 覆盖；开发代理默认指向 `http://localhost:9099`，可用 `VITE_DEV_PROXY_TARGET` 覆盖。以终端实际输出的访问地址为准。

## 后端配置入口

各服务在自己的 `src/main/resources/application.yml` 声明基础配置。使用 `--spring.profiles.active` 选择 `dev`、`test` 或 `prod`，并检查相应 `application-{profile}.yml`。

| 模块 | 配置入口 | 主要内容 |
| --- | --- | --- |
| `zero-admin` | [Nacos 模板](zero-admin/src/main/resources/nacosConfig) | 管理库、Redis、后台通用设置和 WebSocket。 |
| `zero-report` | [Nacos 模板](zero-report/src/main/resources/nacosConfig) | 业务数据源、Redis 和报表服务配置。 |
| `zero-file` | [Nacos 模板](zero-file/src/main/resources/nacosConfig) | 文件记录数据源及存储平台配置。 |
| `zero-jobAdmin` | [Nacos 模板](zero-jobAdmin/src/main/resources/nacosConfig) | 调度库与 XXL-JOB 调度参数。 |
| `zero-task` | [Nacos 模板](zero-task/src/main/resources/nacosConfig) | 任务数据源、Redis 和 XXL-JOB 执行器配置。 |
| `zero-ai` | [Nacos 模板](zero-ai/src/main/resources/nacosConfig) | 专属数据源、Redis 和 XXL-JOB 执行器配置。 |
| `zero-bootAdmin` | [主配置](zero-bootAdmin/src/main/resources/application.yml) | 使用自身环境配置设置监控和服务发现。 |

`nacosConfig` 是发布模板，并非应用实际读取的配置。发布前按 `spring.config.import` 核对 Data ID、配置分组和命名空间，再单独检查服务注册分组和注册名。`optional:` 允许配置项缺失，但依赖该配置的功能仍需提供有效值。真实凭据应通过受控配置维护，不写入仓库或文档。

## 模块与部署文档

- **前端：** [开发、构建与验证](zero-ui/README.md)、[发布与回退](前段项目升级方案/03-发布与回退方案.md)
- **调度：** [调度中心](zero-jobAdmin/README.md)、[任务执行器](zero-task/README.md)、[zero-ai 执行器](zero-ai/README.md)
- **代码生成：** [报表服务 MyBatis-Plus 代码生成器](zero-report/src/test/resources/generator/README.md)
- **后台功能：** [运行统计](docs/运行统计部署与验证.md)、[实时日志](docs/实时日志部署与验证.md)、[WebSocket 与系统通知](docs/WebSocket集成与部署.md)、[备忘录](docs/备忘录功能部署.md)
- **仓库开发规范：** [模块边界、新建模块与验证要求](AGENTS.md)
