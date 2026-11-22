# zero-file 模块规则

本文件适用于 `zero-file/`。同时遵守[仓库全局规则](../AGENTS.md)，通用 Java/YAML 注释规范及安全要求以根文件为准。下文带模块名前缀的代码路径相对仓库根目录，其余模块内部路径相对本目录；Markdown 链接相对本文件。跨模块修改同时读取相关模块规则。

## 技术栈与版本来源

- `zero-file` 是独立文件服务，入口为 `com.zero.ZeroFileApplication`，通过 `@EnableFileStorage` 启用存储能力，使用 MyBatis-Plus、dynamic-datasource 和 `zero-tools`，文件记录表为业务库的 `biz_file_detail`。

## 后端结构规则

- `zero-file` 使用自身的 `packageconfig`、`base.BaseQuery`、`config`、`controller`、`service`、`mapper`、`domain`、`queryDto`、`dto`；存储实现放 `platform`，记录回调放 `recorder`，存储操作和预处理放 `vo`，扩展链放 `aspect`。不要因包名相同就混用 report 的类。

## 模式三：admin 调文件服务

- 文件记录使用 `biz_file_detail`，默认数据源 key 为 `zeroBiz`；`FileDetailServiceImpl` 继承 MyBatis-Plus `ServiceImpl` 并通过 `IFileDetailService` 实现 `FileRecorder`。不要套用 `ReportBaseService` 或报表业务的软删除规则。
- `FileStorageProperties` 配置前缀为 `spring.file-storage`；存储平台的启用、默认平台、域名、磁盘目录和云存储配置以当前配置为准。`DefaultFileRecorder` 仅为空实现兜底，不能宣称能持久化或查询文件记录。
- 文件管理按 ID 删除时，先保存存储对象信息并提交数据库记录删除，再调用原存储平台清理原文件及缩略图。清理失败仍返回成功，`data=false`，并提示“删除成功，云平台记录删除失败”。批量调用方的继续与中止规则见 [admin 文件代理规则](../zero-admin/AGENTS.md#模式三admin-调文件服务)。
- 当前管理页面仅提供列表、详情、上传、删除；file 服务仍存在 `PUT /file/{id}` 修改记录接口。页面不提供编辑不等于下游没有编辑接口，不自行删除接口或新增编辑菜单权限。

## 常用验证命令

- file 编译：`mvn -pl zero-file -am compile -DskipTests`
- file 运行：`mvn -pl zero-file spring-boot:run`

## 关联规则与验证

- 上传入口和后台代理见 [zero-admin 规则](../zero-admin/AGENTS.md)，管理页面见 [zero-ui 规则](../zero-ui/AGENTS.md)。
- 源码依据：FileController、FileDetailServiceImpl、FileStorageService、FileStorageAutoConfiguration、FileExceptionHandler，表定义见 [业务库初始化脚本](../sql/zero_biz.sql)。
