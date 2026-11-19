package com.zero.generator;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.engine.VelocityTemplateEngine;
import com.zero.base.ReportBaseService;
import com.zero.packageconfig.PackageProperties;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/6/28 01:52
 * 说明: 报表服务 MyBatis-Plus 代码生成器，基于数据库表结构生成 domain、mapper、service、controller 和 mapper.xml
 */
public class ReportCodeGenerator
{
    /**
     * 默认连接本地报表业务库
     */
    private static final String DEFAULT_DB_URL = "jdbc:mysql://127.0.0.1:3306/zero_biz"
            + "?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull"
            + "&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true";

    /**
     * 测试库用户名，仅用于本地代码生成器直接运行main方法
     */
    private static final String DEFAULT_DB_USERNAME = "root";

    /**
     * 测试库密码，仅用于本地代码生成器直接运行main方法；不要填写生产库密码
     */
    private static final String DEFAULT_DB_PASSWORD = "8!4hFfBDr7*R<Js79tJnk:BX9ljN";

    /**
     * 生成器默认作者
     */
    private static final String DEFAULT_AUTHOR = "Arthur";

    /**
     * 生成器默认表前缀，生成类名时会自动移除这些前缀
     */
    private static final String DEFAULT_TABLE_PREFIXES = "zero_,biz_,report_";

    /**
     * 默认生成表名，直接运行main方法时使用；需要生成多张表时用逗号分隔，例如：biz_dict_group,biz_dict_item
     */
    private static final String DEFAULT_TABLES = "biz_dict_group,biz_dict_item";

    /**
     * Java源码输出目录
     */
    private static final String JAVA_OUTPUT_DIR = "src/main/java";

