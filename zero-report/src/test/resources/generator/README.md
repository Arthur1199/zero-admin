# zero-report MyBatis-Plus 代码生成器

[ReportCodeGenerator](../../java/com/zero/generator/ReportCodeGenerator.java) 用于读取已有表结构并生成报表服务代码。它位于测试源码中，由开发人员在 IDEA 中显式运行 `main()`。工作目录可选仓库根目录或 `zero-report` 模块目录。

## 运行参数

将目标表名写入 IDEA 的 **Program arguments**，多张表可以用英文逗号或空格分隔，例如 `report_order,report_order_item`。省略参数会使用源码中的默认表 `biz_dict_group,biz_dict_item`。只有源码默认表名为空时，才会继续读取 `report.generator.tables` 或 `REPORT_GENERATOR_TABLES`。临时切换表时直接使用程序实参。

| 用途 | JVM 系统属性 | 环境变量 | 默认行为 |
| --- | --- | --- | --- |
| JDBC 地址 | `report.generator.db-url` | `REPORT_GENERATOR_DB_URL` | 使用源码中的本地测试库地址 |
| 作者 | `report.generator.author` | `REPORT_GENERATOR_AUTHOR` | 使用源码默认作者 |
| 基础包名 | `report.generator.base-package` | `REPORT_GENERATOR_BASE_PACKAGE` | 使用 `com.zero` |
| 移除表前缀 | `report.generator.table-prefixes` | `REPORT_GENERATOR_TABLE_PREFIXES` | 移除 `zero_`、`biz_`、`report_` |
| 覆盖生成文件 | `report.generator.file-override` | `REPORT_GENERATOR_FILE_OVERRIDE` | `false`，跳过已有文件 |

同一配置项优先读取非空 JVM 系统属性，再读取环境变量。数据库账号和密码目前由生成器的本地测试常量提供，命令行密码参数不会生效。运行前核对目标为隔离开发库，新模块参考 [zero-task 的环境变量方式](../../../../../zero-task/README.md#从已有表生成代码)。

在仓库根目录可以先验证生成器能编译：

```powershell
mvn -pl zero-report -am test-compile -DskipTests
```

## 输出

- 实体、Mapper、Service 接口及实现、Controller 分别写入模块的 `src/main/java/com/zero` 下对应包。
- Mapper XML 写入 `src/main/resources/mapper`。
- Service 实现继承 [ReportBaseService](../../../main/java/com/zero/base/ReportBaseService.java)，沿用业务字典、请求上下文与 MyBatis-Plus 的通用能力。
