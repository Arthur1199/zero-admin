package com.zero.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.zero.system.domain.SysNotice;
import com.zero.system.mapper.SysNoticeMapper;
import com.zero.system.service.ISysNoticeService;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 公告 服务层实现
 */
@Service
public class SysNoticeServiceImpl implements ISysNoticeService
{
    /**
     * 通知公告数据访问字段
     */
    @Autowired
    private SysNoticeMapper noticeMapper;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 查询公告信息
     * @param noticeId 公告ID
     * @return
     * 返回值: 公告信息
     * 返回值说明：查询公告信息
     */
    @Override
    public SysNotice selectNoticeById(Long noticeId)
    {
        return noticeMapper.selectNoticeById(noticeId);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 查询公告列表
     * @param notice 公告信息
     * @return
     * 返回值: 公告集合
     * 返回值说明：查询公告列表
     */
    @Override
    public List<SysNotice> selectNoticeList(SysNotice notice)
    {
        return noticeMapper.selectNoticeList(notice);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 新增公告
     * @param notice 公告信息
     * @return
     * 返回值: 结果
     * 返回值说明：新增公告
     */
    @Override
    public int insertNotice(SysNotice notice)
    {
        return noticeMapper.insertNotice(notice);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 修改公告
     * @param notice 公告信息
     * @return
     * 返回值: 结果
     * 返回值说明：修改公告
     */
    @Override
    public int updateNotice(SysNotice notice)
    {
        return noticeMapper.updateNotice(notice);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 删除公告对象
     * @param noticeId 公告ID
     * @return
     * 返回值: 结果
     * 返回值说明：删除公告对象
     */
    @Override
    public int deleteNoticeById(Long noticeId)
    {
        return noticeMapper.deleteNoticeById(noticeId);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 批量删除公告信息
     * @param noticeIds 需要删除的公告ID
     * @return
     * 返回值: 结果
     * 返回值说明：批量删除公告信息
     */
    @Override
    public int deleteNoticeByIds(Long[] noticeIds)
    {
        return noticeMapper.deleteNoticeByIds(noticeIds);
    }
}
