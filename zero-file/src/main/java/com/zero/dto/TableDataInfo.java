package com.zero.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 表格分页返回对象。
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

    public static TableDataInfo success(List<?> rows, long total)
    {
        TableDataInfo dataInfo = new TableDataInfo();
        dataInfo.setCode(SUCCESS_CODE);
        dataInfo.setMsg("查询成功");
        dataInfo.setRows(rows == null ? Collections.emptyList() : rows);
        dataInfo.setTotal(total);
        return dataInfo;
    }

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
