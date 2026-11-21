package com.zero.web.controller.system;

import com.zero.common.core.domain.entity.SysUser;
import com.zero.common.utils.poi.ExcelUtil;
import com.zero.system.service.ISysUserService;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/** 用户导入导出隔离验证：读取实际Excel响应及解析结果，不连接数据库或写入用户数据。 */
class UserExportTest
{
    /** 导入导出字段名称与页面一致，登录锁定字段不进入Excel文件。 */
    @Test
    void usesPageHeadersAndExcludesLoginLock() throws Exception
    {
        SysUser unlocked = new SysUser(); unlocked.setUserId(2L); unlocked.setLoginLock(0); unlocked.setStatus("0");
        SysUser locked = new SysUser(); locked.setUserId(3L); locked.setLoginLock(1); locked.setStatus("1");
        SysUser legacy = new SysUser(); legacy.setUserId(4L);
        unlocked.setUserName("export-account"); unlocked.setNickName("导出昵称");
        MockHttpServletResponse response = new MockHttpServletResponse();
        new ExcelUtil<SysUser>(SysUser.class).exportExcel(response, List.of(unlocked, locked, legacy), "用户数据");
        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(response.getContentAsByteArray())))
        {
            Sheet sheet = workbook.getSheetAt(0);
            Map<String, Integer> headers = headers(sheet);
            assertThat(headers).containsKey("账号状态").doesNotContainKeys("状态", "登录锁定");
            assertThat(headers).containsKeys("用户编号", "用户名称", "用户昵称").doesNotContainKeys("用户序号", "登录名称");
            assertThat(sheet.getRow(1).getCell(headers.get("用户编号")).getNumericCellValue()).isEqualTo(2D);
            assertThat(sheet.getRow(1).getCell(headers.get("用户名称")).getStringCellValue()).isEqualTo("export-account");
            assertThat(sheet.getRow(1).getCell(headers.get("用户昵称")).getStringCellValue()).isEqualTo("导出昵称");
            assertThat(sheet.getRow(1).getCell(headers.get("账号状态")).getStringCellValue()).isEqualTo("正常");
            assertThat(sheet.getRow(2).getCell(headers.get("账号状态")).getStringCellValue()).isEqualTo("停用");
        }
        MockHttpServletResponse template = new MockHttpServletResponse();
        new ExcelUtil<SysUser>(SysUser.class).importTemplateExcel(template, "用户数据");
        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(template.getContentAsByteArray())))
        {
            Map<String, Integer> headers = headers(workbook.getSheetAt(0));
            assertThat(headers).containsEntry("部门编号", 0).containsEntry("用户名称", 1).containsEntry("用户昵称", 2)
                .doesNotContainKeys("登录锁定", "用户编号", "登录名称");
        }
    }

    /** 使用实际下载模板填写不同的账号和昵称，再经生产解析器读取，防止两个字段映射错位。 */
    @Test
    void parsesDepartmentAccountAndNicknameFromNewTemplate() throws Exception
    {
        MockHttpServletResponse template = new MockHttpServletResponse();
        new ExcelUtil<SysUser>(SysUser.class).importTemplateExcel(template, "用户数据");
        byte[] filledTemplate;
        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(template.getContentAsByteArray()));
             ByteArrayOutputStream output = new ByteArrayOutputStream())
        {
            Sheet sheet = workbook.getSheetAt(0);
            Map<String, Integer> headers = headers(sheet);
            var row = sheet.createRow(1);
            row.createCell(headers.get("部门编号")).setCellValue(105);
            row.createCell(headers.get("用户名称")).setCellValue("import-account");
            row.createCell(headers.get("用户昵称")).setCellValue("导入昵称");
            row.createCell(headers.get("用户性别")).setCellValue("男");
            row.createCell(headers.get("账号状态")).setCellValue("正常");
            workbook.write(output);
            filledTemplate = output.toByteArray();
        }
        List<SysUser> users = new ExcelUtil<SysUser>(SysUser.class).importExcel(new ByteArrayInputStream(filledTemplate));
        assertThat(users).hasSize(1);
        assertThat(users.get(0).getDeptId()).isEqualTo(105L);
        assertThat(users.get(0).getUserName()).isEqualTo("import-account");
        assertThat(users.get(0).getNickName()).isEqualTo("导入昵称");
        assertThat(users.get(0).getSex()).isEqualTo("0");
        assertThat(users.get(0).getStatus()).isEqualTo("0");
    }

    /** 导出复用列表Service及完整查询对象，锁定状态0、1和未筛选均可传递。 */
    @Test
    void forwardsAllCurrentFiltersToUserListService()
    {
        ISysUserService service = mock(ISysUserService.class);
        SysUserController controller = new SysUserController();
        ReflectionTestUtils.setField(controller, "userService", service);
        when(service.selectUserList(any())).thenReturn(List.of());
        for (Integer loginLock : new Integer[] {0, 1, null})
        {
            SysUser query = new SysUser();
            query.setUserName("filter-user"); query.setPhonenumber("138"); query.setDeptId(42L);
            query.setStatus("0"); query.setDelFlag("2"); query.setLoginLock(loginLock);
            query.getParams().put("beginTime", "2026-11-01");
            query.getParams().put("endTime", "2026-11-22");
            controller.export(new MockHttpServletResponse(), query);
            verify(service).selectUserList(same(query));
            assertThat(query.getLoginLock()).isEqualTo(loginLock);
            assertThat(query.getParams()).containsEntry("beginTime", "2026-11-01").containsEntry("endTime", "2026-11-22");
        }
    }

    /** 按实际标题定位列，避免依赖其它历史导出列的固定位置。 */
    private Map<String, Integer> headers(Sheet sheet)
    {
        Map<String, Integer> result = new HashMap<>();
        sheet.getRow(0).forEach(cell -> result.put(cell.getStringCellValue(), cell.getColumnIndex()));
        return result;
    }
}
