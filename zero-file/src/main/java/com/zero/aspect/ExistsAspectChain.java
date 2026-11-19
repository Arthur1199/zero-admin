package com.zero.aspect;

import lombok.Getter;
import lombok.Setter;

import java.util.Iterator;

import com.zero.platform.FileStorage;
import com.zero.vo.FileInfo;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 负责单次文件存在性检查。按顺序经过存储切面，最后调用实际存储操作。
 */
@Getter
@Setter
public class ExistsAspectChain {

    private ExistsAspectChainCallback callback;
    private Iterator<FileStorageAspect> aspectIterator;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 为本次存在性检查创建独立的切面迭代器。
     * @param aspects 已按执行顺序提供的文件存储切面
     * @param callback 切面遍历结束后的实际操作
     */
    public ExistsAspectChain(Iterable<FileStorageAspect> aspects,ExistsAspectChainCallback callback) {
        this.aspectIterator = aspects.iterator();
        this.callback = callback;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 将本次存在性检查交给下一个切面，切面遍历结束后调用实际操作。
     * @param fileInfo 要检查的文件信息
     * @param fileStorage 文件原有存储平台
     * @return
     * 返回值: boolean
     * 返回值说明: 平台确认对象存在时返回 true
     */
    public boolean next(FileInfo fileInfo,FileStorage fileStorage) {
        if (aspectIterator.hasNext()) {
            return aspectIterator.next().existsAround(this,fileInfo,fileStorage);
        } else {
            return callback.run(fileInfo,fileStorage);
        }
    }
}

