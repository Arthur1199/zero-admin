package com.zero.generator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.tools.ToolProvider;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/19 14:08
 * 说明: 通过隔离表验证任务代码生成、Java 编译和已有文件保护。
 */
class TaskCodeGeneratorTest {

    /** 生成器仅写入此测试的临时目录。 */
    @TempDir
    Path output;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 14:08
     * 说明: 生成实体、Mapper、XML 和 Service，校验二次生成保留源码。
     */
    @Test
    void generatesCompilableTaskServicesWithoutOverwritingFiles() throws Exception {
        String url = "jdbc:h2:mem:task_gen_" + UUID.randomUUID() + ";MODE=MySQL;DATABASE_TO_LOWER=TRUE";
        try (var connection = DriverManager.getConnection(url, "sa", "");
             var statement = connection.createStatement()) {
            statement.execute("CREATE TABLE biz_task_sample (id BIGINT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(40))");
            TaskCodeGenerator.generate(output, url, "sa", "", List.of("biz_task_sample"));

            Path javaDir = output.resolve("src/main/java/com/zero");
            Path service = javaDir.resolve("service/impl/TaskSampleServiceImpl.java");
            Path mapperXml = output.resolve("src/main/resources/mapper/TaskSampleMapper.xml");
            assertThat(javaDir.resolve("domain/TaskSample.java")).exists();
            assertThat(javaDir.resolve("mapper/TaskSampleMapper.java")).exists();
            assertThat(javaDir.resolve("service/ITaskSampleService.java")).exists();
            assertThat(javaDir.resolve("controller")).doesNotExist();
            assertThat(Files.readString(mapperXml)).contains("com.zero.mapper.TaskSampleMapper", "BaseResultMap");
            // 加入生成器不会输出的标记，才能识别二次执行是否覆盖已有源码。
            String marker = Files.readString(service) + "\n// 保留已有业务代码\n";
            Files.writeString(service, marker);
            TaskCodeGenerator.generate(output, url, "sa", "", List.of("biz_task_sample"));
            assertThat(Files.readString(service)).isEqualTo(marker).contains("@Service").doesNotContain("@Conditional");

            Path classes = Files.createDirectory(output.resolve("classes"));
            String classpath = System.getProperty("surefire.test.class.path", System.getProperty("java.class.path"));
            var compilerArgs = new ArrayList<>(List.of("--release", "17", "-encoding", "UTF-8",
                    "-classpath", classpath, "-d", classes.toString()));
            try (var sources = Files.walk(javaDir)) {
                sources.filter(path -> path.toString().endsWith(".java"))
                        .map(Path::toString).forEach(compilerArgs::add);
            }
            int exitCode = ToolProvider.getSystemJavaCompiler().run(null, null, null, compilerArgs.toArray(String[]::new));
            assertThat(exitCode).isZero();
        }
    }
}
