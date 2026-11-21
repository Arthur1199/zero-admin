package com.zero.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zero.domain.FileDetail;
import com.zero.dto.TableDataInfo;
import com.zero.model.AjaxResult;
import com.zero.queryDto.FileDetailQuery;
import com.zero.service.IFileDetailService;
import com.zero.vo.FileInfo;
import com.zero.vo.FileStorageService;
import com.zero.vo.MockMultipartFile;
import com.zero.vo.UploadPretreatment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 提供文件服务内部的文件上传、分页、详情、记录编辑和删除接口。
 */
@RestController
@RequestMapping("/file")
public class FileController
{
    /** 调用方未指定业务对象时记录的 ID。 */
    private static final String DEFAULT_OBJECT_ID = "0";

    /** 调用方未指定文件业务类型时使用的值。 */
    private static final String DEFAULT_OBJECT_TYPE = "0";

    /** 浏览器上传 Blob 时的默认原始文件名。 */
    private static final String BLOB_NAME = "blob";

    /** 按年、月、日生成上传文件的存储子目录。 */
    private static final DateTimeFormatter PATH_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private IFileDetailService fileDetailService;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 按业务归属和所选平台上传文件，返回已保存的文件信息。
     * @param file 非空的 multipart 文件
     * @param platform 可选存储平台，空白时使用默认平台
     * @param objectId 业务对象 ID，空白时使用默认值
     * @param objectType 业务类型，空白时使用默认值
     * @return
     * 返回值: AjaxResult
     * 返回值说明: 成功时 data 为文件信息，存储返回空结果时返回错误
     */
    @PostMapping("/upload")
    public AjaxResult upload(@RequestParam("file") MultipartFile file,
                             @RequestParam(required = false) String platform,
                             @RequestParam(required = false) String objectId,
                             @RequestParam(required = false) String objectType) throws IOException
    {
        FileInfo fileInfo = buildPretreatment(file, platform, objectId, objectType).upload();
        return fileInfo == null ? AjaxResult.error("上传文件失败") : AjaxResult.success(fileInfo);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 按输入顺序分别上传文件，单项空结果会跳过，上传异常交给全局处理。
     * @param files 非空的 multipart 文件列表
     * @param platform 可选存储平台，空白时使用默认平台
     * @param objectId 业务对象 ID，空白时使用默认值
     * @param objectType 业务类型，空白时使用默认值
     * @return
     * 返回值: AjaxResult
     * 返回值说明: 至少一项成功时 data 为成功文件列表，否则返回上传失败
     */
    @PostMapping("/uploadFiles")
    public AjaxResult uploadFiles(@RequestParam("files") List<MultipartFile> files,
                                  @RequestParam(required = false) String platform,
                                  @RequestParam(required = false) String objectId,
                                  @RequestParam(required = false) String objectType) throws IOException
    {
        validateFiles(files);
        List<FileInfo> result = new ArrayList<>();
        for (MultipartFile file : files)
        {
            FileInfo fileInfo = buildPretreatment(file, platform, objectId, objectType).upload();
            if (fileInfo != null)
            {
                result.add(fileInfo);
            }
        }
        return result.isEmpty() ? AjaxResult.error("上传文件失败") : AjaxResult.success(result);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 将图片缩放到 1000 × 1000 范围内，同时生成 200 × 200 范围内的缩略图。
     * @param file 非空的 multipart 文件
     * @param platform 可选存储平台，空白时使用默认平台
     * @param objectId 业务对象 ID，空白时使用默认值
     * @param objectType 业务类型，空白时使用默认值
     * @return
     * 返回值: AjaxResult
     * 返回值说明: 成功时 data 为原图及缩略图文件信息
     */
    @PostMapping("/upload-image")
    public AjaxResult uploadImage(@RequestParam("file") MultipartFile file,
                                  @RequestParam(required = false) String platform,
                                  @RequestParam(required = false) String objectId,
                                  @RequestParam(required = false) String objectType) throws IOException
    {
        FileInfo fileInfo = buildPretreatment(file, platform, objectId, objectType)
                .image(img -> img.size(1000, 1000))
                .thumbnail(th -> th.size(200, 200))
                .upload();
        return fileInfo == null ? AjaxResult.error("上传图片失败") : AjaxResult.success(fileInfo);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 依次处理图片缩放和缩略图生成，跳过上传返回空结果的图片。
     * @param files 非空的 multipart 图片列表
     * @param platform 可选存储平台，空白时使用默认平台
     * @param objectId 业务对象 ID，空白时使用默认值
     * @param objectType 业务类型，空白时使用默认值
     * @return
     * 返回值: AjaxResult
     * 返回值说明: 成功时 data 为上传成功的图片列表，列表为空时返回错误
     */
    @PostMapping("/upload-images")
    public AjaxResult uploadImages(@RequestParam("files") List<MultipartFile> files,
                                   @RequestParam(required = false) String platform,
                                   @RequestParam(required = false) String objectId,
                                   @RequestParam(required = false) String objectType) throws IOException
    {
        validateFiles(files);
        List<FileInfo> result = new ArrayList<>();
        for (MultipartFile file : files)
        {
            FileInfo fileInfo = buildPretreatment(file, platform, objectId, objectType)
                    .image(img -> img.size(1000, 1000))
                    .thumbnail(th -> th.size(200, 200))
                    .upload();
            if (fileInfo != null)
            {
                result.add(fileInfo);
            }
        }
        return result.isEmpty() ? AjaxResult.error("上传图片失败") : AjaxResult.success(result);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 按文件信息、业务归属及创建日期查询文件记录。
     * @param query 文件筛选、时间范围及分页参数
     * @return
     * 返回值: TableDataInfo
     * 返回值说明: 分页记录列表及总数
     */
    @GetMapping("/list")
    public TableDataInfo list(FileDetailQuery query)
    {
        IPage<FileDetail> page = fileDetailService.selectFilePage(query);
        return TableDataInfo.success(page.getRecords(), page.getTotal());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 根据主键查询单条文件记录。
     * @param id 文件记录主键
     * @return
     * 返回值: AjaxResult
     * 返回值说明: 成功时 data 为文件记录，找不到记录时返回错误
     */
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Integer id)
    {
        FileDetail fileDetail = fileDetailService.selectFileById(id);
        return fileDetail == null ? AjaxResult.error("文件记录不存在") : AjaxResult.success(fileDetail);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 修改已存在文件的原始文件名和业务归属。
     * @param id 文件记录主键
     * @param fileDetail 待修改的记录字段
     * @return
     * 返回值: AjaxResult
     * 返回值说明: 记录修改结果
     */
    @PutMapping("/{id}")
    public AjaxResult edit(@PathVariable Integer id, @RequestBody FileDetail fileDetail)
    {
        return fileDetailService.updateFileDetail(id, fileDetail) ? AjaxResult.success()
                : AjaxResult.error("修改文件记录失败");
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 数据库删除提交后清理存储对象，清理失败时返回成功及提示。
     * @param id 文件记录主键
     * @return
     * 返回值: AjaxResult
     * 返回值说明: 成功响应的 data 表示存储对象是否清理成功
     */
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable Integer id)
    {
        boolean storageDeleted = fileDetailService.deleteFileById(id);
        return AjaxResult.success(storageDeleted ? "删除成功" : "删除成功，云平台记录删除失败", storageDeleted);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 采用应用当前日期划分存储子目录，允许调用方覆盖默认存储平台。
     * @param file 非空的 multipart 文件
     * @param platform 可选存储平台，空白时使用默认平台
     * @param objectId 业务对象 ID，空白时使用默认值
     * @param objectType 业务类型，空白时使用默认值
     * @return
     * 返回值: UploadPretreatment
     * 返回值说明: 已设置路径和业务归属的上传预处理对象
     */
    private UploadPretreatment buildPretreatment(MultipartFile file, String platform, String objectId, String objectType) throws IOException
    {
        MultipartFile safeFile = normalizeFile(file);
        UploadPretreatment pretreatment = fileStorageService.of(safeFile)
                .setPath(LocalDate.now().format(PATH_FORMATTER) + "/")
                .setObjectId(StringUtils.hasText(objectId) ? objectId.trim() : DEFAULT_OBJECT_ID)
                .setObjectType(StringUtils.hasText(objectType) ? objectType.trim() : DEFAULT_OBJECT_TYPE);
        if (StringUtils.hasText(platform))
        {
            pretreatment.setPlatform(platform.trim());
        }
        return pretreatment;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 拒绝空文件。浏览器提交默认文件名 blob 时补上 .jpg 扩展名，以便后续图片流程识别。
     * @param file 原始上传文件
     * @return
     * 返回值: MultipartFile
     * 返回值说明: 文件内容保持原样的 multipart 对象
     */
    private MultipartFile normalizeFile(MultipartFile file) throws IOException
    {
        if (file == null || file.isEmpty())
        {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        // 浏览器上传默认 Blob 名称时补齐扩展名，只重建文件包装，保留原始内容。
        if (BLOB_NAME.equals(file.getOriginalFilename()))
        {
            return new MockMultipartFile(file.getName(), BLOB_NAME + ".jpg", file.getContentType(), file.getInputStream());
        }
        return file;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 校验批量上传请求至少包含一个文件，单项内容由预处理逐个校验。
     * @param files 批量文件列表
     * @return
     * 返回值: void
     * 返回值说明: 列表为空时抛出参数异常
     */
    private void validateFiles(List<MultipartFile> files)
    {
        if (files == null || files.isEmpty())
        {
            throw new IllegalArgumentException("上传文件不能为空");
        }
    }
}
