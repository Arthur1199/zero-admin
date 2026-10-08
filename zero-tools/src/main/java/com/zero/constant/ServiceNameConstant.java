package com.zero.constant;

/**
 * 作者: Jack
 * 时间: 2024年8月30日15:19:25
 * 说明: 常用微服务名称定义
 *
 */
public class ServiceNameConstant {

	/**
	 * 零号后台管理服务名，必须与 zero-admin 的 spring.application.name 保持一致，用于服务注册发现和 Feign 调用。
	 */
	public static final String ZERO_ADMIN = "zeroAdmin";

	/**
	 * 零号报表服务名，必须与 zero-report 的 spring.application.name 保持一致，用于 admin 与 report 之间的跨服务调用。
	 */
	public static final String ZERO_REPORT = "zero-report";

	//系统名称 可以不进行使用
	public static final String SYSTEM_NAME = "Ucoin";
	
	//多语言服务 服务说明：可以支持多国语言，对全json内容进行全量翻译，可集成  ps：暂无需求，未集成  不需要开放外网
	public static final String UCOIN_LANGUAGE = "language-translation";
	
	// 文件上传服务 服务说明：支持各大主流平台的云存储服务，比如七牛云，腾讯云，亚马逊云等云存储桶服务。不需要开放外网
	public static final String UCOIN_FILE_SERVER = "ucoin-file-server";
	
	//前台用户服务 服务说明：用户端的服务，对外网提供用户的注册，报表服务等。 需要开放外网
    public static final String UCOIN_USER_CLIENT = "ucoin-user-client";
    
    //后台框架基础服务 服务说明：支撑后台的管理系统基础服务，支持菜单，按钮，后台用户权限管理等基础服务。 需要开放外网
    public static final String UCOIN_MANAGE_ADMIN = "ucoin-manage-admin";

	//后台业务服务 服务说明：用来提供后台报表的服务。不需要开放外网端口，该服务只提供给manage-admin服务进行微服务调用，不需要开放外网
	public static final String UCOIN_MANAGE_REPORT = "ucoin-manage-report";

	//UCOIN商户管理后台服务
	public static final String UCOIN_MERCHANT_MANAGE_ADMIN = "ucoin-merchant-manage-admin";

	//商户管理后台报表服务
	public static final String UCOIN_MERCHANT_MANAGE_REPORT = "ucoin-merchant-manage-report";
	
	//定时任务调度器服务  服务说明： xxl-job-admin 的定时任务调度服务 负责调用执行器去执行任务 需要开放外网
	public static final String JOB_ADMIN = "job-admin";
	
	//定时任务执行器服务 服务说明： xxl-job-executor 的定时任务执行器服务，负责讲定时任务以消息队列的形式发送到消息中间件kafka  不需要开放外网
	public static final String JOB_EXECUTOR = "job-executor";
	
    //定时任务消息队列消费服务 服务说明：用来消费kafka中的消息，可以用来拉取报表，定时统计报表等。不提供对外网的访问。不需要开放外网
    public static final String UCOIN_TASK_LISTENER = "job-listener";

}
