package com.zero.aspect;

import java.io.InputStream;
import java.util.function.Consumer;

import com.zero.platform.FileStorage;
import com.zero.recorder.FileRecorder;
import com.zero.vo.FileInfo;
import com.zero.vo.UploadPretreatment;

/**
 * 文件服务切面接口，用于干预文件上传、删除、存在性检查和下载。
 */
public interface FileStorageAspect {


    /**
     * 上传文件，成功返回文件信息，失败返回 null。
     */
    default FileInfo uploadAround(UploadAspectChain chain,FileInfo fileInfo,UploadPretreatment pre,FileStorage fileStorage,FileRecorder fileRecorder) {
        return chain.next(fileInfo,pre,fileStorage,fileRecorder);
    }


    /**
     * 删除文件，成功返回 true。
     */
    default boolean deleteAround(DeleteAspectChain chain,FileInfo fileInfo,FileStorage fileStorage,FileRecorder fileRecorder) {
        return chain.next(fileInfo,fileStorage,fileRecorder);
    }

    /**
     * 判断文件是否存在。
     */
    default boolean existsAround(ExistsAspectChain chain,FileInfo fileInfo,FileStorage fileStorage) {
        return chain.next(fileInfo,fileStorage);
    }

    /**
     * 下载文件。
     */
    default void downloadAround(DownloadAspectChain chain,FileInfo fileInfo,FileStorage fileStorage,Consumer<InputStream> consumer) {
        chain.next(fileInfo,fileStorage,consumer);
    }

    /**
     * 下载缩略图文件。
     */
    default void downloadThAround(DownloadThAspectChain chain,FileInfo fileInfo,FileStorage fileStorage,Consumer<InputStream> consumer) {
        chain.next(fileInfo,fileStorage,consumer);
    }
}