    /**
     * Mapper XML输出目录，直接生成到resources，避免历史项目生成后再复制的问题
     */
    private static final String XML_OUTPUT_DIR = "src/main/resources/mapper";

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 04:04
     * 说明: 执行报表服务代码生成；数据库账号密码使用代码常量，表名支持main参数、代码常量、系统属性或环境变量传入
     * @param args 表名列表，支持逗号分隔或多个参数，例如 report_user,report_order
     * @return
     * 返回值: void
     * 返回值说明: 按配置生成MyBatis-Plus标准代码文件
     */
    public static void main(String[] args)
    {
        GeneratorOptions options = GeneratorOptions.from(args);
        Path moduleRoot = resolveModuleRoot();
        Path javaOutputPath = moduleRoot.resolve(JAVA_OUTPUT_DIR);
        Path xmlOutputPath = moduleRoot.resolve(XML_OUTPUT_DIR);

        FastAutoGenerator.create(options.dbUrl(), options.username(), options.password())
                .globalConfig(builder -> {
                    builder.author(options.author())
                            .outputDir(javaOutputPath.toString())
                            .commentDate("yyyy/MM/dd HH:mm")
                            .dateType(DateType.ONLY_DATE)
                            .disableOpenDir();
                })
                .packageConfig(builder -> builder
                        .parent(options.basePackage())
                        .entity("domain")
                        .mapper("mapper")
                        .service("service")
                        .serviceImpl("service.impl")
                        .controller("controller")
                        .xml("mapper")
                        .pathInfo(Collections.singletonMap(OutputFile.xml, xmlOutputPath.toString())))
                .strategyConfig(builder -> {
                    builder.addInclude(options.tables().toArray(new String[0]));
                    if (!options.tablePrefixes().isEmpty())
                    {
                        builder.addTablePrefix(options.tablePrefixes().toArray(new String[0]));
                    }
                    var entityBuilder = builder.entityBuilder()
                            .enableLombok()
                            .enableTableFieldAnnotation();
                    var mapperBuilder = builder.mapperBuilder()
                            .enableBaseResultMap()
                            .enableBaseColumnList();
                    var serviceBuilder = builder.serviceBuilder()
                            .superServiceImplClass(ReportBaseService.class)
                            .formatServiceFileName("I%sService")
                            .formatServiceImplFileName("%sServiceImpl");
                    var controllerBuilder = builder.controllerBuilder()
                            .enableRestStyle();
                    if (options.fileOverride())
                    {
                        entityBuilder.enableFileOverride();
                        mapperBuilder.enableFileOverride();
                        serviceBuilder.enableFileOverride();
                        controllerBuilder.enableFileOverride();
                    }
                })
                .templateEngine(new VelocityTemplateEngine())
                .execute();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 01:52
     * 说明: 解析当前模块根目录，兼容在仓库根目录或zero-report模块目录中直接运行
     * @return
     * 返回值: Path
     * 返回值说明: zero-report模块根目录绝对路径
     */
    private static Path resolveModuleRoot()
    {
        Path current = Paths.get("").toAbsolutePath().normalize();
        if (isReportModuleRoot(current))
        {
            return current;
        }
        Path childModule = current.resolve("zero-report");
        if (isReportModuleRoot(childModule))
        {
            return childModule;
        }
        Path parent = current.getParent();
        while (parent != null)
        {
            Path candidate = parent.resolve("zero-report");
            if (isReportModuleRoot(candidate))
            {
                return candidate;
            }
            parent = parent.getParent();
        }
        throw new IllegalStateException("未找到zero-report模块根目录，请在项目根目录或zero-report目录中运行生成器");
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 01:52
     * 说明: 判断路径是否为zero-report模块根目录
     * @param path 待判断路径
     * @return
     * 返回值: boolean
     * 返回值说明: true-是zero-report模块根目录，false-不是
     */
    private static boolean isReportModuleRoot(Path path)
    {
        return path != null
                && Files.isRegularFile(path.resolve("pom.xml"))
                && Files.isDirectory(path.resolve(JAVA_OUTPUT_DIR))
                && "zero-report".equals(path.getFileName().toString());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 01:52
     * 说明: 按逗号解析配置项，自动过滤空白值
     * @param value 逗号分隔配置值
     * @return
     * 返回值: List<String>
     * 返回值说明: 已去除空白项的配置列表
     */
    private static List<String> parseCsv(String value)
    {
        if (value == null || value.isBlank())
        {
            return Collections.emptyList();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 01:52
     * 说明: 优先读取系统属性，其次读取环境变量，最后使用默认值
     * @param propertyName 系统属性名称
     * @param envName 环境变量名称
     * @param defaultValue 默认值
     * @return
     * 返回值: String
     * 返回值说明: 最终配置值
     */
    private static String readConfig(String propertyName, String envName, String defaultValue)
    {
        String propertyValue = System.getProperty(propertyName);
        if (propertyValue != null && !propertyValue.isBlank())
        {
            return propertyValue.trim();
        }
        String envValue = System.getenv(envName);
        if (envValue != null && !envValue.isBlank())
        {
            return envValue.trim();
        }
        return defaultValue;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 01:52
     * 说明: 代码生成器运行参数
     * @param dbUrl 数据库连接地址
     * @param username 数据库用户名
     * @param password 数据库密码
     * @param author 生成文件作者
     * @param basePackage 生成代码基础包名
     * @param tables 需要生成的表名
     * @param tablePrefixes 生成类名时移除的表前缀
     * @param fileOverride 是否覆盖已有文件，默认false
     */
    private record GeneratorOptions(String dbUrl, String username, String password, String author, String basePackage,
                                    List<String> tables, List<String> tablePrefixes, boolean fileOverride)
    {
        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/6/28 04:04
         * 说明: 从代码常量、命令行参数、系统属性和环境变量中构建生成器配置；数据库账号密码固定读取本类常量
         * @param args main方法参数，优先作为表名来源；未传入时使用DEFAULT_TABLES
         * @return
         * 返回值: GeneratorOptions
         * 返回值说明: 可直接用于FastAutoGenerator的生成器配置
         */
        private static GeneratorOptions from(String[] args)
        {
            String tablesConfig = readTablesConfig(args);
            List<String> tables = parseCsv(tablesConfig);
            if (tables.isEmpty())
            {
                throw new IllegalArgumentException("请通过main参数、DEFAULT_TABLES、-Dreport.generator.tables或REPORT_GENERATOR_TABLES指定需要生成的表名");
            }

            String dbUrl = readConfig("report.generator.db-url", "REPORT_GENERATOR_DB_URL", DEFAULT_DB_URL);
            String username = DEFAULT_DB_USERNAME;
            String password = DEFAULT_DB_PASSWORD;
            String author = readConfig("report.generator.author", "REPORT_GENERATOR_AUTHOR", DEFAULT_AUTHOR);
            String basePackage = readConfig("report.generator.base-package", "REPORT_GENERATOR_BASE_PACKAGE",
                    PackageProperties.BASE_PACKAGE);
            String tablePrefixesConfig = readConfig("report.generator.table-prefixes",
                    "REPORT_GENERATOR_TABLE_PREFIXES", DEFAULT_TABLE_PREFIXES);
            boolean fileOverride = Boolean.parseBoolean(readConfig("report.generator.file-override",
                    "REPORT_GENERATOR_FILE_OVERRIDE", "false"));

            return new GeneratorOptions(dbUrl, username, password, author, basePackage, tables,
                    parseCsv(tablePrefixesConfig), fileOverride);
        }

        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/6/28 04:02
         * 说明: 解析生成表名配置，命令行参数优先，便于临时覆盖；未传参时读取代码内默认表名，方便直接运行main方法
         * @param args main方法参数，支持多个表名或逗号分隔表名
         * @return
         * 返回值: String
         * 返回值说明: 逗号分隔的表名配置，可能来自命令行、代码常量、系统属性或环境变量
         */
        private static String readTablesConfig(String[] args)
        {
            if (args != null && args.length > 0)
            {
                return String.join(",", args);
            }
            if (DEFAULT_TABLES != null && !DEFAULT_TABLES.isBlank())
            {
                return DEFAULT_TABLES;
            }
            return readConfig("report.generator.tables", "REPORT_GENERATOR_TABLES", "");
        }
    }
}
