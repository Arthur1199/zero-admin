# zero-report 模块规则

本文件适用于 `zero-report/`。同时遵守[仓库全局规则](../AGENTS.md)，通用 Java/YAML 注释规范及安全要求以根文件为准。下文带模块名前缀的代码路径相对仓库根目录，其余模块内部路径相对本目录；Markdown 链接相对本文件。跨模块修改同时读取相关模块规则。

## 技术栈与版本来源

- `zero-report` 是报表/业务服务，主入口为 `com.zero.ZeroReportApplication`，启用 Nacos 注册/配置、OpenFeign、`@MapperScan`。主要技术包括 MyBatis-Plus、MyBatis-Plus 分页拦截器、dynamic-datasource、PageHelper（复杂 XML 报表 SQL 保留使用）、Redis、Actuator、Validation、Lombok。

## 后端结构规则

- `zero-report` 结构以当前代码为准：`controller` 放 HTTP 接口，`service` / `service.impl` 放业务规则，`mapper` 放 MyBatis-Plus Mapper，`domain` 放实体，`queryDto` 放查询参数，`dto` 放服务内 DTO/返回结构，`base` 放分页和 Service 父类，`config` 放 MyBatis-Plus、异常、Feign、XML 加载等配置。
- `zero-report` 的 Mapper 扫描由 `ZeroReportApplication` 上的 `@MapperScan(PackageProperties.MAPPER_SCAN)` 和 `PackageProperties` 统一维护；新增包路径前必须确认 `PackageProperties` 是否需要同步。

## 模式二：admin 调 report 服务业务

- report 服务负责真实业务：Controller、Service、Mapper、实体、查询 DTO、MyBatis-Plus 查询、唯一性校验、软删除/恢复、审计字段、事务和异常处理都放在 `zero-report`。
- report Service 通过 `ReportBaseService` 使用 `ReportAdminContext` 的既有上下文：管理员 ID、账号、昵称、公司名称、角色 ID、是否管理员、客户端 IP、Authorization、来源服务。例如 `getAdminId()`、`getAdminName()`、`getAdminRoleIds()`、`isAdmin()`、`getClientIp()`、`getAuthorization()`、`getSourceService()`；不要为兼容不存在的字段自行拼请求头。新增字段须同步维护 admin 侧常量、请求头透传、report 解析和父类获取方法。

## 业务与持久化规则

