# zero-admin 模块规则

本文件适用于 `zero-admin/`。同时遵守[仓库全局规则](../AGENTS.md)，通用 Java/YAML 注释规范及安全要求以根文件为准。下文带模块名前缀的代码路径相对仓库根目录，其余模块内部路径相对本目录；Markdown 链接相对本文件。跨模块修改同时读取相关模块规则。

## 技术栈与版本来源

- admin 登录令牌使用 `io.jsonwebtoken:jjwt-*`，版本由父 POM 管理；与 tools 的 `com.auth0:java-jwt` 分别核对调用方，公共能力抽取遵守[根文件边界](../AGENTS.md#公共能力边界)。
- `zero-admin` 是管理后台服务，主入口为 `com.zero.ZeroAdminApplication`，启用 Nacos 注册/配置、OpenFeign、LoadBalancer，并排除默认数据源自动配置。主要技术包括 Spring Security、JWT、MyBatis XML、PageHelper、Druid、Redis、Quartz、Actuator、SpringDoc、Kaptcha、Fastjson2、POI。
- 系统接口文档使用 SpringDoc 2.8.17，与当前 Boot 3.5 / Spring 6.2 配套；文档子路径 `/v3/api-docs/**` 仅额外放行 GET，保证 Swagger UI 可读取配置和文档，不扩大业务接口的匿名访问。
- Swagger3Config声明HTTP Bearer/JWT方案；后台Axios登录请求头不会自动继承到Swagger，调试受保护接口须在Authorize输入原始Token，不带Bearer前缀。不要把Token写入文档、日志、URL或测试产物。

## 后端结构规则

- `zero-admin` 公共能力集中在 `common`：注解、常量、基础返回、分页、Redis、工具类、异常、XSS。
- `zero-admin` 框架能力集中在 `framework`：Security、JWT 过滤器、数据源、AOP、全局异常、登录服务。
- `zero-admin` 系统业务集中在 `system`：`domain`、`mapper`、`service`、`service.impl`。
- `zero-admin` Web 入口集中在 `web.controller`：`system`、`monitor`、`common`、`tool`；admin 代理 report 的 Controller 单独放在 `web.reportController`。
- 新增或调整后台 Controller 时必须标注 `@SystemModule(SystemModuleEnum.X)`，模块取 `file`、`system`、`user`、`report`、`monitor`、`tools`、`auth`、`other`；方法级声明覆盖类级。未标注时运行统计按路由首段归类，启动日志会输出 `StatisticsModuleVerifier` 告警。
- `zero-admin` 代码生成在 `generator`，定时任务在 `quartz`。Mapper 接口放在 `com.zero.**.mapper`，XML 放在 `zero-admin/src/main/resources/mapper/**`，由 `ApplicationConfig` 的 `@MapperScan` 和 `application.yml` 的 `mapperLocations` 扫描。
- admin 页面使用的代码生成模板与新项目使用的 MyBatis-Plus 开发期生成器各有入口。admin 的 13 个 classpath 模板位于`zero-admin/src/main/resources/vm/`，原件及来源在[模板来源](../前段项目升级方案/若依官方代码生成模板-v3.9.0/)，验证见[生成模板验证报告](../前段项目升级方案/04-代码生成模板集成与验证报告.md)。新增导入表和历史空前端类型默认Vue3，显式Vue2保留；Vue3模板沿用Options API及项目公共组件，Controller输出到`web/controller/模块`。修改后先运行`GeneratorTemplatesTest`验证六类渲染、ZIP及生成Java/XML，再运行读取其产物的前端用例，不以项目自身编译代替生成结果验证；不自动执行生成SQL或开启源码覆盖。

## 本地配置生效边界

- admin 的 `localYml` 导入在主配置中当前被注释，不把该目录的数据库、Redis、上传限制当作生效配置。分析配置问题要同时检查本地文件、Nacos 和启动覆盖参数。

## 模式一：admin 自身业务

- 后端按若依既有结构开发：Controller 放在 `zero-admin/src/main/java/com/zero/web/controller` 对应子包，Service 放在 `system/service` 或同类模块，Mapper 接口放在 `system/mapper`，XML 放在 `zero-admin/src/main/resources/mapper/**`。
- 不是所有 admin 自身列表都返回 `TableDataInfo`：树形菜单类接口参考 `SysMenuController.list`，返回 `AjaxResult.success(list)`；分页表格类接口参考 `SysConfigController.list`、`SysApiAuthController.list`。
- Service 层负责业务规则、唯一性校验、数据权限、事务和缓存维护；Controller 只做参数接收、权限注解、操作日志、调用 Service 和返回封装。
- 当前登录人、审计字段和权限上下文使用 admin 现有能力，例如 `getUsername()`、`SecurityUtils`、`@DataScope`、Redis 缓存等，不要绕到 report 服务。
- 页面辅助接口按现有风格处理：例如 `SysMenuController.treeselect`、`SysApiAuthController.googleAuthApis` 不配置按钮权限，但仍走登录态和全局安全链路；不能把“无按钮权限”误理解为匿名公开。
- 涉及前端全局行为的 admin 配置接口要同步看前端调用方。例如 `SysApiAuthController.googleAuthApis` 被 `zero-ui/src/permission.js` 登录后刷新缓存，`zero-ui/src/utils/request.js` 会按缓存规则触发谷歌验证码弹窗。
- 典型参考：`SysConfigController` + `ISysConfigService` + `SysConfigMapper.xml` + `zero-ui/src/views/system/config/index.vue` + `zero-ui/src/api/system/config.js`；接口谷歌验证参考 `SysApiAuthController` + `zero-ui/src/views/system/apiAuth/index.vue`。

## 模式二：admin 调 report 服务业务

- admin 对 report 业务只做统一入口，使用 `com.zero.feign.ReportRestService` 代理业务请求。业务库访问、Mapper 和 Service 逻辑由 report 维护。现有 `ReportHealthController` 使用独立的 Feign 探活链路。
- admin 代理 Controller 必须放在 `zero-admin/src/main/java/com/zero/web/reportController`。Controller 职责只包含：路由映射、`@RequiresPermissions`、`@Log`、参数透传、返回结构声明和 `ReportRestService` 调用。
- `ReportRestService` 使用名为 `reportRestTemplate` 的 `@LoadBalanced RestTemplate` 调用 `http://zero-report`；`ReportRestTemplateConfig` 会通过拦截器追加管理员上下文请求头，不要在每个业务 Controller 里手动拼这些请求头。
- `AdminForwardHeaderProvider` 负责从当前 admin 登录态和请求中提取 `Authorization`、客户端 IP、管理员ID、账号、昵称、公司名称、角色ID、是否超级管理员，并通过 `ForwardHeaderConstants` 统一命名。中文请求头值会 URL 编码，report 侧再解码。
- `ReportRestService` 会校验 report 路径必须以 `/` 开头且不能是完整 URL，避免把代理能力变成任意转发；新增代理接口只能传服务内路径，例如 `/reportSystem/bizDict/group/list`。
- `ReportRestService.exchange` 对 `AjaxResult` 和 `TableDataInfo` 有统一失败兜底：调用 report 异常时返回固定结构和通用错误提示，并只记录路径、方法和异常，不向前端暴露下游堆栈或敏感信息。
- admin 代理 Controller 不需要给每个 `ReportRestService` 调用再包一层 `try/catch`。下游不可达、路径非法、返回空体时由 `ReportRestService` 兜底；report 内部业务异常由 report 的统一异常处理转成固定响应结构。
- `ReportRestService.invokeGetByObj` 只适合查询对象，会用 `ObjectMapper` 转成 Map；单个 ID、字符串、枚举等基础值 GET 参数必须用 `invokeGetByMap` 或 `exchange` 显式指定参数名，避免丢失查询参数名。
- 按前述内部路径规则传参。`ReportRestService` 的 GET 查询用 `invokeGetByMap` / `exchange`，POST JSON 用 `invokePostJson`，PUT/DELETE 或需要指定返回类型时用 `exchange`。
- 页面初始化用的普通下拉列表、枚举选项等接口，如果需求明确“不需要按钮权限”，admin 代理 Controller 不加 `@RequiresPermissions`，但它仍受登录态保护，不等同于匿名公开接口。
- report 侧这类接口如果被直接访问，当前并不会自动拥有 admin 按钮权限语义；安全边界应放在网关、内网访问、admin 代理层和必要的服务间鉴权上，不要把 report 接收到的自定义请求头当成公网可信身份。
- 删除和恢复如果产品要求共用权限，则恢复接口和恢复按钮复用删除权限，例如 `reportSystem:bizDict:remove`；按钮文案可随状态显示为“删除”或“恢复”。
- 典型参考：`BizDictController` + `ReportRestService` + `zero-report` 的 `DictGroupController`、`DictItemController`、`DictGroupServiceImpl`、`DictItemServiceImpl` + `zero-ui/src/views/reportSystem/bizDict/index.vue` + `zero-ui/src/api/reportSystem/bizDict.js`。

## 模式三：admin 调文件服务

- admin 多 ID 删除逐个调用文件服务；存储清理失败时继续处理，数据库删除失败等接口错误会中止后续操作，已提交的删除不会回滚。单文件提交顺序和 `data=false` 的成功告警语义见 [file 删除规则](../zero-file/AGENTS.md#模式三admin-调文件服务)。
- 文件调用统一复用 `FileRestService` 和名为 `fileRestTemplate` 的 `@LoadBalanced RestTemplate`，目标为 `http://zero-file`；管理员上下文仍由 `AdminForwardHeaderProvider` 拦截追加。multipart 上传复用 `uploadFile/uploadFiles`，不要改成 JSON 或额外手写 HTTP 客户端。
- `sys.file.storage.mode` 集中定义在 `IAdminFileStorageModeService`；当前只有 `local`（忽略大小写并 trim）走 admin 本地上传，其它值和空配置都走 `zero-file`。通用单/多文件上传和头像上传读取该配置；文件管理上传始终走文件服务，不受此开关切回本地。
- 上传流程复用 `AdminFileUploadServiceImpl`，保持扩展名、文件大小、原始文件名校验和历史响应字段。当前业务类型分别为 `admin-common`、`admin-avatar`、`file-management`，不能把不同来源文件混成同一种类型。
- 上传限制同时受 admin、file、反向代理及前端校验影响。文件管理页面读取 admin 的 `/upload/config`，不能只修改页面默认提示。file 本地单文件/整请求默认 30MB/300MB，admin Nacos common 源文件为 30MB/60MB；实际限制按运行配置核对。

## common 包归类规则

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

## 后端接口规范

- `zero-admin` Controller 默认继承 `BaseController`；分页列表使用 `startPage()` + Service 查询 + `getDataTable(list)`，返回 `TableDataInfo`，字段为 `code`、`msg`、`rows`、`total`。普通增删改查沿用根文件的 `AjaxResult` 及 `success()`、`error()`、`toAjax(rows)` 约定。
- 管理后台权限使用项目自定义 `@RequiresPermissions("模块:资源:动作")`，例如 report 业务新增使用 `@RequiresPermissions("reportSystem:bizDict:add")`；不要替换成 Shiro 或其它权限注解。admin 代理 report 的权限仍放在 admin 代理 Controller 上，report Controller 不重复配置后台按钮权限。
- 操作日志使用 `@Log(title = "...", businessType = BusinessType.XXX)`，写操作、导入导出、清缓存等按同类 Controller 补齐，新增操作使用 `BusinessType.INSERT`；admin 代理 report 时，操作日志记录在 admin 代理 Controller。
- 登录、验证码、Token、权限、路由相关逻辑不要随意改动；必须先读取 `SecurityConfig`、`JwtAuthenticationTokenFilter`、`SysLoginService`、`TokenService`、`SysLoginController`。

## 后端安全与权限

- `zero-admin` Spring Security 是无状态 JWT 模式，Token 请求头为 `Authorization: Bearer <token>`。
- 在线用户列表按用户明确授权，向具备`monitor:online:list`菜单权限的所有账号返回每条会话的完整`authorizationToken`（含`Bearer `前缀），不遮挡、不另加超级管理员限制。该菜单权限包含读取其它账号会话凭证的能力；列表响应禁止缓存，不记录令牌日志，不写入数据库，不刷新被查看会话有效期。页面完整显示会话编号，并保留底部横向滚动条。
- `zero-admin` 匿名路径在 `SecurityConfig` 和 `@Anonymous` / `PermitAllUrlProperties` 相关逻辑中体现，新增匿名接口必须谨慎确认。
- 管理后台 WebSocket 为精确 `GET /ws/admin` 握手入口，首帧仅发送含token字符串的JSON，JWT认证成功后返回CREATE_CONNECTION才接收业务事件；认证失败或协议错误直接关闭，不发送错误类型；握手、心跳和会话检查不续期登录。频道必须显式配置，缺失时拒绝启动；消息统一经环境隔离的 Redis Pub/Sub 分发，调用 `AdminWebSocketService` 按用户ID发送，不提供公网通用推送接口。前端从现有 API 地址自动生成 ws/wss 地址；代理须保留 Host 并转发 Upgrade。同账号多会话、多标签页接收，单登录会话连接上限由 Redis 租约跨节点控制。在线消息不落库、不补发，部署和验证边界见 [WebSocket 集成与部署](../docs/WebSocket集成与部署.md)。
- WebSocket认证、心跳、错误及业务消息统一使用 `common.enums.webSocket.WebSocketMessageType`；消息对象 `WebSocketMessageVo` 包含 `id/sendTime/senderIp/fromAdminId/toAdminId/msgType/msgData/forwardUrl`，不恢复版本号。枚举以对象输出事件编号、中文说明、展示形式displayType及级别messageLevel；前端使用 `websocketMessageType.js` 对应编号订阅。业务发送入口只允许业务事件，禁止伪造控制消息；仅保留统一枚举及消息对象发送接口；无openSendMsg开关，前端展示器支持卡片、顶部提示及独立居中弹窗，退出清理自身提示和队列。
- 用户部门或角色关系变更成功后通过 `system.service.AdminUserRefreshService` 在事务提交后定向发送REFRESH_BROWSER；全局前端整页刷新，/getInfo读取最新部门和角色并按需更新当前登录快照。角色菜单授权或实际数据范围变化同样在提交后定向通知所属用户；仅修改角色名称、排序、备注不通知。HTTP认证读取当前角色及菜单权限，Redis会话快照仍用于登录状态，权限快照有变化时更新；离线不补发，推送失败不回滚业务。
- API谷歌验证修改保存成功后，在事务提交后通过 `AdminUserRefreshService.afterApiAuthChanged()` 广播REFRESH_BROWSER至所有在线连接，页面刷新后重新读取 `/system/apiAuth/googleAuthApis` 更新本地规则；查询接口不触发推送，原校验、权限和审计日志保留。
- `zero-admin` 登录验证码由 `/captchaImage`、`SysLoginService.validateCaptcha`、`ISysConfigService.selectCaptchaEnabled()` 和配置项 `sys.account.captchaEnabled` 控制。
- `sys.account.multiClientLogin` 在完整登录验证通过后生效，包含超级管理员。显式设为 `false` 时，通过 `RedisTemplate` 的账号标记和事务保存新会话、撤销同账号旧会话，再按旧会话定向发送 `LOGIN_OUT`。设为 `true` 或未配置时保留多端登录。续期只更新仍存在的会话，参数变更在下一次成功登录时应用。
- `zero-admin` 当前 XSS 配置在 `application.yml` 的 `xss` 节，匹配 `/system/*,/monitor/*,/tool/*`，不要认为所有 JSON 请求都天然安全。
- 数据权限通过 `@DataScope` 和 SQL 中 `${params.dataScope}` 拼接实现。涉及用户、角色、部门数据时必须先确认数据范围规则。
- `DataScopeAspect` 对“本部门”“本部门及以下”范围按当前认证用户ID读取最新归属部门，并仅在当前请求内复用；不能回退登录缓存里的旧部门。辅助读取前挂起、读取成功后恢复原PageHelper实例，异常时清理分页上下文。全部、自定义、本人范围及超级管理员沿用既有规则；该机制不刷新角色、菜单权限或跨请求登录缓存。
- 日志、异常、导出、登录日志中不得输出密码、验证码、Token、数据库密码、密钥等敏感信息。

## 系统表与缓存维护

- 写入用户、角色、菜单、参数等系统表时，维护 `createBy/createTime/updateBy/updateTime` 等审计字段。
- 修改 `sys_config` 后注意缓存：Service 中会更新 Redis；必要时调用或提醒刷新参数缓存。

## 定时任务规则

- 定时任务使用 Quartz 模块既有结构，不要套用外部 `@Scheduled` 规范。

## 编码风格

- Java 代码使用现有工具类：`StringUtils`、`SecurityUtils`、`DateUtils`、`ExcelUtil`、`RedisCache` 等。

## 源码依据

- admin 自身业务依据：`SysConfigController`、`SysMenuController`、`SysApiAuthController` 及其 Service、Mapper XML、前端页面/API。
- report 代理依据：`BizDictController`、`ReportRestService`、`ReportRestTemplateConfig`、`AdminForwardHeaderProvider`、`ReportAdminContext`、字典 Controller/Service、`BaseQuery` 和前端 `reportSystem/bizDict`。
- 文件服务依据：`FileManagementController`、`FileRestService`、`AdminFileUploadServiceImpl`、`AdminFileStorageModeServiceImpl`、`FileController`、`FileDetailServiceImpl`、`FileStorageService`、`FileStorageAutoConfiguration`、`FileExceptionHandler`、[业务库初始化脚本](../sql/zero_biz.sql) 和前端 `systemTools/fileManagement`。

## 常用验证命令

- admin 编译：`mvn -pl zero-admin -am compile -DskipTests`
- admin 运行：`mvn -pl zero-admin spring-boot:run`

## 关联模块规则

- 修改代理、公共类型或前后端契约时，同步读取 [report](../zero-report/AGENTS.md)、[file](../zero-file/AGENTS.md)、[tools](../zero-tools/AGENTS.md)或 [UI](../zero-ui/AGENTS.md)中涉及模块的规则；菜单权限及接口字段语义统一见根规则。
