package com.zero.common.core.page;

import java.io.Serializable;
import java.util.List;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 表格分页数据对象
 */
public class TableDataInfo implements Serializable
{
    private static final long serialVersionUID = 1L;

    /**
     * 总记录数
     */
    private long total;

    /**
     * 列表数据
     */
    private List<?> rows;

    /**
     * 消息状态码
     */
    private int code;

    /**
     * 消息内容
     */
    private String msg;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 表格数据对象
     */
    public TableDataInfo()
    {
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 分页
     * @param list 列表数据
     * @param total 总记录数
     */
    public TableDataInfo(List<?> list, long total)
    {
        this.rows = list;
        this.total = total;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Total
     * @return
     * 返回值: long
     * 返回值说明：获取Total
     */

    public long getTotal()
    {
        return total;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Total
     * @param total total参数
     * @return
     * 返回值: void
     * 返回值说明：设置Total
     */

    public void setTotal(long total)
    {
        this.total = total;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Rows
     * @return
     * 返回值: List<>
     * 返回值说明：获取Rows
     */

    public List<?> getRows()
    {
        return rows;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Rows
     * @param rows rows参数
     * @return
     * 返回值: void
     * 返回值说明：设置Rows
     */

    public void setRows(List<?> rows)
    {
        this.rows = rows;
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
}
