package com.zero.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zero.domain.FileDetail;
import com.zero.queryDto.FileDetailQuery;
import com.zero.recorder.FileRecorder;
import com.zero.vo.FileInfo;

public interface IFileDetailService extends IService<FileDetail>, FileRecorder
{
    IPage<FileDetail> selectFilePage(FileDetailQuery query);

    FileDetail selectFileById(Integer id);

    FileInfo upload(FileInfo fileInfo);

    boolean updateFileDetail(Integer id, FileDetail fileDetail);

    boolean deleteFileById(Integer id);
}
