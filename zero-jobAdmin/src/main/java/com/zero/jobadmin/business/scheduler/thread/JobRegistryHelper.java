package com.zero.jobadmin.business.scheduler.thread;

import com.zero.jobadmin.business.model.XxlJobGroup;
import com.zero.jobadmin.business.model.XxlJobRegistry;
import com.zero.jobadmin.business.scheduler.config.XxlJobAdminBootstrap;
import com.xxl.job.core.constant.Const;
import com.xxl.job.core.constant.RegistTypeEnum;
import com.xxl.job.core.openapi.admin.dto.RegistryRequest;
import com.xxl.tool.concurrent.CyclicThread;
import com.xxl.tool.core.CollectionTool;
import com.xxl.tool.core.StringTool;
import com.xxl.tool.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 *
 * 作者: xuxueli
 * 时间: 2016/10/2 19:10:24
 * 说明: 写入执行器心跳和下线状态，定期维护在线地址。
 */
public class JobRegistryHelper {
	private static final Logger logger = LoggerFactory.getLogger(JobRegistryHelper.class);


	/** 停止时拒绝新的心跳和下线请求。 */
	private volatile boolean stopping;

	/**
	 * registry monitor thread
	 */
	private CyclicThread registryMonitorThread;

	/**
	 *
	 * 作者: xuxueli
	 * 时间: 2016/10/2 19:10
	 * 说明: 接收注册请求并启动周期监控，按心跳维护在线地址。
	 * @return
	 * 返回值: void
	 * 返回值说明: 初始化并启动后台线程
	 */
	public void start(){

		stopping = false;
		registryMonitorThread = new CyclicThread("JobRegistryHelper#registryMonitorThread", true, new Runnable() {
			@Override
			public void run() {
				// 2.1、refresh auto-registry group
				List<XxlJobGroup> groupList = XxlJobAdminBootstrap.getInstance().getXxlJobGroupMapper().findByAddressType(0);
				if (groupList!=null && !groupList.isEmpty()) {
					Date now = new Date();
					var registryMapper = XxlJobAdminBootstrap.getInstance().getXxlJobRegistryMapper();

					// a、remove dead address (admin/executor)
					List<Long> ids = registryMapper.findDead(Const.DEAD_TIMEOUT, now);
					if (ids!=null && !ids.isEmpty()) {
						registryMapper.removeDead(ids, Const.DEAD_TIMEOUT, now);
					}

					// b、fresh online address (appname ： List<address>)
					HashMap<String, List<String>> appnameAddressMap = new HashMap<>();
					List<XxlJobRegistry> list = registryMapper.findAll(Const.DEAD_TIMEOUT, now);
					if (list != null) {
						for (XxlJobRegistry item: list) {
							if (RegistTypeEnum.EXECUTOR.name().equals(item.getRegistryGroup())) {
								String appname = item.getRegistryKey();
								List<String> registryList = appnameAddressMap.get(appname);
								if (registryList == null) {
									registryList = new ArrayList<String>();
								}

								if (!registryList.contains(item.getRegistryValue())) {
									registryList.add(item.getRegistryValue());
								}
								appnameAddressMap.put(appname, registryList);
							}
						}
					}

					// c、write group address
					for (XxlJobGroup group: groupList) {
						List<String> registryList = appnameAddressMap.get(group.getAppname());
						// generate address list
						String addressListStr = null;
						if (CollectionTool.isNotEmpty(registryList)) {
							Collections.sort(registryList);
							addressListStr = StringTool.join(registryList, ",");
						}
						// 监控读到旧配置后，管理员仍可能切换为手动录入。
						XxlJobAdminBootstrap.getInstance().getXxlJobGroupMapper()
								.refreshAutoRegisteredAddresses(group.getId(), addressListStr, now);
					}
				}

			}
		}, Const.BEAT_TIMEOUT * 1000L, true);
		registryMonitorThread.start();
	}


	/**
	 *
	 * 作者: xuxueli
	 * 时间: 2016/10/2 19:10:24
	 * 说明: 拒绝新的注册写入并停止周期监控。
	 * @return
	 * 返回值: void
	 * 返回值说明: 已开始写入的请求返回各自的实际结果
	 */
	public void stop(){
		stopping = true;
		if (registryMonitorThread != null) {
			registryMonitorThread.stop();
		}
	}


	// ---------------------- tool ----------------------

	/**
	 *
	 * 作者: xuxueli
	 * 时间: 2016/10/2 19:10:24
	 * 说明: 写入当前执行器的注册或心跳后确认成功。
	 * @param registryParam 已认证应用的执行器注册信息
	 * @return
	 * 返回值: Response<String>
	 * 返回值说明: 执行器已注册或心跳已更新时返回成功，同秒的重复心跳也视为成功
	 */
	public Response<String> registry(RegistryRequest registryParam) {
		if (!isValidRegistry(registryParam)) {
			return Response.ofFail("Illegal Argument.");
		}
		if (stopping) {
			return Response.ofFail("Registry service is stopping.");
		}
		try {
			XxlJobAdminBootstrap.getInstance().getXxlJobRegistryMapper().registrySaveOrUpdate(
					registryParam.getRegistryGroup(), registryParam.getRegistryKey(), registryParam.getRegistryValue(), new Date());
			return Response.ofSuccess();
		} catch (RuntimeException e) {
			logger.error("Failed to write executor heartbeat", e);
			return Response.ofFail("Failed to save executor heartbeat.");
		}
	}

	/**
	 *
	 * 作者: xuxueli
	 * 时间: 2016/10/2 19:10:24
	 * 说明: 删除指定执行器的注册状态后确认下线。
	 * @param registryParam 已认证应用的执行器注册信息
	 * @return
	 * 返回值: Response<String>
	 * 返回值说明: 当前注册已移除或不存在时返回成功
	 */
	public Response<String> registryRemove(RegistryRequest registryParam) {
		if (!isValidRegistry(registryParam)) {
			return Response.ofFail("Illegal Argument.");
		}
		if (stopping) {
			return Response.ofFail("Registry service is stopping.");
		}
		try {
			XxlJobAdminBootstrap.getInstance().getXxlJobRegistryMapper().registryDelete(
					registryParam.getRegistryGroup(), registryParam.getRegistryKey(), registryParam.getRegistryValue());
			return Response.ofSuccess();
		} catch (RuntimeException e) {
			logger.error("Failed to remove executor registry", e);
			return Response.ofFail("Failed to remove executor registry.");
		}
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/18 21:00
	 * 说明: 校验定位执行器注册记录所需的三个字段。
	 * @param registryParam 执行器注册信息，可为空
	 * @return
	 * 返回值: boolean
	 * 返回值说明: 类别、应用名及执行器地址均有效时返回 true
	 */
	private boolean isValidRegistry(RegistryRequest registryParam) {
		return registryParam != null && StringTool.isNotBlank(registryParam.getRegistryGroup())
				&& StringTool.isNotBlank(registryParam.getRegistryKey())
				&& StringTool.isNotBlank(registryParam.getRegistryValue());
	}
}
