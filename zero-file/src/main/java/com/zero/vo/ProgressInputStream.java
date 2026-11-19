package com.zero.vo;

import java.io.*;

import com.zero.listener.ProgressListener;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 带进度通知的 InputStream 包装类。
 */
public class ProgressInputStream extends FilterInputStream {

    private boolean readFlag;
    private long progressSize;
    private final long allSize;
    private final ProgressListener listener;

    public ProgressInputStream(InputStream in,ProgressListener listener,long allSize) {
        super(in);
        this.listener = listener;
        this.allSize = allSize;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 跳过可跳过的输入字节，并按实际跳过数量更新进度。
     * @param n 希望跳过的字节数
     * @return
     * 返回值: long
     * 返回值说明: 实际跳过的字节数
     */
    @Override
    public long skip(long n) throws IOException {
        long skip = super.skip(n);
        progress(skip);
        return skip;
    }


    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 读取单个字节并通知进度监听器。
     * @return
     * 返回值: int
     * 返回值说明: 读到的无符号字节值，到达结尾时返回 -1
     */
    @Override
    public int read() throws IOException {
        int b = super.read();
        progress(b == -1 ? -1 : 1);
        return b;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 读取到指定数组区域，首次批量读取时触发开始通知。
     * @param b 接收数据的数组
     * @param off 写入起始位置
     * @param len 允许读取的最大字节数
     * @return
     * 返回值: int
     * 返回值说明: 实际读取的字节数，到达结尾时返回 -1
     */
    @Override
    public int read(byte[] b,int off,int len) throws IOException {
        // 批量读取开始前只发一次开始事件，后续读取继续累加已处理的字节数。
        if (!this.readFlag) {
            this.readFlag = true;
            this.listener.start();
        }
        int bytes = super.read(b,off,len);
        progress(bytes);
        return bytes;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 说明下载进度包装流的标记能力。
     * @return
     * 返回值: boolean
     * 返回值说明: 固定返回 false
     */
    @Override
    public boolean markSupported() {
        return false;
    }

    protected void progress(long size) {
        if (size > 0) {
            this.listener.progress(progressSize += size,allSize);
        } else if (size < 0) {
            this.listener.finish();
        }
    }


}