- `zero-report` 当前定位是业务库/report 服务，不是管理后台权限中心。管理后台按钮权限、菜单权限、操作日志放在 `zero-admin` 代理层；report Controller 专注真实业务接口。
- `zero-report` 的 Redis 客户端为 Redisson，连接信息仍从 `zero-report-redis.yml` 的 `spring.data.redis` 读取。Service 子类可通过 `getRedissonUtil()` 使用 Redisson 缓存工具，`RedisTemplate` 兼容入口也复用同一客户端。原生工具使用 JSON 编解码，新增缓存键按业务独立命名，避免与已有键的序列化格式混用。
- `DictConfigUtil` 使用 Redisson `RBucket` 缓存单项和分组查询。单项查无结果返回 `null`，列表查无结果返回空集合。缓存未命中时查询 `zeroBiz`，有结果时写入无有效期缓存，空结果不缓存。Redis 不可用时直接查库。
- 业务字典由 `zero-report` 写入。写入成功后只清理受影响的单项及其分组列表，改编码或移动分组时包含新旧缓存键。分组变更只清理该组列表及其下的单项缓存。有事务时在提交后执行，回滚时保留原缓存。Redis 清理失败时记录告警，受影响的旧缓存需要清理。
- report 接口可以读取 admin 透传的上下文，但这些上下文只应作为内部链路信息使用。直接外部调用 report 时，不应仅凭 `adminId`、`isAdmin` 等自定义请求头做授权判断。
- 启动入口 `ZeroReportApplication` 已统一设置时区 `Asia/Shanghai`、应用 `PackageProperties`，启用服务发现、Feign 和 Mapper 扫描；包路径变更按本文件的扫描规则维护。
- report 的动态数据源以 `zero-report-mysql.yml` 为准，当前 primary 是 `zeroBiz`；`ReportDataSourceConstants.REPORT` 对应 `zeroBiz`，`ReportDataSourceConstants.ADMIN` 对应 `zeroAdmin`。业务表默认走 `zeroBiz`，只有明确需要读取后台基础数据时才切到 `zeroAdmin`。
- 简单 CRUD 和常规分页优先使用 MyBatis-Plus：实体使用 `@TableName`、主键用 `@TableId`、字段必要时用 `@TableField`；Mapper 继承 MyBatis-Plus `BaseMapper`；Service 实现优先继承项目 `ReportBaseService<M, T>`。
- 复杂报表 XML SQL 才使用 PageHelper。不要在同一个查询里同时用 MyBatis-Plus `Page` 和 PageHelper 分页，避免分页叠加。
- 分页查询 DTO 继承 `BaseQuery`，复用 `pageNum`、`pageSize`、`beginTime`、`endTime`，通过 `query.toPage()` 生成 MyBatis-Plus `Page`。日期范围使用 `hasBeginTime()`、`getBeginDateTime()`、`hasEndTime()`、`getEndExclusiveDateTime()` 组成半开区间，禁止手写 `" 00:00:00"`、`" 23:59:59"` 字符串。特殊默认条数或最大条数按 `DictItemQuery` 重写 `defaultPageSize()`、`maxPageSize()`，不在各 Service 重复声明最大 `pageSize` 常量。
- MyBatis-Plus 全局分页兜底在 `MybatisPlusConfig`，当前最大单页限制是 10000；业务查询还应通过 `BaseQuery` 设定更小的业务上限，例如普通列表默认最大 500、展开明细可单独设 1000。
- 查询条件使用 `LambdaQueryWrapper` / `lambdaQuery()` / `lambdaUpdate()`，字符串条件先 `StringUtils.hasText` 再 trim，避免空字符串进入 SQL。排序要稳定，至少包含业务排序字段和主键兜底。
- 业务可预期失败抛 `IllegalArgumentException` 并返回明确提示，例如编码重复、必填为空、记录不存在、父级已删除。不要为每个 MyBatis-Plus 查询、更新或删除重复包 `try/catch`；数据库及未知异常由 `ReportExceptionHandler` 记录和兜底。处理器根据 Controller 返回类型或 `/list` 路径，将列表错误转成 `TableDataInfo.error`、普通错误转成 `AjaxResult.error`；列表返回结构不是 `TableDataInfo` 时，须核对异常结构与前端预期。
- `ReportBaseService` 子类通过 `getBizDictValue("分组编码.字典项编码")` 取原始字符串，或通过 `getBizDictItem(...)` 取完整字典项。按分组读取用 `getBizDictItems("分组编码")`，结果按排序值和主键升序。查询显式访问 `zeroBiz`，只读取已提交、启用且未删除的字典项。单项无结果返回 `null`，列表无结果返回空集合。
- `ReportAdminContext` 会解码中文请求头、解析角色ID、只接受 `true` 或 `1` 表示管理员。业务上不要把空请求头当成超级管理员，也不要因为本地测试缺请求头就放宽权限。
- report 继续调用其它 Java 服务时使用 OpenFeign，并依赖 `FeignRequestInterceptorConfig` 继续透传操作人上下文；不要手写新的 Feign 拦截器或散落的请求头拼接。
- 写入审计字段时，创建人/更新人优先使用 `getAdminName()`；创建/更新时间使用服务端时间。日志中不要输出 Authorization、密码、验证码、数据库密码、密钥等敏感信息。
- 软删除字段按业务表语义处理。列表是否展示已删除记录要按页面需求明确；详情、修改、恢复等接口要分别判断是否只允许未删除记录。
- 如果页面新增“删除标志”查询项，必须同步检查前端 `queryParams`、API 参数、admin 代理注释、report Query DTO 和 Service Wrapper 是否全链路支持。只在页面显示 `delFlag` 不等于已经支持按 `delFlag` 查询。
- 前端去掉输入框 `maxlength`、`show-word-limit` 或计数器，只表示页面不再展示长度限制；后端实体 `@Size`、数据库字段长度和唯一索引仍是最终约束，不能在规范中误写成字段无限长。
- report 的 Lombok 使用统一遵守[根文件规则](../AGENTS.md#java-lombok-使用规则)；实体、DTO 和查询对象保留字段注释及校验注解。

## 后端接口规范

- report Controller 保持轻量，不继承 admin 的 `BaseController`；后台按钮权限和操作日志遵守[根文件业务边界](../AGENTS.md#管理后台业务开发模式规则)，具体代理实现见 [admin 规则](../zero-admin/AGENTS.md#模式二admin-调-report-服务业务)。
- `zero-report` 分页列表接口使用 `BaseQuery.toPage()` + MyBatis-Plus `page(...)`，返回 `com.zero.dto.TableDataInfo.success(records, total)`。

## 常用验证命令

- report 编译：`mvn -pl zero-report -am compile -DskipTests`
- report 运行：`mvn -pl zero-report spring-boot:run`

## 关联规则与验证

- admin 代理入口与权限见 [zero-admin 规则](../zero-admin/AGENTS.md)，共享字典键和发布约定见[根规则](../AGENTS.md#共享业务字典协议)，任务消费端见 [zero-task 规则](../zero-task/AGENTS.md)。
- 字典与上下文源码依据：ReportAdminContext、ReportBaseService、BaseQuery、字典 Controller/Service 及 DictConfigUtil。
