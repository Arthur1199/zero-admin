package com.zero.framework.security.filter;

import com.alibaba.fastjson2.JSON;
import com.zero.common.core.domain.AjaxResult;
import com.zero.common.core.domain.model.LoginUser;
import com.zero.common.utils.SecurityUtils;
import com.zero.common.utils.StringUtils;
import com.zero.framework.web.service.SysPermissionService;
import com.zero.framework.web.service.TokenService;
import com.zero.common.constant.AdminWebSocketConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: token过滤器 验证token有效性
 */
@Slf4j
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter
{
    /**
     * 令牌服务字段
     */
    @Autowired
    private TokenService tokenService;

    /** 认证请求的角色权限来源，页面有无在线连接都执行同样的校验。 */
    @Autowired
    private SysPermissionService permissionService;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: HTTP请求按已提交角色权限认证并按原规则续期，WebSocket精确握手仍由首帧认证。
     * @param request 请求参数
     * @param response 响应参数
     * @param chain chain参数
     * @return
     * 返回值: void
     * 返回值说明：do过滤器Internal
     */

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException
    {
        // WebSocket使用首帧认证，握手即使携带HTTP凭证也不续期登录会话。
        if ("GET".equals(request.getMethod()) && AdminWebSocketConstants.PATH
                .equals(request.getServletPath()))
        {
            chain.doFilter(request, response);
            return;
        }
        LoginUser loginUser = tokenService.getLoginUser(request);
        if (StringUtils.isNotNull(loginUser) && loginUser.getUser() != null
                && StringUtils.isNull(SecurityUtils.getAuthentication()))
        {
            try
            {
                if (permissionService.refreshRoleAuthorization(loginUser))
                {
                    tokenService.refreshToken(loginUser);
                }
                else
                {
                    tokenService.verifyToken(loginUser);
                }
            }
            catch (DataAccessException e)
            {
                // 权限数据不可用时终止请求，避免回退到可能已撤销的会话权限。
                log.warn("读取或更新会话角色权限失败");
                int status = HttpServletResponse.SC_SERVICE_UNAVAILABLE;
                response.setStatus(status);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(JSON.toJSONString(AjaxResult.error(status, "权限服务暂不可用，请稍后重试")));
                return;
            }
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }
        chain.doFilter(request, response);
    }
}
