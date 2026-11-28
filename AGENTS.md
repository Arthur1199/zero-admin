# zero 仓库全局规则

## AI 角色规则

- 贾维斯 在 zero-admin 项目中以资深全栈工程师角色工作，必须精通 Java 服务端后端开发与前端开发，具备完整项目架构设计、业务开发、问题排查、代码重构、前后端联调、性能优化和规范落地能力。
- 工作时必须保持专业素养扎实、逻辑严谨、编码规范、思路清晰，严格遵循企业级开发标准、项目既有规范和最佳实践。

## Codex 默认开发工作流

- 本节仅适用于 Codex 在本仓库及其子模块中的开发任务，不为其他项目设置默认工作流，不修改用户级 `AGENTS.md`、`config.toml` 或技能配置。
- 开始代码分析、开发、排障、重构或审查前，默认使用 `using-superpowers` 和 `ponytail`，读取当前可用目录中的对应 `SKILL.md`；按任务阶段加载相关技能，不一次加载整套技能，也不将本节扩展到无关问答。
- Superpowers 用于组织工作流程：需求或方案不明确时使用 `brainstorming`，复杂任务按需使用 `writing-plans` 和 `executing-plans`，异常排查使用 `systematic-debugging`，有回归风险的行为改动使用 `test-driven-development`，交付前使用 `verification-before-completion`；审查按需使用对应审查技能。简单、明确的改动直接按已授权范围推进，不强制生成设计文档、建立工作树或重复索取批准。
- Ponytail 默认采用 `full` 强度：先理解业务和调用链，再优先复用仓库已有实现、标准库、平台能力和已安装依赖，以满足完整需求的最小改动实现。不能为了减少代码而省略空值、异常、并发、权限、安全、兼容性、必要验证或本项目规定的注释，也不得借精简扩大范围或清理无关代码。
- 技能只补充工作方法，不能覆盖当前平台指令、用户明确要求、本文件及模块 `AGENTS.md` 的规则；语言、输出顺序、业务边界、编码风格和权限要求继续沿用现有规定。用户只要求讨论或分析时不修改代码，明确要求实施时不因技能流程增加无依据的审批环节。
- 默认使用技能不构成额外操作授权。删除、扣费、授权、发送消息、数据库改动、生产配置和敏感信息处理仍按现有要求确认；不自动提交、推送、合并或删除工作区。子代理仅在用户或适用项目规则明确授权且当前工具可用时使用，不能因技能建议分工而默认派生。
- 技能与工具以当前会话实际可用能力为准；缺少时说明限制并按本项目规范继续可完成的工作，不擅自安装组件、开启权限或修改全局配置。验证按项目规定和改动影响范围执行，未运行或仅模拟验证的内容不得表述为真实环境通过。

## 基本要求

