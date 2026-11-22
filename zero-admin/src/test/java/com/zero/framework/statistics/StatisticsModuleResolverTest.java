package com.zero.framework.statistics;

import com.zero.common.annotation.SystemModule;
import com.zero.common.enums.SystemModuleEnum;
import org.junit.jupiter.api.Test;
import org.springframework.web.method.HandlerMethod;
import static org.assertj.core.api.Assertions.*;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/22 18:45
 * 说明: 统计模块解析回归，验证注解优先、方法级覆盖及首段兜底
 */
class StatisticsModuleResolverTest
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/22 18:45
     * 说明: 仅在类上声明注解时，该类所有接口都归入同一个模块
     * @return
     * 返回值: void
     * 返回值说明: 断言解析结果为类级模块
     */
    @Test void classAnnotation() throws Exception
    {
        HandlerMethod method = handler(new ClassAnnotatedController(), "list");
        assertThat(StatisticsModuleResolver.resolve(method)).isEqualTo(SystemModuleEnum.REPORT.getCode());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/22 18:45
     * 说明: 方法级声明覆盖类级声明，便于登录页接口与后台管理接口分开归类
     * @return
     * 返回值: void
     * 返回值说明: 断言方法级模块优先
     */
    @Test void methodOverridesClass() throws Exception
    {
        HandlerMethod override = handler(new ClassAnnotatedController(), "loginPage");
        assertThat(StatisticsModuleResolver.resolve(override)).isEqualTo(SystemModuleEnum.AUTH.getCode());
        HandlerMethod normal = handler(new ClassAnnotatedController(), "detail");
        assertThat(StatisticsModuleResolver.resolve(normal)).isEqualTo(SystemModuleEnum.REPORT.getCode());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/22 18:45
     * 说明: 未声明注解时返回null，由调用方退回路由首段规则
     * @return
     * 返回值: void
     * 返回值说明: 断言无注解时不产生模块值
     */
    @Test void unannotatedReturnsNull() throws Exception
    {
        assertThat(StatisticsModuleResolver.resolve(handler(new PlainController(), "list"))).isNull();
        assertThat(StatisticsModuleResolver.resolve(null)).isNull();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/22 18:45
     * 说明: 首段兜底保持原行为：多段取首段、单段归root、超长首段归other
     * @return
     * 返回值: void
     * 返回值说明: 断言兜底模块值与存储长度约束
     */
    @Test void fallbackByFirstSegment()
    {
        assertThat(StatisticsModuleResolver.fallback("/system/user/{userId}")).isEqualTo("system");
        assertThat(StatisticsModuleResolver.fallback("/monitor/online/list")).isEqualTo("monitor");
        assertThat(StatisticsModuleResolver.fallback("/")).isEqualTo("root");
        assertThat(StatisticsModuleResolver.fallback("/getInfo")).isEqualTo("getInfo");
        assertThat(StatisticsModuleResolver.fallback(null)).isEqualTo(SystemModuleEnum.OTHER.getCode());
        assertThat(StatisticsModuleResolver.fallback(" ")).isEqualTo(SystemModuleEnum.OTHER.getCode());
        assertThat(StatisticsModuleResolver.fallback("/" + "a".repeat(81) + "/list")).isEqualTo(SystemModuleEnum.OTHER.getCode());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/22 18:45
     * 说明: 为测试控制器构建MVC处理方法
     * @param bean 控制器实例
     * @param name 方法名
     * @return
     * 返回值: HandlerMethod
     * 返回值说明: 可直接用于模块解析的处理方法包装
     */
    private static HandlerMethod handler(Object bean,String name) throws NoSuchMethodException
    {
        return new HandlerMethod(bean, bean.getClass().getDeclaredMethod(name));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/22 18:45
     * 说明: 类级声明报表模块、一个方法覆盖为认证模块的测试控制器
     */
    @SystemModule(SystemModuleEnum.REPORT)
    static class ClassAnnotatedController
    {
        /** 普通接口沿用类级模块。 */
        public void list() { }

        /** 详情接口沿用类级模块。 */
        public void detail() { }

        /** 登录页接口覆盖为认证模块。 */
        @SystemModule(SystemModuleEnum.AUTH)
        public void loginPage() { }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/22 18:45
     * 说明: 未声明模块注解的测试控制器
     */
    static class PlainController
    {
        /** 无注解接口。 */
        public void list() { }
    }
}
