package com.zero.generator.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.zero.common.core.text.Convert;
import com.zero.generator.domain.GenTableColumn;
import com.zero.generator.mapper.GenTableColumnMapper;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 业务字段 服务层实现
 */
@Service
public class GenTableColumnServiceImpl implements IGenTableColumnService
{
	/**
	 * 代码生成表字段数据访问字段
	 */
	@Autowired
	private GenTableColumnMapper genTableColumnMapper;

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
	@Override
	public List<GenTableColumn> selectGenTableColumnListByTableId(Long tableId)
	{
	    return genTableColumnMapper.selectGenTableColumnListByTableId(tableId);
	}

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
	@Override
	public int insertGenTableColumn(GenTableColumn genTableColumn)
	{
	    return genTableColumnMapper.insertGenTableColumn(genTableColumn);
	}

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
	@Override
	public int updateGenTableColumn(GenTableColumn genTableColumn)
	{
	    return genTableColumnMapper.updateGenTableColumn(genTableColumn);
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/14 00:58
	 * 说明: 删除业务字段对象
	 * @param ids 需要删除的数据ID
	 * @return
	 * 返回值: 结果
	 * 返回值说明：删除业务字段对象
	 */
	@Override
	public int deleteGenTableColumnByIds(String ids)
	{
		return genTableColumnMapper.deleteGenTableColumnByIds(Convert.toLongArray(ids));
	}
}
