package com.zero.config;

import lombok.extern.slf4j.Slf4j;
import com.zero.util.SlackRobotUtil;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.zero.util.StringHelper;
import com.zero.util.TelRobotUtil;
import com.zero.util.XmlConfig;

/**
 * 作者: Jack
 * 时间: 2019年1月18日下午2:19:46
 * 说明: 应用程序初始化完成，加载配置文件
 *
 */
@Slf4j
@Configuration
public class XmlLoadConfig implements ApplicationListener<ApplicationReadyEvent>{

	
	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("服务初始化完成，开始装载 [configuration.xml] 配置文件");
//		ConfigurableApplicationContext applicationContext = event.getApplicationContext();
		XmlConfig.loadConfig();
		//加载teleGram机器人配置
//		this.loadingRobotConfig();
		//加载Slack电报机器人配置
		loadingSlackRobotConfig();
	}
	
    /**
     * 作者:Jack
     * 日期:2023年4月21日下午2:56:50
     * 返回值:void
     * 返回值说明:加载telegram机器人配置
     */
    private void loadingRobotConfig() {
    	Environment env = SpringContextHolder.getApplicationContext().getEnvironment();
        String[] activeProfiles = env.getActiveProfiles();
        //执行环境
        String active = activeProfiles != null && activeProfiles.length == 1 ? activeProfiles[0] : "dev";
    	String configValue  = XmlConfig.getString("robotConfig." + active);
    	if(StringHelper.isEmpty(configValue)) {
            log.error("当前执行环境:"+active+",获取teleGram机器人配置信息失败！");
    		return;
    	}
    	String[] split = configValue.split("\\|");
    	if(split != null && split.length == 2) {
    		String robotToken = split[0];
    		String chatId = split[1];
            log.info("当前执行环境:{},Telegram机器人配置已加载，chatId:{}", active, chatId);
    		if(!StringHelper.isEmpty(robotToken) && !StringHelper.isEmpty(chatId)) {
    			TelRobotUtil.setROBOT_TOKEN(robotToken);
    			TelRobotUtil.setROBOT_CHAT_ID(chatId);
    		}
    	}else {
            log.error("当前执行环境:"+active+",teleGram机器人配置信息错误！");
    	}
    }

	/**
	 * 作者:Jack
	 * 日期:2023年4月21日下午2:56:50
	 * 返回值:void
	 * 返回值说明:加载slack机器人配置
	 */
	private void loadingSlackRobotConfig() {
		Environment env = SpringContextHolder.getApplicationContext().getEnvironment();
		String[] activeProfiles = env.getActiveProfiles();
		//执行环境
		String active = activeProfiles != null && activeProfiles.length == 1 ? activeProfiles[0] : "dev";
		String configValue  = XmlConfig.getString("slackRobotConfig." + active);
		if(StringHelper.isEmpty(configValue)) {
            log.error("当前执行环境:"+active+",获取Slack机器人配置信息失败！");
			return;
		}
		// 电报机器人请求url
		String requestApi = XmlConfig.getString("slackRobotConfig.api");
		String[] split = configValue.split("\\|");
		if(split != null && split.length == 2) {
			String channel = split[0];
			String authorization = split[1];
            log.info("当前执行环境:{},Slack机器人配置已加载，channel:{}", active, channel);
			if(!StringHelper.isEmpty(authorization) && !StringHelper.isEmpty(channel)) {
				SlackRobotUtil.initConfig(requestApi,channel,authorization);
			}
		}else {
            log.error("当前执行环境:"+active+",Slack机器人配置信息错误！");
		}
	}

}
