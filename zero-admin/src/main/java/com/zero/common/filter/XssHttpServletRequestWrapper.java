package com.zero.common.filter;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import com.zero.common.utils.StringUtils;
import com.zero.common.utils.html.EscapeUtil;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: XSS过滤处理
 */
public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: XSSHttpServlet请求Wrapper
     * @param request 请求参数
     */
    public XssHttpServletRequestWrapper(HttpServletRequest request)
    {
        super(request);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取ParameterValues
     * @param name 名称参数
     * @return
     * 返回值: String[]
     * 返回值说明：获取ParameterValues
     */

    @Override
    public String[] getParameterValues(String name)
    {
        String[] values = super.getParameterValues(name);
        if (values != null)
        {
            int length = values.length;
            String[] escapesValues = new String[length];
            for (int i = 0; i < length; i++)
            {
                // 防xss攻击和过滤前后空格
                escapesValues[i] = EscapeUtil.clean(values[i]).trim();
            }
            return escapesValues;
        }
        return super.getParameterValues(name);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取InputStream
     * @return
     * 返回值: ServletInputStream
     * 返回值说明：获取InputStream
     */

    @Override
    public ServletInputStream getInputStream() throws IOException
    {
        // 非json类型，直接返回
        if (!isJsonRequest())
        {
            return super.getInputStream();
        }

        // 为空，直接返回
        String json = IOUtils.toString(super.getInputStream(), "utf-8");
        if (StringUtils.isEmpty(json))
        {
            return super.getInputStream();
        }

        // xss过滤
        json = EscapeUtil.clean(json).trim();
        byte[] jsonBytes = json.getBytes("utf-8");
        final ByteArrayInputStream bis = new ByteArrayInputStream(jsonBytes);
        return new ServletInputStream()
        {
            /**
             *
             * 作者: Arthur 亚瑟
             * 时间: 2026/5/14 00:58
             * 说明: 判断Finished
             * @return
             * 返回值: boolean
             * 返回值说明：判断Finished
             */
            @Override
            public boolean isFinished()
            {
                return true;
            }
            /**
             *
             * 作者: Arthur 亚瑟
             * 时间: 2026/5/14 00:58
             * 说明: 判断Ready
             * @return
             * 返回值: boolean
             * 返回值说明：判断Ready
             */

            @Override
            public boolean isReady()
            {
                return true;
            }
            /**
             *
             * 作者: Arthur 亚瑟
             * 时间: 2026/5/14 00:58
             * 说明: available
             * @return
             * 返回值: int
             * 返回值说明：available
             */

            @Override
            public int available() throws IOException
            {
                return jsonBytes.length;
            }
            /**
             *
             * 作者: Arthur 亚瑟
             * 时间: 2026/5/14 00:58
             * 说明: 设置ReadListener
             * @param readListener readListener参数
             * @return
             * 返回值: void
             * 返回值说明：设置ReadListener
             */

            @Override
            public void setReadListener(ReadListener readListener)
            {
            }
            /**
             *
             * 作者: Arthur 亚瑟
             * 时间: 2026/5/14 00:58
             * 说明: read
             * @return
             * 返回值: int
             * 返回值说明：read
             */

            @Override
            public int read() throws IOException
            {
                return bis.read();
            }
        };
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 是否是Json请求
     * @return
     * 返回值: boolean
     * 返回值说明：是否是Json请求
     */
    public boolean isJsonRequest()
    {
        String header = super.getHeader(HttpHeaders.CONTENT_TYPE);
        return StringUtils.startsWithIgnoreCase(header, MediaType.APPLICATION_JSON_VALUE);
    }
}