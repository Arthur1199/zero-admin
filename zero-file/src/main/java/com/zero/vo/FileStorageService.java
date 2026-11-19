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
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 文件存储服务，负责选择存储平台并串联上传、删除、下载和记录器。
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

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 查询配置所指向的默认存储平台。
     * @return
     * 返回值: FileStorage
     * 返回值说明: 匹配的存储实例，未启用或未找到时返回 null
     */
    public FileStorage getFileStorage()
    {
        return getFileStorage(properties.getDefaultPlatform());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 在已装配的实例中按平台标识匹配文件存储。
     * @param platform 文件记录或上传配置指定的平台标识
     * @return
     * 返回值: FileStorage
     * 返回值说明: 匹配的存储实例，标识无文本或平台不存在时返回 null
     */
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

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 检查文件信息并解析其原有存储平台，避免用默认平台处理历史文件。
     * @param fileInfo 带有平台标识的文件信息
     * @return
     * 返回值: FileStorage
     * 返回值说明: 实际存储平台，缺少文件信息或平台不可用时抛出异常
     */
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

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 生成存储文件名及缩略图信息，经过上传切面后先保存存储对象，再交给记录器写入元数据。
     * @param pre 包含文件内容、参数及图片预处理结果的对象
     * @return
     * 返回值: FileInfo
     * 返回值说明: 存储及记录均成功时返回文件信息，任一环节返回失败时返回 null
     */
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
            // 平台先回填存储位置，记录器再持久化，避免保存指向未写入对象的记录。
            if (_fileStorage.save(_fileInfo, _pre) && _fileRecorder.record(_fileInfo))
            {
                return _fileInfo;
            }
            return null;
        }).next(fileInfo, pre, fileStorage, fileRecorder);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 通过已配置记录器按文件访问地址查询文件元数据。
     * @param url 文件访问地址
     * @return
     * 返回值: FileInfo
     * 返回值说明: 匹配的文件信息或 null
     */
    public FileInfo getFileInfoByUrl(String url)
    {
        return fileRecorder.getByUrl(url);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 通过访问地址查找文件信息并删除其存储对象及记录。
     * @param url 文件访问地址
     * @return
     * 返回值: boolean
     * 返回值说明: 删除结果，查不到记录时直接返回 true
     */
    public boolean delete(String url)
    {
        return delete(getFileInfoByUrl(url));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 按访问地址查找文件，并在满足指定条件时执行删除。
     * @param url 文件访问地址
     * @param predicate 可选的删除前置条件
     * @return
     * 返回值: boolean
     * 返回值说明: 找不到文件时返回 true，条件拒绝时返回 false，其余返回实际删除结果
     */
    public boolean delete(String url, Predicate<FileInfo> predicate)
    {
        return delete(getFileInfoByUrl(url), predicate);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 根据文件信息删除对象及记录，沿用文件原有存储平台。
     * @param fileInfo 包含平台标识及对象路径的文件信息
     * @return
     * 返回值: boolean
     * 返回值说明: 删除结果，文件信息为空时返回 true
     */
    public boolean delete(FileInfo fileInfo)
    {
        return delete(fileInfo, null);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 执行删除前置条件并选取原存储平台，存储对象删除成功后再清理文件记录。
     * @param fileInfo 待删除的文件信息，可为空
     * @param predicate 可选的删除前置条件
     * @return
     * 返回值: boolean
     * 返回值说明: 无文件信息时返回 true，条件拒绝或删除失败时返回 false
     */
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
            // 对象删除成功后再清理记录，平台返回失败时保留记录供重试。
            if (_fileStorage.delete(_fileInfo))
            {
                return _fileRecorder.delete(_fileInfo.getUrl());
            }
            return false;
        }).next(fileInfo, fileStorage, fileRecorder);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 通过访问地址定位记录并检查对应存储对象是否存在。
     * @param url 文件访问地址
     * @return
     * 返回值: boolean
     * 返回值说明: 原存储平台确认对象存在时返回 true
     */
    public boolean exists(String url)
    {
        return exists(getFileInfoByUrl(url));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 通过切面链查询原存储平台上的文件是否仍存在。
     * @param fileInfo 携带平台和存储路径的文件信息
     * @return
     * 返回值: boolean
     * 返回值说明: 对象存在时返回 true，未提供文件信息时返回 false
     */
    public boolean exists(FileInfo fileInfo)
    {
        if (fileInfo == null)
        {
            return false;
        }
        return new ExistsAspectChain(aspectList, (_fileInfo, _fileStorage) -> _fileStorage.exists(_fileInfo))
                .next(fileInfo, getFileStorageVerify(fileInfo));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 校验文件及其存储平台并创建原文件下载器。
     * @param fileInfo 携带平台和原文件位置的文件信息
     * @return
     * 返回值: Downloader
     * 返回值说明: 等待执行的原文件下载器
     */
    public Downloader download(FileInfo fileInfo)
    {
        return new Downloader(fileInfo, aspectList, getFileStorageVerify(fileInfo), Downloader.TARGET_FILE);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 通过访问地址查找记录并构造原文件下载器。
     * @param url 原文件记录的访问地址
     * @return
     * 返回值: Downloader
     * 返回值说明: 待执行的下载器，无法定位文件或平台时抛出异常
     */
    public Downloader download(String url)
    {
        return download(getFileInfoByUrl(url));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 按文件所属平台创建缩略图下载器。
     * @param fileInfo 携带平台和缩略图位置的文件信息
     * @return
     * 返回值: Downloader
     * 返回值说明: 等待执行的缩略图下载器
     */
    public Downloader downloadTh(FileInfo fileInfo)
    {
        return new Downloader(fileInfo, aspectList, getFileStorageVerify(fileInfo), Downloader.TARGET_TH_FILE);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 从文件记录中的缩略图信息构造下载器。
     * @param url 用于定位文件记录的访问地址
     * @return
     * 返回值: Downloader
     * 返回值说明: 待执行的缩略图下载器，无法定位文件或平台时抛出异常
     */
    public Downloader downloadTh(String url)
    {
        return downloadTh(getFileInfoByUrl(url));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 创建未绑定文件的上传预处理对象，先填入默认平台和缩略图后缀。
     * @return
     * 返回值: UploadPretreatment
     * 返回值说明: 上传预处理对象
     */
    public UploadPretreatment of()
    {
        UploadPretreatment pre = new UploadPretreatment();
        pre.setFileStorageService(self == null ? this : self);
        pre.setPlatform(properties.getDefaultPlatform());
        pre.setThumbnailSuffix(properties.getThumbnailSuffix());
        return pre;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 按默认平台创建预处理对象，并接入原有 multipart 文件。
     * @param file 已接收的 multipart 文件
     * @return
     * 返回值: UploadPretreatment
     * 返回值说明: 持有上传文件的预处理对象
     */
    public UploadPretreatment of(MultipartFile file)
    {
        UploadPretreatment pre = of();
        pre.setFileWrapper(new MultipartFileWrapper(file));
        return pre;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 将字节数组作为上传内容并装配默认平台。
     * @param bytes 上传内容字节
     * @return
     * 返回值: UploadPretreatment
     * 返回值说明: 持有上传内容的预处理对象
     */
    public UploadPretreatment of(byte[] bytes)
    {
        UploadPretreatment pre = of();
        pre.setFileWrapper(new MultipartFileWrapper(new MockMultipartFile("", bytes)));
        return pre;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 读取给定输入流并装配默认平台，读取异常转换为存储异常。
     * @param in 提供上传内容的输入流
     * @return
     * 返回值: UploadPretreatment
     * 返回值说明: 持有上传内容的预处理对象
     */
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

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 读取本地文件并保留本地文件名，读取异常转换为存储异常。
     * @param file 要上传的本地文件
     * @return
     * 返回值: UploadPretreatment
     * 返回值说明: 持有文件内容的预处理对象
     */
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

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 打开 URL 内容流并创建上传预处理对象。
     * @param url 要读取内容的地址
     * @return
     * 返回值: UploadPretreatment
     * 返回值说明: 持有读取结果的预处理对象，读取失败时抛出存储异常
     */
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

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 将 URI 转换为 URL 后读取要上传的内容。
     * @param uri 要转成 URL 的内容地址
     * @return
     * 返回值: UploadPretreatment
     * 返回值说明: 持有读取结果的预处理对象，解析或读取失败时抛出存储异常
     */
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

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 将字符串地址解析为 URL 并读取其内容。
     * @param url 表示内容地址的字符串
     * @return
     * 返回值: UploadPretreatment
     * 返回值说明: 持有读取结果的预处理对象，解析或读取失败时抛出存储异常
     */
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
