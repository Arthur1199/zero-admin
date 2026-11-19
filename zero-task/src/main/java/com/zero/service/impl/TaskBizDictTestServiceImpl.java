package com.zero.service.impl;

import com.zero.base.TaskBaseService;
import com.zero.domain.DictItem;
import com.zero.mapper.TaskDictItemMapper;
import com.zero.service.TaskBizDictTestService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/19 18:59
 * 说明: 开发环境验证任务字典查询及 Redis 缓存。
 */
@Service
public class TaskBizDictTestServiceImpl extends TaskBaseService<TaskDictItemMapper, DictItem> implements TaskBizDictTestService{
    @Override
    public DictItem getActiveItem(String groupCode, String itemCode) {
        return getBizDictItem(groupCode + "." + itemCode);
    }

    @Override
    public List<DictItem> getActiveItems(String groupCode) {
        return getBizDictItems(groupCode);
    }

    @Override
    public String findBizDictValue(String dictKey) {
        return getBizDictValue(dictKey);
    }
}
