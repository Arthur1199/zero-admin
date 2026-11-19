//package com.zero.jobadmin.core.jobbean;
//
//import com.zero.jobadmin.core.thread.JobTriggerPoolHelper;
//import com.zero.jobadmin.core.trigger.TriggerTypeEnum;
//import org.quartz.JobExecutionContext;
//import org.quartz.JobExecutionException;
//import org.quartz.JobKey;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.scheduling.quartz.QuartzJobBean;
//
///**
// * http job bean
// * “@DisallowConcurrentExecution” disable concurrent, thread size can not be only one, better given more
// * 作者: Arthur 亚瑟
// * 时间: 2026/11/20 01:36
// */
////@DisallowConcurrentExecution
//public class RemoteHttpJobBean extends QuartzJobBean {
//	private static Logger logger = LoggerFactory.getLogger(RemoteHttpJobBean.class);
//
//	@Override
//	protected void executeInternal(JobExecutionContext context)
//			throws JobExecutionException {
//
//		// load jobId
//		JobKey jobKey = context.getTrigger().getJobKey();
//		Integer jobId = Integer.valueOf(jobKey.getName());
//
//
//	}
//
//}