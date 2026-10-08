package com.zero.platform;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;

import cn.hutool.core.util.StrUtil;
import com.zero.exception.FileStorageRuntimeException;
import com.zero.vo.FileInfo;
import com.zero.vo.UploadPretreatment;
import lombok.Getter;
import lombok.Setter;

/**
 * AWS S3 存储。
 */
@Getter
@Setter
public class AwsS3FileStorage implements FileStorage {
    private static final Logger log = LoggerFactory.getLogger(AwsS3FileStorage.class);

    /* 存储平台 */
    private String platform;
    private String accessKey;
    private String secretKey;
    private String region;
    private String endpoint;
    private String bucketName;
    private String domain;
    private String basePath;

    public AmazonS3 getAmazonS3() {
        AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey,secretKey)));
        if (StrUtil.isNotBlank(endpoint)) {
            builder.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint,region));
        } else if (StrUtil.isNotBlank(region)) {
            builder.withRegion(region);
        }
        return builder.build();
    }

    /**
     * 关闭客户端。
     */
    public void shutdown(AmazonS3 s3) {
        if (s3 != null) s3.shutdown();
    }

    @Override
    public boolean save(FileInfo fileInfo,UploadPretreatment pre) {
        String newFileKey = basePath + fileInfo.getPath() + fileInfo.getFilename();
        fileInfo.setBasePath(basePath);
        fileInfo.setUrl(domain + newFileKey);

        AmazonS3 s3 = getAmazonS3();
        try {
        	ObjectMetadata objectMetadata = new ObjectMetadata();
			Optional<MediaType> mediaType = MediaTypeFactory.getMediaType(fileInfo.getFilename());
			MediaType contentType = mediaType.orElse(MediaType.APPLICATION_OCTET_STREAM);
//			if(fileInfo.getFilename().endsWith("gif") || fileInfo.getFilename().endsWith("GIF") ) {
//				contentType = MediaType.APPLICATION_OCTET_STREAM;
//			}
			log.info("上传文件类型，filename={}, contentType={}", fileInfo.getFilename(), contentType.toString());
			objectMetadata.setContentLength(Long.valueOf(pre.getFileWrapper().getInputStream().available()));
			objectMetadata.setContentType(contentType.toString());
			objectMetadata.setContentLength(Long.valueOf(pre.getFileWrapper().getInputStream().available()));
			s3.putObject(bucketName, newFileKey, pre.getFileWrapper().getInputStream(), objectMetadata);
            byte[] thumbnailBytes = pre.getThumbnailBytes();
            if (thumbnailBytes != null) { // 上传缩略图
                String newThFileKey = basePath + fileInfo.getPath() + fileInfo.getThFilename();
                fileInfo.setThUrl(domain + newThFileKey);
                s3.putObject(bucketName,newThFileKey,new ByteArrayInputStream(thumbnailBytes),null);
            }

            return true;
        } catch (IOException e) {
            s3.deleteObject(bucketName,newFileKey);
            throw new FileStorageRuntimeException("文件存储操作失败", e);
        } finally {
            shutdown(s3);
        }
    }

    @Override
    public boolean delete(FileInfo fileInfo) {
        AmazonS3 oss = getAmazonS3();
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
        AmazonS3 s3 = getAmazonS3();
        try {
            return s3.doesObjectExist(bucketName,fileInfo.getBasePath() + fileInfo.getPath() + fileInfo.getFilename());
        } finally {
            shutdown(s3);
        }
    }

    @Override
    public void download(FileInfo fileInfo,Consumer<InputStream> consumer) {
        AmazonS3 s3 = getAmazonS3();
        try {
            S3Object object = s3.getObject(bucketName,fileInfo.getBasePath() + fileInfo.getPath() + fileInfo.getFilename());
            try (InputStream in = object.getObjectContent()) {
                consumer.accept(in);
            } catch (IOException e) {
                throw new FileStorageRuntimeException("文件存储操作失败", e);
            }
        } finally {
            shutdown(s3);
        }
    }

    @Override
    public void downloadTh(FileInfo fileInfo,Consumer<InputStream> consumer) {
        if (StrUtil.isBlank(fileInfo.getThFilename())) {
            throw new FileStorageRuntimeException("文件不存在或无法操作");
        }
        AmazonS3 s3 = getAmazonS3();
        try {
            S3Object object = s3.getObject(bucketName,fileInfo.getBasePath() + fileInfo.getPath() + fileInfo.getThFilename());
            try (InputStream in = object.getObjectContent()) {
                consumer.accept(in);
            } catch (IOException e) {
                throw new FileStorageRuntimeException("文件存储操作失败", e);
            }
        } finally {
            shutdown(s3);
        }
    }
}

