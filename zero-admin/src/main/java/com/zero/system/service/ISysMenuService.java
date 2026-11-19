package com.zero.system.service;

import java.util.List;
import java.util.Set;
import com.zero.common.core.domain.TreeSelect;
import com.zero.common.core.domain.entity.SysMenu;
import com.zero.system.domain.vo.RouterVo;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 菜单 业务层
 */
public interface ISysMenuService
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 根据用户查询系统菜单列表
     * @param userId 用户ID
     * @return
     * 返回值: 菜单列表
     * 返回值说明：根据用户查询系统菜单列表
     */
    public List<SysMenu> selectMenuList(Long userId);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 根据用户查询系统菜单列表
     * @param menu 菜单信息
     * @param userId 用户ID
     * @return
     * 返回值: 菜单列表
     * 返回值说明：根据用户查询系统菜单列表
     */
    public List<SysMenu> selectMenuList(SysMenu menu, Long userId);

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
    public Set<String> selectMenuPermsByUserId(Long userId);

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
    public Set<String> selectMenuPermsByRoleId(Long roleId);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 根据用户ID查询菜单树信息
     * @param userId 用户ID
     * @return
     * 返回值: 菜单列表
     * 返回值说明：根据用户ID查询菜单树信息
     */
    public List<SysMenu> selectMenuTreeByUserId(Long userId);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 根据角色ID查询菜单树信息
     * @param roleId 角色ID
     * @return
     * 返回值: 选中菜单列表
     * 返回值说明：根据角色ID查询菜单树信息
     */
    public List<Long> selectMenuListByRoleId(Long roleId);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 构建前端路由所需要的菜单
     * @param menus 菜单列表
     * @return
     * 返回值: 路由列表
     * 返回值说明：构建前端路由所需要的菜单
     */
    public List<RouterVo> buildMenus(List<SysMenu> menus);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 构建前端所需要树结构
     * @param menus 菜单列表
     * @return
     * 返回值: 树结构列表
     * 返回值说明：构建前端所需要树结构
     */
    public List<SysMenu> buildMenuTree(List<SysMenu> menus);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 构建前端所需要下拉树结构
     * @param menus 菜单列表
     * @return
     * 返回值: 下拉树结构列表
     * 返回值说明：构建前端所需要下拉树结构
     */
    public List<TreeSelect> buildMenuTreeSelect(List<SysMenu> menus);

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
     * 返回值: 结果 true 存在 false 不存在
     * 返回值说明：是否存在菜单子节点
     */
    public boolean hasChildByMenuId(Long menuId);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 查询菜单是否存在角色
     * @param menuId 菜单ID
     * @return
     * 返回值: 结果 true 存在 false 不存在
     * 返回值说明：查询菜单是否存在角色
     */
    public boolean checkMenuExistRole(Long menuId);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 新增保存菜单信息
     * @param menu 菜单信息
     * @return
     * 返回值: int
     * 返回值说明：新增保存菜单信息
     */
    public int insertMenu(SysMenu menu);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 修改保存菜单信息
     * @param menu 菜单信息
     * @return
     * 返回值: int
     * 返回值说明：修改保存菜单信息
     */
    public int updateMenu(SysMenu menu);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 删除菜单管理信息
     * @param menuId 菜单ID
     * @return
     * 返回值: int
     * 返回值说明：删除菜单管理信息
     */
    public int deleteMenuById(Long menuId);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 校验菜单名称是否唯一
     * @param menu 菜单信息
     * @return
     * 返回值: boolean
     * 返回值说明：校验菜单名称是否唯一
     */
    public boolean checkMenuNameUnique(SysMenu menu);
}
