package com.zero.common.utils.ip;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.lionsoul.ip2region.xdb.Searcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.zero.common.constant.Constants;
import com.zero.common.utils.StringUtils;

/**
 * IP地址解析工具
 *
 * 优先调用太平洋网络接口获取实时地址；远程不可用或返回无效时，
 * 使用 classpath 下的 ip2region/ip2region.xdb 离线库兜底，避免登录日志地址长期为空。
 */
public class AddressUtils
{
    /**
     * 日志对象
     */
    private static final Logger log = LoggerFactory.getLogger(AddressUtils.class);

    /**
     * 太平洋网络IP地址查询接口，接口返回GBK编码JSON。
     */
    public static final String IP_URL = "https://whois.pconline.com.cn/ipJson.jsp";

    /**
     * IP.cn地址查询接口，页面中的tab0_address节点包含地址文本。
     */
    public static final String IP_CN_URL = "http://www.ip.cn/ip";

    /**
     * IP.cn地址节点匹配表达式，仅提取id=tab0_address的节点内容。
     */
    private static final Pattern IP_CN_ADDRESS_PATTERN = Pattern.compile(
            "<span\\s+[^>]*id=[\"']tab0_address[\"'][^>]*>(.*?)</span>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    /**
     * ip2region离线库在classpath中的位置。
     */
    private static final String IP2REGION_XDB_PATH = "ip2region/ip2region.xdb";

    /**
     * 远程查询超时时间，避免登录、操作日志写入被第三方接口长时间阻塞。
     */
    private static final int REMOTE_TIMEOUT_MILLIS = 2000;

    /**
     * 未知地址
     */
    public static final String UNKNOWN = "XX XX";

    /**
     * 内网地址展示值
     */
    public static final String INTERNAL_IP = "内网IP";

    /**
     * 远程接口数据来源
     */
    public static final String SOURCE_REMOTE = "pconline";

    /**
     * IP.cn远程接口数据来源
     */
    public static final String SOURCE_IP_CN = "ipcn";

    /**
     * 本地离线库数据来源
     */
    public static final String SOURCE_LOCAL = "ip2region";

    /**
     * ip2region查询器，使用内存模式加载xdb文件，查询时不再重复读取资源文件。
     */
    private static volatile Searcher searcher;

    /**
     * 根据IP获取可展示的真实地址
     *
     * @param ip IPv4地址，内网地址直接返回“内网IP”
     * @return 地址文本，优先包含国家、地区、省份、城市和运营商；无法解析返回XX XX
     */
    public static String getRealAddressByIP(String ip)
    {
        IpAddressInfo addressInfo = getRealAddressInfoByIP(ip);
        if (addressInfo == null || StringUtils.isBlank(addressInfo.getAddress()))
        {
            return UNKNOWN;
        }
        return addressInfo.getAddress();
    }

    /**
     * 根据IP获取结构化地址信息
     *
     * @param ip IPv4地址，内网地址不访问远程接口，也不进入离线库查询
     * @return 结构化地址信息，包含国家、地区、省份、城市、运营商、展示地址和数据来源
     */
    public static IpAddressInfo getRealAddressInfoByIP(String ip)
    {
        if (StringUtils.isBlank(ip) || !IpUtils.isIP(ip))
        {
            return unknown(ip);
        }
        if (IpUtils.internalIp(ip))
        {
            return internal(ip);
        }

        IpAddressInfo remoteInfo = queryRemote(ip);
        if (isValidAddress(remoteInfo))
        {
            return remoteInfo;
        }

        IpAddressInfo ipCnInfo = queryIpCn(ip);
        if (isValidAddress(ipCnInfo))
        {
            return ipCnInfo;
        }

        IpAddressInfo localInfo = queryLocal(ip);
        if (isValidAddress(localInfo))
        {
            return localInfo;
        }
        return unknown(ip);
    }

    /**
     * 调用太平洋网络接口查询IP地址
     *
     * @param ip IPv4公网地址
     * @return 远程解析结果；接口关闭、超时、异常或返回空地址时返回null
     */
    private static IpAddressInfo queryRemote(String ip)
    {
        try
        {
            String param = "ip=" + URLEncoder.encode(ip, StandardCharsets.UTF_8) + "&json=true";
            String rspStr = sendRemoteGet(IP_URL, param, Charset.forName(Constants.GBK));
            if (StringUtils.isBlank(rspStr))
            {
                log.warn("远程IP地址解析返回为空，ip={}", ip);
                return null;
            }
            return parseRemoteAddress(ip, rspStr);
        }
        catch (Exception e)
        {
            log.warn("远程IP地址解析失败，ip={}，原因={}", ip, e.getMessage());
            return null;
        }
    }

    /**
     * 调用IP.cn页面查询IP地址
     *
     * @param ip IPv4公网地址
     * @return IP.cn解析结果；页面不可访问、字段不存在或地址为空时返回null
     */
    private static IpAddressInfo queryIpCn(String ip)
    {
        try
        {
            String rspStr = sendRemoteGet(IP_CN_URL + "/" + URLEncoder.encode(ip, StandardCharsets.UTF_8) + ".html",
                    StringUtils.EMPTY, StandardCharsets.UTF_8);
            if (StringUtils.isBlank(rspStr))
            {
                log.warn("IP.cn地址解析返回为空，ip={}", ip);
                return null;
            }
            return parseIpCnAddress(ip, rspStr);
        }
        catch (Exception e)
        {
            log.warn("IP.cn地址解析失败，ip={}，原因={}", ip, e.getMessage());
            return null;
        }
    }

    /**
     * 使用ip2region离线库查询IP地址
     *
     * @param ip IPv4公网地址
     * @return 本地解析结果；离线库不存在、加载失败或查询无结果时返回null
     */
    private static IpAddressInfo queryLocal(String ip)
    {
        try
        {
            Searcher localSearcher = getSearcher();
            if (localSearcher == null)
            {
                return null;
            }
            String regionInfo = localSearcher.search(ip);
            if (StringUtils.isBlank(regionInfo))
            {
                log.warn("本地IP地址解析返回为空，ip={}", ip);
                return null;
            }
            return parseLocalAddress(ip, regionInfo);
        }
        catch (Exception e)
        {
            log.warn("本地IP地址解析失败，ip={}，原因={}", ip, e.getMessage());
            return null;
        }
    }

    /**
     * 初始化并复用ip2region查询器
     *
     * @return Searcher实例，资源缺失或加载异常时返回null
     */
    private static Searcher getSearcher()
    {
        if (searcher != null)
        {
            return searcher;
        }
        synchronized (AddressUtils.class)
        {
            if (searcher != null)
            {
                return searcher;
            }
            try (InputStream inputStream = new ClassPathResource(IP2REGION_XDB_PATH).getInputStream())
            {
                searcher = Searcher.newWithBuffer(inputStream.readAllBytes());
                return searcher;
            }
            catch (Exception e)
            {
                log.warn("ip2region离线库加载失败，path={}", IP2REGION_XDB_PATH, e);
                return null;
            }
        }
    }

    /**
     * 按指定编码发送GET请求
     *
     * @param url 请求地址
     * @param param 查询参数，格式为name=value&name2=value2
     * @param charset 响应内容编码
     * @return 响应文本，异常时由调用方兜底
     * @throws Exception 网络、编码或读取异常
     */
    private static String sendRemoteGet(String url, String param, Charset charset) throws Exception
    {
        StringBuilder result = new StringBuilder();
        String urlNameString = StringUtils.isNotBlank(param) ? url + "?" + param : url;
        URL realUrl = new URL(urlNameString);
        URLConnection connection = realUrl.openConnection();
        connection.setConnectTimeout(REMOTE_TIMEOUT_MILLIS);
        connection.setReadTimeout(REMOTE_TIMEOUT_MILLIS);
        connection.setRequestProperty("accept", "*/*");
        connection.setRequestProperty("connection", "Keep-Alive");
        connection.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), charset)))
        {
            String line;
            while ((line = in.readLine()) != null)
            {
                result.append(line);
            }
        }
        return result.toString();
    }

    /**
     * 解析太平洋网络返回的JSON地址
     *
     * @param ip IPv4公网地址
     * @param rspStr 远程接口响应JSON
     * @return 结构化地址；响应格式异常时返回null
     */
    private static IpAddressInfo parseRemoteAddress(String ip, String rspStr)
    {
        JSONObject obj = JSON.parseObject(rspStr);
        String province = clean(obj.getString("pro"));
        String city = clean(obj.getString("city"));
        String region = clean(obj.getString("region"));
        String rawAddress = clean(obj.getString("addr"));
        String country = inferRemoteCountry(province, city, rawAddress);
        String isp = inferRemoteIsp(rawAddress, country, province, city, region);
        String address = firstNotBlank(rawAddress, joinAddress(country, region, province, city, isp));
        if (StringUtils.isBlank(address))
        {
            return null;
        }
        return new IpAddressInfo(ip, country, region, province, city, isp, address, SOURCE_REMOTE, rspStr);
    }

    /**
     * 解析IP.cn页面地址，页面地址通常格式为：中国 江苏 南京 电信
     *
     * @param ip IPv4公网地址
     * @param rspStr IP.cn HTML页面
     * @return 结构化地址；页面字段不存在或为空时返回null
     */
    private static IpAddressInfo parseIpCnAddress(String ip, String rspStr)
    {
        String rawAddress = extractIpCnAddress(rspStr);
        if (StringUtils.isBlank(rawAddress))
        {
            return null;
        }
        String[] parts = StringUtils.split(rawAddress);
        String country = getArrayPart(parts, 0);
        String province = getArrayPart(parts, 1);
        String city = getArrayPart(parts, 2);
        String isp = joinArrayParts(parts, 3);
        String address = joinAddress(country, province, city, isp);
        if (StringUtils.isBlank(address))
        {
            return null;
        }
        return new IpAddressInfo(ip, country, StringUtils.EMPTY, province, city, isp, address, SOURCE_IP_CN, rawAddress);
    }

    /**
     * 解析ip2region格式，标准格式为：国家|区域|省份|城市|运营商
     *
     * @param ip IPv4公网地址
     * @param regionInfo ip2region原始结果
     * @return 结构化地址
     */
    private static IpAddressInfo parseLocalAddress(String ip, String regionInfo)
    {
        String[] regions = StringUtils.splitPreserveAllTokens(regionInfo, "|");
        String country = getRegionPart(regions, 0);
        String region = getRegionPart(regions, 1);
        String province = getRegionPart(regions, 2);
        String city = getRegionPart(regions, 3);
        String isp = getRegionPart(regions, 4);
        String address = joinAddress(country, region, province, city, isp);
        if (StringUtils.isBlank(address))
        {
            return null;
        }
        return new IpAddressInfo(ip, country, region, province, city, isp, address, SOURCE_LOCAL, regionInfo);
    }

    /**
     * 从远程地址中推断国家字段
     *
     * @param province 省份
     * @param city 城市
     * @param rawAddress 远程接口原始地址
     * @return 国家名称，国内地址补“中国”，国外地址取原始地址第一段
     */
    private static String inferRemoteCountry(String province, String city, String rawAddress)
    {
        if (StringUtils.isNotBlank(province) || StringUtils.isNotBlank(city))
        {
            return "中国";
        }
        if (StringUtils.isBlank(rawAddress))
        {
            return StringUtils.EMPTY;
        }
        String[] addressParts = StringUtils.split(rawAddress);
        return addressParts == null || addressParts.length == 0 ? rawAddress : addressParts[0];
    }

    /**
     * 从远程接口addr字段中提取运营商信息
     *
     * @param rawAddress 远程接口原始地址
     * @param country 国家
     * @param province 省份
     * @param city 城市
     * @param region 区域
     * @return 运营商名称，无法提取时返回空串
     */
    private static String inferRemoteIsp(String rawAddress, String country, String province, String city, String region)
    {
        String isp = clean(rawAddress);
        if (StringUtils.isBlank(isp))
        {
            return StringUtils.EMPTY;
        }
        isp = StringUtils.replace(isp, country, StringUtils.EMPTY);
        isp = StringUtils.replace(isp, province, StringUtils.EMPTY);
        isp = StringUtils.replace(isp, city, StringUtils.EMPTY);
        isp = StringUtils.replace(isp, region, StringUtils.EMPTY);
        return clean(isp);
    }

    /**
     * 读取ip2region指定位置的数据段
     *
     * @param regions ip2region拆分后的数组
     * @param index 数据段下标
     * @return 已清理的字段值，0和空值统一返回空串
     */
    private static String getRegionPart(String[] regions, int index)
    {
        if (regions == null || regions.length <= index)
        {
            return StringUtils.EMPTY;
        }
        return clean(regions[index]);
    }

    /**
     * 从IP.cn页面提取地址节点文本
     *
     * @param html IP.cn页面HTML
     * @return tab0_address节点文本，提取不到时返回空串
     */
    private static String extractIpCnAddress(String html)
    {
        Matcher matcher = IP_CN_ADDRESS_PATTERN.matcher(html);
        if (!matcher.find())
        {
            return StringUtils.EMPTY;
        }
        return cleanHtmlText(matcher.group(1));
    }

    /**
     * 清理HTML节点文本
     *
     * @param value 原始HTML片段
     * @return 去除标签、常见实体和多余空白后的文本
     */
    private static String cleanHtmlText(String value)
    {
        if (StringUtils.isBlank(value))
        {
            return StringUtils.EMPTY;
        }
        String text = value.replaceAll("<[^>]+>", StringUtils.EMPTY);
        text = StringUtils.replace(text, "&nbsp;", " ");
        text = StringUtils.replace(text, "&amp;", "&");
        text = StringUtils.replace(text, "&lt;", "<");
        text = StringUtils.replace(text, "&gt;", ">");
        return clean(text);
    }

    /**
     * 获取数组指定位置的地址字段
     *
     * @param parts 地址文本拆分数组
     * @param index 字段下标
     * @return 字段值，不存在时返回空串
     */
    private static String getArrayPart(String[] parts, int index)
    {
        if (parts == null || parts.length <= index)
        {
            return StringUtils.EMPTY;
        }
        return clean(parts[index]);
    }

    /**
     * 拼接数组指定位置之后的字段
     *
     * @param parts 地址文本拆分数组
     * @param startIndex 起始下标
     * @return 拼接后的字段值
     */
    private static String joinArrayParts(String[] parts, int startIndex)
    {
        if (parts == null || parts.length <= startIndex)
        {
            return StringUtils.EMPTY;
        }
        StringJoiner joiner = new StringJoiner(" ");
        for (int i = startIndex; i < parts.length; i++)
        {
            String part = clean(parts[i]);
            if (StringUtils.isNotBlank(part))
            {
                joiner.add(part);
            }
        }
        return joiner.toString();
    }

    /**
     * 拼接可展示地址
     *
     * @param parts 地址字段，空值和重复值会被忽略
     * @return 地址文本
     */
    private static String joinAddress(String... parts)
    {
        StringJoiner joiner = new StringJoiner(" ");
        String lastPart = StringUtils.EMPTY;
        for (String part : parts)
        {
            String cleanPart = clean(part);
            if (StringUtils.isBlank(cleanPart) || StringUtils.equals(cleanPart, lastPart))
            {
                continue;
            }
            joiner.add(cleanPart);
            lastPart = cleanPart;
        }
        return joiner.toString();
    }

    /**
     * 清理地址字段
     *
     * @param value 原始字段
     * @return 去除无意义0、unknown和多余空白后的值
     */
    private static String clean(String value)
    {
        if (StringUtils.isBlank(value))
        {
            return StringUtils.EMPTY;
        }
        String cleanValue = StringUtils.trim(value);
        if (StringUtils.equals(cleanValue, "0") || StringUtils.equalsIgnoreCase(cleanValue, "unknown"))
        {
            return StringUtils.EMPTY;
        }
        return cleanValue.replaceAll("\\s+", " ");
    }

    /**
     * 返回第一个非空字符串
     *
     * @param values 待检查字符串
     * @return 第一个非空字符串；全部为空时返回空串
     */
    private static String firstNotBlank(String... values)
    {
        if (values == null)
        {
            return StringUtils.EMPTY;
        }
        for (String value : values)
        {
            if (StringUtils.isNotBlank(value))
            {
                return value;
            }
        }
        return StringUtils.EMPTY;
    }

    /**
     * 判断解析结果是否可用于展示
     *
     * @param addressInfo 地址结果
     * @return true-包含有效展示地址，false-不可用
     */
    private static boolean isValidAddress(IpAddressInfo addressInfo)
    {
        return addressInfo != null && StringUtils.isNotBlank(addressInfo.getAddress())
                && !StringUtils.equals(addressInfo.getAddress(), UNKNOWN);
    }

    /**
     * 构造内网地址结果
     *
     * @param ip IPv4内网地址
     * @return 内网地址结构
     */
    private static IpAddressInfo internal(String ip)
    {
        return new IpAddressInfo(ip, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY,
                StringUtils.EMPTY, INTERNAL_IP, "internal", INTERNAL_IP);
    }

    /**
     * 构造未知地址结果
     *
     * @param ip 原始IP
     * @return 未知地址结构
     */
    private static IpAddressInfo unknown(String ip)
    {
        return new IpAddressInfo(ip, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY,
                StringUtils.EMPTY, UNKNOWN, "unknown", UNKNOWN);
    }

    /**
     * IP结构化地址信息
     *
     * 包含远程接口和本地ip2region均可表达的公共字段，便于后续接口直接返回国家、地区、城市等明细。
     */
    public static class IpAddressInfo
    {
        /**
         * 查询的IP地址
         */
        private String ip;

        /**
         * 国家，例如中国、美国
         */
        private String country;

        /**
         * 区域，取远程region或ip2region第二段
         */
        private String region;

        /**
         * 省份或州
         */
        private String province;

        /**
         * 城市
         */
        private String city;

        /**
         * 运营商
         */
        private String isp;

        /**
         * 面向日志和页面展示的完整地址
         */
        private String address;

        /**
         * 数据来源：pconline、ip2region、internal或unknown
         */
        private String source;

        /**
         * 原始返回内容，用于排查地址库字段差异
         */
        private String rawAddress;

        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/5/16 07:16
         * 说明: IP地址信息无参构造方法
         * @return
         * 返回值: IpAddressInfo
         * 返回值说明：创建空的IP地址解析结果
         */
        public IpAddressInfo()
        {
        }

        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/5/16 07:16
         * 说明: IP地址信息全参构造方法
         * @param ip IP地址
         * @param country 国家
         * @param region 地区
         * @param province 省份
         * @param city 城市
         * @param isp 运营商
         * @param address 完整地址
         * @param source 数据来源
         * @param rawAddress 原始返回内容
         * @return
         * 返回值: IpAddressInfo
         * 返回值说明：创建完整的IP地址解析结果
         */
        public IpAddressInfo(String ip, String country, String region, String province, String city, String isp,
                String address, String source, String rawAddress)
        {
            this.ip = ip;
            this.country = country;
            this.region = region;
            this.province = province;
            this.city = city;
            this.isp = isp;
            this.address = address;
            this.source = source;
            this.rawAddress = rawAddress;
        }

        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/5/16 07:16
         * 说明: 获取IP地址
         * @return
         * 返回值: String
         * 返回值说明：IP地址
         */
        public String getIp()
        {
            return ip;
        }

        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/5/16 07:16
         * 说明: 设置IP地址
         * @param ip IP地址
         * @return
         * 返回值: void
         * 返回值说明：设置IP地址
         */
        public void setIp(String ip)
        {
            this.ip = ip;
        }

        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/5/16 07:16
         * 说明: 获取国家
         * @return
         * 返回值: String
         * 返回值说明：国家
         */
        public String getCountry()
        {
            return country;
        }

        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/5/16 07:16
         * 说明: 设置国家
         * @param country 国家
         * @return
         * 返回值: void
         * 返回值说明：设置国家
         */
        public void setCountry(String country)
        {
            this.country = country;
        }

        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/5/16 07:16
         * 说明: 获取地区
         * @return
         * 返回值: String
         * 返回值说明：地区
         */
        public String getRegion()
        {
            return region;
        }

        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/5/16 07:16
         * 说明: 设置地区
         * @param region 地区
         * @return
         * 返回值: void
         * 返回值说明：设置地区
         */
        public void setRegion(String region)
        {
            this.region = region;
        }

        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/5/16 07:16
         * 说明: 获取省份
         * @return
         * 返回值: String
         * 返回值说明：省份
         */
        public String getProvince()
        {
            return province;
        }

        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/5/16 07:16
         * 说明: 设置省份
         * @param province 省份
         * @return
         * 返回值: void
         * 返回值说明：设置省份
         */
        public void setProvince(String province)
        {
            this.province = province;
        }

        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/5/16 07:16
         * 说明: 获取城市
         * @return
         * 返回值: String
         * 返回值说明：城市
         */
        public String getCity()
        {
            return city;
        }

        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/5/16 07:16
         * 说明: 设置城市
         * @param city 城市
         * @return
         * 返回值: void
         * 返回值说明：设置城市
         */
        public void setCity(String city)
        {
            this.city = city;
        }

        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/5/16 07:16
         * 说明: 获取运营商
         * @return
         * 返回值: String
         * 返回值说明：运营商
         */
        public String getIsp()
        {
            return isp;
        }

        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/5/16 07:16
         * 说明: 设置运营商
         * @param isp 运营商
         * @return
         * 返回值: void
         * 返回值说明：设置运营商
         */
        public void setIsp(String isp)
        {
            this.isp = isp;
        }

        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/5/16 07:16
         * 说明: 获取完整地址
         * @return
         * 返回值: String
         * 返回值说明：面向日志和页面展示的完整地址
         */
        public String getAddress()
        {
            return address;
        }

        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/5/16 07:16
         * 说明: 设置完整地址
         * @param address 面向日志和页面展示的完整地址
         * @return
         * 返回值: void
         * 返回值说明：设置完整地址
         */
        public void setAddress(String address)
        {
            this.address = address;
        }

        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/5/16 07:16
         * 说明: 获取数据来源
         * @return
         * 返回值: String
         * 返回值说明：数据来源，可能为pconline、ip2region、internal或unknown
         */
        public String getSource()
        {
            return source;
        }

        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/5/16 07:16
         * 说明: 设置数据来源
         * @param source 数据来源，可能为pconline、ip2region、internal或unknown
         * @return
         * 返回值: void
         * 返回值说明：设置数据来源
         */
        public void setSource(String source)
        {
            this.source = source;
        }

        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/5/16 07:16
         * 说明: 获取原始返回内容
         * @return
         * 返回值: String
         * 返回值说明：原始返回内容，用于排查地址库字段差异
         */
        public String getRawAddress()
        {
            return rawAddress;
        }

        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/5/16 07:16
         * 说明: 设置原始返回内容
         * @param rawAddress 原始返回内容，用于排查地址库字段差异
         * @return
         * 返回值: void
         * 返回值说明：设置原始返回内容
         */
        public void setRawAddress(String rawAddress)
        {
            this.rawAddress = rawAddress;
        }
    }
}
