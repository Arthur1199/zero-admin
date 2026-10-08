package com.zero.platform;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * MinIO 存储。
 */
@Getter
@Setter
public class MinIOFileStorage implements FileStorage {
    private static final Logger log = LoggerFactory.getLogger(MinIOFileStorage.class);

    /* 存储平台 */
    private String platform;
    private String accessKey;
    private String secretKey;
    private String endPoint;
    private String bucketName;
    private String domain;
    private String basePath;

    public MinioClient getClient() {
        try {
            return new MinioClient(endPoint,accessKey,secretKey);
        } catch (InvalidEndpointException | InvalidPortException e) {
            throw new FileStorageRuntimeException("文件存储操作失败", e);
        }
    }

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

    @Override
    public void download(FileInfo fileInfo,Consumer<InputStream> consumer) {
        MinioClient client = getClient();
        try (InputStream in = client.getObject(bucketName,fileInfo.getBasePath() + fileInfo.getPath() + fileInfo.getFilename())) {
            consumer.accept(in);
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidBucketNameException | InvalidKeyException | InvalidResponseException | IOException | NoSuchAlgorithmException | XmlParserException e) {
            throw new FileStorageRuntimeException("文件存储操作失败", e);
        }
    }

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

