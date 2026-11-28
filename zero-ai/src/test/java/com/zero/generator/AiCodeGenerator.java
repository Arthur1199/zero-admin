package com.zero.generator;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.engine.VelocityTemplateEngine;
import com.zero.packageconfig.PackageProperties;
import org.springframework.util.StringUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/19 14:08
 * 说明: 从指定数据库表生成任务服务的实体、Mapper、XML 和 Service。
 */
public final class AiCodeGenerator {

    /** 生成类名时移除的项目表前缀，与 zero-report 一致。 */
    private static final String[] TABLE_PREFIXES = {"zero_", "biz_", "report_"};

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 14:08
     * 说明: 使用开发环境提供的数据库连接生成指定表，跳过已有文件。
     * @param args 表名，支持以英文逗号分隔或分别传入多个参数
     * @return
     * 返回值: void
     * 返回值说明: 将生成结果写入 zero-ai 的源码与 Mapper 资源目录
     */
    public static void main(String[] args) {
        List<String> tables = Arrays.stream(args)
                .flatMap(arg -> Arrays.stream(arg.split(",", -1)))
                .map(String::strip)
                .distinct()
                .toList();
        if (tables.isEmpty() || tables.stream().anyMatch(table -> !table.matches("[A-Za-z0-9_]+"))) {
            throw new IllegalArgumentException("请显式传入表名，仅支持字母、数字和下划线");
        }
        generate(resolveModuleRoot(), requiredEnv("AI_GENERATOR_DB_URL"), requiredEnv("AI_GENERATOR_DB_USERNAME"),
                requiredEnv("AI_GENERATOR_DB_PASSWORD"), tables);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 14:08
     * 说明: 使用指定数据库元数据生成任务业务层，测试可指定独立输出目录。
     * @param moduleRoot 输出所在的模块根目录
     * @param url 数据库 JDBC 地址
     * @param username 数据库账号
     * @param password 数据库密码
     * @param tables 需生成的表名列表
     * @return
     * 返回值: void
     * 返回值说明: 生成任务业务文件，已有文件保持原样
     */
    static void generate(Path moduleRoot, String url, String username, String password, List<String> tables) {
        FastAutoGenerator.create(url, username, password)
                .globalConfig(builder -> builder.author("Arthur").commentDate("yyyy/MM/dd HH:mm")
                        .dateType(DateType.ONLY_DATE).disableOpenDir()
                        .outputDir(moduleRoot.resolve("src/main/java").toString()))
                .packageConfig(builder -> builder.parent(PackageProperties.BASE_PACKAGE).entity("domain").mapper("mapper")
                        .service("service").serviceImpl("service.impl").xml("mapper")
                        .pathInfo(Map.of(OutputFile.xml, moduleRoot.resolve("src/main/resources/mapper").toString())))
                .strategyConfig(builder -> {
                    builder.addInclude(tables.toArray(String[]::new)).addTablePrefix(TABLE_PREFIXES);
                    builder.entityBuilder().enableLombok().enableTableFieldAnnotation();
                    builder.mapperBuilder().enableBaseResultMap().enableBaseColumnList();
                    builder.serviceBuilder().formatServiceFileName("I%sService").formatServiceImplFileName("%sServiceImpl")
                            .serviceImplTemplate("/ai-generator/serviceImpl.java");
                    builder.controllerBuilder().disable();
                })
                .templateEngine(new VelocityTemplateEngine())
                .execute();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 14:08
     * 说明: 兼容从仓库根目录或模块目录运行生成器。
     * @return
     * 返回值: Path
     * 返回值说明: 包含生成器源码的 zero-ai 根目录
     */
    private static Path resolveModuleRoot() {
        Path generatorSource = Path.of("src/test/java/com/zero/generator/AiCodeGenerator.java");
        for (Path current = Path.of("").toAbsolutePath(); current != null; current = current.getParent()) {
            if (Files.isRegularFile(current.resolve(generatorSource))) {
                return current;
            }
            Path child = current.resolve("zero-ai");
            if (Files.isRegularFile(child.resolve(generatorSource))) {
                return child;
            }
        }
        throw new IllegalStateException("请从 zero-admin 仓库或 zero-ai 模块目录运行生成器");
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 14:08
     * 说明: 从进程环境读取连接参数，缺失时仅提示变量名。
     * @param name IDEA 运行配置中的环境变量名
     * @return
     * 返回值: String
     * 返回值说明: 当前运行的数据库连接参数
     */
    private static String requiredEnv(String name) {
        String value = System.getenv(name);
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException("请配置环境变量 " + name);
        }
        return value;
    }
}
