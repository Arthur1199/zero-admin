package com.zero.model;

import java.io.Serial;
import java.util.HashMap;
import java.util.Objects;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 通用接口返回对象，统一承载状态码、提示信息和业务数据
 */
public class AjaxResult extends HashMap<String, Object>
{
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 成功状态码
     */
    public static final int SUCCESS_CODE = 200;

    /**
     * 警告状态码
     */
    public static final int WARN_CODE = 301;

    /**
     * 错误状态码
     */
    public static final int ERROR_CODE = 500;

    /**
     * 状态码字段名
     */
    public static final String CODE_TAG = "code";

    /**
     * 返回内容字段名
     */
    public static final String MSG_TAG = "msg";

    /**
     * 数据对象字段名
     */
    public static final String DATA_TAG = "data";

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 初始化空的AjaxResult对象
     */
    public AjaxResult()
    {
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 初始化只包含状态码和提示信息的AjaxResult对象
     * @param code 状态码
     * @param msg 返回内容
     */
    public AjaxResult(int code, String msg)
    {
        super.put(CODE_TAG, code);
        super.put(MSG_TAG, msg);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 初始化包含状态码、提示信息和业务数据的AjaxResult对象
     * @param code 状态码
     * @param msg 返回内容
     * @param data 数据对象，为空时不写入data字段
     */
    public AjaxResult(int code, String msg, Object data)
    {
        super.put(CODE_TAG, code);
        super.put(MSG_TAG, msg);
        if (data != null)
        {
            super.put(DATA_TAG, data);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 返回默认成功消息
     * @return
     * 返回值: AjaxResult
     * 返回值说明: code为200、msg为操作成功的返回对象
     */
    public static AjaxResult success()
    {
        return AjaxResult.success("操作成功");
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 返回成功数据
     * @param data 数据参数
     * @return
     * 返回值: AjaxResult
     * 返回值说明: code为200、msg为操作成功、data为业务数据的返回对象
     */
    public static AjaxResult success(Object data)
    {
        return AjaxResult.success("操作成功", data);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 返回自定义成功消息
     * @param msg 返回内容
     * @return
     * 返回值: AjaxResult
     * 返回值说明: code为200、msg为自定义内容的返回对象
     */
    public static AjaxResult success(String msg)
    {
        return AjaxResult.success(msg, null);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 返回自定义成功消息和业务数据
     * @param msg 返回内容
     * @param data 数据对象
     * @return
     * 返回值: AjaxResult
     * 返回值说明: code为200、msg为自定义内容、data为业务数据的返回对象
     */
    public static AjaxResult success(String msg, Object data)
    {
        return new AjaxResult(SUCCESS_CODE, msg, data);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 返回警告消息
     * @param msg 返回内容
     * @return
     * 返回值: AjaxResult
     * 返回值说明: code为301、msg为自定义内容的返回对象
     */
    public static AjaxResult warn(String msg)
    {
        return AjaxResult.warn(msg, null);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 返回警告消息和业务数据
     * @param msg 返回内容
     * @param data 数据对象
     * @return
     * 返回值: AjaxResult
     * 返回值说明: code为301、msg为自定义内容、data为业务数据的返回对象
     */
    public static AjaxResult warn(String msg, Object data)
    {
        return new AjaxResult(WARN_CODE, msg, data);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 返回默认错误消息
     * @return
     * 返回值: AjaxResult
     * 返回值说明: code为500、msg为操作失败的返回对象
     */
    public static AjaxResult error()
    {
        return AjaxResult.error("操作失败");
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 返回自定义错误消息
     * @param msg 返回内容
     * @return
     * 返回值: AjaxResult
     * 返回值说明: code为500、msg为自定义内容的返回对象
     */
    public static AjaxResult error(String msg)
    {
        return AjaxResult.error(msg, null);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 返回自定义错误消息和业务数据
     * @param msg 返回内容
     * @param data 数据对象
     * @return
     * 返回值: AjaxResult
     * 返回值说明: code为500、msg为自定义内容、data为业务数据的返回对象
     */
    public static AjaxResult error(String msg, Object data)
    {
        return new AjaxResult(ERROR_CODE, msg, data);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 返回指定状态码的错误消息
     * @param code 状态码
     * @param msg 返回内容
     * @return
     * 返回值: AjaxResult
     * 返回值说明: code为指定状态码、msg为自定义内容的返回对象
     */
    public static AjaxResult error(int code, String msg)
    {
        return new AjaxResult(code, msg, null);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 判断当前返回对象是否为成功消息
     * @return
     * 返回值: boolean
     * 返回值说明: true-成功，false-非成功
     */
    public boolean isSuccess()
    {
        return Objects.equals(SUCCESS_CODE, this.get(CODE_TAG));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 判断当前返回对象是否为警告消息
     * @return
     * 返回值: boolean
     * 返回值说明: true-警告，false-非警告
     */
    public boolean isWarn()
    {
        return Objects.equals(WARN_CODE, this.get(CODE_TAG));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 判断当前返回对象是否为错误消息
     * @return
     * 返回值: boolean
     * 返回值说明: true-错误，false-非错误
     */
    public boolean isError()
    {
        return Objects.equals(ERROR_CODE, this.get(CODE_TAG));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 写入自定义字段，方便链式调用
     * @param key 字段名
     * @param value 字段值
     * @return
     * 返回值: AjaxResult
     * 返回值说明: 当前返回对象
     */
    @Override
    public AjaxResult put(String key, Object value)
    {
        super.put(key, value);
        return this;
    }
}
