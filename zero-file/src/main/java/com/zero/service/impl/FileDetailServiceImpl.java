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
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 文件记录服务。
 */
@Service
public class FileDetailServiceImpl extends ServiceImpl<FileDetailMapper, FileDetail> implements IFileDetailService
{
    @Lazy
    @Autowired
    private FileStorageService fileStorageService;

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

    @Override
    public FileDetail selectFileById(Integer id)
    {
        if (id == null || id < 1)
        {
            throw new IllegalArgumentException("文件ID不能为空");
        }
        return getById(id);
    }

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

    @Override
    public boolean record(FileInfo fileInfo)
    {
        upload(fileInfo);
        return true;
    }

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

    @Override
    public boolean delete(String url)
    {
        if (!StringUtils.hasText(url))
        {
            return true;
        }
        return lambdaUpdate().eq(FileDetail::getUrl, url.trim()).remove();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteFileById(Integer id)
    {
        FileDetail detail = selectFileById(id);
        if (detail == null)
        {
            throw new IllegalArgumentException("文件记录不存在");
        }
        return fileStorageService.delete(detail.getUrl());
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
