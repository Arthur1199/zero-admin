package com.zero.jobadmin.business.scheduler.thread;

import com.zero.jobadmin.business.model.XxlJobLogReport;
import com.zero.jobadmin.business.scheduler.config.XxlJobAdminBootstrap;
import com.zero.jobadmin.business.service.JobLogCleanup;
import com.xxl.tool.concurrent.CyclicThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * 作者: xuxueli
 * 时间: 2019/11/22
 * 说明: 定期维护历史运行报表汇总和调度日志保留周期。
 */
public class JobLogReportHelper {
    private static final Logger logger = LoggerFactory.getLogger(JobLogReportHelper.class);

    /** 每轮刷新当天及前两天的历史报表汇总。 */
    private static final int REPORT_REFRESH_DAYS = 3;

    private CyclicThread logReportThread;
    private AtomicLong lastCleanLogTime;

    /**
     *
     * 作者: xuxueli
     * 时间: 2019/11/22
     * 说明: 启动报表刷新和日志清理线程。
     * @return
     * 返回值: void
     * 返回值说明: 后台线程启动后按现有周期处理
     */
    public void start(){

        /**
         * last clean log time  （ Thread-safe concurrent reading and writing ）
          */
        lastCleanLogTime = new AtomicLong(0);

        // log report thread
        logReportThread = new CyclicThread("JobLogReportHelper#logReportThread", true, new Runnable() {
            @Override
            public void run() {

                ZoneId zone = ZoneId.systemDefault();
                refreshRecentReports(XxlJobAdminBootstrap.getInstance(), LocalDate.now(zone), zone);

                // 2、log-clean: switch open & once each day
                if (XxlJobAdminBootstrap.getInstance().getLogretentiondays()>0
                        && System.currentTimeMillis() - lastCleanLogTime.longValue() > 24*60*60*1000) {

                    // expire-time
                    Calendar expiredDay = Calendar.getInstance();
                    expiredDay.add(Calendar.DAY_OF_MONTH, -1 * XxlJobAdminBootstrap.getInstance().getLogretentiondays());
                    expiredDay.set(Calendar.HOUR_OF_DAY, 0);
                    expiredDay.set(Calendar.MINUTE, 0);
                    expiredDay.set(Calendar.SECOND, 0);
                    expiredDay.set(Calendar.MILLISECOND, 0);
                    Date clearBeforeTime = expiredDay.getTime();

                    try {
                        JobLogCleanup.clear(XxlJobAdminBootstrap.getInstance().getXxlJobLogMapper(), 0, 0, clearBeforeTime, 0);
                        lastCleanLogTime.set(System.currentTimeMillis());
                    } catch (RuntimeException e) {
                        logger.error("Failed to finish retention log cleanup", e);
                    }
                }

            }
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
        for (int offset = 0; offset < REPORT_REFRESH_DAYS; offset++) {
            LocalDate day = today.minusDays(offset);
            Date start = Date.from(day.atStartOfDay(zone).toInstant());
            Date endExclusive = Date.from(day.plusDays(1).atStartOfDay(zone).toInstant());
            try {
                Date snapshotStartedAt = new Date();
                XxlJobLogReport report = bootstrap.getXxlJobLogMapper().findLogReport(start, endExclusive);
                if (report == null) {
                    logger.warn("Missing aggregate result for job report, day={}", day);
                    continue;
                }
                report.setTriggerDay(start);
                report.setUpdateTime(snapshotStartedAt);
                bootstrap.getXxlJobLogReportMapper().saveOrUpdate(report);
            } catch (RuntimeException e) {
                logger.error("Failed to refresh job report, day={}", day, e);
            }
        }
    }

    /**
     * stop
     */
    public void stop(){
        logReportThread.stop();
    }

}
