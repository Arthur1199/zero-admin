package com.zero.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zero.base.ReportBaseService;
import com.zero.domain.DictGroup;
import com.zero.mapper.DictGroupMapper;
import com.zero.queryDto.DictGroupQuery;
import com.zero.service.IDictGroupService;
import com.zero.util.DictConfigUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/6/28 04:34
 * 说明: 业务字典组服务实现，负责 biz_dict_group 的分页查询、唯一性校验、审计字段维护、软删除和恢复控制
 */
@Slf4j
@Service
public class DictGroupServiceImpl extends ReportBaseService<DictGroupMapper, DictGroup> implements IDictGroupService
{
    /**
     * 正常状态：1
     */
    private static final String STATUS_NORMAL = "1";

    /**
     * 未删除标志：0
     */
    private static final String DEL_FLAG_NORMAL = "0";

    /**
     * 已删除标志：1
     */
    private static final String DEL_FLAG_DELETE = "1";

    /** 写入成功后统一刷新业务字典查询缓存。 */
    @Autowired
    private DictConfigUtil dictConfigUtil;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 04:34
     * 说明: 分页查询业务字典组，包含未删除和已删除记录，支持编码、名称、状态、删除标志和创建时间筛选
     * @param query 查询条件和分页参数
     * @return
     * 返回值: IPage<DictGroup>
     * 返回值说明: 分页后的业务字典组列表，delFlag用于页面区分正常和已删除数据
     */
    @Override
    public IPage<DictGroup> selectGroupPage(DictGroupQuery query)
    {
        DictGroupQuery safeQuery = query == null ? new DictGroupQuery() : query;
        LambdaQueryWrapper<DictGroup> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(safeQuery.getGroupCode()), DictGroup::getGroupCode,
                        trim(safeQuery.getGroupCode()))
                .like(StringUtils.hasText(safeQuery.getGroupName()), DictGroup::getGroupName,
                        trim(safeQuery.getGroupName()))
                .eq(StringUtils.hasText(safeQuery.getStatus()), DictGroup::getStatus, trim(safeQuery.getStatus()))
                .eq(StringUtils.hasText(safeQuery.getDelFlag()), DictGroup::getDelFlag, trim(safeQuery.getDelFlag()))
                .ge(safeQuery.hasBeginTime(), DictGroup::getCreateTime, safeQuery.getBeginDateTime())
                .lt(safeQuery.hasEndTime(), DictGroup::getCreateTime, safeQuery.getEndExclusiveDateTime())
                .orderByDesc(DictGroup::getCreateTime)
                .orderByAsc(DictGroup::getId);
        log.info("查询数据字典分组分页数据成功!");
        return page(safeQuery.toPage(), wrapper);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 09:36
     * 说明: 查询字典组下拉列表，不按状态和删除标志过滤，避免查询区无法选择已停用或已删除字典组
     * @return
     * 返回值: List<DictGroup>
     * 返回值说明: 按字典组编码升序排列的全量下拉数据
     */
    @Override
    public List<DictGroup> selectGroupOptions()
    {
        return lambdaQuery().select(DictGroup::getId, DictGroup::getGroupCode, DictGroup::getGroupName)
                .orderByAsc(DictGroup::getGroupCode)
                .list();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 04:34
     * 说明: 根据ID查询未删除的业务字典组详情
     * @param id 字典组ID
     * @return
     * 返回值: DictGroup
     * 返回值说明: 未删除的业务字典组；不存在时返回null
     */
    @Override
    public DictGroup selectGroupById(Long id)
    {
        if (id == null)
        {
            return null;
        }
        return lambdaQuery().eq(DictGroup::getId, id)
                .eq(DictGroup::getDelFlag, DEL_FLAG_NORMAL)
                .one();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 15:28
     * 说明: 新增业务字典组，写入前校验编码唯一并补齐默认状态、删除标志和创建审计字段。写入成功后使业务字典查询缓存失效。
     * @param dictGroup 字典组信息
     * @return
     * 返回值: boolean
     * 返回值说明: true-新增成功，false-新增失败
     */
    @Override
    public boolean insertGroup(DictGroup dictGroup)
    {
        prepareGroup(dictGroup);
        validateGroupCodeUnique(dictGroup);
        dictGroup.setId(null);
        dictGroup.setDelFlag(DEL_FLAG_NORMAL);
        if (!StringUtils.hasText(dictGroup.getStatus()))
        {
            dictGroup.setStatus(STATUS_NORMAL);
        }
        dictGroup.setCreateBy(getAdminName());
        dictGroup.setCreateTime(new Date());
        return dictConfigUtil.invalidateGroupsAfterWrite(save(dictGroup), List.of(dictGroup.getGroupCode()));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 15:28
     * 说明: 修改业务字典组，写入前校验记录存在和编码唯一并补齐更新审计字段。写入成功后使业务字典查询缓存失效。
     * @param dictGroup 字典组信息
     * @return
     * 返回值: boolean
     * 返回值说明: true-修改成功，false-修改失败
     */
    @Override
    public boolean updateGroup(DictGroup dictGroup)
    {
        if (dictGroup == null || dictGroup.getId() == null)
        {
            throw new IllegalArgumentException("字典组ID不能为空");
        }
        DictGroup oldGroup = selectGroupById(dictGroup.getId());
        if (oldGroup == null)
        {
            throw new IllegalArgumentException("字典组不存在或已删除");
        }
        prepareGroup(dictGroup);
        validateGroupCodeUnique(dictGroup);
        dictGroup.setDelFlag(DEL_FLAG_NORMAL);
        dictGroup.setUpdateBy(getAdminName());
        dictGroup.setUpdateTime(new Date());
        return dictConfigUtil.invalidateGroupsAfterWrite(updateById(dictGroup),
                List.of(oldGroup.getGroupCode(), dictGroup.getGroupCode()));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 15:28
     * 说明: 批量软删除业务字典组，不再要求先删除字典项；删除后仍可在页面查看并恢复。写入成功后使业务字典查询缓存失效。
     * @param ids 字典组ID集合
     * @return
     * 返回值: boolean
     * 返回值说明: true-删除成功，false-删除失败
     */
    @Override
    public boolean deleteGroupByIds(Long[] ids)
    {
        List<Long> idList = parseIds(ids, "请选择要删除的字典组");
        List<String> groupCodes = affectedGroupCodes(idList, DEL_FLAG_NORMAL);
        return dictConfigUtil.invalidateGroupsAfterWrite(lambdaUpdate().in(DictGroup::getId, idList)
                .eq(DictGroup::getDelFlag, DEL_FLAG_NORMAL)
                .set(DictGroup::getDelFlag, DEL_FLAG_DELETE)
                .set(DictGroup::getUpdateBy, getAdminName())
                .set(DictGroup::getUpdateTime, new Date())
                .update(), groupCodes);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 15:28
     * 说明: 批量恢复已软删除的业务字典组，恢复后允许继续维护该组下的字典项。写入成功后使业务字典查询缓存失效。
     * @param ids 字典组ID集合
     * @return
     * 返回值: boolean
     * 返回值说明: true-恢复成功，false-恢复失败
     */
    @Override
    public boolean restoreGroupByIds(Long[] ids)
    {
        List<Long> idList = parseIds(ids, "请选择要恢复的字典组");
        List<String> groupCodes = affectedGroupCodes(idList, DEL_FLAG_DELETE);
        return dictConfigUtil.invalidateGroupsAfterWrite(lambdaUpdate().in(DictGroup::getId, idList)
                .eq(DictGroup::getDelFlag, DEL_FLAG_DELETE)
                .set(DictGroup::getDelFlag, DEL_FLAG_NORMAL)
                .set(DictGroup::getUpdateBy, getAdminName())
                .set(DictGroup::getUpdateTime, new Date())
                .update(), groupCodes);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 19:35
     * 说明: 按批量操作的条件获取受影响分组的缓存编码。
     * @param ids 字典组主键
     * @param delFlag 操作前的删除标志
     * @return 受影响的分组编码
     * 返回值: List<String>
     * 返回值说明: 没有匹配记录时为空列表
     */
    private List<String> affectedGroupCodes(List<Long> ids, String delFlag)
    {
        return lambdaQuery().select(DictGroup::getGroupCode).in(DictGroup::getId, ids)
                .eq(DictGroup::getDelFlag, delFlag).list().stream().map(DictGroup::getGroupCode).toList();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 04:34
     * 说明: 清理字典组输入，避免空白编码、非法状态和空删除标志进入数据库
     * @param dictGroup 字典组信息
     * @return
     * 返回值: void
     * 返回值说明: 原对象字段被规范化
     */
    private void prepareGroup(DictGroup dictGroup)
    {
        if (dictGroup == null)
        {
            throw new IllegalArgumentException("字典组信息不能为空");
        }
        dictGroup.setGroupCode(requiredTrim(dictGroup.getGroupCode(), "字典组编码不能为空"));
        dictGroup.setGroupName(requiredTrim(dictGroup.getGroupName(), "字典组名称不能为空"));
        dictGroup.setStatus(StringUtils.hasText(dictGroup.getStatus()) ? trim(dictGroup.getStatus()) : STATUS_NORMAL);
        dictGroup.setRemark(trim(dictGroup.getRemark()));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 04:34
     * 说明: 校验字典组编码全表唯一；数据库唯一索引不包含删除标志，因此历史删除记录也会占用编码
     * @param dictGroup 字典组信息
     * @return
     * 返回值: void
     * 返回值说明: 编码重复时抛出业务异常
     */
    private void validateGroupCodeUnique(DictGroup dictGroup)
    {
        Long count = lambdaQuery().eq(DictGroup::getGroupCode, dictGroup.getGroupCode())
                .ne(dictGroup.getId() != null, DictGroup::getId, dictGroup.getId())
                .count();
        if (count != null && count > 0)
        {
            throw new IllegalArgumentException("字典组编码已存在");
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 04:34
     * 说明: 解析批量ID参数，过滤空ID并阻断空删除请求
     * @param ids 原始ID数组
     * @param emptyMessage 空集合提示
     * @return
     * 返回值: List<Long>
     * 返回值说明: 已过滤空值的ID集合
     */
    private List<Long> parseIds(Long[] ids, String emptyMessage)
    {
        if (ids == null || ids.length == 0)
        {
            throw new IllegalArgumentException(emptyMessage);
        }
        List<Long> idList = Arrays.stream(ids).filter(id -> id != null && id > 0).toList();
        if (idList.isEmpty())
        {
            throw new IllegalArgumentException(emptyMessage);
        }
        return idList;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 04:34
     * 说明: 去除字符串首尾空白，空值保持为空，避免把 null 转成文本
     * @param value 原始字符串
     * @return
     * 返回值: String
     * 返回值说明: 规范化后的字符串
     */
    private String trim(String value)
    {
        return value == null ? null : value.trim();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 04:34
     * 说明: 必填字符串规范化，空白值直接作为业务异常返回前端
     * @param value 原始字符串
     * @param message 空值提示
     * @return
     * 返回值: String
     * 返回值说明: 去除首尾空白后的非空字符串
     */
    private String requiredTrim(String value, String message)
    {
        if (!StringUtils.hasText(value))
        {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

}
