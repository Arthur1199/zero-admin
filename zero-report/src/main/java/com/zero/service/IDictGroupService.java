package com.zero.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zero.domain.DictGroup;
import com.zero.queryDto.DictGroupQuery;

import java.util.List;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/6/28 04:33
 * 说明: 业务字典组服务接口，封装 biz_dict_group 的分页查询、详情、新增、修改、软删除和恢复业务规则
 */
public interface IDictGroupService extends IService<DictGroup>
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 04:33
     * 说明: 分页查询业务字典组，包含未删除和已删除记录，支持编码、名称、状态、删除标志和创建时间筛选
     * @param query 查询条件和分页参数
     * @return
     * 返回值: IPage<DictGroup>
     * 返回值说明: 分页后的业务字典组列表，delFlag用于页面区分删除标志
     */
    IPage<DictGroup> selectGroupPage(DictGroupQuery query);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 09:36
     * 说明: 查询字典组下拉列表，不按状态和删除标志过滤，用于管理后台查询区本地过滤选择
     * @return
     * 返回值: List<DictGroup>
     * 返回值说明: 全量字典组下拉数据，仅包含下拉展示所需的ID、编码和名称
     */
    List<DictGroup> selectGroupOptions();

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 04:33
     * 说明: 根据ID查询业务字典组详情，用于管理后台修改弹窗回显
     * @param id 字典组ID
     * @return
     * 返回值: DictGroup
     * 返回值说明: 未删除的业务字典组；不存在时返回null
     */
    DictGroup selectGroupById(Long id);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 04:33
     * 说明: 新增业务字典组，写入前校验编码唯一并补齐默认状态、删除标志和创建审计字段
     * @param dictGroup 字典组信息
     * @return
     * 返回值: boolean
     * 返回值说明: true-新增成功，false-新增失败
     */
    boolean insertGroup(DictGroup dictGroup);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 04:33
     * 说明: 修改业务字典组，写入前校验记录存在和编码唯一并补齐更新审计字段
     * @param dictGroup 字典组信息
     * @return
     * 返回值: boolean
     * 返回值说明: true-修改成功，false-修改失败
     */
    boolean updateGroup(DictGroup dictGroup);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 04:33
     * 说明: 批量软删除业务字典组，不再要求先删除字典项；删除后仍可在页面查看并恢复
     * @param ids 字典组ID集合
     * @return
     * 返回值: boolean
     * 返回值说明: true-删除成功，false-删除失败
     */
    boolean deleteGroupByIds(Long[] ids);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 06:42
     * 说明: 批量恢复已软删除的业务字典组，恢复后可继续新增字典项和修改基础信息
     * @param ids 字典组ID集合
     * @return
     * 返回值: boolean
     * 返回值说明: true-恢复成功，false-恢复失败
     */
    boolean restoreGroupByIds(Long[] ids);
}
