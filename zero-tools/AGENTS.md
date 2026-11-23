# zero-tools 模块规则

本文件适用于 `zero-tools/`。同时遵守[仓库全局规则](../AGENTS.md)，通用 Java/YAML 注释规范及安全要求以根文件为准。下文带模块名前缀的代码路径相对仓库根目录，其余模块内部路径相对本目录；Markdown 链接相对本文件。跨模块修改同时读取相关模块规则。

## 职责与源码入口

| 事项 | 当前入口 |
| --- | --- |
| 公共业务响应 | [AjaxResult.java](src/main/java/com/zero/model/AjaxResult.java) |
| SQL 日志与通用校验 | [SQLFormatter.java](src/main/java/com/zero/utils/sql/SQLFormatter.java)、[ValidationHelper.java](src/main/java/com/zero/utils/common/ValidationHelper.java) |
| 令牌与动态码 | [JwtUtils.java](src/main/java/com/zero/utils/security/jwt/JwtUtils.java)、[GoogleAuthenticatorUtils.java](src/main/java/com/zero/utils/security/google/GoogleAuthenticatorUtils.java) |
| 外部服务及通知 | [HttpClientHelper.java](src/main/java/com/zero/utils/http/HttpClientHelper.java)、[MailHelper.java](src/main/java/com/zero/utils/mail/MailHelper.java)、[TeleGramMsgHelper.java](src/main/java/com/zero/utils/notify/telegram/TeleGramMsgHelper.java) |

## 技术栈与版本来源

- 本模块 JWT 工具使用 `com.auth0:java-jwt`，当前为 4.6.2；与 admin 登录令牌使用的库不同，维护依赖时核对各自调用方。admin 登录令牌依赖见 [admin 技术栈](../zero-admin/AGENTS.md#技术栈与版本来源)。

## 职责与依赖边界

- 本模块是普通 Java 工具包，无独立启动进程。report、file、task 复用公共能力；admin 当前复用 SQL 日志格式化工具，并排除本模块全部传递依赖。
- 跨服务通用类型和工具保持与具体业务解耦；admin 专属 common、登录态、Mapper 和 Service 不迁入本模块。抽取前核对全部调用方及其依赖解析结果。
- 返回对象沿用根规则的字段语义，修改公共方法签名、异常或空值语义时同步核对使用方。
- 短信、邮件、HTTP 等工具可能访问外部服务；验证优先使用隔离或模拟方式，发送消息及真实外部写操作须事先获得确认。

- 按实际包目录定位工具，不因名称通用就将不同模块的同名实现替换为本模块。安全工具变更先检查算法、密钥来源、编码、有效期及调用方兼容；日志和验证记录不得输出敏感原文。

## 常用验证命令

- 在仓库根目录执行 `mvn -pl zero-tools -am compile -DskipTests`；公共能力变更后，按实际调用范围验证受影响模块。
