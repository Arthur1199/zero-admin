package com.zero.framework.statistics;

import com.zero.system.domain.statistics.StatisticsOutcome;



/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/16 20:46
 * 说明: 统一分类HTTP与业务结果，业务码500不直接等于系统异常。
 */
public final class StatisticsResult
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 分类最终响应，不推断缺少证据的故障原因。
     * @param status HTTP状态
     * @param code 业务码，可空
     * @param systemError 系统异常证据
     * @param failure 已识别失败类别
     * @param json 是否JSON响应
     * @return 固定结果分类
     * 返回值: String
     * 返回值说明: 固定结果分类
     */
    public static String classify(int status, Integer code, boolean systemError, String failure, boolean json)
    {
        if (status == 401 || status == 403 || Integer.valueOf(401).equals(code) || Integer.valueOf(403).equals(code)) return StatisticsOutcome.AUTH.name();
        if (systemError || status >= 500) return StatisticsOutcome.SYSTEM.name();
        if (status < 200 || status >= 300) return StatisticsOutcome.HTTP.name();
        if (code != null && code == 200) return StatisticsOutcome.SUCCESS.name();
        if (failure != null) return failure;
        if (code != null) return StatisticsOutcome.BUSINESS.name();
        return json ? StatisticsOutcome.UNKNOWN.name() : StatisticsOutcome.SUCCESS.name();
    }
}
