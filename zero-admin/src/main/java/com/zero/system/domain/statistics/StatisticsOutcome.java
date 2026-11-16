package com.zero.system.domain.statistics;



/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/16 20:46
 * 说明: 运行统计结果分类，稳定名称用于持久化事件及汇总字段映射。
 */
public enum StatisticsOutcome
{
    /** HTTP与业务结果均成功。 */
    SUCCESS,
    /** 可预期业务拒绝，不等同于程序故障。 */
    BUSINESS,
    /** 未认证或无操作权限。 */
    AUTH,
    /** 系统异常或HTTP服务端失败。 */
    SYSTEM,
    /** 其他非成功HTTP状态。 */
    HTTP,
    /** 统一响应缺少可识别业务结果。 */
    UNKNOWN
}
