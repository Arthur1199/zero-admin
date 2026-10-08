package com.zero.aspect;

import com.zero.platform.FileStorage;
import com.zero.recorder.FileRecorder;
import com.zero.vo.FileInfo;

/**
 * 鍒犻櫎鍒囬潰璋冪敤閾剧粨鏉熷洖璋?
 */
public interface DeleteAspectChainCallback {
    boolean run(FileInfo fileInfo,FileStorage fileStorage,FileRecorder fileRecorder);
}

