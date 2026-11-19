package com.zero.aspect;

import lombok.Getter;
import lombok.Setter;

import java.util.Iterator;

import com.zero.platform.FileStorage;
import com.zero.recorder.FileRecorder;
import com.zero.vo.FileInfo;
import com.zero.vo.UploadPretreatment;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 负责单次文件上传。按顺序经过存储切面，最后调用实际存储操作。
 */
@Getter
@Setter
public class UploadAspectChain {

    private UploadAspectChainCallback callback;
    private Iterator<FileStorageAspect> aspectIterator;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 为本次上传创建独立的切面迭代器。
     * @param aspects 已按执行顺序提供的文件存储切面
     * @param callback 切面遍历结束后的实际操作
     */
    public UploadAspectChain(Iterable<FileStorageAspect> aspects,UploadAspectChainCallback callback) {
        this.aspectIterator = aspects.iterator();
        this.callback = callback;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 将本次上传交给下一个切面，切面遍历结束后调用实际操作。
     * @param fileInfo 待写入的文件信息
     * @param pre 上传预处理及文件内容
     * @param fileStorage 选中的存储平台
     * @param fileRecorder 文件记录器
     * @return
     * 返回值: FileInfo
     * 返回值说明: 保存及记录均成功时返回文件信息，否则返回 null
     */
    public FileInfo next(FileInfo fileInfo,UploadPretreatment pre,FileStorage fileStorage,FileRecorder fileRecorder) {
        if (aspectIterator.hasNext()) {
            return aspectIterator.next().uploadAround(this,fileInfo,pre,fileStorage,fileRecorder);
        } else {
            return callback.run(fileInfo,pre,fileStorage,fileRecorder);
        }
    }
}

