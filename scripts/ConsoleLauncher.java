import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/16 23:05
 * 说明: Java17源码启动器，将服务进程的stdout与stderr合并后逐字节同时写入终端和独立文件，不依赖项目或新增组件。
 */
public final class ConsoleLauncher
{
    /** 子进程读取的完整控制台文件环境变量，不接受来自Web接口的路径。 */
    private static final String CONSOLE_FILE_ENV = "ZERO_REALTIME_LOG_CONSOLE_FILE";
    /** 固定块大小，不按日志级别或行内容过滤。 */
    private static final int COPY_BYTES = 8192;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 23:05
     * 说明: 启动独立Java进程并转发所有输出，包括JVM启动期和绕过System.out的描述符写入；退出码沿用子进程。
     * @param args 新输出文件、--、Java可执行程序及其原始参数，不经shell拼接
     * @return 转发至子进程退出
     * 返回值: void
     * 返回值说明: 不输出命令参数，防止启动参数中的敏感信息进入日志
     */
    public static void main(String[] args) throws Exception
    {
        if (args.length < 4 || !"--".equals(args[1]))
            throw new IllegalArgumentException("用法: java ConsoleLauncher.java <新的控制台文件> -- <java可执行文件> <JVM参数及-jar或主类参数>");
        Path file = Path.of(args[0]).toAbsolutePath().normalize();
        String executable = Path.of(args[2]).getFileName().toString();
        if (!"java".equalsIgnoreCase(executable) && !"java.exe".equalsIgnoreCase(executable))
            throw new IllegalArgumentException("此启动器只允许启动Java服务");
        Files.createDirectories(file.getParent());
        List<String> command = new ArrayList<>();
        command.add(args[2]);
        command.add("-Dfile.encoding=UTF-8");
        command.add("-Dsun.stdout.encoding=UTF-8");
        command.add("-Dsun.stderr.encoding=UTF-8");
        for (String argument : Arrays.asList(args).subList(3, args.length))
        {
            if ((argument.startsWith("-Dfile.encoding=") || argument.startsWith("-Dsun.stdout.encoding=")
                    || argument.startsWith("-Dsun.stderr.encoding=")) && !argument.endsWith("=UTF-8"))
                throw new IllegalArgumentException("完整控制台启动器要求stdout和stderr统一使用UTF-8编码");
            command.add(argument);
        }
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true);
        builder.redirectInput(ProcessBuilder.Redirect.INHERIT);
        builder.environment().put(CONSOLE_FILE_ENV, file.toString());
        builder.environment().put("ZERO_REALTIME_LOG_CONSOLE_CHARSET", "UTF-8");
        // CREATE_NEW防止覆盖已有日志；每次运行应指定独立文件，由部署环境管理留存和磁盘配额。
        int exit;
        try (OutputStream output = Files.newOutputStream(file, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE))
        {
            Process child = builder.start();
            Thread shutdown = new Thread(child::destroy, "console-child-shutdown");
            Runtime.getRuntime().addShutdownHook(shutdown);
            try (InputStream stream = child.getInputStream())
            {
                byte[] block = new byte[COPY_BYTES];
                int length;
                while ((length = stream.read(block)) != -1)
                {
                    output.write(block, 0, length);
                    output.flush();
                    System.out.write(block, 0, length);
                    System.out.flush();
                }
                exit = child.waitFor();
            }
            finally
            {
                if (child.isAlive()) child.destroy();
                try { Runtime.getRuntime().removeShutdownHook(shutdown); }
                catch (IllegalStateException stopping) { /* 父进程退出时由已注册钩子通知子进程。 */ }
            }
        }
        System.exit(exit);
    }
}
