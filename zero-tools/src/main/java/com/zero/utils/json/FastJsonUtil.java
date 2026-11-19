package com.zero.utils.json;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.alibaba.fastjson2.JSONWriter;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/16 23:35
 * 说明: Fastjson2序列化和反序列化工具类，统一处理对象、集合、Map和JSON文件读写
 */
public class FastJsonUtil {

	/**
	 * 默认序列化特性，空值按前端友好格式输出并格式化JSON
	 */
	private static final JSONWriter.Feature[] features = { JSONWriter.Feature.WriteMapNullValue,
			JSONWriter.Feature.WriteNullListAsEmpty,
			JSONWriter.Feature.WriteNullNumberAsZero,
			JSONWriter.Feature.WriteNullBooleanAsFalse,
			JSONWriter.Feature.WriteNullStringAsEmpty,
			JSONWriter.Feature.PrettyFormat
	};

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:35
	 * 说明: 将对象转换为JSON字符串，默认输出空字段并格式化内容
	 * @param object 待序列化对象，可为空
	 * @return
	 * 返回值: String
	 * 返回值说明: JSON字符串；对象为空时按Fastjson默认规则返回null文本
	 */
	public static String toJSONString(Object object) {
		return JSON.toJSONString(object, features);
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:35
	 * 说明: 将对象转换为JSON字符串，不启用空值和格式化特性
	 * @param object 待序列化对象，可为空
	 * @return
	 * 返回值: String
	 * 返回值说明: JSON字符串；对象为空时按Fastjson默认规则返回null文本
	 */
	public static String toJSONNoFeatures(Object object) {
		return JSON.toJSONString(object);
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:35
	 * 说明: 将JSON字符串转换为JSONObject
	 * @param jsonStr JSON字符串
	 * @return
	 * 返回值: JSONObject
	 * 返回值说明: JSON对象；字符串格式错误时由Fastjson抛出解析异常
	 */
	public static JSONObject toJsonObj(String jsonStr) {
		return JSON.parseObject(jsonStr);
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 将JSON字符串转换为指定Java对象
	 * @param <T> 当前方法处理的值类型
	 * @param jsonStr JSON字符串
	 * @param clazz 目标对象类型
	 * @return
	 * 返回值: T
	 * 返回值说明: 转换后的目标对象；字符串格式错误时由Fastjson抛出解析异常
	 */
	public static <T> T toBean(String jsonStr, Class<T> clazz) {
		return JSON.parseObject(jsonStr, clazz);
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 将JSON数组字符串转换为Object数组
	 * @param <T> 当前方法处理的值类型
	 * @param jsonStr JSON数组字符串
	 * @return
	 * 返回值: Object[]
	 * 返回值说明: 转换后的Object数组；字符串格式错误时由Fastjson抛出解析异常
	 */
	public static <T> Object[] toArray(String jsonStr) {
		return toArray(jsonStr, null);
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 将JSON数组字符串转换为指定类型数组
	 * @param <T> 当前方法处理的值类型
	 * @param jsonStr JSON数组字符串
	 * @param clazz 数组元素类型，可为空；为空时按JSONObject等默认类型解析
	 * @return
	 * 返回值: Object[]
	 * 返回值说明: 转换后的数组；字符串格式错误时由Fastjson抛出解析异常
	 */
	public static <T> Object[] toArray(String jsonStr, Class<T> clazz) {
		return JSON.parseArray(jsonStr, clazz).toArray();
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 将JSON数组字符串转换为指定类型列表
	 * @param <T> 当前方法处理的值类型
	 * @param jsonStr JSON数组字符串
	 * @param clazz 列表元素类型
	 * @return
	 * 返回值: List<T>
	 * 返回值说明: 转换后的列表；字符串格式错误时由Fastjson抛出解析异常
	 */
	public static <T> List<T> toList(String jsonStr, Class<T> clazz) {
		return JSON.parseArray(jsonStr, clazz);
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:35
	 * 说明: 将JavaBean转换为JSONObject
	 * @param bean 待转换JavaBean，可为空
	 * @return
	 * 返回值: JSONObject
	 * 返回值说明: 转换后的JSONObject；对象为空时按Fastjson默认规则处理
	 */
	public static JSONObject beanToJsonObj(Object bean) {
		String jsonStr = JSON.toJSONString(bean);
		return JSON.parseObject(jsonStr);
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:35
	 * 说明: 将JSON对象字符串转换为Map
	 * @param jsonStr JSON对象字符串
	 * @return
	 * 返回值: Map<?, ?>
	 * 返回值说明: 转换后的Map；字符串格式错误时由Fastjson抛出解析异常
	 */
	public static Map<?, ?> stringToCollect(String jsonStr) {
		return JSON.parseObject(jsonStr);
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:35
	 * 说明: 将Map转换为JSON字符串
	 * @param map 待转换Map，可为空
	 * @return
	 * 返回值: String
	 * 返回值说明: JSON字符串；Map为空时按Fastjson默认规则返回null文本
	 */
	public static String collectToString(Map<?, ?> map) {
		return JSON.toJSONString(map);
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 将对象序列化为格式化JSON并写入文件
	 * @param <T> 当前方法处理的值类型
	 * @param t 待写入对象，可为空
	 * @param file 目标文件
	 * @return
	 * 返回值: void
	 * 返回值说明: JSON内容写入目标文件，文件不存在时由输出流创建
	 * @throws IOException 文件写入失败时抛出异常
	 */
	public static <T> void writeJsonToFile(T t, File file) throws IOException {
		String jsonStr = JSON.toJSONString(t, JSONWriter.Feature.PrettyFormat);
		try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
			bw.write(jsonStr);
		}
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 将对象序列化为格式化JSON并写入指定路径文件
	 * @param <T> 当前方法处理的值类型
	 * @param t 待写入对象，可为空
	 * @param filename 目标文件路径
	 * @return
	 * 返回值: void
	 * 返回值说明: JSON内容写入目标文件，文件不存在时由输出流创建
	 * @throws IOException 文件写入失败时抛出异常
	 */
	public static <T> void writeJsonToFile(T t, String filename) throws IOException {
		writeJsonToFile(t, new File(filename));
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 从JSON文件读取内容并转换为指定Java对象
	 * @param <T> 当前方法处理的值类型
	 * @param cls 目标对象类型
	 * @param file JSON文件
	 * @return
	 * 返回值: T
	 * 返回值说明: 转换后的目标对象；文件内容格式错误时由Fastjson抛出解析异常
	 * @throws IOException 文件读取失败时抛出异常
	 */
	public static <T> T readJsonFromFile(Class<T> cls, File file) throws IOException {
		return JSON.parseObject(readFileToString(file), cls);
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 从指定路径JSON文件读取内容并转换为指定Java对象
	 * @param <T> 当前方法处理的值类型
	 * @param cls 目标对象类型
	 * @param filename JSON文件路径
	 * @return
	 * 返回值: T
	 * 返回值说明: 转换后的目标对象；文件内容格式错误时由Fastjson抛出解析异常
	 * @throws IOException 文件读取失败时抛出异常
	 */
	public static <T> T readJsonFromFile(Class<T> cls, String filename) throws IOException {
		return readJsonFromFile(cls, new File(filename));
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 从JSON文件读取内容并按TypeReference转换，适合泛型集合或嵌套对象
	 * @param <T> 当前方法处理的值类型
	 * @param typeReference Fastjson类型引用
	 * @param file JSON文件
	 * @return
	 * 返回值: T
	 * 返回值说明: 转换后的目标对象；文件内容格式错误时由Fastjson抛出解析异常
	 * @throws IOException 文件读取失败时抛出异常
	 */
	public static <T> T readJsonFromFile(TypeReference<T> typeReference, File file) throws IOException {
		return JSON.parseObject(readFileToString(file), typeReference);
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 从指定路径JSON文件读取内容并按TypeReference转换，适合泛型集合或嵌套对象
	 * @param <T> 当前方法处理的值类型
	 * @param typeReference Fastjson类型引用
	 * @param filename JSON文件路径
	 * @return
	 * 返回值: T
	 * 返回值说明: 转换后的目标对象；文件内容格式错误时由Fastjson抛出解析异常
	 * @throws IOException 文件读取失败时抛出异常
	 */
	public static <T> T readJsonFromFile(TypeReference<T> typeReference, String filename) throws IOException {
		return readJsonFromFile(typeReference, new File(filename));
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:35
	 * 说明: 读取文件全部文本内容，供JSON文件反序列化方法复用
	 * @param file 待读取文件
	 * @return
	 * 返回值: String
	 * 返回值说明: 文件文本内容，按行拼接不额外追加换行符
	 * @throws IOException 文件读取失败时抛出异常
	 */
	private static String readFileToString(File file) throws IOException {
		StringBuilder strBuilder = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
			String line;
			while ((line = br.readLine()) != null) {
				strBuilder.append(line);
			}
		}
		return strBuilder.toString();
	}
}
