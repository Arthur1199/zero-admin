package com.zero.framework.statistics;

import com.zero.common.core.domain.AjaxResult;
import com.zero.common.core.domain.entity.SysUser;
import com.zero.common.core.domain.model.LoginUser;
import com.zero.common.exception.ServiceException;
import com.zero.framework.aspectj.RequiresPermissionsAspect;
import com.zero.framework.web.service.PermissionService;
import com.zero.framework.web.exception.GlobalExceptionHandler;
import com.zero.system.domain.statistics.StatEvent;
import com.zero.system.service.impl.StatisticsQueryService;
import com.zero.web.controller.monitor.StatisticsController;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/16 20:46
 * 说明: 真实MVC与项目权限切面回归，验证接口模板、业务码及统一异常的采集分类。
 */
class StatisticsWebTest
{
    /** 不存在的接口和资源均返回HTTP 404、业务码404，并保留失败统计路径。 */
    @Test void missingEndpointsReturnHttp404() throws Exception
    {
        StatisticsPipeline pipeline=mock(StatisticsPipeline.class);
        AspectJProxyFactory advice=new AspectJProxyFactory(new GlobalExceptionHandler()); advice.addAspect(new StatisticsAspect());
        var mvc=MockMvcBuilders.standaloneSetup(new MissingResourceController()).addInterceptors(new StatisticsMvc())
            .setControllerAdvice(advice.getProxy(),new StatisticsMvc())
            .addFilters(new StatisticsFilter(new StatisticsProperties(),pipeline,StatisticsTestSwitch.enabled())).build();
        mvc.perform(get("/missing-endpoint").queryParam("probe","ignored"))
            .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value(404));
        mvc.perform(get("/missing-resource"))
            .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value(404));
        ArgumentCaptor<StatEvent> events=ArgumentCaptor.forClass(StatEvent.class);
        verify(pipeline,times(2)).publish(events.capture());
        assertThat(events.getAllValues()).extracting(StatEvent::getOutcome).containsExactly("HTTP","HTTP");
        assertThat(events.getAllValues()).extracting(StatEvent::getRoute).containsExactly("/missing-endpoint","/missing-resource");
    }

    /** 模拟Spring静态资源处理器报告资源不存在。 */
    @RestController
    static class MissingResourceController
    {
        /** 触发资源不存在分支以验证全局异常映射。 */
        @GetMapping("/missing-resource")
        public void missingResource() throws org.springframework.web.servlet.resource.NoResourceFoundException
        {
            throw new org.springframework.web.servlet.resource.NoResourceFoundException(org.springframework.http.HttpMethod.GET,"missing-resource");
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 验证Advice不改变返回结构，原统一异常处理器经统计切面后仍保持原语义。
     * @return 校验接口模板及成功、业务失败、系统异常分类
     * 返回值: void
     * 返回值说明: 校验接口模板及成功、业务失败、系统异常分类
     */
    @Test void mvcOutcomes() throws Exception
    {
        StatisticsProperties p=new StatisticsProperties(); StatisticsPipeline pipeline=mock(StatisticsPipeline.class);
        AspectJProxyFactory advice=new AspectJProxyFactory(new GlobalExceptionHandler()); advice.addAspect(new StatisticsAspect());
        var mvc=MockMvcBuilders.standaloneSetup(new DemoController()).addInterceptors(new StatisticsMvc())
            .setControllerAdvice(advice.getProxy(),new StatisticsMvc()).addFilters(new StatisticsFilter(p,pipeline,StatisticsTestSwitch.enabled())).build();
        mvc.perform(get("/statistics-test/items/123")).andExpect(status().isOk()).andExpect(jsonPath("$.code").value(200));
        mvc.perform(get("/statistics-test/business")).andExpect(status().isOk()).andExpect(jsonPath("$.code").value(500));
        mvc.perform(get("/statistics-test/system")).andExpect(status().isOk()).andExpect(jsonPath("$.code").value(500));
        mvc.perform(get("/statistics-test/nested")).andExpect(status().isOk());
        mvc.perform(get("/statistics-test/fractional")).andExpect(status().isOk());
        ArgumentCaptor<StatEvent> events=ArgumentCaptor.forClass(StatEvent.class); verify(pipeline,times(5)).publish(events.capture());
        assertThat(events.getAllValues()).extracting(StatEvent::getOutcome).containsExactly("SUCCESS","BUSINESS","SYSTEM","UNKNOWN","UNKNOWN");
        assertThat(events.getAllValues().get(0).getRoute()).isEqualTo("/statistics-test/items/{id}");
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 验证统计权限独立于在线会话权限，无权限时不得执行查询。
     * @return 校验菜单只读权限边界
     * 返回值: void
     * 返回值说明: 校验菜单只读权限边界
     */
    @Test void menuPermission()
    {
        StatisticsQueryService service=mock(StatisticsQueryService.class); when(service.overview()).thenReturn(Map.of());
        RequiresPermissionsAspect aspect=new RequiresPermissionsAspect(); ReflectionTestUtils.setField(aspect,"permissionService",new PermissionService());
        AspectJProxyFactory factory=new AspectJProxyFactory(new StatisticsController(service)); factory.addAspect(aspect); StatisticsController controller=factory.getProxy();
        SysUser account=new SysUser(); account.setUserId(2L); account.setUserName("statistics-viewer"); LoginUser user=new LoginUser(account,Set.of("monitor:statistics:list"));
        org.springframework.web.context.request.RequestContextHolder.setRequestAttributes(new org.springframework.web.context.request.ServletRequestAttributes(new org.springframework.mock.web.MockHttpServletRequest()));
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user,null,List.of()));
        try
        {
            controller.overview(); verify(service).overview(); clearInvocations(service);
            user.setPermissions(Set.of("monitor:online:list"));
            assertThatThrownBy(controller::overview).isInstanceOf(AccessDeniedException.class); verifyNoInteractions(service);
        }
        finally { SecurityContextHolder.clearContext(); org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes(); }
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 验证登录最终返回才成功，令牌内容不会进入统计事件。
     * @return 验证登录切面关联用户及失败分类
     * 返回值: void
     * 返回值说明: 验证登录切面关联用户及失败分类
     */
    @Test void loginAspect() throws Exception
    {
        var request=new org.springframework.mock.web.MockHttpServletRequest("POST","/login");
        StatisticsContext context=new StatisticsContext(); context.event.setLogin(true); request.setAttribute(StatisticsContext.ATTRIBUTE,context);
        org.springframework.web.context.request.RequestContextHolder.setRequestAttributes(new org.springframework.web.context.request.ServletRequestAttributes(request));
        var tokens=mock(com.zero.framework.web.service.TokenService.class);
        when(tokens.createToken(any(LoginUser.class))).thenReturn("isolated-value-never-persisted");
        AspectJProxyFactory tokenFactory=new AspectJProxyFactory(tokens); tokenFactory.addAspect(new StatisticsAspect());
        com.zero.framework.web.service.TokenService tokenProxy=tokenFactory.getProxy();
        SysUser account=new SysUser(); account.setUserId(2L);
        LoginUser user=new LoginUser(2L,1L,account,Set.of());
        var login=mock(com.zero.framework.web.service.SysLoginService.class);
        when(login.login("ok","p","c","u","g")).thenAnswer(invocation->tokenProxy.createToken(user));
        when(login.login("bad","p","c","u","g")).thenThrow(new ServiceException("invalid"));
        AspectJProxyFactory loginFactory=new AspectJProxyFactory(login); loginFactory.addAspect(new StatisticsAspect());
        com.zero.framework.web.service.SysLoginService loginProxy=loginFactory.getProxy();
        try
        {
            loginProxy.login("ok","p","c","u","g");
            assertThat(context.event.isLoginSuccess()).isTrue(); assertThat(context.event.getUserId()).isEqualTo(2L);
            assertThat(context.event.getPayload()).doesNotContain("isolated-value-never-persisted");
            context.event.setLoginSuccess(false);
            assertThatThrownBy(()->loginProxy.login("bad","p","c","u","g")).isInstanceOf(ServiceException.class);
            assertThat(context.event.isLoginSuccess()).isFalse(); assertThat(context.failure).isEqualTo("BUSINESS");
        }
        finally { org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes(); }
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 仅供隔离MVC测试的接口，不注册进实际应用。
     */
    @RestController
    static class DemoController
    {
        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/11/16 22:02
         * 说明: 非整数状态码不能被截断为成功码。
         * @return 测试响应
         * 返回值: Map<String,Object>
         * 返回值说明: 仅供响应边界测试
         */
        @GetMapping("/statistics-test/fractional") public Map<String,Object> fractional() { return Map.of("code",200.5); }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 22:02
     * 说明: 缺少顶层code的MVC响应不能因为data中的code=200而计为成功。
     * @return 仅含业务data的测试响应
     * 返回值: Map<String,Object>
     * 返回值说明: 校验统计边界且不改变原业务输出
     */
        @GetMapping("/statistics-test/nested") public Map<String,Object> nested() { return Map.of("data",Map.of("code",200)); }

        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/11/16 20:46
         * 说明: 返回详情以验证路径参数被模板化。
         * @return 成功数据
         * 返回值: AjaxResult
         * 返回值说明: 成功数据
         */
        @GetMapping("/statistics-test/items/{id}") public AjaxResult item() { return AjaxResult.success(Map.of("value",1)); }
        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/11/16 20:46
         * 说明: 抛出可预期业务错误。
         * @return 由原异常处理器返回业务失败
         * 返回值: AjaxResult
         * 返回值说明: 由原异常处理器返回业务失败
         */
        @GetMapping("/statistics-test/business") public AjaxResult business() { throw new ServiceException("isolated business failure"); }
        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/11/16 20:46
         * 说明: 抛出程序错误。
         * @return 由原异常处理器返回系统失败
         * 返回值: AjaxResult
         * 返回值说明: 由原异常处理器返回系统失败
         */
        @GetMapping("/statistics-test/system") public AjaxResult system() { throw new IllegalStateException("isolated system failure"); }
    }
}
