package com.zero.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.mock.env.MockEnvironment;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 *
 * 作者: Jack
 * 时间: 2026/11/18
 * 说明: 环境工具类边界回归，隔离静态上下文读取，不启动数据库、Redis或Nacos
 */
class EnvironmentUtilTest {
	/** 仅在当前测试线程生效，不改写用户的真实静态上下文字段。 */
	private MockedStatic<SpringContextUtils> contextAccess;
	/** 默认应用上下文替身。 */
	private ApplicationContext context;
	/** 可在测试中切换的真实Spring环境实现。 */
	private MockEnvironment environment;

	/**
	 *
	 * 作者: Jack
	 * 时间: 2026/11/18
	 * 说明: 隔离上下文读取，避免测试依赖应用启动顺序
	 * @return
	 * 返回值: void
	 * 返回值说明: 每个测试使用独立环境
	 */
	@BeforeEach
	void setup() {
		contextAccess = mockStatic(SpringContextUtils.class);
		context = mock(ApplicationContext.class);
		environment = new MockEnvironment();
		contextAccess.when(SpringContextUtils::getApplicationContext).thenReturn(context);
		when(context.getEnvironment()).thenReturn(environment);
	}

	/**
	 *
	 * 作者: Jack
	 * 时间: 2026/11/18
	 * 说明: 释放静态方法替身，不影响其它测试或真实上下文
	 * @return
	 * 返回值: void
	 * 返回值说明: 解除当前线程的静态调用拦截
	 */
	@AfterEach
	void cleanup() {
		contextAccess.close();
	}

	/**
	 *
	 * 作者: Jack
	 * 时间: 2026/11/18
	 * 说明: Spring尚未提供上下文时不能出现空指针或误判为开发环境
	 * @return
	 * 返回值: void
	 * 返回值说明: 三种环境判断全部为false
	 */
	@Test
	void missingContextDoesNotThrowOrGuessEnvironment() {
		contextAccess.when(SpringContextUtils::getApplicationContext).thenReturn(null);
		assertThat(EnvironmentUtil.isDev()).isFalse();
		assertThat(EnvironmentUtil.isTest()).isFalse();
		assertThat(EnvironmentUtil.isProd()).isFalse();
	}

	/**
	 *
	 * 作者: Jack
	 * 时间: 2026/11/18
	 * 说明: 不完整上下文没有Environment时安全返回，不读取其它全局配置兜底
	 * @return
	 * 返回值: void
	 * 返回值说明: 不发生空指针
	 */
	@Test
	void missingEnvironmentReturnsFalse() {
		when(context.getEnvironment()).thenReturn(null);
		assertThat(EnvironmentUtil.isDev()).isFalse();
		assertThat(EnvironmentUtil.isTest()).isFalse();
		assertThat(EnvironmentUtil.isProd()).isFalse();
	}

	/**
	 *
	 * 作者: Jack
	 * 时间: 2026/11/18
	 * 说明: 部署环境与功能profile同时激活时仍能识别，多个环境名不推断优先级
	 * @return
	 * 返回值: void
	 * 返回值说明: dev与附加profile共存可识别，同时激活三个环境名时三个方法均为true
	 */
	@Test
	void supportsMultipleActiveProfilesWithoutInventingPriority() {
		environment.setActiveProfiles("feature", "dev");
		assertThat(EnvironmentUtil.isDev()).isTrue();
		assertThat(EnvironmentUtil.isProd()).isFalse();
		environment.setActiveProfiles("dev", "test", "prod", "feature");
		assertThat(EnvironmentUtil.isDev()).isTrue();
		assertThat(EnvironmentUtil.isTest()).isTrue();
		assertThat(EnvironmentUtil.isProd()).isTrue();
	}

	/**
	 *
	 * 作者: Jack
	 * 时间: 2026/11/18
	 * 说明: 保留旧工具忽略大小写但不做子串匹配的行为
	 * @return
	 * 返回值: void
	 * 返回值说明: PrOd可识别，production等其它名称不会被误认为prod
	 */
	@Test
	void matchesWholeProfileIgnoringCase() {
		environment.setActiveProfiles("PrOd", "TeSt");
		assertThat(EnvironmentUtil.isProd()).isTrue();
		assertThat(EnvironmentUtil.isTest()).isTrue();
		assertThat(EnvironmentUtil.isDev()).isFalse();
		environment.setActiveProfiles("production", "dev-extra");
		assertThat(EnvironmentUtil.isProd()).isFalse();
		assertThat(EnvironmentUtil.isDev()).isFalse();
	}

