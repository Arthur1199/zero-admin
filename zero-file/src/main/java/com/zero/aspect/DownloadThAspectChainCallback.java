package com.zero.aspect;

import java.io.InputStream;
import java.util.function.Consumer;

import com.zero.platform.FileStorage;
import com.zero.vo.FileInfo;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 文件缩略图下载切面链结束后执行的实际操作。
 */
public interface DownloadThAspectChainCallback {
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 执行切面链末尾的缩略图下载。
     * @param fileInfo 携带缩略图位置的文件信息
     * @param fileStorage 文件原有存储平台
     * @param consumer 消费缩略图流的回调
     * @return
     * 返回值: void
     * 返回值说明: 由消费回调处理缩略图下载流
     */
    void run(FileInfo fileInfo,FileStorage fileStorage,Consumer<InputStream> consumer);
}

