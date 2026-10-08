package com.zero.aspect;

import java.io.InputStream;
import java.util.function.Consumer;

import com.zero.platform.FileStorage;
import com.zero.vo.FileInfo;

/**
 * 下载缩略图切面调用链结束回调
 */
public interface DownloadThAspectChainCallback {
    void run(FileInfo fileInfo,FileStorage fileStorage,Consumer<InputStream> consumer);
}

