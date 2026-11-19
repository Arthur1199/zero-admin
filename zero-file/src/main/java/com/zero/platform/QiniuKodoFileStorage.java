package com.zero.platform;

import cn.hutool.core.util.StrUtil;
import com.zero.exception.FileStorageRuntimeException;
import com.zero.vo.FileInfo;
import com.zero.vo.UploadPretreatment;

import com.qiniu.common.QiniuException;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import lombok.Getter;
import lombok.Setter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.function.Consumer;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 七牛云 Kodo 存储。
 */
@Getter
@Setter
public class QiniuKodoFileStorage implements FileStorage {

    /* 存储平台 */
    private String platform;
    private String accessKey;
    private String secretKey;
    private String bucketName;
    private String domain;
    private String basePath;
    private Region region;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 为当前配置的存储桶生成上传凭据。
     * @return
     * 返回值: String
     * 返回值说明: 本次操作使用的上传凭据，禁止写入日志
     */
    public String getToken() {
        return getAuth().uploadToken(bucketName);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 用当前认证配置构造七牛 SDK 授权对象。
     * @return
     * 返回值: Auth
     * 返回值说明: 用于生成凭据或调用七牛对象接口的认证对象
     */
    public Auth getAuth() {
        return Auth.create(accessKey,secretKey);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 使用当前授权配置创建七牛桶管理客户端。
     * @return
     * 返回值: BucketManager
     * 返回值说明: 提供对象查询等桶操作的管理客户端
     */
    public BucketManager getBucketManager() {
        return new BucketManager(getAuth(),new Configuration(Region.autoRegion()));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 使用自动区域配置创建七牛上传客户端。
     * @return
     * 返回值: UploadManager
     * 返回值说明: 供上传流程写入对象的客户端
     */
    public UploadManager getUploadManager() {
        return new UploadManager(new Configuration(Region.autoRegion()));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 通过 七牛云 Kodo 存储原文件及可选的缩略图，并更新文件信息中的存储位置。
     * @param fileInfo 用于回填存储位置的文件信息
     * @param pre 含原文件内容及可选缩略图的预处理结果
     * @return
     * 返回值: boolean
     * 返回值说明: 当前平台的保存结果
     */
    @Override
    public boolean save(FileInfo fileInfo,UploadPretreatment pre) {
        String newFileKey = basePath + fileInfo.getPath() + fileInfo.getFilename();
        fileInfo.setBasePath(basePath);
        fileInfo.setUrl(domain + newFileKey);

        try {
            UploadManager uploadManager = getUploadManager();
            String token = getToken();
            uploadManager.put(pre.getFileWrapper().getInputStream(),newFileKey,token,null,null);

            byte[] thumbnailBytes = pre.getThumbnailBytes();
            if (thumbnailBytes != null) { // 上传缩略图
                String newThFileKey = basePath + fileInfo.getPath() + fileInfo.getThFilename();
                fileInfo.setThUrl(domain + newThFileKey);
                uploadManager.put(new ByteArrayInputStream(thumbnailBytes),newThFileKey,token,null,null);
            }

            return true;
        } catch (IOException e) {
            try {
                getBucketManager().delete(bucketName,newFileKey);
            } catch (QiniuException ignored) {
            }
            throw new FileStorageRuntimeException("文件存储操作失败", e);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 按历史记录中的存储路径删除 七牛云 Kodo 的对象及关联缩略图。
     * @param fileInfo 携带原文件及缩略图路径信息的文件记录
     * @return
     * 返回值: boolean
     * 返回值说明: 当前平台的删除结果
     */
    @Override
    public boolean delete(FileInfo fileInfo) {
        BucketManager manager = getBucketManager();
        try {
            if (fileInfo.getThFilename() != null) {   // 删除缩略图
                manager.delete(bucketName,fileInfo.getBasePath() + fileInfo.getPath() + fileInfo.getThFilename());
            }
            manager.delete(bucketName,fileInfo.getBasePath() + fileInfo.getPath() + fileInfo.getFilename());
        } catch (QiniuException e) {
            throw new FileStorageRuntimeException("文件存储操作失败", e);
        }
        return true;
    }


    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 检查 七牛云 Kodo 是否仍可找到文件记录指向的原文件。
     * @param fileInfo 包含原文件位置的文件信息
     * @return
     * 返回值: boolean
     * 返回值说明: 原文件存在时返回 true
     */
    @Override
    public boolean exists(FileInfo fileInfo) {
        BucketManager manager = getBucketManager();
        try {
            com.qiniu.storage.model.FileInfo stat = manager.stat(bucketName,fileInfo.getBasePath() + fileInfo.getPath() + fileInfo.getFilename());
            if (stat != null && stat.md5 != null) return true;
        } catch (QiniuException e) {
            throw new FileStorageRuntimeException("文件存储操作失败", e);
        }
        return false;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 从 七牛云 Kodo 读取文件记录指向的原文件。
     * @param fileInfo 包含原文件位置的文件信息
     * @param consumer 用于读取输入流的回调
     * @return
     * 返回值: void
     * 返回值说明: 在输入流有效期间调用消费回调
     */
    @Override
    public void download(FileInfo fileInfo,Consumer<InputStream> consumer) {
        String url = getAuth().privateDownloadUrl(fileInfo.getUrl());
        try (InputStream in = new URL(url).openStream()) {
            consumer.accept(in);
        } catch (IOException e) {
            throw new FileStorageRuntimeException("文件存储操作失败", e);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 检查缩略图位置，并从 七牛云 Kodo 读取缩略图。
     * @param fileInfo 包含缩略图位置的文件信息
     * @param consumer 用于读取缩略图输入流的回调
     * @return
     * 返回值: void
     * 返回值说明: 读取后在输入流有效期间调用消费回调
     */
    @Override
    public void downloadTh(FileInfo fileInfo,Consumer<InputStream> consumer) {
        if (StrUtil.isBlank(fileInfo.getThUrl())) {
            throw new FileStorageRuntimeException("文件不存在或无法操作");
        }
        String url = getAuth().privateDownloadUrl(fileInfo.getThUrl());
        try (InputStream in = new URL(url).openStream()) {
            consumer.accept(in);
        } catch (IOException e) {
            throw new FileStorageRuntimeException("文件存储操作失败", e);
        }
    }
}

