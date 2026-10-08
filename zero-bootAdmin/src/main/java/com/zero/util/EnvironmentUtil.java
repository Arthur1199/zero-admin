package com.zero.util;

import com.zero.config.SpringContextHolder;

/**
 * 作者: Jack
 * 时间: 2022年5月25日下午5:44:11
 * 说明: 微服务运行环境工具类
 *
 */
public class EnvironmentUtil {
	
	//本地
	private static final String DEV = "dev";
	//测试
	private static final String TEST = "test";
	//生产
	private static final String PROD = "prod";
	
	/**
	 * 作者:Jack
	 * 日期:2022年5月25日下午5:47:30
	 * @return
	 * 返回值:boolean
	 * 返回值说明:本地环境
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
	 * 作者:Jack
	 * 日期:2022年5月25日下午5:47:30
	 * @return
	 * 返回值:boolean
	 * 返回值说明:测试环境
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
	 * 作者:Jack
	 * 日期:2022年5月25日下午5:47:30
	 * @return
	 * 返回值:boolean
	 * 返回值说明:生产环境
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
