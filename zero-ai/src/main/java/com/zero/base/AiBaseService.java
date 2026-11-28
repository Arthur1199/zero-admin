package com.zero.base;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zero.domain.DictItem;
import com.zero.util.DictConfigUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.util.List;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/6/28 02:05
 * 说明: 任务服务基础父类，为子类提供已启用业务字典的单项、分组和字典值查询。
 */
public abstract class AiBaseService<M extends BaseMapper<T>, T> extends ServiceImpl<M, T> {

    /** 业务数据源启用后装配，兼容只运行不依赖数据库的任务服务。 */
    @Autowired(required = false)
    private DictConfigUtil bizDictLookup;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 15:56
     * 说明: 按分组编码和字典项编码读取已启用、未删除的业务字典项。
     * @param dictKey 分组编码.字典项编码，允许去除两段周围的空白
     * @return
     * 返回值: DictItem
     * 返回值说明: 完整字典项，格式非法或无有效记录时返回 null
     */
    protected DictItem getDictItem(String dictKey) {
        if (dictKey == null) {
            return null;
        }
        int separator = dictKey.indexOf('.');
        if (separator < 0 || separator != dictKey.lastIndexOf('.')) {
            return null;
        }
        String groupCode = dictKey.substring(0, separator).strip();
        String itemCode = dictKey.substring(separator + 1).strip();
        if (groupCode.isEmpty() || itemCode.isEmpty()) {
            return null;
        }
        return dictionaryLookup().findActiveItem(groupCode, itemCode);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 15:56
     * 说明: 查询启用且未删除的业务字典分组下所有有效字典项。
     * @param groupCode 字典分组编码，允许两侧有空白
     * @return
     * 返回值: List<DictItem>
     * 返回值说明: 按排序值和主键升序的字典项，分组编码为空或查无有效项时返回空集合
     */
    protected List<DictItem> getDictItems(String groupCode) {
        if (groupCode == null || groupCode.isBlank()) {
            return List.of();
        }
        return dictionaryLookup().findActiveItems(groupCode.strip());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 15:56
     * 说明: 获取有效业务字典项的原始字符串值。
     * @param dictKey 分组编码.字典项编码，允许去除两段周围的空白
     * @return
     * 返回值: String
     * 返回值说明: item_value 字段原值，格式非法或无有效记录时返回 null
     */
    protected String getDictValue(String dictKey) {
        DictItem item = getDictItem(dictKey);
        return item == null ? null : item.getItemValue();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 15:56
     * 说明: 验证字典查询所需的任务数据源已装配，避免把配置缺失当成字典无记录。
     * @return 已装配的字典查询服务
     * 返回值: DictConfigUtil
     * 返回值说明: 业务库的字典查询入口
     * @throws IllegalStateException 业务数据源未启用
     */
    private DictConfigUtil dictionaryLookup() {
        Assert.state(bizDictLookup != null, "查询业务字典需要启用 zero-ai 业务数据源");
        return bizDictLookup;
    }
}
