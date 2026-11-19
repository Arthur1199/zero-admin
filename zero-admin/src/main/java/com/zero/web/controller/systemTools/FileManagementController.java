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

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 管理后台文件管理入口，负责权限校验、操作审计和文件服务代理。
 */
@RestController
@RequestMapping("/systemTools/fileManagement")
public class FileManagementController extends BaseController
{
    /** 文件服务内部文件管理路径前缀。 */
    private static final String FILE_PATH = "/file";

    @Autowired
    private FileRestService fileRestService;

    @Autowired
    private IAdminFileUploadService adminFileUploadService;

    @Autowired
    private MultipartProperties multipartProperties;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 具备文件管理列表权限时，将查询条件和分页参数转给文件服务。
     * @param queryParams 文件服务列表接口接收的筛选和分页参数
     * @return
     * 返回值: TableDataInfo
     * 返回值说明: 分页文件记录及总数
     */
    @RequiresPermissions("systemTools:fileManagement:list")
    @GetMapping("/list")
    public TableDataInfo list(@RequestParam Map<String, Object> queryParams)
    {
        return fileRestService.exchange(FILE_PATH + "/list", HttpMethod.GET, null, queryParams,
                TableDataInfo.class);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 具备文件管理列表权限时，查询管理后台 multipart 单文件及整请求限制。
     * @return
     * 返回值: AjaxResult
     * 返回值说明: data 包含以字节表示的限制和供上传界面使用的文字
     */
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

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 通过管理后台登录链路查询单条文件记录，供列表内查看详情。
     * @param id 文件记录主键
     * @return
     * 返回值: AjaxResult
     * 返回值说明: 文件服务的详情响应，成功时 data 为文件记录
     */
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return fileRestService.invokeGetByMap(FILE_PATH + "/" + id, Map.of());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 具备文件上传权限时，将 multipart 文件交给文件服务并记录上传操作。
     * @param file 文件管理页面提交的文件
     * @return
     * 返回值: AjaxResult
     * 返回值说明: 上传成功的文件信息或上传失败原因
     */
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

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 具备文件删除权限时按传入顺序逐条调用文件服务并记录删除操作，遇到失败即停止。
     * @param ids 以逗号分隔的文件记录主键
     * @return
     * 返回值: AjaxResult
     * 返回值说明: 全部处理结束时返回成功，失败时返回首个错误，此前已完成的删除不会回滚
     */
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

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 按能整除的最大单位显示上传限制，避免小数或取整误导页面提示。
     * @param dataSize 当前配置的大小
     * @return
     * 返回值: String
     * 返回值说明: 带 MB、KB 或 B 单位的大小
     */
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
