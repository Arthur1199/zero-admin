package com.zero.config;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/19 12:22
 * 说明: 验证缺少凭据和主动关闭执行器时的启动行为。
 */
class AiExecutorConfigTest {

    /** 仅加载执行器配置，隔离 Nacos 和外部服务。 */
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(AiExecutorConfig.class);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 12:22
     * 说明: 确保未配置凭据时拒绝启动执行器。
     */
    @Test
    void shouldRejectMissingToken() {
        contextRunner.run(context -> assertThat(context).getFailure()
                .hasRootCauseMessage("必须配置 xxl.job.executor.access-token"));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 12:22
     * 说明: 验证关闭执行器时允许应用加载配置。
     */
    @Test
    void shouldSkipExecutorWhenDisabled() {
        contextRunner.withPropertyValues("xxl.job.executor.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(XxlJobSpringExecutor.class));
    }
}
