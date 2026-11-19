package com.zero.jobadmin.business.scheduler.complete;

import lombok.extern.slf4j.Slf4j;
import com.zero.jobadmin.business.mapper.XxlJobInfoMapper;
import com.zero.jobadmin.business.mapper.XxlJobLogMapper;
import com.zero.jobadmin.business.model.XxlJobInfo;
import com.zero.jobadmin.business.model.XxlJobLog;
import com.zero.jobadmin.business.scheduler.config.XxlJobAdminBootstrap;
import com.zero.jobadmin.business.scheduler.trigger.TriggerTypeEnum;
import com.zero.jobadmin.framework.util.I18nUtil;
import com.xxl.job.core.context.XxlJobContext;
import com.xxl.tool.core.StringTool;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 负责调度任务执行后的收尾处理，包括更新任务日志和触发关联任务。
 */
@Slf4j
@Component
public class JobCompleter {
    /** UTF-8 下按字符保守限制备注长度，避免超出 TEXT 容量。 */
    private static final int MAX_HANDLE_MESSAGE_LENGTH = 15_000;
    /** 主任务备注过长时，子任务提交结果至少可使用的空间。 */
    private static final int RESERVED_CHILD_MESSAGE_LENGTH = MAX_HANDLE_MESSAGE_LENGTH / 2;
    /** 标记因字段容量省略的备注内容。 */
    private static final String MESSAGE_TRUNCATED = "…";

    @Resource
    private XxlJobInfoMapper xxlJobInfoMapper;
    @Resource
    private XxlJobLogMapper xxlJobLogMapper;


    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 20:08
     * 说明: 首次写入成功终态后提交子任务，并为子任务派发结果保留备注空间。
     * @param xxlJobLog 持有最终执行结果的调度日志
     * @return
     * 返回值: int
     * 返回值说明: 成功写入终态返回 1，重复或无效请求返回 0
     */
    public int complete(XxlJobLog xxlJobLog) {

        if (xxlJobLog == null || xxlJobLog.getId() <= 0 || xxlJobLog.getHandleCode() <= 0) {
            return 0;
        }

        String storedMessage = limitHandleMessage(Objects.toString(xxlJobLog.getHandleMsg(), ""), MAX_HANDLE_MESSAGE_LENGTH);
        xxlJobLog.setHandleMsg(storedMessage);
        int updated = xxlJobLogMapper.updateHandleInfo(xxlJobLog);
        if (updated == 1 && XxlJobContext.HANDLE_CODE_SUCCESS == xxlJobLog.getHandleCode()) {
            int childLimit = Math.max(RESERVED_CHILD_MESSAGE_LENGTH, MAX_HANDLE_MESSAGE_LENGTH - storedMessage.length());
            String childMessage = limitHandleMessage(processChildJob(xxlJobLog), childLimit);
            if (!childMessage.isEmpty()) {
                String finalMessage = limitHandleMessage(storedMessage, MAX_HANDLE_MESSAGE_LENGTH - childMessage.length())
                        + childMessage;
                xxlJobLogMapper.updateHandleMessage(xxlJobLog.getId(), finalMessage);
                xxlJobLog.setHandleMsg(finalMessage);
            }
        }
        return updated;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 21:17
     * 说明: 执行器接受人工终止后，用单次更新记录终态并关闭该次执行的重试。
     * @param log 当前执行日志，包含已确认的人工终止结果
     * @return
     * 返回值: boolean
     * 返回值说明: 当前调用先写入终态时返回 true
     */
    public boolean completeManualTermination(XxlJobLog log) {
        log.setHandleMsg(limitHandleMessage(Objects.toString(log.getHandleMsg(), ""), MAX_HANDLE_MESSAGE_LENGTH));
        return xxlJobLogMapper.markManuallyTerminated(log) == 1;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 20:08
     * 说明: 在 UTF-16 字符边界截断超长备注，保留完整表情和扩展字形。
     * @param message 待保存的非空备注
     * @param maxLength 包含省略标记在内的字符上限
     * @return
     * 返回值: String
     * 返回值说明: 可保存到日志字段的备注
     */
    private String limitHandleMessage(String message, int maxLength) {
        if (message.length() <= maxLength) {
            return message;
        }
        int end = maxLength - MESSAGE_TRUNCATED.length();
        if (Character.isHighSurrogate(message.charAt(end - 1))) {
            end--;
        }
        return message.substring(0, end) + MESSAGE_TRUNCATED;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 20:08
     * 说明: 按配置顺序提交子任务，每次父任务完成对同一个子任务最多提交一次。
     * @param xxlJobLog 已成功完成的调度日志
     * @return
     * 返回值: String
     * 返回值说明: 子任务提交结果，未配置子任务时返回空串
     */
    private String processChildJob(XxlJobLog xxlJobLog){

        // 1、handle success, to trigger child job
        String triggerChildMsg = null;
        if (XxlJobContext.HANDLE_CODE_SUCCESS == xxlJobLog.getHandleCode()) {
            XxlJobInfo xxlJobInfo = xxlJobInfoMapper.loadById(xxlJobLog.getJobId());

            // process child job
            if (xxlJobInfo!=null && StringTool.isNotBlank(xxlJobInfo.getChildJobId())) {
                triggerChildMsg = "<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>"+ I18nUtil.getString("jobconf_trigger_child_run") +"<<<<<<<<<<< </span><br>";
                String[] childJobIds = xxlJobInfo.getChildJobId().split(",");
                Set<Integer> dispatched = new HashSet<>();
                for (int i = 0; i < childJobIds.length; i++) {

                    // process eath child
                    String childId = childJobIds[i].strip();
                    int childJobId = -1;
                    try {
                        if (StringTool.isNotBlank(childId) && StringTool.isNumeric(childId)) {
                            childJobId = Integer.parseInt(childId);
                        }
                    } catch (NumberFormatException ignored) {
                        // 历史配置可能包含超出 int 范围的子任务编号。
                    }
                    if (childJobId > 0) {
                        // valid
                        if (childJobId == xxlJobLog.getJobId()) {
                            log.debug(">>>>>>>>>>> xxl-job, XxlJobCompleter-finishJob ignore childJobId,  childJobId {} is self.", childJobId);
                            continue;
                        }
                        if (!dispatched.add(childJobId)) {
                            continue;
                        }

                        // trigger child job
                        boolean submitted = true;
                        try {
                            XxlJobAdminBootstrap.getInstance().getJobTriggerPoolHelper().trigger(childJobId, TriggerTypeEnum.PARENT, -1, null, null, null);
                        } catch (RuntimeException e) {
                            submitted = false;
                            log.error("Failed to submit child job, parentLogId={}, childJobId={}", xxlJobLog.getId(), childJobId, e);
                        }

                        // add msg
                        triggerChildMsg += MessageFormat.format(I18nUtil.getString("jobconf_callback_child_msg1"),
                                (i+1),
                                childJobIds.length,
                                childJobIds[i],
                                I18nUtil.getString(submitted ? "system_success" : "system_fail"),
                                "");
                    } else {
                        triggerChildMsg += MessageFormat.format(I18nUtil.getString("jobconf_callback_child_msg2"),
                                (i+1),
                                childJobIds.length,
                                childJobIds[i]);
                    }
                }

            }
        }

        return Objects.toString(triggerChildMsg, "");
    }

    /*private static boolean isNumeric(String str){
        try {
            int result = Integer.valueOf(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }*/

}
