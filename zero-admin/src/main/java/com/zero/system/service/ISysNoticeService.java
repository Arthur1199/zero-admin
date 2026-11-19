package com.zero.system.service;

import java.util.List;
import com.zero.system.domain.SysNotice;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 公告 服务层
 */
public interface ISysNoticeService
{
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
    public SysNotice selectNoticeById(Long noticeId);

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
    public List<SysNotice> selectNoticeList(SysNotice notice);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 新增公告
     * @param notice 公告信息
     * @return
     * 返回值: int
     * 返回值说明：新增公告
     */
    public int insertNotice(SysNotice notice);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 修改公告
     * @param notice 公告信息
     * @return
     * 返回值: int
     * 返回值说明：修改公告
     */
    public int updateNotice(SysNotice notice);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 删除公告信息
     * @param noticeId 公告ID
     * @return
     * 返回值: int
     * 返回值说明：删除公告信息
     */
    public int deleteNoticeById(Long noticeId);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 批量删除公告信息
     * @param noticeIds 需要删除的公告ID
     * @return
     * 返回值: int
     * 返回值说明：批量删除公告信息
     */
    public int deleteNoticeByIds(Long[] noticeIds);
}