	/**
	 *
	 * 作者: Jack
	 * 时间: 2026/11/18
	 * 说明: 仅当没有激活项时使用Spring默认profile，显式激活项优先
	 * @return
	 * 返回值: void
	 * 返回值说明: 默认dev生效，激活prod后默认dev不再参与匹配
	 */
	@Test
	void defaultProfilesApplyOnlyWithoutActiveProfiles() {
		environment.setDefaultProfiles("dev", "feature");
		assertThat(EnvironmentUtil.isDev()).isTrue();
		environment.setActiveProfiles("prod");
		assertThat(EnvironmentUtil.isDev()).isFalse();
		assertThat(EnvironmentUtil.isProd()).isTrue();
	}

	/**
	 *
	 * 作者: Jack
	 * 时间: 2026/11/18
	 * 说明: Spring内置default不等于业务dev，未指定环境时不擅自认定部署类型
	 * @return
	 * 返回值: void
	 * 返回值说明: 三种环境判断全部为false
	 */
	@Test
	void springDefaultIsNotImplicitlyDevelopment() {
		assertThat(EnvironmentUtil.isDev()).isFalse();
		assertThat(EnvironmentUtil.isTest()).isFalse();
		assertThat(EnvironmentUtil.isProd()).isFalse();
	}

	/**
	 *
	 * 作者: Jack
	 * 时间: 2026/11/18
	 * 说明: 环境配置变化后重新读取，不把第一次判断缓存为永久结果
	 * @return
	 * 返回值: void
	 * 返回值说明: dev切换prod后立即匹配新值
	 */
	@Test
	void readsCurrentProfilesOnEveryCall() {
		environment.setActiveProfiles("dev");
		assertThat(EnvironmentUtil.isDev()).isTrue();
		environment.setActiveProfiles("prod");
		assertThat(EnvironmentUtil.isDev()).isFalse();
		assertThat(EnvironmentUtil.isProd()).isTrue();
	}

	/**
	 *
	 * 作者: Jack
	 * 时间: 2026/11/18
	 * 说明: 容器未刷新或已关闭时不继续返回陈旧的环境判断
	 * @return
	 * 返回值: void
	 * 返回值说明: 容器生效期间返回true，关闭后返回false
	 */
	@Test
	void configurableContextMustBeActive() {
		try (var application = new GenericApplicationContext()) {
			application.getEnvironment().setActiveProfiles("dev");
			contextAccess.when(SpringContextUtils::getApplicationContext).thenReturn(application);
			assertThat(EnvironmentUtil.isDev()).isFalse();
			application.refresh();
			assertThat(EnvironmentUtil.isDev()).isTrue();
			application.close();
			assertThat(EnvironmentUtil.isDev()).isFalse();
		}
	}

	/**
	 *
	 * 作者: Jack
	 * 时间: 2026/11/18
	 * 说明: 防御替身或非标准Environment返回空数组和空元素的情况
	 * @return
	 * 返回值: void
	 * 返回值说明: 空元素不匹配；无激活项时仍能读取默认配置
	 */
	@Test
	void nullProfileArraysAndEntriesAreSafe() {
		Environment incomplete = mock(Environment.class);
		when(context.getEnvironment()).thenReturn(incomplete);
		when(incomplete.getDefaultProfiles()).thenReturn(new String[] { null, "TEST" });
		assertThat(EnvironmentUtil.isTest()).isTrue();
		when(incomplete.getActiveProfiles()).thenReturn(new String[] { null, "", "prod" });
		assertThat(EnvironmentUtil.isProd()).isTrue();
		assertThat(EnvironmentUtil.isTest()).isFalse();
		when(incomplete.getActiveProfiles()).thenReturn(null);
		when(incomplete.getDefaultProfiles()).thenReturn(null);
		assertThat(EnvironmentUtil.isDev()).isFalse();
	}
}

