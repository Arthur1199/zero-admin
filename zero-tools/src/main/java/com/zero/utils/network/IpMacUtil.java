package com.zero.utils.network;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

import jakarta.servlet.http.HttpServletRequest;
import com.zero.utils.common.StringHelper;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/16 23:16
 * 说明: Ip工具类
 */
public class IpMacUtil {

	private static final String IPV6_LOCAL = "0:0:0:0:0:0:0:1";
	private static final String LOCAL_IP = "127.0.0.1";
	private static final String LOCAL_HOST = "localhost";

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:17
	 * @param ip ip地址
	 * @return
	 * 返回值:String
	 * 返回值说明：隐藏IP的最后一段
	 */
	public static String hideIp(String ip) {
		if (StringHelper.isEmpty(ip)) {
			return "";
		}
		int pos = ip.lastIndexOf(".");
		if (pos == -1) {
			return ip;
		}

		ip = ip.substring(0, pos + 1);
		ip = ip + "*";
		return ip;
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:17
	 * @param request 请求
	 * @return
	 * 返回值:String
	 * 返回值说明：获取IP地址.
	 */
	public static String getIpAddr(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		if (LOCAL_IP.equalsIgnoreCase(ip) || LOCAL_HOST.equalsIgnoreCase(ip) || IPV6_LOCAL.equalsIgnoreCase(ip)) {
			ip = getLocalhostIp();
		}
		return ip;
	}

	public static String getLocalhostIp() {
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 判断该字串是否为IP
	 * 
	 * @param ipStr IP字串
	 * @return
	 */
	public static boolean isIP(String ipStr) {
//		String ip = "(25[0-5]|2[0-4]\\d|1\\d\\d|\\d\\d|\\d)";
//		String ipDot = ip + "\\.";
//		return ipStr.matches(ipDot + ipDot + ipDot + ip);
		String ip = "([1-9]|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]).(\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]).(\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]).(\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5])$";
		return  Pattern.matches(ip, ipStr);
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:17
	 * @param ip ip
	 * @return
	 * 返回值:String
	 * 返回值说明： 获取客户端Mac
	 */
	public static String getMACAddress(String ip) {
		String str = "";
		String macAddress = "";
		try {
			Process p = Runtime.getRuntime().exec("nbtstat -A " + ip);
			InputStreamReader ir = new InputStreamReader(p.getInputStream());
			LineNumberReader input = new LineNumberReader(ir);
			for (int i = 1; i < 100; i++) {
				str = input.readLine();
				if (str != null) {
					if (str.indexOf("MAC Address") > 1) {
						macAddress = str.substring(str.indexOf("MAC Address") + 14, str.length());
						break;
					}
				}
			}
		} catch (IOException e) {
			return "";
		}
		return macAddress;
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2025/6/29 20:00
	 * @param ipAddress ip地址
	 * @return
	 * 返回值:boolean
	 * 返回值说明：判断ip是否是内网ip
	 */
	public static boolean isInternalIP(String ipAddress) {
		try {
			InetAddress address = InetAddress.getByName(ipAddress);

			// 检查是否为回环地址
			if (address.isLoopbackAddress()) {
				return true;
			}

			// 检查是否为链路本地地址
			if (address.isLinkLocalAddress()) {
				return true;
			}

			// 检查是否为站点本地地址（已弃用，但仍需兼容）
			if (address.isSiteLocalAddress()) {
				return true;
			}

			// 手动检查常见的私有 IP 范围
			byte[] bytes = address.getAddress();
			if (bytes.length == 4) { // IPv4
				return isPrivateIPv4(bytes);
			}

			// 对于 IPv6，通常不区分内网外网，但可以根据需求扩展判断逻辑
			return false;
		} catch (UnknownHostException e) {
			// 无效的 IP 地址
			return false;
		}
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:16
	 * @param bytes 字节
	 * @return
	 * 返回值:boolean
	 * 返回值说明：是否ipV4
	 */
	private static boolean isPrivateIPv4(byte[] bytes) {
		int firstByte = bytes[0] & 0xFF;
		int secondByte = bytes[1] & 0xFF;

		// A 类私有地址: 10.0.0.0/8
		if (firstByte == 10) {
			return true;
		}

		// B 类私有地址: 172.16.0.0/12
		if (firstByte == 172 && (secondByte >= 16 && secondByte <= 31)) {
			return true;
		}

		// C 类私有地址: 192.168.0.0/16
		if (firstByte == 192 && secondByte == 168) {
			return true;
		}

		// 链路本地地址: 169.254.0.0/16
		if (firstByte == 169 && secondByte == 254) {
			return true;
		}

		return false;
	}

	public static void main(String[] args) {
		boolean ip = IpMacUtil.isIP("1.0.1.0");
		System.out.println(ip);
		// 测试示例
		System.out.println(isInternalIP("10.0.0.1"));      // true
		System.out.println(isInternalIP("172.16.0.1"));    // true
		System.out.println(isInternalIP("192.168.0.1"));   // true
		System.out.println(isInternalIP("127.0.0.1"));     // true
		System.out.println(isInternalIP("169.254.0.1"));   // true
		System.out.println(isInternalIP("8.8.8.8"));       // false
		System.out.println(isInternalIP("2001:0db8:85a3:0000:0000:8a2e:0370:7334")); // false (IPv6)
	}
	
}
