package com.zero.framework.realtimelog;

import ch.qos.logback.classic.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import static org.assertj.core.api.Assertions.*;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/16 23:05
 * 说明: 完整控制台隔离回归，验证原文、字节续读、UTF-8拆分、文件重置及真实JVM原生输出。
 */
class ConsoleFileReaderTest
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 23:05
     * 说明: 原文不补换行、不脱敏、不截断，长行以多块传输，续读无重复。
     * @param directory 临时目录
     * @return 验证字节游标和完整原文
     * 返回值: void
     * 返回值说明: 只使用虚构文本
     */
    @Test void rawTextAndIncrementalChunks(@TempDir Path directory) throws Exception
    {
        Path file = directory.resolve("console.log");
        String original = "stdout中文😀\r\nstderr\n\npassword=fictional-value\n" + "长行😀".repeat(20000);
        Files.writeString(file, original, StandardCharsets.UTF_8);
        ConsoleFileReader reader = new ConsoleFileReader(file.toString(), "UTF-8", 1024 * 1024, 512);
        Map<String, Object> snapshot = reader.read(null, null, 500);
        assertThat(text(snapshot)).isEqualTo(original);
        assertThat(snapshot).containsEntry("sourceMode", "console-file").containsEntry("nextCursor", Files.size(file));
        assertThat(events(snapshot)).allSatisfy(event -> assertThat(event).containsEntry("raw", true).containsEntry("truncated", false));
        Files.writeString(file, "接续无换行", StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        Map<String, Object> next = reader.read((String) snapshot.get("processId"), (Long) snapshot.get("nextCursor"), 500);
        assertThat(text(next)).isEqualTo("接续无换行");
        assertThat(text(reader.read((String) next.get("processId"), (Long) next.get("nextCursor"), 500))).isEmpty();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 23:05
     * 说明: 文件尾部只有半个UTF-8字符时等待后续字节，避免乱码或遗漏。
     * @param directory 临时目录
     * @return 验证半字符和截断重置
     * 返回值: void
     * 返回值说明: 不使用固定等待或外部依赖
     */
    @Test void partialCharacterAndTruncation(@TempDir Path directory) throws Exception
    {
        Path file = directory.resolve("console.log");
        Files.write(file, new byte[] { (byte) 0xe4, (byte) 0xb8 });
        ConsoleFileReader reader = new ConsoleFileReader(file.toString(), "UTF-8", 1024, 512);
        Map<String, Object> first = reader.read(null, null, 500);
        assertThat(text(first)).isEmpty();
        assertThat(first).containsEntry("nextCursor", 0L);
        Files.write(file, new byte[] { (byte) 0xad, '\n' }, StandardOpenOption.APPEND);
        Map<String, Object> complete = reader.read((String) first.get("processId"), 0L, 500);
        assertThat(text(complete)).isEqualTo("中\n");
        Files.writeString(file, "x", StandardCharsets.UTF_8);
        Map<String, Object> reset = reader.read((String) complete.get("processId"), (Long) complete.get("nextCursor"), 500);
        assertThat(reset).containsEntry("reset", true);
        assertThat(text(reset)).isEqualTo("x");
        assertThatThrownBy(() -> new ConsoleFileReader(directory.resolve("missing").toString(), "UTF-8", 1024, 512)
                .read(null, null, 500)).hasMessageContaining("无法读取");
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 23:05
     * 说明: 完整模式只读文件，禁止将同一Logback事件重复加入页面。
     * @param directory 临时目录
     * @return 验证唯一数据来源
     * 返回值: void
     * 返回值说明: 不改变全局日志上下文
     */
    @Test void appenderUsesOnlyConsoleFile(@TempDir Path directory) throws Exception
    {
        Path file = directory.resolve("console.log");
        Files.writeString(file, "真实进程输出\n", StandardCharsets.UTF_8);
        LoggerContext context = new LoggerContext();
        RealtimeLogAppender appender = new RealtimeLogAppender();
        appender.setContext(context); appender.setName(RealtimeLogBuffer.APPENDER_NAME);
        appender.setConsoleFile(file.toString()); appender.start();
        context.getLogger(Logger.ROOT_LOGGER_NAME).addAppender(appender);
        try
        {
            context.getLogger("test.file.mode").info("不得重复入队的Logback事件");
            assertThat(text(RealtimeLogBuffer.readActive(context, null, null, 500))).isEqualTo("真实进程输出\n");
        }
        finally { context.stop(); }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 23:05
     * 说明: 启动独立Java源码进程，覆盖标准流、底层描述符及-Xlog:gc的原生输出，终端和文件逐字节一致。
     * @param directory 临时目录
     * @return 验证真实进程镜像及退出码
     * 返回值: void
     * 返回值说明: 不启动业务服务，不连接数据库或修改当前IDEA进程
     */
    @Test void launcherMirrorsRealJvmOutput(@TempDir Path directory) throws Exception
    {
        Path probe = directory.resolve("ConsoleProbe.java");
        Files.writeString(probe, """
                import java.io.*;
                import java.nio.charset.StandardCharsets;
                class ConsoleProbe {
                    public static void main(String[] args) throws Exception {
                        System.out.print("stdout中文"); System.out.flush();
                        System.err.print("stderr中文\\n\\n"); System.err.flush();
                        new FileOutputStream(FileDescriptor.out).write("descriptor-out\\n".getBytes(StandardCharsets.UTF_8));
                        new FileOutputStream(FileDescriptor.err).write("descriptor-err\\n".getBytes(StandardCharsets.UTF_8));
                        System.gc();
                        System.exit(7);
                    }
                }
                """, StandardCharsets.UTF_8);
        Path console = directory.resolve("process.log"), terminal = directory.resolve("terminal.log");
        String java = Path.of(System.getProperty("java.home"), "bin", System.getProperty("os.name").startsWith("Windows") ? "java.exe" : "java").toString();
        Process child = new ProcessBuilder(java, Path.of("../scripts/ConsoleLauncher.java").toAbsolutePath().toString(),
                console.toString(), "--", java, "-Xlog:gc", probe.toString())
                .redirectErrorStream(true).redirectOutput(terminal.toFile()).start();
        try
        {
            assertThat(child.waitFor(40, TimeUnit.SECONDS)).isTrue();
            assertThat(child.exitValue()).isEqualTo(7);
            assertThat(Files.readAllBytes(console)).isEqualTo(Files.readAllBytes(terminal));
            String original = Files.readString(console, StandardCharsets.UTF_8);
            assertThat(original).contains("stdout中文stderr中文\n\n", "descriptor-out", "descriptor-err", "[info][gc]");
            assertThat(text(new ConsoleFileReader(console.toString(), "UTF-8", 1024 * 1024, 512).read(null, null, 500)))
                    .isEqualTo(original);
        }
        finally { if (child.isAlive()) child.destroy(); }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 23:05
     * 说明: 提取只读事件集合。
     * @param snapshot 增量响应
     * @return 事件集合
     * 返回值: List<Map<String, Object>>
     * 返回值说明: 测试内读取
     */
    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> events(Map<String, Object> snapshot)
    { return (List<Map<String, Object>>) snapshot.get("events"); }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 23:05
     * 说明: 原样拼接块，不额外添加换行。
     * @param snapshot 增量响应
     * @return 完整文本
     * 返回值: String
     * 返回值说明: 用于逐字比较
     */
    private static String text(Map<String, Object> snapshot)
    { return events(snapshot).stream().map(event -> (String) event.get("text")).collect(java.util.stream.Collectors.joining()); }
}
