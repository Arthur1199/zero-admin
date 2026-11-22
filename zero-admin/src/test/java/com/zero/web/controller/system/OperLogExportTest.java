package com.zero.web.controller.system;

import com.zero.common.utils.poi.ExcelUtil;
import com.zero.system.domain.SysOperLog;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.assertj.core.api.Assertions.*;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/22 20:35
 * 说明: 操作日志导出隔离验证，读取实际Excel响应表头，不连接数据库或写入日志数据。
 */
class OperLogExportTest
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/22 20:35
     * 说明: 导出列头与页面一致使用“功能模块”，不能残留旧名称
     * @return
     * 返回值: void
     * 返回值说明: 校验实际下载文件的首行表头
     */
    @Test
    void usesFunctionModuleHeader() throws Exception
    {
        MockHttpServletResponse response = new MockHttpServletResponse();
        new ExcelUtil<SysOperLog>(SysOperLog.class).exportExcel(response, List.<SysOperLog>of(), "操作日志");
        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(response.getContentAsByteArray())))
        {
            Sheet sheet = workbook.getSheetAt(0);
            Map<String, Integer> headers = new HashMap<>();
            sheet.getRow(0).forEach(cell -> headers.put(cell.getStringCellValue(), cell.getColumnIndex()));
            assertThat(headers).containsKey("功能模块").doesNotContainKeys("操作模块", "系统模块");
        }
    }
}
