# zero-task 模块规则

本文件适用于 `zero-task/`。同时遵守[仓库全局规则](../AGENTS.md)，通用 Java/YAML 注释规范及安全要求以根文件为准。下文带模块名前缀的代码路径相对仓库根目录，其余模块内部路径相对本目录；Markdown 链接相对本文件。跨模块修改同时读取相关模块规则。

## zero-task 执行服务规则

- `zero-task` 使用 XXL-JOB Core 3.5.0，主入口为 `com.zero.ZeroTaskApplication`。任务方法使用 `@XxlJob`，执行器端口默认 9998，仅运行 BEAN 任务。
- Boot Admin 从 `zero-task` 的 HTTP 端口读取只读 Actuator 监控数据，环境和配置属性值脱敏。应用日志沿用其他微服务的 Logback 格式，默认在 `logs/zero-task` 下生成 `sys-info.log`、`sys-error.log` 和供 Boot Admin 读取的 `application.log`。任务执行日志由 XXL-JOB 管理。
- 任务按需配置 MySQL，动态数据源由框架自动装配，JDBC 参数从 Nacos 的 `zero-task-mysql.yml` 读取。配置 `spring.data.redis.host` 后装配 Redis，显式设置 `zero.task.redis.enabled=false` 可关闭 Redis。任务和报表的 Redis 模板使用相同的 `spring.data.redis` 连接参数和 `zero.redis` 连接池参数，分别按服务 Data ID 发布。发布连接配置变更后，服务自动重启。MySQL 默认访问 `zeroBiz`，需要后台库时显式使用 `@DS("zeroAdmin")`。Mapper 在 `com.zero.mapper`，任务 Redis 键使用 `zero-task:<环境>:<业务>:<标识>`。
- `zero-task` 启用 Redis 后使用 Redisson，任务按需注入 `RedissonUtil`，已有 Spring Redis 模板复用 Redisson 连接工厂。`TaskBaseService` 提供 `getBizDictItem`、`getBizDictItems`、`getBizDictValue` 三个字典查询方法。字典查询访问 `zeroBiz`，与同环境 `zero-report` 共用 Redis 库和 `zero:dict:config:` 失效协议。有结果时写入无有效期缓存，空结果不缓存，Redis 不可用时回源数据库。
- 开发期通过 `com.zero.generator.TaskCodeGenerator` 和显式表名生成实体、Mapper、XML、Service 接口及实现，凭据从本地环境变量读取。生成器跳过已有文件，生成的 Service 由 Spring 组件扫描装配。
- `zero-task` 的 `PackageProperties` 统一维护组件、Mapper 和实体别名的包路径。Mapper 由启动类扫描，分页由 `MybatisPlusConfig` 装配，Redis 组件由 Redis 配置单独导入。Mapper XML 统一放在 `zero-task/src/main/resources/mapper` 根目录，文件名以 `Mapper.xml` 结尾。MyBatis 使用 `classpath*:mapper/*Mapper.xml` 只扫描这一层，`TaskCodeGenerator` 也直接输出到该目录。
- 服务以 `zero-task` 注册到 Nacos 的 `DEFAULT_GROUP`。Nacos 连接信息由 `application-dev.yml`、`application-test.yml`、`application-prod.yml` 分别维护，默认地址和命名空间与 `zero-job-admin` 对应环境一致。三个环境均从当前 profile 分组可选导入 `zero-task-common.yml`、`zero-task-mysql.yml` 和 `zero-task-redis.yml`，发布变更后服务自动重启。`xxl.job.executor.*` 在新上下文启动时装配。执行器通过 XXL-JOB 原生协议向调度中心注册并接收任务，执行器组 AppName 和 AccessToken 须与对应配置一致。

## 关联规则与验证

- 调度中心规则见 [zero-jobAdmin 规则](../zero-jobAdmin/AGENTS.md)，共享字典键和同步发布要求见[根规则](../AGENTS.md#共享业务字典协议)，写入及失效维护见 [zero-report 规则](../zero-report/AGENTS.md)。
- 配置、启动及代码生成说明见[模块 README](README.md)。在仓库根目录执行 `mvn -pl zero-task -am package`；隔离验证通过不代表真实调度、数据库或 Redis 联调通过。