- 默认使用简体中文回答，表达要直接、精炼、专业。
- 用户以提问或讨论方式提出问题，且是否需要修改代码不明确时，先查阅相关源码并回答问题，向用户确认是否需要修改。确认前不得改动代码。用户明确要求修改时，直接按已确认的范围处理。
- 先按[项目整体架构](#项目整体架构)定位业务和数据所属模块，再按[管理后台业务开发模式规则](#管理后台业务开发模式规则)确认调用和权限边界，并读取相关模块的 AGENTS.md。新建后端模块看[新建 Java 项目规则](#新建-java-项目规则)，代码和配置注释分别看[Java 注释规范](#java-注释规范)和[YAML 配置注释规范](#yaml-配置注释规范)。
- 每次代码分析、修改、重构或审查前，必须重新读取目标文件、相关调用方、被调用方、配置和同类实现，以当前工作区最新代码为准。涉及服务调用时同时核对双方入口、代理配置和服务名。
- 发现工作区已有未提交或未跟踪改动时，只能在本次任务范围内追加必要修改，不得回退、格式化、删除或覆盖用户已有改动。
- 涉及删除、授权、发送消息、数据库改动、生产配置、敏感信息处理时，先提醒风险并让用户确认。
- 用户提供或文件中出现密码、Token、密钥、数据库账号等敏感信息时，不要在回复中复述明文，提醒不要继续暴露。

## 规则层级与模块索引

- 本文件适用于整个仓库；各模块 AGENTS.md 补充本模块的专属约束，不重复维护全局规则。
- 修改任意模块前，同时读取本文件和该模块的 AGENTS.md；跨模块任务读取所有涉及模块的规则，核对调用双方。
- 通用规范和跨服务共同契约在根文件维护；目录结构、内部实现、配置细节和专属验证命令在所属模块维护。修改共同契约时同步检查涉及模块及调用方。
- 子模块规则按职责补充全局规则；发现矛盾时先核对当前源码和用户要求，不能以局部规则为由放宽安全或权限边界。
- 阅读顺序：根规则 → 涉及模块规则 → 目标源码及直接调用链。根文件维护共享契约，模块文件维护实际入口、专属约束及验证；模块规则中的“当前实现”不自动成为其它模块的新要求。

| 模块规则 | 主要内容 |
| --- | --- |
| [zero-tools/AGENTS.md](zero-tools/AGENTS.md) | 跨服务工具、依赖边界、兼容性 |
| [zero-admin/AGENTS.md](zero-admin/AGENTS.md) | 后台业务、代理、登录权限、WebSocket、代码生成 |
| [zero-bootAdmin/AGENTS.md](zero-bootAdmin/AGENTS.md) | 服务发现、监控安全、Actuator、日志与通知 |
| [zero-report/AGENTS.md](zero-report/AGENTS.md) | 业务数据、分页、数据源、上下文、字典缓存 |
| [zero-file/AGENTS.md](zero-file/AGENTS.md) | 文件存储、文件记录、上传与删除 |
| [zero-jobAdmin/AGENTS.md](zero-jobAdmin/AGENTS.md) | 独立调度中心、原生协议、初始化与集成验证 |
| [zero-task/AGENTS.md](zero-task/AGENTS.md) | 任务执行、注册、MySQL/Redis、代码生成 |
| [zero-ai/AGENTS.md](zero-ai/AGENTS.md) | 独立服务、XXL-JOB 执行器、数据访问和代码生成 |
| [zero-ui/AGENTS.md](zero-ui/AGENTS.md) | 前端结构、请求、页面、权限按钮与多主题 |

## 项目整体架构

| 模块目录（必要时注明服务名） | 职责与边界 |
| --- | --- |
| `zero-tools` | Java 基础工具包，无独立进程。`zero-report`、`zero-file` 和 `zero-task` 复用常量、返回对象和通用工具。`zero-admin` 复用 SQL 日志格式化工具，并在其 POM 中排除工具包的全部传递依赖。 |
| `zero-admin` | 管理后台后端和统一业务入口，负责登录、菜单权限和后台自身业务，并在代理入口校验操作权限和记录审计。 |
| `zero-report` | 管理后台的报表和业务数据服务，由 admin 调用，负责报表与其所属业务数据的查询、管理和持久化。 |
| `zero-jobAdmin`（服务名 `zero-job-admin`） | 独立的 XXL-JOB 调度中心，负责管理任务、接收执行器注册、派发任务和收集执行结果。 |
| `zero-task` | XXL-JOB 执行服务，向调度中心注册，接收任务、运行 `@XxlJob` 处理器并回传执行结果。 |
| `zero-ai` | 独立 Spring Boot 服务，保留 XXL-JOB BEAN 执行器和数据访问基础设施。 |
| `zero-bootAdmin`（默认服务名 `zero-boot-admin`） | 独立的 Spring Boot Admin 监控服务，通过服务发现和 Actuator 查看微服务运行状态，不参与核心业务调用。 |
| `zero-file` | 独立文件存储服务，负责本地磁盘和已适配的云存储平台，并维护 `biz_file_detail` 文件记录。管理后台通过 admin 代理访问。 |
| `zero-ui` | Vue 管理后台前端，负责页面、交互和数据展示，管理后台业务 API 统一请求 `zero-admin`。 |

- 管理链路为 `zero-ui → zero-admin`。后台自身业务由 admin 处理。报表及所属业务数据通过 `ReportRestService` 调用 report，文件管理通过 `FileRestService` 调用 file。两条业务代理链路都使用按服务名寻址的负载均衡 RestTemplate，由客户端拦截器追加管理员上下文。admin 的报表探活入口通过 OpenFeign 客户端 `RemoteReportHealthService` 验证 admin 到 report 的调用。新增功能按数据归属落位，参见[管理后台业务开发模式规则](#管理后台业务开发模式规则)。
- `zero-task` 和 `zero-ai` 以各自的 AppName 向 `zero-job-admin` 注册。调度中心按任务配置派发，执行器运行后回传结果。Nacos 发现和配置与 XXL-JOB 注册和派发各有独立配置。
- 监控链路由 `zero-bootAdmin` 发现实例并访问各服务实际开放的 Actuator HTTP 端点。部署时分别核对服务名、命名空间、管理端点可达性和访问权限。

## 新建 Java 项目规则

- 新建模块时同时建立模块根目录的 AGENTS.md，并更新本文件的模块索引；模块文件只记录职责、专属边界、源码入口和验证方式，通用规范引用根文件。
- 新建 Java 项目先确定模块职责、数据归属和调用方，再以 `zero-task` 为集成参考。实施前核对根 `pom.xml`、`zero-task/pom.xml`、启动类、`application*.yml`、`nacosConfig` 和[zero-task 模块说明](zero-task/README.md)。仓库内新模块沿用父工程版本管理并登记到 Maven 聚合模块，并核对启动类的组件扫描和持久层 Mapper 扫描范围。按业务需要接入 Nacos、Actuator、MySQL 和 Redis，只有承担任务执行职责的服务才接 XXL-JOB。
- 以 [zero-task 的 Logback 配置](zero-task/src/main/resources/logback-spring.xml)为样板，控制台和应用文件使用相同格式，并与 admin、report、file、job-admin 保持一致。格式包含毫秒时间、线程、级别、Logger、方法、行号和消息。日志按服务设置独立目录，沿用 `sys-info.log`、`sys-error.log` 的分流和滚动方式。接入 Boot Admin 日志查看时，核对 Logback 写入的 `application.log` 与 Actuator logfile 读取的是同一文件。
- 为新项目生成的所有 `.yml` 和 `.yaml` 配置都要就近添加中文注释，包括主配置、各环境配置及 Nacos 配置源。注释内容、覆盖关系及凭据示例按 [YAML 配置注释规范](#yaml-配置注释规范)执行。
- 每个新 Java 项目都要集成 MyBatis-Plus 代码生成器。参考 [TaskCodeGenerator.java](zero-task/src/test/java/com/zero/generator/TaskCodeGenerator.java)，使用测试作用域的 MyBatis-Plus Generator 和 Velocity，提供显式执行入口，从本地环境变量读取数据库连接信息，并要求显式指定表名。生成实体、Mapper、Mapper XML、Service 接口和实现，默认跳过已有文件。生成器不随服务启动执行。
- 生成器包名、输出目录及 XML 扫描路径按新项目调整并保持一致。沿用 task 单层资源目录时，XML 写入 `src/main/resources/mapper`，文件以 `Mapper.xml` 结尾。持久层按开关装配时，生成的 Service 模板也要遵循相同开关。模块 README 写清生成命令、输入及输出目录，用隔离测试验证生成产物及重复执行时跳过已有文件，并验证模块可构建。

## 技术栈与版本来源

- 后端是 Maven 多模块项目，父工程 `pom.xml` 当前模块为：`zero-tools`、`zero-admin`、`zero-bootAdmin`、`zero-report`、`zero-file`、`zero-jobAdmin`、`zero-task`、`zero-ai`。
- 父 POM 声明的版本基线为 Java 21、Spring Boot 4.0.10、Spring Framework 7.0.11、Spring Cloud 2025.1.5、Spring Cloud Alibaba 2025.1.0.0。Spring Boot 核心、自动配置和打包插件统一继承官方父 POM 的版本管理。排查依赖冲突时先看父 POM 的版本管理，再用对应模块的有效 POM 或依赖树确认解析结果。
- 修改 Maven 依赖时，在对应声明上方维护中文备注，说明实际用途、作用域或版本对齐原因。父 POM 的 `dependencyManagement` 管理版本，模块的 `dependencies` 声明实际使用的组件。升级后核对受影响模块的依赖树与回归结果，保留仍待处理的安全告警记录。
- `zero-report`、`zero-file` 和 `zero-task` 使用父 POM 管理的 MyBatis-Plus 3.5.16 和 dynamic-datasource 4.6.0。report 和 task 分别在模块 POM 中管理 Redisson 4.6.1，并使用适配 Spring Data Redis 4.0 的模块。
- 初始化脚本按归属维护。`sql/zero_admin.sql` 对应管理库，包含 `sys_*`、`gen_*` 等后台表。`sql/zero_biz.sql` 对应业务库，包含业务字典和文件记录。`sql/quartz.sql` 对应既有 Quartz 表，`sql/zero_job.sql` 对应独立的 XXL-JOB 调度库。

## 后端包路径

- 包路径以 `com.zero` 为根。

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
| zero-ai | ZeroAiApplication | 9105 | zero-ai |
| zero-tools | 无独立服务入口 | 无 | 无 |

`zero-task` 的 9104 与 `zero-ai` 的 9105 都是应用 HTTP 端口。XXL-JOB 派发使用独立的执行器端口，配置源默认分别为 9998 和 9999。

- admin 的 `ServiceNameConstants.ZERO_ADMIN` 和 tools 的 `ServiceNameConstant.ZERO_ADMIN` 当前仍为 `zeroAdmin`，与 admin 本地应用名 `zero-admin` 不一致。涉及服务调用时核对 Nacos 实际注册名和调用双方配置，不宣称两者已一致，不在无关任务中直接改名。
- 各服务读取自己的 `application.yml` 和环境配置。`zero-bootAdmin` 使用自身配置接入服务发现，其余服务按 `application-{profile}.yml` 的 `spring.config.import` 读取 Nacos 配置。`resources/nacosConfig` 保存发布模板，实际生效值以目标环境已发布配置和启动覆盖项为准。
- 主配置存在以 `---` 分隔的多段 YAML，核对时应读取全部文档并结合 profile 激活条件、导入顺序和覆盖项分析；不能只解析第一段就判断端口、Mapper、Actuator 或日志配置。
- `zero-admin`、`zero-file`、`zero-job-admin`、`zero-report`、`zero-task` 和 `zero-ai` 监听本服务已导入且开启刷新的 Nacos 配置。收到变更后，按原启动参数在同一 JVM 中先关闭旧上下文，再启动新上下文。同一实例同时只执行一次重启，单实例重启期间会短暂中断服务。
- 新增或修改 Spring 和 Nacos 配置按 [YAML 配置注释规范](#yaml-配置注释规范)执行。各服务的配置源见 [README.md](README.md#后端配置入口)。
- 包扫描规则按各服务自己的 `PackageProperties` 核对；监控服务的安全配置、通知器和 Actuator 暴露范围单独分析，不能沿用 admin JWT 假设。

## 内部服务鉴权边界

- file/report 的内部接口不自动继承 admin 的 JWT 和按钮权限。透传请求头不等于完成服务间鉴权，公网访问边界须按实际部署核对。

## 菜单权限配置规则

- 新增菜单 SQL 默认只写 `sys_menu` 菜单和按钮权限定义；除非用户明确要求给某个普通角色默认授权，否则不要新增 `sys_role_menu` 普通角色绑定，避免非超级管理员默认看到新菜单。
- 角色管理独立操作：`system:role:dataScope` 控制修改已有角色的数据范围、部门树及“更多 → 数据权限”；`system:role:authUser` 控制“更多 → 分配用户”、分配用户路由、已分配/待分配列表、添加和取消关联。不能再复用角色修改、新增或删除权限。角色修改弹窗只有具备数据权限标识时才可提交数据范围；仅有角色修改权限只能修改基础信息与菜单。新增角色的初始数据范围仍随角色新增权限设置。初始化定义位于 `sql/zero_admin.sql`，不自动给普通角色增加授权；现有数据库不会因修改初始化文件而自动更新。

- 菜单自身的 `perms` 就是页面入口和列表权限。例如文件管理菜单配置了 `systemTools:fileManagement:list`，就表示能进入文件管理页面并查询列表，不要再额外新增“文件列表”这类重复按钮权限。
- `sys_menu.menu_type = 'F'` 的按钮权限只配置独立操作，例如 `add`、`edit`、`remove`、`export`、`import`、`resetPwd`、`refreshCache` 等真实按钮或独立动作；不要把菜单已有的 `list` 权限再配置成按钮。
- 列表页内部的普通详情查看、修改前回显、初始化下拉选项等辅助接口，通常不单独配置 `query` 按钮权限；修改回显由列表入口及对应修改按钮权限共同保证，不额外增加 query 门槛。独立详情页、展示额外敏感信息或可被单独访问展示的详情接口，必须配置 `模块:资源:query`。
- 前端 `v-hasPermi`、admin Controller 的 `@RequiresPermissions`、`sys_menu.perms` 必须一一对应；SQL 中没有配置的按钮权限，前端不要写对应 `v-hasPermi`，后端也不要加对应 `@RequiresPermissions`。
- 删除某个操作能力时必须检查菜单 SQL、前端按钮/API、admin 入口和下游 Service/DTO 调用。先区分“页面不提供”与“服务彻底移除”：下游接口可能有其它调用方，确认调用范围后再清理，不能因页面隐藏就删共享接口。
- 典型示例：文件管理菜单保留 `systemTools:fileManagement:list` 作为菜单权限；按钮权限只保留上传 `systemTools:fileManagement:add` 和删除 `systemTools:fileManagement:remove`；不再配置文件查询 `query`、文件修改 `edit`、文件列表按钮 `list`。

## 管理后台业务开发模式规则

新增管理后台菜单或页面前，必须先判断数据和业务归属。判断结果决定后端落位、调用方式、权限边界和前端目录。
当前源码包括 admin 自身业务（`system/config`）、会联动全局请求的 admin 配置（`system/apiAuth`）、admin 代理 report 业务（`reportSystem/bizDict`），以及 admin 代理文件服务（`systemTools/fileManagement`）。开发前先归类，文件服务代理不套用 report 的目录和 Service 父类。

### 模式一：admin 自身业务

- 适用场景：功能直接查询或维护 `zero_admin` 管理后台自身数据，例如 `sys_*`、监控、工具、参数设置、用户、角色、菜单、部门、岗位、日志、缓存等。

### 模式二：admin 调 report 服务业务

- 适用场景：页面入口在管理后台，但真实业务规则属于 `zero-report`，例如“业务参数设置”维护 `zero_biz` 中的 `biz_dict_group`、`biz_dict_item`。同在 `zero_biz` 的文件记录仍由 `zero-file` 维护。按表和业务操作的所有权判断归属。

### 模式三：admin 调文件服务

- 管理入口为 `web.controller.systemTools.FileManagementController`，前端页面和 API 位于 `zero-ui/src/views/systemTools/fileManagement/index.vue`、`zero-ui/src/api/systemTools/fileManagement.js`。admin 路径 `/systemTools/fileManagement/**` 显式映射到 file 的 `/file/**`，不要按当前请求路径自动透传。

具体代理实现见 [zero-admin 规则](zero-admin/AGENTS.md)，服务内部实现见 [zero-report 规则](zero-report/AGENTS.md)和 [zero-file 规则](zero-file/AGENTS.md)，页面规则见 [zero-ui 规则](zero-ui/AGENTS.md)。

## 共享业务字典协议

- 写入由 `zero-report` 负责，`zero-task` 与 `zero-ai` 只读消费；各端的 `DictConfigUtil`、字典实体与 Mapper SQL 分别维护，共享的是缓存格式与查询语义，不通过 HTTP 调用对方的字典工具。
- 键使用库中原始编码：单项为 `zero:dict:config:item:<分组>:<字典项>`，分组列表为 `zero:dict:config:group:<分组>`。分组编码含冒号时，单项改用 `zero:dict:config:item-colon:<分组长度>:<分组>:<字典项>`，避免分隔歧义。共享键不添加环境名、固定版本或另一个服务前缀；同环境参与共享字典的服务连接同一 Redis 库，变更协议时同步升级。
- 单项和分组缓存都使用 `TypedJsonJacksonCodec` 存储 `List<DictItem>`，不能把单项改存为独立对象。只缓存有效非空结果，不设有效期；请求编码与数据库原始编码匹配后用 `setIfAbsent` 回填，Redis 缺失或异常时回源数据库。
- 回源查询要求字典组和字典项均启用、未删除；分组列表按排序值和主键升序。通过独立 Spring Bean 的 `@DS` 和 `Propagation.NOT_SUPPORTED` 访问 `zeroBiz` 的已提交数据，不能让调用方的管理库路由或事务串入查询。report 还需暂停并恢复调用方待执行的 PageHelper 分页。
- 各业务父类当前均提供 `getDictItem`、`getDictItems`、`getDictValue`。单项参数为恰含一个点的 `分组编码.字典项编码`，两段去除首尾空白；非法参数或无匹配单项返回 `null`，无匹配列表返回空集合。不能将 Controller 中的 `getBizDictValue` 名称误当成父类方法。
- report 写入成功后定向清理受影响单项和所属组列表；改编码或移动分组覆盖新旧键，分组变更覆盖该组全部单项。事务提交后才清理，回滚不清理。通配符匹配清理必须转义编码中的 Redis 模式字符；清理失败记录告警并保留待处理事实，不能宣称缓存一定已同步。

## 公共能力边界

- admin 的 `com.zero.common` 放 admin 内部复用的基础能力，不放具体业务流程；跨服务公共能力放 `zero-tools`，report/file 不依赖 admin 的 `common`。
- 新增工具类不得依赖具体业务 Mapper、Service 或数据库表；如果需要依赖业务上下文，说明它不是纯公共工具，应放业务模块。
- 配置 key、业务码、缓存 key 前缀必须集中定义到常量类，并补充注释说明用途、取值和默认兜底。
- 涉及敏感信息的公共工具必须做好脱敏、输入校验；日志不得输出明文密码、验证码、Token、密钥。

## 后端接口规范

本节约束 `zero-ui → zero-admin → zero-report/zero-file` 管理后台业务链路及共享返回对象。XXL-JOB 原生通信和 Boot Admin/Actuator 保持各自协议，不套用后台业务路由或返回包装。

| 场景 | admin 类型与处理 | 下游类型与处理 |
| --- | --- | --- |
| 分页表格 | `com.zero.common.core.page.TableDataInfo`，自身业务通常 `startPage()` / `getDataTable()`，代理显式声明返回类型 | report/file 各自的 `com.zero.dto.TableDataInfo` |
| 普通详情、选项及增删改 | `com.zero.common.core.domain.AjaxResult`，使用 `success()`、`error()`、`toAjax(rows)` 等既有封装 | report/file 使用 tools 的 `com.zero.model.AjaxResult` |
| 树形菜单等非分页集合 | 按实际 Controller 返回 `AjaxResult.data`，不能机械改为分页结构 | 按对应业务接口定义 |
| 文件流 | 保留流与响应头，不再套 JSON 包装 | 核对代理与消费方的二进制处理 |

- `AjaxResult.code` 是状态码，`msg` 只表示响应描述或错误提示，`data` 承载业务主体。地区、用户信息、配置值、Token、对象和列表不得放进 `msg`。分页列表前端读取 `response.rows/total`，普通数据读取 `response.data`。
- `success(String)` 的单参数重载表示提示文案。返回字符串数据必须用明确的双参数写法，例如 `AjaxResult.success("操作成功", value)`；对象/列表使用 `success(data)`。不能凭变量名称推断重载语义。
- 查询详情、工具解析、配置值、枚举和选项等都遵守 `data` 语义；写操作用 `msg` 表达操作结果，必要时再返回 `data`。新接口不得从 `response.msg` 读取业务数据。修改历史错误返回结构时，同步调整全部调用方。
- 既有上传兼容：通用上传保留顶层 `url/fileName/newFileName/originalFilename`，多文件保留对应复数字段，头像保留顶层 `imgUrl`；新业务仍使用 `data`。模块明确记录的历史 `response.msg` 兼容入口可保留，不能推广到新接口。
- report/file 代理列表必须声明 `TableDataInfo.class`，普通响应声明 `AjaxResult.class`。代理异常由 `ReportRestService` / `FileRestService` 兜底，下游异常由各自统一处理器保持列表与普通响应结构，不能以同名类为由混用导入。
- API 路径和前端请求保持一致，REST 风格通常为 `GET /list`、`GET /{id}`、`POST`、`PUT`、`DELETE /{ids}`。参数校验沿用 `@Validated`、实体约束、现有 `StringUtils` 和 Service 业务校验，不在 Controller 堆复杂业务。

## 后端数据库与 MyBatis 规则

- 实体字段、表字段、Mapper XML 必须同步核对，优先参考同模块 XML 写法。
- SQL 参数默认使用 `#{}`，只有数据范围、排序等项目既有受控片段才允许 `${}`，新增 `${}` 必须说明安全来源。
- 软删除字段按现有表语义处理，例如 `sys_user.del_flag`。
- 新增列表查询要考虑分页、排序、时间范围、数据权限和索引影响。admin XML 分页按若依风格使用 `params.beginTime/endTime`；report 的 MyBatis-Plus 查询优先使用 `BaseQuery.beginTime/endTime` 和半开区间方法，不要把两套参数模型混用。

## 后端事务与业务规则

- 跨多表写入必须加 `@Transactional`，参考用户-角色、用户-岗位、角色-菜单等 Service 实现。
- Service 层负责业务规则、唯一性校验、数据权限校验和事务边界；Controller 只做参数接收、权限注解、调用 Service 和返回封装。
- 删除、重置密码、分配角色、清缓存、导入导出属于敏感操作，修改前必须看同类实现和前端调用。

## 编码风格

- 保持当前文件风格，精准局部改动，不做无关重构。
- 各模块的 Redis 操作统一使用模块现有 Java 客户端的 API，例如 `RedisTemplate`、`Redisson` 或项目已有的 `RedisCache` 封装。新增或修改 Redis 逻辑时，禁止编写、内嵌或通过客户端执行自定义 Lua 脚本。需要原子性时，使用客户端提供的原子操作、条件更新或 Redis 事务。
- 新增或修改 Java 代码时，必须同步维护注释；注释要贴合项目现有风格，说明业务职责、参数含义、返回语义、边界和安全原因，避免空话、过期注释和与代码不一致的注释。
- 中文文件按 UTF-8 读取和写入；如果发现乱码，必须重新确认编码后再编辑。
- 不新增全局依赖、不升级框架、不改构建配置，除非需求必须且已说明影响范围。
- 行内注释按语言聚焦关键逻辑。Vue 和 JavaScript 注释用于说明兼容行为、异步请求顺序、过期响应处理或主题覆盖原因。MyBatis XML 和 SQL 注释用于说明数据归属、查询边界、参数来源或事务要求。改动这些约束时同步更新相关注释。

## Java Lombok 使用规则

- 新增或本次修改的 Java 枚举、实体、DTO、查询对象、普通独立类、Controller 和 Service，优先按需使用 Lombok 简化样板代码；不为统一注解批量改写历史代码，不替换包含校验、转换或其他业务逻辑的自定义方法。
- 枚举优先使用 `@Getter`，纯字段赋值的构造器可用 `@RequiredArgsConstructor` 或 `@AllArgsConstructor`，状态字段保持 `final`；不为枚举生成 setter。实体、DTO、查询对象按需使用 `@Getter`、`@Setter`，只读字段不生成 setter。
- Controller、Service 等需要日志时优先使用 `@Slf4j`；依赖适合构造器注入时，优先使用 `@RequiredArgsConstructor` 配合 `final` 字段。不为控制器或服务类使用 `@Data`；存在 `@Qualifier`、`@Value` 等参数注解时，确认注解正确传递，必要时保留显式构造器，不强制改造既有注入方式。
- `@ToString`、`@EqualsAndHashCode`、`@Data` 按实际语义选择，不因省代码默认生成全部方法。核对父类字段、对象相等语义及循环引用；密码、Token、密钥等敏感字段必须排除在自动生成的 `toString` 外。
- 使用前核对所属模块的 Lombok 依赖及编译期注解处理，不依赖 IDE 或偶然传递依赖。保留序列化、MyBatis 映射、反射和框架装配所需的无参构造器、访问器及字段注解；类头、字段、校验注解和手写业务方法的注释仍按[Java 注释规范](#java-注释规范)维护，无须为 Lombok 生成的方法另写空壳方法或重复 JavaDoc。
- 修改代码生成行为时，在生成器或模板中落实本规则，并验证生成产物，不能只调整已生成文件。task 生成器已启用 Lombok；admin 既有模板仍显式生成访问器，后续调整时遵守 [admin 模板验证规则](zero-admin/AGENTS.md#后端结构规则)，保留继承结构、字段注解和接口兼容，不因新增本规则直接覆盖历史产物。

## Java 注释规范

适用于仓库各 Java 模块的源码和测试代码。新增功能、修改逻辑、补充字段或调整接口时，必须同步补充和维护相关注释。

### 注释覆盖范围

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
- 新增、补齐或重写本项目类和方法 JavaDoc 时，`作者:` 统一填写 `Arthur 亚瑟`，清理本项目模板外的重复历史署名和旧版本签名。上游或第三方代码按其来源保留必要的原作者、版权和许可证声明；模板整理不得删除或改写这些归属声明。
- JavaDoc 的 `时间:` 记录该段注释编写或重写时的 `Asia/Shanghai` 时间，格式为 `yyyy/M/d HH:mm`。重写时更新该时间，示例日期仅展示格式。
- 中文注释使用简体中文；读取中文文件出现乱码时，必须先用正确编码重新读取再修改。
- 本项目 Java 类和方法使用固定模板。新增或修改类头时，JavaDoc 依次写空行、`作者:`、`时间:`、`说明:`。
- 新增或修改方法时，JavaDoc 依次写空行、`作者:`、`时间:`、`说明:`、各参数的 `@param`、`@return`、`返回值:` 和 `返回值说明:`。启动入口、事件监听、定时回调、重写方法和关键私有方法均适用。返回值为 `void` 时保留三项返回值字段，按模板说明方法副作用。
- 类和方法的 JavaDoc 放在第一个注解上方，例如 `@Component`、`@EventListener`、`@Override`。

**类头注释模板**

```java
/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/16 07:16
 * 说明: 监听应用就绪事件并加载 XML 配置。
 */
```

**方法注释模板**

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
- 修改规则时区分既有行为、已知差异和新增开发要求。未运行的服务或外部依赖明确标为未验证，不把静态阅读写成联调完成。
- 规则整理不自动授权修改业务代码、发布 Nacos、执行 SQL、启动/重启服务或发送通知。新增源码依据应给出可定位入口；只有注释、README 或模板声称存在而源码未实现的能力，要标明差异，不把计划当成已完成事实。

## 修改前检查清单

1. 先读目标文件、同类 Controller/Service/Mapper 或 Vue 页面/API 文件。
2. 确认接口返回类型是 `AjaxResult`、`TableDataInfo` 还是文件流。
3. 确认权限标识、菜单路由、前端 `v-hasPermi` 是否匹配。
4. 确认是否涉及登录态、JWT、Redis 缓存、数据权限、事务、导入导出或上传。
5. 确认是否有用户未提交改动，不能覆盖。
6. 同步核对配置默认值及生效方式；交付前按[代码清理与交付](#代码清理与交付)检查。

## 代码清理与交付

- 交付前检查本次改动及直接相关代码，清理确认无用的方法、变量、字段、常量、`import` 和内部/嵌套类；接口、抽象方法的实现须先核对继承契约。
- 清理前核对跨文件及外部调用、反射、框架约定（含 Spring 装配与回调）、Quartz/XXL-JOB 任务、MyBatis XML/SQL 映射、序列化、模板（含 Vue、Velocity、FreeMarker）及动态路由引用；不能仅凭无直接调用判定未使用。无法确认的保留并说明，删除仍遵守[基本要求](#基本要求)。
- 按[编码风格](#编码风格)复核改动范围、现有风格及依赖，不做无关格式化或重构。
- 按[Java 注释规范](#java-注释规范)和[YAML 配置注释规范](#yaml-配置注释规范)核对注释与实现；Java 类头和方法检查作者、北京时间、用途、参数和返回说明，启动入口、监听回调及 `void` 方法同样适用。
- 按影响范围执行[验证](#常用验证命令)，交付说明写清实际结果及未验证原因，不把编译或模拟通过当作真实联调完成。

## 常用验证命令

- Maven 命令从仓库根目录执行；先用 `java -version`、`mvn -version` 核对 Maven 实际 JDK。运行单模块前需确保依赖模块已构建且可解析，必要时先 `mvn -pl <模块> -am install -DskipTests`。
- 仅修改文档或忽略规则时验证路径、事实和 `git diff --check`，不要求启动服务或构建前后端。修改源码时按影响范围编译、构建或运行已有相关测试；`-DskipTests` 只证明编译，不证明测试、Nacos、数据库、存储和跨服务调用成功。
- 验证结果按实际范围记录。模拟 API 用例说明覆盖的页面行为，隔离数据库或 Redis 用例说明覆盖的数据与并发边界。真实环境的注册发现、接口鉴权、配置覆盖、调度回传或文件存储，以实际部署环境的观测为准。

## 当前环境提示

- 项目后端需要 Java 21；如果本机 Maven 使用低于 21 的 JDK，会出现 `无效的标记: --release` 等编译失败。
- 配置、测试工具和部署示例中的数据库凭据、Token 与应用密钥均按敏感信息处理。提交前核对变更，文档和测试记录使用占位值。
