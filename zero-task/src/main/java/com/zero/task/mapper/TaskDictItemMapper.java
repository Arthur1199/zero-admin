package com.zero.task.mapper;

import com.zero.task.domain.DictItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/19 15:56
 * 说明: 为任务服务读取业务字典，查询同时检查字典组和字典项的启用及删除状态。
 */
public interface TaskDictItemMapper {

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 15:56
     * 说明: 按字典组编码和项编码联查有效字典项，按业务库的编码匹配规则执行。
     * @param groupCode 字典组编码
     * @param itemCode 字典项编码
     * @return 匹配的字典项，无有效记录时返回 null
     * 返回值: DictItem
     * 返回值说明: 任务使用的完整业务字典项
     */
    DictItem selectActiveByCodes(@Param("groupCode") String groupCode, @Param("itemCode") String itemCode);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 15:56
     * 说明: 读取指定分组中的有效字典项。
     * @param groupCode 字典组编码
     * @return 按排序字段和主键升序排列的字典项列表
     * 返回值: List<DictItem>
     * 返回值说明: 无有效字典项时返回空集合
     */
    List<DictItem> selectActiveByGroupCode(@Param("groupCode") String groupCode);
}
