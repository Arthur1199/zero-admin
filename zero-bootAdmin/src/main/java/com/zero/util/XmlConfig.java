package com.zero.util;

import lombok.extern.slf4j.Slf4j;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/17 01:46
 * 说明: XML配置读取工具，支持从环境变量读取敏感配置
 */
@Slf4j
public class XmlConfig {

	private static final Pattern ENV_PLACEHOLDER_PATTERN = Pattern.compile("^\\$\\{([A-Za-z0-9_]+)(?::([^}]*))?}$");

	private static final Map<String, String> items = new HashMap<>();

	private static final String CONFIG_FILE_NAME = "configuration.xml";

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/17 01:46
	 * 说明: 加载configuration.xml配置，配置项按category.item格式写入内存
	 * @return
	 * 返回值: void
	 * 返回值说明: 加载XML配置到内存，敏感占位符会解析为环境变量
	 */
	@SuppressWarnings("rawtypes")
	public static void loadConfig() {
		if (!items.isEmpty()) {
			log.error("配置{}文件已经装载完成，无需重新加载", CONFIG_FILE_NAME);
			return;
		}

		log.info("开始加载{}配置文件", CONFIG_FILE_NAME);
		InputStream inputStream = PropHelper.guessPropFile(XmlConfig.class, CONFIG_FILE_NAME);
		if (inputStream == null) {
			log.error("未找到{}配置文件", CONFIG_FILE_NAME);
			return;
		}

		try (InputStream is = inputStream) {
			SAXReader reader = new SAXReader();
			Document document = reader.read(is);
			Element systemElement = document.getRootElement();
			List catList = systemElement.elements("category");
			for (Iterator catIter = catList.iterator(); catIter.hasNext();) {
				Element catElement = (Element) catIter.next();
				String catName = catElement.attributeValue("name");
				if (StringHelper.isEmpty(catName)) {
					continue;
				}
				List itemList = catElement.elements("item");
				for (Iterator itemIter = itemList.iterator(); itemIter.hasNext();) {
					Element itemElement = (Element) itemIter.next();
					String itemName = itemElement.attributeValue("name");
					String value = itemElement.attributeValue("value");
					if (!StringHelper.isEmpty(itemName)) {
						items.put(catName + "." + itemName, resolveEnvPlaceholder(value));
					}
				}
			}
		} catch (Exception ex) {
			log.error("读取配置文件出错", ex);
			return;
		}
		log.info("加载{}配置文件完成，配置项数量：{}", CONFIG_FILE_NAME, items.size());
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/17 01:46
	 * 说明: 获取字符串配置值
	 * @param name 配置项名称，格式为category.item
	 * @return
	 * 返回值: String
	 * 返回值说明: 配置值不存在时返回空字符串
	 */
	public static String getString(String name) {
		String value = items.get(name);
		return value == null ? "" : value;
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/17 01:46
	 * 说明: 获取字符串配置值，配置为空时使用默认值兜底
	 * @param name 配置项名称，格式为category.item
	 * @param defaultValue 默认值
	 * @return
	 * 返回值: String
	 * 返回值说明: 有效配置值或默认值
	 */
	public static String getString(String name, String defaultValue) {
		String value = items.get(name);
		return value != null && !value.isEmpty() ? value : defaultValue;
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/17 01:46
	 * 说明: 获取整数配置值
	 * @param name 配置项名称，格式为category.item
	 * @return
	 * 返回值: int
	 * 返回值说明: 转换成功返回配置值，转换失败返回0
	 */
	public static int getInt(String name) {
		String value = getString(name);
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException ex) {
			log.error("配置文件key[{}]配置错误，return 0", name, ex);
			return 0;
		}
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/17 01:46
	 * 说明: 获取整数配置值，配置为空或格式错误时使用默认值兜底
	 * @param name 配置项名称，格式为category.item
	 * @param defaultValue 默认值
	 * @return
	 * 返回值: int
	 * 返回值说明: 转换成功返回配置值，转换失败返回默认值
	 */
	public static int getInt(String name, int defaultValue) {
		String value = getString(name);
		if ("".equals(value)) {
			return defaultValue;
		}
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException ex) {
			log.error("配置文件key[{}]配置错误，return {}", name, defaultValue, ex);
			return defaultValue;
		}
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/17 01:46
	 * 说明: 获取布尔配置值
	 * @param name 配置项名称，格式为category.item
	 * @return
	 * 返回值: boolean
	 * 返回值说明: true-配置为true，false-未配置或非true
	 */
	public static boolean getBoolean(String name) {
		String value = getString(name);
		return Boolean.parseBoolean(value);
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/17 01:46
	 * 说明: 获取浮点数配置值，配置格式错误时使用默认值兜底
	 * @param name 配置项名称，格式为category.item
	 * @param defaultValue 默认值
	 * @return
	 * 返回值: double
	 * 返回值说明: 转换成功返回配置值，转换失败返回默认值
	 */
	public static double getDouble(String name, double defaultValue) {
		String value = getString(name);
		try {
			return Double.parseDouble(value);
		} catch (NumberFormatException ex) {
			log.error("配置文件key[{}]配置错误，return {}", name, defaultValue, ex);
			return defaultValue;
		}
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/17 01:46
	 * 说明: 获取当前已加载的全部配置项
	 * @return
	 * 返回值: Map
	 * 返回值说明: 配置项快照，调用方不得向日志输出敏感值
	 */
	@SuppressWarnings("rawtypes")
	public static Map getItems() {
		return items;
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/17 01:46
	 * 说明: 解析XML中的环境变量占位符，避免机器人密钥等敏感配置写入仓库
	 * @param value XML中配置的原始值，支持${ENV_NAME:defaultValue}格式
	 * @return
	 * 返回值: String
	 * 返回值说明: 环境变量实际值；环境变量不存在时返回默认值或空字符串
	 */
	private static String resolveEnvPlaceholder(String value) {
		if (StringHelper.isEmpty(value)) {
			return "";
		}
		Matcher matcher = ENV_PLACEHOLDER_PATTERN.matcher(value);
		if (!matcher.matches()) {
			return value;
		}
		String envValue = System.getenv(matcher.group(1));
		if (!StringHelper.isEmpty(envValue)) {
			return envValue;
		}
		String defaultValue = matcher.group(2);
		return defaultValue == null ? "" : defaultValue;
	}
}
