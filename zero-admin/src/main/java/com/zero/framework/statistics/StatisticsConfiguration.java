package com.zero.framework.statistics;

import jakarta.servlet.DispatcherType;
import com.zero.system.service.IStatisticsSwitchService;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.*;
import org.springframework.web.servlet.config.annotation.*;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/16 20:46
 * 说明: 独立注册统计Filter和拦截器，不改动现有Security及业务Controller。
 */
@Configuration
public class StatisticsConfiguration implements WebMvcConfigurer
{
    /** 独立采集配置。 */
    private final StatisticsProperties properties;
    /** 持久化事件管道。 */
    private final StatisticsPipeline pipeline;
    /** MVC接口识别组件。 */
    private final StatisticsMvc mvc;
    /** MySQL后台参数开关，与YAML技术参数分离。 */
    private final IStatisticsSwitchService switchService;
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 注入统计框架组件。
     * @param properties 统计配置
     * @param pipeline 持久化管道
     * @param mvc 接口识别器
     * @param switchService 后台参数开关
     */
    public StatisticsConfiguration(StatisticsProperties properties, StatisticsPipeline pipeline, StatisticsMvc mvc, IStatisticsSwitchService switchService)
    { this.properties=properties; this.pipeline=pipeline; this.mvc=mvc; this.switchService=switchService; }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 在Security之前注册采集，不将同一Filter重复注册进安全链。
     * @return 支持异步请求的独立Filter
     * 返回值: FilterRegistrationBean<StatisticsFilter>
     * 返回值说明: 支持异步请求的独立Filter
     */
    @Bean
    public FilterRegistrationBean<StatisticsFilter> statisticsFilter()
    {
        FilterRegistrationBean<StatisticsFilter> bean=new FilterRegistrationBean<>(new StatisticsFilter(properties,pipeline,switchService));
        bean.setOrder(-200); bean.setAsyncSupported(true); bean.setDispatcherTypes(DispatcherType.REQUEST,DispatcherType.ASYNC,DispatcherType.ERROR);
        bean.addUrlPatterns("/*"); return bean;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 先于原业务拦截器识别路由及用户，不改变校验顺序。
     * @param registry MVC拦截器注册器
     * @return 注册只读识别器
     * 返回值: void
     * 返回值说明: 注册只读识别器
     */
    @Override public void addInterceptors(InterceptorRegistry registry) { registry.addInterceptor(mvc).order(Integer.MIN_VALUE); }
}
