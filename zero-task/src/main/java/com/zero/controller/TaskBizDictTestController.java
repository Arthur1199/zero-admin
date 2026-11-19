package com.zero.controller;

import com.zero.domain.DictItem;
import com.zero.model.AjaxResult;
import com.zero.service.TaskBizDictTestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/19 18:43
 * 说明: 验证任务父类的业务字典查询，数据源关闭时仍注册测试路由。
 */
@Slf4j
@RestController
@RequestMapping("/internal/task/biz-dict")
public class TaskBizDictTestController {

    /** 字典测试服务在请求时初始化，缺少 Mapper 时由 Spring 报错。 */
    @Autowired
    private TaskBizDictTestService taskBizDictTestService;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19
     * 说明: 查询字典项的原始值。
     * @param dictKey 分组编码.字典项编码
     * @return 匹配的字典原值
     * 返回值: String
     * 返回值说明: 未匹配时为空
     */
    @GetMapping("/findBizDictValue")
    public AjaxResult getBizDictValue(String dictKey) {
        String bizDictValue = taskBizDictTestService.findBizDictValue(dictKey);
        return AjaxResult.success("查询成功",bizDictValue);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19
     * 说明: 查询指定字典项。
     * @param dictKey 字典分组编码.字典项编码
     * @return 匹配的有效字典项
     * 返回值: DictItem
     * 返回值说明: 未匹配时为空
     */
    @GetMapping("/getActiveItem")
    public AjaxResult findActiveItem(String dictKey) {
        DictItem activeItem = taskBizDictTestService.getActiveItem(dictKey);
        return AjaxResult.success(activeItem);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19
     * 说明: 查询分组下的有效字典项。
     * @param groupCode 字典分组编码
     * @return 匹配的有效字典项列表
     * 返回值: List<DictItem>
     * 返回值说明: 未匹配时为空列表
     */
    @GetMapping("/getActiveItems")
    public AjaxResult findActiveItems(String groupCode) {
        List<DictItem> activeItems = taskBizDictTestService.getActiveItems(groupCode);
        log.info("查询字典列表成功,groupCodes:{}",groupCode);
        return AjaxResult.success(activeItems);
    }


}
