# 官方来源与改造记录

- 官方仓库：https://github.com/xuxueli/xxl-job
- 标签：`3.5.0`
- 提交：`3bebfc1609b95413581b11e270e5670dce679813`
- 下载地址：https://github.com/xuxueli/xxl-job/archive/refs/tags/3.5.0.zip
- 下载日期：2026-11-18（Asia/Shanghai）
- 下载ZIP的SHA-256：`49aebb7d4eb0b1f94c8d12bc3d0058b389128399d5a4c30c047c9a21aa8d078f`
- 迁入范围：官方 `xxl-job-admin/src`；保留官方 `LICENSE`（GPL-3.0）及 `NOTICE`，保留原作者说明。发行和再分发时遵守上游许可证。

## 差异

1. Maven 父工程改为 `com.zero:zero:1.0.0`；调度业务包由 `com.xxl.job.admin` 改为 `com.zero.jobadmin`，同步 XML、模板、测试中的引用；仅作为测试示例的 OpenAPI 测试也归入新根包。
2. 官方启动入口由 `com.zero.ZeroJobAdminApplication` 替代，启用服务发现并统一上海时区；组件扫描边界由 `PackageProperties` 维护。
3. Java 17 沿用项目基线；Boot 4 / Spring 7 改为项目的 Boot 3.5 / Spring 6.2；MyBatis starter 使用 3.0.5。core、SSO、tool 仍分别为官方 3.5.0、2.4.1、2.5.2，Spring、Netty、Groovy、Gson 等版本由本工程依赖管理解析，不导入上游父BOM。
4. `WebErrorPageRegistrar` 改用 Boot 3 的 `org.springframework.boot.web.server` API，官方错误处理业务不变。
5. 官方 `application.properties` 改为 YAML 和环境配置；Boot 4 的错误页配置改用 `server.error.whitelabel.enabled`。凭据外置，数据库改为独立 `zero_job`，监控与report/file一致暴露全部已启用Actuator端点并显示健康详情，供Boot Admin及管理后台采集；监控路径排除控制台登录拦截，部署时须限制内部访问来源。官方 `logback.xml` 替换为项目风格的独立日志配置。
6. 添加 Nacos discovery/config；排除 Nacos 配置依赖传递引入的旧版 Druid starter，保留官方 Hikari。调度配置采用启动时加载，修改后需重启。
7. 官方测试放入显式 profile 管理的集成测试目录，并添加 `job-integration` 标签，普通打包始终排除，即使此前编译过官方用例也不会误运行。新增隔离Web兼容性测试；未批量重写第三方源码的原始注释和业务逻辑。
8. `tables_xxl_job-3.5.0.sql` 是上游原件。仓库 `sql/zero_job.sql` 用于初始化 `zero_job`，每次执行都会重建 8 张调度表并重新写入初始数据。已有实例升级应使用对应版本的迁移脚本。
9. 从 `XxlJobGroup.toString()` 移除AccessToken，防止官方缓存日志记录执行器凭据；保留getter/setter及实际通信认证逻辑。

未自动建库、发布Nacos配置、启动真实调度或发送邮件。当前工作区的修改版本不能标称为未经修改的官方发行版。
