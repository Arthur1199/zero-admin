package com.zero.feign;

import com.zero.common.constant.ForwardHeaderConstants;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/6/28 02:52
 * 说明: 后台管理员跨服务调用上下文，集中表达 admin 调用 report 时需要透传的登录态、操作人和审计信息
 */
public class AdminForwardContext
{
    /**
     * 登录令牌请求头原始值
     */
    private String authorization;

    /**
     * 调用来源服务名
     */
    private String sourceService;

    /**
     * 后台操作用户ID
     */
    private String adminId;

    /**
     * 后台操作用户账号
     */
    private String adminName;

    /**
     * 后台操作用户是否为超级管理员，null 表示未登录或无法判断
     */
    private Boolean isAdmin;

    /**
     * 后台操作用户昵称
     */
    private String adminNickName;

    /**
     * 后台操作用户所属公司名称
     */
    private String adminCompanyName;

    /**
     * 当前登录用户的所有角色Id
     */
    private List<Long> adminRoleIds;

    /**
     * 客户端真实IP
     */
    private String clientIp;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 02:52
     * 说明: 将管理员转发上下文转换为 HTTP 请求头；中文字段统一 URL 编码，空字段不会写入
     * @return
     * 返回值: Map<String, String>
     * 返回值说明: 可直接写入 Feign 或 RestTemplate 的请求头集合
     */
    public Map<String, String> toHeaders()
    {
        Map<String, String> headers = new LinkedHashMap<>();
        addIfHasText(headers, ForwardHeaderConstants.SOURCE_SERVICE, sourceService);
        addIfHasText(headers, ForwardHeaderConstants.AUTHORIZATION, authorization);
        addIfHasText(headers, ForwardHeaderConstants.CLIENT_IP, clientIp);
        addIfHasText(headers, ForwardHeaderConstants.ADMIN_ID, adminId);
        addIfHasText(headers, ForwardHeaderConstants.ADMIN_NAME, encode(adminName));
        if (isAdmin != null)
        {
            headers.put(ForwardHeaderConstants.IS_ADMIN, String.valueOf(isAdmin));
        }
        addIfHasText(headers, ForwardHeaderConstants.ADMIN_NICK_NAME, encode(adminNickName));
        addIfHasText(headers, ForwardHeaderConstants.ADMIN_COMPANY_NAME, encode(adminCompanyName));
        addIfHasText(headers, ForwardHeaderConstants.ADMIN_ROLE_IDS, joinRoleIds(adminRoleIds));
        return headers;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 02:52
     * 说明: 写入非空请求头，避免空字符串覆盖下游默认值
     * @param headers 请求头集合
     * @param headerName 请求头名称
     * @param headerValue 请求头值
     * @return
     * 返回值: void
     * 返回值说明: 按需追加请求头
     */
    private void addIfHasText(Map<String, String> headers, String headerName, String headerValue)
    {
        if (StringUtils.hasText(headerName) && StringUtils.hasText(headerValue))
        {
            headers.put(headerName, headerValue);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 02:52
     * 说明: 对可能包含中文的请求头值做 UTF-8 编码，避免跨服务传输时出现乱码
     * @param value 请求头原始值
     * @return
     * 返回值: String
     * 返回值说明: URL 编码后的请求头值，空值返回空字符串
     */
    private String encode(String value)
    {
        return StringUtils.hasText(value) ? URLEncoder.encode(value, StandardCharsets.UTF_8) : "";
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 03:10
     * 说明: 将管理员角色ID列表转换为请求头字符串，多个角色使用英文逗号分隔
     * @param roleIds 管理员角色ID列表，可为空
     * @return
     * 返回值: String
     * 返回值说明: 可写入请求头的角色ID字符串
     */
    private String joinRoleIds(List<Long> roleIds)
    {
        if (roleIds == null || roleIds.isEmpty())
        {
            return "";
        }
        return roleIds.stream()
                .filter(roleId -> roleId != null)
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    void setAuthorization(String authorization)
    {
        this.authorization = authorization;
    }

    void setSourceService(String sourceService)
    {
        this.sourceService = sourceService;
    }

    void setAdminId(String adminId)
    {
        this.adminId = adminId;
    }

    void setAdminName(String adminName)
    {
        this.adminName = adminName;
    }

    void setAdmin(Boolean admin)
    {
        isAdmin = admin;
    }

    void setAdminNickName(String adminNickName)
    {
        this.adminNickName = adminNickName;
    }

    void setAdminCompanyName(String adminCompanyName)
    {
        this.adminCompanyName = adminCompanyName;
    }

    void setAdminRoleIds(List<Long> adminRoleIds)
    {
        this.adminRoleIds = adminRoleIds;
    }

    void setClientIp(String clientIp)
    {
        this.clientIp = clientIp;
    }
}
