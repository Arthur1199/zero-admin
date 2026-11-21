package com.zero.system.mapper;

import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.zero.system.domain.SysProfileStatistics.MemoCounts;
import com.zero.system.domain.SysProfileStatistics.MemoEntry;
import com.zero.system.domain.SysProfileStatistics.Operation;
import com.zero.system.domain.SysProfileStatistics.OperationDay;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/21 19:16
 * 说明: 只读查询个人中心统计，账号名和用户 ID 均由服务端登录态提供
 */
public interface SysProfileStatisticsMapper
{
    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/21 19:16
     * 说明: 按当前账号精确匹配，按自然日汇总操作日志
     * @param userName 当前登录账号名
     * @param start 统计起点，包含且不早于账号创建时间
     * @param end 统计终点，不包含
     * @return 有操作记录的每日计数
     */
    List<OperationDay> selectOperationDays(@Param("userName") String userName,
            @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/21 19:16
     * 说明: 查询与汇总条件一致的最近操作，只返回人员本人可用的展示字段
     * @param userName 当前登录账号名
     * @param start 统计起点，包含且不早于账号创建时间
     * @param end 统计终点，不包含
     * @return 最近五条本人操作
     */
    List<Operation> selectRecentOperations(@Param("userName") String userName,
            @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/21 19:16
     * 说明: 按创建人统计现存备忘录，日期范围仅限制新增计数
     * @param userId 当前登录用户 ID
     * @param start 新增统计起点，包含
     * @param end 新增统计终点，不包含
     * @return 总数、公开数、私有数及近期新增数
     */
    MemoCounts selectMemoCounts(@Param("userId") Long userId,
            @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/21 20:31
     * 说明: 按创建人查询最近更新的备忘录标题及时间，不读取正文
     * @param userId 当前登录用户 ID
     * @return 最近更新的最多十条备忘录
     */
    List<MemoEntry> selectRecentMemos(@Param("userId") Long userId);
}
