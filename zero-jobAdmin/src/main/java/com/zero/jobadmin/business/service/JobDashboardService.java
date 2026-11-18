package com.zero.jobadmin.business.service;

import com.zero.jobadmin.business.mapper.JobDashboardMapper;
import com.zero.jobadmin.business.model.dto.JobDashboardReport;
import com.zero.jobadmin.business.model.dto.JobDashboardReport.DayResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/18 19:08
 * 说明: 聚合 ZERO-JOB 留存的调度明细，统一运行报表三块数据的统计口径
 */
@Service
public class JobDashboardService {

    /** 运行报表与调度中心按上海时区划分自然日。 */
    public static final ZoneId REPORT_ZONE = ZoneId.of("Asia/Shanghai");

    /** 单次查询允许包含的最大自然日数。 */
    public static final int MAX_DAYS = 31;

    /** 待回调列表每页返回条数。 */
    private static final int PAGE_SIZE = 10;

    private final JobDashboardMapper mapper;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 19:08
     * 说明: 注入运行报表查询 Mapper
     * @param mapper 调度明细只读查询
     */
    public JobDashboardService(JobDashboardMapper mapper) {
        this.mapper = mapper;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 19:08
     * 说明: 使用同一数据库快照返回趋势、失败榜和待回调记录，组权限由调用方校验
     * @param startDate 首日，包含
     * @param endDate 末日，包含
     * @param groups 当前账号有权查看的执行器组 ID
     * @param page 待回调列表页码，从 1 开始
     * @return
     * 返回值: JobDashboardReport
     * 返回值说明: 对应日期和执行器组范围的报表数据
     */
    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public JobDashboardReport report(LocalDate startDate, LocalDate endDate, List<Integer> groups, int page) {
        LocalDateTime asOf = LocalDateTime.now(REPORT_ZONE);
        LocalDateTime from = startDate.atStartOfDay();
        LocalDateTime until = endDate.plusDays(1).atStartOfDay();
        var byDay = (groups.isEmpty() ? List.<DayResult>of() : mapper.trend(from, until, groups))
                .stream().collect(Collectors.toMap(DayResult::day, Function.identity()));
        List<DayResult> trend = startDate.datesUntil(until.toLocalDate())
                .map(day -> byDay.getOrDefault(day, new DayResult(day, 0, 0, 0, 0)))
                .toList();

        long pendingTotal = trend.stream().mapToLong(DayResult::pending).sum();
        int actualPage = (int) Math.min(page, Math.max(1, (pendingTotal + PAGE_SIZE - 1) / PAGE_SIZE));
        long offset = (long) (actualPage - 1) * PAGE_SIZE;
        return new JobDashboardReport(trend,
                groups.isEmpty() ? List.of() : mapper.topFailures(from, until, groups),
                pendingTotal == 0 ? List.of() : mapper.pending(from, until, groups, offset, PAGE_SIZE),
                pendingTotal, actualPage, PAGE_SIZE, asOf);
    }
}
