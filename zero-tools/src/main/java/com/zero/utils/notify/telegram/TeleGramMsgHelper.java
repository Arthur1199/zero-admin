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
 * 作者: Jack 时间: 2025年3月5日17:24:14 说明: telegram消息组装工具类
 *
 */
public class TeleGramMsgHelper {

	// * 号最大个数
	private static final Integer MAX_START = 54;

	// 换行符
	private static final String LINE = "\r\n";
	
	//分隔符
	private static final String SPLIT = "*";

	/**
	 * 作者:Jack 日期:2025年3月5日17:31:56
	 * 
	 * @param title
	 * @param objet
	 * @return 返回值:String 返回值说明:生成消息
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
		// 获取对象的类信息
		Class<?> clazz = objet.getClass();
		boolean primitiveObject = isPrimitiveOrWrapper(objet);
		if(primitiveObject) {
			sb.append(objet.toString()).append(LINE);
			String sysTimeStr = DateHelper.formatDate(new Date());
			sb.append("系统时间：" + sysTimeStr);
			return sb.toString();
		}
//		System.out.println("对象类型：" + clazz.getName());
		// 获取类的所有字段（包括私有字段）
		Field[] fields = clazz.getDeclaredFields();
		// 遍历字段并打印字段名和值
		for (Field field : fields) {
			try {
				// 设置私有字段可访问
				field.setAccessible(true);
				// 获取字段名和字段值
				TelegramMsgKey annotation = field.getAnnotation(TelegramMsgKey.class);
				if (annotation != null) {
					String keyName = annotation.keyName();
//                	System.out.println("keyName" + keyName);
//                	String fieldName = field.getName();
					Object fieldValue = field.get(objet);
					boolean primitiveOrWrapper = isPrimitiveOrWrapper(fieldValue);
					if (primitiveOrWrapper) {
						if(fieldValue != null && StringHelper.isNotEmpty(fieldValue.toString())) {
							sb.append(keyName).append("：");
							sb.append(fieldValue != null ? fieldValue.toString() : "").append(LINE);
						}
						// 打印字段信息
//                    	System.out.println("字段名：" + fieldName + ", 字段值：" + fieldValue);
					} else {
//						sb.append(keyName).append("：");
						if(fieldValue != null) {
							sb.append(keyName).append("：");
							sb.append(LINE);
							sb.append(fieldValue != null ? JSON.toJSONString(fieldValue,JSONWriter.Feature.PrettyFormat) : "").append(LINE);
						}
//						else {
//							sb.append(LINE);
//						}
						// 打印字段信息
//                    	System.out.println("字段名：" + fieldName + ", 字段JSON值：" + (fieldValue != null ? JSON.toJSONString(fieldValue):""));
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
	 * 判断对象是否是基本数据类型或其包装类
	 * 
	 * @param obj 要判断的对象
	 * @return 如果是基本数据类型或包装类，返回 true；否则返回 false
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
	 * 作者:Jack
	 * 日期:2025年3月5日18:27:03
	 * @param args
	 * 返回值:void
	 * 返回值说明:测试方法
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
