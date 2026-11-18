package com.zero.jobadmin.business.scheduler.complete;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.Objects;

/**
 * xxl-job job log complete
 *
 * @author xuxueli 2020-10-30 20:43:10
 */
@Component
public class JobCompleter {
    private static final Logger logger = LoggerFactory.getLogger(JobCompleter.class);

    /** UTF-8 下按字符保守限制备注长度，避免超出 TEXT 容量。 */
    private static final int MAX_HANDLE_MESSAGE_LENGTH = 15000;

    @Resource
    private XxlJobInfoMapper xxlJobInfoMapper;
    @Resource
    private XxlJobLogMapper xxlJobLogMapper;


    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 20:08
     * 说明: 在执行结果首次写入成功后提交子任务，重复完成请求直接返回。
     * @param xxlJobLog 持有最终执行结果的调度日志
     * @return
     * 返回值: int
     * 返回值说明: 成功写入终态返回 1，重复或无效请求返回 0
     */
    public int complete(XxlJobLog xxlJobLog) {

        if (xxlJobLog == null || xxlJobLog.getId() <= 0 || xxlJobLog.getHandleCode() <= 0) {
            return 0;
        }

        String storedMessage = limitHandleMessage(Objects.toString(xxlJobLog.getHandleMsg(), ""));
        xxlJobLog.setHandleMsg(storedMessage);
        int updated = xxlJobLogMapper.updateHandleInfo(xxlJobLog);
        if (updated == 1 && XxlJobContext.HANDLE_CODE_SUCCESS == xxlJobLog.getHandleCode()) {
            processChildJob(xxlJobLog);
            String finalMessage = limitHandleMessage(xxlJobLog.getHandleMsg());
            if (!storedMessage.equals(finalMessage)) {
                xxlJobLogMapper.updateHandleMessage(xxlJobLog.getId(), finalMessage);
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
        log.setHandleMsg(limitHandleMessage(Objects.toString(log.getHandleMsg(), "")));
        return xxlJobLogMapper.markManuallyTerminated(log) == 1;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 20:08
     * 说明: 在 UTF-16 字符边界截断超长备注，保留完整表情和扩展字形。
     * @param message 待保存的非空备注
     * @return
     * 返回值: String
     * 返回值说明: 可保存到日志字段的备注
     */
    private String limitHandleMessage(String message) {
        if (message.length() <= MAX_HANDLE_MESSAGE_LENGTH) {
            return message;
        }
        int end = MAX_HANDLE_MESSAGE_LENGTH;
        if (Character.isHighSurrogate(message.charAt(end - 1))) {
            end--;
        }
        return message.substring(0, end);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 20:08
     * 说明: 提交父任务配置中的子任务，并在内存中追加提交结果。
     * @param xxlJobLog 已成功完成的调度日志
     * @return
     * 返回值: void
     * 返回值说明: 仅在配置子任务时追加备注
     */
    private void processChildJob(XxlJobLog xxlJobLog){

        // 1、handle success, to trigger child job
        String triggerChildMsg = null;
        if (XxlJobContext.HANDLE_CODE_SUCCESS == xxlJobLog.getHandleCode()) {
            XxlJobInfo xxlJobInfo = xxlJobInfoMapper.loadById(xxlJobLog.getJobId());

            // process child job
            if (xxlJobInfo!=null && StringTool.isNotBlank(xxlJobInfo.getChildJobId())) {
                triggerChildMsg = "<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>"+ I18nUtil.getString("jobconf_trigger_child_run") +"<<<<<<<<<<< </span><br>";
                String[] childJobIds = xxlJobInfo.getChildJobId().split(",");
                for (int i = 0; i < childJobIds.length; i++) {

                    // process eath child
                    int childJobId = -1;
                    try {
                        if (StringTool.isNotBlank(childJobIds[i]) && StringTool.isNumeric(childJobIds[i])) {
                            childJobId = Integer.parseInt(childJobIds[i]);
                        }
                    } catch (NumberFormatException ignored) {
                        // 历史配置可能包含超出 int 范围的子任务编号。
                    }
                    if (childJobId > 0) {
                        // valid
                        if (childJobId == xxlJobLog.getJobId()) {
                            logger.debug(">>>>>>>>>>> xxl-job, XxlJobCompleter-finishJob ignore childJobId,  childJobId {} is self.", childJobId);
                            continue;
                        }

                        // trigger child job
                        boolean submitted = true;
                        try {
                            XxlJobAdminBootstrap.getInstance().getJobTriggerPoolHelper().trigger(childJobId, TriggerTypeEnum.PARENT, -1, null, null, null);
                        } catch (RuntimeException e) {
                            submitted = false;
                            logger.error("Failed to submit child job, parentLogId={}, childJobId={}", xxlJobLog.getId(), childJobId, e);
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

        // 2、append trigger-child message
        if (StringTool.isNotBlank(triggerChildMsg)) {
            xxlJobLog.setHandleMsg( xxlJobLog.getHandleMsg() + triggerChildMsg );
        }

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
