package com.zero.jobadmin.business.scheduler.thread;

import com.zero.jobadmin.business.model.XxlJobGroup;
import com.zero.jobadmin.business.model.XxlJobRegistry;
import com.zero.jobadmin.business.scheduler.config.XxlJobAdminBootstrap;
import com.zero.jobadmin.business.util.ExecutorAddressUtil;
import com.xxl.job.core.constant.Const;
import com.xxl.job.core.constant.RegistTypeEnum;
import com.xxl.job.core.openapi.admin.dto.RegistryRequest;
import com.xxl.tool.concurrent.CyclicThread;
import com.xxl.tool.core.StringTool;
import com.xxl.tool.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

import java.util.*;

/**
 *
 * 作者: xuxueli
 * 时间: 2016/10/2 19:10:24
 * 说明: 写入执行器心跳和下线状态，定期维护在线地址。
 */
public class JobRegistryHelper {
	private static final Logger logger = LoggerFactory.getLogger(JobRegistryHelper.class);

	/** 每轮最多清理的过期心跳数，限制 SQL 参数和删除事务规模。 */
	private static final int REGISTRY_CLEANUP_BATCH_SIZE = 1000;

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
	 * 说明: 接收注册请求并启动周期监控，按心跳维护在线地址，过期快照留待下一轮刷新。
	 * @return
	 * 返回值: void
	 * 返回值说明: 初始化并启动后台线程
	 */
	public void start(){

		stopping = false;
		registryMonitorThread = new CyclicThread("JobRegistryHelper#registryMonitorThread", true,
				this::refreshRegistry, Const.BEAT_TIMEOUT * 1000L, true);
		registryMonitorThread.start();
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/18 22:27
	 * 说明: 每轮限量清理过期心跳，清理与自动注册组地址刷新独立执行。
	 * @return
	 * 返回值: void
	 * 返回值说明: 保留查询后完成续约的节点，更新仍有效的自动注册组快照
	 */
	private void refreshRegistry() {
		var bootstrap = XxlJobAdminBootstrap.getInstance();
		Date now = new Date();
		var registryMapper = bootstrap.getXxlJobRegistryMapper();
		try {
			List<Long> expiredIds = registryMapper.findDead(Const.DEAD_TIMEOUT, now, REGISTRY_CLEANUP_BATCH_SIZE);
			if (expiredIds != null && !expiredIds.isEmpty()) {
				registryMapper.removeDead(expiredIds, Const.DEAD_TIMEOUT, now);
			}
		} catch (DataAccessException e) {
			// 在线地址查询自己会排除过期记录，本轮仍可继续刷新。
			logger.warn("Failed to clean expired registry entries", e);
		}

		var groupMapper = bootstrap.getXxlJobGroupMapper();
		List<XxlJobGroup> groups = groupMapper.findByAddressType(0);
		if (groups == null || groups.isEmpty()) {
			return;
		}

		Map<String, Set<String>> addressesByApp = new HashMap<>();
		List<XxlJobRegistry> activeNodes = registryMapper.findAll(Const.DEAD_TIMEOUT, now);
		if (activeNodes != null) {
			for (XxlJobRegistry node : activeNodes) {
				if (RegistTypeEnum.EXECUTOR.name().equals(node.getRegistryGroup())
						&& StringTool.isNotBlank(node.getRegistryValue())) {
					addressesByApp.computeIfAbsent(node.getRegistryKey(), key -> new TreeSet<>())
							.add(node.getRegistryValue());
				}
			}
		}
		for (XxlJobGroup group : groups) {
			Set<String> addresses = addressesByApp.get(group.getAppname());
			String addressList = addresses == null ? null : String.join(",", addresses);
			// 管理员或其他调度节点可能已更新该组，旧快照交给下一轮重读。
			groupMapper.refreshAutoRegisteredAddresses(group, addressList, now);
		}
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
	 * 说明: 校验注册记录所需字段，避免地址超过注册表及调度日志列容量。
	 * @param registryParam 执行器注册信息，可为空
	 * @return
	 * 返回值: boolean
	 * 返回值说明: 类别、应用名及执行器地址均有效时返回 true
	 */
	private boolean isValidRegistry(RegistryRequest registryParam) {
		return registryParam != null && StringTool.isNotBlank(registryParam.getRegistryGroup())
				&& StringTool.isNotBlank(registryParam.getRegistryKey())
				&& ExecutorAddressUtil.fitsAddressColumn(registryParam.getRegistryValue());
	}
}
