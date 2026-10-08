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
 * 时间: 2026/6/28 04:30
 * 说明: 报表服务分页表格返回对象，字段与管理后台 TableDataInfo 保持一致，便于 admin 通过 ReportRestService 直接透传给前端表格
 */
@Setter
@Getter
public class TableDataInfo implements Serializable
{
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 成功状态码，保持与后台 AjaxResult 成功码一致
     */
    private static final int SUCCESS_CODE = 200;

    /**
     * 失败状态码，保持与后台 AjaxResult 失败码一致
     */
    private static final int ERROR_CODE = 500;

    /**
     * 默认成功提示
     */
    private static final String SUCCESS_MSG = "查询成功";

    /**
     * 默认失败提示
     */
    private static final String ERROR_MSG = "查询失败";

    /**
     * 分页查询总记录数
     */
    private long total;

    /**
     * 当前页业务数据列表
     */
    private List<?> rows;

    /**
     * 响应状态码，200表示查询成功
     */
    private int code;

    /**
     * 响应描述，只承载提示语，不承载业务数据
     */
    private String msg;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 04:30
     * 说明: 创建空分页返回对象，供 JSON 反序列化和异常兜底使用
     */
    public TableDataInfo()
    {
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 04:30
     * 说明: 创建成功分页返回对象
     * @param rows 当前页列表数据
     * @param total 总记录数
     * @return
     * 返回值: TableDataInfo
     * 返回值说明: code为200、msg为查询成功的分页结果
     */
    public static TableDataInfo success(List<?> rows, long total)
    {
        TableDataInfo dataInfo = new TableDataInfo();
        dataInfo.setCode(SUCCESS_CODE);
        dataInfo.setMsg(SUCCESS_MSG);
        dataInfo.setRows(rows == null ? Collections.emptyList() : rows);
        dataInfo.setTotal(total);
        return dataInfo;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 05:02
     * 说明: 创建失败分页返回对象，用于列表接口异常时保持前端响应结构稳定
     * @param msg 失败提示，为空时使用默认查询失败提示
     * @return
     * 返回值: TableDataInfo
     * 返回值说明: code为500、rows为空列表、total为0的分页失败结果
     */
    public static TableDataInfo error(String msg)
    {
        TableDataInfo dataInfo = new TableDataInfo();
        dataInfo.setCode(ERROR_CODE);
        dataInfo.setMsg(msg == null || msg.isBlank() ? ERROR_MSG : msg);
        dataInfo.setRows(Collections.emptyList());
        dataInfo.setTotal(0);
        return dataInfo;
    }

}
