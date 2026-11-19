# ZERO-TASK 定时任务执行服务

`zero-task` 使用 Java 17 和 XXL-JOB Core 3.5.0。ZERO-JOB 通过 XXL-JOB 原生 HTTP 协议派发任务，执行器调用本服务中的 `@XxlJob` 方法并回传结果。

## 配置和启动

1. Nacos 连接信息分别在 [application-dev.yml](src/main/resources/application-dev.yml)、[application-test.yml](src/main/resources/application-test.yml) 和 [application-prod.yml](src/main/resources/application-prod.yml) 中维护，默认地址和命名空间与 `zero-job-admin` 对应环境一致。在对应命名空间发布 [zero-task-common.yml](src/main/resources/nacosConfig/zero-task-common.yml)，Data ID 填 `zero-task-common.yml`，Group 按环境填 `dev`、`test` 或 `prod`。服务启动时读取该分组的配置，并以 `zero-task` 注册到该环境命名空间的 `DEFAULT_GROUP`。如果多个环境共用命名空间，配置分组只区分配置项，服务实例仍会注册到同一分组。Nacos 开启鉴权时，通过 Spring Boot 外部配置提供连接凭据。
2. 配置 `xxl.job.executor.admin-addresses` 为执行器可以访问的调度中心 HTTP 基址。同机开发可用 `http://127.0.0.1:9102`，SDK 自动追加 `/api`。多个调度中心地址以逗号分隔。
3. 配置 `xxl.job.executor.address` 为调度中心可以访问的执行器地址。同机开发可用 `http://127.0.0.1:9998`，跨主机时使用可达的内网地址。默认 `9998` 用于任务派发和查询任务执行日志，`9104` 用于 Boot Admin 采集 `/actuator` 监控数据。执行器端口只向调度中心开放，监控路径只向内网监控端开放。
4. 在 ZERO-JOB 的「执行器管理」新增自动注册的执行器组，AppName 填 `zero-task`，AccessToken 与 Nacos 的 `xxl.job.executor.access-token` 使用同一值。凭据通过受控配置维护。

三个环境文件通过 `spring.config.import` 声明要加载的 Data ID。`optional:` 允许其中一个模板尚未发布。`zero-task-common.yml` 提供执行器参数，MySQL 和 Redis 模板按任务需求发布。

服务运行时会监听 `zero-task-common.yml` 的变更，并刷新 Spring 环境中的配置。执行器在启动时读取 `xxl.job.executor.*`，修改调度地址、端口、AccessToken 等启动参数后，仍需重启 `zero-task` 才会应用到执行器。应用运行日志由 `logging.file.*` 和 Logback 管理，单次任务日志由 `xxl.job.executor.log-path` 和 `log-retention-days` 管理。

在仓库根目录执行：

```powershell
mvn -pl zero-task -am package
java -jar zero-task/target/zero-task-1.0.0.jar --spring.profiles.active=dev
```

## 验证任务

执行器上线后，在 ZERO-JOB 的「任务管理」新增任务，选择 `zero-task` 执行器组，运行模式选 `BEAN`，JobHandler 填 `zeroTaskDemoHandler`。

- 点击「执行一次」，参数留空，调度日志应显示执行成功。
- 参数填 `fail` 再执行一次，调度日志应显示执行失败。
- 在「执行日志」查看参数长度和分片信息。

新增业务任务时，在 `com.zero` 下添加 Spring 组件，参考 [DemoTaskHandler](src/main/java/com/zero/handler/DemoTaskHandler.java) 编写无参方法并标注唯一的 `@XxlJob("handlerName")`，然后在 ZERO-JOB 中使用同名 JobHandler。执行器仅启用 BEAN 模式。

## MySQL 和 Redis

将配置发布到当前环境的 Nacos 分组，填写连接信息后重启 `zero-task`。启动依赖 MySQL，Redis 按需接入。

| 配置 | Data ID | 启用条件 |
| --- | --- | --- |
| [MySQL 模板](src/main/resources/nacosConfig/zero-task-mysql.yml) | `zero-task-mysql.yml` | 启动必需，配置 `zeroBiz` 和 `zeroAdmin` |
| [Redis 模板](src/main/resources/nacosConfig/zero-task-redis.yml) | `zero-task-redis.yml` | 配置 `spring.data.redis.host` |

- JDBC 连接从当前环境的 Nacos 配置 `zero-task-mysql.yml` 读取，启动时验证 `zeroBiz` 可连接。默认访问 `zeroBiz`，后台库通过独立 Service Bean 的公开方法标注 `@DS("zeroAdmin")` 切换。启动类扫描 `com.zero.mapper`，对应 XML 放在 `src/main/resources/mapper`，分页由 `MybatisPlusConfig` 装配。
- Redis 启用后可注入 `RedissonUtil`。`StringRedisTemplate` 和 `RedisTemplate` 也使用 Redisson 连接工厂。任务键统一采用 `zero-task:<环境>:<业务>:<标识>`，例如 `zero-task:dev:order-sync:cursor`。
- `TaskBaseService` 子类可用 `getBizDictItem("order_status.paid")`、`getBizDictValue("order_status.paid")` 或 `getBizDictItems("order_status")` 查询业务字典。两服务共用 `zeroBiz` 和 Redis 库，报表服务提交字典修改后只清理受影响的缓存，下次查询有结果时回填无有效期缓存。查无结果时，单项和值返回 null，分组返回空列表，均不写入 Redis。Redis 关闭或连接失败时，任务直接查询 `zeroBiz`。
- 依赖这些能力的 JobHandler 按数据源和 Redis 客户端的装配条件启用。旧配置若显式设置 `zero.task.redis.enabled=false`，继续关闭 Redis。

### 从已有表生成代码

在 IDEA 运行 [TaskCodeGenerator](src/test/java/com/zero/generator/TaskCodeGenerator.java) 的 `main()`，程序实参填写表名，多张表用英文逗号分隔。运行配置需提供 `TASK_GENERATOR_DB_URL`、`TASK_GENERATOR_DB_USERNAME`、`TASK_GENERATOR_DB_PASSWORD` 三个环境变量。每次连接一个目标库，使用具备读取表结构权限的开发账号，凭据放在本地运行配置中。

生成实体、Mapper 接口、Mapper XML、Service 接口和实现，输出到模块自身的 `src/main/java/com/zero` 与 `src/main/resources/mapper`。生成器会去掉 `zero_`、`biz_`、`report_` 前缀，跳过已有文件。生成的 Service 由 Spring 组件扫描装配。

## Boot Admin 监控

Boot Admin 通过 Nacos 发现实例，再从 `/actuator` 读取健康详情、JVM 指标、线程、环境和应用日志。环境变量和配置属性的值会脱敏，监控端点为只读。

应用日志沿用其他微服务的 Logback 格式，记录时间、线程、级别、类名、方法和行号。默认目录为 `logs/zero-task`，普通日志写入 `sys-info.log`，异常日志写入 `sys-error.log`，Boot Admin 读取包含各级别运行信息的汇总文件 `application.log`。文件按日期和大小滚动，单文件上限 200 MB，归档保留 60 天。单次任务执行日志在 ZERO-JOB 调度中心查看。
