# zero / ruo-yi-zero 项目规则

## 基本要求

- 默认使用简体中文回答，表达要直接、精炼、专业。
- 每次代码分析、修改、重构或审查前，必须重新读取目标文件、相关调用方、被调用方、配置和同类实现，以当前工作区最新代码为准。
- 发现工作区已有未提交或未跟踪改动时，只能在本次任务范围内追加必要修改，不得回退、格式化、删除或覆盖用户已有改动。
- 涉及删除、授权、发送消息、数据库改动、生产配置、敏感信息处理时，先提醒风险并让用户确认。
- 用户提供或文件中出现密码、Token、密钥、数据库账号等敏感信息时，不要在回复中复述明文，提醒不要继续暴露。

## 当前项目真实技术栈

- 后端是 Maven 多模块项目，父工程 `pom.xml` 当前模块为：`zero-tools`、`zero-admin`、`zero-bootAdmin`、`zero-report`。
- 父工程当前基线：Java 17、Spring Boot 3.5.12、Spring Framework 6.2.18、Spring Cloud 2025.0.2、Spring Cloud Alibaba 2025.0.0.0。不要按 Java 8 或旧 Spring Boot 2 项目处理，也不要引入未经项目验证的新框架。
- `zero-admin` 是管理后台服务，主入口为 `com.zero.ZeroAdminApplication`，启用 Nacos 注册/配置、OpenFeign、LoadBalancer，并排除默认数据源自动配置。主要技术包括 Spring Security、JWT、MyBatis XML、PageHelper、Druid、Redis、Quartz、Actuator、SpringDoc、Kaptcha、Fastjson2、POI。
- `zero-report` 是报表/业务服务，主入口为 `com.zero.ZeroReportApplication`，启用 Nacos 注册/配置、OpenFeign、`@MapperScan`。主要技术包括 MyBatis-Plus、MyBatis-Plus 分页拦截器、dynamic-datasource、PageHelper（复杂 XML 报表 SQL 保留使用）、Redis、Actuator、Validation、Lombok。
- `zero-tools` 承载跨服务复用的基础常量、统一返回对象和通用工具，`zero-report` 当前直接依赖它；不要把只属于 admin 的 `common` 类误搬到 `zero-tools`。
- `zero-bootAdmin` 是服务监控相关模块，分析监控功能时需单独读取该模块和各服务 Actuator 暴露配置。
- 前端模块：`zero-ui`，Vue 2.6 + Vue CLI 4 + Vue Router 3 + Vuex 3 + Element UI 2。
- 前端请求：Axios 封装在 `zero-ui/src/utils/request.js`，开发代理在 `zero-ui/vue.config.js`，后端默认 `http://localhost:9099`。
- 数据库脚本当前分为 `sql/zero_admin.sql`、`sql/zero_biz.sql`、`sql/quartz.sql`。`zero_admin` 承载管理后台和若依系 `sys_*`、`gen_*`、监控/配置等表；`zero_biz` 承载业务库表，例如 `biz_dict_group`、`biz_dict_item`。

## 桌面安装包打包规则

- 桌面安装包配置文件固定为 `apps/packageConfig.json`，打包前必须重新读取该文件，以当前配置为准，不要使用记忆中的旧 URL、旧版本号或旧命名规则。
- 配置文件中的环境 key 固定为：`dev` 表示本地环境，`test` 表示测试环境，`prod` 表示生产环境。
- 每个环境配置必须相互独立，分别读取自己的 `environment`、`appName`、`version`、`description`、`logo`、`url`、`outputDir`、`artifactNameTemplate`，不要把公共配置强行套用到其它环境。
- 用户说“打包本地环境电脑安装包”时，读取 `environments.dev`，同时打包 Windows 安装程序和 macOS 安装程序，并将产物放入 `apps/packageConfig.json` 中该环境配置的 `outputDir` 对应目录。
- 用户说“打包测试环境电脑安装包”时，读取 `environments.test`，同时打包 Windows 安装程序和 macOS 安装程序，并将产物放入该环境配置的 `outputDir` 对应目录。
- 用户说“打包生产环境电脑安装包”时，读取 `environments.prod`，同时打包 Windows 安装程序和 macOS 安装程序，并将产物放入该环境配置的 `outputDir` 对应目录。
- 安装包或启动程序文件名必须按环境配置中的 `artifactNameTemplate` 生成，默认规则为 `{appName}-{environment}-{version}.{extension}`，例如 `零号后台管理系统-本地环境-1.0.0.exe`。
- 每个环境的 `logo` 为相对于 `apps/packageConfig.json` 所在目录的图片路径，例如 `logo-dev.png`、`logo-test.png`、`logo-prod.png`；打包前必须确认文件存在，不存在时停止打包并提示用户补齐。
- Windows 打包时必须将对应环境的 `logo` 转换为标准多尺寸 `.ico`，并用于启动程序文件图标、窗口左上角图标和任务栏图标，不能继续使用默认系统图标。
- Windows 图标验证不能只用 `Icon.ExtractAssociatedIcon` 判断，还必须确认资源管理器可见的 exe 图标已变化；如果资源管理器仍显示旧图标或默认图标，应检查 `.ico` 格式、exe 资源写入、文件名复用导致的 Windows 图标缓存，并通过重新生成不同时间戳/清理同名旧产物/刷新图标缓存等方式修正后再交付。
- Windows 若使用 .NET WebView2 壳，除设置 `<ApplicationIcon>` 外，窗口运行时也必须显式设置 `Form.Icon`，并确保最终输出 exe 的图标资源来自当前环境 `logo`，而不是 SDK 默认图标或旧构建残留。
- macOS 打包时必须将对应环境的 `logo` 转换为 `.icns`，并写入 `.app/Contents/Resources` 和 `Info.plist` 的 `CFBundleIconFile`，让 Finder、Dock、窗口和安装包展示对应环境图标。
- Windows 轻量壳优先使用 WebView2，只负责打开对应环境的 `url`，不要把 `zero-ui`、Spring Boot 后端、数据库或 Redis 打包进桌面程序。
- macOS 轻量壳只负责打开对应环境的 `url`。如果当前机器不是 macOS，无法直接完成 `.dmg`、`.pkg`、签名或公证时，必须明确说明限制，并准备可在 macOS 上执行的打包工程或命令。
- 如果目标环境的 `url` 为空，不要编造地址或继续打包，必须提示用户先补齐对应环境 URL。
- 打包完成后，只保留目标环境输出目录中的最终产物；临时构建目录、下载的临时 SDK、依赖目录和中间文件应清理干净。涉及删除临时目录时，必须先确认路径位于 `apps` 下的本次构建目录中。

