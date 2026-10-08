package com.zero.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.zero.system.domain.SysOperLog;
import com.zero.system.mapper.SysOperLogMapper;
import com.zero.system.service.ISysOperLogService;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 操作日志 服务层处理
 */
@Service
public class SysOperLogServiceImpl implements ISysOperLogService
{
    /**
     * 操作日志数据访问字段
     */
    @Autowired
    private SysOperLogMapper operLogMapper;

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
    @Override
    public void insertOperlog(SysOperLog operLog)
    {
        operLogMapper.insertOperlog(operLog);
    }

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
    @Override
    public List<SysOperLog> selectOperLogList(SysOperLog operLog)
    {
        return operLogMapper.selectOperLogList(operLog);
    }

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
    @Override
    public int deleteOperLogByIds(Long[] operIds)
    {
        return operLogMapper.deleteOperLogByIds(operIds);
    }

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
    @Override
    public SysOperLog selectOperLogById(Long operId)
    {
        return operLogMapper.selectOperLogById(operId);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 清空操作日志
     * @return
     * 返回值: void
     * 返回值说明：清空操作日志
     */
    @Override
    public void cleanOperLog()
    {
        operLogMapper.cleanOperLog();
    }
}
