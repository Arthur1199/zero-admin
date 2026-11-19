package com.zero.aspect;

import com.zero.platform.FileStorage;
import com.zero.recorder.FileRecorder;
import com.zero.vo.FileInfo;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 文件删除切面链结束后执行的实际操作。
 */
public interface DeleteAspectChainCallback {
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 执行切面链末尾的删除。
     * @param fileInfo 待删除的文件信息
     * @param fileStorage 文件原有存储平台
     * @param fileRecorder 文件记录器
     * @return
     * 返回值: boolean
     * 返回值说明: 文件及记录删除均成功时返回 true
     */
    boolean run(FileInfo fileInfo,FileStorage fileStorage,FileRecorder fileRecorder);
}

