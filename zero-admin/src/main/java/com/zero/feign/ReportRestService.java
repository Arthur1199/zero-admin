package com.zero.feign;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;
import com.zero.common.constant.HttpStatus;
import com.zero.common.constant.ServiceNameConstants;
import com.zero.common.core.domain.AjaxResult;
import com.zero.common.core.page.TableDataInfo;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.Map;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/6/28 02:25
 * 说明: admin 调用 report 的通用服务，支持按当前请求路径或自定义路径转发 GET、POST JSON 请求
 */
@Slf4j
@Service
public class ReportRestService
{
    /**
     * 报表服务通用调用失败提示，避免向前端暴露下游异常细节
     */
    private static final String ERROR_MSG = "报表服务请求异常，请联系管理员！";

    /**
     * 报表服务注册中心访问前缀，由 LoadBalanced RestTemplate 解析为真实实例
     */
    private static final String REPORT_SERVICE_URL = "http://" + ServiceNameConstants.ZERO_REPORT;

    /**
     * 专用于调用报表服务的负载均衡 RestTemplate
     */
    private final RestTemplate restTemplate;

    /**
     * 对象转换器，用于将查询对象转换为请求参数 Map
     */
    private final JsonMapper objectMapper;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 02:25
     * 说明: 构造报表通用调用服务，通过构造注入保证依赖不可变
     * @param restTemplate 负载均衡 RestTemplate
     * @param objectMapper 对象转换器
     */
    public ReportRestService(@Qualifier("reportRestTemplate") RestTemplate restTemplate, JsonMapper objectMapper)
    {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 02:25
     * 说明: 按当前 admin 请求路径向 report 发送 POST JSON 请求，适用于两边 Controller 路径保持一致的接口
     * @param params POST JSON 请求体
     * @return
     * 返回值: AjaxResult
     * 返回值说明: report 返回结果；调用失败时返回统一错误结果
     */
    public AjaxResult invokePostJson(Object params)
    {
        return invokePostJson(getCurrentRequestPath(), params);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 02:25
     * 说明: 兼容历史项目 invockPostJson 方法命名，内部统一委托给 invokePostJson
     * @param params POST JSON 请求体
     * @return
     * 返回值: AjaxResult
     * 返回值说明: report 返回结果；调用失败时返回统一错误结果
     */
    public AjaxResult invockPostJson(Object params)
    {
        return invokePostJson(params);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 02:25
     * 说明: 按自定义路径向 report 发送 POST JSON 请求，适用于 admin 与 report Controller 路径不一致的接口
     * @param requestPath report 接口路径，必须以 / 开头
     * @param params POST JSON 请求体
     * @return
     * 返回值: AjaxResult
     * 返回值说明: report 返回结果；调用失败时返回统一错误结果
     */
    public AjaxResult invokePostJson(String requestPath, Object params)
    {
        return exchange(requestPath, HttpMethod.POST, params, Collections.emptyMap(), AjaxResult.class);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 02:25
     * 说明: 兼容历史项目 invockPostJson 方法命名，内部统一委托给 invokePostJson
     * @param requestPath report 接口路径，必须以 / 开头
     * @param params POST JSON 请求体
     * @return
     * 返回值: AjaxResult
     * 返回值说明: report 返回结果；调用失败时返回统一错误结果
     */
    public AjaxResult invockPostJson(String requestPath, Object params)
    {
        return invokePostJson(requestPath, params);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 02:25
     * 说明: 按当前 admin 请求路径向 report 发送 GET 请求，查询参数由调用方显式传入
     * @param paramMap GET 查询参数
     * @return
     * 返回值: AjaxResult
     * 返回值说明: report 返回结果；调用失败时返回统一错误结果
     */
    public AjaxResult invokeGetByMap(Map<String, ?> paramMap)
    {
        return invokeGetByMap(getCurrentRequestPath(), paramMap);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 02:25
     * 说明: 兼容历史项目 invockGetByMap 方法命名，内部统一委托给 invokeGetByMap
     * @param paramMap GET 查询参数
     * @return
     * 返回值: AjaxResult
     * 返回值说明: report 返回结果；调用失败时返回统一错误结果
     */
    public AjaxResult invockGetByMap(Map<String, ?> paramMap)
    {
        return invokeGetByMap(paramMap);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 02:25
     * 说明: 按自定义路径向 report 发送 GET 请求，查询参数由调用方显式传入
     * @param requestPath report 接口路径，必须以 / 开头
     * @param paramMap GET 查询参数
     * @return
     * 返回值: AjaxResult
     * 返回值说明: report 返回结果；调用失败时返回统一错误结果
     */
    public AjaxResult invokeGetByMap(String requestPath, Map<String, ?> paramMap)
    {
        return exchange(requestPath, HttpMethod.GET, null, paramMap, AjaxResult.class);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 02:25
     * 说明: 兼容历史项目 invockGetByMap 方法命名，内部统一委托给 invokeGetByMap
     * @param requestPath report 接口路径，必须以 / 开头
     * @param paramMap GET 查询参数
     * @return
     * 返回值: AjaxResult
     * 返回值说明: report 返回结果；调用失败时返回统一错误结果
     */
    public AjaxResult invockGetByMap(String requestPath, Map<String, ?> paramMap)
    {
        return invokeGetByMap(requestPath, paramMap);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 02:25
     * 说明: 按当前 admin 请求路径向 report 发送 GET 请求，查询对象会转换为请求参数
     * @param queryObj GET 查询对象，不能是基础类型
     * @return
     * 返回值: AjaxResult
     * 返回值说明: report 返回结果；调用失败时返回统一错误结果
     */
    public AjaxResult invokeGetByObj(Object queryObj)
    {
        return invokeGetByObj(getCurrentRequestPath(), queryObj);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 02:25
     * 说明: 兼容历史项目 invockGetByObj 方法命名，内部统一委托给 invokeGetByObj
     * @param queryObj GET 查询对象，不能是基础类型
     * @return
     * 返回值: AjaxResult
     * 返回值说明: report 返回结果；调用失败时返回统一错误结果
     */
    public AjaxResult invockGetByObj(Object queryObj)
    {
        return invokeGetByObj(queryObj);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 02:25
     * 说明: 按自定义路径向 report 发送 GET 请求，查询对象会转换为请求参数
     * @param requestPath report 接口路径，必须以 / 开头
     * @param queryObj GET 查询对象，不能是基础类型
     * @return
     * 返回值: AjaxResult
     * 返回值说明: report 返回结果；调用失败时返回统一错误结果
     */
    public AjaxResult invokeGetByObj(String requestPath, Object queryObj)
    {
        if (isSimpleValue(queryObj))
        {
            return AjaxResult.error(HttpStatus.ERROR, "参数对象为基础数据类型，请使用invokeGetByMap方法进行调用！");
        }
        return exchange(requestPath, HttpMethod.GET, null, objectToMap(queryObj), AjaxResult.class);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 02:25
     * 说明: 兼容历史项目 invockGetByObj 方法命名，内部统一委托给 invokeGetByObj
     * @param requestPath report 接口路径，必须以 / 开头
     * @param queryObj GET 查询对象，不能是基础类型
     * @return
     * 返回值: AjaxResult
     * 返回值说明: report 返回结果；调用失败时返回统一错误结果
     */
    public AjaxResult invockGetByObj(String requestPath, Object queryObj)
    {
        return invokeGetByObj(requestPath, queryObj);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 通用调用 report 接口，可指定返回类型以兼容 AjaxResult、TableDataInfo 或自定义 DTO
     * @param <T> 当前方法处理的值类型
     * @param requestPath report 接口路径，必须以 / 开头
     * @param method HTTP 请求方法
     * @param requestBody POST/PUT 等请求体，GET 请求可为空
     * @param queryParams 查询参数，可为空
     * @param responseType 返回值类型
     * @return
     * 返回值: T
     * 返回值说明: report 返回体；调用失败时AjaxResult和TableDataInfo返回对应错误结构，其它类型抛出异常
     */
    public <T> T exchange(String requestPath, HttpMethod method, Object requestBody, Map<String, ?> queryParams,
            Class<T> responseType)
    {
        try
        {
            URI uri = buildUri(requestPath, queryParams);
            HttpEntity<Object> requestEntity = new HttpEntity<>(requestBody, buildJsonHeaders());
            ResponseEntity<T> responseEntity = restTemplate.exchange(uri, method, requestEntity, responseType);
            T responseBody = responseEntity.getBody();
            if (responseBody == null)
            {
                T errorResponse = buildErrorResponse(responseType, "报表服务未返回响应数据");
                if (errorResponse != null)
                {
                    return errorResponse;
                }
            }
            return responseBody;
        }
        catch (RestClientException | IllegalArgumentException e)
        {
            log.error("调用报表服务失败，path={}，method={}", requestPath, method, e);
            T errorResponse = buildErrorResponse(responseType, ERROR_MSG);
            if (errorResponse != null)
            {
                return errorResponse;
            }
            throw e;
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 按调用方声明的返回类型构建统一失败结果，保证前端拦截器和列表页能按固定结构处理失败
     * @param <T> 当前方法处理的值类型
     * @param responseType 调用方期望的返回类型
     * @param msg 失败提示，不包含下游异常堆栈或敏感信息
     * @return
     * 返回值: T
     * 返回值说明: AjaxResult或TableDataInfo失败结构；其它返回类型返回null并交由调用方抛出异常
     */
    private <T> T buildErrorResponse(Class<T> responseType, String msg)
    {
        if (AjaxResult.class.equals(responseType))
        {
            return responseType.cast(AjaxResult.error(msg));
        }
        if (TableDataInfo.class.equals(responseType))
        {
            TableDataInfo dataInfo = new TableDataInfo();
            dataInfo.setCode(HttpStatus.ERROR);
            dataInfo.setMsg(msg);
            dataInfo.setRows(Collections.emptyList());
            dataInfo.setTotal(0);
            return responseType.cast(dataInfo);
        }
        return null;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 02:25
     * 说明: 构建 JSON 请求头；管理员上下文由 reportRestTemplate 拦截器统一追加
     * @return
     * 返回值: HttpHeaders
     * 返回值说明: JSON 请求头
     */
    private HttpHeaders buildJsonHeaders()
    {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 02:25
     * 说明: 构建 report 服务调用 URI，只允许服务内路径，避免调用方传入完整 URL 造成越权转发
     * @param requestPath report 接口路径，必须以 / 开头
     * @param queryParams 查询参数，可为空
     * @return
     * 返回值: URI
     * 返回值说明: 可由负载均衡 RestTemplate 解析的服务名 URI
     */
    private URI buildUri(String requestPath, Map<String, ?> queryParams)
    {
        String safePath = normalizePath(requestPath);
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(REPORT_SERVICE_URL).path(safePath);
        if (queryParams != null)
        {
            queryParams.forEach((key, value) -> {
                if (StringUtils.hasText(key) && value != null)
                {
                    builder.queryParam(key, value);
                }
            });
        }
        return builder.build().encode().toUri();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 02:25
     * 说明: 校验并规范化 report 接口路径，拒绝完整 URL 和空路径
     * @param requestPath 原始接口路径
     * @return
     * 返回值: String
     * 返回值说明: 以 / 开头的安全接口路径
     */
    private String normalizePath(String requestPath)
    {
        if (!StringUtils.hasText(requestPath))
        {
            throw new IllegalArgumentException("报表服务请求路径不能为空");
        }
        String path = requestPath.trim();
        if (path.contains("://") || !path.startsWith("/"))
        {
            throw new IllegalArgumentException("报表服务请求路径必须以/开头，且不能传入完整URL");
        }
        return path;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 02:25
     * 说明: 获取当前 admin 请求路径，用于两边 Controller 路径保持一致时自动转发
     * @return
     * 返回值: String
     * 返回值说明: 当前请求 URI，不存在请求上下文时返回空字符串并由上层校验拦截
     */
    private String getCurrentRequestPath()
    {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes servletRequestAttributes))
        {
            return "";
        }
        HttpServletRequest request = servletRequestAttributes.getRequest();
        return request == null ? "" : request.getRequestURI();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 02:25
     * 说明: 将查询对象转换为 Map，空对象返回空参数，避免历史反射方式漏掉父类属性或访问私有字段异常
     * @param queryObj 查询对象
     * @return
     * 返回值: Map<String, Object>
     * 返回值说明: 查询参数 Map
     */
    private Map<String, Object> objectToMap(Object queryObj)
    {
        if (queryObj == null)
        {
            return Collections.emptyMap();
        }
        return objectMapper.convertValue(queryObj, new TypeReference<Map<String, Object>>()
        {
        });
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 02:25
     * 说明: 判断对象是否为基础值类型；基础值 GET 参数需要调用方明确指定参数名
     * @param value 待判断对象
     * @return
     * 返回值: boolean
     * 返回值说明: true-基础值类型，false-可转换为查询参数对象
     */
    private boolean isSimpleValue(Object value)
    {
        if (value == null)
        {
            return false;
        }
        return value instanceof CharSequence
                || value instanceof Number
                || value instanceof Boolean
                || value instanceof Character
                || value.getClass().isEnum();
    }
}
