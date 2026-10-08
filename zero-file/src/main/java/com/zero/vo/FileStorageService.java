package com.zero.vo;

import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import com.zero.aspect.DeleteAspectChain;
import com.zero.aspect.ExistsAspectChain;
import com.zero.aspect.FileStorageAspect;
import com.zero.aspect.UploadAspectChain;
import com.zero.config.FileStorageProperties;
import com.zero.exception.FileStorageRuntimeException;
import com.zero.platform.FileStorage;
import com.zero.recorder.FileRecorder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

/**
 * 文件存储服务，负责选择存储平台并串联上传、删除、下载和记录器。
 */
@Getter
@Setter
public class FileStorageService
{
    private FileStorageService self;

    private FileRecorder fileRecorder;

    private CopyOnWriteArrayList<FileStorage> fileStorageList;

    private FileStorageProperties properties;

    private CopyOnWriteArrayList<FileStorageAspect> aspectList;

    public FileStorage getFileStorage()
    {
        return getFileStorage(properties.getDefaultPlatform());
    }

    public FileStorage getFileStorage(String platform)
    {
        if (!StrUtil.isNotBlank(platform) || fileStorageList == null)
        {
            return null;
        }
        for (FileStorage fileStorage : fileStorageList)
        {
            if (platform.equals(fileStorage.getPlatform()))
            {
                return fileStorage;
            }
        }
        return null;
    }

    public FileStorage getFileStorageVerify(FileInfo fileInfo)
    {
        if (fileInfo == null)
        {
            throw new FileStorageRuntimeException("文件信息不能为空");
        }
        FileStorage fileStorage = getFileStorage(fileInfo.getPlatform());
        if (fileStorage == null)
        {
            throw new FileStorageRuntimeException("未找到对应的文件存储平台");
        }
        return fileStorage;
    }

    public FileInfo upload(UploadPretreatment pre)
    {
        if (pre == null || pre.getFileWrapper() == null)
        {
            throw new FileStorageRuntimeException("上传文件不能为空");
        }
        if (!StrUtil.isNotBlank(pre.getPlatform()))
        {
            throw new FileStorageRuntimeException("文件存储平台不能为空");
        }
        MultipartFile file = pre.getFileWrapper();

        FileInfo fileInfo = new FileInfo();
        fileInfo.setCreateTime(new Date());
        fileInfo.setSize(file.getSize());
        fileInfo.setOriginalFilename(file.getOriginalFilename());
        fileInfo.setExt(FileNameUtil.getSuffix(file.getOriginalFilename()));
        fileInfo.setObjectId(pre.getObjectId());
        fileInfo.setObjectType(pre.getObjectType());
        fileInfo.setPath(pre.getPath());
        fileInfo.setPlatform(pre.getPlatform());
        if (StrUtil.isNotBlank(pre.getSaveFilename()))
        {
            fileInfo.setFilename(pre.getSaveFilename());
        }
        else
        {
            fileInfo.setFilename(IdUtil.objectId() + (StrUtil.isEmpty(fileInfo.getExt()) ? StrUtil.EMPTY : "." + fileInfo.getExt()));
        }

        byte[] thumbnailBytes = pre.getThumbnailBytes();
        if (thumbnailBytes != null)
        {
            fileInfo.setThSize((long) thumbnailBytes.length);
            if (StrUtil.isNotBlank(pre.getSaveThFilename()))
            {
                fileInfo.setThFilename(pre.getSaveThFilename() + pre.getThumbnailSuffix());
            }
            else
            {
                fileInfo.setThFilename(fileInfo.getFilename() + pre.getThumbnailSuffix());
            }
        }

        FileStorage fileStorage = getFileStorage(pre.getPlatform());
        if (fileStorage == null)
        {
            throw new FileStorageRuntimeException("未找到对应的文件存储平台");
        }

        return new UploadAspectChain(aspectList, (_fileInfo, _pre, _fileStorage, _fileRecorder) -> {
            if (_fileStorage.save(_fileInfo, _pre) && _fileRecorder.record(_fileInfo))
            {
                return _fileInfo;
            }
            return null;
        }).next(fileInfo, pre, fileStorage, fileRecorder);
    }

    public FileInfo getFileInfoByUrl(String url)
    {
        return fileRecorder.getByUrl(url);
    }

