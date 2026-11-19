package com.zero.jobadmin.openapi;

import lombok.extern.slf4j.Slf4j;
import com.xxl.job.core.constant.Const;
import com.xxl.job.core.openapi.admin.AdminJobBiz;
import com.xxl.job.core.openapi.admin.dto.JobAddRequest;
import com.xxl.job.core.openapi.admin.dto.JobOperateRequest;
import com.xxl.job.core.openapi.admin.dto.JobTriggerRequest;
import com.xxl.job.core.openapi.admin.dto.JobUpdateRequest;
import com.xxl.tool.http.HttpTool;
import com.xxl.tool.response.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * admin job api test
 *
 * @author xuxueli
 */
// 官方集成用例可能操作数据库或执行器，普通打包按标签排除。
@Slf4j
@org.junit.jupiter.api.Tag("job-integration")
public class AdminJobBizTest {
    private static String addressUrl = "http://127.0.0.1:8080";
    private static String accessToken = "default_token";
    private static String appname = "xxl-job-executor-sample";

    private AdminJobBiz buildClient() {
        String finalUrl = addressUrl + "/api";

        return HttpTool.createClient()
                .url(finalUrl)
                .connectTimeout(3 * 1000)
                .readTimeout(3 * 1000)
                .header(Const.XXL_JOB_ACCESS_TOKEN, accessToken)
                .header(Const.XXL_JOB_APPNAME, appname)
                .proxy(AdminJobBiz.class);
    }

    private int jobId = 10;

    @Test
    public void jobAdd() throws Exception {
        AdminJobBiz adminJobBiz = buildClient();

        JobAddRequest request = new JobAddRequest();
        request.setJobGroup(1);
        request.setName("openapi test job");
        request.setAuthor("openapi");
        request.setScheduleType("CRON");
        request.setScheduleConf("0/20 * * * * ?");
        request.setMisfireStrategy("DO_NOTHING");
        request.setExecutorRouteStrategy("FIRST");
        request.setExecutorHandler("demoJobHandler");
        request.setExecutorBlockStrategy("SERIAL_EXECUTION");
        request.setExecutorTimeout(0);
        request.setExecutorFailRetryCount(0);
        request.setGlueType("BEAN");

        Response<String> returnT = adminJobBiz.addJob(request);
        assertTrue(returnT.isSuccess());
        assertNotNull(returnT.getData());
        jobId = Integer.parseInt(returnT.getData());
        log.info("addJob jobId: {}", jobId);
    }

    @Test
    public void jobUpdate() throws Exception {
        AdminJobBiz adminJobBiz = buildClient();

        // update the job created by jobAdd (id should exist in test env)
        JobUpdateRequest request = new JobUpdateRequest();
        request.setId(jobId);
        request.setName("openapi test job update");
        request.setAuthor("openapi");
        request.setScheduleType("CRON");
        request.setScheduleConf("0/10 * * * * ?");
        request.setMisfireStrategy("DO_NOTHING");
        request.setExecutorRouteStrategy("FIRST");
        request.setExecutorHandler("demoJobHandler");
        request.setExecutorBlockStrategy("SERIAL_EXECUTION");
        request.setExecutorTimeout(0);
        request.setExecutorFailRetryCount(0);
        request.setGlueType("BEAN");

        Response<String> returnT = adminJobBiz.updateJob(request);
        log.info("updateJob response: {}", returnT);
        assertTrue(returnT.isSuccess());
    }

    @Test
    public void jobStart() throws Exception {
        AdminJobBiz adminJobBiz = buildClient();

        JobOperateRequest request = new JobOperateRequest();
        request.setId(jobId);

        Response<String> returnT = adminJobBiz.startJob(request);
        log.info("startJob response: {}", returnT);
        assertTrue(returnT.isSuccess());
    }

    @Test
    public void jobStop() throws Exception {
        AdminJobBiz adminJobBiz = buildClient();

        JobOperateRequest request = new JobOperateRequest();
        request.setId(jobId);

        Response<String> returnT = adminJobBiz.stopJob(request);
        log.info("stopJob response: {}", returnT);
        assertTrue(returnT.isSuccess());
    }

    @Test
    public void jobTrigger() throws Exception {
        AdminJobBiz adminJobBiz = buildClient();

        JobTriggerRequest request = new JobTriggerRequest();
        request.setId(jobId);

        Response<String> returnT = adminJobBiz.triggerJob(request);
        log.info("triggerJob response: {}", returnT);
        assertTrue(returnT.isSuccess());
    }

    @Test
    public void jobRemove() throws Exception {
        AdminJobBiz adminJobBiz = buildClient();

        JobOperateRequest request = new JobOperateRequest();
        request.setId(jobId);

        Response<String> returnT = adminJobBiz.removeJob(request);
        log.info("removeJob response: {}", returnT);
        assertTrue(returnT.isSuccess());
    }

}
