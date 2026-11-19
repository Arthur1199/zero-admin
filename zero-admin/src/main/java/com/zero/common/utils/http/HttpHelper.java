package com.zero.common.utils.http;

import lombok.extern.slf4j.Slf4j;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.ServletRequest;
import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 通用http工具封装
 */
@Slf4j
public class HttpHelper
{
    /**
     * LOGGER字段
     */
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取请求体String
     * @param request 请求参数
     * @return
     * 返回值: String
     * 返回值说明：获取请求体String
     */

    public static String getBodyString(ServletRequest request)
    {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = null;
        try (InputStream inputStream = request.getInputStream())
        {
            reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String line = "";
            while ((line = reader.readLine()) != null)
            {
                sb.append(line);
            }
        }
        catch (IOException e)
        {
            log.warn("getBodyString出现问题！");
        }
        finally
        {
            if (reader != null)
            {
                try
                {
                    reader.close();
                }
                catch (IOException e)
                {
                    log.error(ExceptionUtils.getMessage(e));
                }
            }
        }
        return sb.toString();
    }
}
