package com.zero.system.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zero.common.config.ZeroConfig;
import com.zero.common.core.domain.AjaxResult;
import com.zero.common.exception.file.FileNameLengthLimitExceededException;
import com.zero.common.utils.StringUtils;
import com.zero.common.utils.file.FileUploadUtils;
import com.zero.common.utils.file.FileUtils;
import com.zero.common.utils.file.MimeTypeUtils;
import com.zero.feign.FileRestService;
import com.zero.framework.config.ServerConfig;
import com.zero.system.service.IAdminFileStorageModeService;
import com.zero.system.service.IAdminFileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 校验管理后台上传，按入口选择存储方式，并兼容旧上传组件的响应字段。
 */
@Service
public class AdminFileUploadServiceImpl implements IAdminFileUploadService
{
    /** 旧版多文件响应使用的分隔符。 */
    private static final String FILE_DELIMETER = ",";

    /** 未关联具体业务记录时传给文件服务的业务对象 ID。 */
    private static final String DEFAULT_OBJECT_ID = "0";

    /** 通用单文件和多文件上传在文件服务中的业务类型。 */
    private static final String OBJECT_TYPE_COMMON = "admin-common";

    /** 用户头像上传在文件服务中的业务类型。 */
    private static final String OBJECT_TYPE_AVATAR = "admin-avatar";

    /** 文件管理菜单上传在文件服务中的业务类型。 */
    private static final String OBJECT_TYPE_MANAGEMENT = "file-management";

    @Autowired
    private IAdminFileStorageModeService storageModeService;

    @Autowired
    private FileRestService fileRestService;

