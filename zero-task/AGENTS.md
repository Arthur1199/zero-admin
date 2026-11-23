# zero-task 模块规则

本文件适用于 `zero-task/`。同时遵守[仓库全局规则](../AGENTS.md)，通用 Java/YAML 注释规范及安全要求以根文件为准。下文带模块名前缀的代码路径相对仓库根目录，其余模块内部路径相对本目录；Markdown 链接相对本文件。跨模块修改同时读取相关模块规则。

## 职责与源码入口

| 事项 | 当前入口 |
| --- | --- |
| 启动、扫描与配置变化 | [ZeroTaskApplication.java](src/main/java/com/zero/ZeroTaskApplication.java)、[PackageProperties.java](src/main/java/com/zero/packageconfig/PackageProperties.java)、[NacosConfigChangeListener.java](src/main/java/com/zero/listener/NacosConfigChangeListener.java) |
| 执行器配置与任务 | [TaskExecutorConfig.java](src/main/java/com/zero/config/TaskExecutorConfig.java)、[TaskExecutorProperties.java](src/main/java/com/zero/config/TaskExecutorProperties.java) |
| 数据源、分页与 Redis | [MybatisPlusConfig.java](src/main/java/com/zero/config/MybatisPlusConfig.java)、[TaskRedisConfig.java](src/main/java/com/zero/config/TaskRedisConfig.java)、[RedissonAutoConfigCustomizer.java](src/main/java/com/zero/config/RedissonAutoConfigCustomizer.java) |
| 业务字典消费 | [TaskBaseService.java](src/main/java/com/zero/base/TaskBaseService.java)、[DictConfigUtil.java](src/main/java/com/zero/util/DictConfigUtil.java)、[TaskDictItemMapper.java](src/main/java/com/zero/mapper/TaskDictItemMapper.java)、[TaskDictItemMapper.xml](src/main/resources/mapper/TaskDictItemMapper.xml) |
| 显式代码生成 | [TaskCodeGenerator.java](src/test/java/com/zero/generator/TaskCodeGenerator.java)、[serviceImpl.java.vm](src/test/resources/task-generator/serviceImpl.java.vm) |

## 任务执行与监控

- `zero-task` 使用 XXL-JOB Core 3.5.0，主入口为 `com.zero.ZeroTaskApplication`。任务方法使用 `@XxlJob`，执行器端口默认 9998，仅运行 BEAN 任务。
- Boot Admin 从 `zero-task` 的 HTTP 端口读取只读 Actuator 监控数据，环境和配置属性值脱敏。应用日志沿用其他微服务的 Logback 格式，默认在 `logs/zero-task` 下生成 `sys-info.log`、`sys-error.log` 和供 Boot Admin 读取的 `application.log`。任务执行日志由 XXL-JOB 管理。

- 执行器由 `xxl.job.executor.enabled` 控制，未配置时默认启用；`TaskExecutorConfig` 校验调度地址、AppName、AccessToken、端口、超时和日志路径，并关闭 GLUE。新增处理器保留 XXL-JOB 原生执行及回传语义，不套用后台返回对象。

## 数据源、扫描与 Redis

- 任务按需配置 MySQL，动态数据源由框架自动装配，JDBC 参数从 Nacos 的 `zero-task-mysql.yml` 读取。配置 `spring.data.redis.host` 后装配 Redis，显式设置 `zero.task.redis.enabled=false` 可关闭 Redis。任务和报表的 Redis 模板使用相同的 `spring.data.redis` 连接参数和 `zero.redis` 连接池参数，分别按服务 Data ID 发布。发布连接配置变更后，服务自动重启。MySQL 默认访问 `zeroBiz`，需要后台库时显式使用 `@DS("zeroAdmin")`。Mapper 在 `com.zero.mapper`，任务 Redis 键使用 `zero-task:<环境>:<业务>:<标识>`。
- `zero-task` 的 `PackageProperties` 统一维护组件、Mapper 和实体别名的包路径。Mapper 由启动类扫描，分页由 `MybatisPlusConfig` 装配，Redis 组件由 Redis 配置单独导入。Mapper XML 统一放在 `zero-task/src/main/resources/mapper` 根目录，文件名以 `Mapper.xml` 结尾。MyBatis 使用 `classpath*:mapper/*Mapper.xml` 只扫描这一层，`TaskCodeGenerator` 也直接输出到该目录。

