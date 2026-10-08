package com.zero.platform;

import java.io.InputStream;
import java.util.function.Consumer;

import com.zero.vo.FileInfo;
import com.zero.vo.UploadPretreatment;

/**
 * 文件存储操作接口，由各存储平台实现。
 */
public interface FileStorage {

    /**
     * 获取存储平台。
     */
    String getPlatform();

    /**
     * 设置存储平台。
     */
    void setPlatform(String platform);

    /**
     * 保存文件。
     */
    boolean save(FileInfo fileInfo,UploadPretreatment pre);


    /**
     * 删除文件。
     */
    boolean delete(FileInfo fileInfo);

    /**
     * 判断文件是否存在。
     */
    boolean exists(FileInfo fileInfo);

    /**
     * 下载文件。
     */
    void download(FileInfo fileInfo,Consumer<InputStream> consumer);

    /**
     * 下载缩略图文件。
     */
    void downloadTh(FileInfo fileInfo,Consumer<InputStream> consumer);
}

