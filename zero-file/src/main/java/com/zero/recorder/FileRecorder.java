package com.zero.recorder;

import com.zero.vo.FileInfo;

/**
 * 文件记录器接口。
 */
public interface FileRecorder {

    /**
     * 保存文件记录
     */
    boolean record(FileInfo fileInfo);

    /**
     * 根据 url 获取文件记录
     */
    FileInfo getByUrl(String url);

    /**
     * 根据 url 删除文件记录
     */
    boolean delete(String url);
}

