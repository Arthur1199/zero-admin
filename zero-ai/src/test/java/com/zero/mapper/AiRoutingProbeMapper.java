package com.zero.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zero.domain.DictItem;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/19 14:08
 * 说明: 测试专用 Mapper，对两个隔离数据源执行同一条分页 SQL。
 */
public interface AiRoutingProbeMapper extends BaseMapper<DictItem> {

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 14:08
     * 说明: 通过 XML 加载测试数据，返回当前数据源的来源标志。
     * @param page 要读取的页码和单页条数
     * @return
     * 返回值: IPage<String>
     * 返回值说明: 仅包含隔离测试数据
     */
    IPage<String> selectOrigin(Page<String> page);
}
