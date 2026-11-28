# ZERO-AI 服务

`zero-ai` 是独立的 Java 21 / Spring Boot 服务，保留 XXL-JOB Core 3.5.0 执行器，仅运行部署在本服务中的 `@XxlJob` BEAN 任务。原 `zero-task` 不受影响。

## 配置和启动

1. 选择 `dev`（默认）、`test` 或 `prod`，核对对应 [dev](src/main/resources/application-dev.yml)、[test](src/main/resources/application-test.yml) 或 [prod](src/main/resources/application-prod.yml) 配置中的 Nacos 地址和命名空间。开启鉴权时，通过部署配置分别覆盖 `spring.cloud.nacos.discovery` 和 `spring.cloud.nacos.config` 下的 `username`、`password`，不要沿用仓库中的示例凭据。配置按当前 profile 分组导入；服务默认以 `zero-ai` 注册到发现命名空间的 `DEFAULT_GROUP`，配置分组本身不隔离实例。
2. 在 Nacos 配置命名空间和对应 profile 分组中，按下列 Data ID 发布配置。`optional:` 只允许导入项缺失，不能替代启用组件所需的有效配置。收到导入项刷新事件后，服务会尝试用原启动参数重建上下文；单实例会短暂中断。

   | Nacos Data ID | 用途 |
   | --- | --- |
   | [zero-ai-task.yml](src/main/resources/nacosConfig/zero-ai-task.yml) | 执行器开关、调度中心地址、注册和任务日志。 |
   | [zero-ai-mysql.yml](src/main/resources/nacosConfig/zero-ai-mysql.yml) | `zeroAi`（默认）、`zeroBiz`、`zeroAdmin` 三个数据源。发布前按环境配置各自的 JDBC `url`、`username` 和 `password`。 |
   | [zero-ai-redis.yml](src/main/resources/nacosConfig/zero-ai-redis.yml) | Redis 主机、逻辑库和连接池。启用认证时提供 `spring.data.redis.password`；若采用模板中的环境变量示例，须先取消密码项的注释。可用 `zero.ai.redis.enabled=false` 关闭 Redis。 |

3. 在 [ZERO-JOB](../zero-jobAdmin/README.md#初始化与启动) 的「执行器管理」创建独立的自动注册组，AppName 填 `zero-ai`。通过部署配置为 `xxl.job.executor.access-token` 注入该组的 AccessToken，不使用模板中的现有值。`xxl.job.executor.admin-addresses` 填执行器可访问的调度中心基址，例如 `http://127.0.0.1:9102`（SDK 追加 `/api`）。`xxl.job.executor.address` 填调度中心可访问的执行器地址，同机时可用 `http://127.0.0.1:9999`。若暂不启用执行器，设置 `xxl.job.executor.enabled=false`。
4. [应用 HTTP](src/main/resources/application.yml) 默认 `9105`，执行器独立监听 `9999`。应用汇总日志为 `logs/zero-ai/application.log`，供 Boot Admin 通过 `/actuator/logfile` 查看；任务日志默认写入 `logs/zero-ai/jobhandler`。仅向内网授权方开放监控、执行器和字典调试路径 `/internal/ai/biz-dict`。

构建和启动命令（仓库根目录，Java 21）：

```powershell
mvn -pl zero-ai -am package
java -jar zero-ai/target/zero-ai-1.0.0.jar --spring.profiles.active=dev
```

当前配置与模块身份测试有差异，直接运行构建命令前请先看[验证](#验证)。

## 任务及数据访问

在已授权的测试环境里，选择 `zero-ai` 执行器组，以 BEAN 模式运行 [示例任务](src/main/java/com/zero/handler/DemoAiHandler.java) `zeroAiDemoHandler`。参数留空验证成功回调，参数 `fail` 验证失败回调。

未显式切库时，按 MySQL 发布模板访问 `zeroAi`；需要访问管理库时，在独立 Service Bean 的公开方法用 `@DS("zeroAdmin")` 切换。[AiBaseService](src/main/java/com/zero/base/AiBaseService.java) 提供 `getDictItem`、`getDictItems`、`getDictValue` 查询有效业务字典，数据库回源固定走 `zeroBiz`。启用共享缓存时与 `zero-report` 使用同一个 Redis 实例和逻辑库；业务私有缓存键不使用公共字典键前缀。

## 从已有表生成代码

在 IDEA 运行 [AiCodeGenerator](src/test/java/com/zero/generator/AiCodeGenerator.java) 的 `main()`，实参填表名，多张表以英文逗号分隔。运行配置提供 `AI_GENERATOR_DB_URL`、`AI_GENERATOR_DB_USERNAME`、`AI_GENERATOR_DB_PASSWORD`，使用只需读表结构的开发账号。

实体、Mapper 接口、Service 接口与实现输出到模块的 `src/main/java/com/zero`；Mapper XML 写入 `src/main/resources/mapper`。默认去除 `zero_`、`biz_`、`report_` 表名前缀并跳过已有文件。不生成 Controller，也不随服务启动。

## 验证

- `mvn -pl zero-ai -am -Dzero.ai.http-test=true test`：在本机用模拟调度中心验证执行器注册、任务派发和回调，不连接真实 Nacos 或调度中心。
- 测试默认跳过需要真实 Redis 的用例；准备隔离 Redis 实例后，追加 `-Dzero.ai.test.redis-port=<端口>` 验证缓存读写及字典回源。
- 已知差异：[模块身份测试](src/test/java/com/zero/config/AiServiceIdentityTest.java)仍期望旧的执行器 Data ID，并要求模板的 `access-token` 使用环境变量占位；当前发布模板与断言不一致。带测试的构建前需同步测试和配置，不要把测试中的旧文件名当成发布指南。
