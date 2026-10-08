package com.zero.aspect;

import lombok.Getter;
import lombok.Setter;

import java.util.Iterator;

import com.zero.platform.FileStorage;
import com.zero.recorder.FileRecorder;
import com.zero.vo.FileInfo;

/**
 * 鍒犻櫎鐨勫垏闈㈣皟鐢ㄩ摼
 */
@Getter
@Setter
public class DeleteAspectChain {

    private DeleteAspectChainCallback callback;
    private Iterator<FileStorageAspect> aspectIterator;

    public DeleteAspectChain(Iterable<FileStorageAspect> aspects,DeleteAspectChainCallback callback) {
        this.aspectIterator = aspects.iterator();
        this.callback = callback;
    }

    /**
     * 璋冪敤涓嬩竴涓垏闈?
     */
    public boolean next(FileInfo fileInfo,FileStorage fileStorage,FileRecorder fileRecorder) {
        if (aspectIterator.hasNext()) {//杩樻湁涓嬩竴涓?
            return aspectIterator.next().deleteAround(this,fileInfo,fileStorage,fileRecorder);
        } else {
            return callback.run(fileInfo,fileStorage,fileRecorder);
        }
    }
}

