package com.zero.utils.common;

import java.util.Map;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/16 23:39
 * 说明: Map取值工具类，按指定键读取字符串、数字和布尔值，并在缺失或转换失败时返回默认值
 */
public class MapHelper {

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:39
	 * 说明: 根据键名从Map中读取字符串值
	 * @param map 数据Map，不能为空
	 * @param name 键名，不能为空
	 * @return
	 * 返回值: String
	 * 返回值说明: 键对应的字符串值；键名为空、键不存在或值为空时返回空字符串
	 */
	public static String getString(Map<?, ?> map, String name) {
		if (name == null || name.isEmpty()) {
			return "";
		}

		String value = "";
		if (!map.containsKey(name)) {
			return "";
		}
		Object obj = map.get(name);
		if (obj != null) {
			value = obj.toString();
		}
		obj = null;

		return value;
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:39
	 * 说明: 根据键名从Map中读取int值
	 * @param map 数据Map，不能为空
	 * @param name 键名，不能为空
	 * @return
	 * 返回值: int
	 * 返回值说明: 键对应的int值；键名为空、键不存在、值为空或转换失败时返回0
	 */
	public static int getInt(Map<?, ?> map, String name) {
		if (name == null || name.isEmpty()) {
			return 0;
		}

		int value = 0;
		if (!map.containsKey(name)) {
			return 0;
		}

		Object obj = map.get(name);
		if (obj == null) {
			return 0;
		}

		if (!(obj instanceof Integer)) {
			try {
				value = Integer.parseInt(obj.toString());
			} catch (Exception ex) {
				ex.printStackTrace();
				System.err.println("name[" + name + "]对应的值不是数字，返回0");
            }
		} else {
			value = (Integer) obj;
			obj = null;
		}

		return value;
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:39
	 * 说明: 根据键名从Map中读取long值
	 * @param map 数据Map，不能为空
	 * @param name 键名，不能为空
	 * @return
	 * 返回值: long
	 * 返回值说明: 键对应的long值；键名为空、键不存在、值为空或转换失败时返回0
	 */
	public static long getLong(Map<?, ?> map, String name) {
		if (name == null || name.isEmpty()) {
			return 0;
		}

		long value = 0;
		if (!map.containsKey(name)) {
			return 0;
		}

		Object obj = map.get(name);
		if (obj == null) {
			return 0;
		}

		if (!(obj instanceof Long)) {
			try {
				value = Long.parseLong(obj.toString());
			} catch (Exception ex) {
				ex.printStackTrace();
				System.err.println("name[" + name + "]对应的值不是数字，返回0");
            }
		} else {
			value = (Long) obj;
			obj = null;
		}

		return value;
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:39
	 * 说明: 根据键名从Map中读取float值
	 * @param map 数据Map，不能为空
	 * @param name 键名，不能为空
	 * @return
	 * 返回值: float
	 * 返回值说明: 键对应的float值；键名为空、键不存在、值为空或转换失败时返回0
	 */
	public static float getFloat(Map<?, ?> map, String name) {
		if (name == null || name.equals("")) {
			return 0;
		}

		float value = 0;
		if (!map.containsKey(name)) {
			return 0;
		}

		Object obj = map.get(name);
		if (obj == null) {
			return 0;
		}

		if (!(obj instanceof Float)) {
			try {
				value = Float.parseFloat(obj.toString());
			} catch (Exception ex) {
				ex.printStackTrace();
				System.err.println("name[" + name + "]对应的值不是数字，返回0");
				value = 0;
			}
		} else {
			value = (Float) obj;
			obj = null;
		}

		return value;
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:39
	 * 说明: 根据键名从Map中读取double值
	 * @param map 数据Map，不能为空
	 * @param name 键名，不能为空
	 * @return
	 * 返回值: double
	 * 返回值说明: 键对应的double值；键名为空、键不存在、值为空或转换失败时返回0
	 */
	public static double getDouble(Map<?, ?> map, String name) {
		if (name == null || name.isEmpty()) {
			return 0;
		}

		double value = 0;
		if (!map.containsKey(name)) {
			return 0;
		}

		Object obj = map.get(name);
		if (obj == null) {
			return 0;
		}

		if (!(obj instanceof Double)) {
			try {
				value = Double.parseDouble(obj.toString());
			} catch (Exception ex) {
				ex.printStackTrace();
				System.err.println("name[" + name + "]对应的值不是数字，返回0");
				value = 0;
			}
		} else {
			value = (Double) obj;
			obj = null;
		}

		return value;
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:39
	 * 说明: 根据键名从Map中读取boolean值
	 * @param map 数据Map，不能为空
	 * @param name 键名，不能为空
	 * @return
	 * 返回值: boolean
	 * 返回值说明: 键对应的boolean值；键名为空、键不存在或值为空时返回false
	 */
	public static boolean getBoolean(Map<?, ?> map, String name) {
		if (name == null || name.isEmpty()) {
			return false;
		}

		boolean value = false;
		if (!map.containsKey(name)) {
			return false;
		}
		Object obj = map.get(name);
		if (obj == null) {
			return false;
		}
		if (obj instanceof Boolean) {
			return (Boolean) obj;
		}
		value = Boolean.parseBoolean(obj.toString());
		obj = null;
		return value;
	}
}
