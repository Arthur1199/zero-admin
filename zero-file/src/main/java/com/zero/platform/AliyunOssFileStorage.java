package com.zero.platform;

import cn.hutool.core.util.StrUtil;
import com.zero.exception.FileStorageRuntimeException;
import com.zero.vo.FileInfo;
import com.zero.vo.UploadPretreatment;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.OSSObject;
import lombok.Getter;
import lombok.Setter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

/**
 * 阿里云 OSS 存储。
 */
@Getter
@Setter
public class AliyunOssFileStorage implements FileStorage {

    /* 存储平台 */
    private String platform;
    private String accessKey;
    private String secretKey;
    private String endPoint;
    private String bucketName;
    private String domain;
    private String basePath;

    public OSS getOss() {
        return new OSSClientBuilder().build(endPoint,accessKey,secretKey);
    }

    /**
     * 关闭客户端。
     */
    public void shutdown(OSS oss) {
        if (oss != null) oss.shutdown();
    }

    @Override
    public boolean save(FileInfo fileInfo,UploadPretreatment pre) {
        String newFileKey = basePath + fileInfo.getPath() + fileInfo.getFilename();
        fileInfo.setBasePath(basePath);
        fileInfo.setUrl(domain + newFileKey);

        OSS oss = getOss();
        try {
            oss.putObject(bucketName,newFileKey,pre.getFileWrapper().getInputStream());

            byte[] thumbnailBytes = pre.getThumbnailBytes();
            if (thumbnailBytes != null) { // 上传缩略图
                String newThFileKey = basePath + fileInfo.getPath() + fileInfo.getThFilename();
                fileInfo.setThUrl(domain + newThFileKey);
                oss.putObject(bucketName,newThFileKey,new ByteArrayInputStream(thumbnailBytes));
            }

            return true;
        } catch (IOException e) {
            oss.deleteObject(bucketName,newFileKey);
            throw new FileStorageRuntimeException("文件存储操作失败", e);
        } finally {
            shutdown(oss);
        }
    }

    @Override
    public boolean delete(FileInfo fileInfo) {
        OSS oss = getOss();
        try {
            if (fileInfo.getThFilename() != null) {   // 删除缩略图
                oss.deleteObject(bucketName,fileInfo.getBasePath() + fileInfo.getPath() + fileInfo.getThFilename());
            }
            oss.deleteObject(bucketName,fileInfo.getBasePath() + fileInfo.getPath() + fileInfo.getFilename());
            return true;
        } finally {
            shutdown(oss);
        }
    }


    @Override
    public boolean exists(FileInfo fileInfo) {
        OSS oss = getOss();
        try {
            return oss.doesObjectExist(bucketName,fileInfo.getBasePath() + fileInfo.getPath() + fileInfo.getFilename());
        } finally {
            shutdown(oss);
        }
    }

    @Override
    public void download(FileInfo fileInfo,Consumer<InputStream> consumer) {
        OSS oss = getOss();
        try {
            OSSObject object = oss.getObject(bucketName,fileInfo.getBasePath() + fileInfo.getPath() + fileInfo.getFilename());
            try (InputStream in = object.getObjectContent()) {
                consumer.accept(in);
            } catch (IOException e) {
                throw new FileStorageRuntimeException("文件存储操作失败", e);
            }
        } finally {
            shutdown(oss);
        }
    }

    @Override
    public void downloadTh(FileInfo fileInfo,Consumer<InputStream> consumer) {
        if (StrUtil.isBlank(fileInfo.getThFilename())) {
            throw new FileStorageRuntimeException("文件不存在或无法操作");
        }
        OSS oss = getOss();
        try {
            OSSObject object = oss.getObject(bucketName,fileInfo.getBasePath() + fileInfo.getPath() + fileInfo.getThFilename());
            try (InputStream in = object.getObjectContent()) {
                consumer.accept(in);
            } catch (IOException e) {
                throw new FileStorageRuntimeException("文件存储操作失败", e);
            }
        } finally {
            shutdown(oss);
        }
    }
}

