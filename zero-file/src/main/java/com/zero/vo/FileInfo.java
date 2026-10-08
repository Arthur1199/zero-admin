package com.zero.vo;


import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class FileInfo implements Serializable {

    /**
     * 文件 ID。
     */
    private Integer id;

    /**
     * 文件访问地址。
     */
    private String url;

    /**
     * 文件大小，单位字节。
     */
    private Long size;

    /**
     * 文件名。
     */
    private String filename;

    /**
     * 原始文件名。
     */
    private String originalFilename;

    /**
     * 基础存储路径。
     */
    private String basePath;

    /**
     * 存储路径。
     */
    private String path;

    /**
     * 文件扩展名。
     */
    private String ext;

    /**
     * 存储平台。
     */
    private String platform;

    /**
     * 缩略图访问地址。
     */
    private String thUrl;

    /**
     * 缩略图文件名。
     */
    private String thFilename;

    /**
     * 缩略图大小，单位字节。
     */
    private Long thSize;

    /**
     * 文件所属对象 ID。
     */
    private String objectId;

    /**
     * 文件所属对象类型，例如用户头像、评价图片。
     */
    private String objectType;

    /**
     * 创建时间。
     */
    private Date createTime;

    private static final long serialVersionUID = 1L;
}

