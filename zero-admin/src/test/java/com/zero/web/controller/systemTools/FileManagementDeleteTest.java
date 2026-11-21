package com.zero.web.controller.systemTools;

import com.zero.common.core.domain.AjaxResult;
import com.zero.feign.FileRestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 验证文件管理入口保留单删结果并汇总批量删除的存储清理提示。
 */
class FileManagementDeleteTest
{
    private static final String WARNING = "删除成功，云平台记录删除失败";
    private final FileRestService fileService = mock(FileRestService.class);
    private final FileManagementController controller = new FileManagementController();

    /** 注入模拟文件服务，避免访问数据库及外部存储。 */
    @BeforeEach
    void setUp()
    {
        ReflectionTestUtils.setField(controller, "fileRestService", fileService);
    }

    /** 单删完全成功时显示成功文案。 */
    @Test
    void returnsSuccessForSingleDeletion()
    {
        when(fileService.exchange("/file/1", HttpMethod.DELETE, null, Map.of(), AjaxResult.class))
                .thenReturn(AjaxResult.success("删除成功", true));
        AjaxResult result = controller.remove("1");
        assertTrue(result.isSuccess());
        assertEquals("删除成功", result.get(AjaxResult.MSG_TAG));
        assertEquals(true, result.get(AjaxResult.DATA_TAG));
    }

    /** 批删遇到云端清理失败仍继续处理后续记录，最后返回统一提示。 */
    @Test
    void continuesBatchAfterStorageFailure()
    {
        when(fileService.exchange("/file/1", HttpMethod.DELETE, null, Map.of(), AjaxResult.class))
                .thenReturn(AjaxResult.success(WARNING, false));
        when(fileService.exchange("/file/2", HttpMethod.DELETE, null, Map.of(), AjaxResult.class))
                .thenReturn(AjaxResult.success("删除成功", true));
        AjaxResult result = controller.remove("1,2");
        assertTrue(result.isSuccess());
        assertEquals(WARNING, result.get(AjaxResult.MSG_TAG));
        assertEquals(false, result.get(AjaxResult.DATA_TAG));
        verify(fileService).exchange("/file/2", HttpMethod.DELETE, null, Map.of(), AjaxResult.class);
    }

    /** 数据库删除错误仍按现有批处理规则停止。 */
    @Test
    void stopsBatchAfterDatabaseFailure()
    {
        AjaxResult failure = AjaxResult.error("删除文件记录失败");
        when(fileService.exchange("/file/1", HttpMethod.DELETE, null, Map.of(), AjaxResult.class)).thenReturn(failure);
        assertSame(failure, controller.remove("1,2"));
        verify(fileService, never()).exchange("/file/2", HttpMethod.DELETE, null, Map.of(), AjaxResult.class);
    }
}
