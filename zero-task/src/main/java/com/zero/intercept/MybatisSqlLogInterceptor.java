package com.zero.intercept;

import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceProperties;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.zero.utils.sql.SQLFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.Configuration;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/23 21:42
 * 说明: 按运行环境和日志开关记录当前服务实际执行的 SQL 模板及 Mapper 方法标识。
 */
//@Profile("dev")
@Slf4j
@Component
@RequiredArgsConstructor
@Intercepts({
        @Signature(type = StatementHandler.class, method = "query", args = {Statement.class, ResultHandler.class}),
        @Signature(type = StatementHandler.class, method = "queryCursor", args = {Statement.class}),
        @Signature(type = StatementHandler.class, method = "update", args = {Statement.class})
})
public class MybatisSqlLogInterceptor
        implements Interceptor
{
    /** 默认支持 SQL 日志的本地、测试和生产 Spring Profile。 */
    private static final String DEV = "dev";
    /** 测试环境的 Profile 名称，用于 SQL 日志开关判断。 */
    private static final String TEST = "test";
    /** 生产环境的 Profile 名称，用于 SQL 日志开关判断。 */
    private static final String PROD = "prod";

    /** 用于判断当前服务启用的 Spring Profile。 */
    private final Environment environment;

    /** 无显式路由时使用动态数据源配置中的默认库名称。 */
    private final ObjectProvider<DynamicDataSourceProperties> dataSourceProperties;

    /** SQL 日志总开关。 */
    private static final boolean IS_NEED_PRINT_SQL = true;

    /** 是否为日志中的 SQL 模板添加换行和缩进。 */
    private static final boolean IS_FORMAT_SQL = true;

    /** 是否按 JDBC 占位符顺序记录实际绑定值。 */
    private static final boolean IS_PRINT_PARAM = true;

    /** 是否记录已返回的查询结果或更新影响行数。 */
    private static final boolean IS_LOG_RESULT = true;

    /** 单次语句超过此耗时后记录慢 SQL 告警，单位为毫秒。 */
    private static final long MAX_RUN_TIME = 5000L;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/23 21:23
     * 说明: 记录当前路由、分页改写后的 SQL 和 Mapper 方法标识，保留原执行结果与异常。
     * @param invocation 查询、游标查询或更新的语句执行调用
     * @return 原调用结果
     * 返回值: Object
     * 返回值说明: 由原 StatementHandler 返回，日志不修改原值
     */
    @Override
    public Object intercept(Invocation invocation) throws Throwable
    {
        if (!IS_NEED_PRINT_SQL || !log.isInfoEnabled() || !environment.matchesProfiles(DEV, TEST, PROD))
        {
            return invocation.proceed();
        }

        String mapperId;
        String sql;
        String dataSource;
        List<Object> parameters = List.of();
        try
        {
            var handler = PluginUtils.mpStatementHandler((StatementHandler) invocation.getTarget());
            var mappedStatement = handler.mappedStatement();
            mapperId = mappedStatement.getId();
            var boundSql = handler.boundSql();
            sql = boundSql.getSql();
            if (IS_PRINT_PARAM)
            {
                parameters = resolveParameters(boundSql, mappedStatement.getConfiguration());
            }
            dataSource = DynamicDataSourceContextHolder.peek();
            if (dataSource == null || dataSource.isEmpty())
            {
                var properties = dataSourceProperties.getIfAvailable();
                dataSource = properties == null ? "未知" : properties.getPrimary();
            }
        }
        catch (RuntimeException ex)
        {
            // 上下文关闭或诊断信息读取失败时，日志不应阻断正在执行的 SQL。
            log.debug("读取 MyBatis SQL 日志信息失败，异常类型为 {}", ex.getClass().getSimpleName());
            return invocation.proceed();
        }
        log.info("开始执行 SQL数据源 [{}]，SQLID [{}]，SQL=\n[{}\n]", dataSource, mapperId,
                IS_FORMAT_SQL ? formatSql(sql) : sql);
        if (IS_PRINT_PARAM)
        {
            log.info("执行 SQL数据源 [{}]，SQLID [{}]，执行参数={}", dataSource, mapperId, parameters);
        }
        long begin = System.nanoTime();
        Object result = null;
        try
        {
            result = invocation.proceed();
            return result;
        }
        finally
        {
            long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - begin);
            log.info("执行 SQL数据源 [{}]，SQLID [{}]，耗时={}ms", dataSource, mapperId, elapsedMillis);
            if (elapsedMillis > MAX_RUN_TIME)
            {
                log.warn("执行 SQL数据源 [{}]，SQLID [{}] 耗时过长，耗时={}ms", dataSource, mapperId, elapsedMillis);
            }
            if (IS_LOG_RESULT)
            {
                // 游标按需读取，日志只输出已经返回的列表，避免提前消费游标。
                if (result instanceof List<?> rows)
                {
                    log.info("执行 SQL数据源 [{}]，SQLID [{}]，查询结果={}", dataSource, mapperId, rows);
                }
                else if (result instanceof Integer affectedRows)
                {
                    log.info("执行 SQL数据源 [{}]，SQLID [{}]，影响行数={}", dataSource, mapperId, affectedRows);
                }
            }
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 23:34
     * 说明: 按 MyBatis 绑定规则读取动态 SQL、分页和普通输入参数。
     * @param boundSql 最终 SQL 及参数映射
     * @param configuration 当前 Mapper 所属 MyBatis 配置
     * @return 按绑定顺序排列的输入参数值
     * 返回值: List<Object>
     * 返回值说明: 保留 null，跳过仅用于输出的参数
     */
    private List<Object> resolveParameters(BoundSql boundSql, Configuration configuration)
    {
        Object parameterObject = boundSql.getParameterObject();
        var metaObject = parameterObject == null ? null : configuration.newMetaObject(parameterObject);
        var values = new ArrayList<Object>();
        for (var mapping : boundSql.getParameterMappings())
        {
            if (mapping.getMode() == ParameterMode.OUT)
            {
                continue;
            }
            String name = mapping.getProperty();
            if (boundSql.hasAdditionalParameter(name))
            {
                values.add(boundSql.getAdditionalParameter(name));
            }
            else if (parameterObject == null || configuration.getTypeHandlerRegistry().hasTypeHandler(parameterObject.getClass()))
            {
                values.add(parameterObject);
            }
            else
            {
                values.add(metaObject.getValue(name));
            }
        }
        return values;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 23:22
     * 说明: 格式化日志中的 SQL 文本，解析失败时使用 MyBatis 提供的原始文本。
     * @param sql 分页改写后的 SQL 模板，保留参数占位符
     * @return SQL 日志文本
     * 返回值: String
     * 返回值说明: 仅用于日志，不写回 JDBC 执行对象
     */
    private String formatSql(String sql)
    {
        if (sql == null || sql.isBlank())
        {
            return sql;
        }
        try
        {
            return SQLFormatter.format(sql).stripTrailing();
        }
        catch (RuntimeException ex)
        {
            log.debug("格式化 MyBatis SQL 失败，异常类型为 {}", ex.getClass().getSimpleName());
            return sql;
        }
    }
}
