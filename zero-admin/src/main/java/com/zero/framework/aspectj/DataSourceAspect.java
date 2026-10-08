package com.zero.framework.aspectj;

import java.util.Objects;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import com.zero.common.annotation.DataSource;
import com.zero.common.utils.StringUtils;
import com.zero.framework.datasource.DynamicDataSourceContextHolder;
import com.zero.packageconfig.PackageProperties;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 多数据源处理
 */
@Aspect
@Order(1)
@Component
public class DataSourceAspect
{
    /**
     * logger字段
     */
    protected Logger logger = LoggerFactory.getLogger(getClass());

    @Pointcut(PackageProperties.DATA_SOURCE_POINTCUT_SIGN)
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: dsPointCut
     * @return
     * 返回值: void
     * 返回值说明：dsPointCut
     */
    public void dsPointCut()
    {

    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: around
     * @param point point参数
     * @return
     * 返回值: Object
     * 返回值说明：around
     */

    @Around("dsPointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable
    {
        DataSource dataSource = getDataSource(point);

        if (StringUtils.isNotNull(dataSource))
        {
            DynamicDataSourceContextHolder.setDataSourceType(dataSource.value().name());
        }

        try
        {
            return point.proceed();
        }
        finally
        {
            // 销毁数据源 在执行方法之后
            DynamicDataSourceContextHolder.clearDataSourceType();
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取需要切换的数据源
     * @param point point参数
     * @return
     * 返回值: DataSource
     * 返回值说明：获取需要切换的数据源
     */
    public DataSource getDataSource(ProceedingJoinPoint point)
    {
        MethodSignature signature = (MethodSignature) point.getSignature();
        DataSource dataSource = AnnotationUtils.findAnnotation(signature.getMethod(), DataSource.class);
        if (Objects.nonNull(dataSource))
        {
            return dataSource;
        }

        return AnnotationUtils.findAnnotation(signature.getDeclaringType(), DataSource.class);
    }
}
