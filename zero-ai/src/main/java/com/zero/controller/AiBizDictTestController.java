package com.zero.controller;

import com.zero.domain.DictItem;
import com.zero.model.AjaxResult;
import com.zero.service.AiBizDictTestService;
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
@RequestMapping("/internal/ai/biz-dict")
public class AiBizDictTestController {

    /** 字典测试服务在请求时初始化，缺少 Mapper 时由 Spring 报错。 */
    @Autowired
    private AiBizDictTestService aiBizDictTestService;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 读取指定业务字典项的原始值。
     * @param dictKey 以点分隔的字典分组和字典项编码
     * @return
     * 返回值: AjaxResult
     * 返回值说明: 成功响应的 data 保存字典值，无匹配项时为空
     */
    @GetMapping("/findBizDictValue")
    public AjaxResult getBizDictValue(String dictKey) {
        String bizDictValue = aiBizDictTestService.findBizDictValue(dictKey);
        return AjaxResult.success("查询成功",bizDictValue);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 读取指定的有效业务字典项及其完整字段。
     * @param dictKey 以点分隔的字典分组和字典项编码
     * @return
     * 返回值: AjaxResult
     * 返回值说明: 成功响应的 data 保存字典项，无匹配项时为空
     */
    @GetMapping("/getActiveItem")
    public AjaxResult findActiveItem(String dictKey) {
        DictItem activeItem = aiBizDictTestService.getActiveItem(dictKey);
        return AjaxResult.success(activeItem);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 按字典分组查询全部有效字典项。
     * @param groupCode 字典分组编码
     * @return
     * 返回值: AjaxResult
     * 返回值说明: 成功响应的 data 保存字典项列表，没有匹配项时为空列表
     */
    @GetMapping("/getActiveItems")
    public AjaxResult findActiveItems(String groupCode) {
        List<DictItem> activeItems = aiBizDictTestService.getActiveItems(groupCode);
        log.info("查询字典列表成功,groupCodes:{}",groupCode);
        return AjaxResult.success(activeItems);
    }


}