## 后端结构规则

- 包路径以 `com.zero` 为根。
- `zero-admin` 公共能力集中在 `common`：注解、常量、基础返回、分页、Redis、工具类、异常、XSS。
- `zero-admin` 框架能力集中在 `framework`：Security、JWT 过滤器、数据源、AOP、全局异常、登录服务。
- `zero-admin` 系统业务集中在 `system`：`domain`、`mapper`、`service`、`service.impl`。
- `zero-admin` Web 入口集中在 `web.controller`：`system`、`monitor`、`common`、`tool`；admin 代理 report 的 Controller 单独放在 `web.reportController`。
- `zero-admin` 代码生成在 `generator`，定时任务在 `quartz`。Mapper 接口放在 `com.zero.**.mapper`，XML 放在 `zero-admin/src/main/resources/mapper/**`，由 `ApplicationConfig` 的 `@MapperScan` 和 `application.yml` 的 `mapperLocations` 扫描。
- `zero-report` 结构以当前代码为准：`controller` 放 HTTP 接口，`service` / `service.impl` 放业务规则，`mapper` 放 MyBatis-Plus Mapper，`domain` 放实体，`queryDto` 放查询参数，`dto` 放服务内 DTO/返回结构，`base` 放分页和 Service 父类，`config` 放 MyBatis-Plus、异常、Feign、XML 加载等配置。
- `zero-report` 的 Mapper 扫描由 `ZeroReportApplication` 上的 `@MapperScan(PackageProperties.MAPPER_SCAN)` 和 `PackageProperties` 统一维护；新增包路径前必须确认 `PackageProperties` 是否需要同步。

## 管理后台业务开发模式规则

新增管理后台菜单或页面前，必须先判断数据和业务归属。判断结果决定后端落位、调用方式、权限边界和前端目录。
当前源码里至少有三类模式：`system/config` 这类 admin 自身 CRUD、`system/apiAuth` 这类 admin 自身但会联动前端请求拦截的配置、`reportSystem/bizDict` 这类 admin 只做代理而真实业务在 report 的功能。开发前必须先归类，不要把三种模式混在一起。

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

