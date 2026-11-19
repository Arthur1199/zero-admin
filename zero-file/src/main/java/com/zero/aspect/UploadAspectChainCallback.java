package com.zero.aspect;

import com.zero.platform.FileStorage;
import com.zero.recorder.FileRecorder;
import com.zero.vo.FileInfo;
import com.zero.vo.UploadPretreatment;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 文件上传切面链结束后执行的实际操作。
 */
public interface UploadAspectChainCallback {
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 执行切面链末尾的上传。
     * @param fileInfo 待写入的文件信息
     * @param pre 上传预处理及文件内容
     * @param fileStorage 选中的存储平台
     * @param fileRecorder 文件记录器
     * @return
     * 返回值: FileInfo
     * 返回值说明: 保存及记录均成功时返回文件信息，否则返回 null
     */
    FileInfo run(FileInfo fileInfo,UploadPretreatment pre,FileStorage fileStorage,FileRecorder fileRecorder);
}

