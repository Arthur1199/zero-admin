package com.zero.common.core.domain;

import java.io.Serializable;
import com.zero.common.constant.HttpStatus;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 响应信息主体
 */
public class R<T> implements Serializable
{
    private static final long serialVersionUID = 1L;

    /**
     * 成功
     */
    public static final int SUCCESS = HttpStatus.SUCCESS;

    /**
     * 失败
     */
    public static final int FAIL = HttpStatus.ERROR;
    /**
     * 编码字段
     */

    private int code;
    /**
     * msg字段
     */

    private String msg;
    /**
     * 数据字段
     */

    private T data;
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: ok
     * @param <T> 当前方法处理的值类型
     * @return
     * 返回值: R<T>
     * 返回值说明: ok
     */

    public static <T> R<T> ok()
    {
        return restResult(null, SUCCESS, "操作成功");
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: ok
     * @param <T> 当前方法处理的值类型
     * @param data 数据参数
     * @return
     * 返回值: R<T>
     * 返回值说明: ok
     */

    public static <T> R<T> ok(T data)
    {
        return restResult(data, SUCCESS, "操作成功");
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: ok
     * @param <T> 当前方法处理的值类型
     * @param data 数据参数
     * @param msg msg参数
     * @return
     * 返回值: R<T>
     * 返回值说明: ok
     */

    public static <T> R<T> ok(T data, String msg)
    {
        return restResult(data, SUCCESS, msg);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: fail
     * @param <T> 当前方法处理的值类型
     * @return
     * 返回值: R<T>
     * 返回值说明: fail
     */

    public static <T> R<T> fail()
    {
        return restResult(null, FAIL, "操作失败");
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: fail
     * @param <T> 当前方法处理的值类型
     * @param msg msg参数
     * @return
     * 返回值: R<T>
     * 返回值说明: fail
     */

    public static <T> R<T> fail(String msg)
    {
        return restResult(null, FAIL, msg);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: fail
     * @param <T> 当前方法处理的值类型
     * @param data 数据参数
     * @return
     * 返回值: R<T>
     * 返回值说明: fail
     */

    public static <T> R<T> fail(T data)
    {
        return restResult(data, FAIL, "操作失败");
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: fail
     * @param <T> 当前方法处理的值类型
     * @param data 数据参数
     * @param msg msg参数
     * @return
     * 返回值: R<T>
     * 返回值说明: fail
     */

    public static <T> R<T> fail(T data, String msg)
    {
        return restResult(data, FAIL, msg);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: fail
     * @param <T> 当前方法处理的值类型
     * @param code 编码参数
     * @param msg msg参数
     * @return
     * 返回值: R<T>
     * 返回值说明: fail
     */

    public static <T> R<T> fail(int code, String msg)
    {
        return restResult(null, code, msg);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: rest结果
     * @param <T> 当前方法处理的值类型
     * @param data 数据参数
     * @param code 编码参数
     * @param msg msg参数
     * @return
     * 返回值: R<T>
     * 返回值说明: rest结果
     */

    private static <T> R<T> restResult(T data, int code, String msg)
    {
        R<T> apiResult = new R<>();
        apiResult.setCode(code);
        apiResult.setData(data);
        apiResult.setMsg(msg);
        return apiResult;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取编码
     * @return
     * 返回值: int
     * 返回值说明：获取编码
     */

    public int getCode()
    {
        return code;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置编码
     * @param code 编码参数
     * @return
     * 返回值: void
     * 返回值说明：设置编码
     */

    public void setCode(int code)
    {
        this.code = code;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Msg
     * @return
     * 返回值: String
     * 返回值说明：获取Msg
     */

    public String getMsg()
    {
        return msg;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Msg
     * @param msg msg参数
     * @return
     * 返回值: void
     * 返回值说明：设置Msg
     */

    public void setMsg(String msg)
    {
        this.msg = msg;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取数据
     * @return
     * 返回值: T
     * 返回值说明：获取数据
     */

    public T getData()
    {
        return data;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置数据
     * @param data 数据参数
     * @return
     * 返回值: void
     * 返回值说明：设置数据
     */

    public void setData(T data)
    {
        this.data = data;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 判断Error
     * @param <T> 当前方法处理的值类型
     * @param ret ret参数
     * @return
     * 返回值: Boolean
     * 返回值说明: 判断Error
     */

    public static <T> Boolean isError(R<T> ret)
    {
        return !isSuccess(ret);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 判断Success
     * @param <T> 当前方法处理的值类型
     * @param ret ret参数
     * @return
     * 返回值: Boolean
     * 返回值说明: 判断Success
     */

    public static <T> Boolean isSuccess(R<T> ret)
    {
        return R.SUCCESS == ret.getCode();
    }
}
