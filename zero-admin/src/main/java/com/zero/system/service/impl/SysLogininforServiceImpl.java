package com.zero.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.zero.system.domain.SysLogininfor;
import com.zero.system.mapper.SysLogininforMapper;
import com.zero.system.service.ISysLogininforService;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 系统访问日志情况信息 服务层处理
 */
@Service
public class SysLogininforServiceImpl implements ISysLogininforService
{
    /**
     * 登录日志数据访问字段
     */

    @Autowired
    private SysLogininforMapper logininforMapper;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 新增系统登录日志
     * @param logininfor 访问日志对象
     * @return
     * 返回值: void
     * 返回值说明：新增系统登录日志
     */
    @Override
    public void insertLogininfor(SysLogininfor logininfor)
    {
        logininforMapper.insertLogininfor(logininfor);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 查询系统登录日志集合
     * @param logininfor 访问日志对象
     * @return
     * 返回值: 登录记录集合
     * 返回值说明：查询系统登录日志集合
     */
    @Override
    public List<SysLogininfor> selectLogininforList(SysLogininfor logininfor)
    {
        return logininforMapper.selectLogininforList(logininfor);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 批量删除系统登录日志
     * @param infoIds 需要删除的登录日志ID
     * @return
     * 返回值: 结果
     * 返回值说明：批量删除系统登录日志
     */
    @Override
    public int deleteLogininforByIds(Long[] infoIds)
    {
        return logininforMapper.deleteLogininforByIds(infoIds);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 清空系统登录日志
     * @return
     * 返回值: void
     * 返回值说明：清空系统登录日志
     */
    @Override
    public void cleanLogininfor()
    {
        logininforMapper.cleanLogininfor();
    }
}
