package com.zero.config;

import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.redisson.spring.starter.RedissonAutoConfigurationCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/19 15:10
 * 说明: 保留报表缓存 JSON 序列化设置，并配置 Redisson 单节点连接池。
 */
@Component
public class RedissonAutoConfigCustomizer implements RedissonAutoConfigurationCustomizer {

	/** 单节点普通命令连接池上限。 */
	private final int connectionPoolSize;

	/** 单节点普通命令连接池的最小空闲连接数。 */
	private final int connectionMinimumIdleSize;

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/19 14:47
	 * 说明: 优先读取公共连接池参数，兼容旧服务前缀和 Lettuce 连接池配置。
	 * @param connectionPoolSize 连接池最大连接数，必须大于零
	 * @param connectionMinimumIdleSize 最小空闲连接数，范围为零到连接池上限
	 */
	public RedissonAutoConfigCustomizer(
			@Value("${zero.redis.connection-pool-size:${zero.report.redis.connection-pool-size:${spring.data.redis.lettuce.pool.max-active:8}}}")
			int connectionPoolSize,
			@Value("${zero.redis.connection-minimum-idle-size:${zero.report.redis.connection-minimum-idle-size:${spring.data.redis.lettuce.pool.min-idle:0}}}")
			int connectionMinimumIdleSize) {
		this.connectionPoolSize = connectionPoolSize;
		this.connectionMinimumIdleSize = connectionMinimumIdleSize;
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/19 14:47
	 * 说明: 客户端启动前应用 JSON 编解码器和单节点连接池参数。
	 * @param cfg Starter 根据 Spring Redis 连接配置构造的客户端配置
	 * @return
	 * 返回值: void
	 * 返回值说明: 调整 Redisson 编解码器和普通连接池参数
	 */
	@Override
	public void customize(Config cfg) {
		//cfg.setCodec(new org.redisson.codec.FstCodec());
//		cfg.setCodec(new org.redisson.codec.SerializationCodec());
//		SentinelServersConfig useSentinelServers = cfg.useSentinelServers();
//		useSentinelServers.setCheckSentinelsList(false);
//		SingleServerConfig useSingleServer = cfg.useSingleServer();
		cfg.setCodec(new JsonJacksonCodec());
		// 与原 Redis 客户端一样，在首次访问时建立连接，保持服务独立启动。
		cfg.setLazyInitialization(true);
		if (cfg.isSingleConfig()) {
			if (connectionPoolSize < 1 || connectionMinimumIdleSize < 0
					|| connectionMinimumIdleSize > connectionPoolSize) {
				throw new IllegalArgumentException("Redisson 连接池上限必须大于零，最小空闲连接数须在零与上限之间");
			}
			cfg.useSingleServer().setConnectionPoolSize(connectionPoolSize)
					.setConnectionMinimumIdleSize(connectionMinimumIdleSize);
		}
	}

}
