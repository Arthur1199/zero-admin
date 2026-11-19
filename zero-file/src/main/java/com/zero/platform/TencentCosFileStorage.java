package com.zero.platform;

import cn.hutool.core.util.StrUtil;
import com.zero.exception.FileStorageRuntimeException;
import com.zero.vo.FileInfo;
import com.zero.vo.UploadPretreatment;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.region.Region;
import lombok.Getter;
import lombok.Setter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 腾讯云 COS 存储。
 */
@Getter
@Setter
public class TencentCosFileStorage implements FileStorage {

    /* 存储平台 */
    private String platform;
    private String secretId;
    private String secretKey;
    private String region;
    private String bucketName;
    private String domain;
    private String basePath;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 用当前区域及认证配置创建使用 HTTPS 的 COS 客户端。
     * @return
     * 返回值: COSClient
     * 返回值说明: 用于腾讯云对象操作的 COS 客户端
     */
    public COSClient getCos() {
        COSCredentials cred = new BasicCOSCredentials(secretId,secretKey);
        ClientConfig clientConfig = new ClientConfig(new Region(region));
        clientConfig.setHttpProtocol(HttpProtocol.https);
        return new COSClient(cred,clientConfig);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 关闭之前使用的 腾讯云 COS SDK 客户端。
     * @param cos 待关闭的 腾讯云 COS 客户端
     * @return
     * 返回值: void
     * 返回值说明: 客户端存在时释放其资源
     */
    public void shutdown(COSClient cos) {
        if (cos != null) cos.shutdown();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 通过 腾讯云 COS 存储原文件及可选的缩略图，并更新文件信息中的存储位置。
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

        COSClient cos = getCos();
        try {
            cos.putObject(bucketName,newFileKey,pre.getFileWrapper().getInputStream(),null);

            byte[] thumbnailBytes = pre.getThumbnailBytes();
            if (thumbnailBytes != null) { // 上传缩略图
                String newThFileKey = basePath + fileInfo.getPath() + fileInfo.getThFilename();
                fileInfo.setThUrl(domain + newThFileKey);
                cos.putObject(bucketName,newThFileKey,new ByteArrayInputStream(thumbnailBytes),null);
            }

            return true;
        } catch (IOException e) {
            cos.deleteObject(bucketName,newFileKey);
            throw new FileStorageRuntimeException("文件存储操作失败", e);
        } finally {
            shutdown(cos);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 按历史记录中的存储路径删除 腾讯云 COS 的对象及关联缩略图。
     * @param fileInfo 携带原文件及缩略图路径信息的文件记录
     * @return
     * 返回值: boolean
     * 返回值说明: 当前平台的删除结果
     */
    @Override
    public boolean delete(FileInfo fileInfo) {
        COSClient cos = getCos();
        try {
            if (fileInfo.getThFilename() != null) {   // 删除缩略图
                cos.deleteObject(bucketName,fileInfo.getBasePath() + fileInfo.getPath() + fileInfo.getThFilename());
            }
            cos.deleteObject(bucketName,fileInfo.getBasePath() + fileInfo.getPath() + fileInfo.getFilename());
            return true;
        } finally {
            shutdown(cos);
        }
    }


    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 检查 腾讯云 COS 是否仍可找到文件记录指向的原文件。
     * @param fileInfo 包含原文件位置的文件信息
     * @return
     * 返回值: boolean
     * 返回值说明: 原文件存在时返回 true
     */
    @Override
    public boolean exists(FileInfo fileInfo) {
        COSClient cos = getCos();
        try {
            return cos.doesObjectExist(bucketName,fileInfo.getBasePath() + fileInfo.getPath() + fileInfo.getFilename());
        } finally {
            shutdown(cos);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 从 腾讯云 COS 读取文件记录指向的原文件。
     * @param fileInfo 包含原文件位置的文件信息
     * @param consumer 用于读取输入流的回调
     * @return
     * 返回值: void
     * 返回值说明: 在输入流有效期间调用消费回调
     */
    @Override
    public void download(FileInfo fileInfo,Consumer<InputStream> consumer) {
        COSClient cos = getCos();
        try {
            COSObject object = cos.getObject(bucketName,fileInfo.getBasePath() + fileInfo.getPath() + fileInfo.getFilename());
            try (InputStream in = object.getObjectContent()) {
                consumer.accept(in);
            } catch (IOException e) {
                throw new FileStorageRuntimeException("文件存储操作失败", e);
            }
        } finally {
            shutdown(cos);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 检查缩略图位置，并从 腾讯云 COS 读取缩略图。
     * @param fileInfo 包含缩略图位置的文件信息
     * @param consumer 用于读取缩略图输入流的回调
     * @return
     * 返回值: void
     * 返回值说明: 读取后在输入流有效期间调用消费回调
     */
    @Override
    public void downloadTh(FileInfo fileInfo,Consumer<InputStream> consumer) {
        if (StrUtil.isBlank(fileInfo.getThFilename())) {
            throw new FileStorageRuntimeException("文件不存在或无法操作");
        }
        COSClient cos = getCos();
        try {
            COSObject object = cos.getObject(bucketName,fileInfo.getBasePath() + fileInfo.getPath() + fileInfo.getThFilename());
            try (InputStream in = object.getObjectContent()) {
                consumer.accept(in);
            } catch (IOException e) {
                throw new FileStorageRuntimeException("文件存储操作失败", e);
            }
        } finally {
            shutdown(cos);
        }
    }
}

