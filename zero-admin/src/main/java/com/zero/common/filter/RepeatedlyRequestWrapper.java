package com.zero.common.filter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import com.zero.common.utils.http.HttpHelper;
import com.zero.common.constant.Constants;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 构建可重复读取inputStream的request
 */
public class RepeatedlyRequestWrapper extends HttpServletRequestWrapper
{
    /**
     * 请求体字段
     */
    private final byte[] body;
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: Repeatedly请求Wrapper
     * @param request 请求参数
     * @param response 响应参数
     */

    public RepeatedlyRequestWrapper(HttpServletRequest request, ServletResponse response) throws IOException
    {
        super(request);
        request.setCharacterEncoding(Constants.UTF8);
        response.setCharacterEncoding(Constants.UTF8);

        body = HttpHelper.getBodyString(request).getBytes(Constants.UTF8);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Reader
     * @return
     * 返回值: BufferedReader
     * 返回值说明：获取Reader
     */

    @Override
    public BufferedReader getReader() throws IOException
    {
        return new BufferedReader(new InputStreamReader(getInputStream()));
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
        final ByteArrayInputStream bais = new ByteArrayInputStream(body);
        return new ServletInputStream()
        {
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
                return bais.read();
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
                return body.length;
            }
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
                return false;
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
                return false;
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
        };
    }
}
