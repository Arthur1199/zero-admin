package com.zero.web.controller.systemTools;

import com.zero.common.annotation.Log;
import com.zero.common.annotation.RequiresPermissions;
import com.zero.common.core.controller.BaseController;
import com.zero.common.core.domain.AjaxResult;
import com.zero.common.core.page.TableDataInfo;
import com.zero.common.enums.BusinessType;
import com.zero.feign.FileRestService;
import com.zero.system.service.IAdminFileUploadService;
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.unit.DataSize;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/systemTools/fileManagement")
public class FileManagementController extends BaseController
{
    private static final String FILE_PATH = "/file";

    @Autowired
    private FileRestService fileRestService;

    @Autowired
    private IAdminFileUploadService adminFileUploadService;

    @Autowired
    private MultipartProperties multipartProperties;

    @RequiresPermissions("systemTools:fileManagement:list")
    @GetMapping("/list")
    public TableDataInfo list(@RequestParam Map<String, Object> queryParams)
    {
        return fileRestService.exchange(FILE_PATH + "/list", HttpMethod.GET, null, queryParams,
                TableDataInfo.class);
    }

    @RequiresPermissions("systemTools:fileManagement:list")
    @GetMapping("/upload/config")
    public AjaxResult uploadConfig()
    {
        DataSize maxFileSize = multipartProperties.getMaxFileSize();
        DataSize maxRequestSize = multipartProperties.getMaxRequestSize();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("maxFileSize", maxFileSize.toBytes());
        data.put("maxFileSizeText", formatDataSize(maxFileSize));
        data.put("maxRequestSize", maxRequestSize.toBytes());
        data.put("maxRequestSizeText", formatDataSize(maxRequestSize));
        return success(data);
    }

    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return fileRestService.invokeGetByMap(FILE_PATH + "/" + id, Map.of());
    }

    @RequiresPermissions("systemTools:fileManagement:add")
    @Log(title = "文件管理", businessType = BusinessType.INSERT)
    @PostMapping("/upload")
    public AjaxResult upload(@RequestParam("file") MultipartFile file)
    {
        try
        {
            return adminFileUploadService.uploadManagementFile(file);
        }
        catch (Exception e)
        {
            return AjaxResult.error(e.getMessage());
        }
    }

    @RequiresPermissions("systemTools:fileManagement:remove")
    @Log(title = "文件管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String ids)
    {
        if (ids == null || ids.isBlank())
        {
            return AjaxResult.error("文件ID不能为空");
        }
        for (String id : ids.split(","))
        {
            String safeId = id == null ? "" : id.trim();
            if (safeId.isEmpty())
            {
                continue;
            }
            AjaxResult result = fileRestService.exchange(FILE_PATH + "/" + safeId, HttpMethod.DELETE, null, Map.of(),
                    AjaxResult.class);
            if (!result.isSuccess())
            {
                return result;
            }
        }
        return AjaxResult.success();
    }

    private String formatDataSize(DataSize dataSize)
    {
        long bytes = dataSize.toBytes();
        long mb = 1024L * 1024L;
        long kb = 1024L;
        if (bytes % mb == 0)
        {
            return bytes / mb + "MB";
        }
        if (bytes % kb == 0)
        {
            return bytes / kb + "KB";
        }
        return bytes + "B";
    }
}