- 适用场景：页面入口在管理后台，但真实业务数据和业务规则属于 `zero-report` 或 `zero_biz` 业务库，例如本轮完成的“业务参数设置”维护 `biz_dict_group`、`biz_dict_item`。
- admin 只做统一入口，不直接访问业务库，不注入 report Mapper，不复制 report Service 逻辑，不另起一套 HTTP 工具；必须通过 `com.zero.feign.ReportRestService` 调用 report 服务。
- admin 代理 Controller 必须放在 `zero-admin/src/main/java/com/zero/web/reportController`。Controller 职责只包含：路由映射、`@RequiresPermissions`、`@Log`、参数透传、返回结构声明和 `ReportRestService` 调用。
- `ReportRestService` 使用名为 `reportRestTemplate` 的 `@LoadBalanced RestTemplate` 调用 `http://zero-report`；`ReportRestTemplateConfig` 会通过拦截器追加管理员上下文请求头，不要在每个业务 Controller 里手动拼这些请求头。
- `AdminForwardHeaderProvider` 负责从当前 admin 登录态和请求中提取 `Authorization`、客户端 IP、管理员ID、账号、昵称、公司名称、角色ID、是否超级管理员，并通过 `ForwardHeaderConstants` 统一命名。中文请求头值会 URL 编码，report 侧再解码。
- `ReportRestService` 会校验 report 路径必须以 `/` 开头且不能是完整 URL，避免把代理能力变成任意转发；新增代理接口只能传服务内路径，例如 `/reportSystem/bizDict/group/list`。
- `ReportRestService.exchange` 对 `AjaxResult` 和 `TableDataInfo` 有统一失败兜底：调用 report 异常时返回固定结构和通用错误提示，并只记录路径、方法和异常，不向前端暴露下游堆栈或敏感信息。
- admin 代理 Controller 不需要给每个 `ReportRestService` 调用再包一层 `try/catch`。下游不可达、路径非法、返回空体时由 `ReportRestService` 兜底；report 内部业务异常由 report 的统一异常处理转成固定响应结构。
- `ReportRestService.invokeGetByObj` 只适合查询对象，会用 `ObjectMapper` 转成 Map；单个 ID、字符串、枚举等基础值 GET 参数必须用 `invokeGetByMap` 或 `exchange` 显式指定参数名，避免丢失查询参数名。
- report 服务负责真实业务：Controller、Service、Mapper、实体、查询 DTO、MyBatis-Plus 查询、唯一性校验、软删除/恢复、审计字段、事务和异常处理都放在 `zero-report`。
- report Service 继承 `BaseService` 时，只能使用 `ReportAdminContext` 已提供的上下文获取方法，例如 `getAdminId()`、`getAdminName()`、`getAdminRoleIds()`、`isAdmin()`、`getClientIp()`、`getAuthorization()`、`getSourceService()`；不要为了兼容不存在的字段自行拼请求头。
- report 分页查询参数优先继承 `BaseQuery`，使用 `query.toPage()` 生成 MyBatis-Plus `Page`，不要在每个 Service 实现里重复写最大 `pageSize` 常量。日期范围使用 `hasBeginTime()`、`getBeginDateTime()`、`hasEndTime()`、`getEndExclusiveDateTime()` 组成半开区间，避免手写 `" 00:00:00"`、`" 23:59:59"` 字符串。
- report 列表接口返回 `com.zero.dto.TableDataInfo`，普通详情、新增、修改、删除、恢复、下拉选项等返回 `com.zero.model.AjaxResult`。admin 代理调用时必须声明对应返回类型，列表用 `exchange(..., TableDataInfo.class)`，普通接口用 `AjaxResult`。注意 admin 侧同名类型来自 `com.zero.common.*`，report 侧普通返回来自 `zero-tools` 的 `com.zero.model.AjaxResult`。
- report 服务已有 `ReportExceptionHandler` 统一处理异常：列表接口转成 `TableDataInfo.error`，普通接口转成 `AjaxResult.error`。Service 中不需要给每个 MyBatis-Plus 查询都包一层 `try/catch`；业务可预期错误抛 `IllegalArgumentException` 并给清晰提示，未知异常交给全局异常处理记录日志。
- `ReportRestService` 只允许传 report 内部路径，路径必须以 `/` 开头，不能传完整 URL；GET 查询用 `invokeGetByMap` / `exchange`，POST JSON 用 `invokePostJson`，PUT/DELETE 或需要指定返回类型时用 `exchange`。
- 写操作权限和操作日志放在 admin 代理 Controller 上，例如 `@RequiresPermissions("reportSystem:bizDict:add")`、`@Log(title = "...", businessType = BusinessType.INSERT)`；report Controller 不重复做管理后台按钮权限。
- 查询详情接口如果只用于列表页修改弹窗回显，通常不单独加 `query` 按钮权限，能进入列表并具备修改按钮权限即可完成流程；如果接口会独立展示敏感详情，再单独配置查询权限。
- 页面初始化用的普通下拉列表、枚举选项等接口，如果需求明确“不需要按钮权限”，admin 代理 Controller 不加 `@RequiresPermissions`，但它仍受登录态保护，不等同于匿名公开接口。
- report 侧这类接口如果被直接访问，当前并不会自动拥有 admin 按钮权限语义；安全边界应放在网关、内网访问、admin 代理层和必要的服务间鉴权上，不要把 report 接收到的自定义请求头当成公网可信身份。
- 删除和恢复如果产品要求共用权限，则恢复接口和恢复按钮复用删除权限，例如 `reportSystem:bizDict:remove`；按钮文案可随状态显示为“删除”或“恢复”。
- 新增菜单 SQL 默认只写 `sys_menu` 菜单和按钮权限定义；除非用户明确要求给某个普通角色默认授权，否则不要新增 `sys_role_menu` 普通角色绑定，避免非超级管理员默认看到新菜单。
- admin + report 联动功能的前端文件默认独立归类：页面放 `zero-ui/src/views/reportSystem`，请求文件放 `zero-ui/src/api/reportSystem`；后端 admin 代理路径和前端 API 路径保持一致，例如 `/reportSystem/bizDict/**`。
- 典型参考：`BizDictController` + `ReportRestService` + `zero-report` 的 `DictGroupController`、`DictItemController`、`DictGroupServiceImpl`、`DictItemServiceImpl` + `zero-ui/src/views/reportSystem/bizDict/index.vue` + `zero-ui/src/api/reportSystem/bizDict.js`。

### 菜单权限配置规则

- 菜单自身的 `perms` 就是页面入口和列表权限。例如文件管理菜单配置了 `systemTools:fileManagement:list`，就表示能进入文件管理页面并查询列表，不要再额外新增“文件列表”这类重复按钮权限。
- `sys_menu.menu_type = 'F'` 的按钮权限只配置独立操作，例如 `add`、`edit`、`remove`、`export`、`import`、`resetPwd`、`refreshCache` 等真实按钮或独立动作；不要把菜单已有的 `list` 权限再配置成按钮。
- 列表页内部的普通详情查看、修改前回显、初始化下拉选项等辅助接口，通常不单独配置 `query` 按钮权限；能进入列表且不展示额外敏感信息即可访问。只有独立详情页或会展示敏感信息时，才单独配置 `模块:资源:query`。
- 前端 `v-hasPermi`、admin Controller 的 `@RequiresPermissions`、`sys_menu.perms` 必须一一对应；SQL 中没有配置的按钮权限，前端不要写对应 `v-hasPermi`，后端也不要加对应 `@RequiresPermissions`。
- 删除某个操作能力时必须全链路清理：`sys_menu` 按钮权限、前端按钮、前端 API、admin Controller 接口、Service/DTO 调用入口都要同步检查，避免页面隐藏但接口残留或权限冗余。
- 典型示例：文件管理菜单保留 `systemTools:fileManagement:list` 作为菜单权限；按钮权限只保留上传 `systemTools:fileManagement:add` 和删除 `systemTools:fileManagement:remove`；不再配置文件查询 `query`、文件修改 `edit`、文件列表按钮 `list`。

