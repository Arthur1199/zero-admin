package com.zero.listener;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 进度监听器。
 */
public interface ProgressListener {

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 在下载进度跟踪开始时通知监听器。
     * @return
     * 返回值: void
     * 返回值说明: 触发下载开始回调
     */
    void start();

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 在读取下载流时报告当前已处理的字节数。
     * @param progressSize 当前已处理的字节数
     * @param allSize 下载内容的总字节数
     * @return
     * 返回值: void
     * 返回值说明: 向调用方报告当前进度及总长度
     */
    void progress(long progressSize,long allSize);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 在结束下载进度跟踪时通知监听器。
     * @return
     * 返回值: void
     * 返回值说明: 触发下载结束回调
     */
    void finish();
}

