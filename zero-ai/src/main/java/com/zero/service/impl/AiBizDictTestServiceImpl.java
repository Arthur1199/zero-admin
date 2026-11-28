package com.zero.service.impl;

import com.zero.base.AiBaseService;
import com.zero.domain.DictItem;
import com.zero.mapper.AiDictItemMapper;
import com.zero.service.AiBizDictTestService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/19 18:59
 * 说明: 开发环境验证任务字典查询及 Redis 缓存。
 */
@Service
public class AiBizDictTestServiceImpl extends AiBaseService<AiDictItemMapper, DictItem> implements AiBizDictTestService{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 查询调试入口指定的有效业务字典项。
     * @param dictKey 以点分隔的分组编码和字典项编码
     * @return
     * 返回值: DictItem
     * 返回值说明: 符合条件的字典项，查不到时返回 null
     */
    @Override
    public DictItem getActiveItem(String dictKey) {
        return getDictItem(dictKey);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 查询调试入口指定分组的有效业务字典项。
     * @param groupCode 业务字典分组编码
     * @return
     * 返回值: List<DictItem>
     * 返回值说明: 按字典排序返回记录，无记录时返回空列表
     */
    @Override
    public List<DictItem> getActiveItems(String groupCode) {
        return getDictItems(groupCode);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 读取调试入口指定字典项的原始配置值。
     * @param dictKey 以点分隔的分组编码和字典项编码
     * @return
     * 返回值: String
     * 返回值说明: 字典值，查不到时返回 null
     */
    @Override
    public String findBizDictValue(String dictKey) {
        return getDictValue(dictKey);
    }
}
