package com.zero.jobadmin.business.scheduler.trigger;

import lombok.extern.slf4j.Slf4j;
import com.zero.jobadmin.business.mapper.XxlJobGroupMapper;
import com.zero.jobadmin.business.mapper.XxlJobInfoMapper;
import com.zero.jobadmin.business.mapper.XxlJobLogMapper;
import com.zero.jobadmin.business.model.XxlJobGroup;
import com.zero.jobadmin.business.model.XxlJobInfo;
import com.zero.jobadmin.business.model.XxlJobLog;
import com.zero.jobadmin.business.scheduler.config.XxlJobAdminBootstrap;
import com.zero.jobadmin.business.scheduler.route.ExecutorRouteStrategyEnum;
import com.zero.jobadmin.business.util.ExecutorAddressUtil;
import com.zero.jobadmin.framework.util.I18nUtil;
import com.xxl.job.core.constant.ExecutorBlockStrategyEnum;
import com.xxl.job.core.context.XxlJobContext;
import com.xxl.job.core.glue.GlueTypeEnum;
import com.xxl.job.core.openapi.executor.ExecutorBiz;
import com.xxl.job.core.openapi.executor.dto.TriggerRequest;
import com.xxl.tool.core.StringTool;
import com.xxl.tool.crypto.Sha256Tool;
import com.xxl.tool.http.IPTool;
import com.xxl.tool.response.Response;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 将调度触发转换为执行器请求，处理路由、分片和触发结果记录。
 */
@Slf4j
@Component
public class JobTrigger {
    /** 调度备注的 UTF-8 字节上限，给后续追加的重试备注预留 TEXT 容量。 */
    private static final int MAX_TRIGGER_MESSAGE_BYTES = 64_000;
    /** 标记备注中因容量限制省略的内容。 */
    private static final String MESSAGE_TRUNCATED = "…";

