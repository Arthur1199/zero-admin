package com.zero.system.mapper;

import java.util.List;
import com.zero.system.domain.SysGoogle;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 用户谷歌动态码绑定 数据层
 */
public interface SysGoogleMapper
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 查询谷歌动态码绑定列表
     * @param sysGoogle 查询条件
     * @return
     * 返回值: List<SysGoogle>
     * 返回值说明: 绑定列表
     */
    public List<SysGoogle> selectGoogleList(SysGoogle sysGoogle);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 根据用户ID查询绑定记录
     * @param userId 用户ID
     * @return
     * 返回值: SysGoogle
     * 返回值说明: 绑定记录
     */
    public SysGoogle selectGoogleByUserId(Long userId);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 在 sys_google 插入用户的动态码绑定记录，回填生成的记录主键。
     * @param sysGoogle 绑定记录
     * @return
     * 返回值: int
     * 返回值说明: 插入的记录数
     */
    public int insertGoogle(SysGoogle sysGoogle);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 按用户 ID 更新 sys_google 的动态码密钥及更新审计字段。
     * @param sysGoogle 绑定记录
     * @return
     * 返回值: int
     * 返回值说明: 更新的记录数
     */
    public int updateGoogleAuthKey(SysGoogle sysGoogle);
}