    @Autowired
    private ServerConfig serverConfig;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 校验并上传单个通用文件，本地分支使用管理后台地址拼出访问 URL。
     * @param file 要上传的非空文件
     * @return
     * 返回值: AjaxResult
     * 返回值说明: 上传结果，成功时包含旧页面读取的文件字段
     */
    @Override
    public AjaxResult uploadCommonFile(MultipartFile file) throws Exception
    {
        if (storageModeService.useZeroFile())
        {
            return uploadRemoteFile("/file/upload", file, OBJECT_TYPE_COMMON, MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION);
        }
        validateUpload(file, MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION);
        String fileName = FileUploadUtils.upload(ZeroConfig.getUploadPath(), file);
        String url = serverConfig.getUrl() + fileName;
        AjaxResult ajax = AjaxResult.success();
        ajax.put("url", url);
        ajax.put("fileName", fileName);
        ajax.put("newFileName", FileUtils.getName(fileName));
        ajax.put("originalFilename", file.getOriginalFilename());
        return ajax;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 写入前先校验全部文件，远程分支一次转发，本地分支按输入顺序逐个保存。
     * @param files 本次上传的文件列表
     * @return
     * 返回值: AjaxResult
     * 返回值说明: 上传结果，顶层复数字段按逗号拼接
     */
    @Override
    public AjaxResult uploadCommonFiles(List<MultipartFile> files) throws Exception
    {
        validateFiles(files, MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION);
        if (storageModeService.useZeroFile())
        {
            AjaxResult remoteResult = fileRestService.uploadFiles("/file/uploadFiles", "files", files,
                    remoteParams(OBJECT_TYPE_COMMON));
            return buildCompatibleListResult(remoteResult);
        }

        List<String> urls = new ArrayList<>();
        List<String> fileNames = new ArrayList<>();
        List<String> newFileNames = new ArrayList<>();
        List<String> originalFilenames = new ArrayList<>();
        for (MultipartFile file : files)
        {
            String fileName = FileUploadUtils.upload(ZeroConfig.getUploadPath(), file);
            String url = serverConfig.getUrl() + fileName;
            urls.add(url);
            fileNames.add(fileName);
            newFileNames.add(FileUtils.getName(fileName));
            originalFilenames.add(file.getOriginalFilename());
        }
        AjaxResult ajax = AjaxResult.success();
        ajax.put("urls", StringUtils.join(urls, FILE_DELIMETER));
        ajax.put("fileNames", StringUtils.join(fileNames, FILE_DELIMETER));
        ajax.put("newFileNames", StringUtils.join(newFileNames, FILE_DELIMETER));
        ajax.put("originalFilenames", StringUtils.join(originalFilenames, FILE_DELIMETER));
        return ajax;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 按配置上传头像并限制为图片扩展名，文件服务报错时将错误交给调用方处理。
     * @param file 用户头像图片
     * @return
     * 返回值: String
     * 返回值说明: 远程文件访问地址或本地资源映射路径
     */
    @Override
    public String uploadAvatar(MultipartFile file) throws Exception
    {
        if (storageModeService.useZeroFile())
        {
            AjaxResult ajax = uploadRemoteFile("/file/upload-image", file, OBJECT_TYPE_AVATAR,
                    MimeTypeUtils.IMAGE_EXTENSION);
            if (!ajax.isSuccess())
            {
                throw new IOException(String.valueOf(ajax.get(AjaxResult.MSG_TAG)));
            }
            return String.valueOf(ajax.get("url"));
        }
        validateUpload(file, MimeTypeUtils.IMAGE_EXTENSION);
        return FileUploadUtils.upload(ZeroConfig.getAvatarPath(), file, MimeTypeUtils.IMAGE_EXTENSION, true);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 将文件管理上传按独立业务类型转发给文件服务。
     * @param file 文件管理页面上传的非空文件
     * @return
     * 返回值: AjaxResult
     * 返回值说明: 文件服务上传结果及兼容字段
     */
    @Override
    public AjaxResult uploadManagementFile(MultipartFile file) throws Exception
    {
        return uploadRemoteFile("/file/upload", file, OBJECT_TYPE_MANAGEMENT, MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 在发送 multipart 请求前执行管理后台上传校验。
     * @param requestPath 文件服务上传路径
     * @param file 上传文件
     * @param objectType 文件服务业务类型
     * @param allowedExtension 该入口允许的文件扩展名
     * @return
     * 返回值: AjaxResult
     * 返回值说明: 文件服务返回的错误或补齐旧响应字段的成功结果
     */
    private AjaxResult uploadRemoteFile(String requestPath, MultipartFile file, String objectType,
            String[] allowedExtension) throws Exception
    {
        validateUpload(file, allowedExtension);
        AjaxResult remoteResult = fileRestService.uploadFile(requestPath, "file", file, remoteParams(objectType));
        return buildCompatibleFileResult(remoteResult);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 为文件服务构造业务归属参数，上传时使用默认业务对象 ID。
     * @param objectType 文件上传所属入口的业务类型
     * @return
     * 返回值: Map<String, Object>
     * 返回值说明: 文件服务接收的 multipart 业务字段
     */
    private Map<String, Object> remoteParams(String objectType)
    {
        Map<String, Object> params = new HashMap<>();
        params.put("objectId", DEFAULT_OBJECT_ID);
        params.put("objectType", objectType);
        return params;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 保留文件服务的结构化文件信息，并回填旧页面读取的顶层字段。
     * @param remoteResult 文件服务的单文件上传响应
     * @return
     * 返回值: AjaxResult
     * 返回值说明: 有访问地址时返回成功信息，否则返回明确错误
     */
    private AjaxResult buildCompatibleFileResult(AjaxResult remoteResult)
    {
        if (remoteResult == null)
        {
            return AjaxResult.error("文件服务未返回响应数据");
        }
        if (!remoteResult.isSuccess())
        {
            return remoteResult;
        }
        Map<String, Object> fileInfo = toMap(remoteResult.get(AjaxResult.DATA_TAG));
        String url = toString(fileInfo.get("url"));
        if (StringUtils.isEmpty(url))
        {
            return AjaxResult.error("文件服务未返回文件访问地址");
        }
        AjaxResult ajax = AjaxResult.success(fileInfo);
        ajax.put("url", url);
        ajax.put("fileName", url);
        ajax.put("newFileName", toString(fileInfo.get("filename")));
        ajax.put("originalFilename", toString(fileInfo.get("originalFilename")));
        return ajax;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 在 data 保留文件服务的完整列表，用带有效访问地址的记录生成旧页面的逗号分隔字段。
     * @param remoteResult 文件服务的多文件上传响应
     * @return
     * 返回值: AjaxResult
     * 返回值说明: 包含文件列表及旧版字段的响应，空响应或空文件列表返回错误
     */
    private AjaxResult buildCompatibleListResult(AjaxResult remoteResult)
    {
        if (remoteResult == null)
        {
            return AjaxResult.error("文件服务未返回响应数据");
        }
        if (!remoteResult.isSuccess())
        {
            return remoteResult;
        }
        List<Map<String, Object>> fileInfos = toMapList(remoteResult.get(AjaxResult.DATA_TAG));
        if (fileInfos.isEmpty())
        {
            return AjaxResult.error("文件服务未返回文件信息");
        }

        List<String> urls = new ArrayList<>();
        List<String> fileNames = new ArrayList<>();
        List<String> newFileNames = new ArrayList<>();
        List<String> originalFilenames = new ArrayList<>();
        for (Map<String, Object> fileInfo : fileInfos)
        {
            String url = toString(fileInfo.get("url"));
            if (StringUtils.isEmpty(url))
            {
                continue;
            }
            urls.add(url);
            fileNames.add(url);
            newFileNames.add(toString(fileInfo.get("filename")));
            originalFilenames.add(toString(fileInfo.get("originalFilename")));
        }
        AjaxResult ajax = AjaxResult.success(fileInfos);
        ajax.put("urls", StringUtils.join(urls, FILE_DELIMETER));
        ajax.put("fileNames", StringUtils.join(fileNames, FILE_DELIMETER));
        ajax.put("newFileNames", StringUtils.join(newFileNames, FILE_DELIMETER));
        ajax.put("originalFilenames", StringUtils.join(originalFilenames, FILE_DELIMETER));
        return ajax;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 在开始批量上传前校验所有文件，避免列表中后续文件不合规时产生本可避免的部分写入。
     * @param files 本次上传的文件列表
     * @param allowedExtension 该入口允许的文件扩展名
     * @return
     * 返回值: void
     * 返回值说明: 文件列表为空或任一文件不合规时抛出异常
     */
    private void validateFiles(List<MultipartFile> files, String[] allowedExtension) throws Exception
    {
        if (files == null || files.isEmpty())
        {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        for (MultipartFile file : files)
        {
            validateUpload(file, allowedExtension);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 校验单个文件是否为空，以及原始文件名长度、文件大小和扩展名。
     * @param file 要上传的文件
     * @param allowedExtension 该入口允许的文件扩展名
     * @return
     * 返回值: void
     * 返回值说明: 校验失败时抛出相应异常
     */
    private void validateUpload(MultipartFile file, String[] allowedExtension) throws Exception
    {
        if (file == null || file.isEmpty())
        {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        int fileNameLength = Objects.requireNonNull(file.getOriginalFilename()).length();
        if (fileNameLength > FileUploadUtils.DEFAULT_FILE_NAME_LENGTH)
        {
            throw new FileNameLengthLimitExceededException(FileUploadUtils.DEFAULT_FILE_NAME_LENGTH);
        }
        FileUploadUtils.assertAllowed(file, allowedExtension);
    }

    private Map<String, Object> toMap(Object value)
    {
        if (value == null)
        {
            return new HashMap<>();
        }
        return objectMapper.convertValue(value, new TypeReference<Map<String, Object>>()
        {
        });
    }

    private List<Map<String, Object>> toMapList(Object value)
    {
        if (value == null)
        {
            return new ArrayList<>();
        }
        return objectMapper.convertValue(value, new TypeReference<List<Map<String, Object>>>()
        {
        });
    }

    private String toString(Object value)
    {
        return value == null ? "" : String.valueOf(value);
    }
}
