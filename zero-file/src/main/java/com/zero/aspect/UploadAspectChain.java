package com.zero.aspect;

import lombok.Getter;
import lombok.Setter;

import java.util.Iterator;

import com.zero.platform.FileStorage;
import com.zero.recorder.FileRecorder;
import com.zero.vo.FileInfo;
import com.zero.vo.UploadPretreatment;

/**
 * 涓婁紶鐨勫垏闈㈣皟鐢ㄩ摼
 */
@Getter
@Setter
public class UploadAspectChain {

    private UploadAspectChainCallback callback;
    private Iterator<FileStorageAspect> aspectIterator;

    public UploadAspectChain(Iterable<FileStorageAspect> aspects,UploadAspectChainCallback callback) {
        this.aspectIterator = aspects.iterator();
        this.callback = callback;
    }

    /**
     * 璋冪敤涓嬩竴涓垏闈?
     */
    public FileInfo next(FileInfo fileInfo,UploadPretreatment pre,FileStorage fileStorage,FileRecorder fileRecorder) {
        if (aspectIterator.hasNext()) {//杩樻湁涓嬩竴涓?
            return aspectIterator.next().uploadAround(this,fileInfo,pre,fileStorage,fileRecorder);
        } else {
            return callback.run(fileInfo,pre,fileStorage,fileRecorder);
        }
    }
}

