package com.zero.system.mapper;

import java.util.List;
import com.zero.system.domain.SysGoogle;

/**
 * 用户谷歌动态码绑定 数据层
 */
public interface SysGoogleMapper
{
    /**
     * 查询谷歌动态码绑定列表
     *
     * @param sysGoogle 查询条件
     * @return 绑定列表
     */
    public List<SysGoogle> selectGoogleList(SysGoogle sysGoogle);

    /**
     * 根据用户ID查询绑定记录
     *
     * @param userId 用户ID
     * @return 绑定记录
     */
    public SysGoogle selectGoogleByUserId(Long userId);

    /**
     * 新增绑定记录
     *
     * @param sysGoogle 绑定记录
     * @return 结果
     */
    public int insertGoogle(SysGoogle sysGoogle);

    /**
     * 更新密钥
     *
     * @param sysGoogle 绑定记录
     * @return 结果
     */
    public int updateGoogleAuthKey(SysGoogle sysGoogle);
}
