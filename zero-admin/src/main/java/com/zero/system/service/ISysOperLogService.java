package com.zero.system.service;

import java.util.List;
import com.zero.system.domain.SysOperLog;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 操作日志 服务层
 */
public interface ISysOperLogService
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 新增操作日志
     * @param operLog 操作日志对象
     * @return
     * 返回值: void
     * 返回值说明：新增操作日志
     */
    public void insertOperlog(SysOperLog operLog);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 查询系统操作日志集合
     * @param operLog 操作日志对象
     * @return
     * 返回值: 操作日志集合
     * 返回值说明：查询系统操作日志集合
     */
    public List<SysOperLog> selectOperLogList(SysOperLog operLog);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 批量删除系统操作日志
     * @param operIds 需要删除的操作日志ID
     * @return
     * 返回值: 结果
     * 返回值说明：批量删除系统操作日志
     */
    public int deleteOperLogByIds(Long[] operIds);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 查询操作日志详细
     * @param operId 操作ID
     * @return
     * 返回值: 操作日志对象
     * 返回值说明：查询操作日志详细
     */
    public SysOperLog selectOperLogById(Long operId);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 清空操作日志
     * @return
     * 返回值: void
     * 返回值说明：清空操作日志
     */
    public void cleanOperLog();
}
