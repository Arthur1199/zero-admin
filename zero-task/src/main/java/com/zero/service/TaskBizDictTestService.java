package com.zero.service;

import com.zero.domain.DictItem;

import java.util.List;
import java.util.Map;

/**
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/19 18:57
 * 说明: 开发环境字典查询及缓存验证接口。
 */
public interface TaskBizDictTestService {

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 19:20
     * 说明: 查询启用且未删除的字典项。
     * @param dictKey 字典分组.字典项
     * @return 字典项，无有效记录时为空
     * 返回值: DictItem
     * 返回值说明: 匹配的有效字典项
     */
    DictItem getActiveItem(String dictKey);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 19:20
     * 说明: 按分组查询有效字典项。
     * @param groupCode 字典分组编码
     * @return 分组下的有效字典项，无记录时为空列表
     * 返回值: List<DictItem>
     * 返回值说明: 按排序值和主键升序排列
     */
    List<DictItem> getActiveItems(String groupCode);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 19:41
     * @param dictKey
     * @return
     * 返回值:String
     * 返回值说明：
     */
     String findBizDictValue(String dictKey);
}
