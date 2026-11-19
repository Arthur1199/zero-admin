package com.zero.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zero.base.ReportBaseService;
import com.zero.domain.DictGroup;
import com.zero.domain.DictItem;
import com.zero.queryDto.DictItemQuery;
import com.zero.mapper.DictGroupMapper;
import com.zero.mapper.DictItemMapper;
import com.zero.service.BizDictLookup;
import com.zero.service.IDictItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/6/28 04:35
 * 说明: 业务字典项服务实现，负责 biz_dict_item 的分页查询、组内唯一性校验、审计字段维护、软删除和恢复控制
 */
@Service
public class DictItemServiceImpl extends ReportBaseService<DictItemMapper, DictItem> implements IDictItemService
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
    private BizDictLookup bizDictLookup;

    /**
     * 字典组数据访问，用于校验字典项所属分组是否存在且未删除
     */
    @Autowired
    private DictGroupMapper dictGroupMapper;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 04:35
     * 说明: 分页查询业务字典项，包含未删除和已删除记录，支持按字典组、编码、名称、值和状态筛选
     * @param query 查询条件和分页参数
     * @return
     * 返回值: IPage<DictItem>
     * 返回值说明: 分页后的业务字典项列表，delFlag用于页面区分正常和已删除数据
     */
    @Override
    public IPage<DictItem> selectItemPage(DictItemQuery query)
    {
        DictItemQuery safeQuery = query == null ? new DictItemQuery() : query;
        LambdaQueryWrapper<DictItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(safeQuery.getGroupId() != null, DictItem::getGroupId, safeQuery.getGroupId())
                .like(StringUtils.hasText(safeQuery.getItemCode()), DictItem::getItemCode,
                        trim(safeQuery.getItemCode()))
                .like(StringUtils.hasText(safeQuery.getItemName()), DictItem::getItemName,
                        trim(safeQuery.getItemName()))
                .like(StringUtils.hasText(safeQuery.getItemValue()), DictItem::getItemValue,
                        trim(safeQuery.getItemValue()))
                .eq(StringUtils.hasText(safeQuery.getStatus()), DictItem::getStatus, trim(safeQuery.getStatus()))
                .orderByAsc(DictItem::getItemSort)
                .orderByAsc(DictItem::getId);
        return page(safeQuery.toPage(), wrapper);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 04:35
     * 说明: 根据ID查询未删除的业务字典项详情
     * @param id 字典项ID
     * @return
     * 返回值: DictItem
     * 返回值说明: 未删除的业务字典项；不存在时返回null
     */
    @Override
    public DictItem selectItemById(Long id)
    {
        if (id == null)
        {
            return null;
        }
        return lambdaQuery().eq(DictItem::getId, id)
                .eq(DictItem::getDelFlag, DEL_FLAG_NORMAL)
                .one();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 15:28
     * 说明: 新增业务字典项，写入前校验字典组存在、编码和值在组内唯一，并补齐审计字段。写入成功后使业务字典查询缓存失效。
     * @param dictItem 字典项信息
     * @return
     * 返回值: boolean
     * 返回值说明: true-新增成功，false-新增失败
     */
    @Override
    public boolean insertItem(DictItem dictItem)
    {
        prepareItem(dictItem);
        validateGroupExists(dictItem.getGroupId());
        validateItemUnique(dictItem);
        dictItem.setId(null);
        dictItem.setDelFlag(DEL_FLAG_NORMAL);
        if (!StringUtils.hasText(dictItem.getStatus()))
        {
            dictItem.setStatus(STATUS_NORMAL);
        }
        if (dictItem.getItemSort() == null)
        {
            dictItem.setItemSort(0);
        }
        dictItem.setCreateBy(getAdminName());
        dictItem.setCreateTime(new Date());
        return bizDictLookup.invalidateAfterWrite(save(dictItem));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 15:28
     * 说明: 修改业务字典项，写入前校验记录存在、字典组存在、编码和值在组内唯一，并补齐审计字段。写入成功后使业务字典查询缓存失效。
     * @param dictItem 字典项信息
     * @return
     * 返回值: boolean
     * 返回值说明: true-修改成功，false-修改失败
     */
    @Override
    public boolean updateItem(DictItem dictItem)
    {
        if (dictItem == null || dictItem.getId() == null)
        {
            throw new IllegalArgumentException("字典项ID不能为空");
        }
        DictItem oldItem = selectItemById(dictItem.getId());
        if (oldItem == null)
        {
            throw new IllegalArgumentException("字典项不存在或已删除");
        }
        prepareItem(dictItem);
        validateGroupExists(dictItem.getGroupId());
        validateItemUnique(dictItem);
        dictItem.setDelFlag(DEL_FLAG_NORMAL);
        dictItem.setUpdateBy(getAdminName());
        dictItem.setUpdateTime(new Date());
        return bizDictLookup.invalidateAfterWrite(updateById(dictItem));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 15:28
     * 说明: 批量软删除业务字典项，删除后仍可在业务参数设置页面查看并恢复。写入成功后使业务字典查询缓存失效。
     * @param ids 字典项ID集合
     * @return
     * 返回值: boolean
     * 返回值说明: true-删除成功，false-删除失败
     */
    @Override
    public boolean deleteItemByIds(Long[] ids)
    {
        List<Long> idList = parseIds(ids, "请选择要删除的字典项");
        return bizDictLookup.invalidateAfterWrite(lambdaUpdate().in(DictItem::getId, idList)
                .eq(DictItem::getDelFlag, DEL_FLAG_NORMAL)
                .set(DictItem::getDelFlag, DEL_FLAG_DELETE)
                .set(DictItem::getUpdateBy, getAdminName())
                .set(DictItem::getUpdateTime, new Date())
                .update());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 15:28
     * 说明: 批量恢复已软删除的业务字典项，恢复前校验所属字典组已经恢复，避免明细挂到删除分组下。写入成功后使业务字典查询缓存失效。
     * @param ids 字典项ID集合
     * @return
     * 返回值: boolean
     * 返回值说明: true-恢复成功，false-恢复失败
     */
    @Override
    public boolean restoreItemByIds(Long[] ids)
    {
        List<Long> idList = parseIds(ids, "请选择要恢复的字典项");
        List<DictItem> itemList = lambdaQuery().in(DictItem::getId, idList)
                .eq(DictItem::getDelFlag, DEL_FLAG_DELETE)
                .list();
        if (itemList.isEmpty())
        {
            throw new IllegalArgumentException("字典项不存在或未删除");
        }
        List<Long> groupIds = itemList.stream()
                .map(DictItem::getGroupId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (groupIds.isEmpty())
        {
            throw new IllegalArgumentException("字典项所属字典组不能为空，无法恢复");
        }
        Long groupCount = dictGroupMapper.selectCount(new LambdaQueryWrapper<DictGroup>()
                .in(DictGroup::getId, groupIds)
                .eq(DictGroup::getDelFlag, DEL_FLAG_NORMAL));
        if (groupCount == null || groupCount != groupIds.size())
        {
            throw new IllegalArgumentException("所属字典组不存在或已删除，请先恢复字典组");
        }
        return bizDictLookup.invalidateAfterWrite(lambdaUpdate().in(DictItem::getId, idList)
                .eq(DictItem::getDelFlag, DEL_FLAG_DELETE)
                .set(DictItem::getDelFlag, DEL_FLAG_NORMAL)
                .set(DictItem::getUpdateBy, getAdminName())
                .set(DictItem::getUpdateTime, new Date())
                .update());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 04:35
     * 说明: 清理字典项输入，保证 item_code、item_name、item_value 和 group_id 都具备有效值
     * @param dictItem 字典项信息
     * @return
     * 返回值: void
     * 返回值说明: 原对象字段被规范化
     */
    private void prepareItem(DictItem dictItem)
    {
        if (dictItem == null)
        {
            throw new IllegalArgumentException("字典项信息不能为空");
        }
        if (dictItem.getGroupId() == null)
        {
            throw new IllegalArgumentException("字典组ID不能为空");
        }
        dictItem.setItemCode(requiredTrim(dictItem.getItemCode(), "字典编码不能为空"));
        dictItem.setItemName(requiredTrim(dictItem.getItemName(), "字典项名称不能为空"));
        dictItem.setItemValue(requiredTrim(dictItem.getItemValue(), "字典值不能为空"));
        dictItem.setExtraJson(trim(dictItem.getExtraJson()));
        dictItem.setStatus(StringUtils.hasText(dictItem.getStatus()) ? trim(dictItem.getStatus()) : STATUS_NORMAL);
        dictItem.setRemark(trim(dictItem.getRemark()));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 04:35
     * 说明: 校验字典项所属分组存在且未删除，避免明细挂到无效分组
     * @param groupId 字典组ID
     * @return
     * 返回值: void
     * 返回值说明: 分组不存在时抛出业务异常
     */
    private void validateGroupExists(Long groupId)
    {
        Long count = dictGroupMapper.selectCount(new LambdaQueryWrapper<DictGroup>()
                .eq(DictGroup::getId, groupId)
                .eq(DictGroup::getDelFlag, DEL_FLAG_NORMAL));
        if (count == null || count == 0)
        {
            throw new IllegalArgumentException("字典组不存在或已删除");
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 04:35
     * 说明: 校验同一字典组下字典编码和值全表唯一；数据库唯一索引不包含删除标志，因此历史删除记录也会占用编码和值
     * @param dictItem 字典项信息
     * @return
     * 返回值: void
     * 返回值说明: 编码或值重复时抛出业务异常
     */
    private void validateItemUnique(DictItem dictItem)
    {
        Long codeCount = lambdaQuery().eq(DictItem::getGroupId, dictItem.getGroupId())
                .eq(DictItem::getItemCode, dictItem.getItemCode())
                .ne(dictItem.getId() != null, DictItem::getId, dictItem.getId())
                .count();
        if (codeCount != null && codeCount > 0)
        {
            throw new IllegalArgumentException("同一字典组下字典编码已存在");
        }
        Long valueCount = lambdaQuery().eq(DictItem::getGroupId, dictItem.getGroupId())
                .eq(DictItem::getItemValue, dictItem.getItemValue())
                .ne(dictItem.getId() != null, DictItem::getId, dictItem.getId())
                .count();
        if (valueCount != null && valueCount > 0)
        {
            throw new IllegalArgumentException("同一字典组下字典值已存在");
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 04:35
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
     * 时间: 2026/6/28 04:35
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
     * 时间: 2026/6/28 04:35
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
