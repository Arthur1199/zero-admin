package com.zero.system.service;
import java.util.*;
import java.util.stream.*;
import com.zero.system.domain.*;
import com.zero.system.mapper.*;
import com.zero.framework.websocket.*;
import com.zero.framework.web.service.TokenService;
import com.zero.common.core.domain.model.LoginUser;
import com.zero.common.core.domain.entity.SysUser;
import com.zero.common.enums.webSocket.*;
import org.junit.jupiter.api.Test;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.assertj.core.api.Assertions.*;
/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/17
 * 说明: 系统通知目标隔离、动态配置及批次失败回归，不操作真实用户和数据库
 */
class SystemNoticeTest {
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 编辑器图文在发布前清理，保留安全图片链接，旧纯文本格式默认值保持TEXT
     * @return
     * 返回值: void
     * 返回值说明: 不把攻击属性或图片文件内容发布到Redis
     */
    @Test void richContentIsSanitizedBeforePublishing() {
        when(leases.onlineSessions()).thenReturn(Set.of("rich-session"));
        when(tokens.getWebSocketLoginUser("rich-session")).thenReturn(login(2L));
        when(mapper.selectTargets(anyCollection(),any(),isNull())).thenReturn(List.of(user(2L)));
        var request=request();
        assertThat(request.getContentFormat()).isEqualTo(SystemNoticeRequest.ContentFormat.TEXT);
        request.setContentFormat(SystemNoticeRequest.ContentFormat.HTML);
        request.setContent("<p><strong>图文通知</strong></p><img src='/profile/test.png' onerror='alert(1)'><script>alert(1)</script>");
        when(ws.sendToUsers(anyCollection(),any())).thenReturn("rich-message");
        assertThat(service.send(request,7L)).containsEntry("status","SUBMITTED");
        var message=org.mockito.ArgumentCaptor.forClass(WebSocketMessageVo.class);
        verify(ws).sendToUsers(eq(List.of(2L)),message.capture());
        var data=(Map<?,?>)message.getValue().getMsgData();
        assertThat(data.get("contentFormat")).isEqualTo("HTML");
        assertThat((String)data.get("content")).contains("<strong>","/profile/test.png").doesNotContain("onerror","<script");
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 成功、部分失败和失败文案由服务端msg返回，发布数据继续放data
     * @return
     * 返回值: void
     * 返回值说明: 前端只需按状态选择样式，无需拼接结果文案
     */
    @Test void sendResponseSeparatesMessageAndBusinessData() {
        var sender = mock(SystemNoticeService.class);
        var controller = spy(new com.zero.web.controller.systemTools.SystemNoticeController(sender));
        doReturn(7L).when(controller).getUserId();
        var request = request();
        for (String status : List.of("SUBMITTED", "PARTIAL", "FAILED")) {
            var result = Map.<String,Object>of("status", status, "submittedCount", 2);
            when(sender.send(request,7L)).thenReturn(result);
            var response = controller.send(request);
            assertThat(response.get("code")).isEqualTo(200);
            assertThat(response.get("data")).isSameAs(result);
            String message = (String) response.get("msg");
            if ("SUBMITTED".equals(status)) assertThat(message).isEqualTo("系统通知发送成功");
            else if ("PARTIAL".equals(status)) assertThat(message).contains("部分发送成功", "2", "请勿直接重复发送");
            else assertThat(message).contains("发送失败");
        }
    }
    /** 已认证在线索引隔离替身。 */ private final AdminWebSocketLease leases=mock(AdminWebSocketLease.class);
    /** 登录会话只读替身。 */ private final TokenService tokens=mock(TokenService.class);
    /** 批量关系查询替身。 */ private final SystemNoticeMapper mapper=mock(SystemNoticeMapper.class);
    /** 统一发布替身。 */ private final AdminWebSocketService ws=mock(AdminWebSocketService.class);
    /** 隔离业务实例。 */ private final SystemNoticeService service=new SystemNoticeService(leases,tokens,mapper,ws);
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 空目标、混合目标、展示控制事件和非法时长均拒绝
     * @return
     * 返回值: void
     * 返回值说明: 不产生广播或通知
     */
    @Test void rejectsInvalidTargetsAndDisplay() {
        var request=request(); request.setTargetType(SystemNoticeRequest.TargetType.USERS);
        assertThatThrownBy(request::validate).isInstanceOf(IllegalArgumentException.class);
        request.setUserIds(Set.of(-1L));
        assertThatThrownBy(request::validate).isInstanceOf(IllegalArgumentException.class);
        request.setUserIds(Set.of(2L)); request.setDeptIds(Set.of(1L));
        assertThatThrownBy(request::validate).isInstanceOf(IllegalArgumentException.class);
        request.setDeptIds(Set.of()); request.setDuration(1);
        assertThatThrownBy(request::validate).isInstanceOf(IllegalArgumentException.class);
        request.setDuration(0); request.validate();
        request.setDisplayType(WebSocketDisplayType.NONE);
        assertThatThrownBy(request::validate).isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(ws);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 无有效连接时不能查询全表或回退为广播
     * @return
     * 返回值: void
     * 返回值说明: 空目标拒绝
     */
    @Test void noOnlineUsersNeverBroadcasts() {
        when(leases.onlineSessions()).thenReturn(Set.of());
        assertThatThrownBy(()->service.send(request(),1L)).hasMessageContaining("没有匹配");
        verifyNoInteractions(mapper,ws);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 同账号会话去重，失效会话跳过；预检查不发布，发送复制动态参数和真实来源
     * @return
     * 返回值: void
     * 返回值说明: 不调用登录续期或广播方法
     */
    @Test void deduplicatesAndPublishesValidatedDisplay() {
        when(leases.onlineSessions()).thenReturn(Set.of("a","b","expired"));
        when(tokens.getWebSocketLoginUser("a")).thenReturn(login(2L));
        when(tokens.getWebSocketLoginUser("b")).thenReturn(login(2L));
        when(mapper.selectTargets(anyCollection(),any(),isNull())).thenReturn(List.of(user(2L)));
        var request=request(); request.setDuration(0); request.setMessageLevel(WebSocketMessageLevel.WARNING);
        assertThat(service.preview(request)).isEqualTo(1); verifyNoInteractions(ws);
        when(ws.sendToUsers(anyCollection(),any())).thenReturn("isolated-message");
        assertThat(service.send(request,7L)).containsEntry("submittedCount",1).containsEntry("status","SUBMITTED");
        verify(ws).sendToUsers(eq(List.of(2L)),argThat(message -> message.getMsgType()==WebSocketMessageType.SYSTEM_NOTICE
            && message.getDisplayOptions().duration()==0 && message.getDisplayOptions().messageLevel()==WebSocketMessageLevel.WARNING
            && message.getFromAdminId()==7L));
        verify(tokens,never()).verifyToken(any()); verify(ws,never()).broadcast(any());
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 超过单批上限时第二批失败，只报告已提交范围，不自动重发
     * @return
     * 返回值: void
     * 返回值说明: 大目标集合按500分批
     */
    @Test void partialFailureDoesNotRetrySubmittedBatch() {
        Set<String> sessions=IntStream.rangeClosed(1,501).mapToObj(String::valueOf).collect(Collectors.toSet());
        when(leases.onlineSessions()).thenReturn(sessions);
        when(tokens.getWebSocketLoginUser(anyString())).thenAnswer(inv->login(Long.valueOf(inv.getArgument(0))));
        when(mapper.selectTargets(anyCollection(),any(),isNull())).thenAnswer(inv->((Collection<Long>)inv.getArgument(0)).stream().map(this::user).toList());
        when(ws.sendToUsers(anyCollection(),any())).thenReturn("batch-one").thenThrow(new IllegalStateException("isolated-failure"));
        assertThat(service.send(request(),7L)).containsEntry("status","PARTIAL").containsEntry("matchedCount",501).containsEntry("submittedCount",500);
        verify(ws,times(2)).sendToUsers(anyCollection(),any()); verify(ws,never()).broadcast(any());
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: MyBatis真实解析四种范围及部门下级开关，SQL只使用占位符；不代表真实数据库执行
     * @return
     * 返回值: void
     * 返回值说明: 角色EXISTS去重，空在线ID使用恒假条件
     */
    @Test void parsesAllTargetSqlSafely() throws Exception {
        var config=new org.apache.ibatis.session.Configuration();
        String resource="mapper/system/SystemNoticeMapper.xml";
        try(var input=getClass().getClassLoader().getResourceAsStream(resource)) {
            new XMLMapperBuilder(input,config,resource,config.getSqlFragments()).parse();
        }
        var query=config.getMappedStatement("com.zero.system.mapper.SystemNoticeMapper.selectTargets");
        for(var type:SystemNoticeRequest.TargetType.values()) {
            var r=request();r.setTargetType(type);r.setUserIds(Set.of(2L));r.setDeptIds(Set.of(3L));r.setRoleIds(Set.of(4L));
            var args=new HashMap<String,Object>();args.put("onlineIds",List.of(2L));args.put("filter",r);args.put("keyword","' isolated");
            var sql=query.getBoundSql(args).getSql();
            assertThat(sql).contains("u.user_id in","?").doesNotContain("' isolated");
            if(type==SystemNoticeRequest.TargetType.DEPT) {
                assertThat(sql).contains("find_in_set");r.setIncludeChildren(false);
                assertThat(query.getBoundSql(args).getSql()).doesNotContain("find_in_set");
            }
            if(type==SystemNoticeRequest.TargetType.ROLE) assertThat(sql).contains("exists","sys_user_role");
        }
        var args=new HashMap<String,Object>();args.put("onlineIds",List.of());args.put("filter",null);
        assertThat(query.getBoundSql(args).getSql()).contains("and 1=0");
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 生成纯测试请求
     * @return
     * 返回值: SystemNoticeRequest
     * 返回值说明: 不携带真实业务信息
     */
    private SystemNoticeRequest request() { var r=new SystemNoticeRequest();r.setTitle("测试标题");r.setContent("纯文本");r.setTargetType(SystemNoticeRequest.TargetType.ALL);return r; }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 创建隔离用户
     * @param id 测试ID
     * @return
     * 返回值: SystemNoticeUser
     * 返回值说明: 最小候选字段
     */
    private SystemNoticeUser user(Long id) { var u=new SystemNoticeUser();u.setUserId(id);return u; }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 创建隔离会话模型
     * @param id 测试账号ID
     * @return
     * 返回值: LoginUser
     * 返回值说明: 不包含Token
     */
    private LoginUser login(Long id) { return new LoginUser(id,1L,new SysUser(id),Set.of()); }
}
