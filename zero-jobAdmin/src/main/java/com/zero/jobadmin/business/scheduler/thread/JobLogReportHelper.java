package com.zero.jobadmin.business.scheduler.thread;

import lombok.extern.slf4j.Slf4j;
import com.zero.jobadmin.business.model.XxlJobLogReport;
import com.zero.jobadmin.business.scheduler.config.XxlJobAdminBootstrap;
import com.zero.jobadmin.business.service.JobLogCleanup;
import com.xxl.tool.concurrent.CyclicThread;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 定期维护历史运行报表汇总和调度日志保留周期。
 */
@Slf4j
public class JobLogReportHelper {
    /** 每轮刷新当天及前两天的历史报表汇总。 */
    private static final int REPORT_REFRESH_DAYS = 3;
    /** 每轮额外补算的历史日期上限，避免积压占满刷新线程。 */
    private static final int HISTORY_REFRESH_BATCH = 3;
    /** 每轮保留期清理的最大批数，避免积压阻塞下一次报表刷新。 */
    private static final int RETENTION_BATCHES_PER_ROUND = 3;
    /** 历史补算游标，仅由报表线程更新，扫完后从头重查仍未完成的日期。 */
    private Date historyCursor;

    private CyclicThread logReportThread;
    /** 只累积一次保留期清理扫描的进度，由报表线程更新。 */
    private RetentionSweep retentionSweep;
    /** 已扫完的保留边界，以本地自然日判断。 */
    private LocalDate lastCompletedCutoff;
    /** 关停期间不再开始新的汇总查询或删除批次。 */
    private volatile boolean stopping;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 启动报表刷新和日志清理线程。
     * @return
     * 返回值: void
     * 返回值说明: 后台线程启动后按现有周期处理
     */
    public void start(){
        stopping = false;
        historyCursor = null;
        retentionSweep = null;
        lastCompletedCutoff = null;

        logReportThread = new CyclicThread("JobLogReportHelper#logReportThread", true, () -> {
            ZoneId zone = ZoneId.systemDefault();
            LocalDate today = LocalDate.now(zone);
            var bootstrap = XxlJobAdminBootstrap.getInstance();
            refreshRecentReports(bootstrap, today, zone);
            refreshPendingHistoricalReports(bootstrap, today, zone);
            cleanExpiredLogs(bootstrap, today, zone);
        }, 60 * 1000L, true);
        logReportThread.start();

    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 21:06
     * 说明: 用同一个自然日和时区计算最近三天的汇总范围，单日失败时继续处理其他日期。
     * @param bootstrap 调度中心组件入口
     * @param today 本轮刷新时的当地日期
     * @param zone 调度中心使用的时区
     * @return
     * 返回值: void
     * 返回值说明: 保存可用的日汇总，缺失的查询结果保留原有汇总
     */
    private void refreshRecentReports(XxlJobAdminBootstrap bootstrap, LocalDate today, ZoneId zone) {
        for (int offset = 0; offset < REPORT_REFRESH_DAYS && !stopping; offset++) {
            refreshReportDay(bootstrap, today.minusDays(offset), zone, false);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 23:07
     * 说明: 为历史进行中任务补算终态，日志已超过保留期的日期不再扫描。
     * @param bootstrap 调度中心组件入口
     * @param today 本轮刷新的自然日
     * @param zone 调度中心使用的时区
     * @return
     * 返回值: void
     * 返回值说明: 每轮最多补算一批日期，游标推进后单日失败仍可在下一轮扫描重试
     */
    private void refreshPendingHistoricalReports(XxlJobAdminBootstrap bootstrap, LocalDate today, ZoneId zone) {
        if (stopping) {
            return;
        }
        int retentionDays = bootstrap.getLogretentiondays();
        Date retentionStart = retentionDays > 0 ? Date.from(today.minusDays(retentionDays).atStartOfDay(zone).toInstant()) : null;
        Date recentStart = Date.from(today.minusDays(REPORT_REFRESH_DAYS - 1).atStartOfDay(zone).toInstant());
        try {
            List<Date> days = bootstrap.getXxlJobLogReportMapper()
                    .findPendingReportDays(retentionStart, recentStart, historyCursor, HISTORY_REFRESH_BATCH);
            for (Date day : days) {
                if (stopping) {
                    return;
                }
                refreshReportDay(bootstrap, Instant.ofEpochMilli(day.getTime()).atZone(zone).toLocalDate(), zone, true);
                historyCursor = day;
            }
            if (days.size() < HISTORY_REFRESH_BATCH) {
                historyCursor = null;
            }
        } catch (RuntimeException e) {
            log.error("Failed to scan historical job reports", e);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 23:09
     * 说明: 按当前保留边界分批清理旧日志，同一天完成后不重复扫表。
     * @param bootstrap 调度中心组件入口
     * @param today 本轮刷新时的当地日期
     * @param zone 用于计算保留边界的时区
     * @return
     * 返回值: void
     * 返回值说明: 待处理批次留给下一轮，停止后不再提交新批次
     */
    private void cleanExpiredLogs(XxlJobAdminBootstrap bootstrap, LocalDate today, ZoneId zone) {
        int retentionDays = bootstrap.getLogretentiondays();
        if (stopping || retentionDays <= 0) {
            return;
        }
        LocalDate cutoff = today.minusDays(retentionDays);
        if (cutoff.equals(lastCompletedCutoff)) {
            return;
        }
        var mapper = bootstrap.getXxlJobLogMapper();
        try {
            if (retentionSweep == null || !cutoff.equals(retentionSweep.cutoff())) {
                Long maxId = mapper.findMaxClearLogId(0, 0);
                if (maxId == null) {
                    lastCompletedCutoff = cutoff;
                    return;
                }
                retentionSweep = new RetentionSweep(cutoff, 0, maxId);
            }
            Date before = Date.from(cutoff.atStartOfDay(zone).toInstant());
            for (int batch = 0; batch < RETENTION_BATCHES_PER_ROUND && !stopping; batch++) {
                long maxId = retentionSweep.maxId();
                long afterId = JobLogCleanup.clearBatch(mapper, 0, 0, before, 0, retentionSweep.afterId(), maxId);
                if (afterId == maxId) {
                    lastCompletedCutoff = cutoff;
                    retentionSweep = null;
                    return;
                }
                retentionSweep = new RetentionSweep(cutoff, afterId, maxId);
            }
        } catch (RuntimeException e) {
            log.error("Failed to finish retention log cleanup, cutoff={}", cutoff, e);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 23:09
     * 说明: 记录保留期清理的日期边界、已处理位置和开始时的日志 ID 上限。
     */
    private record RetentionSweep(LocalDate cutoff, long afterId, long maxId) {
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 23:07
     * 说明: 按本地自然日汇总日志，历史补算发现明细已被清空时保留原有报表。
     * @param bootstrap 调度中心组件入口
     * @param day 待汇总日期
     * @param zone 调度中心使用的时区
     * @param historical 是否补算历史报表
     * @return
     * 返回值: void
     * 返回值说明: 保存实际明细的统计结果，单日查询异常时写入日志
     */
    private void refreshReportDay(XxlJobAdminBootstrap bootstrap, LocalDate day, ZoneId zone, boolean historical) {
        Date start = Date.from(day.atStartOfDay(zone).toInstant());
        Date endExclusive = Date.from(day.plusDays(1).atStartOfDay(zone).toInstant());
        try {
            Date snapshotStartedAt = new Date();
            XxlJobLogReport report = bootstrap.getXxlJobLogMapper().findLogReport(start, endExclusive);
            if (report == null) {
                log.warn("Missing aggregate result for job report, day={}", day);
                return;
            }
            if (historical && report.getRunningCount() == 0 && report.getSucCount() == 0 && report.getFailCount() == 0) {
                return;
            }
            report.setTriggerDay(start);
            report.setUpdateTime(snapshotStartedAt);
            bootstrap.getXxlJobLogReportMapper().saveOrUpdate(report);
        } catch (RuntimeException e) {
            log.error("Failed to refresh job report, day={}", day, e);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 22:42
     * 说明: 通知报表线程在当前批次结束后停止，兼容调度中心启动回滚。
     * @return
     * 返回值: void
     * 返回值说明: 已启动的报表刷新线程停止运行
     */
    public void stop(){
        stopping = true;
        if (logReportThread != null) {
            logReportThread.stop();
        }
    }

}
