package com.zero.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zero.domain.FileDetail;
import com.zero.mapper.FileDetailMapper;
import com.zero.queryDto.FileDetailQuery;
import com.zero.service.IFileDetailService;
import com.zero.vo.FileInfo;
import com.zero.vo.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 维护 biz_file_detail 文件记录，并为存储链提供记录查询和删除回调。
 */
@Service
@Slf4j
public class FileDetailServiceImpl extends ServiceImpl<FileDetailMapper, FileDetail> implements IFileDetailService
{
    private final TransactionTemplate deleteTransaction;

    @Lazy
    @Autowired
    private FileStorageService fileStorageService;

    /**
     * 初始化独立的删除事务，确保访问存储平台前数据库删除已经提交。
     *
     * @param transactionManager 文件数据库的事务管理器
     */
    public FileDetailServiceImpl(PlatformTransactionManager transactionManager)
    {
        deleteTransaction = new TransactionTemplate(transactionManager);
        deleteTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

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
    @Override
    public IPage<FileDetail> selectFilePage(FileDetailQuery query)
    {
        FileDetailQuery safeQuery = query == null ? new FileDetailQuery() : query;
        LambdaQueryWrapper<FileDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(safeQuery.getFileName()), FileDetail::getOriginalFilename, trim(safeQuery.getFileName()))
                .eq(StringUtils.hasText(safeQuery.getPlatform()), FileDetail::getPlatform, trim(safeQuery.getPlatform()))
                .eq(StringUtils.hasText(safeQuery.getExt()), FileDetail::getExt, trim(safeQuery.getExt()))
                .eq(StringUtils.hasText(safeQuery.getObjectId()), FileDetail::getObjectId, trim(safeQuery.getObjectId()))
                .eq(StringUtils.hasText(safeQuery.getObjectType()), FileDetail::getObjectType, trim(safeQuery.getObjectType()))
                .ge(safeQuery.hasBeginTime(), FileDetail::getCreateTime, safeQuery.getBeginDateTime())
                .lt(safeQuery.hasEndTime(), FileDetail::getCreateTime, safeQuery.getEndExclusiveDateTime())
                .orderByDesc(FileDetail::getCreateTime)
                .orderByDesc(FileDetail::getId);
        return page(safeQuery.toPage(), wrapper);
    }

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
    @Override
    public FileDetail selectFileById(Integer id)
    {
        if (id == null || id < 1)
        {
            throw new IllegalArgumentException("文件ID不能为空");
        }
        return getById(id);
    }

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
    @Override
    public FileInfo upload(FileInfo fileInfo)
    {
        if (fileInfo == null)
        {
            throw new IllegalArgumentException("文件信息不能为空");
        }
        FileDetail detail = toDetail(fileInfo);
        if (!save(detail))
        {
            throw new IllegalArgumentException("文件记录保存失败");
        }
        fileInfo.setId(detail.getId());
        return fileInfo;
    }

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
    @Override
    public boolean updateFileDetail(Integer id, FileDetail fileDetail)
    {
        if (id == null || id < 1)
        {
            throw new IllegalArgumentException("文件ID不能为空");
        }
        FileDetail oldDetail = getById(id);
        if (oldDetail == null)
        {
            throw new IllegalArgumentException("文件记录不存在");
        }
        if (fileDetail == null)
        {
            return true;
        }
        FileDetail update = new FileDetail();
        update.setId(id);
        update.setOriginalFilename(trim(fileDetail.getOriginalFilename()));
        update.setObjectId(trim(fileDetail.getObjectId()));
        update.setObjectType(trim(fileDetail.getObjectType()));
        return updateById(update);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 在存储成功后写入文件元数据。
     * @param fileInfo 存储平台返回的文件信息
     * @return
     * 返回值: boolean
     * 返回值说明: 成功时返回 true，持久化失败时抛出异常
     */
    @Override
    public boolean record(FileInfo fileInfo)
    {
        upload(fileInfo);
        return true;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 按去除首尾空白后的访问地址查询文件记录。
     * @param url 文件访问地址
     * @return
     * 返回值: FileInfo
     * 返回值说明: 匹配的文件信息，地址为空或找不到记录时返回 null
     */
    @Override
    public FileInfo getByUrl(String url)
    {
        if (!StringUtils.hasText(url))
        {
            return null;
        }
        FileDetail detail = lambdaQuery().eq(FileDetail::getUrl, url.trim()).one();
        return detail == null ? null : toFileInfo(detail);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 根据访问地址物理删除文件记录，供存储对象删除成功后回调。
     * @param url 已删除存储对象的访问地址
     * @return
     * 返回值: boolean
     * 返回值说明: 有有效地址时返回记录删除结果，地址为空时直接返回 true
     */
    @Override
    public boolean delete(String url)
    {
        if (!StringUtils.hasText(url))
        {
            return true;
        }
        return lambdaUpdate().eq(FileDetail::getUrl, url.trim()).remove();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 按主键删除记录并提交，再使用保留的文件信息清理存储对象。
     * @param id 文件记录主键
     * @return
     * 返回值: boolean
     * 返回值说明: 数据库删除成功后返回存储清理结果，数据库删除失败时抛出异常
     */
    @Override
    public boolean deleteFileById(Integer id)
    {
        FileInfo fileInfo = deleteTransaction.execute(status -> {
            FileDetail detail = selectFileById(id);
            if (detail == null)
            {
                throw new IllegalArgumentException("文件记录不存在");
            }
            FileInfo snapshot = toFileInfo(detail);
            if (!removeById(id))
            {
                throw new IllegalArgumentException("删除文件记录失败");
            }
            return snapshot;
        });
        try
        {
            if (fileStorageService.deleteStorageObjects(fileInfo))
            {
                return true;
            }
            log.warn("文件记录已删除，存储对象清理失败，id={}，platform={}", id, fileInfo.getPlatform());
        }
        catch (RuntimeException e)
        {
            log.warn("文件记录已删除，存储对象清理异常，id={}，platform={}，errorType={}",
                    id, fileInfo.getPlatform(), e.getClass().getSimpleName());
        }
        return false;
    }

    private FileDetail toDetail(FileInfo fileInfo)
    {
        FileDetail detail = new FileDetail();
        BeanUtils.copyProperties(fileInfo, detail);
        return detail;
    }

    private FileInfo toFileInfo(FileDetail detail)
    {
        FileInfo fileInfo = new FileInfo();
        BeanUtils.copyProperties(detail, fileInfo);
        return fileInfo;
    }

    private String trim(String value)
    {
        return value == null ? null : value.trim();
    }
}
