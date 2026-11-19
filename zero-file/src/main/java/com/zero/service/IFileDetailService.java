package com.zero.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zero.domain.FileDetail;
import com.zero.queryDto.FileDetailQuery;
import com.zero.recorder.FileRecorder;
import com.zero.vo.FileInfo;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 定义 biz_file_detail 文件记录管理及存储层记录回调。
 */
public interface IFileDetailService extends IService<FileDetail>, FileRecorder
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 按条件分页查询文件记录，结束日期包含当天。
     * @param query 文件名、平台、业务类型、时间和分页条件
     * @return
     * 返回值: IPage<FileDetail>
     * 返回值说明: 按创建时间和主键倒序的分页结果
     */
    IPage<FileDetail> selectFilePage(FileDetailQuery query);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 按主键查询文件记录，空值或小于 1 的主键抛出参数异常。
     * @param id 文件记录主键
     * @return
     * 返回值: FileDetail
     * 返回值说明: 文件记录，主键有效且查不到时返回 null
     */
    FileDetail selectFileById(Integer id);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 保存已上传的文件信息并回填主键。
     * @param fileInfo 存储平台生成的文件信息
     * @return
     * 返回值: FileInfo
     * 返回值说明: 带记录主键的文件信息，保存失败时抛出异常
     */
    FileInfo upload(FileInfo fileInfo);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 更新记录的原始文件名及业务归属。
     * @param id 文件记录主键
     * @param fileDetail 要更新的记录字段
     * @return
     * 返回值: boolean
     * 返回值说明: 更新结果，记录存在且请求体为空时返回 true
     */
    boolean updateFileDetail(Integer id, FileDetail fileDetail);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 按记录 ID 发起文件删除，先由存储平台删除对象，再处理记录。
     * @param id 文件记录主键
     * @return
     * 返回值: boolean
     * 返回值说明: 文件删除结果，记录不存在时抛出参数异常
     */
    boolean deleteFileById(Integer id);
}