    @Resource
    private XxlJobInfoMapper xxlJobInfoMapper;
    @Resource
    private XxlJobGroupMapper xxlJobGroupMapper;
    @Resource
    private XxlJobLogMapper xxlJobLogMapper;


    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 读取任务配置并触发执行，临时覆盖仅用于本次任务派发，配置异常写入调度日志。
     * @param jobId 任务 ID
     * @param triggerType 触发类型
     * @param failRetryCount 剩余重试次数，小于 0 时使用任务配置
     * @param executorShardingParam 指定重试分片，非空时格式为 索引/总数
     * @param executorParam 临时任务参数，null 时使用任务配置，空串表示清空参数
     * @param addressList 临时指定的地址列表，为空时使用执行器组地址
     * @return
     * 返回值: void
     * 返回值说明: 更新调度日志，广播时每个实际分片单独记录
     */
    public void trigger(int jobId,
                               TriggerTypeEnum triggerType,
                               int failRetryCount,
                               String executorShardingParam,
                               String executorParam,
                               String addressList) {

        // load data
        XxlJobInfo jobInfo = xxlJobInfoMapper.loadById(jobId);
        if (jobInfo == null) {
            log.warn(">>>>>>>>>>>> trigger fail, jobId invalid，jobId={}", jobId);
            return;
        }
        String effectiveParam = executorParam == null ? jobInfo.getExecutorParam() : executorParam;
        int finalFailRetryCount = failRetryCount>=0?failRetryCount:jobInfo.getExecutorFailRetryCount();
        XxlJobGroup group = resolveTriggerGroup(xxlJobGroupMapper.load(jobInfo.getJobGroup()), addressList);

        // sharding param
        Date triggerTime = new Date();
        int[] shardingParam = parseShardingParam(executorShardingParam);
        if (StringTool.isNotBlank(executorShardingParam) && shardingParam == null) {
            processTrigger(group, jobInfo, effectiveParam, finalFailRetryCount, triggerType, triggerTime, 0, 1,
                    I18nUtil.getString("jobconf_trigger_sharding_invalid"));
            return;
        }
        List<String> addresses = group == null ? null : group.getRegistryList();
        if (ExecutorRouteStrategyEnum.SHARDING_BROADCAST == ExecutorRouteStrategyEnum.match(jobInfo.getExecutorRouteStrategy(), null)
                && addresses != null && !addresses.isEmpty() && shardingParam == null) {
            for (int i = 0; i < addresses.size(); i++) {
                processTrigger(group, jobInfo, effectiveParam, finalFailRetryCount, triggerType, triggerTime, i, addresses.size(), null);
            }
        } else {
            int index = shardingParam == null ? 0 : shardingParam[0];
            int total = shardingParam == null ? 1 : shardingParam[1];
            processTrigger(group, jobInfo, effectiveParam, finalFailRetryCount, triggerType, triggerTime, index, total, null);
        }

    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 23:03
     * 说明: 以独立的执行器组配置承载本次触发的临时地址，避免改写 Mapper 返回的数据。
     * @param group 原执行器组，历史数据中可能缺失
     * @param addressList 临时地址列表，空白时使用原执行器组
     * @return
     * 返回值: XxlJobGroup
     * 返回值说明: 临时地址配置保留原组的应用和通信凭据
     */
    private XxlJobGroup resolveTriggerGroup(XxlJobGroup group, String addressList) {
        if (group == null || StringTool.isBlank(addressList)) {
            return group;
        }
        XxlJobGroup override = new XxlJobGroup();
        override.setId(group.getId());
        override.setAppname(group.getAppname());
        override.setName(group.getName());
        override.setAccessToken(group.getAccessToken());
        override.setAddressType(1);
        override.setAddressList(addressList.trim());
        return override;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 20:52
     * 说明: 线程池拒绝派发或停机取消排队任务时，直接写入失败日志。
     * @param jobId 任务 ID
     * @param triggerType 触发类型
     * @param failRetryCount 剩余重试次数，小于 0 时使用任务配置
     * @param executorShardingParam 可选分片编号，重试时保持原分片
     * @param executorParam 临时任务参数，为空时使用任务配置
     * @param cancelledAfterAccept 已入队但因停机取消时为 true
     * @return
     * 返回值: void
     * 返回值说明: 任务存在时插入一条失败日志，未开始的手动派发不后台重试
     */
    public void recordUndispatchedTrigger(int jobId, TriggerTypeEnum triggerType, int failRetryCount,
                                          String executorShardingParam, String executorParam, boolean cancelledAfterAccept) {
        XxlJobInfo job = xxlJobInfoMapper.loadById(jobId);
        if (job == null) {
            log.warn("Undispatched job could not be recorded because it no longer exists, jobId={}", jobId);
            return;
        }

        String message = I18nUtil.getString(cancelledAfterAccept
                ? "jobconf_trigger_shutdown_cancelled" : "jobconf_trigger_pool_rejected");
        int retryCount = failRetryCount >= 0 ? failRetryCount : job.getExecutorFailRetryCount();
        boolean invalidShard = StringTool.isNotBlank(executorShardingParam) && parseShardingParam(executorShardingParam) == null;
        XxlJobLog log = new XxlJobLog();
        log.setJobId(jobId);
        log.setJobGroup(job.getJobGroup());
        log.setTriggerTime(new Date());
        log.setTriggerCode(Response.ofFail(message).getCode());
        log.setTriggerMsg(I18nUtil.getString("jobconf_trigger_type") + "：" + triggerType.getTitle() + "<br>" + message);
        log.setExecutorHandler(job.getExecutorHandler());
        log.setExecutorParam(executorParam == null ? job.getExecutorParam() : executorParam);
        log.setExecutorShardingParam(invalidShard ? null : executorShardingParam);
        log.setExecutorFailRetryCount(triggerType == TriggerTypeEnum.MANUAL || invalidShard ? 0 : Math.max(0, retryCount));
        if (xxlJobLogMapper.save(log) != 1) {
            throw new IllegalStateException("Undispatched job log was not inserted for jobId=" + jobId);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 20:11
     * 说明: 解析既有分片记录，检查索引范围和整数边界。
     * @param value 可选分片编号，格式为 索引/总数
     * @return
     * 返回值: int[]
     * 返回值说明: 分片索引和总数，缺失或无效时为空
     */
    private int[] parseShardingParam(String value) {
        if (StringTool.isBlank(value)) {
            return null;
        }
        String[] fields = value.split("/", -1);
        if (fields.length != 2 || !StringTool.isNumeric(fields[0]) || !StringTool.isNumeric(fields[1])) {
            return null;
        }
        try {
            int index = Integer.parseInt(fields[0]);
            int total = Integer.parseInt(fields[1]);
            return index >= 0 && index < total ? new int[]{index, total} : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 20:11
     * 说明: 创建调度日志后尝试路由和提交，限制备注长度并保留实际调度结果及失败原因。
     * @param group 执行器组，历史数据中可能缺失
     * @param jobInfo 任务配置
     * @param executorParam 本次派发的实际任务参数，可能为空
     * @param finalFailRetryCount 剩余重试次数
     * @param triggerType 触发类型
     * @param triggerTime 调度发起时间
     * @param index 分片索引
     * @param total 分片总数
     * @param preflightFailure 在提交前已发现的配置错误，为空时继续路由
     * @return
     * 返回值: void
     * 返回值说明: 持久化调度结果
     */
    private void processTrigger(XxlJobGroup group,
                                XxlJobInfo jobInfo,
                                String executorParam,
                                int finalFailRetryCount,
                                TriggerTypeEnum triggerType,
                                Date triggerTime,
                                int index,
                                int total,
                                String preflightFailure){

        // param
        ExecutorBlockStrategyEnum blockStrategy = ExecutorBlockStrategyEnum.match(jobInfo.getExecutorBlockStrategy(), ExecutorBlockStrategyEnum.SERIAL_EXECUTION);  // block strategy
        ExecutorRouteStrategyEnum executorRouteStrategyEnum = ExecutorRouteStrategyEnum.match(jobInfo.getExecutorRouteStrategy(), null);    // route strategy
        String shardingParam = preflightFailure == null && ExecutorRouteStrategyEnum.SHARDING_BROADCAST == executorRouteStrategyEnum
                ? index + "/" + total : null;
        // 无效分片不能由重试退化为其他分片或广播。
        int retryCount = preflightFailure == null ? finalFailRetryCount : 0;

        // 1、save log-id
        XxlJobLog jobLog = new XxlJobLog();
        jobLog.setJobGroup(jobInfo.getJobGroup());
        jobLog.setJobId(jobInfo.getId());
        jobLog.setTriggerTime(triggerTime);
        // 执行器可能在派发响应返回前回调，失败处理必须能从首条记录读取重试上下文。
        jobLog.setExecutorHandler(jobInfo.getExecutorHandler());
        jobLog.setExecutorParam(executorParam);
        jobLog.setExecutorShardingParam(shardingParam);
        jobLog.setExecutorFailRetryCount(retryCount);
        xxlJobLogMapper.save(jobLog);
        if (jobLog.getId() <= 0) {
            log.error("Trigger log was not created, jobId={}", jobInfo.getId());
            return;
        }
        log.debug(">>>>>>>>>>> xxl-job trigger start, jobId:{}", jobLog.getJobId());

        // 2、init trigger-param
        TriggerRequest triggerParam = new TriggerRequest();
        triggerParam.setJobId(jobInfo.getId());
        triggerParam.setExecutorHandler(jobInfo.getExecutorHandler());
        triggerParam.setExecutorParams(executorParam);
        triggerParam.setExecutorBlockStrategy(blockStrategy.name());
        triggerParam.setExecutorTimeout(jobInfo.getExecutorTimeout());
        triggerParam.setLogId(jobLog.getId());
        triggerParam.setLogDateTime(jobLog.getTriggerTime().getTime());
        triggerParam.setGlueType(jobInfo.getGlueType());
        triggerParam.setGlueSource(jobInfo.getGlueSource());
        triggerParam.setGlueUpdatetime(glueCacheVersion(jobInfo));
        triggerParam.setBroadcastIndex(index);
        triggerParam.setBroadcastTotal(total);

        // 3、init address
        String address = null;
        Response<String> routeAddressResult = null;
        List<String> addresses = group == null ? null : group.getRegistryList();
        if (preflightFailure != null) {
            routeAddressResult = Response.of(XxlJobContext.HANDLE_CODE_FAIL, preflightFailure);
        } else if (group == null) {
            routeAddressResult = Response.of(XxlJobContext.HANDLE_CODE_FAIL, I18nUtil.getString("jobgroup_empty"));
        } else if (executorRouteStrategyEnum == null) {
            routeAddressResult = Response.of(XxlJobContext.HANDLE_CODE_FAIL,
                    I18nUtil.getString("jobinfo_field_executorRouteStrategy") + I18nUtil.getString("system_invalid"));
        } else if (addresses != null && !addresses.isEmpty()) {
            if (ExecutorRouteStrategyEnum.SHARDING_BROADCAST == executorRouteStrategyEnum) {
                address = addresses.get(index < addresses.size() ? index : 0);
            } else {
                try {
                    routeAddressResult = executorRouteStrategyEnum.getRouter().route(triggerParam, group);
                    if (routeAddressResult != null && routeAddressResult.isSuccess()) {
                        address = routeAddressResult.getData();
                    }
                } catch (RuntimeException e) {
                    log.error("Routing failed, jobId={}, logId={}", jobInfo.getId(), jobLog.getId(), e);
                }
            }
        } else {
            routeAddressResult = Response.of(XxlJobContext.HANDLE_CODE_FAIL, I18nUtil.getString("jobconf_trigger_address_empty"));
        }

        // 历史执行器组中可能留有超长地址，避免已派发后无法保存调度结果。
        if (StringTool.isNotBlank(address) && !ExecutorAddressUtil.fitsAddressColumn(address)) {
            address = null;
            routeAddressResult = Response.ofFail(I18nUtil.getString("jobgroup_field_registryList_invalid"));
        }

        // 4、trigger remote executor
        Response<String> triggerResult = null;
        if (StringTool.isNotBlank(address)) {
            triggerResult = doTrigger(triggerParam, address, group);
        } else {
            triggerResult = Response.of(XxlJobContext.HANDLE_CODE_FAIL, "Address Router Fail.");
        }

        // 5、collection trigger info
        // trigger config
        StringBuilder triggerMsgSb = new StringBuilder();
        triggerMsgSb.append(I18nUtil.getString("jobconf_trigger_type")).append("：").append(triggerType.getTitle());
        triggerMsgSb.append("<br>").append(I18nUtil.getString("jobconf_trigger_admin_adress")).append("：").append(IPTool.getIp());
        triggerMsgSb.append("<br>").append(I18nUtil.getString("jobconf_trigger_exe_regtype")).append("：")
                .append(group == null ? I18nUtil.getString("system_empty")
                        : I18nUtil.getString(group.getAddressType() == 0 ? "jobgroup_field_addressType_0" : "jobgroup_field_addressType_1"));
        triggerMsgSb.append("<br>").append(I18nUtil.getString("jobconf_trigger_exe_regaddress")).append("：").append(addresses);
        triggerMsgSb.append("<br>").append(I18nUtil.getString("jobinfo_field_executorRouteStrategy")).append("：")
                .append(executorRouteStrategyEnum == null ? I18nUtil.getString("system_invalid") : executorRouteStrategyEnum.getTitle());
        if (shardingParam != null) {
            triggerMsgSb.append("(").append(shardingParam).append(")");
        }
        triggerMsgSb.append("<br>").append(I18nUtil.getString("jobinfo_field_executorBlockStrategy")).append("：").append(blockStrategy.getTitle());
        triggerMsgSb.append("<br>").append(I18nUtil.getString("jobinfo_field_timeout")).append("：").append(jobInfo.getExecutorTimeout());
        triggerMsgSb.append("<br>").append(I18nUtil.getString("jobinfo_field_executorFailRetryCount")).append("：").append(retryCount);

        // trigger data
        triggerMsgSb.append("<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>").append(I18nUtil.getString("jobconf_trigger_run")).append("<<<<<<<<<<< </span><br>");
        triggerMsgSb.append("<br>").append(I18nUtil.getString("joblog_field_executorAddress")).append("：");
        if (StringTool.isNotBlank(address)) {
            triggerMsgSb.append(address);
        } else if (routeAddressResult!=null && !routeAddressResult.isSuccess() && routeAddressResult.getMsg()!=null) {
            triggerMsgSb.append("address route fail, ").append(routeAddressResult.getMsg());
        } else {
            triggerMsgSb.append("address route fail.");
        }
        if (StringTool.isNotBlank(jobInfo.getExecutorHandler())) {
            triggerMsgSb.append("<br>").append("JobHandler").append("：").append(jobInfo.getExecutorHandler());
        }
        triggerMsgSb.append("<br>").append(I18nUtil.getString("jobinfo_field_executorparam")).append("：").append(executorParam);
        StringBuilder resultMessage = new StringBuilder("<br>").append(I18nUtil.getString("joblog_field_triggerMsg")).append("：");
        if (triggerResult.isSuccess()) {
            resultMessage.append("success");
        } else if (triggerResult.getMsg()!=null) {
            resultMessage.append("error, ").append(triggerResult.getMsg());
        } else {
            resultMessage.append("fail");
        }

        // 6、save log trigger-info
        jobLog.setExecutorAddress(address);
        jobLog.setTriggerCode(triggerResult.getCode());
        jobLog.setTriggerMsg(fitTriggerMessage(triggerMsgSb.toString(), resultMessage.toString()));
        xxlJobLogMapper.updateTriggerInfo(jobLog);

        log.debug(">>>>>>>>>>> xxl-job trigger end, jobId:{}", jobLog.getJobId());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 23:00
     * 说明: 调度备注超过字段容量时，给执行器响应保留空间，避免参数占满备注。
     * @param context 调度配置、路由信息和执行参数
     * @param result 实际派发结果和诊断信息
     * @return
     * 返回值: String
     * 返回值说明: 不超过调度备注字节上限的完整或截断文本
     */
    private String fitTriggerMessage(String context, String result) {
        int resultBytes = result.getBytes(StandardCharsets.UTF_8).length;
        int contextBytes = context.getBytes(StandardCharsets.UTF_8).length;
        if (contextBytes <= MAX_TRIGGER_MESSAGE_BYTES - resultBytes) {
            return context + result;
        }
        String boundedContext = truncateMessage(context, MAX_TRIGGER_MESSAGE_BYTES
                - Math.min(resultBytes, MAX_TRIGGER_MESSAGE_BYTES / 2));
        return boundedContext + truncateMessage(result,
                MAX_TRIGGER_MESSAGE_BYTES - boundedContext.getBytes(StandardCharsets.UTF_8).length);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 23:00
     * 说明: 按 UTF-8 字节预算截断日志，避免拆开一个 Unicode 字符。
     * @param message 待保存的非空备注
     * @param maxBytes 包含省略标记在内的字节上限
     * @return
     * 返回值: String
     * 返回值说明: 可完整编码到指定字节容量的文本
     */
    private String truncateMessage(String message, int maxBytes) {
        if (message.getBytes(StandardCharsets.UTF_8).length <= maxBytes) {
            return message;
        }
        int remaining = maxBytes - MESSAGE_TRUNCATED.getBytes(StandardCharsets.UTF_8).length;
        int offset = 0;
        while (remaining > 0) {
            int codePoint = message.codePointAt(offset);
            int size = codePoint < 0x80 ? 1 : codePoint < 0x800 ? 2 : codePoint < 0x10000 ? 3 : 4;
            if (size > remaining) {
                break;
            }
            remaining -= size;
            offset += Character.charCount(codePoint);
        }
        return message.substring(0, offset) + MESSAGE_TRUNCATED;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 22:11
     * 说明: GLUE 执行器用版本值判断是否复用处理器，数据库时间只有秒精度。
     * @param job 本次派发读取的任务快照
     * @return
     * 返回值: long
     * 返回值说明: 有源码的 GLUE 任务返回类型和源码的稳定标识，其余任务沿用原时间值
     */
    private long glueCacheVersion(XxlJobInfo job) {
        GlueTypeEnum type = GlueTypeEnum.match(job.getGlueType());
        String source = job.getGlueSource();
        if (type != null && type != GlueTypeEnum.BEAN && source != null && !source.isEmpty()) {
            String digest = Sha256Tool.sha256(type.name() + "\n" + source);
            // 执行器只比较 long 是否相等，使用负值区间避开旧调度中心的现代时间戳。
            return Long.parseUnsignedLong(digest.substring(0, 16), 16) | Long.MIN_VALUE;
        }
        Date updated = job.getGlueUpdatetime() != null ? job.getGlueUpdatetime() : job.getUpdateTime();
        return updated == null ? 0L : updated.getTime();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 向选中的节点提交任务，将空响应和通信异常转换成调度失败。
     * @param triggerParam 调度参数，包含任务和日志 ID
     * @param address 选中的执行器地址
     * @param jobGroup 提供执行器客户端的通信配置
     * @return
     * 返回值: Response<String>
     * 返回值说明: 保留执行器的有效状态码，失败时提供可展示的原因
     */
    private Response<String> doTrigger(TriggerRequest triggerParam, String address, XxlJobGroup jobGroup) {
        try {
            ExecutorBiz executorBiz = XxlJobAdminBootstrap.getExecutorBiz(address, jobGroup);
            Response<String> result = executorBiz == null ? null : executorBiz.trigger(triggerParam);
            if (result == null || result.getCode() <= 0) {
                return Response.of(XxlJobContext.HANDLE_CODE_FAIL, I18nUtil.getString("jobconf_trigger_empty_response"));
            }
            String message = result.getMsg();
            String token = jobGroup.getAccessToken();
            if (message != null && StringTool.isNotBlank(token)) {
                message = message.replace(token, "***");
            }
            return Response.of(result.getCode(), I18nUtil.getString("jobconf_trigger_run") + "："
                    + "<br>address：" + HtmlUtils.htmlEscape(address, "UTF-8")
                    + "<br>code：" + result.getCode()
                    + "<br>msg：" + HtmlUtils.htmlEscape(message == null ? "" : message, "UTF-8"));
        } catch (Exception e) {
            // HTTP 客户端异常详情可能包含通信凭据，使用日志 ID 和异常类型定位。
            String errorType = e.getClass().getSimpleName();
            log.warn("Executor dispatch failed, jobId={}, logId={}, type={}",
                    triggerParam.getJobId(), triggerParam.getLogId(), errorType);
            return Response.of(XxlJobContext.HANDLE_CODE_FAIL,
                    I18nUtil.getString("jobconf_trigger_communication_error") + " (" + errorType + ")");
        }
    }

}
