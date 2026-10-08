package com.zero.common.utils.poi;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Workbook;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: Excel数据格式处理适配器
 */
public interface ExcelHandlerAdapter
{
    /**
     * 格式化
     *
     * @param value 单元格数据值
     * @param args excel注解args参数组
     * @param cell 单元格对象
     * @param wb 工作簿对象
     *
     * @return 处理后的值
     */
    Object format(Object value, String[] args, Cell cell, Workbook wb);
}
