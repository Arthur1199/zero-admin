package com.zero.constant;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 集中保存共享模块使用的服务标识，包含当前报表服务及历史系统的命名约定。
 */
public class ServiceNameConstant {

	/**
	 * 报表服务反向探测管理后台时使用的 Feign 目标名。部署时按实际注册名核对，主配置声明为 zero-admin。
	 */
	public static final String ZERO_ADMIN = "zeroAdmin";

	/**
	 * 零号报表服务名，必须与 zero-report 的 spring.application.name 保持一致，用于 admin 与 report 之间的跨服务调用。
	 */
	public static final String ZERO_REPORT = "zero-report";

	/** 历史系统的业务标识。 */
	public static final String SYSTEM_NAME = "Ucoin";
	
	/** 历史多语言翻译服务标识。 */
	public static final String UCOIN_LANGUAGE = "language-translation";
	
	/** 历史文件存储服务标识。 */
	public static final String UCOIN_FILE_SERVER = "ucoin-file-server";
	
    /** 历史前台用户服务标识。 */
    public static final String UCOIN_USER_CLIENT = "ucoin-user-client";
    
    /** 历史管理后台框架服务标识。 */
    public static final String UCOIN_MANAGE_ADMIN = "ucoin-manage-admin";

	/** 历史管理后台报表服务标识。 */
	public static final String UCOIN_MANAGE_REPORT = "ucoin-manage-report";

	/** 历史商户管理后台服务标识。 */
	public static final String UCOIN_MERCHANT_MANAGE_ADMIN = "ucoin-merchant-manage-admin";

	/** 历史商户报表服务标识。 */
	public static final String UCOIN_MERCHANT_MANAGE_REPORT = "ucoin-merchant-manage-report";
	
	/** 历史调度中心标识，当前调度中心主配置使用 zero-job-admin。 */
	public static final String JOB_ADMIN = "job-admin";
	
	/** 历史执行器服务标识，当前任务执行服务主配置使用 zero-task。 */
	public static final String JOB_EXECUTOR = "job-executor";
	
    /** 历史任务消息消费服务标识。 */
    public static final String UCOIN_TASK_LISTENER = "job-listener";

}
