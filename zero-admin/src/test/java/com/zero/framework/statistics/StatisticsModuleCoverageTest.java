package com.zero.framework.statistics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.zero.common.annotation.SystemModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.web.bind.annotation.RestController;
import static org.assertj.core.api.Assertions.*;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/22 18:45
 * 说明: 控制器模块声明覆盖回归，阻止新增控制器漏标注注解
 */
class StatisticsModuleCoverageTest
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/22 18:45
     * 说明: 后台所有REST控制器必须在类或方法上声明统计模块
     * @return
     * 返回值: void
     * 返回值说明: 存在漏标注控制器时用例失败并列出类名
     */
    @Test void allAdminControllersDeclareModule()
    {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));
        List<Class<?>> controllers = new ArrayList<>();
        for (BeanDefinition definition : scanner.findCandidateComponents("com.zero.web"))
        {
            controllers.add(load(definition.getBeanClassName()));
        }
        // 先确认扫描确实命中了后台控制器，避免包名写错时用例空跑通过。
        assertThat(controllers).extracting(Class::getSimpleName)
                .contains("SysConfigController", "FileManagementController", "SysLoginController");
        List<String> missing = new ArrayList<>();
        for (Class<?> type : controllers)
        {
            // 类级声明覆盖整类接口，方法级声明用于同一控制器内的例外接口，两者都算已声明。
            boolean declared = AnnotatedElementUtils.hasAnnotation(type, SystemModule.class)
                    || Arrays.stream(type.getDeclaredMethods()).anyMatch(method -> AnnotatedElementUtils.hasAnnotation(method, SystemModule.class));
            if (!declared)
            {
                missing.add(type.getSimpleName());
            }
        }
        assertThat(missing).as("未声明@SystemModule的后台控制器").isEmpty();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/22 18:45
     * 说明: 按扫描到的全限定名加载控制器类型
     * @param className 扫描得到的类名
     * @return
     * 返回值: Class<?>
     * 返回值说明: 对应的控制器类型
     */
    private static Class<?> load(String className)
    {
        try
        {
            return Class.forName(className);
        }
        catch (ClassNotFoundException e)
        {
            throw new IllegalStateException("无法加载扫描到的控制器: " + className, e);
        }
    }
}
