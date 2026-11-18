package com.zero.jobadmin.business.scheduler.route.strategy;

import com.zero.jobadmin.business.model.XxlJobGroup;
import com.zero.jobadmin.business.scheduler.config.XxlJobAdminBootstrap;
import com.zero.jobadmin.framework.util.I18nUtil;
import com.xxl.job.core.openapi.executor.ExecutorBiz;
import com.xxl.tool.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.HtmlUtils;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/18 22:54
 * 说明: 按组内顺序探测执行器，为故障转移和忙碌转移记录各节点的探测结果。
 */
final class ExecutorRouteProbe {

    private static final Logger logger = LoggerFactory.getLogger(ExecutorRouteProbe.class);

    private ExecutorRouteProbe() {
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 22:54
     * 说明: 空响应和单节点异常都视为当前节点不可用，继续探测下一个节点。
     * @param group 当前执行器组，提供节点顺序和通信凭据
     * @param label 探测类型的本地化名称
     * @param probe 调用执行器的心跳或空闲探测
     * @return
     * 返回值: Response<String>
     * 返回值说明: 返回第一个通过探测的节点及探测记录，全部失败时返回失败记录
     */
    static Response<String> select(XxlJobGroup group, String label, Function<ExecutorBiz, Response<String>> probe) {
        List<String> addresses = group == null ? null : group.getRegistryList();
        if (addresses == null || addresses.isEmpty()) {
            return Response.ofFail(I18nUtil.getString("jobconf_trigger_address_empty"));
        }
        String token = group.getAccessToken();
        StringBuilder diagnostics = new StringBuilder();
        for (String address : addresses) {
            Response<String> result = null;
            try {
                ExecutorBiz executor = XxlJobAdminBootstrap.getExecutorBiz(address, group);
                if (executor != null) {
                    result = probe.apply(executor);
                }
            } catch (Exception e) {
                // 客户端异常详情可能含通信凭据，诊断仅记录异常类型。
                logger.warn("Executor probe failed, groupId={}, probe={}, type={}",
                        group.getId(), label, e.getClass().getSimpleName());
            }
            if (result == null) {
                result = Response.ofFail(I18nUtil.getString("system_opt_fail"));
            }
            if (diagnostics.length() > 0) {
                diagnostics.append("<br><br>");
            }
            diagnostics.append(label).append("：")
                    .append("<br>address：").append(diagnosticText(address, token))
                    .append("<br>code：").append(result.getCode())
                    .append("<br>msg：").append(diagnosticText(result.getMsg(), token));
            if (result.isSuccess()) {
                Response<String> selected = Response.ofSuccess(address);
                selected.setMsg(diagnostics.toString());
                return selected;
            }
        }
        return Response.ofFail(diagnostics.toString());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 22:54
     * 说明: 将执行器返回的内容作为文本写入调度备注，并屏蔽回显的通信凭据。
     * @param value 执行器地址或响应消息，可以为空
     * @param token 当前执行器组的通信凭据
     * @return
     * 返回值: String
     * 返回值说明: 转义后的备注文本，空值返回空串
     */
    private static String diagnosticText(String value, String token) {
        String text = Objects.toString(value, "");
        if (token != null && !token.isBlank()) {
            text = text.replace(token, "***");
        }
        return HtmlUtils.htmlEscape(text, "UTF-8");
    }
}
