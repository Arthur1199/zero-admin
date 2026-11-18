package com.zero.jobadmin.business.util;

import com.zero.jobadmin.framework.util.XssUtil;
import com.xxl.tool.core.StringTool;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/18 21:34
 * 说明: 校验管理端录入的执行器 HTTP 地址，并保留配置中的节点顺序。
 */
public final class ExecutorAddressUtil {

    /** 注册地址和调度日志执行器地址都是 VARCHAR(255)。 */
    private static final int ADDRESS_CHAR_LIMIT = 255;
    /** 执行器组的地址列表使用 utf8mb4 TEXT。 */
    private static final int ADDRESS_LIST_BYTE_LIMIT = 65_535;

    private ExecutorAddressUtil() {
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 21:34
     * 说明: 去除每个地址两侧空白，检查协议、主机、端口及字段容量。
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
            if (!fitsAddressColumn(normalized)) {
                throw new IllegalArgumentException("Executor address exceeds column length");
            }
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
        String normalizedList = String.join(",", addresses);
        if (normalizedList.getBytes(StandardCharsets.UTF_8).length > ADDRESS_LIST_BYTE_LIMIT) {
            throw new IllegalArgumentException("Executor address list exceeds column capacity");
        }
        return normalizedList;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 23:05
     * 说明: 检查单个节点能否写入注册表和调度日志的地址列。
     * @param address 执行器地址，可以为空
     * @return
     * 返回值: boolean
     * 返回值说明: 地址非空且 Unicode 字符数量满足列容量时返回 true
     */
    public static boolean fitsAddressColumn(String address) {
        return StringTool.isNotBlank(address) && address.codePointCount(0, address.length()) <= ADDRESS_CHAR_LIMIT;
    }
}
