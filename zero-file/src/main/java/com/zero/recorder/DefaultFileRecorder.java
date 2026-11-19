package com.zero.recorder;

import com.zero.vo.FileInfo;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 缺少业务记录器时的默认实现。保存和删除回调直接返回成功，按地址查询始终返回空。
 */
public class DefaultFileRecorder implements FileRecorder {
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 缺少持久化记录器时，给上传流程提供可继续执行的记录结果。
     * @param fileInfo 存储平台生成的文件信息
     * @return
     * 返回值: boolean
     * 返回值说明: 固定返回 true
     */
    @Override
    public boolean record(FileInfo fileInfo) {
        return true;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 提供无持久化记录器时的查询入口。
     * @param url 文件访问地址
     * @return
     * 返回值: FileInfo
     * 返回值说明: 固定返回 null
     */
    @Override
    public FileInfo getByUrl(String url) {
        return null;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 为没有持久化记录器的存储流程确认清理结果。
     * @param url 文件访问地址
     * @return
     * 返回值: boolean
     * 返回值说明: 固定返回 true
     */
    @Override
    public boolean delete(String url) {
        return true;
    }
}