## zero-report 服务端开发规则

- `zero-report` 当前定位是业务库/report 服务，不是管理后台权限中心。管理后台按钮权限、菜单权限、操作日志放在 `zero-admin` 代理层；report Controller 专注真实业务接口。
- report 接口可以读取 admin 透传的上下文，但这些上下文只应作为内部链路信息使用。直接外部调用 report 时，不应仅凭 `adminId`、`isAdmin` 等自定义请求头做授权判断。
- 启动入口 `ZeroReportApplication` 已统一设置时区 `Asia/Shanghai`、应用 `PackageProperties`、启用服务发现、Feign、Mapper 扫描。新增包路径和 Mapper 前先确认 `PackageProperties`。
- report 的动态数据源以 `zero-report-mysql.yml` 为准，当前 primary 是 `zeroBiz`；`ReportDataSourceConstants.REPORT` 对应 `zeroBiz`，`ReportDataSourceConstants.ADMIN` 对应 `zeroAdmin`。业务表默认走 `zeroBiz`，只有明确需要读取后台基础数据时才切到 `zeroAdmin`。
- 简单 CRUD 和常规分页优先使用 MyBatis-Plus：实体使用 `@TableName`、主键用 `@TableId`、字段必要时用 `@TableField`；Mapper 继承 MyBatis-Plus `BaseMapper`；Service 实现优先继承项目 `BaseService<M,T>`。
- 复杂报表 XML SQL 才使用 PageHelper。不要在同一个查询里同时用 MyBatis-Plus `Page` 和 PageHelper 分页，避免分页叠加。
- 分页查询 DTO 继承 `BaseQuery`，复用 `pageNum`、`pageSize`、`beginTime`、`endTime`、`toPage()`、日期半开区间方法。特殊默认条数或最大条数按 `DictItemQuery` 的方式重写 `defaultPageSize()`、`maxPageSize()`。
- MyBatis-Plus 全局分页兜底在 `MybatisPlusConfig`，当前最大单页限制是 10000；业务查询还应通过 `BaseQuery` 设定更小的业务上限，例如普通列表默认最大 500、展开明细可单独设 1000。
- 查询条件使用 `LambdaQueryWrapper` / `lambdaQuery()` / `lambdaUpdate()`，字符串条件先 `StringUtils.hasText` 再 trim，避免空字符串进入 SQL。排序要稳定，至少包含业务排序字段和主键兜底。
- 业务可预期失败使用 `IllegalArgumentException` 返回明确提示，例如编码重复、必填为空、记录不存在、父级已删除。不要在每个 Service 查询、更新、删除方法里重复 `try/catch`；数据库和未知异常交给 `ReportExceptionHandler`。
- `ReportExceptionHandler` 会根据 Controller 方法返回类型或 `/list` 路径返回 `TableDataInfo.error` 或 `AjaxResult.error`。新增列表接口如果返回结构不是 `TableDataInfo`，要确认异常结构是否仍符合前端预期。
- `BaseService` 只按 `ReportAdminContext` 提供当前已有上下文方法：管理员ID、账号、昵称、公司名称、角色ID、是否管理员、客户端IP、Authorization、来源服务。需要新增上下文字段时，应先扩展 admin 侧常量、透传、report 解析和 BaseService 方法，不能在业务里私自读魔法请求头。
- `ReportAdminContext` 会解码中文请求头、解析角色ID、只接受 `true` 或 `1` 表示管理员。业务上不要把空请求头当成超级管理员，也不要因为本地测试缺请求头就放宽权限。
- report 继续调用其它 Java 服务时使用 OpenFeign，并依赖 `FeignRequestInterceptorConfig` 继续透传操作人上下文；不要手写新的 Feign 拦截器或散落的请求头拼接。
- 写入审计字段时，创建人/更新人优先使用 `getAdminName()`；创建/更新时间使用服务端时间。日志中不要输出 Authorization、密码、验证码、数据库密码、密钥等敏感信息。
- 软删除字段按业务表语义处理。列表是否展示已删除记录要按页面需求明确；详情、修改、恢复等接口要分别判断是否只允许未删除记录。
- 如果页面新增“删除标志”查询项，必须同步检查前端 `queryParams`、API 参数、admin 代理注释、report Query DTO 和 Service Wrapper 是否全链路支持。只在页面显示 `delFlag` 不等于已经支持按 `delFlag` 查询。
- 前端去掉输入框 `maxlength`、`show-word-limit` 或计数器，只表示页面不再展示长度限制；后端实体 `@Size`、数据库字段长度和唯一索引仍是最终约束，不能在规范中误写成字段无限长。
- 使用 Lombok 是当前 report 模块已有做法，实体、DTO、查询对象可优先用 `@Getter`、`@Setter`、`@ToString`，但仍要补齐字段注释和校验注解。

