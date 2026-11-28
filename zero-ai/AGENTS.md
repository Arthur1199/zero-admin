# zero-ai 模块规则

本模块是独立的 Spring Boot 服务，保留 XXL-JOB BEAN 执行器和业务基础设施。通用规则见[仓库规则](../AGENTS.md)。

| 事项 | 源码入口 |
| --- | --- |
| 启动、组件和 Mapper 扫描 | [ZeroAiApplication.java](src/main/java/com/zero/ZeroAiApplication.java)、[PackageProperties.java](src/main/java/com/zero/packageconfig/PackageProperties.java) |
| XXL-JOB 装配与示例 | [AiExecutorConfig.java](src/main/java/com/zero/config/AiExecutorConfig.java)、[AiExecutorProperties.java](src/main/java/com/zero/config/AiExecutorProperties.java)、[DemoAiHandler.java](src/main/java/com/zero/handler/DemoAiHandler.java) |
| Redis 与只读字典 | [AiRedisConfig.java](src/main/java/com/zero/config/AiRedisConfig.java)、[AiBaseService.java](src/main/java/com/zero/base/AiBaseService.java)、[DictConfigUtil.java](src/main/java/com/zero/util/DictConfigUtil.java)、[AiDictItemMapper.xml](src/main/resources/mapper/AiDictItemMapper.xml) |
| 测试作用域代码生成 | [AiCodeGenerator.java](src/test/java/com/zero/generator/AiCodeGenerator.java)、[serviceImpl.java.vm](src/test/resources/ai-generator/serviceImpl.java.vm) |

- 应用 HTTP 默认 `9105`，执行器默认 AppName `zero-ai`、端口 `9999`。管理端点和执行器端口不能当作公网业务入口，内部字典调试接口也不继承后台鉴权。执行器使用独立的组凭据，不启用 GLUE。
- 各环境仅导入 [zero-ai 专属 Nacos 模板](src/main/resources/nacosConfig)。[配置监听器](src/main/java/com/zero/listener/NacosConfigChangeListener.java)在收到导入项的刷新事件后请求重建上下文；发布前核对服务和配置的命名空间、分组及部署覆盖值。
- 默认业务数据源别名仍为 `zeroBiz`，显式访问管理库用 `zeroAdmin`。只有配置 `spring.data.redis.host` 且未关闭 `zero.ai.redis.enabled` 时装配 Redis。模块私有键用 `zero-ai:<环境>:<业务>:<标识>`，共享字典键仍遵循[仓库字典协议](../AGENTS.md#共享业务字典协议)，由 `zero-report` 写入和清理。
- 启动和生成器参数见[模块说明](README.md)。仓库根目录用 Java 21 执行 `mvn -pl zero-ai -am -Dzero.ai.http-test=true package`，验证生成器、模块身份和隔离 HTTP 调度；真实 Redis 用例须在隔离实例上用 `-Dzero.ai.test.redis-port=<端口>` 显式开启。
