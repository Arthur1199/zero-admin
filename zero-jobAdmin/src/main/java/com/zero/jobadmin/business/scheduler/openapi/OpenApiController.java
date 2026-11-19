package com.zero.jobadmin.business.scheduler.openapi;

import lombok.extern.slf4j.Slf4j;
import com.google.gson.JsonParseException;
import com.zero.jobadmin.business.mapper.XxlJobGroupMapper;
import com.zero.jobadmin.business.model.XxlJobGroup;
import com.zero.jobadmin.business.scheduler.openapi.impl.AdminBizImpl;
import com.zero.jobadmin.business.scheduler.openapi.impl.AdminJobBizImpl;
import com.xxl.job.core.constant.Const;
import com.xxl.job.core.openapi.admin.dto.CallbackRequest;
import com.xxl.job.core.openapi.admin.dto.JobAddRequest;
import com.xxl.job.core.openapi.admin.dto.JobOperateRequest;
import com.xxl.job.core.openapi.admin.dto.JobTriggerRequest;
import com.xxl.job.core.openapi.admin.dto.JobUpdateRequest;
import com.xxl.job.core.openapi.admin.dto.RegistryRequest;
import com.xxl.sso.core.annotation.XxlSso;
import com.xxl.tool.core.StringTool;
import com.xxl.tool.json.GsonTool;
import com.xxl.tool.response.Response;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

/**
 *
 * 作者: xuxueli
 * 时间: 2017/5/10
 * 说明: 按当前执行器组配置认证 OpenAPI，向业务操作传递已认证的组。
 */
@Slf4j
@Controller
public class OpenApiController {
    @Resource
    private XxlJobGroupMapper xxlJobGroupMapper;

    @Resource
    private AdminBizImpl adminBiz;

    @Resource
    private AdminJobBizImpl adminJobBiz;

    /**
     *
     * 作者: xuxueli
     * 时间: 2017/5/10
     * 说明: 使用数据库中的当前执行器组配置认证请求，凭据更新后下一次请求使用新配置。
     * @param request 当前请求
     * @param uri OpenAPI 操作名称
     * @param accessToken 执行器通信凭据
     * @param appname 执行器应用标识
     * @param requestBody 执行器发送的 JSON 内容
     * @return
     * 返回值: Object
     * 返回值说明: 对应操作的响应对象
     */
    @RequestMapping("/api/{uri}")
    @ResponseBody
    @XxlSso(login = false)
    public Object api(HttpServletRequest request,
                      @PathVariable("uri") String uri,
                      @RequestHeader(value = Const.XXL_JOB_ACCESS_TOKEN, required = false) String accessToken,
                      @RequestHeader(value = Const.XXL_JOB_APPNAME, required = false) String appname,
                      @RequestBody(required = false) String requestBody) {

        // valid
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return Response.ofFail("invalid request, HttpMethod not support.");
        }
        if (StringTool.isBlank(uri)) {
            return Response.ofFail("invalid request, uri-mapping empty.");
        }
        if (StringTool.isBlank(requestBody)) {
            return Response.ofFail("invalid request, requestBody empty.");
        }

        // valid: appname + accessToken
        if (StringTool.isBlank(accessToken) || StringTool.isBlank(appname)) {
            return Response.ofFail("invalid request, accessToken or appname is empty.");
        }
        XxlJobGroup xxlJobGroup;
        try {
            xxlJobGroup = xxlJobGroupMapper.loadByAppname(appname);
        } catch (RuntimeException e) {
            logOpenApiFailure("authenticate", null, e);
            return Response.ofFail("Executor authentication is temporarily unavailable.");
        }
        // 数据库排序规则可能忽略大小写，应用名校验保留原来的精确匹配。
        if (xxlJobGroup == null || xxlJobGroup.getId() <= 0 || !appname.equals(xxlJobGroup.getAppname())
                || !accessToken.equals(xxlJobGroup.getAccessToken())) {
            return Response.ofFail("invalid request, accessToken or appname invalid.");
        }

        // dispatch request
        try {
            switch (uri) {
                case "callback": {
                    CallbackRequest callbackParam = GsonTool.fromJson(requestBody, CallbackRequest.class);
                    return adminBiz.callback(xxlJobGroup.getId(), callbackParam);
                }
                case "registry": {
                    RegistryRequest registryParam = GsonTool.fromJson(requestBody, RegistryRequest.class);
                    return adminBiz.registry(xxlJobGroup.getAppname(), registryParam);
                }
                case "registryRemove": {
                    RegistryRequest registryParam = GsonTool.fromJson(requestBody, RegistryRequest.class);
                    return adminBiz.registryRemove(xxlJobGroup.getAppname(), registryParam);
                    }
                case "addJob": {
                    JobAddRequest jobParam = GsonTool.fromJson(requestBody, JobAddRequest.class);
                    return adminJobBiz.addJob(xxlJobGroup.getId(), jobParam);
                }
                case "updateJob": {
                    JobUpdateRequest jobParam = GsonTool.fromJson(requestBody, JobUpdateRequest.class);
                    return adminJobBiz.updateJob(xxlJobGroup.getId(), jobParam);
                }
                case "removeJob": {
                    JobOperateRequest jobParam = GsonTool.fromJson(requestBody, JobOperateRequest.class);
                    return adminJobBiz.removeJob(xxlJobGroup.getId(), jobParam);
                }
                case "startJob": {
                    JobOperateRequest jobParam = GsonTool.fromJson(requestBody, JobOperateRequest.class);
                    return adminJobBiz.startJob(xxlJobGroup.getId(), jobParam);
                }
                case "stopJob": {
                    JobOperateRequest jobParam = GsonTool.fromJson(requestBody, JobOperateRequest.class);
                    return adminJobBiz.stopJob(xxlJobGroup.getId(), jobParam);
                }
                case "triggerJob": {
                    JobTriggerRequest jobParam = GsonTool.fromJson(requestBody, JobTriggerRequest.class);
                    return adminJobBiz.triggerJob(xxlJobGroup.getId(), jobParam);
                }
                default:
                    return Response.ofFail("invalid request, uri-mapping("+ uri +") not found.");
            }
        } catch (JsonParseException e) {
            return Response.ofFail("Invalid request body.");
        } catch (RuntimeException e) {
            logOpenApiFailure(uri, xxlJobGroup.getId(), e);
            return Response.ofFail("OpenAPI request could not be processed.");
        }

    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 21:39
     * 说明: 记录 OpenAPI 内部故障位置，避免异常消息中的请求字段或数据库信息进入日志。
     * @param action 已匹配的 OpenAPI 操作或认证阶段
     * @param jobGroupId 已认证的执行器组 ID，认证阶段为空
     * @param error 捕获的服务异常
     * @return
     * 返回值: void
     * 返回值说明: 写入操作、组 ID、异常类型及堆栈位置
     */
    private void logOpenApiFailure(String action, Integer jobGroupId, RuntimeException error) {
        log.error("OpenAPI request failed, action={}, jobGroupId={}, errorType={}, stack={}",
                action, jobGroupId, error.getClass().getName(), Arrays.toString(error.getStackTrace()));
    }

}
