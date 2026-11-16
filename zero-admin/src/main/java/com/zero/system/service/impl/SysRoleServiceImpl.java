package com.zero.system.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.zero.common.annotation.DataScope;
import com.zero.common.constant.UserConstants;
import com.zero.common.constant.RoleDataScopeConstants;
import com.zero.common.core.domain.entity.SysRole;
import com.zero.common.core.domain.entity.SysUser;
import com.zero.common.exception.ServiceException;
import com.zero.common.utils.SecurityUtils;
import com.zero.common.utils.StringUtils;
import com.zero.common.utils.spring.SpringUtils;
import com.zero.system.domain.SysRoleDept;
import com.zero.system.domain.SysRoleMenu;
import com.zero.system.domain.SysUserRole;
import com.zero.system.mapper.SysRoleDeptMapper;
import com.zero.system.mapper.SysRoleMapper;
import com.zero.system.mapper.SysRoleMenuMapper;
import com.zero.system.mapper.SysUserRoleMapper;
import com.zero.system.service.ISysRoleService;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 角色 业务层处理
 */
@Service
public class SysRoleServiceImpl implements ISysRoleService
{
    /**
     * 角色数据访问字段
     */
    @Autowired
    private SysRoleMapper roleMapper;
    /**
     * 角色菜单数据访问字段
     */

    @Autowired
    private SysRoleMenuMapper roleMenuMapper;
    /**
     * 用户角色数据访问字段
     */

    @Autowired
    private SysUserRoleMapper userRoleMapper;
    /**
     * 角色部门数据访问字段
     */

