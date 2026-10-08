package com.zero.aspect;

import java.io.InputStream;
import java.util.function.Consumer;

import com.zero.platform.FileStorage;
import com.zero.vo.FileInfo;

/**
 * 涓嬭浇鍒囬潰璋冪敤閾剧粨鏉熷洖璋?
 */
public interface DownloadAspectChainCallback {
    void run(FileInfo fileInfo,FileStorage fileStorage,Consumer<InputStream> consumer);
}

