package com.zero.framework.statistics;

import com.zero.system.domain.statistics.StatEvent;
import com.zero.system.domain.statistics.StatisticsQuery;
import com.zero.system.mapper.StatisticsMapper;
import com.zero.system.service.impl.StatisticsQueryService;
import jakarta.servlet.*;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.*;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/16 20:46
 * 说明: 统计采集隔离回归，验证业务码、响应不变、排除规则及异步唯一完成。
 */
class StatisticsCaptureTest
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 验证HTTP200中的业务失败不能计为成功，响应正文保持不变。
     * @return 断言结果分类和响应一致
     * 返回值: void
     * 返回值说明: 断言结果分类和响应一致
     */
    @Test void securityJsonAndBusinessCode() throws Exception
    {
        StatisticsProperties p=new StatisticsProperties();
        StatisticsPipeline pipeline=mock(StatisticsPipeline.class);
        MockHttpServletRequest request=new MockHttpServletRequest("GET","/system/user/list");
        MockHttpServletResponse response=new MockHttpServletResponse();
        new StatisticsFilter(p,pipeline,StatisticsTestSwitch.enabled()).doFilter(request,response,(req,res)-> {
            res.setContentType("application/json"); res.getWriter().write("{\"code\":401,\"msg\":\"denied\"}");
        });
        ArgumentCaptor<StatEvent> event=ArgumentCaptor.forClass(StatEvent.class); verify(pipeline).publish(event.capture());
        assertThat(event.getValue().getOutcome()).isEqualTo("AUTH");
        assertThat(response.getContentAsString()).isEqualTo("{\"code\":401,\"msg\":\"denied\"}");
        assertThat(event.getValue().getPayload()).doesNotContain("denied","Authorization");
        assertThat(StatisticsResult.classify(200,500,false,null,true)).isEqualTo("BUSINESS");
        assertThat(StatisticsResult.classify(200,null,false,null,true)).isEqualTo("UNKNOWN");
        assertThat(StatisticsResult.classify(200,null,false,null,false)).isEqualTo("SUCCESS");
        assertThat(StatisticsResult.classify(200,200,true,null,true)).isEqualTo("SYSTEM");
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 验证二进制下载透传、请求失败及自身排除不干扰业务。
     * @return 断言流式输出与排除规则
     * 返回值: void
     * 返回值说明: 断言流式输出与排除规则
     */
    @Test void binaryAndExclusions() throws Exception
    {
        StatisticsProperties p=new StatisticsProperties(); StatisticsPipeline pipeline=mock(StatisticsPipeline.class);
        StatisticsFilter filter=new StatisticsFilter(p,pipeline,StatisticsTestSwitch.enabled());
        for (String uri:List.of("/monitor/statistics/overview","/actuator/health","/profile/a.png"))
            filter.doFilter(new MockHttpServletRequest("GET",uri),new MockHttpServletResponse(),(req,res)->{});
        filter.doFilter(new MockHttpServletRequest("OPTIONS","/system/user/list"),new MockHttpServletResponse(),(req,res)->{});
        verifyNoInteractions(pipeline);
        byte[] bytes={0,1,2,(byte)255}; MockHttpServletResponse response=new MockHttpServletResponse();
        filter.doFilter(new MockHttpServletRequest("GET","/download"),response,(req,res)-> {res.setContentType("application/octet-stream");res.getOutputStream().write(bytes);});
        assertThat(response.getContentAsByteArray()).isEqualTo(bytes);
        verify(pipeline).publish(argThat(event->"SUCCESS".equals(event.getOutcome())));
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 验证异步分派和完成只提交一个事件。
     * @return 断言异步完成前不提交，完成后唯一提交
     * 返回值: void
     * 返回值说明: 断言异步完成前不提交，完成后唯一提交
     */
    @Test void asyncCompletesOnce() throws Exception
    {
        StatisticsProperties p=new StatisticsProperties(); StatisticsPipeline pipeline=mock(StatisticsPipeline.class);
        StatisticsFilter filter=new StatisticsFilter(p,pipeline,StatisticsTestSwitch.enabled()); MockHttpServletRequest request=new MockHttpServletRequest("GET","/async");
        request.setAsyncSupported(true); MockHttpServletResponse response=new MockHttpServletResponse();
        filter.doFilter(request,response,(req,res)->req.startAsync(req,res));
        verifyNoInteractions(pipeline);
        filter.doFilter(request,response,(req,res)->{});
        request.getAsyncContext().complete();
        verify(pipeline,times(1)).publish(any());
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 验证日期跨度、分页上限与排序白名单，避免无限扫描及SQL注入。
     * @return 断言查询边界
     * 返回值: void
     * 返回值说明: 断言查询边界
     */
    @Test void queryBounds()
    {
        StatisticsQueryService service=new StatisticsQueryService(mock(StatisticsMapper.class),mock(StatisticsPipeline.class));
        StatisticsQuery query=new StatisticsQuery(); query.setBeginTime("2026-01-01"); query.setEndTime("2026-01-07"); query.setPageSize(10000);
        assertThat(service.parameters(query)).containsEntry("minute",true).containsEntry("limit",100);
        query.setEndTime("2026-01-08"); assertThat(service.parameters(query)).containsEntry("minute",false);
        query.setRanking("total desc; drop table x"); assertThatThrownBy(()->service.parameters(query)).hasMessage("不支持的排行类型");
        query.setRanking("calls"); query.setEndTime("2027-12-31"); assertThatThrownBy(()->service.parameters(query)).hasMessage("统计日期范围必须为1至366天");
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 22:02
     * 说明: 安全链JSON仅识别顶层整数code，不能将嵌套业务字段或数组内容误判为请求成功。
     * @return
     * 返回值: void
     * 返回值说明: 校验统计边界且不改变原业务输出
     */
    @Test void onlyTopLevelCodeCounts() throws Exception
    {
        String[][] cases={
            {"{\"data\":{\"code\":200},\"code\":401}","AUTH"},
            {"{\"data\":{\"code\":200}}","UNKNOWN"},
            {"[{\"code\":200}]","UNKNOWN"},
            {"{\"code\":\"200\"}","UNKNOWN"}
        };
        for (String[] item:cases)
        {
            StatisticsPipeline pipeline=mock(StatisticsPipeline.class);
            new StatisticsFilter(new StatisticsProperties(),pipeline,StatisticsTestSwitch.enabled()).doFilter(
                new MockHttpServletRequest("GET","/test"),new MockHttpServletResponse(),(req,res)-> {
                    res.setContentType("APPLICATION/JSON"); res.getWriter().write(item[0]);
                });
            verify(pipeline).publish(argThat(event->item[1].equals(event.getOutcome())));
        }
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 22:02
     * 说明: 响应reset允许切换字符和字节输出，不能继续使用已废弃的响应码或输出类型。
     * @return
     * 返回值: void
     * 返回值说明: 校验统计边界且不改变原业务输出
     */
    @Test void resetAllowsChangingOutputMode() throws Exception
    {
        StatisticsPipeline pipeline=mock(StatisticsPipeline.class); MockHttpServletResponse response=new MockHttpServletResponse();
        new StatisticsFilter(new StatisticsProperties(),pipeline,StatisticsTestSwitch.enabled()).doFilter(
            new MockHttpServletRequest("GET","/test"),response,(req,res)-> {
                res.setContentType("application/json"); res.getWriter().write("{\"code\":401}");
                res.reset(); res.setContentType("application/octet-stream"); res.getOutputStream().write(new byte[]{1,2,3});
            });
        assertThat(response.getContentAsByteArray()).containsExactly(1,2,3);
        verify(pipeline).publish(argThat(event->"SUCCESS".equals(event.getOutcome())));
        StatisticsPipeline second=mock(StatisticsPipeline.class); MockHttpServletResponse json=new MockHttpServletResponse();
        new StatisticsFilter(new StatisticsProperties(),second,StatisticsTestSwitch.enabled()).doFilter(
            new MockHttpServletRequest("GET","/test"),json,(req,res)-> {
                res.getOutputStream().write(new byte[]{1}); res.reset(); res.setContentType("application/json"); res.getWriter().write("{\"code\":200}");
            });
        assertThat(json.getContentAsString()).isEqualTo("{\"code\":200}");
        verify(second).publish(argThat(event->"SUCCESS".equals(event.getOutcome())));
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 22:02
     * 说明: 退出由安全过滤器处理时仍应归类到退出接口，并从原令牌读取结果提取用户ID。
     * @return
     * 返回值: void
     * 返回值说明: 校验统计边界且不改变原业务输出
     */
    @Test void logoutHasKnownRouteAndUser() throws Exception
    {
        var tokens=mock(com.zero.framework.web.service.TokenService.class);
        var user=new com.zero.common.core.domain.entity.SysUser(); user.setUserId(2L);
        var login=new com.zero.common.core.domain.model.LoginUser(2L,1L,user,Set.of()); login.setToken("isolated-never-store");
        when(tokens.getLoginUser(any())).thenReturn(login);
        var factory=new org.springframework.aop.aspectj.annotation.AspectJProxyFactory(tokens); factory.addAspect(new StatisticsAspect());
        com.zero.framework.web.service.TokenService proxy=factory.getProxy();
        StatisticsPipeline pipeline=mock(StatisticsPipeline.class);
        new StatisticsFilter(new StatisticsProperties(),pipeline,StatisticsTestSwitch.enabled()).doFilter(
            new MockHttpServletRequest("POST","/logout"),new MockHttpServletResponse(),(req,res)-> {
                proxy.getLoginUser((jakarta.servlet.http.HttpServletRequest)req);
                res.setContentType("application/json"); res.getWriter().write("{\"code\":200}");
            });
        ArgumentCaptor<StatEvent> captured=ArgumentCaptor.forClass(StatEvent.class); verify(pipeline).publish(captured.capture());
        assertThat(captured.getValue().getRoute()).isEqualTo("/logout"); assertThat(captured.getValue().isLogin()).isFalse();
        assertThat(captured.getValue().getUserId()).isEqualTo(2L); assertThat(captured.getValue().getPayload()).doesNotContain("isolated-never-store");
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 22:02
     * 说明: 非法事件不能依赖数据库IGNORE截断后再参与去重，必须在任何SQL执行前拒绝。
     * @return
     * 返回值: void
     * 返回值说明: 校验统计边界且不改变原业务输出
     */
    @Test void malformedEventsNeverReachDatabase()
    {
        StatisticsMapper mapper=mock(StatisticsMapper.class);
        var store=new com.zero.system.service.impl.StatisticsStore(mapper);
        StatEvent event=new StatEvent(); event.setEventId("invalid"); event.setOutcome("SUCCESS"); event.setCompletedAt(System.currentTimeMillis());
        assertThatThrownBy(()->store.accept(event)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(()->store.fallback(event)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(()->store.accept(null)).isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(mapper);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 22:02
     * 说明: 错误派发不应将原业务接口覆盖成/error，已完成请求也不能再变更接口元数据。
     * @return
     * 返回值: void
     * 返回值说明: 原请求维度保持稳定，参数边界有清晰错误
     */
    @Test void errorDispatchKeepsOriginalRoute() throws Exception
    {
        MockHttpServletRequest request=new MockHttpServletRequest("GET","/error"); request.setDispatcherType(DispatcherType.ERROR);
        StatisticsContext context=new StatisticsContext(); context.event.setRoute("/original/{id}"); request.setAttribute(StatisticsContext.ATTRIBUTE,context);
        request.setAttribute(org.springframework.web.servlet.HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE,"/error");
        var method=new org.springframework.web.method.HandlerMethod(this,getClass().getDeclaredMethod("queryBounds"));
        new StatisticsMvc().preHandle(request,new MockHttpServletResponse(),method);
        assertThat(context.event.getRoute()).isEqualTo("/original/{id}");
        StatisticsQueryService service=new StatisticsQueryService(mock(StatisticsMapper.class),mock(StatisticsPipeline.class));
        assertThat(service.parameters(null)).containsEntry("limit",20);
        StatisticsQuery query=new StatisticsQuery(); query.setBeginTime("9999-12-31"); query.setEndTime("9999-12-31");
        assertThatThrownBy(()->service.parameters(query)).hasMessage("统计日期超出数据库支持范围");
    }

}
