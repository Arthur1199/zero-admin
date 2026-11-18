package com.zero.jobadmin.business.service;

import com.zero.jobadmin.business.mapper.XxlJobLogMapper;

import java.util.Date;
import java.util.List;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/18 21:25
 * 说明: 手动清理和保留期清理共用的日志批处理。
 */
public final class JobLogCleanup {

    /** 单批删除的日志上限。 */
    private static final int BATCH_SIZE = 1000;

    private JobLogCleanup() {
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 21:25
     * 说明: 按日志 ID 批量推进，保留开始清理后新增的日志。
     * @param mapper 调度日志 Mapper
     * @param jobGroup 已校验权限的执行器组 ID，后台保留期清理传 0
     * @param jobId 任务 ID，0 表示范围内全部任务
     * @param before 清理时间上限，可为空
     * @param keepCount 保留最新的记录数，0 表示不按数量保留
     * @return
     * 返回值: void
     * 返回值说明: 分批提交，失败前已删除的记录不会回滚
     */
    public static void clear(XxlJobLogMapper mapper, int jobGroup, int jobId, Date before, int keepCount) {
        Long maxId = mapper.findMaxClearLogId(jobGroup, jobId);
        if (maxId == null) {
            return;
        }
        long afterId = 0;
        while (afterId < maxId) {
            afterId = clearBatch(mapper, jobGroup, jobId, before, keepCount, afterId, maxId);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 23:09
     * 说明: 在既定日志 ID 上限内清理一批，并返回可用于下次续跑的游标。
     * @param mapper 调度日志 Mapper
     * @param jobGroup 已授权的执行器组 ID，后台保留期清理传 0
     * @param jobId 任务 ID，0 表示范围内全部任务
     * @param before 清理时间上限，可为空
     * @param keepCount 保留最新的日志数，0 表示不按数量保留
     * @param afterId 上一批处理到的日志 ID，首批传 0
     * @param maxId 本轮开始时确定的日志 ID 上限
     * @return
     * 返回值: long
     * 返回值说明: 返回本批最后一个 ID，无匹配日志时返回上限标记完成
     */
    public static long clearBatch(XxlJobLogMapper mapper, int jobGroup, int jobId, Date before, int keepCount,
                                  long afterId, long maxId) {
        List<Long> ids = mapper.findClearLogIds(jobGroup, jobId, before, keepCount, BATCH_SIZE, afterId, maxId);
        if (ids.isEmpty()) {
            return maxId;
        }
        long nextId = ids.get(ids.size() - 1);
        if (nextId <= afterId || nextId > maxId) {
            throw new IllegalStateException("Invalid log cleanup cursor");
        }
        mapper.clearLog(ids);
        // 其他实例可能已删除这批记录，仍从本批最后一个 ID 继续。
        return nextId;
    }
}
