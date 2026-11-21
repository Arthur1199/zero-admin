package com.zero.system.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/21 19:16
 * 说明: 个人中心的统计结果及列表投影，仅包含展示需要的字段
 */
public final class SysProfileStatistics
{
    private SysProfileStatistics()
    {
    }

    /** 当前请求的网络及设备信息，在线秒数缺失表示旧会话没有保存首次登录时间。 */
    public record Session(Long onlineSeconds, String ip, String location, String browser, String os)
    {
    }

    /** 每日操作总量及成功、失败次数，日期按后台时区划分。 */
    public record OperationDay(LocalDate day, long total, long succeeded, long failed)
    {
    }

    /** 操作日志的展示字段，状态及动作沿用操作日志字典。 */
    public record Operation(String title, int businessType, int status, LocalDateTime operTime)
    {
    }

    /** 近七个自然日操作汇总、逐日统计及最近操作。 */
    public record Operations(long total, long succeeded, long failed, List<OperationDay> days, List<Operation> recent)
    {
    }

    /** 当前用户的备忘录总量、私有、公开及近七日新增数量。 */
    public record MemoCounts(long total, long privateCount, long publicCount, long newCount)
    {
    }

    /** 备忘录标题及最后修改时间，未修改时使用创建时间。 */
    public record MemoEntry(String title, LocalDateTime updatedAt)
    {
    }

    /** 当前用户自己创建的备忘录统计及最近更新列表。 */
    public record Memos(MemoCounts counts, List<MemoEntry> recent)
    {
    }
}
