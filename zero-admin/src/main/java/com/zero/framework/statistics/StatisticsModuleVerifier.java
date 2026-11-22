package com.zero.framework.statistics;

import java.util.Set;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/22 18:45
 * 说明: 启动时检查控制器是否声明统计模块，只告警不阻断启动
 */
@Component
public class StatisticsModuleVerifier implements ApplicationRunner
{
    /**
     * 日志记录器，用于输出未声明模块的控制器清单
     */
    private static final Logger log = LoggerFactory.getLogger(StatisticsModuleVerifier.class);

    /**
     * 后台自有控制器包前缀，只检查该范围，框架自带端点不需要声明统计模块
     */
    private static final String ADMIN_CONTROLLER_PACKAGE = "com.zero.web";

    /**
     * MVC映射注册表，仅只读遍历已有映射，不新增或修改任何映射
     */
    private final RequestMappingHandlerMapping mapping;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/22 18:45
     * 说明: 注入业务MVC映射注册表，用于启动时枚举已注册的处理方法
     * @param mapping MVC请求映射注册表，按bean名称限定，避免与Actuator的controllerEndpointHandlerMapping同类型冲突
     */
    public StatisticsModuleVerifier(@Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping mapping)
    {
        this.mapping = mapping;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/22 18:45
     * 说明: 应用启动完成后汇总 com.zero.web 下未声明@SystemModule的控制器并输出一次告警
     * @param args 启动参数，不参与检查
     * @return
     * 返回值: void
     * 返回值说明: 仅在日志中提示，运行统计仍按路由首段兜底，不影响业务
     */
    @Override
    public void run(ApplicationArguments args)
    {
        Set<String> missing = new TreeSet<>();
        for (HandlerMethod method : mapping.getHandlerMethods().values())
        {
            // 只检查后台自有控制器，BasicErrorController、SpringDoc等框架端点不要求声明模块。
            if (!method.getBeanType().getPackageName().startsWith(ADMIN_CONTROLLER_PACKAGE))
            {
                continue;
            }
            if (StatisticsModuleResolver.resolve(method) == null)
            {
                missing.add(method.getBeanType().getSimpleName());
            }
        }
        if (!missing.isEmpty())
        {
            log.warn("以下控制器未声明@SystemModule，运行统计将按路由首段归类: {}", missing);
        }
    }
}
