package com.zero.jobadmin.business.mapper;

import com.zero.jobadmin.business.model.dto.JobDashboardReport.DayResult;
import com.zero.jobadmin.business.model.dto.JobDashboardReport.FailedJob;
import com.zero.jobadmin.business.model.dto.JobDashboardReport.PendingJob;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/18 19:08
 * 说明: 从调度日志明细聚合运行报表，查询必须限定日期和已授权的执行器组
 */
@Mapper
public interface JobDashboardMapper {

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 19:08
     * 说明: 按调度日期汇总四类结果
     * @param from 首日零点，包含
     * @param until 末日次日零点，不包含
     * @param groups 当前用户可查询的执行器组 ID
     * @return
     * 返回值: List<DayResult>
     * 返回值说明: 仅返回存在调度记录的日期
     */
    List<DayResult> trend(@Param("from") LocalDateTime from, @Param("until") LocalDateTime until,
                          @Param("groups") List<Integer> groups);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 19:08
     * 说明: 按组和任务汇总失败，榜单最多返回十条
     * @param from 首日零点，包含
     * @param until 末日次日零点，不包含
     * @param groups 当前用户可查询的执行器组 ID
     * @return
     * 返回值: List<FailedJob>
     * 返回值说明: 按失败次数降序返回任务
     */
    List<FailedJob> topFailures(@Param("from") LocalDateTime from, @Param("until") LocalDateTime until,
                               @Param("groups") List<Integer> groups);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 19:08
     * 说明: 查询已派发且待回调的日志分页
     * @param from 首日零点，包含
     * @param until 末日次日零点，不包含
     * @param groups 当前用户可查询的执行器组 ID
     * @param offset 跳过的记录数
     * @param limit 本页最多返回的记录数
     * @return
     * 返回值: List<PendingJob>
     * 返回值说明: 按等待时间降序返回明细
     */
    List<PendingJob> pending(@Param("from") LocalDateTime from, @Param("until") LocalDateTime until,
                             @Param("groups") List<Integer> groups, @Param("offset") long offset,
                             @Param("limit") int limit);
}