## common 包归类规则

- `com.zero.common` 只放跨模块复用的基础能力，不放具体业务流程。
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
- `zero-admin` 匿名路径在 `SecurityConfig` 和 `@Anonymous` / `PermitAllUrlProperties` 相关逻辑中体现，新增匿名接口必须谨慎确认。
- `zero-admin` 登录验证码由 `/captchaImage`、`SysLoginService.validateCaptcha`、`ISysConfigService.selectCaptchaEnabled()` 和配置项 `sys.account.captchaEnabled` 控制。
- `zero-admin` 当前 XSS 配置在 `application.yml` 的 `xss` 节，匹配 `/system/*,/monitor/*,/tool/*`，不要认为所有 JSON 请求都天然安全。
- 数据权限通过 `@DataScope` 和 SQL 中 `${params.dataScope}` 拼接实现。涉及用户、角色、部门数据时必须先确认数据范围规则。
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

- 前端是 Vue2 Options API 项目，不使用 Vue3、Pinia、Vite、Composition API，除非用户明确要求大改并确认风险。
- API 请求统一放在 `src/api/**`，通过 `@/utils/request` 发起。
- 页面放在 `src/views/**`，布局在 `src/layout/**`，公共组件在 `src/components/**`。
- 路由分两类：`router/index.js` 中的静态/动态路由，以及后端 `getRouters` 返回的菜单路由。后端菜单的 `component` 会按 `@/views/${view}` 动态加载，例如 report 页面组件应写成 `reportSystem/bizDict/index`。
- 登录后路由守卫在 `permission.js` 中先调用 `GetInfo`，再调用 `getGoogleAuthApis()` 刷新接口谷歌验证缓存，最后 `GenerateRoutes` 生成菜单路由。不要在业务页面里重复做这一套全局初始化。
- `main.js` 已全局注册 `Pagination`、`RightToolbar`、`DictTag`、上传、富文本、图片预览等组件，并挂载 `parseTime`、`resetForm`、`addDateRange`、`download` 等方法；页面优先复用这些能力。
- 权限按钮使用 `v-hasPermi` / `v-hasRole`，不要手写分散权限判断。
- 字典使用 `dicts: [...]`、`dict-tag` 和项目字典组件，不重复硬编码字典标签。

## 前端请求与页面规范

- 请求成功判断以 `request.js` 拦截器为准：后端 `code === 200` 才作为成功返回；`401` 触发重新登录，`500` 弹错误消息，接口谷歌验证错误码按全局逻辑处理。
- `request.js` 会自动追加 `Authorization: Bearer <token>`，GET 参数会序列化到 URL，POST/PUT 默认做 5MB 内的一秒防重复提交。业务 API 文件不要重复实现这些逻辑。
- 接口谷歌验证由 `request.js` 根据 localStorage 中的 `google_auth_apis` 判断，支持精确路径、`{param}`、`*`、`**` 和请求方法匹配；命中后统一弹 6 位验证码输入框并添加 `X-Google-Code` 请求头。
- 列表页沿用若依页面结构：`queryParams`、`dateRange`、`loading`、`total`、`getList()`、`handleQuery()`、`resetQuery()`、`pagination`。
- 查询接口用 `GET + params`，新增用 `POST + data`，修改用 `PUT + data`，删除用 `DELETE /ids`。
- 导出使用 `this.download(...)` 或现有下载封装，不自行拼接 blob 逻辑。
- 表单弹窗使用 `el-dialog` + `el-form` + `rules` + `this.$refs["form"].validate`，提交后刷新列表并关闭弹窗。
- UI 改动要遵循 Element UI 和当前后台风格，避免引入新的 UI 库或重写整体布局。
- 该项目已有主题、菜单、Tab 等大量样式改动，修改样式前必须先读相关 SCSS、组件和现有说明文档，避免破坏多主题适配。

## 管理后台 UI 开发沉淀规则

