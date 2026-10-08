package com.zero.listener;

/**
 * 进度监听器。
 */
public interface ProgressListener {

    /**
     * 开始。
     */
    void start();

    /**
     * 进度变化。
     *
     * @param progressSize 已处理字节数
     * @param allSize      总字节数，来自 fileInfo.getSize()
     */
    void progress(long progressSize,long allSize);

    /**
     * 结束。
     */
    void finish();
}

