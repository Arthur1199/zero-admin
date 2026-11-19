package com.zero.platform;

import lombok.extern.slf4j.Slf4j;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.function.Consumer;

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
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: AWS S3 存储。
 */
@Slf4j
@Getter
@Setter
public class AwsS3FileStorage implements FileStorage {
    /* 存储平台 */
    private String platform;
    private String accessKey;
    private String secretKey;
    private String region;
    private String endpoint;
    private String bucketName;
    private String domain;
    private String basePath;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 按当前 S3 配置创建客户端，自定义终端地址存在时走终端配置。
     * @return
     * 返回值: AmazonS3
     * 返回值说明: 用于对象操作的 Amazon S3 客户端
     */
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
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 关闭之前使用的 AWS S3 SDK 客户端。
     * @param s3 待关闭的 AWS S3 客户端
     * @return
     * 返回值: void
     * 返回值说明: 客户端存在时释放其资源
     */
    public void shutdown(AmazonS3 s3) {
        if (s3 != null) s3.shutdown();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 通过 AWS S3 存储原文件及可选的缩略图，并更新文件信息中的存储位置。
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

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 按历史记录中的存储路径删除 AWS S3 的对象及关联缩略图。
     * @param fileInfo 携带原文件及缩略图路径信息的文件记录
     * @return
     * 返回值: boolean
     * 返回值说明: 当前平台的删除结果
     */
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


    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 检查 AWS S3 是否仍可找到文件记录指向的原文件。
     * @param fileInfo 包含原文件位置的文件信息
     * @return
     * 返回值: boolean
     * 返回值说明: 原文件存在时返回 true
     */
    @Override
    public boolean exists(FileInfo fileInfo) {
        AmazonS3 s3 = getAmazonS3();
        try {
            return s3.doesObjectExist(bucketName,fileInfo.getBasePath() + fileInfo.getPath() + fileInfo.getFilename());
        } finally {
            shutdown(s3);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 从 AWS S3 读取文件记录指向的原文件。
     * @param fileInfo 包含原文件位置的文件信息
     * @param consumer 用于读取输入流的回调
     * @return
     * 返回值: void
     * 返回值说明: 在输入流有效期间调用消费回调
     */
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

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 检查缩略图位置，并从 AWS S3 读取缩略图。
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

