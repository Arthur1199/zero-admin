package com.zero.quartz.mapper;

import java.util.List;
import com.zero.quartz.domain.SysJob;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 调度任务信息 数据层
 */
public interface SysJobMapper
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 查询调度任务日志集合
     * @param job 调度信息
     * @return
     * 返回值: 操作日志集合
     * 返回值说明：查询调度任务日志集合
     */
    public List<SysJob> selectJobList(SysJob job);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 查询所有调度任务
     * @return
     * 返回值: 调度任务列表
     * 返回值说明：查询所有调度任务
     */
    public List<SysJob> selectJobAll();

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 通过调度ID查询调度任务信息
     * @param jobId 调度ID
     * @return
     * 返回值: 角色对象信息
     * 返回值说明：通过调度ID查询调度任务信息
     */
    public SysJob selectJobById(Long jobId);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 通过调度ID删除调度任务信息
     * @param jobId 调度ID
     * @return
     * 返回值: int
     * 返回值说明：通过调度ID删除调度任务信息
     */
    public int deleteJobById(Long jobId);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 批量删除调度任务信息
     * @param ids 需要删除的数据ID
     * @return
     * 返回值: int
     * 返回值说明：批量删除调度任务信息
     */
    public int deleteJobByIds(Long[] ids);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 修改调度任务信息
     * @param job 调度任务信息
     * @return
     * 返回值: int
     * 返回值说明：修改调度任务信息
     */
    public int updateJob(SysJob job);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 新增调度任务信息
     * @param job 调度任务信息
     * @return
     * 返回值: int
     * 返回值说明：新增调度任务信息
     */
    public int insertJob(SysJob job);
}
