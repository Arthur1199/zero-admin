package com.zero.system.mapper;

import com.zero.system.domain.statistics.StatEvent;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/16 20:46
 * 说明: 运行统计Mapper，仅访问zero_admin统计表。
 */
public interface StatisticsMapper
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 写入MySQL回退事件。
     * @param event 事件、查询条件或汇总增量
     * @return 查询结果或受影响行数
     * 返回值: int
     * 返回值说明: 查询结果或受影响行数
     */
    int insertInbox(StatEvent event);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 按创建时间和事件编号继续扫描回退队列，轮次结束后从头重试失败记录，不受部门数据权限影响。
     * @param query limit为本轮上限，afterTime及afterId为可选的上一批末尾游标
     * @return 带游标的未完成事件
     * 返回值: java.util.List<StatInboxEntry>
     * 返回值说明: 保留原始事件编号和时间，正文无效也能继续读取后续记录
     */
    java.util.List<com.zero.system.domain.statistics.StatInboxEntry> pendingInbox(java.util.Map<String, Object> query);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 标记回退事件处理完成。
     * @param eventId 事件、查询条件或汇总增量
     * @return 查询结果或受影响行数
     * 返回值: int
     * 返回值说明: 查询结果或受影响行数
     */
    int markInbox(String eventId);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 登记唯一事件，重复时返回0。
     * @param eventId 事件、查询条件或汇总增量
     * @param consumedAt 本次入库的北京时间，避免依赖数据库会话时区
     * @return 查询结果或受影响行数
     * 返回值: int
     * 返回值说明: 查询结果或受影响行数
     */
    int consume(@org.apache.ibatis.annotations.Param("eventId") String eventId,
                @org.apache.ibatis.annotations.Param("consumedAt") java.time.LocalDateTime consumedAt);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 登记接口模板。
     * @param row 事件、查询条件或汇总增量
     * @return 查询结果或受影响行数
     * 返回值: int
     * 返回值说明: 查询结果或受影响行数
     */
    int endpoint(java.util.Map<String, Object> row);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 原子累加分钟接口统计。
     * @param row 事件、查询条件或汇总增量
     * @return 查询结果或受影响行数
     * 返回值: int
     * 返回值说明: 查询结果或受影响行数
     */
    int apiMinute(java.util.Map<String, Object> row);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 原子累加日接口统计。
     * @param row 事件、查询条件或汇总增量
     * @return 查询结果或受影响行数
     * 返回值: int
     * 返回值说明: 查询结果或受影响行数
     */
    int apiDay(java.util.Map<String, Object> row);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 累加最终登录结果。
     * @param row 事件、查询条件或汇总增量
     * @return 查询结果或受影响行数
     * 返回值: int
     * 返回值说明: 查询结果或受影响行数
     */
    int loginDay(java.util.Map<String, Object> row);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 累加每日用户活动。
     * @param row 事件、查询条件或汇总增量
     * @return 查询结果或受影响行数
     * 返回值: int
     * 返回值说明: 查询结果或受影响行数
     */
    int userDay(java.util.Map<String, Object> row);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 保存在线采样和分钟采样峰值。
     * @param row 事件、查询条件或汇总增量
     * @return 查询结果或受影响行数
     * 返回值: int
     * 返回值说明: 查询结果或受影响行数
     */
    int online(java.util.Map<String, Object> row);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 22:02
     * 说明: 从已持久化的用户活动查询近五分钟去重人数，跨零点同时读取相邻两天，不额外写Redis索引。
     * @param query beginDay和endDay限制日期，beginAt为不含边界起点，endAt为含边界终点
     * @return 最近活跃用户去重数量
     * 返回值: long
     * 返回值说明: 仅统计已入库事件，可能有正常消费延迟；失败交给采样任务报告，不伪造零值
     */
    long countRecentActive(java.util.Map<String, Object> query);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 查询今日汇总。
     * @param query 事件、查询条件或汇总增量
     * @return 查询结果或受影响行数
     * 返回值: java.util.Map<String, Object>
     * 返回值说明: 查询结果或受影响行数
     */
    java.util.Map<String, Object> overview(java.util.Map<String, Object> query);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 查询趋势，不伪造缺失在线采样。
     * @param query 事件、查询条件或汇总增量
     * @return 查询结果或受影响行数
     * 返回值: java.util.List<java.util.Map<String, Object>>
     * 返回值说明: 查询结果或受影响行数
     */
    java.util.List<java.util.Map<String, Object>> trend(java.util.Map<String, Object> query);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 查询模块及失败分布。
     * @param query 事件、查询条件或汇总增量
     * @return 查询结果或受影响行数
     * 返回值: java.util.List<java.util.Map<String, Object>>
     * 返回值说明: 查询结果或受影响行数
     */
    java.util.List<java.util.Map<String, Object>> distribution(java.util.Map<String, Object> query);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 按白名单排序查询接口汇总，不受部门数据范围过滤。
     * @param query 事件、查询条件或汇总增量
     * @return 查询结果或受影响行数
     * 返回值: java.util.List<java.util.Map<String, Object>>
     * 返回值说明: 查询结果或受影响行数
     */
    java.util.List<java.util.Map<String, Object>> list(java.util.Map<String, Object> query);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 查询接口汇总条数。
     * @param query 事件、查询条件或汇总增量
     * @return 查询结果或受影响行数
     * 返回值: long
     * 返回值说明: 查询结果或受影响行数
     */
    long countEndpoints(java.util.Map<String, Object> query);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 读取采集起点与待处理数量。
     * @return 查询结果或受影响行数
     * 返回值: java.util.Map<String, Object>
     * 返回值说明: 查询结果或受影响行数
     */
    java.util.Map<String, Object> health();
}
