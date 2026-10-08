package com.zero.framework.interceptor;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.HandlerInterceptor;
import com.alibaba.fastjson2.JSON;
import com.zero.common.annotation.RequiresPermissions;
import com.zero.common.constant.ApiAuthConstants;
import com.zero.common.constant.GoogleAuthConstants;
import com.zero.common.core.domain.AjaxResult;
import com.zero.common.exception.ServiceException;
import com.zero.common.utils.SecurityUtils;
import com.zero.common.utils.ServletUtils;
import com.zero.common.utils.StringUtils;
import com.zero.system.domain.SysApiAuth;
import com.zero.system.service.ISysApiAuthService;
import com.zero.system.service.ISysConfigService;
import com.zero.system.service.ISysGoogleService;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/6/27 04:49
 * 说明: 接口级谷歌动态码二次验证拦截器，根据sys_config总开关和sys_api_auth接口配置决定是否校验X-Google-Code请求头
 */
@Component
public class ApiGoogleAuthInterceptor implements HandlerInterceptor
{
    /**
     * Spring风格路径匹配器，支持/system/user/{id}和/system/**这类接口模板
     */
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * 接口谷歌验证配置服务，读取需要二次验证的API地址
     */
    @Autowired
    private ISysApiAuthService apiAuthService;

    /**
     * 参数配置服务，读取接口谷歌验证总开关
     */
    @Autowired
    private ISysConfigService configService;

