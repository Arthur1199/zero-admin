package com.zero.utils.notify.telegram;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.zero.utils.common.StringHelper;
import com.zero.utils.common.DateHelper;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 把普通值或带 TelegramMsgKey 标记的对象字段转换成通知文本，供调用方决定是否发送。
 */
public class TeleGramMsgHelper {

	/** 标题与消息内容之间的分隔线字符数。 */
	private static final Integer MAX_START = 54;

	/** 消息文本使用的行分隔符。 */
	private static final String LINE = "\r\n";
	
	/** 标题分隔线使用的字符。 */
	private static final String SPLIT = "*";

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 按对象类型组装标题、字段和系统时间。普通值直接展示，复杂字段按注解名称输出，嵌套值按 JSON 展示。
	 * @param title 可选的消息标题
	 * @param objet 待格式化的值或带消息字段注解的对象
	 * @return
	 * 返回值: String
	 * 返回值说明: 生成的消息文本，待展示对象为 null 时返回空字符串
	 */
	public static String generateMsg(String title, Object objet) {
		StringBuffer sb = new StringBuffer();
		if (objet == null) {
			System.out.println("对象为空，无法获取属性信息。");
			return "";
		}
		if (StringHelper.isNotEmpty(title)) {
			sb.append(title).append(LINE);
			for (int i = 0; i < MAX_START; i++) {
				sb.append(SPLIT);
			}
			sb.append(LINE);
		}
		Class<?> clazz = objet.getClass();
		// 字符串和基础值可直接展示，只对复杂对象按字段注解取值。
		boolean primitiveObject = isPrimitiveOrWrapper(objet);
		if(primitiveObject) {
			sb.append(objet.toString()).append(LINE);
			String sysTimeStr = DateHelper.formatDate(new Date());
			sb.append("系统时间：" + sysTimeStr);
			return sb.toString();
		}
		// 仅遍历当前类声明的字段，消息内容由字段上的注解决定。
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			try {
				field.setAccessible(true);
				TelegramMsgKey annotation = field.getAnnotation(TelegramMsgKey.class);
				if (annotation != null) {
					String keyName = annotation.keyName();
					Object fieldValue = field.get(objet);
					boolean primitiveOrWrapper = isPrimitiveOrWrapper(fieldValue);
					if (primitiveOrWrapper) {
						if(fieldValue != null && StringHelper.isNotEmpty(fieldValue.toString())) {
							sb.append(keyName).append("：");
							sb.append(fieldValue != null ? fieldValue.toString() : "").append(LINE);
						}
					} else {
						// 集合或嵌套对象单独换行，保留格式化后的 JSON 结构。
						if(fieldValue != null) {
							sb.append(keyName).append("：");
							sb.append(LINE);
							sb.append(fieldValue != null ? JSON.toJSONString(fieldValue,JSONWriter.Feature.PrettyFormat) : "").append(LINE);
						}
					}
				}
			} catch (IllegalAccessException e) {
				System.out.println("无法访问字段：" + field.getName());
				e.printStackTrace();
			}
		}
		String sysTimeStr = DateHelper.formatDate(new Date());
		sb.append("系统时间：" + sysTimeStr);
		return sb.toString();
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 判断对象是否是基本数据类型或其包装类
	 * @param obj 要判断的对象
	 * @return
	 * 返回值: boolean
	 * 返回值说明: 如果是基本数据类型或包装类，返回 true；否则返回 false
	 */
	public static boolean isPrimitiveOrWrapper(Object obj) {
		if (obj == null) {
			return false; // null 不是任何类型的实例
		}

		// 获取对象的类
		Class<?> clazz = obj.getClass();

		// 检查是否是基本数据类型的包装类
		if (clazz.isPrimitive()) {
			return true; // 基本数据类型
		}

		//是否String
		if (clazz.equals(String.class)) {
			return true; // 基本数据类型
		}
		// 检查是否是基本数据类型的包装类
		return clazz.equals(Byte.class) || clazz.equals(Short.class) || clazz.equals(Integer.class)
				|| clazz.equals(Long.class) || clazz.equals(Float.class) || clazz.equals(Double.class)
				|| clazz.equals(Character.class) || clazz.equals(Boolean.class);
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 用包含普通字段、集合和映射的样例对象演示 Telegram 消息格式。
	 * @param args 命令行参数，示例不读取
	 * @return
	 * 返回值: void
	 * 返回值说明: 只在控制台打印生成的消息文本
	 */
	public static void main(String[] args) {
		Map<String, Object> userInfoMap = new HashMap<>();
		userInfoMap.put("amount", "99999");
		userInfoMap.put("address", "北京三里胡同");
		List<Hobby> hobbyList = new ArrayList<>();
		Hobby hobby1 = new Hobby("足球", "周末踢足球");
		Hobby hobby2 = new Hobby("羽毛球", "每天打羽毛球");
		hobbyList.add(hobby1);
		hobbyList.add(hobby2);
		TestTeleGramMsg msg = new TestTeleGramMsg("jack", 18, 175.1, userInfoMap, hobbyList);
		System.out.println(msg);
		String teleGramMsg = TeleGramMsgHelper.generateMsg("用户信息", msg);
		System.out.println(teleGramMsg);
	}
}
