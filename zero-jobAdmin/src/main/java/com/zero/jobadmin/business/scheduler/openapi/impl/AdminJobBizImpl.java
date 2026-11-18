package com.zero.jobadmin.business.scheduler.openapi.impl;

import com.zero.jobadmin.business.model.XxlJobInfo;
import com.zero.jobadmin.business.service.XxlJobService;
import com.xxl.job.core.openapi.admin.dto.JobAddRequest;
import com.xxl.job.core.openapi.admin.dto.JobOperateRequest;
import com.xxl.job.core.openapi.admin.dto.JobTriggerRequest;
import com.xxl.job.core.openapi.admin.dto.JobUpdateRequest;
import com.xxl.sso.core.model.LoginInfo;
import com.xxl.tool.concurrent.TokenBucket;
import com.xxl.tool.response.Response;
import com.xxl.tool.response.ResponseCode;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/18 20:33
 * 说明: 接收执行器任务操作，复用任务服务的执行器组权限校验。
 */
@Service
public class AdminJobBizImpl {

    @Resource
    private XxlJobService xxlJobService;

    /**
     * token bucket, for rate limiting
     */
    private final TokenBucket tokenBucket = TokenBucket.create(100, Duration.ofSeconds(30));

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 20:33
     * 说明: 在 OpenAPI 认证范围内新增任务。
     * @param jobGroupId 认证通过的执行器组 ID
     * @param request OpenAPI 请求参数
     * @return
     * 返回值: Response<String>
     * 返回值说明: 新增结果，超限时返回限流提示
     */
    public Response<String> addJob(int jobGroupId, JobAddRequest request) {
        if (request == null) {
            return Response.ofFail("Illegal Argument.");
        }

        // rate limit
        if (!tokenBucket.tryAcquire(1, Duration.ofMillis(1000L))) {
            return Response.of(ResponseCode.CODE_502.getCode(), "Too many requests, please try again later.");
        }

        // convert JobAddRequest to XxlJobInfo
        XxlJobInfo jobInfo = new XxlJobInfo();
        jobInfo.setJobGroup(request.getJobGroup());
        jobInfo.setName(request.getName());
        jobInfo.setAuthor(request.getAuthor());
        jobInfo.setAlarmEmail(request.getAlarmEmail());
        jobInfo.setScheduleType(request.getScheduleType());
        jobInfo.setScheduleConf(request.getScheduleConf());
        jobInfo.setMisfireStrategy(request.getMisfireStrategy());
        jobInfo.setExecutorRouteStrategy(request.getExecutorRouteStrategy());
        jobInfo.setExecutorHandler(request.getExecutorHandler());
        jobInfo.setExecutorParam(request.getExecutorParam());
        jobInfo.setExecutorBlockStrategy(request.getExecutorBlockStrategy());
        jobInfo.setExecutorTimeout(request.getExecutorTimeout());
        jobInfo.setExecutorFailRetryCount(request.getExecutorFailRetryCount());
        jobInfo.setGlueType(request.getGlueType());
        jobInfo.setGlueSource(request.getGlueSource());
        jobInfo.setGlueRemark(request.getGlueRemark());
        jobInfo.setChildJobId(null);

        // add job
        return xxlJobService.add(jobInfo, groupLogin(jobGroupId));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 20:33
     * 说明: 在 OpenAPI 认证范围内更新组内任务配置。
     * @param jobGroupId 认证通过的执行器组 ID
     * @param request OpenAPI 请求参数
     * @return
     * 返回值: Response<String>
     * 返回值说明: 更新结果，超限时返回限流提示
     */
    public Response<String> updateJob(int jobGroupId, JobUpdateRequest request) {
        if (request == null) {
            return Response.ofFail("Illegal Argument.");
        }

        // rate limit
        if (!tokenBucket.tryAcquire(1, Duration.ofMillis(1000L))) {
            return Response.of(ResponseCode.CODE_502.getCode(), "Too many requests, please try again later.");
        }

        // convert JobUpdateRequest to XxlJobInfo
        XxlJobInfo jobInfo = new XxlJobInfo();
        jobInfo.setId(request.getId());
        jobInfo.setJobGroup(0);
        jobInfo.setName(request.getName());
        jobInfo.setAuthor(request.getAuthor());
        jobInfo.setAlarmEmail(request.getAlarmEmail());
        jobInfo.setScheduleType(request.getScheduleType());
        jobInfo.setScheduleConf(request.getScheduleConf());
        jobInfo.setMisfireStrategy(request.getMisfireStrategy());
        jobInfo.setExecutorRouteStrategy(request.getExecutorRouteStrategy());
        jobInfo.setExecutorHandler(request.getExecutorHandler());
        jobInfo.setExecutorParam(request.getExecutorParam());
        jobInfo.setExecutorBlockStrategy(request.getExecutorBlockStrategy());
        jobInfo.setExecutorTimeout(request.getExecutorTimeout());
        jobInfo.setExecutorFailRetryCount(request.getExecutorFailRetryCount());
        jobInfo.setGlueType(request.getGlueType());
        jobInfo.setGlueSource(request.getGlueSource());
        jobInfo.setGlueRemark(request.getGlueRemark());
        jobInfo.setChildJobId(null);

        // update job
        return xxlJobService.update(jobInfo, groupLogin(jobGroupId));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 20:33
     * 说明: 在 OpenAPI 认证范围内删除组内任务。
     * @param jobGroupId 认证通过的执行器组 ID
     * @param request OpenAPI 请求参数
     * @return
     * 返回值: Response<String>
     * 返回值说明: 删除结果，超限时返回限流提示
     */
    public Response<String> removeJob(int jobGroupId, JobOperateRequest request) {
        if (request == null) {
            return Response.ofFail("Illegal Argument.");
        }

        // rate limit
        if (!tokenBucket.tryAcquire(1, Duration.ofMillis(1000L))) {
            return Response.of(ResponseCode.CODE_502.getCode(), "Too many requests, please try again later.");
        }

        return xxlJobService.remove(request.getId(), groupLogin(jobGroupId));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 20:33
     * 说明: 在 OpenAPI 认证范围内启动组内任务。
     * @param jobGroupId 认证通过的执行器组 ID
     * @param request OpenAPI 请求参数
     * @return
     * 返回值: Response<String>
     * 返回值说明: 启动结果，超限时返回限流提示
     */
    public Response<String> startJob(int jobGroupId, JobOperateRequest request) {
        if (request == null) {
            return Response.ofFail("Illegal Argument.");
        }

        // rate limit
        if (!tokenBucket.tryAcquire(1, Duration.ofMillis(1000L))) {
            return Response.of(ResponseCode.CODE_502.getCode(), "Too many requests, please try again later.");
        }

        return xxlJobService.start(request.getId(), groupLogin(jobGroupId));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 20:33
     * 说明: 在 OpenAPI 认证范围内停止组内任务。
     * @param jobGroupId 认证通过的执行器组 ID
     * @param request OpenAPI 请求参数
     * @return
     * 返回值: Response<String>
     * 返回值说明: 停止结果，超限时返回限流提示
     */
    public Response<String> stopJob(int jobGroupId, JobOperateRequest request) {
        if (request == null) {
            return Response.ofFail("Illegal Argument.");
        }

        // rate limit
        if (!tokenBucket.tryAcquire(1, Duration.ofMillis(1000L))) {
            return Response.of(ResponseCode.CODE_502.getCode(), "Too many requests, please try again later.");
        }

        return xxlJobService.stop(request.getId(), groupLogin(jobGroupId));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 20:33
     * 说明: 在 OpenAPI 认证范围内触发组内任务。
     * @param jobGroupId 认证通过的执行器组 ID
     * @param request OpenAPI 请求参数
     * @return
     * 返回值: Response<String>
     * 返回值说明: 触发请求接收结果，超限时返回限流提示
     */
    public Response<String> triggerJob(int jobGroupId, JobTriggerRequest request) {
        if (request == null) {
            return Response.ofFail("Illegal Argument.");
        }

        // rate limit
        if (!tokenBucket.tryAcquire(1, Duration.ofMillis(1000L))) {
            return Response.of(ResponseCode.CODE_502.getCode(), "Too many requests, please try again later.");
        }

        return xxlJobService.trigger(
                groupLogin(jobGroupId),
                request.getId(),
                request.getExecutorParam(),
                request.getAddressList());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 20:33
     * 说明: 将认证结果转换为任务服务使用的组权限上下文。
     * @param jobGroupId 认证通过的执行器组 ID
     * @return
     * 返回值: LoginInfo
     * 返回值说明: 仅拥有当前执行器组操作权限的 OpenAPI 身份
     */
    private LoginInfo groupLogin(int jobGroupId) {
        LoginInfo login = new LoginInfo("0", "openapi");
        login.setUserName("OpenAPI");
        login.setExtraInfo(Map.of("jobGroups", String.valueOf(jobGroupId)));
        return login;
    }
}
