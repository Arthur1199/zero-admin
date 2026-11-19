package com.zero.jobadmin.business.scheduler.openapi.impl;

import com.zero.jobadmin.business.scheduler.config.XxlJobAdminBootstrap;
import com.xxl.job.core.constant.RegistTypeEnum;
import com.xxl.job.core.openapi.admin.dto.CallbackRequest;
import com.xxl.job.core.openapi.admin.dto.RegistryRequest;
import com.xxl.tool.response.Response;
import org.springframework.stereotype.Service;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 执行器回调及心跳请求的服务端实现。
 */
@Service
public class AdminBizImpl {

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 20:32
     * 说明: 使用 OpenAPI 认证确定的执行器组确认完成回调。
     * @param jobGroupId 认证通过的执行器组 ID
     * @param callbackRequest 执行器提交的回调批次
     * @return
     * 返回值: Response<String>
     * 返回值说明: 成功表示回调已处理，写入失败时由执行器重试
     */
    public Response<String> callback(int jobGroupId, CallbackRequest callbackRequest) {
        if (callbackRequest == null) {
            return Response.ofFail("Illegal Argument.");
        }
        return XxlJobAdminBootstrap.getInstance().getJobCompleteHelper()
                .callback(jobGroupId, callbackRequest.getCallbackList());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 20:33
     * 说明: 注册请求的 AppName 必须与认证结果一致。
     * @param appName 已认证应用名
     * @param registryRequest 执行器注册请求
     * @return
     * 返回值: Response<String>
     * 返回值说明: 本应用执行器的心跳写入结果
     */
    public Response<String> registry(String appName, RegistryRequest registryRequest) {
        if (!isOwnRegistry(appName, registryRequest)) {
            return Response.ofFail("Invalid executor registry.");
        }
        return XxlJobAdminBootstrap.getInstance().getJobRegistryHelper().registry(registryRequest);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 20:33
     * 说明: 下线请求只对已认证应用的执行器生效。
     * @param appName 已认证应用名
     * @param registryRequest 执行器下线请求
     * @return
     * 返回值: Response<String>
     * 返回值说明: 本应用执行器的注册记录移除结果
     */
    public Response<String> registryRemove(String appName, RegistryRequest registryRequest) {
        if (!isOwnRegistry(appName, registryRequest)) {
            return Response.ofFail("Invalid executor registry.");
        }
        return XxlJobAdminBootstrap.getInstance().getJobRegistryHelper().registryRemove(registryRequest);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 20:33
     * 说明: 核对心跳类别与请求中的应用名。
     * @param appName 已认证应用名
     * @param request 执行器请求，可为空
     * @return
     * 返回值: boolean
     * 返回值说明: 仅本应用执行器类型返回 true
     */
    private boolean isOwnRegistry(String appName, RegistryRequest request) {
        return appName != null && request != null && appName.equals(request.getRegistryKey())
                && RegistTypeEnum.EXECUTOR.name().equals(request.getRegistryGroup());
    }
}
