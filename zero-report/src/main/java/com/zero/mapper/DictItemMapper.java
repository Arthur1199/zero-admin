package com.zero.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zero.domain.DictItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 访问业务字典明细表，提供管理查询和有效字典项读取。
 * @since 2026/06/28 04:06
 */
public interface DictItemMapper extends BaseMapper<DictItem> {

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 14:34
     * 说明: 通过分组及项编码的唯一索引查找有效字典项，供内部业务读取。
     * @param groupCode 业务字典分组编码
     * @param itemCode 业务字典项编码，在当前分组内唯一
     * @return
     * 返回值: DictItem
     * 返回值说明: 启用且未删除的字典项，查不到时返回 null
     */
    DictItem selectActiveByCodes(@Param("groupCode") String groupCode, @Param("itemCode") String itemCode);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 14:41
     * 说明: 按业务字典分组编码查询分组内全部有效项，供内部业务读取。
     * @param groupCode 业务字典分组编码
     * @return
     * 返回值: List<DictItem>
     * 返回值说明: 启用且未删除的字典项，按排序值和主键升序
     */
    List<DictItem> selectActiveByGroupCode(@Param("groupCode") String groupCode);
}
