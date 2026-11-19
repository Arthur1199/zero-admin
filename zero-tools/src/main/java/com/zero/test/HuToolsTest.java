package com.zero.test;

import cn.hutool.json.JSONUtil;

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * 作者: Arthur 亚瑟
 * 时间: 2025/5/23 23:24
 * 说明: HuTools 三方api工具测试
 */
public class HuToolsTest {
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 使用固定示例展示 Hutool 工具和排序映射的使用。
     * @param args 命令行参数，示例不读取
     * @return
     * 返回值: void
     * 返回值说明: 向控制台输出示例结果
     */
    public static void main(String[] args) {
        SortedMap<Object, Object> sortedMap = new TreeMap<Object, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("attributes", "a");
                put("b", "b");
                put("c", "c");
            }};

        String jsonStr = JSONUtil.toJsonStr(sortedMap);
        System.out.println(jsonStr);
    }
}
