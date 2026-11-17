package com.zero.system.service;
import java.util.*;
import org.springframework.stereotype.Service;
import com.zero.framework.websocket.*;
import com.zero.framework.web.service.TokenService;
import com.zero.common.enums.webSocket.WebSocketMessageType;
import com.zero.system.domain.*;
import com.zero.system.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/17
 * 说明: 系统通知接收人解析与跨节点发布，仅在线、不落库、不记录正文，发送权限允许全员通知
 */

@Slf4j @Service @RequiredArgsConstructor
public class SystemNoticeService {
    /** 单批查询及发布上限，控制SQL IN和Redis信封大小。 */
    private static final int BATCH_SIZE = 500;
    /** 环境隔离的已认证连接索引。 */ private final AdminWebSocketLease leases;
    /** 只读核对登录，不续期。 */ private final TokenService tokens;
    /** 账号及关系批量查询。 */ private final SystemNoticeMapper mapper;
    /** 统一Redis发送入口。 */ private final AdminWebSocketService webSocket;
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 汇总有效在线账号，批量查询最新账号状态，不返回凭证
     * @param filter 可选范围，发送时须已验证
     * @param keyword 可选搜索关键字
     * @return
     * 返回值: List<SystemNoticeUser>
     * 返回值说明: 去重且稳定排序的正常在线账号
     */

    public List<SystemNoticeUser> onlineUsers(SystemNoticeRequest filter, String keyword) {
        if (keyword != null && keyword.length() > 100) throw new IllegalArgumentException("搜索关键字过长");
        Set<Long> ids = new TreeSet<>();
        try {
            for (String session : leases.onlineSessions()) {
                var user = tokens.getWebSocketLoginUser(session);
                if (user != null && user.getUserId() != null && user.getUserId() > 0) ids.add(user.getUserId());
            }
        } catch (Exception e) {
            throw new com.zero.common.exception.ServiceException("在线账号查询失败，请检查Redis");
        }
        List<Long> all = new ArrayList<>(ids);
        List<SystemNoticeUser> result = new ArrayList<>();
        for (int i=0; i<all.size(); i+=BATCH_SIZE)
            result.addAll(mapper.selectTargets(all.subList(i, Math.min(all.size(),i+BATCH_SIZE)), filter, keyword == null ? null : keyword.trim()));
        result.sort(Comparator.comparing(SystemNoticeUser::getUserId));
        return result;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 读取全员通知所需部门角色及展示选项，不复用含数据范围限制的管理查询
     * @return
     * 返回值: Map<String,Object>
     * 返回值说明: 不包含角色权限明细或部门联系方式
     */

    public Map<String,Object> options() {
        List<Map<String,Object>> depts = new ArrayList<>();
        for (var dept : mapper.selectDepartments()) {
            if (dept == null || dept.getDeptId() == null || !"0".equals(dept.getStatus()) || !"0".equals(dept.getDelFlag())) continue;
            depts.add(Map.of("deptId",dept.getDeptId(),"parentId",dept.getParentId() == null ? 0L : dept.getParentId(),"deptName",Objects.toString(dept.getDeptName(),"")));
        }
        List<Map<String,Object>> roles = new ArrayList<>();
        for (var role : mapper.selectRoles()) {
            if (role != null && role.getRoleId() != null && "0".equals(role.getStatus()) && "0".equals(role.getDelFlag()))
                roles.add(Map.of("roleId",role.getRoleId(),"roleName",Objects.toString(role.getRoleName(),"")));
        }
        return Map.of("departments",depts,"roles",roles,
            "displayTypes",List.of("NOTIFICATION","MESSAGE","MESSAGE_BOX"),"messageLevels",List.of("NORMAL","WARNING","ERROR"));
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 验证通知并计算当前接收人数，不产生推送
     * @param request 页面通知参数
     * @return
     * 返回值: int
     * 返回值说明: 当前匹配账号数，无人在线则拒绝
     */

    public int preview(SystemNoticeRequest request) { return targets(request).size(); }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 重新解析接收人后分批发布，失败保留部分提交状态，不自动重发
     * @param request 已校验的通知请求
     * @param adminId 当前认证管理员ID，不能由浏览器指定
     * @return
     * 返回值: Map<String,Object>
     * 返回值说明: 消息ID集合及匹配、提交账号数量，不表示浏览器送达
     */

    public Map<String,Object> send(SystemNoticeRequest request, Long adminId) {
        if (adminId == null || adminId <= 0) throw new IllegalArgumentException("发送人无效");
        List<Long> ids = targets(request).stream().map(SystemNoticeUser::getUserId).toList();
        String content = request.getContentFormat() == SystemNoticeRequest.ContentFormat.HTML
            ? SystemNoticeContent.clean(request.getContent()) : request.getContent().trim();
        List<String> messages = new ArrayList<>();
        int submitted=0;
        for (int i=0; i<ids.size(); i+=BATCH_SIZE) {
            var batch=ids.subList(i,Math.min(i+BATCH_SIZE,ids.size()));
            try {
                messages.add(webSocket.sendToUsers(batch,WebSocketMessageVo.builder().msgType(WebSocketMessageType.SYSTEM_NOTICE)
                    .msgData(Map.of("title",request.getTitle().trim(),"content",content,"contentFormat",request.getContentFormat().name()))
                    .displayOptions(new WebSocketDisplayOptions(request.getDisplayType(),request.getMessageLevel(),request.getDuration()))
                    .fromAdminId(adminId).build()));
                submitted+=batch.size();
            } catch (Exception e) { log.warn("系统通知批次发布失败，已停止后续发布"); break; }
        }
        return Map.of("messageIds",messages,"matchedCount",ids.size(),"submittedCount",submitted,
            "status",submitted==ids.size()?"SUBMITTED":submitted==0?"FAILED":"PARTIAL", "targetType",request.getTargetType());
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 统一验证目标方式与文本，空目标不能广播
     * @param request 通知请求
     * @return
     * 返回值: List<SystemNoticeUser>
     * 返回值说明: 当前有效目标，无目标拒绝
     */

    private List<SystemNoticeUser> targets(SystemNoticeRequest request) {
        if (request == null) throw new IllegalArgumentException("通知不能为空");
        request.validate();
        var targets=onlineUsers(request,null);
        if (targets.isEmpty()) throw new IllegalArgumentException("没有匹配的在线账号");
        return targets;
    }
}
