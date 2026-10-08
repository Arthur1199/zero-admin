package com.zero.framework.security.handle;

import com.alibaba.fastjson2.JSON;
import com.zero.common.constant.Constants;
import com.zero.common.core.domain.AjaxResult;
import com.zero.common.core.domain.model.LoginUser;
import com.zero.common.utils.MessageUtils;
import com.zero.common.utils.ServletUtils;
import com.zero.common.utils.StringUtils;
import com.zero.framework.manager.AsyncManager;
import com.zero.framework.manager.factory.AsyncFactory;
import com.zero.framework.web.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import jakarta.servlet.ServletException;
import java.io.IOException;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 自定义退出处理类 返回成功
 */
@Configuration
public class LogoutSuccessHandlerImpl implements LogoutSuccessHandler
{
    /**
     * 令牌服务字段
     */
    @Autowired
    private TokenService tokenService;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 退出处理
     * @param request 请求参数
     * @param response 响应参数
     * @param authentication authentication参数
     * @return
     * 返回值: void
     * 返回值说明：退出处理
     */
    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException
    {
        LoginUser loginUser = tokenService.getLoginUser(request);
        if (StringUtils.isNotNull(loginUser))
        {
            String userName = loginUser.getUsername();
            // 删除用户缓存记录
            tokenService.delLoginUser(loginUser.getToken());
            // 记录用户退出日志
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(userName, Constants.LOGOUT, MessageUtils.message("user.logout.success")));
        }
        ServletUtils.renderString(response, JSON.toJSONString(AjaxResult.success(MessageUtils.message("user.logout.success"))));
    }
}
