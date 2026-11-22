package com.zero.framework.statistics;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/22 18:45
 * 说明: 模块检查器装配回归，验证同类型多bean时仍能按名称注入业务映射
 */
class StatisticsModuleVerifierTest
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/22 18:45
     * 说明: Actuator的controllerEndpointHandlerMapping与业务映射同类型共存时，构造器必须按名称选中业务映射
     * @return
     * 返回值: void
     * 返回值说明: 断言装配成功且不抛NoUniqueBeanDefinitionException
     */
    @Test void resolvesBusinessMappingAmongTwoCandidates()
    {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext())
        {
            context.registerBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class, () -> mock(RequestMappingHandlerMapping.class));
            context.registerBean("controllerEndpointHandlerMapping", RequestMappingHandlerMapping.class, () -> mock(RequestMappingHandlerMapping.class));
            context.register(StatisticsModuleVerifier.class);
            context.refresh();
            assertThat(context.getBean(StatisticsModuleVerifier.class)).isNotNull();
        }
    }
}
