package com.zero.util;

import com.zero.config.SpringContextHolder;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 按唯一激活的 Spring Profile 判断 Boot Admin 所处部署环境，供监控消息选择环境文案。
 */
public class EnvironmentUtil {
	
	//本地
	private static final String DEV = "dev";
	//测试
	private static final String TEST = "test";
	//生产
	private static final String PROD = "prod";
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 按完整 Profile 名称识别 Boot Admin 的开发环境。
	 * @return
	 * 返回值: boolean
	 * 返回值说明: 仅有一个激活项且忽略大小写后等于 dev 时返回 true
	 */
	public static boolean isDev() {
		String[] activeProfiles = SpringContextHolder.getApplicationContext().getEnvironment().getActiveProfiles();
		 if(activeProfiles != null && activeProfiles.length == 1) {
		       	String activeEnv = activeProfiles[0];
		       	if(StringHelper.isNotEmpty(activeEnv)) {
		       		if(activeEnv.equalsIgnoreCase(DEV)) {
		       			return true;
		       		}
		       	}
		 }
		return false;
	}
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 按完整 Profile 名称识别 Boot Admin 的测试环境。
	 * @return
	 * 返回值: boolean
	 * 返回值说明: 仅有一个激活项且忽略大小写后等于 test 时返回 true
	 */
	public static boolean isTest() {
		String[] activeProfiles = SpringContextHolder.getApplicationContext().getEnvironment().getActiveProfiles();
		 if(activeProfiles != null && activeProfiles.length == 1) {
		       	String activeEnv = activeProfiles[0];
		       	if(StringHelper.isNotEmpty(activeEnv)) {
		       		if(activeEnv.equalsIgnoreCase(TEST)) {
		       			return true;
		       		}
		       	}
		 }
		return false;
	}
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 按 Profile 中的 prod 片段识别 Boot Admin 的生产环境。
	 * @return
	 * 返回值: boolean
	 * 返回值说明: 仅有一个激活项且包含小写 prod 时返回 true
	 */
	public static boolean isProd() {
		String[] activeProfiles = SpringContextHolder.getApplicationContext().getEnvironment().getActiveProfiles();
		 if(activeProfiles != null && activeProfiles.length == 1) {
		       	String activeEnv = activeProfiles[0];
		       	if(StringHelper.isNotEmpty(activeEnv)) {
                    if(activeEnv.contains(PROD)) {
		       			return true;
		       		}
		       	}
		 }
		return false;
	}

}
