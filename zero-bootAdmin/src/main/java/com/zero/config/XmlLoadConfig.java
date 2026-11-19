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
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 在 Boot Admin 应用就绪后加载 XML 配置，并初始化当前环境的 Slack 通知设置。
 */
@Slf4j
@Configuration
public class XmlLoadConfig implements ApplicationListener<ApplicationReadyEvent>{

	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 应用启动完成后读取 XML 配置并初始化 Slack 机器人配置。
	 * @param event Spring 应用启动完成事件
	 * @return
	 * 返回值: void
	 * 返回值说明: 加载配置并在配置可用时初始化通知发送器
	 */
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
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 根据唯一激活的 Profile 读取 Telegram 通知配置，缺少唯一激活项时回退 dev。
     * @return
     * 返回值: void
     * 返回值说明: 仅在能解析出有效的机器人凭据和会话标识时初始化发送配置
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
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 读取当前环境的 Slack 频道及授权信息，唯一激活的 Profile 决定配置项，其他情况取 dev。
	 * @return
	 * 返回值: void
	 * 返回值说明: 配置完整时将请求地址及频道信息交给 Slack 发送器
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
		// 各环境使用同一 Slack 请求地址，频道及授权按环境选择。
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
