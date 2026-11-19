package com.zero.aspect;

import lombok.Getter;
import lombok.Setter;

import java.util.Iterator;

import com.zero.platform.FileStorage;
import com.zero.recorder.FileRecorder;
import com.zero.vo.FileInfo;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 负责单次文件删除。按顺序经过存储切面，最后调用实际存储操作。
 */
@Getter
@Setter
public class DeleteAspectChain {

    private DeleteAspectChainCallback callback;
    private Iterator<FileStorageAspect> aspectIterator;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 为本次删除创建独立的切面迭代器。
     * @param aspects 已按执行顺序提供的文件存储切面
     * @param callback 切面遍历结束后的实际操作
     */
    public DeleteAspectChain(Iterable<FileStorageAspect> aspects,DeleteAspectChainCallback callback) {
        this.aspectIterator = aspects.iterator();
        this.callback = callback;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 将本次删除交给下一个切面，切面遍历结束后调用实际操作。
     * @param fileInfo 待删除的文件信息
     * @param fileStorage 文件原有存储平台
     * @param fileRecorder 文件记录器
     * @return
     * 返回值: boolean
     * 返回值说明: 文件及记录删除均成功时返回 true
     */
    public boolean next(FileInfo fileInfo,FileStorage fileStorage,FileRecorder fileRecorder) {
        if (aspectIterator.hasNext()) {
            return aspectIterator.next().deleteAround(this,fileInfo,fileStorage,fileRecorder);
        } else {
            return callback.run(fileInfo,fileStorage,fileRecorder);
        }
    }
}

