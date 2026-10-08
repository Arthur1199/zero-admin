package com.zero;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: web容器中进行部署
 */
public class ZeroAdminServletInitializer extends SpringBootServletInitializer {
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: configure
     * @param application application参数
     * @return
     * 返回值: SpringApplicationBuilder
     * 返回值说明：configure
     */

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application)
    {
        return application.sources(ZeroAdminApplication.class);
    }
}
