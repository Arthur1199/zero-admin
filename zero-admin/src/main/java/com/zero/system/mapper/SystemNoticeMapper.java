package com.zero.system.mapper;
import java.util.List;
import java.util.Collection;
import org.apache.ibatis.annotations.Param;
import com.zero.system.domain.SystemNoticeUser;
import com.zero.system.domain.SystemNoticeRequest;
/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/17
 * 说明: 系统通知专用账号查询，发送权限明确包含全员通知能力，不套用操作人数据范围
 */

public interface SystemNoticeMapper {
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 分批匹配在线账号，部门和角色使用最新关系，角色EXISTS避免重复行
     * @param onlineIds 当前批次在线账号ID，最多500个，空集合禁止查全表
     * @param filter 可选目标筛选
     * @param keyword 可选账号或昵称关键字
     * @return
     * 返回值: List<SystemNoticeUser>
     * 返回值说明: 正常且未删除的账号最小字段集合，按用户ID排序
     */

    List<SystemNoticeUser> selectTargets(@Param("onlineIds") Collection<Long> onlineIds,
        @Param("filter") SystemNoticeRequest filter, @Param("keyword") String keyword);
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 读取正常部门最小字段，不套用数据范围或读取联系方式
     * @return
     * 返回值: List<SysDept>
     * 返回值说明: 正常且未删除部门，稳定排序
     */
    List<com.zero.common.core.domain.entity.SysDept> selectDepartments();
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 直接读取正常角色，不关联全量用户表或读取权限明细
     * @return
     * 返回值: List<SysRole>
     * 返回值说明: 正常且未删除角色，稳定排序
     */
    List<com.zero.common.core.domain.entity.SysRole> selectRoles();
}

