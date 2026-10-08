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
