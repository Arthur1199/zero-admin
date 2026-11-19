package com.zero.aspect;

import java.io.InputStream;
import java.util.function.Consumer;

import com.zero.platform.FileStorage;
import com.zero.recorder.FileRecorder;
import com.zero.vo.FileInfo;
import com.zero.vo.UploadPretreatment;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 文件服务切面接口，用于干预文件上传、删除、存在性检查和下载。
 */
public interface FileStorageAspect {


    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 拦截文件上传，默认交给链中下一环节。
     * @param chain 本次操作的切面链
     * @param fileInfo 待写入的文件信息
     * @param pre 上传预处理及文件内容
     * @param fileStorage 选中的存储平台
     * @param fileRecorder 文件记录器
     * @return
     * 返回值: FileInfo
     * 返回值说明: 保存及记录均成功时返回文件信息，否则返回 null
     */
    default FileInfo uploadAround(UploadAspectChain chain,FileInfo fileInfo,UploadPretreatment pre,FileStorage fileStorage,FileRecorder fileRecorder) {
        return chain.next(fileInfo,pre,fileStorage,fileRecorder);
    }


    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 拦截文件删除，默认交给链中下一环节。
     * @param chain 本次操作的切面链
     * @param fileInfo 待删除的文件信息
     * @param fileStorage 文件原有存储平台
     * @param fileRecorder 文件记录器
     * @return
     * 返回值: boolean
     * 返回值说明: 文件及记录删除均成功时返回 true
     */
    default boolean deleteAround(DeleteAspectChain chain,FileInfo fileInfo,FileStorage fileStorage,FileRecorder fileRecorder) {
        return chain.next(fileInfo,fileStorage,fileRecorder);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 拦截文件存在性检查，默认交给链中下一环节。
     * @param chain 本次操作的切面链
     * @param fileInfo 要检查的文件信息
     * @param fileStorage 文件原有存储平台
     * @return
     * 返回值: boolean
     * 返回值说明: 平台确认对象存在时返回 true
     */
    default boolean existsAround(ExistsAspectChain chain,FileInfo fileInfo,FileStorage fileStorage) {
        return chain.next(fileInfo,fileStorage);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 拦截文件原文件下载，默认交给链中下一环节。
     * @param chain 本次操作的切面链
     * @param fileInfo 待下载的文件信息
     * @param fileStorage 文件原有存储平台
     * @param consumer 消费下载流的回调
     * @return
     * 返回值: void
     * 返回值说明: 由消费回调处理下载流
     */
    default void downloadAround(DownloadAspectChain chain,FileInfo fileInfo,FileStorage fileStorage,Consumer<InputStream> consumer) {
        chain.next(fileInfo,fileStorage,consumer);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 拦截文件缩略图下载，默认交给链中下一环节。
     * @param chain 本次操作的切面链
     * @param fileInfo 携带缩略图位置的文件信息
     * @param fileStorage 文件原有存储平台
     * @param consumer 消费缩略图流的回调
     * @return
     * 返回值: void
     * 返回值说明: 由消费回调处理缩略图下载流
     */
    default void downloadThAround(DownloadThAspectChain chain,FileInfo fileInfo,FileStorage fileStorage,Consumer<InputStream> consumer) {
        chain.next(fileInfo,fileStorage,consumer);
    }
}

