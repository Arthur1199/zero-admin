package com.zero.framework.config.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import com.alibaba.druid.pool.DruidDataSource;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: druid 配置属性
 */
@Configuration
public class DruidProperties
{
    /**
     * initial大小字段
     */
    @Value("${spring.datasource.druid.initialSize}")
    private int initialSize;
    /**
     * minIdle字段
     */

    @Value("${spring.datasource.druid.minIdle}")
    private int minIdle;
    /**
     * maxActive字段
     */

    @Value("${spring.datasource.druid.maxActive}")
    private int maxActive;
    /**
     * maxWait字段
     */

    @Value("${spring.datasource.druid.maxWait}")
    private int maxWait;
    /**
     * connectTimeout字段
     */

    @Value("${spring.datasource.druid.connectTimeout}")
    private int connectTimeout;
    /**
     * socketTimeout字段
     */

    @Value("${spring.datasource.druid.socketTimeout}")
    private int socketTimeout;
    /**
     * 时间BetweenEvictionRunsMillis字段
     */

    @Value("${spring.datasource.druid.timeBetweenEvictionRunsMillis}")
    private int timeBetweenEvictionRunsMillis;
    /**
     * minEvictableIdle时间Millis字段
     */

    @Value("${spring.datasource.druid.minEvictableIdleTimeMillis}")
    private int minEvictableIdleTimeMillis;
    /**
     * maxEvictableIdle时间Millis字段
     */

    @Value("${spring.datasource.druid.maxEvictableIdleTimeMillis}")
    private int maxEvictableIdleTimeMillis;
    /**
     * validation查询字段
     */

    @Value("${spring.datasource.druid.validationQuery}")
    private String validationQuery;
    /**
     * testWhileIdle字段
     */

    @Value("${spring.datasource.druid.testWhileIdle}")
    private boolean testWhileIdle;
    /**
     * testOnBorrow字段
     */

    @Value("${spring.datasource.druid.testOnBorrow}")
    private boolean testOnBorrow;
    /**
     * testOnReturn字段
     */

    @Value("${spring.datasource.druid.testOnReturn}")
    private boolean testOnReturn;
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 数据Source
     * @param datasource datasource参数
     * @return
     * 返回值: DruidDataSource
     * 返回值说明：数据Source
     */

    public DruidDataSource dataSource(DruidDataSource datasource)
    {
        /** 配置初始化大小、最小、最大 */
        datasource.setInitialSize(initialSize);
        datasource.setMaxActive(maxActive);
        datasource.setMinIdle(minIdle);

        /** 配置获取连接等待超时的时间 */
        datasource.setMaxWait(maxWait);

        /** 配置驱动连接超时时间，检测数据库建立连接的超时时间，单位是毫秒 */
        datasource.setConnectTimeout(connectTimeout);

        /** 配置网络超时时间，等待数据库操作完成的网络超时时间，单位是毫秒 */
        datasource.setSocketTimeout(socketTimeout);

        /** 配置间隔多久才进行一次检测，检测需要关闭的空闲连接，单位是毫秒 */
        datasource.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);

        /** 配置一个连接在池中最小、最大生存的时间，单位是毫秒 */
        datasource.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
        datasource.setMaxEvictableIdleTimeMillis(maxEvictableIdleTimeMillis);

        /**
         * 用来检测连接是否有效的sql，要求是一个查询语句，常用select 'x'。如果validationQuery为null，testOnBorrow、testOnReturn、testWhileIdle都不会起作用。
         */
        datasource.setValidationQuery(validationQuery);
        /** 建议配置为true，不影响性能，并且保证安全性。申请连接的时候检测，如果空闲时间大于timeBetweenEvictionRunsMillis，执行validationQuery检测连接是否有效。 */
        datasource.setTestWhileIdle(testWhileIdle);
        /** 申请连接时执行validationQuery检测连接是否有效，做了这个配置会降低性能。 */
        datasource.setTestOnBorrow(testOnBorrow);
        /** 归还连接时执行validationQuery检测连接是否有效，做了这个配置会降低性能。 */
        datasource.setTestOnReturn(testOnReturn);
        return datasource;
    }
}
