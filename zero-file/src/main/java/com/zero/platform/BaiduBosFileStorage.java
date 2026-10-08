package com.zero.platform;

import cn.hutool.core.util.StrUtil;
import com.zero.exception.FileStorageRuntimeException;
import com.zero.vo.FileInfo;
import com.zero.vo.UploadPretreatment;

import com.baidubce.Protocol;
import com.baidubce.auth.DefaultBceCredentials;
import com.baidubce.services.bos.BosClient;
import com.baidubce.services.bos.BosClientConfiguration;
import com.baidubce.services.bos.model.BosObject;
import lombok.Getter;
import lombok.Setter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

/**
 * 百度云 BOS 存储。
 */
@Getter
@Setter
public class BaiduBosFileStorage implements FileStorage {

    /* 存储平台 */
    private String platform;
    private String accessKey;
    private String secretKey;
    private String endPoint;
    private String bucketName;
    private String domain;
    private String basePath;

    public BosClient getBos() {
        BosClientConfiguration config = new BosClientConfiguration();
        config.setCredentials(new DefaultBceCredentials(accessKey,secretKey));
        config.setEndpoint(endPoint);
        config.setProtocol(Protocol.HTTPS);
        return new BosClient(config);
    }

    /**
     * 关闭客户端。
     */
    public void shutdown(BosClient bos) {
        if (bos != null) bos.shutdown();
    }

    @Override
    public boolean save(FileInfo fileInfo,UploadPretreatment pre) {
        String newFileKey = basePath + fileInfo.getPath() + fileInfo.getFilename();
        fileInfo.setBasePath(basePath);
        fileInfo.setUrl(domain + newFileKey);

        BosClient bos = getBos();
        try {
            bos.putObject(bucketName,newFileKey,pre.getFileWrapper().getInputStream());

            byte[] thumbnailBytes = pre.getThumbnailBytes();
            if (thumbnailBytes != null) { // 上传缩略图
                String newThFileKey = basePath + fileInfo.getPath() + fileInfo.getThFilename();
                fileInfo.setThUrl(domain + newThFileKey);
                bos.putObject(bucketName,newThFileKey,new ByteArrayInputStream(thumbnailBytes));
            }

            return true;
        } catch (IOException e) {
            bos.deleteObject(bucketName,newFileKey);
            throw new FileStorageRuntimeException("文件存储操作失败", e);
        } finally {
            shutdown(bos);
        }
    }

    @Override
    public boolean delete(FileInfo fileInfo) {
        BosClient oss = getBos();
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
        BosClient oss = getBos();
        try {
            return oss.doesObjectExist(bucketName,fileInfo.getBasePath() + fileInfo.getPath() + fileInfo.getFilename());
        } finally {
            shutdown(oss);
        }
    }

    @Override
    public void download(FileInfo fileInfo,Consumer<InputStream> consumer) {
        BosClient bos = getBos();
        try {
            BosObject object = bos.getObject(bucketName,fileInfo.getBasePath() + fileInfo.getPath() + fileInfo.getFilename());
            try (InputStream in = object.getObjectContent()) {
                consumer.accept(in);
            } catch (IOException e) {
                throw new FileStorageRuntimeException("文件存储操作失败", e);
            }
        } finally {
            shutdown(bos);
        }
    }

    @Override
    public void downloadTh(FileInfo fileInfo,Consumer<InputStream> consumer) {
        if (StrUtil.isBlank(fileInfo.getThFilename())) {
            throw new FileStorageRuntimeException("文件不存在或无法操作");
        }
        BosClient bos = getBos();
        try {
            BosObject object = bos.getObject(bucketName,fileInfo.getBasePath() + fileInfo.getPath() + fileInfo.getThFilename());
            try (InputStream in = object.getObjectContent()) {
                consumer.accept(in);
            } catch (IOException e) {
                throw new FileStorageRuntimeException("文件存储操作失败", e);
            }
        } finally {
            shutdown(bos);
        }
    }
}

