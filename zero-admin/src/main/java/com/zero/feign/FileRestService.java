package com.zero.feign;

import com.zero.common.constant.HttpStatus;
import com.zero.common.constant.ServiceNameConstants;
import com.zero.common.core.domain.AjaxResult;
import com.zero.common.core.page.TableDataInfo;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 通过服务名将管理后台的文件请求转发给文件服务，兼容普通响应、分页和 multipart 上传。
 */
@Slf4j
@Service
public class FileRestService
{
    private static final String ERROR_MSG = "文件服务请求异常，请联系管理员！";

    private static final String FILE_SERVICE_URL = "http://" + ServiceNameConstants.ZERO_FILE;

    private final RestTemplate restTemplate;

    public FileRestService(@Qualifier("fileRestTemplate") RestTemplate restTemplate)
    {
        this.restTemplate = restTemplate;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 将查询参数转发给文件服务的 GET 接口。
     * @param requestPath 文件服务内部路径
     * @param paramMap 以参数名为键的查询参数，可以为空
     * @return
     * 返回值: AjaxResult
     * 返回值说明: 文件服务返回的普通接口结果或统一错误
     */
    public AjaxResult invokeGetByMap(String requestPath, Map<String, ?> paramMap)
    {
        return exchange(requestPath, HttpMethod.GET, null, paramMap, AjaxResult.class);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 向文件服务的 POST 接口发送 JSON 请求。
     * @param requestPath 文件服务内部路径
     * @param params JSON 请求体，可以为空
     * @return
     * 返回值: AjaxResult
     * 返回值说明: 文件服务返回的普通接口结果或统一错误
     */
    public AjaxResult invokePostJson(String requestPath, Object params)
    {
        return exchange(requestPath, HttpMethod.POST, params, Collections.emptyMap(), AjaxResult.class);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 通过服务名请求文件服务，保留普通响应和分页响应各自的结构。
     * @param <T> 响应体类型
     * @param requestPath 以斜杠开头的文件服务内部路径
     * @param method 本次调用的 HTTP 方法
     * @param requestBody JSON 请求体，可以为空
     * @param queryParams 查询参数，可以为空
     * @param responseType 文件服务响应体类型
     * @return
     * 返回值: T
     * 返回值说明: 文件服务的响应体，AjaxResult 或 TableDataInfo 失败时返回对应结构
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
                T errorResponse = buildErrorResponse(responseType, "文件服务未返回响应数据");
                if (errorResponse != null)
                {
                    return errorResponse;
                }
            }
            return responseBody;
        }
        catch (RestClientException | IllegalArgumentException e)
        {
            log.error("调用文件服务失败，path={}，method={}", requestPath, method, e);
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
     * 说明: 以 multipart 请求转发单个文件及其业务参数。
     * @param requestPath 文件服务的上传路径
     * @param paramName 下游接收的 multipart 字段名
     * @param file 上传文件
     * @param queryParams 随请求保留的查询参数
     * @return
     * 返回值: AjaxResult
     * 返回值说明: 文件服务上传结果，调用失败或响应为空时返回统一错误
     */
    public AjaxResult uploadFile(String requestPath, String paramName, MultipartFile file, Map<String, ?> queryParams)
            throws IOException
    {
        try
        {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add(paramName, toResource(file));
            HttpEntity<MultiValueMap<String, Object>> requestEntity =
                    new HttpEntity<>(body, buildMultipartHeaders());
            ResponseEntity<AjaxResult> responseEntity = restTemplate.exchange(buildUri(requestPath, queryParams),
                    HttpMethod.POST, requestEntity, AjaxResult.class);
            AjaxResult responseBody = responseEntity.getBody();
            return responseBody == null ? AjaxResult.error("文件服务未返回响应数据") : responseBody;
        }
        catch (RestClientException | IllegalArgumentException e)
        {
            log.error("上传文件到文件服务失败，path={}", requestPath, e);
            return AjaxResult.error(ERROR_MSG);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 在同一 multipart 请求中，以重复同名部件向文件服务发送多个文件。
     * @param requestPath 文件服务的批量上传路径
     * @param paramName 下游接收的 multipart 字段名
     * @param files 待转发的文件列表
     * @param queryParams 随请求保留的查询参数
     * @return
     * 返回值: AjaxResult
     * 返回值说明: 文件服务多文件上传结果或统一错误
     */
    public AjaxResult uploadFiles(String requestPath, String paramName, List<MultipartFile> files,
            Map<String, ?> queryParams) throws IOException
    {
        try
        {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            for (MultipartFile file : files)
            {
                body.add(paramName, toResource(file));
            }
            HttpEntity<MultiValueMap<String, Object>> requestEntity =
                    new HttpEntity<>(body, buildMultipartHeaders());
            ResponseEntity<AjaxResult> responseEntity = restTemplate.exchange(buildUri(requestPath, queryParams),
                    HttpMethod.POST, requestEntity, AjaxResult.class);
            AjaxResult responseBody = responseEntity.getBody();
            return responseBody == null ? AjaxResult.error("文件服务未返回响应数据") : responseBody;
        }
        catch (RestClientException | IllegalArgumentException e)
        {
            log.error("批量上传文件到文件服务失败，path={}", requestPath, e);
            return AjaxResult.error(ERROR_MSG);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 在 Servlet 请求线程中读取当前请求路径。
     * @return
     * 返回值: String
     * 返回值说明: 当前请求 URI，没有 Servlet 上下文时返回空字符串
     */
    public String getCurrentRequestPath()
    {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes servletRequestAttributes))
        {
            return "";
        }
        HttpServletRequest request = servletRequestAttributes.getRequest();
        return request == null ? "" : request.getRequestURI();
    }

    private HttpHeaders buildJsonHeaders()
    {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private HttpHeaders buildMultipartHeaders()
    {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        return headers;
    }

    private URI buildUri(String requestPath, Map<String, ?> queryParams)
    {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(FILE_SERVICE_URL).path(normalizePath(requestPath));
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
     * 时间: 2026/11/20 01:36
     * 说明: 校验转发路径。代理目标由服务名固定，调用方仅提供以斜杠开头的服务内路径。
     * @param requestPath 调用方提供的服务内路径
     * @return
     * 返回值: String
     * 返回值说明: 去除首尾空白后的服务内路径
     */
    private String normalizePath(String requestPath)
    {
        if (!StringUtils.hasText(requestPath))
        {
            throw new IllegalArgumentException("文件服务请求路径不能为空");
        }
        String path = requestPath.trim();
        if (path.contains("://") || !path.startsWith("/"))
        {
            throw new IllegalArgumentException("文件服务请求路径必须以/开头，且不能传入完整URL");
        }
        return path;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 按声明的接口类型构造文件服务调用错误，保持调用方可解析。
     * @param <T> 调用方声明的响应体类型
     * @param responseType 用于选择错误结构的返回类型
     * @param msg 提供给调用方的错误描述
     * @return
     * 返回值: T
     * 返回值说明: 普通错误响应或分页错误响应，其他类型返回 null
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

    private MultipartInputStreamFileResource toResource(MultipartFile file) throws IOException
    {
        return new MultipartInputStreamFileResource(file.getInputStream(), file.getOriginalFilename(), file.getSize());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 为转发的 multipart 文件保留原始文件名和长度，避免为计算长度提前读尽输入流。
     */
    private static class MultipartInputStreamFileResource extends InputStreamResource
    {
        private final String filename;

        private final long contentLength;

        MultipartInputStreamFileResource(InputStream inputStream, String filename, long contentLength)
        {
            super(inputStream);
            this.filename = filename;
            this.contentLength = contentLength;
        }

        @Override
        public String getFilename()
        {
            return filename;
        }

        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/11/20 01:36
         * 说明: 使用 multipart 文件预先提供的长度，避免读取输入流来推算长度。
         * @return
         * 返回值: long
         * 返回值说明: 上传内容的字节数
         */
        @Override
        public long contentLength()
        {
            return contentLength;
        }
    }
}
