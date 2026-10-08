package com.zero.common.constant;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 任务调度通用常量
 */
public class ScheduleConstants
{
    /**
     * TASKCLASS名称字段
     */
    public static final String TASK_CLASS_NAME = "TASK_CLASS_NAME";

    /**
     * 执行目标key
     */
    public static final String TASK_PROPERTIES = "TASK_PROPERTIES";

    /**
     * 默认
     */
    public static final String MISFIRE_DEFAULT = "0";

    /**
     * 立即触发执行
     */
    public static final String MISFIRE_IGNORE_MISFIRES = "1";

    /**
     * 触发一次执行
     */
    public static final String MISFIRE_FIRE_AND_PROCEED = "2";

    /**
     * 不触发立即执行
     */
    public static final String MISFIRE_DO_NOTHING = "3";
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 状态枚举状态
     */

    public enum Status
    {
        /**
         * 正常
         */
        NORMAL("0"),
        /**
         * 暂停
         */
        PAUSE("1");
        /**
         * 值字段
         */

        private String value;

        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/5/16 07:16
         * 说明: 定时任务状态枚举构造方法
         * @param value 状态值，0-正常，1-暂停
         * @return
         * 返回值: Status
         * 返回值说明：创建定时任务状态枚举
         */
        private Status(String value)
        {
            this.value = value;
        }
        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/5/14 00:58
         * 说明: 获取值
         * @return
         * 返回值: String
         * 返回值说明：获取值
         */

        public String getValue()
        {
            return value;
        }
    }
}
