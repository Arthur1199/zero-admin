package com.zero.utils.common;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/16 21:59
 * 说明: 计算数字组合工具类
 */
public class AssembleUtil {

	/**
	 * 递归计算结果暂存集合，使用TreeSet保证结果去重并按自然顺序返回。
	 */
	private static final Set<String> set = new TreeSet<String>();

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 22:46
	 * 说明: 根据逗号分隔的源字符串生成指定长度的有序排列结果
	 * @param sourceStr 源字符串，多个元素使用英文逗号分隔，例如1,2,3,4
	 * @param max 每个排列结果包含的元素数量
	 * @return
	 * 返回值: String[]
	 * 返回值说明: 去重并排序后的排列结果数组，结果元素会去掉递归过程中的连接符
	 */
	public static String[] getAssemble(String sourceStr, int max) {
		String[] sourceList = sourceStr.split(",");
        return getAssemble(sourceList, max);
	}
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 22:46
	 * 说明: 根据源数组生成指定长度的有序排列结果
	 * @param sourceArray 源数组，每个元素在同一个结果中最多使用一次
	 * @param max 每个排列结果包含的元素数量
	 * @return
	 * 返回值: String[]
	 * 返回值说明: 去重并排序后的排列结果数组，调用结束后会清空内部暂存集合
	 */
	public static String[] getAssemble(String[] sourceArray, int max) {
        for (String s : sourceArray) {
            doSet(s, sourceArray, max);
        }
		String[] arr = new String[set.size()];
		String[] array = set.toArray(arr);
		set.clear();
		return array;
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 22:46
	 * 说明: 递归拼接排列结果，当前路径达到指定长度后写入暂存集合
	 * @param start 当前递归路径，元素之间使用下划线临时分隔
	 * @param sourceList 源数组，递归时从中选择尚未出现在当前路径中的元素
	 * @param max 每个排列结果包含的元素数量
	 * 返回值: void
	 * 返回值说明: 满足长度要求的结果会写入类级暂存集合set
	 */
	private static void doSet(String start, String[] sourceList, int max) {
		String[] olds = start.split("_");
		if (olds.length == max) {
			set.add(start.replace("_", "").trim());
		} else {
            for (String string : sourceList) {
                if (Arrays.asList(olds).contains(string)) {
                    continue;
                } else {
                    doSet(start + "_" + string, sourceList, max);
                }
            }
		}
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 22:46
	 * 说明: 本地验证排列计算结果
	 * @param args 启动参数，当前测试方法未使用
	 * 返回值: void
	 * 返回值说明: 在控制台输出排列总数和明细
	 */
	public static void main(String[] args) {
		    String[] sourceArr = new String[] { "1", "2", "3", "4", "5","6","7" };
	        String[] resultArr = getAssemble(sourceArr, 3);
	        System.out.println("累计组合："+resultArr.length+","+Arrays.toString(resultArr));
	        String sourceStr = "1,2,3,4,5,6,7";
	        String[] resultArr2 = getAssemble(sourceStr, 3);
	        System.out.println("累计组合："+resultArr2.length+","+Arrays.toString(resultArr2));
	}
}