- 先按业务归属确定前端目录：admin 自身业务继续放 `views/system`、`views/monitor`、`views/tool` 等既有目录；admin 调 report 的业务统一放 `zero-ui/src/views/reportSystem`，API 文件统一放 `zero-ui/src/api/reportSystem`。
- 页面结构优先沿用若依列表页：顶部查询表单、操作按钮行、`right-toolbar`、`el-table`、`pagination`、新增/修改 `el-dialog`。不要为了单个页面重做一套布局。
- API 文件只封装请求，不写业务判断；命名按动作拆清楚，例如 `listDictGroup`、`listDictGroupOptions`、`getDictGroup`、`addDictGroup`、`updateDictGroup`、`delDictGroup`、`restoreDictGroup`。
- admin 自身页面 API 走 `/system/**`、`/monitor/**` 等现有路径；admin 代理 report 的页面 API 走 `/reportSystem/**`，前端不要直接请求 `zero-report` 端口或服务名。
- 查询条件里需要下拉选项时，先判断数据来源是否独立于分页列表。如果是字典组编码这类全量选项，应新增独立 options 接口，进入页面 `created()` 时加载，用 Element UI 的 `filterable` 做本地过滤；不要临时用当前页表格数据拼下拉，也不要无必要地做远程搜索。
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
- 通用页面样式优先使用 Element UI 既有结构、项目已有 CSS 变量或主题类作用域；如果必须写颜色、背景、边框、阴影或透明度，要么使用能兼容明暗主题的中性色和透明度，要么分别在 `.theme-luxury-business`、`.theme-glassmorphism`、`.theme-vibrant-tech`、`.theme-minimalist-future`、`.theme-warm-nature`、`.theme-material-design`、`.theme-dark-premium` 和默认样式下做适配。
- 修复某个主题下的视觉问题时，也要检查其它主题是否会出现白底、黑字看不清、透明层叠、固定列背景错位、hover/active 状态异常、弹层被遮挡、边框割裂、按钮不可见等问题。
- 新增或调整业务页面时，页面本身不要强绑定某一种主题色；业务页面只负责结构和局部必要样式，整体背景、菜单、顶部栏、Tabs、分页、弹窗和表格基础观感尽量交给全局主题样式控制。
- 主题切换问题不能只看刷新后的效果。切换时要确认 `App.vue` 会清理 `body` 上全部旧主题类、毛玻璃背景类和运行时样式，`layout/index.vue` 的主题类也要同步，`AppMain.vue` 的缓存 key 要包含主题值，避免 keep-alive 页面沿用旧主题渲染结果。
- Element UI 的弹层、下拉框、消息框、日期选择器等经常挂在 `body` 下，业务页的 `scoped` 样式管不到；这类样式必须写在全局样式或非 scoped 块里，并按主题类限定作用域。
- 表格展开行、固定列、空数据行、hover 行、选中行和右侧操作列是多主题最容易出问题的位置。修这些位置时要同时处理 header、body、fixed wrapper、expanded cell、border、阴影和 hover 状态；Element UI 固定列会渲染镜像表，已有通用修复在 `table-fixed-column.scss`，局部页面不要随意覆盖成单一背景色。
- 操作列确实需要固定时，优先复用 `table-fixed-column.scss` 的 `--zero-fixed-table-*` 变量；页面局部只能补必要结构样式，不能把固定列背景硬写成某一个主题的颜色。
- 毛玻璃主题有背景渐变和透明叠层，切出毛玻璃时必须清理 `glass-bg-*` 类、`--glassmorphism-bg-gradient` 变量和运行时样式，否则会出现“刷新后正常、切换后残留”的问题。
- 能验证前端时，至少运行 `npm run build:prod`；涉及明显视觉风险的改动，要优先用浏览器在多个代表主题下查看：`glassmorphism`、`dark-premium`、`minimalist-future`、`default`，必要时再补查其它主题。

## 编码风格

- 保持当前文件风格，精准局部改动，不做无关重构。
- Java 代码使用现有工具类：`StringUtils`、`SecurityUtils`、`DateUtils`、`ExcelUtil`、`RedisCache` 等。
- 新增或修改 Java 代码时，必须同步维护注释；注释要贴合项目现有风格，说明业务职责、参数含义、返回语义、边界和安全原因，避免空话、过期注释和与代码不一致的注释。
- 前端保持 Vue2 写法、单引号/双引号风格按当前文件就近一致，不批量格式化。
- 中文文件按 UTF-8 读取和写入；如果发现乱码，必须重新确认编码后再编辑。
- 不新增全局依赖、不升级框架、不改构建配置，除非需求必须且已说明影响范围。

## Java 注释规范

适用于 `zero-admin`、`zero-report` 以及当前仓库新增的所有 Java 源码。新增功能、修改逻辑、补充字段或调整接口时，必须同步补充和维护相关注释。

### 基本要求

- 类、interface、enum 必须有类头 JavaDoc，说明职责、业务范围、对应表或接口用途；类名不能完整表达业务时，必须在说明中补足上下文。
- Controller 类注释需说明接口职责；每个对外映射方法必须有方法 JavaDoc，写清用途、关键参数和返回结果。
- Service、Mapper、工具类的公开方法必须有方法 JavaDoc；重要的 `private` 辅助方法也要注释，说明输入来源、输出格式、异常兜底或副作用。
- 实体、DTO、请求参数对象的字段上方使用 `/** ... */` 说明业务含义、单位、枚举取值、默认值或脱敏规则；不为简单 getter/setter 机械重复注释，除非当前文件已有统一模板。
- 静态常量、配置 key、Redis key、URL、路径、权限标识、业务状态码、开关字段必须在字段上方说明含义、取值范围、存储格式或业务用途。
- 涉及登录、权限、数据权限、JWT、Redis TTL、验证码、谷歌动态码、密钥、操作日志、XSS、事务、定时任务、文件上传、导入导出等特殊逻辑时，必须在就近位置注释说明为什么这样处理。
- 修改既有代码时，如果行为发生变化，必须同步更新原注释，避免注释仍描述旧逻辑。

### 注释内容写法

- 注释写业务含义，不写代码表面动作。例如写“生成令牌前校验谷歌动态码，避免未通过二次验证的用户拿到 token”，不要写“调用 validateGoogleCode 方法”。
- 方法 `@param` 要写清业务含义、是否可选、默认值、上限、单位或枚举值，例如 `pageSize 每页条数，默认10`、`googleCode 谷歌动态码，6位数字`。
- 方法 `@return` 要写清返回结构和兜底语义，例如 `绑定记录列表，googleKey 已脱敏`、`true-校验通过，false-校验失败`；`void` 方法要说明真实副作用。
- 对接口方法，注释中要体现登录要求、权限要求、数据权限、返回 DTO、分页口径、敏感字段脱敏或不落日志等前端和安全侧关心的规则。
- 对 MyBatis XML 对应的 Mapper 方法，注释需说明查询条件、返回对象和是否受数据权限影响；新增 `${}` 拼接时必须在代码或 XML 附近说明安全来源。
- 行内注释只写业务原因、边界保护、兼容老数据、安全控制或性能考虑，不写“给变量赋值”“循环列表”这类显而易见的内容。
- 注释中允许保留表名、字段名、DTO、Redis key、URL、API、SQL、token、data 等技术词；中文说明要让业务读者能理解。
- 不保留空的 `说明:`、错误的返回值类型、与实现不一致的默认值、过期作者时间、重复模板或无意义生成注释。

