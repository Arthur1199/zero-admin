package com.zero.aspect;

import com.zero.platform.FileStorage;
import com.zero.vo.FileInfo;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 文件存在性检查切面链结束后执行的实际操作。
 */
public interface ExistsAspectChainCallback {
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 执行切面链末尾的存在性检查。
     * @param fileInfo 要检查的文件信息
     * @param fileStorage 文件原有存储平台
     * @return
     * 返回值: boolean
     * 返回值说明: 平台确认对象存在时返回 true
     */
    boolean run(FileInfo fileInfo,FileStorage fileStorage);
}

