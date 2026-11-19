package com.zero.util;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

/**
 *
 * 作者: Jack
 * 时间: 2022年5月25日下午5:44:11
 * 说明: 管理后台运行环境判断，支持多个激活profile；无激活项时使用Spring默认profile
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
     * 时间: 2026/11/18 14:45
     * @return
     * 返回值:String
     * 返回值说明：获取运行时候的中文运行环境
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
     * 时间: 2026/11/18 14:46
     * @return
     * 返回值:String
     * 返回值说明：获取运行时候的英文运行环境
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
	 * 作者: Jack
	 * 时间: 2026/11/18
	 * 说明: 仅提供静态环境判断方法，不允许实例化
	 */
	private EnvironmentUtil() {
	}

	/**
	 *
	 * 作者:Jack
	 * 时间:2022年5月25日下午5:47:30
	 * 说明: 判断有效profile中是否包含dev，不要求它是唯一激活项
	 * @return
	 * 返回值:boolean
	 * 返回值说明:包含dev时为true，上下文未就绪或已关闭时为false
	 */
	public static boolean isDev() {
		return hasProfile(DEV);
	}

	/**
	 *
	 * 作者:Jack
	 * 时间:2022年5月25日下午5:47:30
	 * 说明: 判断有效profile中是否包含test，不要求它是唯一激活项
	 * @return
	 * 返回值:boolean
	 * 返回值说明:包含test时为true，上下文未就绪或已关闭时为false
	 */
	public static boolean isTest() {
		return hasProfile(TEST);
	}

	/**
	 *
	 * 作者:Jack
	 * 时间:2022年5月25日下午5:47:30
	 * 说明: 判断有效profile中是否包含prod，不要求它是唯一激活项
	 * @return
	 * 返回值:boolean
	 * 返回值说明:包含prod时为true，上下文未就绪或已关闭时为false
	 */
	public static boolean isProd() {
		return hasProfile(PROD);
	}

	/**
	 *
	 * 作者: Jack
	 * 时间: 2026/11/18
	 * 说明: 每次读取当前环境，避免缓存旧profile；多个profile可分别匹配不同判断方法，不做环境优先级推断
	 * @param profile 待匹配的环境名称，由本类常量提供
	 * @return
	 * 返回值: boolean
	 * 返回值说明: 任一有效profile忽略大小写匹配即返回true，无匹配或上下文不可用时返回false
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