### 作者、时间与风格

- 维护旧文件时优先贴合当前文件已有注释风格；文件已有作者、时间模板的，新增同类方法可延续该模板。
- 新增类如果周边文件使用作者、时间模板，可使用当前项目已有署名风格；时间必须使用实施当时本机时间，格式与当前文件保持一致，不得臆测或编造。
- 新增类如果周边文件没有作者、时间模板，则使用简洁 JavaDoc，不强行添加作者和时间。
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

### 示例

```java
/**
 * 用户谷歌动态码绑定记录
 *
 * 对应数据库表：sys_google
 */
public class SysGoogle extends BaseEntity {
}
```

```java
/**
 * 校验登录谷歌动态码
 *
 * @param userId 用户ID
 * @param userName 用户名，用于绑定提示和日志上下文
 * @param googleCode 用户输入的6位谷歌动态码
 */
public void validateLoginCode(Long userId, String userName, String googleCode) {
}
```

```java
/**
 * 谷歌动态码登录开关配置 key，true-开启，false或未配置-关闭
 */
public static final String OPEN_GOOGLE_AUTH = "sys.account.openGoogleAuth";
```

## 源码逆向验证 50 轮记录

以下记录用于说明本文件规则已经按当前源码反推校验。每轮只保留可复用结论，重复或不稳定结论已合并到正文，不作为独立规则扩散。

