package com.zero.dto;

import com.zero.constant.ReportHeaderConstants;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.springframework.util.StringUtils;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/6/28 02:55
 * 说明: 报表服务接收到的后台管理员上下文，统一解析 admin 传入的操作管理员、公司、角色和客户端IP信息
 */
@Getter
public class ReportAdminContext
{
    /**
     * 登录令牌请求头原始值
     */
    private String authorization = "";

    /**
     * 上游调用来源服务名
     */
    private String sourceService = "";

    /**
     * 后台操作用户ID
     */
    private String adminId = "";

    /**
     * 后台操作用户账号，已按 UTF-8 解码
     */
    private String adminName = "";

    /**
     * 后台操作用户是否为超级管理员
     */
    private boolean admin;

    /**
     * 后台操作用户昵称，已按 UTF-8 解码
     */
    private String adminNickName = "";

    /**
     * 后台操作用户所属公司，已按 UTF-8 解码
     */
    private String adminCompanyName = "";

    /**
     * 当前登录用户的所有角色Id
     */
    private List<Long> adminRoleIds = List.of();

    /**
     * 客户端真实IP
     */
    private String clientIp = "";

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 02:55
     * 说明: 从 HTTP 请求头解析后台管理员上下文；无请求时返回空上下文，避免定时任务或异步线程空指针
     * @param request HTTP 请求对象，可为空
     * @return
     * 返回值: ReportAdminContext
     * 返回值说明: 已解析并解码的后台管理员上下文
     */
    public static ReportAdminContext fromRequest(HttpServletRequest request)
    {
        ReportAdminContext context = new ReportAdminContext();
        if (request == null)
        {
            return context;
        }
        context.authorization = readHeader(request, ReportHeaderConstants.AUTHORIZATION);
        context.sourceService = readHeader(request, ReportHeaderConstants.SOURCE_SERVICE);
        context.adminId = readHeader(request, ReportHeaderConstants.ADMIN_ID);
        context.adminName = decode(readHeader(request, ReportHeaderConstants.ADMIN_NAME));
        context.admin = parseAdmin(readHeader(request, ReportHeaderConstants.IS_ADMIN));
        context.adminNickName = decode(readHeader(request, ReportHeaderConstants.ADMIN_NICK_NAME));
        context.adminCompanyName = decode(readHeader(request, ReportHeaderConstants.ADMIN_COMPANY_NAME));
        context.adminRoleIds = parseRoleIds(readHeader(request, ReportHeaderConstants.ADMIN_ROLE_IDS));
        context.clientIp = readHeader(request, ReportHeaderConstants.CLIENT_IP);
        return context;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 02:55
     * 说明: 将 report 接收到的管理员上下文重新转换为下游请求头，便于 report 继续调用其它服务时透传操作人
     * @param currentSourceService 当前调用方服务名，通常为 zero-report
     * @return
     * 返回值: Map<String, String>
     * 返回值说明: 可直接写入 Feign 请求模板的请求头集合
     */
    public Map<String, String> toForwardHeaders(String currentSourceService)
    {
        Map<String, String> headers = new LinkedHashMap<>();
        addIfHasText(headers, ReportHeaderConstants.SOURCE_SERVICE, currentSourceService);
        addIfHasText(headers, ReportHeaderConstants.AUTHORIZATION, authorization);
        addIfHasText(headers, ReportHeaderConstants.ADMIN_ID, adminId);
        addIfHasText(headers, ReportHeaderConstants.ADMIN_NAME, encode(adminName));
        headers.put(ReportHeaderConstants.IS_ADMIN, String.valueOf(admin));
        addIfHasText(headers, ReportHeaderConstants.ADMIN_NICK_NAME, encode(adminNickName));
        addIfHasText(headers, ReportHeaderConstants.ADMIN_COMPANY_NAME, encode(adminCompanyName));
        addIfHasText(headers, ReportHeaderConstants.ADMIN_ROLE_IDS, joinRoleIds(adminRoleIds));
        addIfHasText(headers, ReportHeaderConstants.CLIENT_IP, clientIp);
        return headers;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 02:55
     * 说明: 读取请求头并做空值兜底
     * @param request HTTP 请求对象
     * @param headerName 请求头名称
     * @return
     * 返回值: String
     * 返回值说明: 请求头原始值，未读取到时为空字符串
     */
    private static String readHeader(HttpServletRequest request, String headerName)
    {
        if (request == null || !StringUtils.hasText(headerName))
        {
            return "";
        }
        String headerValue = request.getHeader(headerName);
        return StringUtils.hasText(headerValue) ? headerValue : "";
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 02:55
     * 说明: 解析管理员标识，只接受 true 或 1 表示管理员，避免空值被误判
     * @param value 请求头原始值
     * @return
     * 返回值: boolean
     * 返回值说明: true-管理员，false-非管理员或未传入
     */
    private static boolean parseAdmin(String value)
    {
        return "true".equalsIgnoreCase(value) || "1".equals(value);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 03:10
     * 说明: 解析管理员角色ID列表，忽略空值和非数字项，避免脏请求头影响主流程
     * @param value 请求头原始值，多个角色ID使用英文逗号分隔
     * @return
     * 返回值: List<Long>
     * 返回值说明: 管理员角色ID列表
     */
    private static List<Long> parseRoleIds(String value)
    {
        if (!StringUtils.hasText(value))
        {
            return List.of();
        }
        List<Long> roleIds = new ArrayList<>();
        for (String item : value.split(","))
        {
            if (!StringUtils.hasText(item))
            {
                continue;
            }
            try
            {
                roleIds.add(Long.valueOf(item.trim()));
            }
            catch (NumberFormatException ignored)
            {
            }
        }
        return roleIds;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 02:55
     * 说明: 对 URL 编码的中文请求头做 UTF-8 解码，解码失败时返回原始值兜底
     * @param value 请求头原始值
     * @return
     * 返回值: String
     * 返回值说明: 解码后的值，空值返回空字符串
     */
    private static String decode(String value)
    {
        if (!StringUtils.hasText(value))
        {
            return "";
        }
        try
        {
            return URLDecoder.decode(value, StandardCharsets.UTF_8);
        }
        catch (IllegalArgumentException e)
        {
            return value;
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 02:55
     * 说明: 对可能包含中文的请求头值做 UTF-8 编码，便于 report 继续透传给下游服务
     * @param value 请求头原始值
     * @return
     * 返回值: String
     * 返回值说明: URL 编码后的值，空值返回空字符串
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

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 02:55
     * 说明: 写入非空请求头，避免空值覆盖下游默认值
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

    public String getFirstAdminRoleId()
    {
        return adminRoleIds == null || adminRoleIds.isEmpty() ? "" : String.valueOf(adminRoleIds.get(0));
    }

}