- Redis 的装配条件仅针对 Redis：声明 `spring.data.redis.host`，且 `zero.task.redis.enabled` 为 true 或未设置。主机空值由配置校验拒绝；缺失 Redis Bean 时，业务注入与字典回源应按现有可选依赖处理。数据源仍由 dynamic-datasource 自动配置，不能把 Redis 条件描述成所有 Mapper、Service 和 MySQL 的统一开关。

## 业务字典消费

- `zero-task` 启用 Redis 后使用 Redisson，任务按需注入 `RedissonUtil`，已有 Spring Redis 模板复用 Redisson 连接工厂。`TaskBaseService` 提供 `getDictItem`、`getDictItems`、`getDictValue` 三个字典查询方法，显式访问本地 `zeroBiz`，与同环境 `zero-report` 共用 Redis 库和 `zero:dict:config:` 失效协议。

- 单项参数、原始编码、缓存载荷、无结果语义及事务隔离遵守[共享业务字典协议](../AGENTS.md#共享业务字典协议)，不使用任务私有缓存前缀。`TaskBizDictTestController` 是内部诊断入口；它没有自动继承 admin JWT 或按钮权限，部署时核对实际访问边界，不能据路径含 `internal` 就认定已经鉴权。

## 开发期代码生成

- 开发期通过测试源码中的 `com.zero.generator.TaskCodeGenerator` 显式生成，连接信息读取 `TASK_GENERATOR_DB_URL`、`TASK_GENERATOR_DB_USERNAME`、`TASK_GENERATOR_DB_PASSWORD`，必须指定表名。当前生成实体、Mapper、单层 Mapper XML、Service 接口及实现，不生成 Controller；实体启用 Lombok，已有文件跳过，Service 由 Spring 组件扫描装配。修改模板后验证产物及重复执行跳过行为，不能只改已生成文件。

## 注册与配置生效

- 服务以 `zero-task` 注册到 Nacos 的 `DEFAULT_GROUP`。Nacos 连接信息由 `application-dev.yml`、`application-test.yml`、`application-prod.yml` 分别维护，默认地址和命名空间与 `zero-job-admin` 对应环境一致。三个环境均从当前 profile 分组可选导入 `zero-task-common.yml`、`zero-task-mysql.yml` 和 `zero-task-redis.yml`，发布变更后服务自动重启。`xxl.job.executor.*` 在新上下文启动时装配。执行器通过 XXL-JOB 原生协议向调度中心注册并接收任务，执行器组 AppName 和 AccessToken 须与对应配置一致。

## 关联规则与验证

- 调度中心规则见 [zero-jobAdmin 规则](../zero-jobAdmin/AGENTS.md)，共享字典键和同步发布要求见[根规则](../AGENTS.md#共享业务字典协议)，写入及失效维护见 [zero-report 规则](../zero-report/AGENTS.md)。
- 配置、启动及代码生成说明见[模块 README](README.md)。在仓库根目录执行 `mvn -pl zero-task -am package`；隔离验证通过不代表真实调度、数据库或 Redis 联调通过。

- 现有回归入口：`TaskInfrastructureTest`、`TaskExecutorConfigTest`、`TaskExecutorHttpTest`、`TaskCodeGeneratorTest`。先看测试使用的隔离资源与写入范围，再选择运行；真实调度派发、数据库写入及配置发布仍须遵守根文件权限边界。
