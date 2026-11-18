package com.zero.jobadmin.business.scheduler.config;

import com.zero.jobadmin.business.mapper.*;
import com.zero.jobadmin.business.model.XxlJobGroup;
import com.zero.jobadmin.business.scheduler.alarm.JobAlarmer;
import com.zero.jobadmin.business.scheduler.complete.JobCompleter;
import com.zero.jobadmin.business.scheduler.thread.*;
import com.zero.jobadmin.business.scheduler.trigger.JobTrigger;
import com.xxl.job.core.constant.Const;
import com.xxl.job.core.openapi.executor.ExecutorBiz;
import com.xxl.tool.core.StringTool;
import com.xxl.tool.http.HttpTool;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Objects;

/**
 * xxl-job config
 *
 * @author xuxueli 2017-04-28
 */

@Component
public class XxlJobAdminBootstrap implements InitializingBean, DisposableBean {
    private static final Logger logger = LoggerFactory.getLogger(XxlJobAdminBootstrap.class);

    // ---------------------- instance ----------------------

    private static XxlJobAdminBootstrap adminConfig = null;
    public static XxlJobAdminBootstrap getInstance() {
        return adminConfig;
    }


    // ---------------------- start / stop ----------------------

    @Override
    public void afterPropertiesSet() throws Exception {
        // init instance
        adminConfig = this;

        // start
        doStart();
    }

    @Override
    public void destroy() throws Exception {
        // stop
        doStop();
    }

    // job module
    private JobTriggerPoolHelper jobTriggerPoolHelper;
    private JobRegistryHelper jobRegistryHelper;
    private JobFailAlarmMonitorHelper jobFailAlarmMonitorHelper;
    private JobCompleteHelper jobCompleteHelper;
    private JobLogReportHelper jobLogReportHelper;
    private JobScheduleHelper jobScheduleHelper;

    public JobTriggerPoolHelper getJobTriggerPoolHelper() {
        return jobTriggerPoolHelper;
    }
    public JobRegistryHelper getJobRegistryHelper() {
        return jobRegistryHelper;
    }
    public JobCompleteHelper getJobCompleteHelper() {
        return jobCompleteHelper;
    }

    /**
     * do start
     */
    private void doStart() throws Exception {
        // trigger-pool start
        jobTriggerPoolHelper = new JobTriggerPoolHelper();
        jobTriggerPoolHelper.start();

        // registry monitor start
        jobRegistryHelper = new JobRegistryHelper();
        jobRegistryHelper.start();

        // fail-alarm monitor start
        jobFailAlarmMonitorHelper = new JobFailAlarmMonitorHelper();
        jobFailAlarmMonitorHelper.start();

        // job complate start  ( depend on JobTriggerPoolHelper ) for callback and result-lost
        jobCompleteHelper = new JobCompleteHelper();
        jobCompleteHelper.start();

        // log-report start
        jobLogReportHelper = new JobLogReportHelper();
        jobLogReportHelper.start();

        // job-schedule start  ( depend on JobTriggerPoolHelper )
        jobScheduleHelper = new JobScheduleHelper();
        jobScheduleHelper.start();

        logger.info(">>>>>>>>> xxl-job admin start success.");
    }

    /**
     * do stop
     */
    private void doStop(){
        // job-schedule stop
        jobScheduleHelper.stop();

        // log-report stop
        jobLogReportHelper.stop();

        // job complate stop
        jobCompleteHelper.stop();

        // fail-alarm monitor stop
        jobFailAlarmMonitorHelper.stop();

        // registry monitor stop
        jobRegistryHelper.stop();

        // trigger-pool stop
        jobTriggerPoolHelper.stop();

        logger.info(">>>>>>>>> xxl-job admin stopped.");
    }


    // ---------------------- executor-client ----------------------

    /** 按最近使用顺序存放执行器代理，访问与逐条淘汰使用同一把锁。 */
    private static final LinkedHashMap<ExecutorEndpoint, ExecutorClient> executorBizRepository =
            new LinkedHashMap<>(16, 0.75f, true);
    /** 执行器代理缓存上限。 */
    private static final int EXECUTOR_BIZ_CACHE_MAX_SIZE = 1000;

