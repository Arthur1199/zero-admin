package com.zero.framework.statistics;

import com.zero.common.annotation.SystemModule;
import com.zero.common.enums.SystemModuleEnum;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.method.HandlerMethod;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/22 18:45
 * 说明: 解析接口所属统计模块，注解优先、路由首段兜底
 */
public final class StatisticsModuleResolver
{
    /**
     * 模块编码最大长度，与 sys_stat_endpoint.module_name 列长度及入库校验保持一致
     */
    private static final int MAX_MODULE_LENGTH = 80;

    /**
     * 单段路由无法取到业务前缀时使用的兜底模块
     */
    private static final String ROOT_MODULE = "root";

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/22 18:45
     * 说明: 纯静态工具类，禁止实例化
     */
    private StatisticsModuleResolver()
    {
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/22 18:45
     * 说明: 先取方法级注解，再取控制器类级注解；都未声明时返回null交给路由首段兜底
     * @param method 已匹配的MVC处理方法
     * @return
     * 返回值: String
     * 返回值说明: 模块编码；未声明注解时为null
     */
    public static String resolve(HandlerMethod method)
    {
        if (method == null)
        {
            return null;
        }
        // 方法级声明优先，便于同一控制器下的登录页接口与后台管理接口分属不同模块。
        SystemModule annotation = AnnotatedElementUtils.findMergedAnnotation(method.getMethod(), SystemModule.class);
        if (annotation == null)
        {
            annotation = AnnotatedElementUtils.findMergedAnnotation(method.getBeanType(), SystemModule.class);
        }
        return annotation == null ? null : annotation.value().getCode();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/22 18:45
     * 说明: 按接口模板首段兜底归类，单段路由归root，超长首段归other
     * @param route 归一化接口模板，例如 /system/user/{userId}
     * @return
     * 返回值: String
     * 返回值说明: 首段模块编码或root、other兜底值
     */
    public static String fallback(String route)
    {
        if (route == null || route.isBlank())
        {
            return SystemModuleEnum.OTHER.getCode();
        }
        String[] segments = route.split("/");
        String module = segments.length > 1 ? segments[1] : ROOT_MODULE;
        return module.length() > MAX_MODULE_LENGTH ? SystemModuleEnum.OTHER.getCode() : module;
    }
}