    public boolean delete(String url)
    {
        return delete(getFileInfoByUrl(url));
    }

    public boolean delete(String url, Predicate<FileInfo> predicate)
    {
        return delete(getFileInfoByUrl(url), predicate);
    }

    public boolean delete(FileInfo fileInfo)
    {
        return delete(fileInfo, null);
    }

    public boolean delete(FileInfo fileInfo, Predicate<FileInfo> predicate)
    {
        if (fileInfo == null)
        {
            return true;
        }
        if (predicate != null && !predicate.test(fileInfo))
        {
            return false;
        }
        FileStorage fileStorage = getFileStorageVerify(fileInfo);
        return new DeleteAspectChain(aspectList, (_fileInfo, _fileStorage, _fileRecorder) -> {
            if (_fileStorage.delete(_fileInfo))
            {
                return _fileRecorder.delete(_fileInfo.getUrl());
            }
            return false;
        }).next(fileInfo, fileStorage, fileRecorder);
    }

    public boolean exists(String url)
    {
        return exists(getFileInfoByUrl(url));
    }

    public boolean exists(FileInfo fileInfo)
    {
        if (fileInfo == null)
        {
            return false;
        }
        return new ExistsAspectChain(aspectList, (_fileInfo, _fileStorage) -> _fileStorage.exists(_fileInfo))
                .next(fileInfo, getFileStorageVerify(fileInfo));
    }

    public Downloader download(FileInfo fileInfo)
    {
        return new Downloader(fileInfo, aspectList, getFileStorageVerify(fileInfo), Downloader.TARGET_FILE);
    }

    public Downloader download(String url)
    {
        return download(getFileInfoByUrl(url));
    }

    public Downloader downloadTh(FileInfo fileInfo)
    {
        return new Downloader(fileInfo, aspectList, getFileStorageVerify(fileInfo), Downloader.TARGET_TH_FILE);
    }

    public Downloader downloadTh(String url)
    {
        return downloadTh(getFileInfoByUrl(url));
    }

    public UploadPretreatment of()
    {
        UploadPretreatment pre = new UploadPretreatment();
        pre.setFileStorageService(self == null ? this : self);
        pre.setPlatform(properties.getDefaultPlatform());
        pre.setThumbnailSuffix(properties.getThumbnailSuffix());
        return pre;
    }

    public UploadPretreatment of(MultipartFile file)
    {
        UploadPretreatment pre = of();
        pre.setFileWrapper(new MultipartFileWrapper(file));
        return pre;
    }

    public UploadPretreatment of(byte[] bytes)
    {
        UploadPretreatment pre = of();
        pre.setFileWrapper(new MultipartFileWrapper(new MockMultipartFile("", bytes)));
        return pre;
    }

    public UploadPretreatment of(InputStream in)
    {
        try
        {
            UploadPretreatment pre = of();
            pre.setFileWrapper(new MultipartFileWrapper(new MockMultipartFile("", in)));
            return pre;
        }
        catch (Exception e)
        {
            throw new FileStorageRuntimeException("创建上传对象失败", e);
        }
    }

    public UploadPretreatment of(File file)
    {
        try
        {
            UploadPretreatment pre = of();
            pre.setFileWrapper(new MultipartFileWrapper(new MockMultipartFile(file.getName(), file.getName(), null, new FileInputStream(file))));
            return pre;
        }
        catch (Exception e)
        {
            throw new FileStorageRuntimeException("创建上传对象失败", e);
        }
    }

    public UploadPretreatment of(URL url)
    {
        try
        {
            UploadPretreatment pre = of();
            pre.setFileWrapper(new MultipartFileWrapper(new MockMultipartFile("", url.openStream())));
            return pre;
        }
        catch (Exception e)
        {
            throw new FileStorageRuntimeException("创建上传对象失败", e);
        }
    }

    public UploadPretreatment of(URI uri)
    {
        try
        {
            return of(uri.toURL());
        }
        catch (Exception e)
        {
            throw new FileStorageRuntimeException("创建上传对象失败", e);
        }
    }

    public UploadPretreatment of(String url)
    {
        try
        {
            return of(URLUtil.url(url));
        }
        catch (Exception e)
        {
            throw new FileStorageRuntimeException("创建上传对象失败", e);
        }
    }
}
