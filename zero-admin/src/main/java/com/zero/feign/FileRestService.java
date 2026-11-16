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
 * zero-file proxy service.
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

    public AjaxResult invokeGetByMap(String requestPath, Map<String, ?> paramMap)
    {
        return exchange(requestPath, HttpMethod.GET, null, paramMap, AjaxResult.class);
    }

    public AjaxResult invokePostJson(String requestPath, Object params)
    {
        return exchange(requestPath, HttpMethod.POST, params, Collections.emptyMap(), AjaxResult.class);
    }

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

        @Override
        public long contentLength()
        {
            return contentLength;
        }
    }
}
