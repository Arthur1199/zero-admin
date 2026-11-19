package com.zero.utils.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/16 23:45
 * 说明: 反射工具类，统一处理类加载、对象实例化、字段读写和方法调用
 */
public class ReflectHelper {

	private static final Logger logger = LoggerFactory.getLogger(ReflectHelper.class);

	private ReflectHelper() {
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:45
	 * 说明: 根据类全限定名加载Class对象，优先使用当前线程上下文类加载器
	 * @param name 类全限定名，不能为空
	 * @return
	 * 返回值: Class<?>
	 * 返回值说明: 加载到的Class对象；上下文类加载器失败时降级使用Class.forName
	 * @throws ClassNotFoundException 类不存在时抛出异常
	 */
	public static Class<?> classForName(String name) throws ClassNotFoundException {
		try {
			return Thread.currentThread().getContextClassLoader().loadClass(name);
		} catch (ClassNotFoundException | SecurityException e) {
			logger.error(e.getMessage(), e);
			System.err.println("类[" + name + "]加载出错");
		}
        return Class.forName(name);
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:45
	 * 说明: 根据类全限定名创建对象实例，保留历史方法入口
	 * @param name 类全限定名，不能为空
	 * @return
	 * 返回值: Object
	 * 返回值说明: 创建成功的对象实例；类不存在、无可用构造方法或实例化失败时返回null
	 */
	public static Object objectForName(String name) {
		try {
			return newInstance(classForName(name));
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			System.err.println("类[" + name + "]获取对象实例出错");
		}
		return null;
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 根据Class类型创建对象实例
	 * @param <T> 当前方法处理的值类型
	 * @param clazz 目标类型，不能为空
	 * @return
	 * 返回值: T
	 * 返回值说明: 通过无参构造方法创建的对象实例
	 */
	public static <T> T newInstance(Class<T> clazz) {
		if (clazz == null) {
			throw new IllegalArgumentException("clazz must not be null");
		}
		try {
			return clazz.getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			throw new IllegalStateException("类[" + clazz.getName() + "]获取对象实例出错", e);
		}
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 根据类全限定名创建指定类型对象实例
	 * @param <T> 当前方法处理的值类型
	 * @param name 类全限定名，不能为空
	 * @param targetType 目标类型，不能为空
	 * @return
	 * 返回值: T
	 * 返回值说明: 创建并转换后的对象实例
	 * @throws ClassNotFoundException 类不存在时抛出异常
	 */
	public static <T> T newInstance(String name, Class<T> targetType) throws ClassNotFoundException {
		if (targetType == null) {
			throw new IllegalArgumentException("targetType must not be null");
		}
		Object instance = newInstance(classForName(name));
		return targetType.cast(instance);
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:45
	 * 说明: 读取对象字段值，支持向父类查找私有字段
	 * @param target 目标对象，不能为空
	 * @param fieldName 字段名称，不能为空
	 * @return
	 * 返回值: Object
	 * 返回值说明: 字段值；字段不存在或读取失败时抛出异常
	 */
	public static Object getFieldValue(Object target, String fieldName) {
		if (target == null) {
			throw new IllegalArgumentException("target must not be null");
		}
		try {
			Field field = findField(target.getClass(), fieldName);
			field.setAccessible(true);
			return field.get(target);
		} catch (Exception e) {
			throw new IllegalStateException("读取字段[" + fieldName + "]失败", e);
		}
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:45
	 * 说明: 设置对象字段值，支持向父类查找私有字段
	 * @param target 目标对象，不能为空
	 * @param fieldName 字段名称，不能为空
	 * @param value 字段值，可为空
	 * 返回值: void
	 * 返回值说明: 字段设置成功后无返回
	 */
	public static void setFieldValue(Object target, String fieldName, Object value) {
		if (target == null) {
			throw new IllegalArgumentException("target must not be null");
		}
		try {
			Field field = findField(target.getClass(), fieldName);
			field.setAccessible(true);
			field.set(target, value);
		} catch (Exception e) {
			throw new IllegalStateException("设置字段[" + fieldName + "]失败", e);
		}
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:45
	 * 说明: 调用对象方法，支持向父类查找私有方法
	 * @param target 目标对象，不能为空
	 * @param methodName 方法名称，不能为空
	 * @param parameterTypes 方法参数类型数组，无参数时传空数组
	 * @param args 方法实参数组，无参数时传空数组
	 * @return
	 * 返回值: Object
	 * 返回值说明: 方法返回值；void方法返回null
	 */
	public static Object invokeMethod(Object target, String methodName, Class<?>[] parameterTypes, Object[] args) {
		if (target == null) {
			throw new IllegalArgumentException("target must not be null");
		}
		try {
			Method method = findMethod(target.getClass(), methodName, parameterTypes);
			method.setAccessible(true);
			return method.invoke(target, args);
		} catch (Exception e) {
			throw new IllegalStateException("调用方法[" + methodName + "]失败", e);
		}
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:45
	 * 说明: 查找字段，当前类不存在时继续向父类查找
	 * @param clazz 起始类型，不能为空
	 * @param fieldName 字段名称，不能为空
	 * @return
	 * 返回值: Field
	 * 返回值说明: 查找到的字段对象
	 * @throws NoSuchFieldException 字段不存在时抛出异常
	 */
	public static Field findField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
		if (clazz == null) {
			throw new IllegalArgumentException("clazz must not be null");
		}
		if (fieldName == null || fieldName.isEmpty()) {
			throw new IllegalArgumentException("fieldName must not be empty");
		}
		Class<?> searchType = clazz;
		while (searchType != null && searchType != Object.class) {
			try {
				return searchType.getDeclaredField(fieldName);
			} catch (NoSuchFieldException e) {
				searchType = searchType.getSuperclass();
			}
		}
		throw new NoSuchFieldException(fieldName);
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:45
	 * 说明: 查找方法，当前类不存在时继续向父类查找
	 * @param clazz 起始类型，不能为空
	 * @param methodName 方法名称，不能为空
	 * @param parameterTypes 方法参数类型数组，无参数时传空数组
	 * @return
	 * 返回值: Method
	 * 返回值说明: 查找到的方法对象
	 * @throws NoSuchMethodException 方法不存在时抛出异常
	 */
	public static Method findMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
		if (clazz == null) {
			throw new IllegalArgumentException("clazz must not be null");
		}
		if (methodName == null || methodName.isEmpty()) {
			throw new IllegalArgumentException("methodName must not be empty");
		}
		Class<?> searchType = clazz;
		while (searchType != null && searchType != Object.class) {
			try {
				return searchType.getDeclaredMethod(methodName, parameterTypes);
			} catch (NoSuchMethodException e) {
				searchType = searchType.getSuperclass();
			}
		}
		throw new NoSuchMethodException(methodName);
	}
}
