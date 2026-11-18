package com.zero.jobadmin.business.util;

import com.zero.jobadmin.framework.util.XssUtil;
import com.xxl.tool.core.StringTool;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/18 21:34
 * 说明: 校验管理端录入的执行器 HTTP 地址，并保留配置中的节点顺序。
 */
public final class ExecutorAddressUtil {

    private ExecutorAddressUtil() {
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 21:34
     * 说明: 去除每个地址两侧空白，检查协议、主机及端口。
     * @param addressList 英文逗号分隔的执行器 HTTP 客户端地址列表，可为空
     * @return
     * 返回值: String
     * 返回值说明: 返回原顺序的规范地址列表，空输入返回 null
     * @throws IllegalArgumentException 任一地址格式不合法
     */
    public static String normalize(String addressList) {
        if (StringTool.isBlank(addressList)) {
            return null;
        }
        if (XssUtil.hasXss(addressList)) {
            throw new IllegalArgumentException("Invalid executor address list");
        }
        List<String> addresses = new ArrayList<>();
        for (String address : addressList.split(",", -1)) {
            String normalized = address.strip();
            try {
                URI uri = new URI(normalized);
                if (!("http".equals(uri.getScheme()) || "https".equals(uri.getScheme()))) {
                    throw new IllegalArgumentException("Invalid executor address scheme");
                }
                var url = uri.toURL();
                if (url.getHost().isBlank() || url.getPort() == 0 || url.getPort() > 65535) {
                    throw new IllegalArgumentException("Invalid executor address host or port");
                }
                addresses.add(normalized);
            } catch (URISyntaxException | MalformedURLException e) {
                throw new IllegalArgumentException("Invalid executor address", e);
            }
        }
        return String.join(",", addresses);
    }
}
