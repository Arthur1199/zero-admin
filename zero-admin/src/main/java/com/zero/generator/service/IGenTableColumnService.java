package com.zero.generator.service;

import java.util.List;
import com.zero.generator.domain.GenTableColumn;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 业务字段 服务层
 */
public interface IGenTableColumnService
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 查询业务字段列表
     * @param tableId 业务字段编号
     * @return
     * 返回值: 业务字段集合
     * 返回值说明：查询业务字段列表
     */
    public List<GenTableColumn> selectGenTableColumnListByTableId(Long tableId);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 新增业务字段
     * @param genTableColumn 业务字段信息
     * @return
     * 返回值: 结果
     * 返回值说明：新增业务字段
     */
    public int insertGenTableColumn(GenTableColumn genTableColumn);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 修改业务字段
     * @param genTableColumn 业务字段信息
     * @return
     * 返回值: 结果
     * 返回值说明：修改业务字段
     */
    public int updateGenTableColumn(GenTableColumn genTableColumn);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 删除业务字段信息
     * @param ids 需要删除的数据ID
     * @return
     * 返回值: 结果
     * 返回值说明：删除业务字段信息
     */
    public int deleteGenTableColumnByIds(String ids);
}
