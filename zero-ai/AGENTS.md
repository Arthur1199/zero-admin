# zero-ai 模块规则

本模块是独立的 Spring Boot 服务，保留 XXL-JOB BEAN 执行器和业务基础设施。通用规则见[仓库规则](../AGENTS.md)。

| 事项 | 源码入口 |
| --- | --- |
| 启动、组件和 Mapper 扫描 | [ZeroAiApplication.java](src/main/java/com/zero/ZeroAiApplication.java)、[PackageProperties.java](src/main/java/com/zero/packageconfig/PackageProperties.java) |
| XXL-JOB 装配与示例 | [AiExecutorConfig.java](src/main/java/com/zero/config/AiExecutorConfig.java)、[AiExecutorProperties.java](src/main/java/com/zero/config/AiExecutorProperties.java)、[DemoAiHandler.java](src/main/java/com/zero/handler/DemoAiHandler.java) |
| Redis 与只读字典 | [AiRedisConfig.java](src/main/java/com/zero/config/AiRedisConfig.java)、[AiBaseService.java](src/main/java/com/zero/base/AiBaseService.java)、[DictConfigUtil.java](src/main/java/com/zero/util/DictConfigUtil.java)、[AiDictItemMapper.xml](src/main/resources/mapper/AiDictItemMapper.xml) |
| 测试作用域代码生成 | [AiCodeGenerator.java](src/test/java/com/zero/generator/AiCodeGenerator.java)、[serviceImpl.java.vm](src/test/resources/ai-generator/serviceImpl.java.vm) |

## 服务与部署边界

- 应用 HTTP 默认 `9105`，执行器默认 AppName `zero-ai`、端口 `9999`。新增任务使用 `@XxlJob` BEAN 模式，不启用 GLUE，不复用 `zero-task` 的执行器组或凭据。Actuator、执行器端口及[内部字典调试接口](src/main/java/com/zero/controller/AiBizDictTestController.java)只对受控网络开放；调试接口不继承后台鉴权。
- 各环境按活跃 profile 分组导入 `zero-ai-task.yml`、`zero-ai-mysql.yml`、`zero-ai-redis.yml`。[配置监听器](src/main/java/com/zero/listener/NacosConfigChangeListener.java)收到导入项刷新事件后请求重建上下文，单实例在重启期间会短暂中断。修改 Data ID 时同步核对三个环境的导入声明、发布模板及[模块身份测试](src/test/java/com/zero/config/AiServiceIdentityTest.java)。不要把配置中的凭据复制到文档、日志或示例。

## 数据与缓存边界

- [MySQL 发布模板](src/main/resources/nacosConfig/zero-ai-mysql.yml)声明 `zeroAi` 为默认数据源并启用严格路由；共享业务数据用 `zeroBiz`，管理数据用 `zeroAdmin`。切库在独立 Spring Bean 的公开方法上声明 `@DS`，不要依赖调用方的路由或事务。
- `zero-report` 负责写入字典和清理缓存；本模块只读，缓存未命中或不可用时由 `DictConfigUtil` 切换到 `zeroBiz` 并暂停调用方事务回源。涉及字典的改动须遵循[仓库字典协议](../AGENTS.md#共享业务字典协议)。只有配置 `spring.data.redis.host` 且未关闭 `zero.ai.redis.enabled` 时才装配 Redis；模块私有键用 `zero-ai:<环境>:<业务>:<标识>`，不要占用公共字典前缀。

## 验证

- 从仓库根目录用 Java 21 执行 `mvn -pl zero-ai -am -Dzero.ai.http-test=true package`。HTTP 调度用例使用本地模拟调度中心，不能代替真实注册和回调验证。Redis 用例只在隔离实例上用 `-Dzero.ai.test.redis-port=<端口>` 开启。启动、生成器及当前测试配置差异见[模块说明](README.md)。
