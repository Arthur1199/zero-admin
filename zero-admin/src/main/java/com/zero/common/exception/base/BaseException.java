package com.zero.common.exception.base;

import com.zero.common.utils.MessageUtils;
import com.zero.common.utils.StringUtils;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 基础异常
 */
public class BaseException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    /**
     * 所属模块
     */
    private String module;

    /**
     * 错误码
     */
    private String code;

    /**
     * 错误码对应的参数
     */
    private Object[] args;

    /**
     * 错误消息
     */
    private String defaultMessage;
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: Base异常
     * @param module module参数
     * @param code 编码参数
     * @param args args参数
     * @param defaultMessage default消息参数
     */

    public BaseException(String module, String code, Object[] args, String defaultMessage)
    {
        this.module = module;
        this.code = code;
        this.args = args;
        this.defaultMessage = defaultMessage;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: Base异常
     * @param module module参数
     * @param code 编码参数
     * @param args args参数
     */

    public BaseException(String module, String code, Object[] args)
    {
        this(module, code, args, null);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: Base异常
     * @param module module参数
     * @param defaultMessage default消息参数
     */

    public BaseException(String module, String defaultMessage)
    {
        this(module, null, null, defaultMessage);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: Base异常
     * @param code 编码参数
     * @param args args参数
     */

    public BaseException(String code, Object[] args)
    {
        this(null, code, args, null);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: Base异常
     * @param defaultMessage default消息参数
     */

    public BaseException(String defaultMessage)
    {
        this(null, null, null, defaultMessage);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取消息
     * @return
     * 返回值: String
     * 返回值说明：获取消息
     */

    @Override
    public String getMessage()
    {
        String message = null;
        if (!StringUtils.isEmpty(code))
        {
            message = MessageUtils.message(code, args);
        }
        if (message == null)
        {
            message = defaultMessage;
        }
        return message;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Module
     * @return
     * 返回值: String
     * 返回值说明：获取Module
     */

    public String getModule()
    {
        return module;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取编码
     * @return
     * 返回值: String
     * 返回值说明：获取编码
     */

    public String getCode()
    {
        return code;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Args
     * @return
     * 返回值: Object[]
     * 返回值说明：获取Args
     */

    public Object[] getArgs()
    {
        return args;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Default消息
     * @return
     * 返回值: String
     * 返回值说明：获取Default消息
     */

    public String getDefaultMessage()
    {
        return defaultMessage;
    }
}
