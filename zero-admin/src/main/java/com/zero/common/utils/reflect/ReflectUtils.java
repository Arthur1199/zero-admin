package com.zero.common.utils.reflect;

import lombok.extern.slf4j.Slf4j;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.poi.ss.usermodel.DateUtil;
import com.zero.common.core.text.Convert;
import com.zero.common.utils.DateUtils;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 反射工具类. 提供调用getter/setter方法, 访问私有变量, 调用私有方法, 获取泛型类型Class, 被AOP过的真实类等工具函数.
 */
@Slf4j
@SuppressWarnings("rawtypes")
public class ReflectUtils
{
    /**
     * SETTERPREFIX字段
     */
    private static final String SETTER_PREFIX = "set";
    /**
     * GETTERPREFIX字段
     */

    private static final String GETTER_PREFIX = "get";
    /**
     * CGLIBCLASSSEPARATOR字段
     */

    private static final String CGLIB_CLASS_SEPARATOR = "$$";
    /**
     * logger字段
     */

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 调用Getter方法.支持多级，如：对象名.对象名.方法
     * @param <E> 由调用方提供或期望的对象类型
     * @param obj obj参数
     * @param propertyName property名称参数
     * @return
     * 返回值: E
     * 返回值说明: 调用Getter方法.支持多级，如：对象名.对象名.方法
     */
    @SuppressWarnings("unchecked")
    public static <E> E invokeGetter(Object obj, String propertyName)
    {
        Object object = obj;
        for (String name : StringUtils.split(propertyName, "."))
        {
            String getterMethodName = GETTER_PREFIX + StringUtils.capitalize(name);
            object = invokeMethod(object, getterMethodName, new Class[] {}, new Object[] {});
        }
        return (E) object;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 沿以点分隔的属性路径定位对象，并按方法名调用最终属性的 setter。
     * @param <E> 待写入的值类型
     * @param obj 属性路径的根对象
     * @param propertyName 属性路径
     * @param value 待写入的值
     * @return
     * 返回值: void
     * 返回值说明: 找到匹配的 setter 时更新属性
     */
    public static <E> void invokeSetter(Object obj, String propertyName, E value)
    {
        Object object = obj;
        String[] names = StringUtils.split(propertyName, ".");
        for (int i = 0; i < names.length; i++)
        {
            if (i < names.length - 1)
            {
                String getterMethodName = GETTER_PREFIX + StringUtils.capitalize(names[i]);
                object = invokeMethod(object, getterMethodName, new Class[] {}, new Object[] {});
            }
            else
            {
                String setterMethodName = SETTER_PREFIX + StringUtils.capitalize(names[i]);
                invokeMethodByName(object, setterMethodName, new Object[] { value });
            }
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 直接读取对象属性值, 无视private/protected修饰符, 不经过getter函数.
     * @param <E> 由调用方提供或期望的对象类型
     * @param obj obj参数
     * @param fieldName field名称参数
     * @return
     * 返回值: E
     * 返回值说明: 直接读取对象属性值, 无视private/protected修饰符, 不经过getter函数.
     */
    @SuppressWarnings("unchecked")
    public static <E> E getFieldValue(final Object obj, final String fieldName)
    {
        Field field = getAccessibleField(obj, fieldName);
        if (field == null)
        {
            log.debug("在 [" + obj.getClass() + "] 中，没有找到 [" + fieldName + "] 字段 ");
            return null;
        }
        E result = null;
        try
        {
            result = (E) field.get(obj);
        }
        catch (IllegalAccessException e)
        {
            log.error("不可能抛出的异常{}", e.getMessage());
        }
        return result;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 沿类继承层级定位字段并直接赋值，绕过属性 setter。
     * @param <E> 待写入的值类型
     * @param obj 要修改的对象
     * @param fieldName 字段名称
     * @param value 待写入的值
     * @return
     * 返回值: void
     * 返回值说明: 定位到字段后尝试写入
     */
    public static <E> void setFieldValue(final Object obj, final String fieldName, final E value)
    {
        Field field = getAccessibleField(obj, fieldName);
        if (field == null)
        {
            // throw new IllegalArgumentException("在 [" + obj.getClass() + "] 中，没有找到 [" + fieldName + "] 字段 ");
            log.debug("在 [" + obj.getClass() + "] 中，没有找到 [" + fieldName + "] 字段 ");
            return;
        }
        try
        {
            field.set(obj, value);
        }
        catch (IllegalAccessException e)
        {
            log.error("不可能抛出的异常: {}", e.getMessage());
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 按方法名及精确的形参类型定位目标方法，找到后通过反射调用。
     * @param <E> 目标方法的结果类型
     * @param obj 待调用方法的对象
     * @param methodName 要调用的方法名
     * @param parameterTypes 用于定位重载的形参类型数组
     * @param args 调用时传入的实参
     * @return
     * 返回值: E
     * 返回值说明: 目标方法的结果，目标或方法缺失时返回 null，调用异常向上传递
     */
    @SuppressWarnings("unchecked")
    public static <E> E invokeMethod(final Object obj, final String methodName, final Class<?>[] parameterTypes,
            final Object[] args)
    {
        if (obj == null || methodName == null)
        {
            return null;
        }
        Method method = getAccessibleMethod(obj, methodName, parameterTypes);
        if (method == null)
        {
            log.debug("在 [" + obj.getClass() + "] 中，没有找到 [" + methodName + "] 方法 ");
            return null;
        }
        try
        {
            return (E) method.invoke(obj, args);
        }
        catch (Exception e)
        {
            String msg = "method: " + method + ", obj: " + obj + ", args: " + args + "";
            throw convertReflectionExceptionToUnchecked(msg, e);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 直接调用对象方法, 无视private/protected修饰符，用于一次性调用的情况，否则应使用getAccessibleMethodByName()函数获得Method后反复调用.只匹配函数名，如果有多个同名函数调用第一个。
     * @param <E> 由调用方提供或期望的对象类型
     * @param obj obj参数
     * @param methodName 方法名称参数
     * @param args args参数
     * @return
     * 返回值: E
     * 返回值说明: 直接调用对象方法, 无视private/protected修饰符，用于一次性调用的情况，否则应使用getAccessibleMethodByName()函数获得Method后反复调用.只匹配函数名，如果有多个同名函数调用第一个。
     */
    @SuppressWarnings("unchecked")
    public static <E> E invokeMethodByName(final Object obj, final String methodName, final Object[] args)
    {
        Method method = getAccessibleMethodByName(obj, methodName, args.length);
        if (method == null)
        {
            // 如果为空不报错，直接返回空。
            log.debug("在 [" + obj.getClass() + "] 中，没有找到 [" + methodName + "] 方法 ");
            return null;
        }
        try
        {
            // 类型转换（将参数数据类型转换为目标方法参数类型）
            Class<?>[] cs = method.getParameterTypes();
            for (int i = 0; i < cs.length; i++)
            {
                if (args[i] != null && !args[i].getClass().equals(cs[i]))
                {
                    if (cs[i] == String.class)
                    {
                        args[i] = Convert.toStr(args[i]);
                        if (StringUtils.endsWith((String) args[i], ".0"))
                        {
                            args[i] = StringUtils.substringBefore((String) args[i], ".0");
                        }
                    }
                    else if (cs[i] == Integer.class)
                    {
                        args[i] = Convert.toInt(args[i]);
                    }
                    else if (cs[i] == Long.class)
                    {
                        args[i] = Convert.toLong(args[i]);
                    }
                    else if (cs[i] == Double.class)
                    {
                        args[i] = Convert.toDouble(args[i]);
                    }
                    else if (cs[i] == Float.class)
                    {
                        args[i] = Convert.toFloat(args[i]);
                    }
                    else if (cs[i] == Date.class)
                    {
                        if (args[i] instanceof String)
                        {
                            args[i] = DateUtils.parseDate(args[i]);
                        }
                        else
                        {
                            args[i] = DateUtil.getJavaDate((Double) args[i]);
                        }
                    }
                    else if (cs[i] == boolean.class || cs[i] == Boolean.class)
                    {
                        args[i] = Convert.toBool(args[i]);
                    }
                }
            }
            return (E) method.invoke(obj, args);
        }
        catch (Exception e)
        {
            String msg = "method: " + method + ", obj: " + obj + ", args: " + args + "";
            throw convertReflectionExceptionToUnchecked(msg, e);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 循环向上转型, 获取对象的DeclaredField, 并强制设置为可访问.如向上转型到Object仍无法找到, 返回null.
     * @param obj obj参数
     * @param fieldName field名称参数
     * @return
     * 返回值: Field
     * 返回值说明：循环向上转型, 获取对象的DeclaredField, 并强制设置为可访问.如向上转型到Object仍无法找到, 返回null.
     */
    public static Field getAccessibleField(final Object obj, final String fieldName)
    {
        // 为空不报错。直接返回 null
        if (obj == null)
        {
            return null;
        }
        Validate.notBlank(fieldName, "fieldName can't be blank");
        for (Class<?> superClass = obj.getClass(); superClass != Object.class; superClass = superClass.getSuperclass())
        {
            try
            {
                Field field = superClass.getDeclaredField(fieldName);
                makeAccessible(field);
                return field;
            }
            catch (NoSuchFieldException e)
            {
                continue;
            }
        }
        return null;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 沿父类逐级查找具有指定签名的方法，并设置反射可访问性。
     * @param obj 要查找方法的对象
     * @param methodName 要查找的方法名
     * @param parameterTypes 方法声明的形参类型数组
     * @return
     * 返回值: Method
     * 返回值说明: 找到的方法，无法定位时返回 null
     */
    public static Method getAccessibleMethod(final Object obj, final String methodName,
            final Class<?>... parameterTypes)
    {
        // 为空不报错。直接返回 null
        if (obj == null)
        {
            return null;
        }
        Validate.notBlank(methodName, "methodName can't be blank");
        for (Class<?> searchType = obj.getClass(); searchType != Object.class; searchType = searchType.getSuperclass())
        {
            try
            {
                Method method = searchType.getDeclaredMethod(methodName, parameterTypes);
                makeAccessible(method);
                return method;
            }
            catch (NoSuchMethodException e)
            {
                continue;
            }
        }
        return null;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 循环向上转型, 获取对象的DeclaredMethod,并强制设置为可访问.如向上转型到Object仍无法找到, 返回null.只匹配函数名。用于方法需要被多次调用的情况. 先使用本函数先取得Method,然后调用Method.invoke(Object obj, Object... args)
     * @param obj obj参数
     * @param methodName 方法名称参数
     * @param argsNum argsNum参数
     * @return
     * 返回值: Method
     * 返回值说明：循环向上转型, 获取对象的DeclaredMethod,并强制设置为可访问.如向上转型到Object仍无法找到, 返回null.只匹配函数名。用于方法需要被多次调用的情况. 先使用本函数先取得Method,然后调用Method.invoke(Object obj, Object... args)
     */
    public static Method getAccessibleMethodByName(final Object obj, final String methodName, int argsNum)
    {
        // 为空不报错。直接返回 null
        if (obj == null)
        {
            return null;
        }
        Validate.notBlank(methodName, "methodName can't be blank");
        for (Class<?> searchType = obj.getClass(); searchType != Object.class; searchType = searchType.getSuperclass())
        {
            Method[] methods = searchType.getDeclaredMethods();
            for (Method method : methods)
            {
                if (method.getName().equals(methodName) && method.getParameterTypes().length == argsNum)
                {
                    makeAccessible(method);
                    return method;
                }
            }
        }
        return null;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 改变private/protected的方法为public，尽量不调用实际改动的语句，避免JDK的SecurityManager抱怨。
     * @param method 方法参数
     * @return
     * 返回值: void
     * 返回值说明：改变private/protected的方法为public，尽量不调用实际改动的语句，避免JDK的SecurityManager抱怨。
     */
    public static void makeAccessible(Method method)
    {
        if ((!Modifier.isPublic(method.getModifiers()) || !Modifier.isPublic(method.getDeclaringClass().getModifiers()))
                && !method.isAccessible())
        {
            method.setAccessible(true);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 改变private/protected的成员变量为public，尽量不调用实际改动的语句，避免JDK的SecurityManager抱怨。
     * @param field field参数
     * @return
     * 返回值: void
     * 返回值说明：改变private/protected的成员变量为public，尽量不调用实际改动的语句，避免JDK的SecurityManager抱怨。
     */
    public static void makeAccessible(Field field)
    {
        if ((!Modifier.isPublic(field.getModifiers()) || !Modifier.isPublic(field.getDeclaringClass().getModifiers())
                || Modifier.isFinal(field.getModifiers())) && !field.isAccessible())
        {
            field.setAccessible(true);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 通过反射, 获得Class定义中声明的泛型参数的类型, 注意泛型必须定义在父类处如无法找到, 返回Object.class.
     * @param <T> 当前方法处理的值类型
     * @param clazz clazz参数
     * @return
     * 返回值: Class<T>
     * 返回值说明: 通过反射, 获得Class定义中声明的泛型参数的类型, 注意泛型必须定义在父类处如无法找到, 返回Object.class.
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> getClassGenricType(final Class clazz)
    {
        return getClassGenricType(clazz, 0);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 通过反射, 获得Class定义中声明的父类的泛型参数的类型.如无法找到, 返回Object.class.
     * @param clazz clazz参数
     * @param index index参数
     * @return
     * 返回值: Class
     * 返回值说明：通过反射, 获得Class定义中声明的父类的泛型参数的类型.如无法找到, 返回Object.class.
     */
    public static Class getClassGenricType(final Class clazz, final int index)
    {
        Type genType = clazz.getGenericSuperclass();

        if (!(genType instanceof ParameterizedType))
        {
            log.debug(clazz.getSimpleName() + "'s superclass not ParameterizedType");
            return Object.class;
        }

        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();

        if (index >= params.length || index < 0)
        {
            log.debug("Index: " + index + ", Size of " + clazz.getSimpleName() + "'s Parameterized Type: "
                    + params.length);
            return Object.class;
        }
        if (!(params[index] instanceof Class))
        {
            log.debug(clazz.getSimpleName() + " not set the actual class on superclass generic parameter");
            return Object.class;
        }

        return (Class) params[index];
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取用户Class
     * @param instance instance参数
     * @return
     * 返回值: Class<>
     * 返回值说明：获取用户Class
     */

    public static Class<?> getUserClass(Object instance)
    {
        if (instance == null)
        {
            throw new RuntimeException("Instance must not be null");
        }
        Class clazz = instance.getClass();
        if (clazz != null && clazz.getName().contains(CGLIB_CLASS_SEPARATOR))
        {
            Class<?> superClass = clazz.getSuperclass();
            if (superClass != null && !Object.class.equals(superClass))
            {
                return superClass;
            }
        }
        return clazz;

    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 将反射时的checked exception转换为unchecked exception.
     * @param msg msg参数
     * @param e e参数
     * @return
     * 返回值: RuntimeException
     * 返回值说明：将反射时的checked exception转换为unchecked exception.
     */
    public static RuntimeException convertReflectionExceptionToUnchecked(String msg, Exception e)
    {
        if (e instanceof IllegalAccessException || e instanceof IllegalArgumentException
                || e instanceof NoSuchMethodException)
        {
            return new IllegalArgumentException(msg, e);
        }
        else if (e instanceof InvocationTargetException)
        {
            return new RuntimeException(msg, ((InvocationTargetException) e).getTargetException());
        }
        return new RuntimeException(msg, e);
    }
}
