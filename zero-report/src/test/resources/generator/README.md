# zero-report MyBatis-Plus代码生成器

生成器入口：

```text
com.zero.generator.ReportCodeGenerator
```

必填参数：

```text
report.generator.tables
```

可选参数：

```text
report.generator.db-url
report.generator.username
report.generator.password
report.generator.author
report.generator.base-package
report.generator.table-prefixes
report.generator.file-override
```

示例：

```bash
mvn -pl zero-report -am test-compile -DskipTests
mvn -pl zero-report exec:java -Dexec.classpathScope=test -Dexec.mainClass=com.zero.generator.ReportCodeGenerator -Dexec.args="report_order,report_order_item" -Dreport.generator.password=你的数据库密码
```

默认输出：

```text
src/main/java/com/zero/domain
src/main/java/com/zero/mapper
src/main/java/com/zero/service
src/main/java/com/zero/service/impl
src/main/java/com/zero/controller
src/main/resources/mapper
```

生成规则：

```text
service.impl 默认继承 com.zero.base.BaseService，统一复用请求上下文、Redis、日志和 MyBatis-Plus CRUD 能力。
```

默认不覆盖已有文件；确需覆盖时显式传入：

```text
-Dreport.generator.file-override=true
```
