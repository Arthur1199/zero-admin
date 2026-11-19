package com.zero.util;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 读取任务执行服务当前 Spring Profile，供环境显示和按环境执行的逻辑使用。显式激活项优先，缺少激活项时读取默认项。
 */
public final class EnvironmentUtil {

	/** 本地开发环境profile名称，匹配时忽略大小写。 */
	private static final String DEV = "dev";
	/** 测试环境profile名称，匹配时忽略大小写。 */
	private static final String TEST = "test";
	/** 生产环境profile名称，匹配时忽略大小写。 */
	private static final String PROD = "prod";

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 获取运行时候的中文运行环境
     * @return
     * 返回值: String
     * 返回值说明: 获取运行时候的中文运行环境
     */
    public static String  getEnvZhName(){
        if (isDev()){
            return "本地环境";
        }
        if (isTest()){
            return "测试环境";
        }
        if (isProd()){
            return "生产环境";
        }
        return "未知";
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 获取运行时候的英文运行环境
     * @return
     * 返回值: String
     * 返回值说明: 获取运行时候的英文运行环境
     */
    public static String  getEnvEnName(){
        if (isDev()){
            return "dev";
        }
        if (isTest()){
            return "test";
        }
        if (isProd()){
            return "prod";
        }
        return "unknown";
    }

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 私有构造方法，用于限制环境工具的使用方式。
	 */
	private EnvironmentUtil() {
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 判断开发 Profile 是否参与当前部署配置。
	 * @return
	 * 返回值: boolean
	 * 返回值说明: 有效 Profile 中包含 dev 时返回 true，忽略大小写
	 */
	public static boolean isDev() {
		return hasProfile(DEV);
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 判断测试 Profile 是否参与当前部署配置。
	 * @return
	 * 返回值: boolean
	 * 返回值说明: 有效 Profile 中包含 test 时返回 true，忽略大小写
	 */
	public static boolean isTest() {
		return hasProfile(TEST);
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 判断生产 Profile 是否参与当前部署配置。
	 * @return
	 * 返回值: boolean
	 * 返回值说明: 有效 Profile 中包含 prod 时返回 true，忽略大小写
	 */
	public static boolean isProd() {
		return hasProfile(PROD);
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 每次调用都重新读取有效 Profile，允许多个 Profile 分别匹配。
	 * @param profile 当前方法需要判断的部署环境名称
	 * @return
	 * 返回值: boolean
	 * 返回值说明: 找到匹配项时返回 true，容器未就绪、已关闭或环境缺失时返回 false
	 */
	private static boolean hasProfile(String profile) {
		ApplicationContext context = SpringContextUtils.getApplicationContext();
		if (context == null || (context instanceof ConfigurableApplicationContext configurableContext
				&& !configurableContext.isActive())) {
			return false;
		}
		Environment environment = context.getEnvironment();
		if (environment == null) {
			return false;
		}
		String[] profiles = environment.getActiveProfiles();
		if (profiles == null || profiles.length == 0) {
			profiles = environment.getDefaultProfiles();
		}
		if (profiles != null) {
			for (String candidate : profiles) {
				if (profile.equalsIgnoreCase(candidate)) {
					return true;
				}
			}
		}
		return false;
	}

}
