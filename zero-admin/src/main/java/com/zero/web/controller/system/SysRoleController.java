package com.zero.web.controller.system;

import java.util.List;
import com.zero.common.annotation.Logical;
import com.zero.common.constant.RolePermissionConstants;
import com.zero.framework.web.service.PermissionService;
import com.zero.framework.security.context.PermissionContextHolder;
import org.springframework.security.access.AccessDeniedException;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import com.zero.common.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.zero.common.annotation.Log;
import com.zero.common.annotation.SystemModule;
import com.zero.common.core.controller.BaseController;
import com.zero.common.core.domain.AjaxResult;
import com.zero.common.core.domain.entity.SysDept;
import com.zero.common.core.domain.entity.SysRole;
import com.zero.common.core.domain.entity.SysUser;
import com.zero.common.core.domain.model.LoginUser;
import com.zero.common.core.page.TableDataInfo;
import com.zero.common.enums.BusinessType;
import com.zero.common.enums.SystemModuleEnum;
import com.zero.common.utils.StringUtils;
import com.zero.common.utils.poi.ExcelUtil;
import com.zero.framework.web.service.SysPermissionService;
import com.zero.framework.web.service.TokenService;
import com.zero.system.domain.SysUserRole;
import com.zero.system.service.ISysDeptService;
import com.zero.system.service.ISysRoleService;
import com.zero.system.service.ISysUserService;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 角色信息
 */
@RestController
@RequestMapping("/system/role")
@SystemModule(SystemModuleEnum.SYSTEM)
public class SysRoleController extends BaseController
{
    /**
     * 角色服务字段
     */
    @Autowired
    private ISysRoleService roleService;
    /**
     * 令牌服务字段
     */

    @Autowired
    private TokenService tokenService;
    /**
     * 权限服务字段
     */

    @Autowired
    private SysPermissionService permissionService;
    /**
     * 用户服务字段
     */

    @Autowired
    private ISysUserService userService;
    /**
     * 部门服务字段
     */

    @Autowired
    private ISysDeptService deptService;

