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

@Service
public class AdminFileUploadServiceImpl implements IAdminFileUploadService
{
    private static final String FILE_DELIMETER = ",";

    private static final String DEFAULT_OBJECT_ID = "0";

    private static final String OBJECT_TYPE_COMMON = "admin-common";

    private static final String OBJECT_TYPE_AVATAR = "admin-avatar";

    private static final String OBJECT_TYPE_MANAGEMENT = "file-management";

    @Autowired
    private IAdminFileStorageModeService storageModeService;

    @Autowired
    private FileRestService fileRestService;

    @Autowired
    private ServerConfig serverConfig;

    @Autowired
    private ObjectMapper objectMapper;

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

    @Override
    public AjaxResult uploadManagementFile(MultipartFile file) throws Exception
    {
        return uploadRemoteFile("/file/upload", file, OBJECT_TYPE_MANAGEMENT, MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION);
    }

    private AjaxResult uploadRemoteFile(String requestPath, MultipartFile file, String objectType,
            String[] allowedExtension) throws Exception
    {
        validateUpload(file, allowedExtension);
        AjaxResult remoteResult = fileRestService.uploadFile(requestPath, "file", file, remoteParams(objectType));
        return buildCompatibleFileResult(remoteResult);
    }

    private Map<String, Object> remoteParams(String objectType)
    {
        Map<String, Object> params = new HashMap<>();
        params.put("objectId", DEFAULT_OBJECT_ID);
        params.put("objectType", objectType);
        return params;
    }

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
