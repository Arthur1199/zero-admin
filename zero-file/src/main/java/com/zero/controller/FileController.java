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
 * 文件上传与记录查询接口。
 */
@RestController
@RequestMapping("/file")
public class FileController
{
    private static final String DEFAULT_OBJECT_ID = "0";

    private static final String DEFAULT_OBJECT_TYPE = "0";

    private static final String BLOB_NAME = "blob";

    private static final DateTimeFormatter PATH_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private IFileDetailService fileDetailService;

    @PostMapping("/upload")
    public AjaxResult upload(@RequestParam("file") MultipartFile file,
                             @RequestParam(required = false) String platform,
                             @RequestParam(required = false) String objectId,
                             @RequestParam(required = false) String objectType) throws IOException
    {
        FileInfo fileInfo = buildPretreatment(file, platform, objectId, objectType).upload();
        return fileInfo == null ? AjaxResult.error("上传文件失败") : AjaxResult.success(fileInfo);
    }

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

    @GetMapping("/list")
    public TableDataInfo list(FileDetailQuery query)
    {
        IPage<FileDetail> page = fileDetailService.selectFilePage(query);
        return TableDataInfo.success(page.getRecords(), page.getTotal());
    }

    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Integer id)
    {
        FileDetail fileDetail = fileDetailService.selectFileById(id);
        return fileDetail == null ? AjaxResult.error("文件记录不存在") : AjaxResult.success(fileDetail);
    }

    @PutMapping("/{id}")
    public AjaxResult edit(@PathVariable Integer id, @RequestBody FileDetail fileDetail)
    {
        return fileDetailService.updateFileDetail(id, fileDetail) ? AjaxResult.success()
                : AjaxResult.error("修改文件记录失败");
    }

    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable Integer id)
    {
        return fileDetailService.deleteFileById(id) ? AjaxResult.success() : AjaxResult.error("删除文件失败");
    }

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

    private MultipartFile normalizeFile(MultipartFile file) throws IOException
    {
        if (file == null || file.isEmpty())
        {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        if (BLOB_NAME.equals(file.getOriginalFilename()))
        {
            return new MockMultipartFile(file.getName(), BLOB_NAME + ".jpg", file.getContentType(), file.getInputStream());
        }
        return file;
    }

    private void validateFiles(List<MultipartFile> files)
    {
        if (files == null || files.isEmpty())
        {
            throw new IllegalArgumentException("上传文件不能为空");
        }
    }
}
