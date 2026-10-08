package com.zero.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.zero.common.core.domain.entity.SysMenu;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 菜单表 数据层
 */
public interface SysMenuMapper
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 查询系统菜单列表
     * @param menu 菜单信息
     * @return
     * 返回值: 菜单列表
     * 返回值说明：查询系统菜单列表
     */
    public List<SysMenu> selectMenuList(SysMenu menu);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 根据用户所有权限
     * @return
     * 返回值: 权限列表
     * 返回值说明：根据用户所有权限
     */
    public List<String> selectMenuPerms();

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 根据用户查询系统菜单列表
     * @param menu 菜单信息
     * @return
     * 返回值: 菜单列表
     * 返回值说明：根据用户查询系统菜单列表
     */
    public List<SysMenu> selectMenuListByUserId(SysMenu menu);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 根据角色ID查询权限
     * @param roleId 角色ID
     * @return
     * 返回值: 权限列表
     * 返回值说明：根据角色ID查询权限
     */
    public List<String> selectMenuPermsByRoleId(Long roleId);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 根据用户ID查询权限
     * @param userId 用户ID
     * @return
     * 返回值: 权限列表
     * 返回值说明：根据用户ID查询权限
     */
    public List<String> selectMenuPermsByUserId(Long userId);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 根据用户ID查询菜单
     * @return
     * 返回值: 菜单列表
     * 返回值说明：根据用户ID查询菜单
     */
    public List<SysMenu> selectMenuTreeAll();

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 根据用户ID查询菜单
     * @param userId 用户ID
     * @return
     * 返回值: 菜单列表
     * 返回值说明：根据用户ID查询菜单
     */
    public List<SysMenu> selectMenuTreeByUserId(Long userId);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 根据角色ID查询菜单树信息
     * @param roleId 角色ID
     * @param menuCheckStrictly 菜单树选择项是否关联显示
     * @return
     * 返回值: 选中菜单列表
     * 返回值说明：根据角色ID查询菜单树信息
     */
    public List<Long> selectMenuListByRoleId(@Param("roleId") Long roleId, @Param("menuCheckStrictly") boolean menuCheckStrictly);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 根据菜单ID查询信息
     * @param menuId 菜单ID
     * @return
     * 返回值: 菜单信息
     * 返回值说明：根据菜单ID查询信息
     */
    public SysMenu selectMenuById(Long menuId);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 是否存在菜单子节点
     * @param menuId 菜单ID
     * @return
     * 返回值: 结果
     * 返回值说明：是否存在菜单子节点
     */
    public int hasChildByMenuId(Long menuId);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 新增菜单信息
     * @param menu 菜单信息
     * @return
     * 返回值: 结果
     * 返回值说明：新增菜单信息
     */
    public int insertMenu(SysMenu menu);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 修改菜单信息
     * @param menu 菜单信息
     * @return
     * 返回值: 结果
     * 返回值说明：修改菜单信息
     */
    public int updateMenu(SysMenu menu);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 删除菜单管理信息
     * @param menuId 菜单ID
     * @return
     * 返回值: 结果
     * 返回值说明：删除菜单管理信息
     */
    public int deleteMenuById(Long menuId);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 校验菜单名称是否唯一
     * @param menuName 菜单名称
     * @param parentId 父菜单ID
     * @return
     * 返回值: 结果
     * 返回值说明：校验菜单名称是否唯一
     */
    public SysMenu checkMenuNameUnique(@Param("menuName") String menuName, @Param("parentId") Long parentId);
}
