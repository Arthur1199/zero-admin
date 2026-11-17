package com.zero.common.utils.ip;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.zero.common.utils.ServletUtils;
import com.zero.common.utils.StringUtils;
import jakarta.servlet.http.HttpServletRequest;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 获取IP方法
 */
public class IpUtils
{
    /**
     * REGX_0_255字段
     */
    public final static String REGX_0_255 = "(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]\\d|\\d)";
    // 匹配 ip
    /**
     * REGX_IP字段
     */
    public final static String REGX_IP = "((" + REGX_0_255 + "\\.){3}" + REGX_0_255 + ")";
    /**
     * REGX_IP_WILDCARD字段
     */
    public final static String REGX_IP_WILDCARD = "(((\\*\\.){3}\\*)|(" + REGX_0_255 + "(\\.\\*){3})|(" + REGX_0_255 + "\\." + REGX_0_255 + ")(\\.\\*){2}" + "|((" + REGX_0_255 + "\\.){3}\\*))";
    // 匹配网段
    /**
     * REGX_IP_SEG字段
     */
    public final static String REGX_IP_SEG = "(" + REGX_IP + "\\-" + REGX_IP + ")";

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取客户端IP
     * @return
     * 返回值: IP地址
     * 返回值说明：获取客户端IP
     */
    public static String getIpAddr()
    {
        return getIpAddr(ServletUtils.getRequest());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取客户端IP
     * @param request 请求对象
     * @return
     * 返回值: IP地址
     * 返回值说明：获取客户端IP
     */
    public static String getIpAddr(HttpServletRequest request)
    {
        if (request == null)
        {
            return "unknown";
        }
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip))
        {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip))
        {
            ip = request.getHeader("X-Forwarded-For");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip))
        {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip))
        {
            ip = request.getHeader("X-Real-IP");
        }

        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip))
        {
            ip = request.getRemoteAddr();
        }

        return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : getMultistageReverseProxyIp(ip);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 检查是否为内部IP地址
     * @param ip IP地址
     * @return
     * 返回值: 结果
     * 返回值说明：检查是否为内部IP地址
     */
    public static boolean internalIp(String ip)
    {
        byte[] addr = textToNumericFormatV4(ip);
        return internalIp(addr) || "127.0.0.1".equals(ip);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 检查是否为内部IP地址
     * @param addr byte地址
     * @return
     * 返回值: 结果
     * 返回值说明：检查是否为内部IP地址
     */
    private static boolean internalIp(byte[] addr)
    {
        if (StringUtils.isNull(addr) || addr.length < 2)
        {
            return true;
        }
        final byte b0 = addr[0];
        final byte b1 = addr[1];
        // 10.x.x.x/8
        final byte SECTION_1 = 0x0A;
        // 172.16.x.x/12
        final byte SECTION_2 = (byte) 0xAC;
        final byte SECTION_3 = (byte) 0x10;
        final byte SECTION_4 = (byte) 0x1F;
        // 192.168.x.x/16
        final byte SECTION_5 = (byte) 0xC0;
        final byte SECTION_6 = (byte) 0xA8;
        switch (b0)
        {
            case SECTION_1:
                return true;
            case SECTION_2:
                if (b1 >= SECTION_3 && b1 <= SECTION_4)
                {
                    return true;
                }
            case SECTION_5:
                switch (b1)
                {
                    case SECTION_6:
                        return true;
                }
            default:
                return false;
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 将IPv4地址转换成字节
     * @param text IPv4地址
     * @return
     * 返回值: byte 字节
     * 返回值说明：将IPv4地址转换成字节
     */
    public static byte[] textToNumericFormatV4(String text)
    {
        if (text.length() == 0)
        {
            return null;
        }

        byte[] bytes = new byte[4];
        String[] elements = text.split("\\.", -1);
        try
        {
            long l;
            int i;
            switch (elements.length)
            {
                case 1:
                    l = Long.parseLong(elements[0]);
                    if ((l < 0L) || (l > 4294967295L))
                    {
                        return null;
                    }
                    bytes[0] = (byte) (int) (l >> 24 & 0xFF);
                    bytes[1] = (byte) (int) ((l & 0xFFFFFF) >> 16 & 0xFF);
                    bytes[2] = (byte) (int) ((l & 0xFFFF) >> 8 & 0xFF);
                    bytes[3] = (byte) (int) (l & 0xFF);
                    break;
                case 2:
                    l = Integer.parseInt(elements[0]);
                    if ((l < 0L) || (l > 255L))
                    {
                        return null;
                    }
                    bytes[0] = (byte) (int) (l & 0xFF);
                    l = Integer.parseInt(elements[1]);
                    if ((l < 0L) || (l > 16777215L))
                    {
                        return null;
                    }
                    bytes[1] = (byte) (int) (l >> 16 & 0xFF);
                    bytes[2] = (byte) (int) ((l & 0xFFFF) >> 8 & 0xFF);
                    bytes[3] = (byte) (int) (l & 0xFF);
                    break;
                case 3:
                    for (i = 0; i < 2; ++i)
                    {
                        l = Integer.parseInt(elements[i]);
                        if ((l < 0L) || (l > 255L))
                        {
                            return null;
                        }
                        bytes[i] = (byte) (int) (l & 0xFF);
                    }
                    l = Integer.parseInt(elements[2]);
                    if ((l < 0L) || (l > 65535L))
                    {
                        return null;
                    }
                    bytes[2] = (byte) (int) (l >> 8 & 0xFF);
                    bytes[3] = (byte) (int) (l & 0xFF);
                    break;
                case 4:
                    for (i = 0; i < 4; ++i)
                    {
                        l = Integer.parseInt(elements[i]);
                        if ((l < 0L) || (l > 255L))
                        {
                            return null;
                        }
                        bytes[i] = (byte) (int) (l & 0xFF);
                    }
                    break;
                default:
                    return null;
            }
        }
        catch (NumberFormatException e)
        {
            return null;
        }
        return bytes;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取IP地址
     * @return
     * 返回值: 本地IP地址
     * 返回值说明：获取IP地址
     */
    public static String getHostIp()
    {
        try
        {
            return InetAddress.getLocalHost().getHostAddress();
        }
        catch (UnknownHostException e)
        {
        }
        return "127.0.0.1";
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取主机名
     * @return
     * 返回值: 本地主机名
     * 返回值说明：获取主机名
     */
    public static String getHostName()
    {
        try
        {
            return InetAddress.getLocalHost().getHostName();
        }
        catch (UnknownHostException e)
        {
        }
        return "未知";
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 从多级反向代理中获得第一个非unknown IP地址
     * @param ip 获得的IP地址
     * @return
     * 返回值: 第一个非unknown IP地址
     * 返回值说明：从多级反向代理中获得第一个非unknown IP地址
     */
    public static String getMultistageReverseProxyIp(String ip)
    {
        // 多级反向代理检测
        if (ip != null && ip.indexOf(",") > 0)
        {
            final String[] ips = ip.trim().split(",");
            for (String subIp : ips)
            {
                if (false == isUnknown(subIp))
                {
                    ip = subIp;
                    break;
                }
            }
        }
        return StringUtils.substring(ip, 0, 255);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 检测给定字符串是否为未知，多用于检测HTTP请求相关
     * @param checkString 被检测的字符串
     * @return
     * 返回值: 是否未知
     * 返回值说明：检测给定字符串是否为未知，多用于检测HTTP请求相关
     */
    public static boolean isUnknown(String checkString)
    {
        return StringUtils.isBlank(checkString) || "unknown".equalsIgnoreCase(checkString);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 是否为IP
     * @param ip IP参数
     * @return
     * 返回值: boolean
     * 返回值说明：是否为IP
     */
    public static boolean isIP(String ip)
    {
        return StringUtils.isNotBlank(ip) && ip.matches(REGX_IP);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 是否为IP，或 *为间隔的通配符地址
     * @param ip IP参数
     * @return
     * 返回值: boolean
     * 返回值说明：是否为IP，或 *为间隔的通配符地址
     */
    public static boolean isIpWildCard(String ip)
    {
        return StringUtils.isNotBlank(ip) && ip.matches(REGX_IP_WILDCARD);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 检测参数是否在ip通配符里
     * @param ipWildCard IPWildCard参数
     * @param ip IP参数
     * @return
     * 返回值: boolean
     * 返回值说明：检测参数是否在ip通配符里
     */
    public static boolean ipIsInWildCardNoCheck(String ipWildCard, String ip)
    {
        String[] s1 = ipWildCard.split("\\.");
        String[] s2 = ip.split("\\.");
        boolean isMatchedSeg = true;
        for (int i = 0; i < s1.length && !s1[i].equals("*"); i++)
        {
            if (!s1[i].equals(s2[i]))
            {
                isMatchedSeg = false;
                break;
            }
        }
        return isMatchedSeg;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 是否为特定格式如:“10.10.10.1-10.10.10.99”的ip段字符串
     * @param ipSeg IPSeg参数
     * @return
     * 返回值: boolean
     * 返回值说明：是否为特定格式如:“10.10.10.1-10.10.10.99”的ip段字符串
     */
    public static boolean isIPSegment(String ipSeg)
    {
        return StringUtils.isNotBlank(ipSeg) && ipSeg.matches(REGX_IP_SEG);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 判断ip是否在指定网段中
     * @param iparea iparea参数
     * @param ip IP参数
     * @return
     * 返回值: boolean
     * 返回值说明：判断ip是否在指定网段中
     */
    public static boolean ipIsInNetNoCheck(String iparea, String ip)
    {
        int idx = iparea.indexOf('-');
        String[] sips = iparea.substring(0, idx).split("\\.");
        String[] sipe = iparea.substring(idx + 1).split("\\.");
        String[] sipt = ip.split("\\.");
        long ips = 0L, ipe = 0L, ipt = 0L;
        for (int i = 0; i < 4; ++i)
        {
            ips = ips << 8 | Integer.parseInt(sips[i]);
            ipe = ipe << 8 | Integer.parseInt(sipe[i]);
            ipt = ipt << 8 | Integer.parseInt(sipt[i]);
        }
        if (ips > ipe)
        {
            long t = ips;
            ips = ipe;
            ipe = t;
        }
        return ips <= ipt && ipt <= ipe;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 校验ip是否符合过滤串规则
     * @param filter 过滤IP列表,支持后缀'*'通配,支持网段如:`10.10.10.1-10.10.10.99`
     * @param ip 校验IP地址
     * @return
     * 返回值: boolean 结果
     * 返回值说明：校验ip是否符合过滤串规则
     */
    public static boolean isMatchedIp(String filter, String ip)
    {
        if (StringUtils.isEmpty(filter) || StringUtils.isEmpty(ip))
        {
            return false;
        }
        String[] ips = filter.split(";");
        for (String iStr : ips)
        {
            if (isIP(iStr) && iStr.equals(ip))
            {
                return true;
            }
            else if (isIpWildCard(iStr) && ipIsInWildCardNoCheck(iStr, ip))
            {
                return true;
            }
            else if (isIPSegment(iStr) && ipIsInNetNoCheck(iStr, ip))
            {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 延迟缓存当前节点地址，避免每条WebSocket消息都扫描网卡，不访问外部服务
     * @return
     * 返回值: String
     * 返回值说明: 优先内网IPv4，其次其它有效IPv4；无可用网卡或读取失败返回回环地址
     */
    public static String getLocalIpAddress()
    {
        return AddressHolder.ADDRESS;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 只扫描启用的非回环网卡，容器网卡也保留，失败不能中断业务消息发送
     * @return
     * 返回值: String
     * 返回值说明: 当前节点地址或回环兜底，未查询NAT后的公网出口IP
     */
    private static String resolveAddress()
    {
        List<InetAddress> addresses = new ArrayList<>();
        try
        {
            var interfaces = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface network : Collections.list(interfaces)) {
                if (network.isUp() && !network.isLoopback()) {
                    addresses.addAll(Collections.list(network.getInetAddresses()));
                }
            }
        }
        catch (Exception e)
        {
            // 获取诊断信息失败时使用兜底，不记录网络环境或影响推送。
        }
        return selectAddress(addresses);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 从网卡地址中优先选择内网IPv4，排除回环、链路本地、组播及未指定地址
     * @param addresses 本机网卡地址，可空
     * @return
     * 返回值: String
     * 返回值说明: 内网IPv4优先，无内网时选择其它有效IPv4，无候选返回回环地址
     */
    static String selectAddress(List<InetAddress> addresses)
    {
        String candidate = null;
        if (addresses != null)
        {
            for (InetAddress address : addresses)
            {
                if (!(address instanceof Inet4Address) || address.isLoopbackAddress()
                        || address.isAnyLocalAddress() || address.isLinkLocalAddress() || address.isMulticastAddress())
                {
                    continue;
                }
                if (address.isSiteLocalAddress())
                {
                    return address.getHostAddress();
                }
                if (candidate == null)
                {
                    candidate = address.getHostAddress();
                }
            }
        }
        return candidate == null ? InetAddress.getLoopbackAddress().getHostAddress() : candidate;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: JVM类初始化保证缓存只计算一次且对并发发送线程可见
     */
    private static final class AddressHolder
    {
        /** 当前服务节点地址，网卡变化后随应用重启重新获取。 */
        private static final String ADDRESS = resolveAddress();
    }
}
