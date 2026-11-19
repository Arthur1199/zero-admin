package com.zero.aspect;

import lombok.Getter;
import lombok.Setter;

import java.io.InputStream;
import java.util.Iterator;
import java.util.function.Consumer;

import com.zero.platform.FileStorage;
import com.zero.vo.FileInfo;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 负责单次原文件下载。按顺序经过存储切面，最后调用实际存储操作。
 */
@Getter
@Setter
public class DownloadAspectChain {

    private DownloadAspectChainCallback callback;
    private Iterator<FileStorageAspect> aspectIterator;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 为本次原文件下载创建独立的切面迭代器。
     * @param aspects 已按执行顺序提供的文件存储切面
     * @param callback 切面遍历结束后的实际操作
     */
    public DownloadAspectChain(Iterable<FileStorageAspect> aspects,DownloadAspectChainCallback callback) {
        this.aspectIterator = aspects.iterator();
        this.callback = callback;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 将本次原文件下载交给下一个切面，切面遍历结束后调用实际操作。
     * @param fileInfo 待下载的文件信息
     * @param fileStorage 文件原有存储平台
     * @param consumer 消费下载流的回调
     * @return
     * 返回值: void
     * 返回值说明: 由消费回调处理下载流
     */
    public void next(FileInfo fileInfo,FileStorage fileStorage,Consumer<InputStream> consumer) {
        if (aspectIterator.hasNext()) {
            aspectIterator.next().downloadAround(this,fileInfo,fileStorage,consumer);
        } else {
            callback.run(fileInfo,fileStorage,consumer);
        }
    }
}

