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
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 验证管理后台部署环境识别的空值、匹配方式和容器生命周期边界，使用静态方法替身隔离真实应用。
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
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 在每个测试前替换静态上下文读取，注入可单独设置 Profile 的 Spring 环境。
	 * @return
	 * 返回值: void
	 * 返回值说明: 建立当前测试专用的环境和上下文替身
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
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 在每个测试结束后关闭静态替身。
	 * @return
	 * 返回值: void
	 * 返回值说明: 解除当前线程对上下文读取方法的拦截
	 */
	@AfterEach
	void cleanup() {
		contextAccess.close();
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 验证静态上下文尚未注入时的环境识别结果。
	 * @return
	 * 返回值: void
	 * 返回值说明: 断言开发、测试和生产判断均为 false
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
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 模拟已有上下文却无法取得 Spring 环境的情况。
	 * @return
	 * 返回值: void
	 * 返回值说明: 断言各环境判断安全返回 false
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
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 验证功能 Profile 与部署 Profile 可并存，多个部署 Profile 可同时命中。
	 * @return
	 * 返回值: void
	 * 返回值说明: 断言环境检查按各自的目标名称独立判断
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
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 验证现代环境判断对大小写不敏感，并按完整 Profile 名称匹配。
	 * @return
	 * 返回值: void
	 * 返回值说明: 断言大小写混合的 prod 可匹配，较长的名称无法匹配
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
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 验证默认 Profile 仅在显式激活列表为空时参与判断。
	 * @return
	 * 返回值: void
	 * 返回值说明: 断言设置激活项后立即以激活项为准
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
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 覆盖 Spring 使用内置 default Profile 的场景。
	 * @return
	 * 返回值: void
	 * 返回值说明: 断言内置 default 不满足任何部署环境检查
	 */
	@Test
	void springDefaultIsNotImplicitlyDevelopment() {
		assertThat(EnvironmentUtil.isDev()).isFalse();
		assertThat(EnvironmentUtil.isTest()).isFalse();
		assertThat(EnvironmentUtil.isProd()).isFalse();
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 在同一环境对象中切换激活的 Profile，验证读取时机。
	 * @return
	 * 返回值: void
	 * 返回值说明: 断言旧 Profile 随切换立即失效，新 Profile 正常匹配
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
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 对同一可配置上下文依次检查未刷新、刷新和关闭状态。
	 * @return
	 * 返回值: void
	 * 返回值说明: 断言仅在容器活动期间读到匹配的 Profile
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
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 模拟环境返回空数组引用、空元素及空字符串，验证默认项回退和有效项匹配。
	 * @return
	 * 返回值: void
	 * 返回值说明: 断言异常元素被跳过，缺少有效信息时返回 false
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