    /**
     *
     * 作者: xuxueli
     * 时间: 2017/4/28
     * 说明: 按地址和应用复用执行器客户端，令牌或超时配置变化时替换旧客户端。
     * @param address 执行器 HTTP 地址
     * @param xxlJobGroup 当前执行器组配置
     * @return
     * 返回值: ExecutorBiz
     * 返回值说明: 绑定当前应用和通信凭据的执行器客户端，缺少地址或配置时返回空
     */
    public static ExecutorBiz getExecutorBiz(String address, XxlJobGroup xxlJobGroup) throws Exception {
        if (StringTool.isBlank(address) || xxlJobGroup == null) {
            return null;
        }
        ExecutorEndpoint endpoint = new ExecutorEndpoint(address.trim(), xxlJobGroup.getAppname());
        String accessToken = xxlJobGroup.getAccessToken();
        int timeoutMillis = XxlJobAdminBootstrap.getInstance().getTimeout() * 1000;
        synchronized (executorBizRepository) {
            ExecutorClient cached = executorBizRepository.get(endpoint);
            if (cached != null && cached.timeoutMillis() == timeoutMillis
                    && Objects.equals(cached.accessToken(), accessToken)) {
                return cached.client();
            }

            ExecutorBiz client = HttpTool.createClient()
                    .url(endpoint.address())
                    .connectTimeout(timeoutMillis)
                    .readTimeout(timeoutMillis)
                    .header(Const.XXL_JOB_ACCESS_TOKEN, accessToken)
                    .header(Const.XXL_JOB_APPNAME, endpoint.appName())
                    .proxy(ExecutorBiz.class);
            executorBizRepository.put(endpoint, new ExecutorClient(accessToken, timeoutMillis, client));
            if (executorBizRepository.size() > EXECUTOR_BIZ_CACHE_MAX_SIZE) {
                executorBizRepository.remove(executorBizRepository.keySet().iterator().next());
            }
            return client;
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 21:04
     * 说明: 同一执行器地址可以服务不同应用，客户端按应用隔离。
     */
    private record ExecutorEndpoint(String address, String appName) {
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 21:04
     * 说明: 记录创建代理时的通信配置，仅用于判断是否能复用。
     */
    private record ExecutorClient(String accessToken, int timeoutMillis, ExecutorBiz client) {
    }


    // ---------------------- field ----------------------

    // conf
    @Value("${xxl.job.i18n}")
    private String i18n;

    @Value("${xxl.job.timeout}")
    private int timeout;

    @Value("${spring.mail.from}")
    private String emailFrom;

    @Value("${xxl.job.triggerpool.fast.max}")
    private int triggerPoolFastMax;

    @Value("${xxl.job.triggerpool.slow.max}")
    private int triggerPoolSlowMax;

    @Value("${xxl.job.schedule.batchsize}")
    private int scheduleBatchSize;

    @Value("${xxl.job.logretentiondays}")
    private int logretentiondays;

    // service, mapper
    @Resource
    private XxlJobLogMapper xxlJobLogMapper;
    @Resource
    private XxlJobInfoMapper xxlJobInfoMapper;
    @Resource
    private XxlJobRegistryMapper xxlJobRegistryMapper;
    @Resource
    private XxlJobGroupMapper xxlJobGroupMapper;
    @Resource
    private XxlJobLogReportMapper xxlJobLogReportMapper;
    @Resource
    private XxlJobLockMapper xxlJobLockMapper;
    @Resource
    private JavaMailSender mailSender;
    /*@Resource
    private DataSource dataSource;*/
    @Resource
    private PlatformTransactionManager transactionManager;
    @Resource
    private JobAlarmer jobAlarmer;
    @Resource
    private JobTrigger jobTrigger;
    @Resource
    private JobCompleter jobCompleter;


    public String getI18n() {
        if (!Arrays.asList("zh_CN", "zh_TC", "en").contains(i18n)) {
            return "zh_CN";
        }
        return i18n;
    }

    public int getTimeout() {
        return timeout;
    }

    public String getEmailFrom() {
        return emailFrom;
    }

    public int getTriggerPoolFastMax() {
        if (triggerPoolFastMax < 200) {
            return 200;
        }
        return triggerPoolFastMax;
    }

    public int getTriggerPoolSlowMax() {
        if (triggerPoolSlowMax < 100) {
            return 100;
        }
        return triggerPoolSlowMax;
    }

    public int getScheduleBatchSize() {
        if (!(scheduleBatchSize >=50 && scheduleBatchSize <= 500)) {
            return 100;
        }
        return scheduleBatchSize;
    }

    public int getLogretentiondays() {
        if (logretentiondays < 3) {
            return -1;  // Limit greater than or equal to 3, otherwise close
        }
        return logretentiondays;
    }

    public XxlJobLogMapper getXxlJobLogMapper() {
        return xxlJobLogMapper;
    }

    public XxlJobInfoMapper getXxlJobInfoMapper() {
        return xxlJobInfoMapper;
    }

    public XxlJobRegistryMapper getXxlJobRegistryMapper() {
        return xxlJobRegistryMapper;
    }

    public XxlJobGroupMapper getXxlJobGroupMapper() {
        return xxlJobGroupMapper;
    }

    public XxlJobLogReportMapper getXxlJobLogReportMapper() {
        return xxlJobLogReportMapper;
    }

    public XxlJobLockMapper getXxlJobLockMapper() {
        return xxlJobLockMapper;
    }

    public JavaMailSender getMailSender() {
        return mailSender;
    }

    /*public DataSource getDataSource() {
        return dataSource;
    }*/

    public PlatformTransactionManager getTransactionManager() {
        return transactionManager;
    }

    public JobAlarmer getJobAlarmer() {
        return jobAlarmer;
    }

    public JobTrigger getJobTrigger() {
        return jobTrigger;
    }

    public JobCompleter getJobCompleter() {
        return jobCompleter;
    }

}
