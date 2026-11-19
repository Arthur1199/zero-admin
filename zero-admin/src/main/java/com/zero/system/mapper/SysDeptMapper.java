package com.zero.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.zero.common.core.domain.entity.SysDept;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 部门管理 数据层
 */
public interface SysDeptMapper
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 查询部门管理数据
     * @param dept 部门信息
     * @return
     * 返回值: 部门信息集合
     * 返回值说明：查询部门管理数据
     */
    public List<SysDept> selectDeptList(SysDept dept);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 根据角色ID查询部门树信息
     * @param roleId 角色ID
     * @param deptCheckStrictly 部门树选择项是否关联显示
     * @return
     * 返回值: 选中部门列表
     * 返回值说明：根据角色ID查询部门树信息
     */
    public List<Long> selectDeptListByRoleId(@Param("roleId") Long roleId, @Param("deptCheckStrictly") boolean deptCheckStrictly);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 根据部门ID查询信息
     * @param deptId 部门ID
     * @return
     * 返回值: 部门信息
     * 返回值说明：根据部门ID查询信息
     */
    public SysDept selectDeptById(Long deptId);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 根据ID查询所有子部门
     * @param deptId 部门ID
     * @return
     * 返回值: 部门列表
     * 返回值说明：根据ID查询所有子部门
     */
    public List<SysDept> selectChildrenDeptById(Long deptId);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 根据ID查询所有子部门（正常状态）
     * @param deptId 部门ID
     * @return
     * 返回值: 子部门数
     * 返回值说明：根据ID查询所有子部门（正常状态）
     */
    public int selectNormalChildrenDeptById(Long deptId);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 是否存在子节点
     * @param deptId 部门ID
     * @return
     * 返回值: int
     * 返回值说明：是否存在子节点
     */
    public int hasChildByDeptId(Long deptId);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 查询部门是否存在用户
     * @param deptId 部门ID
     * @return
     * 返回值: int
     * 返回值说明：查询部门是否存在用户
     */
    public int checkDeptExistUser(Long deptId);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 校验部门名称是否唯一
     * @param deptName 部门名称
     * @param parentId 父部门ID
     * @return
     * 返回值: SysDept
     * 返回值说明：校验部门名称是否唯一
     */
    public SysDept checkDeptNameUnique(@Param("deptName") String deptName, @Param("parentId") Long parentId);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 新增部门信息
     * @param dept 部门信息
     * @return
     * 返回值: int
     * 返回值说明：新增部门信息
     */
    public int insertDept(SysDept dept);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 修改部门信息
     * @param dept 部门信息
     * @return
     * 返回值: int
     * 返回值说明：修改部门信息
     */
    public int updateDept(SysDept dept);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 修改所在部门正常状态
     * @param deptIds 部门ID组
     * @return
     * 返回值: void
     * 返回值说明：修改所在部门正常状态
     */
    public void updateDeptStatusNormal(Long[] deptIds);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 修改子元素关系
     * @param depts 子元素
     * @return
     * 返回值: int
     * 返回值说明：修改子元素关系
     */
    public int updateDeptChildren(@Param("depts") List<SysDept> depts);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 删除部门管理信息
     * @param deptId 部门ID
     * @return
     * 返回值: int
     * 返回值说明：删除部门管理信息
     */
    public int deleteDeptById(Long deptId);
}
