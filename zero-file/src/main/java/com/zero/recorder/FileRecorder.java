package com.zero.recorder;

import com.zero.vo.FileInfo;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 文件记录器接口。
 */
public interface FileRecorder {

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 在文件存储成功后保存文件元数据。
     * @param fileInfo 包含文件地址和存储位置的元数据
     * @return
     * 返回值: boolean
     * 返回值说明: 记录处理是否成功
     */
    boolean record(FileInfo fileInfo);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 通过访问地址查找存储平台及对象位置。
     * @param url 文件访问地址
     * @return
     * 返回值: FileInfo
     * 返回值说明: 已记录的文件信息，查不到时返回 null
     */
    FileInfo getByUrl(String url);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 在存储对象删除后按访问地址清理文件记录。
     * @param url 被删除文件的访问地址
     * @return
     * 返回值: boolean
     * 返回值说明: 记录清理结果
     */
    boolean delete(String url);
}

