package com.zero.aspect;

import com.zero.platform.FileStorage;
import com.zero.vo.FileInfo;

/**
 * 文件存在性检查切面调用链结束回调。
 */
public interface ExistsAspectChainCallback {
    boolean run(FileInfo fileInfo,FileStorage fileStorage);
}