    @Autowired
    private SysRoleDeptMapper roleDeptMapper;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 根据条件分页查询角色数据
     * @param role 角色信息
     * @return
     * 返回值: 角色数据集合信息
     * 返回值说明：根据条件分页查询角色数据
     */
    @Override
    @DataScope(deptAlias = "d")
    public List<SysRole> selectRoleList(SysRole role)
    {
        return roleMapper.selectRoleList(role);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 根据用户ID查询角色
     * @param userId 用户ID
     * @return
     * 返回值: 角色列表
     * 返回值说明：根据用户ID查询角色
     */
    @Override
    public List<SysRole> selectRolesByUserId(Long userId)
    {
        List<SysRole> userRoles = roleMapper.selectRolePermissionByUserId(userId);
        List<SysRole> roles = selectRoleAll();
        for (SysRole role : roles)
        {
            for (SysRole userRole : userRoles)
            {
                if (role.getRoleId().longValue() == userRole.getRoleId().longValue())
                {
                    role.setFlag(true);
                    break;
                }
            }
        }
        return roles;
    }

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
    @Override
    public Set<String> selectRolePermissionByUserId(Long userId)
    {
        List<SysRole> perms = roleMapper.selectRolePermissionByUserId(userId);
        Set<String> permsSet = new HashSet<>();
        for (SysRole perm : perms)
        {
            if (StringUtils.isNotNull(perm))
            {
                permsSet.addAll(Arrays.asList(perm.getRoleKey().trim().split(",")));
            }
        }
        return permsSet;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 查询所有角色
     * @return
     * 返回值: 角色列表
     * 返回值说明：查询所有角色
     */
    @Override
    public List<SysRole> selectRoleAll()
    {
        return SpringUtils.getAopProxy(this).selectRoleList(new SysRole());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 根据用户ID获取角色选择框列表
     * @param userId 用户ID
     * @return
     * 返回值: 选中角色ID列表
     * 返回值说明：根据用户ID获取角色选择框列表
     */
    @Override
    public List<Long> selectRoleListByUserId(Long userId)
    {
        return roleMapper.selectRoleListByUserId(userId);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 通过角色ID查询角色
     * @param roleId 角色ID
     * @return
     * 返回值: 角色对象信息
     * 返回值说明：通过角色ID查询角色
     */
    @Override
    public SysRole selectRoleById(Long roleId)
    {
        return roleMapper.selectRoleById(roleId);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 校验角色名称是否唯一
     * @param role 角色信息
     * @return
     * 返回值: 结果
     * 返回值说明：校验角色名称是否唯一
     */
    @Override
    public boolean checkRoleNameUnique(SysRole role)
    {
        Long roleId = StringUtils.isNull(role.getRoleId()) ? -1L : role.getRoleId();
        SysRole info = roleMapper.checkRoleNameUnique(role.getRoleName());
        if (StringUtils.isNotNull(info) && info.getRoleId().longValue() != roleId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 校验角色权限是否唯一
     * @param role 角色信息
     * @return
     * 返回值: 结果
     * 返回值说明：校验角色权限是否唯一
     */
    @Override
    public boolean checkRoleKeyUnique(SysRole role)
    {
        Long roleId = StringUtils.isNull(role.getRoleId()) ? -1L : role.getRoleId();
        SysRole info = roleMapper.checkRoleKeyUnique(role.getRoleKey());
        if (StringUtils.isNotNull(info) && info.getRoleId().longValue() != roleId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 校验角色是否允许操作
     * @param role 角色信息
     * @return
     * 返回值: void
     * 返回值说明：校验角色是否允许操作
     */
    @Override
    public void checkRoleAllowed(SysRole role)
    {
        if (StringUtils.isNotNull(role.getRoleId()) && role.isAdmin())
        {
            throw new ServiceException("不允许操作超级管理员角色");
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 校验角色是否有数据权限
     * @param roleIds 角色id
     * @return
     * 返回值: void
     * 返回值说明：校验角色是否有数据权限
     */
    @Override
    public void checkRoleDataScope(Long... roleIds)
    {
        if (!SysUser.isAdmin(SecurityUtils.getUserId()))
        {
            for (Long roleId : roleIds)
            {
                SysRole role = new SysRole();
                role.setRoleId(roleId);
                List<SysRole> roles = SpringUtils.getAopProxy(this).selectRoleList(role);
                if (StringUtils.isEmpty(roles))
                {
                    throw new ServiceException("没有权限访问角色数据！");
                }
            }
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 通过角色ID查询角色使用数量
     * @param roleId 角色ID
     * @return
     * 返回值: 结果
     * 返回值说明：通过角色ID查询角色使用数量
     */
    @Override
    public int countUserRoleByRoleId(Long roleId)
    {
        return userRoleMapper.countUserRoleByRoleId(roleId);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 在同一事务内新增角色、菜单及自定部门范围，范围无效时不写入；兼容旧调用默认全部数据
     * @param role 角色信息
     * @return
     * 返回值: 结果
     * 返回值说明：新增保存角色信息
     */
    @Override
    @Transactional
    public int insertRole(SysRole role)
    {
        validateRoleDataScope(role);
        // 新增角色信息
        roleMapper.insertRole(role);
        int rows = insertRoleMenu(role);
        if (RoleDataScopeConstants.CUSTOM.equals(role.getDataScope()))
        {
            insertRoleDept(role);
        }
        return rows;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 16:52
     * 说明: 保存前校验数据范围及部门集合，防止无效范围或隐藏部门关系写入
     * @param role 待保存角色，调用方确定是否修改数据范围
     * @return
     * 返回值: void
     * 返回值说明: 校验失败抛业务异常，尚未执行数据库写入
     */
    private void validateRoleDataScope(SysRole role)
    {
        if (role == null)
        {
            throw new ServiceException("角色信息不能为空");
        }
        String scope = StringUtils.isEmpty(role.getDataScope()) ? RoleDataScopeConstants.ALL : role.getDataScope();
        if (!RoleDataScopeConstants.VALUES.contains(scope))
        {
            throw new ServiceException("数据权限范围无效");
        }
        role.setDataScope(scope);
        if (RoleDataScopeConstants.CUSTOM.equals(scope))
        {
            Long[] deptIds = role.getDeptIds();
            if (deptIds == null || deptIds.length == 0)
            {
                throw new ServiceException("自定义数据权限至少需要选择一个部门");
            }
            if (Arrays.stream(deptIds).anyMatch(id -> id == null || id <= 0))
            {
                throw new ServiceException("部门编号无效");
            }
            role.setDeptIds(Arrays.stream(deptIds).distinct().toArray(Long[]::new));
        }
        else
        {
            // 非自定范围不保存隐藏或过期的部门选择。
            role.setDeptIds(new Long[0]);
        }
        if (role.getMenuIds() == null)
        {
            role.setMenuIds(new Long[0]);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 同一事务保存角色、菜单及显式提交的数据范围，旧调用未传范围时保留部门关联
     * @param role 角色信息
     * @return
     * 返回值: 结果
     * 返回值说明：修改保存角色信息
     */
    @Override
    @Transactional
    public int updateRole(SysRole role)
    {
        if (role == null || role.getRoleId() == null)
        {
            throw new ServiceException("角色编号不能为空");
        }
        boolean updateScope = StringUtils.isNotEmpty(role.getDataScope());
        if (updateScope)
        {
            validateRoleDataScope(role);
        }
        // 角色、菜单和部门关系共同提交，异常由事务回滚。
        roleMapper.updateRole(role);
        roleMenuMapper.deleteRoleMenuByRoleId(role.getRoleId());
        int rows = insertRoleMenu(role);
        if (updateScope)
        {
            roleDeptMapper.deleteRoleDeptByRoleId(role.getRoleId());
            if (RoleDataScopeConstants.CUSTOM.equals(role.getDataScope()))
            {
                insertRoleDept(role);
            }
        }
        return rows;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 修改角色状态
     * @param role 角色信息
     * @return
     * 返回值: 结果
     * 返回值说明：修改角色状态
     */
    @Override
    public int updateRoleStatus(SysRole role)
    {
        return roleMapper.updateRole(role);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 修改数据权限信息
     * @param role 角色信息
     * @return
     * 返回值: 结果
     * 返回值说明：修改数据权限信息
     */
    @Override
    @Transactional
    public int authDataScope(SysRole role)
    {
        // 修改角色信息
        roleMapper.updateRole(role);
        // 删除角色与部门关联
        roleDeptMapper.deleteRoleDeptByRoleId(role.getRoleId());
        // 新增角色和部门信息（数据权限）
        return insertRoleDept(role);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 新增角色菜单信息
     * @param role 角色对象
     * @return
     * 返回值: int
     * 返回值说明：新增角色菜单信息
     */
    public int insertRoleMenu(SysRole role)
    {
        int rows = 1;
        // 新增用户与角色管理
        List<SysRoleMenu> list = new ArrayList<SysRoleMenu>();
        for (Long menuId : role.getMenuIds())
        {
            SysRoleMenu rm = new SysRoleMenu();
            rm.setRoleId(role.getRoleId());
            rm.setMenuId(menuId);
            list.add(rm);
        }
        if (list.size() > 0)
        {
            rows = roleMenuMapper.batchRoleMenu(list);
        }
        return rows;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 新增角色部门信息(数据权限)
     * @param role 角色对象
     * @return
     * 返回值: int
     * 返回值说明：新增角色部门信息(数据权限)
     */
    public int insertRoleDept(SysRole role)
    {
        int rows = 1;
        // 新增角色与部门（数据权限）管理
        List<SysRoleDept> list = new ArrayList<SysRoleDept>();
        for (Long deptId : role.getDeptIds())
        {
            SysRoleDept rd = new SysRoleDept();
            rd.setRoleId(role.getRoleId());
            rd.setDeptId(deptId);
            list.add(rd);
        }
        if (list.size() > 0)
        {
            rows = roleDeptMapper.batchRoleDept(list);
        }
        return rows;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 通过角色ID删除角色
     * @param roleId 角色ID
     * @return
     * 返回值: 结果
     * 返回值说明：通过角色ID删除角色
     */
    @Override
    @Transactional
    public int deleteRoleById(Long roleId)
    {
        // 删除角色与菜单关联
        roleMenuMapper.deleteRoleMenuByRoleId(roleId);
        // 删除角色与部门关联
        roleDeptMapper.deleteRoleDeptByRoleId(roleId);
        return roleMapper.deleteRoleById(roleId);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 批量删除角色信息
     * @param roleIds 需要删除的角色ID
     * @return
     * 返回值: 结果
     * 返回值说明：批量删除角色信息
     */
    @Override
    @Transactional
    public int deleteRoleByIds(Long[] roleIds)
    {
        for (Long roleId : roleIds)
        {
            checkRoleAllowed(new SysRole(roleId));
            checkRoleDataScope(roleId);
            SysRole role = selectRoleById(roleId);
            if (countUserRoleByRoleId(roleId) > 0)
            {
                throw new ServiceException(String.format("%1$s已分配,不能删除", role.getRoleName()));
            }
        }
        // 删除角色与菜单关联
        roleMenuMapper.deleteRoleMenu(roleIds);
        // 删除角色与部门关联
        roleDeptMapper.deleteRoleDept(roleIds);
        return roleMapper.deleteRoleByIds(roleIds);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 取消授权用户角色
     * @param userRole 用户和角色关联信息
     * @return
     * 返回值: 结果
     * 返回值说明：取消授权用户角色
     */
    @Override
    public int deleteAuthUser(SysUserRole userRole)
    {
        return userRoleMapper.deleteUserRoleInfo(userRole);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 批量取消授权用户角色
     * @param roleId 角色ID
     * @param userIds 需要取消授权的用户数据ID
     * @return
     * 返回值: 结果
     * 返回值说明：批量取消授权用户角色
     */
    @Override
    public int deleteAuthUsers(Long roleId, Long[] userIds)
    {
        return userRoleMapper.deleteUserRoleInfos(roleId, userIds);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 批量选择授权用户角色
     * @param roleId 角色ID
     * @param userIds 需要授权的用户数据ID
     * @return
     * 返回值: 结果
     * 返回值说明：批量选择授权用户角色
     */
    @Override
    public int insertAuthUsers(Long roleId, Long[] userIds)
    {
        // 新增用户与角色管理
        List<SysUserRole> list = new ArrayList<SysUserRole>();
        for (Long userId : userIds)
        {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(userId);
            ur.setRoleId(roleId);
            list.add(ur);
        }
        return userRoleMapper.batchUserRole(list);
    }
}