    /** 独立操作权限校验，防止普通角色修改绕过数据权限授权 */
    @Autowired
    private PermissionService operationPermissionService;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 18:10
     * 说明: 具有角色新增权限时读取建议显示顺序，不要求角色查询权限，不执行写入
     * @return
     * 返回值: AjaxResult
     * 返回值说明: data为未删除角色最大顺序加1，允许新增表单手动修改
     */
    @RequiresPermissions("system:role:add")
    @GetMapping("/nextSort")
    public AjaxResult nextSort()
    {
        return success(roleService.selectNextRoleSort());
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 查询
     * @param role 角色参数
     * @return
     * 返回值: TableDataInfo
     * 返回值说明：查询
     */

    @RequiresPermissions("system:role:list")
    @GetMapping("/list")
    public TableDataInfo list(SysRole role)
    {
        startPage();
        List<SysRole> list = roleService.selectRoleList(role);
        return getDataTable(list);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 导出
     * @param response 响应参数
     * @param role 角色参数
     * @return
     * 返回值: void
     * 返回值说明：导出
     */

    @Log(title = "角色管理", businessType = BusinessType.EXPORT)
    @RequiresPermissions("system:role:export")
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysRole role)
    {
        List<SysRole> list = roleService.selectRoleList(role);
        ExcelUtil<SysRole> util = new ExcelUtil<SysRole>(SysRole.class);
        util.exportExcel(response, list, "角色数据");
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 按角色查询、修改或独立数据权限授权读取回显，仍校验角色数据范围
     * @param roleId 角色ID参数
     * @return
     * 返回值: AjaxResult
     * 返回值说明：根据角色编号获取详细信息
     */
    @RequiresPermissions(value = {"system:role:query", "system:role:edit", RolePermissionConstants.DATA_SCOPE}, logical = Logical.OR)
    @GetMapping(value = "/{roleId}")
    public AjaxResult getInfo(@PathVariable Long roleId)
    {
        roleService.checkRoleDataScope(roleId);
        return success(roleService.selectRoleById(roleId));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 新增角色
     * @param role 角色参数
     * @return
     * 返回值: AjaxResult
     * 返回值说明：新增角色
     */
    @RequiresPermissions("system:role:add")
    @Log(title = "角色管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysRole role)
    {
        if (!roleService.checkRoleNameUnique(role))
        {
            return error("新增角色'" + role.getRoleName() + "'失败，角色名称已存在");
        }
        else if (!roleService.checkRoleKeyUnique(role))
        {
            return error("新增角色'" + role.getRoleName() + "'失败，角色权限已存在");
        }
        role.setCreateBy(getUsername());
        return toAjax(roleService.insertRole(role));

    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 角色修改权限保存基础信息和菜单，提交数据范围时还必须拥有独立数据权限
     * @param role 角色参数
     * @return
     * 返回值: AjaxResult
     * 返回值说明：修改保存角色
     */
    @RequiresPermissions("system:role:edit")
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysRole role)
    {
        boolean canEditScope = operationPermissionService.hasPermi(RolePermissionConstants.DATA_SCOPE);
        PermissionContextHolder.setContext("system:role:edit");
        if (!canEditScope)
        {
            if (StringUtils.isNotEmpty(role.getDataScope()) || role.getDeptIds() != null)
            {
                throw new AccessDeniedException("无权限：" + RolePermissionConstants.DATA_SCOPE);
            }
            // 基础编辑不允许修改部门联动设置，兼容实体boolean无法区分缺省值。
            SysRole saved = roleService.selectRoleById(role.getRoleId());
            if (saved != null) role.setDeptCheckStrictly(saved.isDeptCheckStrictly());
        }
        roleService.checkRoleAllowed(role);
        roleService.checkRoleDataScope(role.getRoleId());
        if (!roleService.checkRoleNameUnique(role))
        {
            return error("修改角色'" + role.getRoleName() + "'失败，角色名称已存在");
        }
        else if (!roleService.checkRoleKeyUnique(role))
        {
            return error("修改角色'" + role.getRoleName() + "'失败，角色权限已存在");
        }
        role.setUpdateBy(getUsername());

        if (roleService.updateRole(role) > 0)
        {
            // 更新缓存用户权限
            LoginUser loginUser = getLoginUser();
            if (StringUtils.isNotNull(loginUser.getUser()) && !loginUser.getUser().isAdmin())
            {
                loginUser.setUser(userService.selectUserByUserName(loginUser.getUser().getUserName()));
                loginUser.setPermissions(permissionService.getMenuPermission(loginUser.getUser()));
                tokenService.setLoginUser(loginUser);
            }
            return success();
        }
        return error("修改角色'" + role.getRoleName() + "'失败，请联系管理员");
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 使用独立数据权限保存范围和部门关联，不允许改角色基础字段
     * @param role 角色参数
     * @return
     * 返回值: AjaxResult
     * 返回值说明：修改保存数据权限
     */
    @RequiresPermissions(RolePermissionConstants.DATA_SCOPE)
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @PutMapping("/dataScope")
    public AjaxResult dataScope(@RequestBody SysRole role)
    {
        roleService.checkRoleAllowed(role);
        roleService.checkRoleDataScope(role.getRoleId());
        role.setUpdateBy(getUsername());
        return toAjax(roleService.authDataScope(role));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 状态修改
     * @param role 角色参数
     * @return
     * 返回值: AjaxResult
     * 返回值说明：状态修改
     */
    @RequiresPermissions("system:role:edit")
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestBody SysRole role)
    {
        roleService.checkRoleAllowed(role);
        roleService.checkRoleDataScope(role.getRoleId());
        role.setUpdateBy(getUsername());
        return toAjax(roleService.updateRoleStatus(role));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 删除角色
     * @param roleIds 角色ID集合参数
     * @return
     * 返回值: AjaxResult
     * 返回值说明：删除角色
     */
    @RequiresPermissions("system:role:remove")
    @Log(title = "角色管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{roleIds}")
    public AjaxResult remove(@PathVariable Long[] roleIds)
    {
        return toAjax(roleService.deleteRoleByIds(roleIds));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取角色选择框列表
     * @return
     * 返回值: AjaxResult
     * 返回值说明：获取角色选择框列表
     */
    @RequiresPermissions("system:role:query")
    @GetMapping("/optionselect")
    public AjaxResult optionselect()
    {
        return success(roleService.selectRoleAll());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 查询已分配用户角色列表，要求独立分配用户权限并校验角色数据范围
     * @param user 用户参数
     * @return
     * 返回值: TableDataInfo
     * 返回值说明：查询已分配用户角色列表
     */
    @RequiresPermissions(RolePermissionConstants.AUTH_USER)
    @GetMapping("/authUser/allocatedList")
    public TableDataInfo allocatedList(SysUser user)
    {
        roleService.checkRoleDataScope(user.getRoleId());
        startPage();
        List<SysUser> list = userService.selectAllocatedList(user);
        return getDataTable(list);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 查询未分配用户角色列表，要求独立分配用户权限并校验角色数据范围
     * @param user 用户参数
     * @return
     * 返回值: TableDataInfo
     * 返回值说明：查询未分配用户角色列表
     */
    @RequiresPermissions(RolePermissionConstants.AUTH_USER)
    @GetMapping("/authUser/unallocatedList")
    public TableDataInfo unallocatedList(SysUser user)
    {
        roleService.checkRoleDataScope(user.getRoleId());
        startPage();
        List<SysUser> list = userService.selectUnallocatedList(user);
        return getDataTable(list);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 取消授权用户，要求独立分配用户权限并校验角色数据范围
     * @param userRole 用户角色参数
     * @return
     * 返回值: AjaxResult
     * 返回值说明：取消授权用户
     */
    @RequiresPermissions(RolePermissionConstants.AUTH_USER)
    @Log(title = "角色管理", businessType = BusinessType.GRANT)
    @PutMapping("/authUser/cancel")
    public AjaxResult cancelAuthUser(@RequestBody SysUserRole userRole)
    {
        roleService.checkRoleDataScope(userRole.getRoleId());
        return toAjax(roleService.deleteAuthUser(userRole));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 批量取消授权用户，要求独立分配用户权限并校验角色数据范围
     * @param roleId 角色ID参数
     * @param userIds 用户ID集合参数
     * @return
     * 返回值: AjaxResult
     * 返回值说明：批量取消授权用户
     */
    @RequiresPermissions(RolePermissionConstants.AUTH_USER)
    @Log(title = "角色管理", businessType = BusinessType.GRANT)
    @PutMapping("/authUser/cancelAll")
    public AjaxResult cancelAuthUserAll(Long roleId, Long[] userIds)
    {
        roleService.checkRoleDataScope(roleId);
        return toAjax(roleService.deleteAuthUsers(roleId, userIds));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 批量选择用户授权，要求独立分配用户权限并校验角色数据范围
     * @param roleId 角色ID参数
     * @param userIds 用户ID集合参数
     * @return
     * 返回值: AjaxResult
     * 返回值说明：批量选择用户授权
     */
    @RequiresPermissions(RolePermissionConstants.AUTH_USER)
    @Log(title = "角色管理", businessType = BusinessType.GRANT)
    @PutMapping("/authUser/selectAll")
    public AjaxResult selectAuthUserAll(Long roleId, Long[] userIds)
    {
        roleService.checkRoleDataScope(roleId);
        return toAjax(roleService.insertAuthUsers(roleId, userIds));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 使用独立数据权限读取角色部门树及勾选项，仍校验角色数据范围
     * @param roleId 角色ID参数
     * @return
     * 返回值: AjaxResult
     * 返回值说明：获取对应角色部门树列表
     */
    @RequiresPermissions(RolePermissionConstants.DATA_SCOPE)
    @GetMapping(value = "/deptTree/{roleId}")
    public AjaxResult deptTree(@PathVariable("roleId") Long roleId)
    {
        roleService.checkRoleDataScope(roleId);
        AjaxResult ajax = AjaxResult.success();
        ajax.put("checkedKeys", deptService.selectDeptListByRoleId(roleId));
        ajax.put("depts", deptService.selectDeptTreeList(new SysDept()));
        return ajax;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 16:37
     * 说明: 新增角色时读取当前账号可见的部门树，复用新增角色权限，不依赖用户管理或角色查询权限
     * @return
     * 返回值: AjaxResult
     * 返回值说明: data中的部门树，不含已有角色的勾选关系，不写入任何授权
     */
    @RequiresPermissions("system:role:add")
    @GetMapping("/deptTree")
    public AjaxResult creationDeptTree()
    {
        return success(deptService.selectDeptTreeList(new SysDept()));
    }
}
