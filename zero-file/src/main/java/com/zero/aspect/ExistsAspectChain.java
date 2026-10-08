package com.zero.aspect;

import lombok.Getter;
import lombok.Setter;

import java.util.Iterator;

import com.zero.platform.FileStorage;
import com.zero.vo.FileInfo;

/**
 * 文件是否存在的切面调用链
 */
@Getter
@Setter
public class ExistsAspectChain {

    private ExistsAspectChainCallback callback;
    private Iterator<FileStorageAspect> aspectIterator;

    public ExistsAspectChain(Iterable<FileStorageAspect> aspects,ExistsAspectChainCallback callback) {
        this.aspectIterator = aspects.iterator();
        this.callback = callback;
    }

    /**
     * 璋冪敤涓嬩竴涓垏闈?
     */
    public boolean next(FileInfo fileInfo,FileStorage fileStorage) {
        if (aspectIterator.hasNext()) {//杩樻湁涓嬩竴涓?
            return aspectIterator.next().existsAround(this,fileInfo,fileStorage);
        } else {
            return callback.run(fileInfo,fileStorage);
        }
    }
}

