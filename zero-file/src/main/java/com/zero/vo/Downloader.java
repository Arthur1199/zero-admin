package com.zero.vo;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import com.zero.aspect.DownloadAspectChain;
import com.zero.aspect.DownloadThAspectChain;
import com.zero.aspect.FileStorageAspect;
import com.zero.exception.FileStorageRuntimeException;
import com.zero.listener.ProgressListener;
import com.zero.platform.FileStorage;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * 下载器。
 */
public class Downloader {
    /**
     * 下载目标：原文件。
     */
    public static final int TARGET_FILE = 1;
    /**
     * 下载目标：缩略图文件。
     */
    public static final int TARGET_TH_FILE = 2;

    private final FileStorage fileStorage;
    private final List<FileStorageAspect> aspectList;
    private final FileInfo fileInfo;
    private final Integer target;
    private ProgressListener progressListener;

    /**
     * 构造下载器。
     *
     * @param target 下载目标：TARGET_FILE 下载原文件，TARGET_TH_FILE 下载缩略图文件
     */
    public Downloader(FileInfo fileInfo,List<FileStorageAspect> aspectList,FileStorage fileStorage,Integer target) {
        this.fileStorage = fileStorage;
        this.aspectList = aspectList;
        this.fileInfo = fileInfo;
        this.target = target;
    }

    /**
     * 设置下载进度监听器。
     *
     * @param progressListener 只接收已传输字节数的进度回调
     */
    public Downloader setProgressMonitor(Consumer<Long> progressListener) {
        return setProgressMonitor((progressSize,allSize) -> progressListener.accept(progressSize));
    }

    /**
     * 设置下载进度监听器。
     *
     * @param progressListener 接收已传输字节数和总字节数的进度回调
     */
    public Downloader setProgressMonitor(BiConsumer<Long,Long> progressListener) {
        return setProgressMonitor(new ProgressListener() {
            @Override
            public void start() {
            }

            @Override
            public void progress(long progressSize,long allSize) {
                progressListener.accept(progressSize,allSize);
            }

            @Override
            public void finish() {
            }
        });
    }

    /**
     * 设置下载进度监听器。
     */
    public Downloader setProgressMonitor(ProgressListener progressListener) {
        this.progressListener = progressListener;
        return this;
    }

    /**
     * 以 InputStream 方式消费下载内容。
     */
    public void inputStream(Consumer<InputStream> consumer) {
        if (target == TARGET_FILE) {    //下载文件
            new DownloadAspectChain(aspectList,(_fileInfo,_fileStorage,_consumer) ->
                    _fileStorage.download(_fileInfo,_consumer)
            ).next(fileInfo,fileStorage,in ->
                    consumer.accept(progressListener == null ? in : new ProgressInputStream(in,progressListener,fileInfo.getSize()))
            );
        } else if (target == TARGET_TH_FILE) {  // 下载缩略图文件
            new DownloadThAspectChain(aspectList,(_fileInfo,_fileStorage,_consumer) ->
                    _fileStorage.downloadTh(_fileInfo,_consumer)
            ).next(fileInfo,fileStorage,in ->
                    consumer.accept(progressListener == null ? in : new ProgressInputStream(in,progressListener,fileInfo.getThSize()))
            );
        } else {
            throw new FileStorageRuntimeException("文件处理失败");
        }
    }

    /**
     * 下载为 byte 数组。
     */
    public byte[] bytes() {
        byte[][] bytes = new byte[1][];
        inputStream(in -> bytes[0] = IoUtil.readBytes(in));
        return bytes[0];
    }

    /**
     * 下载到指定文件。
     */
    public void file(File file) {
        inputStream(in -> FileUtil.writeFromStream(in,file));
    }

    /**
     * 下载到指定文件。
     */
    public void file(String filename) {
        inputStream(in -> FileUtil.writeFromStream(in,filename));
    }

    /**
     * 下载到指定输出流
     */
    public void outputStream(OutputStream out) {
        inputStream(in -> IoUtil.copy(in,out));
    }


}

