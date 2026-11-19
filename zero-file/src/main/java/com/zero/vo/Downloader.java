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
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 下载器。
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
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 根据原文件或缩略图目标准备下载器，下载动作在调用输出方法后开始。
     * @param fileInfo 包含原图及缩略图位置的文件信息
     * @param aspectList 文件存储切面
     * @param fileStorage 文件所属平台
     * @param target 下载类型，原文件或缩略图
     */
    public Downloader(FileInfo fileInfo,List<FileStorageAspect> aspectList,FileStorage fileStorage,Integer target) {
        this.fileStorage = fileStorage;
        this.aspectList = aspectList;
        this.fileInfo = fileInfo;
        this.target = target;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 设置下载进度监听器。
     * @param progressListener 只接收已传输字节数的进度回调
     * @return
     * 返回值: Downloader
     * 返回值说明: 当前下载器，可继续配置或执行下载
     */
    public Downloader setProgressMonitor(Consumer<Long> progressListener) {
        return setProgressMonitor((progressSize,allSize) -> progressListener.accept(progressSize));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 设置下载进度监听器。
     * @param progressListener 接收已传输字节数和总字节数的进度回调
     * @return
     * 返回值: Downloader
     * 返回值说明: 当前下载器，可继续配置或执行下载
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
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 设置下载进度监听器。
     * @param progressListener 用于接收下载进度事件的监听器
     * @return
     * 返回值: Downloader
     * 返回值说明: 当前下载器，可继续配置或执行下载
     */
    public Downloader setProgressMonitor(ProgressListener progressListener) {
        this.progressListener = progressListener;
        return this;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 按目标类型选择下载切面，配置进度监听时用对应对象的大小包装下载流。
     * @param consumer 消费输入流的回调
     * @return
     * 返回值: void
     * 返回值说明: 由消费回调在下载流可用期间读取内容
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
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 读取全部下载内容到内存，适合可完整加载的文件。
     * @return
     * 返回值: byte[]
     * 返回值说明: 完整文件内容，消费回调未执行时为 null
     */
    public byte[] bytes() {
        byte[][] bytes = new byte[1][];
        inputStream(in -> bytes[0] = IoUtil.readBytes(in));
        return bytes[0];
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 将下载内容写入指定的本地文件。
     * @param file 接收下载内容的本地文件
     * @return
     * 返回值: void
     * 返回值说明: 在目标路径保存文件内容
     */
    public void file(File file) {
        inputStream(in -> FileUtil.writeFromStream(in,file));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 将下载内容写入指定路径。
     * @param filename 接收下载内容的文件路径
     * @return
     * 返回值: void
     * 返回值说明: 在目标路径保存文件内容
     */
    public void file(String filename) {
        inputStream(in -> FileUtil.writeFromStream(in,filename));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 将下载内容流式复制到调用方提供的输出流。
     * @param out 接收内容的输出流
     * @return
     * 返回值: void
     * 返回值说明: 写入目标输出流，输出流由调用方管理
     */
    public void outputStream(OutputStream out) {
        inputStream(in -> IoUtil.copy(in,out));
    }


}

