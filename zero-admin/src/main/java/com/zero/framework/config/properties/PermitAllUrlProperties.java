package com.zero.framework.config.properties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.commons.lang3.RegExUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import com.zero.common.annotation.Anonymous;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 设置Anonymous注解允许匿名访问的url
 */
@Configuration
public class PermitAllUrlProperties implements InitializingBean, ApplicationContextAware
{
    /**
     * PATTERN字段
     */
    /** Spring MVC主请求映射Bean名称，避免Actuator端点映射器导致按类型获取Bean冲突 */
    private static final String REQUEST_MAPPING_HANDLER_MAPPING_BEAN_NAME = "requestMappingHandlerMapping";

    /**
     * Spring MVC主请求映射Bean名称，避免Actuator端点映射器导致按类型获取Bean冲突
     */
    private static final Pattern PATTERN = Pattern.compile("\\{(.*?)\\}");
    /**
     * applicationContext字段
     */

    private ApplicationContext applicationContext;
    /**
     * urls字段
     */

    private List<String> urls = new ArrayList<>();
    /**
     * ASTERISK字段
     */

    public String ASTERISK = "*";
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: after配置属性集合
     * @return
     * 返回值: void
     * 返回值说明：after配置属性集合
     */

    @Override
    public void afterPropertiesSet()
    {
        RequestMappingHandlerMapping mapping = applicationContext.getBean(REQUEST_MAPPING_HANDLER_MAPPING_BEAN_NAME,
            RequestMappingHandlerMapping.class);
        Map<RequestMappingInfo, HandlerMethod> map = mapping.getHandlerMethods();

        map.keySet().forEach(info -> {
            HandlerMethod handlerMethod = map.get(info);

            // 获取方法上边的注解 替代path variable 为 *
            Anonymous method = AnnotationUtils.findAnnotation(handlerMethod.getMethod(), Anonymous.class);
            Optional.ofNullable(method).ifPresent(anonymous -> addPermitAllUrl(info));

            // 获取类上边的注解, 替代path variable 为 *
            Anonymous controller = AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), Anonymous.class);
            Optional.ofNullable(controller).ifPresent(anonymous -> addPermitAllUrl(info));
        });
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 收集匿名访问路径，兼容Spring MVC旧版PatternsCondition和新版PathPatternsCondition
     * @param info 请求映射信息，来源于Spring MVC启动后的HandlerMapping
     * @return
     * 返回值: void
     * 返回值说明: 路径变量会替换为*后加入匿名访问列表
     */
    private void addPermitAllUrl(RequestMappingInfo info)
    {
        getPatterns(info).forEach(url -> urls.add(RegExUtils.replaceAll(url, PATTERN, ASTERISK)));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 获取请求路径集合，避免Spring Boot 3默认PathPatternParser模式下getPatternsCondition为空导致启动失败
     * @param info 请求映射信息
     * @return
     * 返回值: Set<String>
     * 返回值说明: 可用于Spring Security匹配的请求路径集合，无法识别时返回空集合
     */
    private Set<String> getPatterns(RequestMappingInfo info)
    {
        if (info.getPatternsCondition() != null)
        {
            return info.getPatternsCondition().getPatterns();
        }
        if (info.getPathPatternsCondition() != null)
        {
            return info.getPathPatternsCondition().getPatternValues();
        }
        return Collections.emptySet();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置ApplicationContext
     * @param context context参数
     * @return
     * 返回值: void
     * 返回值说明：设置ApplicationContext
     */

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException
    {
        this.applicationContext = context;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Urls
     * @return
     * 返回值: List<String>
     * 返回值说明：获取Urls
     */

    public List<String> getUrls()
    {
        return urls;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Urls
     * @param urls urls参数
     * @return
     * 返回值: void
     * 返回值说明：设置Urls
     */

    public void setUrls(List<String> urls)
    {
        this.urls = urls;
    }
}