1. 第01轮：父工程 `pom.xml` 反推后端基线，确认 Java 17、Spring Boot 3.5.12、多模块结构为当前事实。
2. 第02轮：`ZeroAdminApplication` 反推 admin 启动模式，确认启用服务发现、Feign、LoadBalancer，并排除默认数据源自动配置。
3. 第03轮：`ZeroReportApplication` 反推 report 启动模式，确认 Mapper 扫描和包扫描由 `PackageProperties` 集中维护。
4. 第04轮：`zero-tools` 依赖关系反推公共包边界，确认 report 依赖 tools，不能把 admin 的 `common` 当成跨服务公共包随意搬用。
5. 第05轮：SQL 文件和 report 数据源配置反推库边界，确认 `zero_admin` 管管理后台，`zero_biz` 管业务表。
6. 第06轮：`SysConfigController` 反推 admin 自身分页 CRUD 规范，确认 `BaseController`、`startPage`、`getDataTable`、`AjaxResult` 是主模式。
7. 第07轮：`SysMenuController` 反推树形接口例外，确认不是所有 admin 查询都返回 `TableDataInfo`。
8. 第08轮：`SysApiAuthController`、`permission.js`、`request.js` 联动反推全局配置风险，确认改 admin 配置接口必须同步看前端拦截器。
9. 第09轮：`BizDictController` 反推 admin 代理 report 的落位，确认代理 Controller 固定放 `web.reportController`。
10. 第10轮：`ReportRestTemplateConfig` 反推上下文透传入口，确认业务 Controller 不需要手动拼管理员请求头。
11. 第11轮：`ReportRestService.buildUri` 反推转发安全边界，确认只允许服务内路径，禁止完整 URL。
12. 第12轮：`ReportRestService.invokeGetByObj` 反推 GET 参数规则，确认基础值必须用 Map 或 `exchange` 指定参数名。
13. 第13轮：`ReportRestService.exchange` 反推代理异常策略，确认 admin 代理层不需要重复包裹通用 `try/catch`。
14. 第14轮：`AdminForwardHeaderProvider` 反推 admin 上下文来源，确认管理员ID、账号、公司、角色、客户端IP来自登录态和当前请求。
15. 第15轮：`AdminForwardContext` 和 `ReportAdminContext` 反推中文请求头处理，确认中文值要编码/解码，不能散落手写。
16. 第16轮：`ReportAdminContext.parseAdmin` 反推管理员标识语义，确认只有 `true` 或 `1` 才是管理员。
17. 第17轮：`FeignRequestInterceptorConfig` 反推 report 继续调用下游的规范，确认使用统一 Feign 拦截器透传上下文。
18. 第18轮：服务名常量反推注册中心命名，确认 admin 服务名是 `zeroAdmin`，report 服务名是 `zero-report`。
19. 第19轮：`ReportDataSourceConstants` 和 `zero-report-mysql.yml` 反推数据源命名，确认 `zeroBiz`、`zeroAdmin` 是精确 key。
20. 第20轮：`BaseQuery` 反推 report 分页父类规则，确认普通列表默认 10、默认最大 500，子类可覆盖。
21. 第21轮：`DictItemQuery` 反推特殊分页场景，确认展开明细默认 500、最大 1000 是子类覆盖方式。
22. 第22轮：`BaseQuery` 日期方法反推时间范围规则，确认用开始当天零点和结束次日零点组成半开区间。
23. 第23轮：`MybatisPlusConfig` 反推分页兜底边界，确认全局 10000 只是最后保护，业务仍应有更小上限。
24. 第24轮：`ReportExceptionHandler` 反推异常返回结构，确认列表失败走 `TableDataInfo.error`，普通失败走 `AjaxResult.error`。
25. 第25轮：report Service 实现反推异常写法，确认可预期业务失败抛 `IllegalArgumentException`，不要每个 SQL 手写日志捕获。
26. 第26轮：`DictGroup`、`DictItem` 实体反推字段约束，确认前端去掉计数器不代表后端字段无限长。
27. 第27轮：report 实体和 Query DTO 反推 Lombok 规则，确认可优先用 Lombok，但字段注释和校验不能省。
28. 第28轮：`DictGroupServiceImpl.selectGroupPage` 反推软删除列表规则，确认字典组分页默认展示未删除和已删除，并支持 `delFlag` 查询。
29. 第29轮：`DictGroupServiceImpl.selectGroupOptions` 反推下拉规则，确认字典组编码下拉全量返回，不按状态和删除标志过滤。
30. 第30轮：`DictItemServiceImpl.selectItemPage` 和 `DictItemQuery` 反推子表查询边界，确认当前字典项展示删除标志但没有独立 `delFlag` 查询条件。
31. 第31轮：`selectGroupById`、`selectItemById` 反推详情规则，确认修改回显只查未删除数据。
32. 第32轮：字典组删除和恢复实现反推父表规则，确认删除字典组不再强制先删子项，恢复后再允许维护。
33. 第33轮：字典项恢复实现反推父子联动，确认子项恢复前必须先恢复所属字典组。
34. 第34轮：字典唯一性校验反推编码规则，确认删除记录也占用字典组编码、字典项编码和值。
35. 第35轮：`zero-ui` 技术栈反推前端约束，确认 Vue2 Options API、Vue Router 3、Vuex 3、Element UI 2 是当前基线。
36. 第36轮：`main.js` 反推全局能力，确认分页、右侧工具栏、字典标签、上传、下载、日期、表单重置等应优先复用。
37. 第37轮：`request.js` 反推请求规范，确认 Token、GET 序列化、重复提交、谷歌验证码由全局封装处理。
38. 第38轮：`permission.js` 反推登录后初始化，确认先取用户信息、刷新谷歌验证接口缓存，再生成动态路由。
39. 第39轮：`store/modules/permission.js` 反推菜单组件路径，确认后端 `component` 会解析到 `@/views/${view}`。
40. 第40轮：`views/reportSystem/bizDict` 反推 report 前端目录，确认 report 联动页面与 admin 自身页面分目录维护。
41. 第41轮：`api/reportSystem/bizDict.js` 反推 API 文件职责，确认 API 只封装请求，不写页面业务判断。
42. 第42轮：业务字典页面反推返回读取规则，确认分页读 `rows/total`，普通接口读 `data`。
43. 第43轮：业务字典页面下拉实现反推查询选项规则，确认进入页面加载 options，使用 `filterable` 本地过滤。
44. 第44轮：业务字典页面表格实现反推批量操作规则，确认无批量操作时不保留 selection 全选列。
45. 第45轮：业务字典页面操作列反推固定列策略，确认外层能不用 fixed，内层字段多时可 fixed 但要适配主题。
46. 第46轮：`Settings/index.vue` 反推主题枚举，确认正式可选主题是 7 个增强主题加 `default`。
47. 第47轮：`App.vue` 反推主题切换清理链路，确认主题类在 `body` 和 `.app-wrapper` 上都要同步清理/应用。
48. 第48轮：`AppMain.vue` 反推刷新后正常、切换后异常的原因，确认主题值进入路由 key 用于重建 keep-alive 页面。
49. 第49轮：`table-fixed-column.scss` 反推表格固定列主题规则，确认 Element UI 镜像表要统一处理表头、行、空数据、阴影。
50. 第50轮：验证命令和敏感配置反推交付规则，确认能验证时跑最小编译/构建，配置文件含敏感信息时回复不得复述明文。

## 修改前检查清单

1. 先读目标文件、同类 Controller/Service/Mapper 或 Vue 页面/API 文件。
2. 确认接口返回类型是 `AjaxResult`、`TableDataInfo` 还是文件流。
3. 确认权限标识、菜单路由、前端 `v-hasPermi` 是否匹配。
4. 确认是否涉及登录态、JWT、Redis 缓存、数据权限、事务、导入导出或上传。
5. 确认是否有用户未提交改动，不能覆盖。
6. 能验证时运行最小范围编译或前端构建；不能验证时说明原因。

## 常用验证命令

- admin 编译：`mvn -pl zero-admin -am compile -DskipTests`
- report 编译：`mvn -pl zero-report -am compile -DskipTests`
- admin 运行：`mvn -pl zero-admin spring-boot:run`
- report 运行：`mvn -pl zero-report spring-boot:run`
- 前端开发：在 `zero-ui` 执行 `npm run dev`
- 前端生产构建：在 `zero-ui` 执行 `npm run build:prod`

## 当前环境提示

- 项目后端需要 Java 17；如果本机 Maven 使用 JDK 8，会出现 `无效的标记: --release` 等编译失败。
- `application-dev.yml` 含数据库连接信息，提交或回复时不要泄露明文密码。
