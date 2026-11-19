package com.zero.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 文件记录实体。
 */
@Getter
@Setter
@ToString
@TableName("biz_file_detail")
public class FileDetail implements Serializable
{
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("url")
    private String url;

    @TableField("`size`")
    private Long size;

    @TableField("filename")
    private String filename;

    @TableField("original_filename")
    private String originalFilename;

    @TableField("base_path")
    private String basePath;

    @TableField("`path`")
    private String path;

    @TableField("ext")
    private String ext;

    @TableField("platform")
    private String platform;

    @TableField("th_url")
    private String thUrl;

    @TableField("th_filename")
    private String thFilename;

    @TableField("th_size")
    private Long thSize;

    @TableField("object_id")
    private String objectId;

    @TableField("object_type")
    private String objectType;

    @TableField("create_time")
    private Date createTime;
}
