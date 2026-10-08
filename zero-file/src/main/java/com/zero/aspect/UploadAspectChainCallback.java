package com.zero.aspect;

import com.zero.platform.FileStorage;
import com.zero.recorder.FileRecorder;
import com.zero.vo.FileInfo;
import com.zero.vo.UploadPretreatment;

/**
 * 涓婁紶鍒囬潰璋冪敤閾剧粨鏉熷洖璋?
 */
public interface UploadAspectChainCallback {
    FileInfo run(FileInfo fileInfo,UploadPretreatment pre,FileStorage fileStorage,FileRecorder fileRecorder);
}

