package com.zero.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 表格分页返回对象。
 */
@Getter
@Setter
public class TableDataInfo implements Serializable
{
    @Serial
    private static final long serialVersionUID = 1L;

    private static final int SUCCESS_CODE = 200;

    private static final int ERROR_CODE = 500;

    private long total;

    private List<?> rows;

    private int code;

    private String msg;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 构造文件列表查询成功响应。
     * @param rows 当前页记录
     * @param total 本次查询的记录总数
     * @return
     * 返回值: TableDataInfo
     * 返回值说明: 包含当前页记录及总数的分页响应
     */
    public static TableDataInfo success(List<?> rows, long total)
    {
        TableDataInfo dataInfo = new TableDataInfo();
        dataInfo.setCode(SUCCESS_CODE);
        dataInfo.setMsg("查询成功");
        dataInfo.setRows(rows == null ? Collections.emptyList() : rows);
        dataInfo.setTotal(total);
        return dataInfo;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 构造文件列表查询失败响应，保留分页字段。
     * @param msg 返回给调用方的错误提示
     * @return
     * 返回值: TableDataInfo
     * 返回值说明: 带错误提示及空分页数据的响应
     */
    public static TableDataInfo error(String msg)
    {
        TableDataInfo dataInfo = new TableDataInfo();
        dataInfo.setCode(ERROR_CODE);
        dataInfo.setMsg(msg == null || msg.isBlank() ? "查询失败" : msg);
        dataInfo.setRows(Collections.emptyList());
        dataInfo.setTotal(0L);
        return dataInfo;
    }
}