    /**
     * 用户谷歌动态码服务，复用登录谷歌码校验规则
     */
    @Autowired
    private ISysGoogleService googleService;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/27 04:49
     * 说明: 请求进入业务Controller前校验接口谷歌动态码，未开启总开关或未命中API配置时直接放行
     * @param request 当前请求，验证码从X-Google-Code请求头读取，避免读取body影响@RequestBody接参
     * @param response 当前响应，校验失败时写入统一AjaxResult
     * @param handler 当前处理器，仅拦截Controller方法
     * @return
     * 返回值: boolean
     * 返回值说明: true-放行业务接口，false-已输出错误响应
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
    {
        if (!(handler instanceof HandlerMethod) || HttpMethod.OPTIONS.matches(request.getMethod()))
        {
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        String requestPath = normalizeRequestPath(request);
        if (isIgnorePath(requestPath))
        {
            return true;
        }
        boolean enabled = Boolean.parseBoolean(configService.selectConfigByKey(GoogleAuthConstants.API_GOOGLE_AUTH_ENABLED));
        String matchedPattern = normalizeMatchedPattern(request, requestPath);
        List<String> permissions = getPermissions(handlerMethod);
        if (!enabled || !isNeedGoogleAuth(permissions, request.getMethod(), requestPath, matchedPattern))
        {
            return true;
        }
        String googleCode = request.getHeader(GoogleAuthConstants.API_GOOGLE_CODE_HEADER);
        if (StringUtils.isEmpty(googleCode))
        {
            renderError(response, GoogleAuthConstants.GOOGLE_CODE_NOT_NULL, "请输入谷歌验证码");
            return false;
        }
        try
        {
            googleService.validateLoginCode(SecurityUtils.getUserId(), SecurityUtils.getUsername(), googleCode);
            return true;
        }
        catch (ServiceException e)
        {
            Integer code = Objects.isNull(e.getCode()) ? GoogleAuthConstants.GOOGLE_CODE_ERROR : e.getCode();
            renderError(response, code, e.getMessage());
            return false;
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/27 04:49
     * 说明: 判断当前请求路径是否命中启用的接口谷歌验证配置，路径按不带查询参数的URI精确匹配
     * @param requestPath 当前请求路径，不包含contextPath和查询参数
     * @return
     * 返回值: boolean
     * 返回值说明: true-需要谷歌二次验证，false-不需要验证
     */
    private boolean isNeedGoogleAuth(List<String> permissions, String requestMethod, String requestPath, String matchedPattern)
    {
        List<SysApiAuth> authApis = apiAuthService.selectGoogleAuthApis();
        if (StringUtils.isEmpty(authApis))
        {
            return false;
        }
        return authApis.stream().filter(Objects::nonNull)
                .anyMatch(apiAuth -> isApiAuthMatched(apiAuth, permissions, requestMethod, requestPath, matchedPattern));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/27 05:36
     * 说明: 判断单条接口谷歌验证配置是否命中当前请求，先按METHOD + Spring路径模板匹配，配置权限标识时再校验当前接口权限一致
     * @param apiAuth 接口谷歌验证配置
     * @param permissions 当前Controller方法或类上的权限标识集合，接口未配置权限时为空集合
     * @param requestMethod 当前HTTP请求方式
     * @param requestPath 当前实际请求路径，例如/system/user/1
     * @param matchedPattern Spring MVC匹配模板，例如/system/user/{userId}
     * @return
     * 返回值: boolean
     * 返回值说明: true-命中二次验证规则，false-未命中
     */
    private boolean isApiAuthMatched(SysApiAuth apiAuth, List<String> permissions, String requestMethod, String requestPath, String matchedPattern)
    {
        if (!isMethodMatched(apiAuth.getApiMethod(), requestMethod))
        {
            return false;
        }
        String apiUrl = normalizeApiPath(apiAuth.getApiUrl());
        if (!isPathMatched(apiUrl, matchedPattern) && !isPathMatched(apiUrl, requestPath))
        {
            return false;
        }
        String apiPermission = StringUtils.trim(apiAuth.getApiPermission());
        return StringUtils.isEmpty(apiPermission) || permissions.contains(apiPermission);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/27 05:36
     * 说明: 判断请求方式是否命中配置，ALL作为通配方式兼容未细分HTTP动作的老配置
     * @param apiMethod 配置的请求方式
     * @param requestMethod 当前请求方式
     * @return
     * 返回值: boolean
     * 返回值说明: true-请求方式命中，false-请求方式不匹配
     */
    private boolean isMethodMatched(String apiMethod, String requestMethod)
    {
        String ruleMethod = StringUtils.isEmpty(apiMethod) ? ApiAuthConstants.METHOD_ALL : apiMethod.trim().toUpperCase();
        String currentMethod = StringUtils.isEmpty(requestMethod) ? StringUtils.EMPTY : requestMethod.trim().toUpperCase();
        return ApiAuthConstants.METHOD_ALL.equals(ruleMethod) || Objects.equals(ruleMethod, currentMethod);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/27 05:36
     * 说明: 使用精确匹配和Spring Ant模板匹配接口路径，兼容/system/user/{id}、/system/**和实际路径
     * @param apiUrl 配置的接口路径
     * @param requestPath 当前请求路径或Spring MVC匹配模板
     * @return
     * 返回值: boolean
     * 返回值说明: true-路径命中，false-路径不匹配
     */
    private boolean isPathMatched(String apiUrl, String requestPath)
    {
        if (StringUtils.isEmpty(apiUrl) || StringUtils.isEmpty(requestPath))
        {
            return false;
        }
        return Objects.equals(apiUrl, requestPath) || pathMatcher.match(apiUrl, requestPath);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/27 05:36
     * 说明: 提取Controller方法和类上的权限标识，便于配置了权限标识的规则优先按权限匹配
     * @param handlerMethod 当前Controller处理方法
     * @return
     * 返回值: List<String>
     * 返回值说明: 权限标识集合，接口未声明权限时返回空集合
     */
    private List<String> getPermissions(HandlerMethod handlerMethod)
    {
        RequiresPermissions requiresPermissions = handlerMethod.getMethodAnnotation(RequiresPermissions.class);
        if (Objects.isNull(requiresPermissions))
        {
            requiresPermissions = handlerMethod.getBeanType().getAnnotation(RequiresPermissions.class);
        }
        if (Objects.isNull(requiresPermissions) || StringUtils.isEmpty(requiresPermissions.value()))
        {
            return List.of();
        }
        return List.of(requiresPermissions.value());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/27 04:49
     * 说明: 规范化请求路径，去除contextPath，保证前端配置的/system/**路径能和后端实际请求URI一致匹配
     * @param request 当前请求
     * @return
     * 返回值: String
     * 返回值说明: 不含contextPath的请求路径
     */
    private String normalizeRequestPath(HttpServletRequest request)
    {
        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (StringUtils.isNotEmpty(contextPath) && requestUri.startsWith(contextPath))
        {
            return requestUri.substring(contextPath.length());
        }
        return requestUri;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/27 05:36
     * 说明: 获取Spring MVC最终命中的路径模板，解决PUT/DELETE等接口没有显式value但实际映射到类路径或/{id}模板的问题
     * @param request 当前请求
     * @param requestPath 当前实际请求路径，未取得模板时作为兜底
     * @return
     * 返回值: String
     * 返回值说明: Spring MVC匹配模板或实际请求路径
     */
    private String normalizeMatchedPattern(HttpServletRequest request, String requestPath)
    {
        Object pattern = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        if (Objects.isNull(pattern) || StringUtils.isEmpty(pattern.toString()))
        {
            return requestPath;
        }
        return normalizeApiPath(pattern.toString());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/27 05:36
     * 说明: 规范化配置路径，去除查询参数和域名，仅保留可与Spring接口模板匹配的路径部分
     * @param apiPath 配置路径或完整URL
     * @return
     * 返回值: String
     * 返回值说明: 以/开头的接口路径，空值返回空字符串
     */
    private String normalizeApiPath(String apiPath)
    {
        if (StringUtils.isEmpty(apiPath))
        {
            return StringUtils.EMPTY;
        }
        String path = apiPath.trim();
        int queryIndex = path.indexOf("?");
        if (queryIndex >= 0)
        {
            path = path.substring(0, queryIndex);
        }
        if (path.startsWith("http://") || path.startsWith("https://"))
        {
            try
            {
                path = URI.create(path).getPath();
            }
            catch (IllegalArgumentException e)
            {
                return StringUtils.EMPTY;
            }
        }
        return path.startsWith("/") ? path : "/" + path;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/27 04:49
     * 说明: 排除登录、验证码、谷歌绑定、静态资源和验证配置读取接口，避免自身递归或影响基础登录流程
     * @param requestPath 当前请求路径
     * @return
     * 返回值: boolean
     * 返回值说明: true-跳过接口谷歌二次验证，false-继续按配置判断
     */
    private boolean isIgnorePath(String requestPath)
    {
        return Objects.equals(requestPath, "/login")
                || Objects.equals(requestPath, "/logout")
                || Objects.equals(requestPath, "/captchaImage")
                || Objects.equals(requestPath, "/google/generateGaImage")
                || Objects.equals(requestPath, "/google/bind")
                || Objects.equals(requestPath, "/system/apiAuth/googleAuthApis")
                || requestPath.startsWith("/profile/")
                || requestPath.startsWith("/swagger-ui/")
                || requestPath.startsWith("/webjars/")
                || requestPath.startsWith("/v3/api-docs")
                || requestPath.startsWith("/druid/")
                || requestPath.startsWith("/actuator/");
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/27 04:49
     * 说明: 输出谷歌二次验证失败响应，保持前端request.js按AjaxResult结构处理错误
     * @param response 当前响应
     * @param code 业务错误码，-1001表示需要绑定，-1002表示验证码为空，-1003表示验证码错误
     * @param message 错误提示
     * @return
     * 返回值: void
     * 返回值说明: 已向客户端写入JSON响应
     */
    private void renderError(HttpServletResponse response, int code, String message)
    {
        ServletUtils.renderString(response, JSON.toJSONString(AjaxResult.error(code, message)));
    }
}
