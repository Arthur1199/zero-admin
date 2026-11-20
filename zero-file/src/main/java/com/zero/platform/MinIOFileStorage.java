package com.zero.platform;

import lombok.extern.slf4j.Slf4j;
import cn.hutool.core.util.StrUtil;
import com.zero.exception.FileStorageRuntimeException;
import com.zero.vo.FileInfo;
import com.zero.vo.UploadPretreatment;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Optional;
import java.util.function.Consumer;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: MinIO 存储。
 */
@Slf4j
@Getter
@Setter
public class MinIOFileStorage implements FileStorage {
    /* 存储平台 */
    private String platform;
    private String accessKey;
    private String secretKey;
    private String endPoint;
    private String bucketName;
    private String domain;
    private String basePath;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 使用当前 MinIO 终端及认证配置创建客户端。
     * @return
     * 返回值: MinioClient
     * 返回值说明: 对象操作客户端，无效终端或端口会转换为存储异常
     */
    public MinioClient getClient() {
        try {
            return MinioClient.builder().endpoint(endPoint).credentials(accessKey, secretKey).build();
        } catch (IllegalArgumentException e) {
            throw new FileStorageRuntimeException("文件存储操作失败", e);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 通过 MinIO 存储原文件及可选的缩略图，并更新文件信息中的存储位置。
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

        try (MinioClient client = getClient()) {
            Optional<MediaType> mediaType = MediaTypeFactory.getMediaType(fileInfo.getFilename());
            MediaType contentType = mediaType.orElse(MediaType.APPLICATION_OCTET_STREAM);
            log.info("上传文件类型，filename={}, contentType={}", fileInfo.getFilename(), contentType.toString());

            try (InputStream in = pre.getFileWrapper().getInputStream()) {
                client.putObject(PutObjectArgs.builder().bucket(bucketName).object(newFileKey)
                        .stream(in, pre.getFileWrapper().getSize(), -1).contentType(contentType.toString()).build());
            }

            byte[] thumbnailBytes = pre.getThumbnailBytes();
            if (thumbnailBytes != null) {
                String newThFileKey = basePath + fileInfo.getPath() + fileInfo.getThFilename();
                fileInfo.setThUrl(domain + newThFileKey);
                try {
                    client.putObject(PutObjectArgs.builder().bucket(bucketName).object(newThFileKey)
                            .stream(new ByteArrayInputStream(thumbnailBytes), thumbnailBytes.length, -1)
                            .contentType(contentType.toString()).build());
                } catch (Exception uploadFailure) {
                    // 缩略图上传失败时清理本次已经写入的原件，保留上传异常。
                    try {
                        client.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(newFileKey).build());
                    } catch (Exception cleanupFailure) {
                        uploadFailure.addSuppressed(cleanupFailure);
                    }
                    throw uploadFailure;
                }
            }

            return true;
        } catch (Exception e) {
            throw new FileStorageRuntimeException("文件存储操作失败", e);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 按历史记录中的存储路径删除 MinIO 的对象及关联缩略图。
     * @param fileInfo 携带原文件及缩略图路径信息的文件记录
     * @return
     * 返回值: boolean
     * 返回值说明: 当前平台的删除结果
     */
    @Override
    public boolean delete(FileInfo fileInfo) {
        String directory = fileInfo.getBasePath() + fileInfo.getPath();
        try (MinioClient client = getClient()) {
            if (fileInfo.getThFilename() != null) {
                client.removeObject(RemoveObjectArgs.builder().bucket(bucketName)
                        .object(directory + fileInfo.getThFilename()).build());
            }
            client.removeObject(RemoveObjectArgs.builder().bucket(bucketName)
                    .object(directory + fileInfo.getFilename()).build());
            return true;
        } catch (Exception e) {
            throw new FileStorageRuntimeException("文件存储操作失败", e);
        }
    }


    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 检查 MinIO 是否仍可找到文件记录指向的原文件。
     * @param fileInfo 包含原文件位置的文件信息
     * @return
     * 返回值: boolean
     * 返回值说明: 原文件存在时返回 true
     */
    @Override
    public boolean exists(FileInfo fileInfo) {
        try (MinioClient client = getClient()) {
            var stat = client.statObject(StatObjectArgs.builder().bucket(bucketName)
                    .object(fileInfo.getBasePath() + fileInfo.getPath() + fileInfo.getFilename()).build());
            return stat.lastModified() != null;
        } catch (ErrorResponseException e) {
            String code = e.errorResponse().code();
            if ("NoSuchKey".equals(code) || "NoSuchObject".equals(code) || "ResourceNotFound".equals(code)) {
                return false;
            }
            throw new FileStorageRuntimeException("文件存储操作失败", e);
        } catch (Exception e) {
            throw new FileStorageRuntimeException("文件存储操作失败", e);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 从 MinIO 读取文件记录指向的原文件。
     * @param fileInfo 包含原文件位置的文件信息
     * @param consumer 用于读取输入流的回调
     * @return
     * 返回值: void
     * 返回值说明: 在输入流有效期间调用消费回调
     */
    @Override
    public void download(FileInfo fileInfo,Consumer<InputStream> consumer) {
        downloadObject(fileInfo.getBasePath() + fileInfo.getPath() + fileInfo.getFilename(), consumer);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 检查缩略图位置，并从 MinIO 读取缩略图。
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
        downloadObject(fileInfo.getBasePath() + fileInfo.getPath() + fileInfo.getThFilename(), consumer);
    }

    /**
     * 在消费回调返回后关闭对象流和客户端，沿用历史文件记录的对象路径。
     * @param key 对象在当前存储桶内的完整路径
     * @param consumer 在流打开期间读取对象的回调
     */
    private void downloadObject(String key, Consumer<InputStream> consumer) {
        try (MinioClient client = getClient();
             InputStream in = client.getObject(GetObjectArgs.builder().bucket(bucketName).object(key).build())) {
            consumer.accept(in);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new FileStorageRuntimeException("文件存储操作失败", e);
        }
    }
}

