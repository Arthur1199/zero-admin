package com.zero.aspect;

import lombok.Getter;
import lombok.Setter;

import java.io.InputStream;
import java.util.Iterator;
import java.util.function.Consumer;

import com.zero.platform.FileStorage;
import com.zero.vo.FileInfo;

/**
 * 涓嬭浇鐨勫垏闈㈣皟鐢ㄩ摼
 */
@Getter
@Setter
public class DownloadAspectChain {

    private DownloadAspectChainCallback callback;
    private Iterator<FileStorageAspect> aspectIterator;

    public DownloadAspectChain(Iterable<FileStorageAspect> aspects,DownloadAspectChainCallback callback) {
        this.aspectIterator = aspects.iterator();
        this.callback = callback;
    }

    /**
     * 璋冪敤涓嬩竴涓垏闈?
     */
    public void next(FileInfo fileInfo,FileStorage fileStorage,Consumer<InputStream> consumer) {
        if (aspectIterator.hasNext()) {//杩樻湁涓嬩竴涓?
            aspectIterator.next().downloadAround(this,fileInfo,fileStorage,consumer);
        } else {
            callback.run(fileInfo,fileStorage,consumer);
        }
    }
}

