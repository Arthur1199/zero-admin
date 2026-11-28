package com.zero.config;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.io.ClassPathResource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/28 20:25
 * 说明: 验证应用、Nacos 发布资源和执行器使用独立于原服务的配置。
 */
class AiServiceIdentityTest {

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/28 20:25
     * 说明: 校验应用名、HTTP 端口及各环境导入的发布模板，避免启动原任务服务的配置。
     * @return
     * 返回值: void
     * 返回值说明: 仅从类路径读取配置，不访问 Nacos。
     */
    @Test
    void shouldLoadIndependentServiceConfiguration() throws IOException {
        assertThat(property("application.yml", "spring.application.name")).isEqualTo("zero-ai");
        assertThat(property("application.yml", "server.port")).isEqualTo(9105);
        for (String profile : List.of("dev", "test", "prod")) {
            String resource = "application-" + profile + ".yml";
            for (int index = 0; index < 3; index++) {
                String dataId = List.of("zero-ai-common.yml", "zero-ai-mysql.yml", "zero-ai-redis.yml").get(index);
                assertThat(property(resource, "spring.config.import[" + index + "]"))
                        .isEqualTo("optional:nacos:" + dataId + "?group=${spring.profiles.active}&refreshEnabled=true");
                assertThat(new ClassPathResource("nacosConfig/" + dataId).exists()).isTrue();
            }
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/28 20:25
     * 说明: 校验未覆盖时的执行器身份，及 Nacos 发布模板是否与默认值一致。
     * @return
     * 返回值: void
     * 返回值说明: 不启动 HTTP 监听，也不向真实调度中心注册。
     */
    @Test
    void shouldBindIndependentExecutorDefaults() throws IOException {
        var defaults = new Binder(new MapConfigurationPropertySource())
                .bindOrCreate("xxl.job.executor", Bindable.of(AiExecutorProperties.class));
        assertThat(defaults.appName()).isEqualTo("zero-ai");
        assertThat(defaults.port()).isEqualTo(9999);
        assertThat(defaults.logPath()).isEqualTo("logs/zero-ai/jobhandler");

        String template = "nacosConfig/zero-ai-task.yml";
        assertThat(property(template, "xxl.job.executor.app-name")).isEqualTo("zero-ai");
        assertThat(property(template, "xxl.job.executor.port")).isEqualTo(9999);
        assertThat(property(template, "xxl.job.executor.log-path")).isEqualTo("logs/zero-ai/jobhandler");
        assertThat(property(template, "xxl.job.executor.access-token")).isEqualTo("${ZERO_AI_XXL_JOB_ACCESS_TOKEN:}");
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/28 20:25
     * 说明: 读取多段 YAML 中的配置声明，不解析环境变量或加载外部配置。
     * @param resource 类路径下的 YAML 资源
     * @param key 展平后的属性名称
     * @return
     * 返回值: Object
     * 返回值说明: 首个已声明的属性值，未声明时返回 null。
     */
    private static Object property(String resource, String key) throws IOException {
        return new YamlPropertySourceLoader().load(resource, new ClassPathResource(resource)).stream()
                .filter(source -> source.containsProperty(key))
                .flatMap(source -> Stream.ofNullable(source.getProperty(key)))
                .findFirst().orElse(null);
    }
}
