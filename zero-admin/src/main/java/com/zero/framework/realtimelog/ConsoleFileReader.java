package com.zero.framework.realtimelog;

import com.zero.common.exception.ServiceException;
import java.io.IOException;
import java.nio.*;
import java.nio.channels.FileChannel;
import java.nio.charset.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/16 23:05
 * 说明: 从管理员配置的进程控制台文件增量读取原始文本，覆盖stdout、stderr及写入相同描述符的JVM原生输出。
 */
public final class ConsoleFileReader
{
    /** 单次磁盘读取上限，避免日志积压时长时间占用请求线程。 */
    private static final int BATCH_BYTES = 512 * 1024;
    /** 仅由服务启动配置指定，不接受HTTP文件路径。 */
    private final Path path;
    /** 必须与控制台文件实际编码一致。 */
    private final Charset charset;
    /** 首次最多回看字节和每个文本块上限，不对块内原文做截断或脱敏。 */
    private final int initialBytes, chunkBytes;
    /** 当前文件代次；替换、截断后通知页面重新建立游标。 */
    private String generation = UUID.randomUUID().toString();
    /** 用于识别文件轮转，避免把旧偏移应用到新文件。 */
    private Object identity;
    /** 最近观察长度，截断时使旧游标失效。 */
    private long observedSize = -1;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 23:05
     * 说明: 固定读取来源和编码，构造阶段不读取文件，允许启动器尚未开始写入。
     * @param file 启动配置中的控制台文件
     * @param encoding 文件编码，默认UTF-8
     * @param maxBytes 首次回看预算，限制1KB至100MB
     * @param maxEventBytes 每块字节上限，限制512B至1MB
     */
    public ConsoleFileReader(String file, String encoding, int maxBytes, int maxEventBytes)
    {
        path = Path.of(file).toAbsolutePath().normalize();
        charset = Charset.forName(encoding == null || encoding.isBlank() ? "UTF-8" : encoding.trim());
        initialBytes = Math.max(1024, Math.min(maxBytes, 100 * 1024 * 1024));
        chunkBytes = Math.max(512, Math.min(maxEventBytes, 1024 * 1024));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 23:05
     * 说明: 使用字节偏移续读，保留换行、空行和无换行输出；不再次采集Logback事件，避免重复。
     * @param processId 上次文件代次，首次为空
     * @param after 上次返回的完整字符边界字节偏移
     * @param limit 每批块数，限制1至500
     * @return 原始控制台文本块与续读状态
     * 返回值: Map<String, Object>
     * 返回值说明: raw=true表示不得为文本块额外补换行；文件不可读时明确失败，不回退不完整日志
     */
    public synchronized Map<String, Object> read(String processId, Long after, int limit)
    {
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ))
        {
            BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);
            if (!attributes.isRegularFile()) throw new IOException("not-regular-file");
            Object key = attributes.fileKey() == null ? attributes.creationTime() : attributes.fileKey();
            long size = channel.size();
            if (identity != null && (!identity.equals(key) || size < observedSize)) generation = UUID.randomUUID().toString();
            identity = key;
            observedSize = size;
            boolean reset = processId != null && !processId.isBlank() && !generation.equals(processId);
            boolean initial = after == null || processId == null || processId.isBlank() || reset;
            long offset = initial ? Math.max(0, size - initialBytes) : Math.max(0, after);
            boolean gap = initial && offset > 0;
            if (offset > size) { offset = Math.max(0, size - initialBytes); gap = true; }
            // 首次从文件中间回看时对齐到下一行，避免从UTF-8或GBK半个字符开始。
            if ((initial || gap) && offset > 0) offset = align(channel, offset, size);
            ByteBuffer bytes = ByteBuffer.allocate((int) Math.min(BATCH_BYTES, size - offset));
            channel.position(offset);
            while (bytes.hasRemaining() && channel.read(bytes) > 0) { }
            bytes.flip();
            List<Map<String, Object>> events = new ArrayList<>();
            int count = Math.max(1, Math.min(limit, RealtimeLogBuffer.BATCH_SIZE));
            while (bytes.hasRemaining() && events.size() < count)
            {
                ByteBuffer part = bytes.slice();
                part.limit(Math.min(part.remaining(), chunkBytes));
                CharBuffer text = CharBuffer.allocate(part.remaining() + 1);
                CharsetDecoder decoder = charset.newDecoder().onMalformedInput(CodingErrorAction.REPLACE)
                        .onUnmappableCharacter(CodingErrorAction.REPLACE);
                // endOfInput=false：文件末尾尚未写完整的多字节字符留到下一次，不生成乱码或丢字节。
                decoder.decode(part, text, false);
                if (part.position() == 0) break;
                bytes.position(bytes.position() + part.position());
                text.flip();
                events.add(Map.of("sequence", offset + bytes.position(), "timestamp", System.currentTimeMillis(),
                        "level", "CONSOLE", "thread", "", "logger", "", "text", text.toString(), "raw", true, "truncated", false));
            }
            long next = offset + bytes.position();
            return Map.of("events", events, "processId", generation, "nextCursor", next,
                    "hasMore", next < size && size - next > 8, "gap", gap, "reset", reset, "sourceMode", "console-file");
        }
        catch (IOException | SecurityException error)
        {
            // 不输出文件内容和路径，不用日志框架记录读取失败，避免制造控制台反馈。
            throw new ServiceException("完整控制台文件尚未就绪或无法读取，请检查启动器、IDEA输出文件及服务读取权限");
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 23:05
     * 说明: 从回看窗口中间进入时跳过半行；超长无换行文本仅跳过UTF-8续字节，不丢弃整段。
     * @param channel 已打开文件
     * @param offset 初始偏移
     * @param size 本次观察长度
     * @return 可读取边界
     * 返回值: long
     * 返回值说明: 最大扫描4KB，不全文件搜索
     */
    private long align(FileChannel channel, long offset, long size) throws IOException
    {
        ByteBuffer sample = ByteBuffer.allocate((int) Math.min(4096, size - offset));
        channel.read(sample, offset);
        sample.flip();
        for (int index = 0; index < sample.limit(); index++) if (sample.get(index) == '\n') return offset + index + 1;
        if (charset.equals(StandardCharsets.UTF_8))
            while (sample.hasRemaining() && (sample.get(sample.position()) & 0xC0) == 0x80) { sample.get(); offset++; }
        return offset;
    }
}
