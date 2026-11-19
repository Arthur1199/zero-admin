package com.zero.platform;

import lombok.extern.slf4j.Slf4j;
import cn.hutool.core.util.StrUtil;
import com.zero.exception.FileStorageRuntimeException;
import com.zero.vo.FileInfo;
import com.zero.vo.UploadPretreatment;
import io.minio.ErrorCode;
import io.minio.MinioClient;
import io.minio.ObjectStat;
import io.minio.PutObjectOptions;
import io.minio.errors.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
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
            return new MinioClient(endPoint,accessKey,secretKey);
        } catch (InvalidEndpointException | InvalidPortException e) {
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

        MinioClient client = getClient();
        try {
            Optional<MediaType> mediaType = MediaTypeFactory.getMediaType(fileInfo.getFilename());
            MediaType contentType = mediaType.orElse(MediaType.APPLICATION_OCTET_STREAM);
//			if(fileInfo.getFilename().endsWith("gif") || fileInfo.getFilename().endsWith("GIF") ) {
//				contentType = MediaType.APPLICATION_OCTET_STREAM;
//			}
            log.info("上传文件类型，filename={}, contentType={}", fileInfo.getFilename(), contentType.toString());

            PutObjectOptions objOpt =  new PutObjectOptions(pre.getFileWrapper().getSize(),-1);
            objOpt.setContentType(contentType.toString());
            client.putObject(bucketName,newFileKey,pre.getFileWrapper().getInputStream(),objOpt);

            byte[] thumbnailBytes = pre.getThumbnailBytes();
            if (thumbnailBytes != null) { // 上传缩略图
                String newThFileKey = basePath + fileInfo.getPath() + fileInfo.getThFilename();
                fileInfo.setThUrl(domain + newThFileKey);
                PutObjectOptions objOptions = new PutObjectOptions(thumbnailBytes.length,-1);
                objOptions.setContentType(contentType.toString());
               // client.putObject(bucketName,newThFileKey,new ByteArrayInputStream(thumbnailBytes),new PutObjectOptions(thumbnailBytes.length,-1));
                client.putObject(bucketName,newThFileKey,new ByteArrayInputStream(thumbnailBytes),objOptions);

            }

            return true;
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidBucketNameException | InvalidKeyException | InvalidResponseException | IOException | NoSuchAlgorithmException | XmlParserException e) {
            try {
                client.removeObject(bucketName,newFileKey);
            } catch (Exception ignored) {
            }
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
        MinioClient client = getClient();
        try {
            if (fileInfo.getThFilename() != null) {   // 删除缩略图
                client.removeObject(bucketName,fileInfo.getBasePath() + fileInfo.getPath() + fileInfo.getThFilename());
            }
            client.removeObject(bucketName,fileInfo.getBasePath() + fileInfo.getPath() + fileInfo.getFilename());
            return true;
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidBucketNameException | InvalidKeyException | InvalidResponseException | IOException | NoSuchAlgorithmException | XmlParserException e) {
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
        MinioClient client = getClient();
        try {
            ObjectStat stat = client.statObject(bucketName,fileInfo.getBasePath() + fileInfo.getPath() + fileInfo.getFilename());
            return stat != null && stat.createdTime() != null;
        } catch (ErrorResponseException e) {
            String code = e.errorResponse().errorCode().code();
            if (ErrorCode.RESOURCE_NOT_FOUND.code().equals(code) || ErrorCode.NO_SUCH_OBJECT.code().equals(code)) {
                return false;
            }
            throw new FileStorageRuntimeException("文件存储操作失败", e);
        } catch (InsufficientDataException | InternalException | InvalidBucketNameException | InvalidKeyException | InvalidResponseException | IOException | NoSuchAlgorithmException | XmlParserException e) {
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
        MinioClient client = getClient();
        try (InputStream in = client.getObject(bucketName,fileInfo.getBasePath() + fileInfo.getPath() + fileInfo.getFilename())) {
            consumer.accept(in);
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidBucketNameException | InvalidKeyException | InvalidResponseException | IOException | NoSuchAlgorithmException | XmlParserException e) {
            throw new FileStorageRuntimeException("文件存储操作失败", e);
        }
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
        MinioClient client = getClient();
        try (InputStream in = client.getObject(bucketName,fileInfo.getBasePath() + fileInfo.getPath() + fileInfo.getThFilename())) {
            consumer.accept(in);
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidBucketNameException | InvalidKeyException | InvalidResponseException | IOException | NoSuchAlgorithmException | XmlParserException e) {
            throw new FileStorageRuntimeException("文件存储操作失败", e);
        }

    }
}

