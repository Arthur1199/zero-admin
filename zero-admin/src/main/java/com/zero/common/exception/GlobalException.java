package com.zero.common.exception;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 全局异常
 */
public class GlobalException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    /**
     * 错误提示
     */
    private String message;

    /**
     * 错误明细，内部调试错误和 {@link CommonResult#getDetailMessage()} 一致的设计
     */
    private String detailMessage;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 空构造方法，避免反序列化问题
     */
    public GlobalException()
    {
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: Global异常
     * @param message 消息参数
     */

    public GlobalException(String message)
    {
        this.message = message;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Detail消息
     * @return
     * 返回值: String
     * 返回值说明：获取Detail消息
     */

    public String getDetailMessage()
    {
        return detailMessage;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Detail消息
     * @param detailMessage detail消息参数
     * @return
     * 返回值: GlobalException
     * 返回值说明：设置Detail消息
     */

    public GlobalException setDetailMessage(String detailMessage)
    {
        this.detailMessage = detailMessage;
        return this;
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
        return message;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置消息
     * @param message 消息参数
     * @return
     * 返回值: GlobalException
     * 返回值说明：设置消息
     */

    public GlobalException setMessage(String message)
    {
        this.message = message;
        return this;
    }
}