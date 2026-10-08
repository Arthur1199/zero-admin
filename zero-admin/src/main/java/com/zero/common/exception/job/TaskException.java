package com.zero.common.exception.job;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 计划策略异常
 */
public class TaskException extends Exception
{
    private static final long serialVersionUID = 1L;
    /**
     * 编码字段
     */

    private Code code;
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: Task异常
     * @param msg msg参数
     * @param code 编码参数
     */

    public TaskException(String msg, Code code)
    {
        this(msg, code, null);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: Task异常
     * @param msg msg参数
     * @param code 编码参数
     * @param nestedEx nestedEx参数
     */

    public TaskException(String msg, Code code, Exception nestedEx)
    {
        super(msg, nestedEx);
        this.code = code;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取编码
     * @return
     * 返回值: Code
     * 返回值说明：获取编码
     */

    public Code getCode()
    {
        return code;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 编码枚举编码
     */

    public enum Code
    {
        TASK_EXISTS, NO_TASK_EXISTS, TASK_ALREADY_STARTED, UNKNOWN, CONFIG_ERROR, TASK_NODE_NOT_AVAILABLE
    }
}