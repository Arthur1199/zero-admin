package com.zero.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 绑定 spring.file-storage 下的默认平台、缩略图后缀和各存储平台配置。
 */
@Data
//@Component
@Configuration
//@ConditionalOnMissingBean(FileStorageProperties.class)
@ConfigurationProperties(prefix = "spring.file-storage")
public class FileStorageProperties {

    /**
     * 默认存储平台。
     */
    private String defaultPlatform = "local";
    /**
     * 缩略图后缀，例如 .min.jpg、.png。
     */
    private String thumbnailSuffix = ".min.jpg";
    /**
     * 本地存储配置。
     */
    private List<Local> local = new ArrayList<>();
    /**
     * 华为云 OBS 配置。
     */
    private List<HuaweiObs> huaweiObs = new ArrayList<>();
    /**
     * 阿里云 OSS 配置。
     */
    private List<AliyunOss> aliyunOss = new ArrayList<>();
    /**
     * 七牛云 Kodo 配置。
     */
    private List<QiniuKodo> qiniuKodo = new ArrayList<>();
    /**
     * 腾讯云 COS 配置。
     */
    private List<TencentCos> tencentCos = new ArrayList<>();
    /**
     * 百度云 BOS 配置。
     */
    private List<BaiduBos> baiduBos = new ArrayList<>();
    /**
     * 又拍云 USS 配置。
     */
    private List<UpyunUSS> upyunUSS = new ArrayList<>();
    /**
     * MinIO USS
     */
    private List<MinIO> minio = new ArrayList<>();

    /**
     * AWS S3
     */
    private List<AwsS3> awsS3 = new ArrayList<>();

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 本地存储。
     */
    @Data
    public static class Local {
        /**
         * 本地存储路径
         */
        private String basePath = "";
        /**
         * 本地存储访问路径
         */
        private String[] pathPatterns = new String[0];
        /**
         * 启用本地存储
         */
        private Boolean enableStorage = false;
        /**
         * 启用本地访问
         */
        private Boolean enableAccess = false;
        /**
         * 存储平台
         */
        private String platform = "local";
        /**
         * 访问域名
         */
        private String domain = "";
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 华为云 OBS。
     */
    @Data
    public static class HuaweiObs {
        private String accessKey;
        private String secretKey;
        private String endPoint;
        private String bucketName;
        /**
         * 访问域名
         */
        private String domain = "";
        /**
         * 启用存储
         */
        private Boolean enableStorage = false;
        /**
         * 存储平台
         */
        private String platform = "";
        /**
         * 基础路径
         */
        private String basePath = "";
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 阿里云 OSS。
     */
    @Data
    public static class AliyunOss {
        private String accessKey;
        private String secretKey;
        private String endPoint;
        private String bucketName;
        /**
         * 访问域名
         */
        private String domain = "";
        /**
         * 启用存储
         */
        private Boolean enableStorage = false;
        /**
         * 存储平台
         */
        private String platform = "";
        /**
         * 基础路径
         */
        private String basePath = "";
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 七牛云 Kodo。
     */
    @Data
    public static class QiniuKodo {
        private String accessKey;
        private String secretKey;
        private String bucketName;
        /**
         * 访问域名
         */
        private String domain = "";
        /**
         * 启用存储
         */
        private Boolean enableStorage = false;
        /**
         * 存储平台
         */
        private String platform = "";
        /**
         * 基础路径
         */
        private String basePath = "";
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 腾讯云 COS。
     */
    @Data
    public static class TencentCos {
        private String secretId;
        private String secretKey;
        private String region;
        private String bucketName;
        /**
         * 访问域名
         */
        private String domain = "";
        /**
         * 启用存储
         */
        private Boolean enableStorage = false;
        /**
         * 存储平台
         */
        private String platform = "";
        /**
         * 基础路径
         */
        private String basePath = "";
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 百度云 BOS。
     */
    @Data
    public static class BaiduBos {
        private String accessKey;
        private String secretKey;
        private String endPoint;
        private String bucketName;
        /**
         * 访问域名
         */
        private String domain = "";
        /**
         * 启用存储
         */
        private Boolean enableStorage = false;
        /**
         * 存储平台
         */
        private String platform = "";
        /**
         * 基础路径
         */
        private String basePath = "";
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 又拍云 USS。
     */
    @Data
    public static class UpyunUSS {
        private String username;
        private String password;
        private String bucketName;
        /**
         * 访问域名
         */
        private String domain = "";
        /**
         * 启用存储
         */
        private Boolean enableStorage = false;
        /**
         * 存储平台
         */
        private String platform = "";
        /**
         * 基础路径
         */
        private String basePath = "";
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: MinIO
     */
    @Data
    public static class MinIO {
        private String accessKey;
        private String secretKey;
        private String endPoint;
        private String bucketName;
        /**
         * 访问域名
         */
        private String domain = "";
        /**
         * 启用存储
         */
        private Boolean enableStorage = false;
        /**
         * 存储平台
         */
        private String platform = "";
        /**
         * 基础路径
         */
        private String basePath = "";
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: AWS S3
     */
    @Data
    public static class AwsS3 {
        private String accessKey;
        private String secretKey;
        private String region;
        private String endpoint;
        private String bucketName;
        /**
         * 访问域名
         */
        private String domain = "";
        /**
         * 启用存储
         */
        private Boolean enableStorage = false;
        /**
         * 存储平台
         */
        private String platform = "";
        /**
         * 基础路径
         */
        private String basePath = "";
    }


}

