package com.zero.jobadmin.business.scheduler.alarm.impl;

import com.zero.jobadmin.business.scheduler.alarm.JobAlarm;
import com.zero.jobadmin.business.scheduler.config.XxlJobAdminBootstrap;
import com.zero.jobadmin.business.model.XxlJobGroup;
import com.zero.jobadmin.business.model.XxlJobInfo;
import com.zero.jobadmin.business.model.XxlJobLog;
import com.zero.jobadmin.framework.util.I18nUtil;
import com.xxl.job.core.context.XxlJobContext;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * job alarm by email
 *
 * @author xuxueli 2020-01-19
 */
@Component
public class EmailJobAlarm implements JobAlarm {
    private static final Logger logger = LoggerFactory.getLogger(EmailJobAlarm.class);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 22:36
     * 说明: 将日志按文本写入独立邮件，批量投递以复用 SMTP 连接。
     * @param info 告警对应的任务配置，未配置收件人时跳过
     * @param jobLog 产生告警的调度日志
     * @return
     * 返回值: boolean
     * 返回值说明: 未配置邮箱或邮件均已提交发送时返回 true，存在发送失败时返回 false
     */
    @Override
    public boolean doAlarm(XxlJobInfo info, XxlJobLog jobLog) {
        if (info == null || info.getAlarmEmail() == null || info.getAlarmEmail().isBlank()) {
            return true;
        }
        if (jobLog == null) {
            return false;
        }
        Set<String> recipients = new LinkedHashSet<>();
        for (String part : info.getAlarmEmail().split(",")) {
            String address = part.trim();
            if (!address.isEmpty()) {
                recipients.add(address);
            }
        }
        if (recipients.isEmpty()) {
            logger.warn("No valid recipients configured for job alarm, logId={}", jobLog.getId());
            return false;
        }

        StringBuilder alarmContent = new StringBuilder("Alarm Job LogId=").append(jobLog.getId());
        if (jobLog.getTriggerCode() != XxlJobContext.HANDLE_CODE_SUCCESS) {
            alarmContent.append("<br>TriggerMsg=<br>").append(formatLogText(jobLog.getTriggerMsg()));
        }
        if (jobLog.getHandleCode() != 0 && jobLog.getHandleCode() != XxlJobContext.HANDLE_CODE_SUCCESS) {
            alarmContent.append("<br>HandleCode=").append(jobLog.getHandleCode())
                    .append("<br>HandleMsg=<br>").append(formatLogText(jobLog.getHandleMsg()));
        }

        XxlJobAdminBootstrap bootstrap = XxlJobAdminBootstrap.getInstance();
        XxlJobGroup group = bootstrap.getXxlJobGroupMapper().load(info.getJobGroup());
        String content = MessageFormat.format(loadEmailJobAlarmTemplate(),
                HtmlUtils.htmlEscape(group == null ? "" : Objects.toString(group.getName(), "")),
                Integer.toString(info.getId()),
                HtmlUtils.htmlEscape(Objects.toString(info.getName(), "")),
                alarmContent.toString());
        String personal = I18nUtil.getString("admin_name_full");
        String title = I18nUtil.getString("jobconf_monitor");
        boolean alarmResult = true;
        var sender = bootstrap.getMailSender();
        List<MimeMessage> messages = new ArrayList<>(recipients.size());
        for (String recipient : recipients) {
            try {
                MimeMessage mimeMessage = sender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, StandardCharsets.UTF_8.name());
                helper.setValidateAddresses(true);
                helper.setFrom(bootstrap.getEmailFrom(), personal);
                helper.setTo(recipient);
                helper.setSubject(title);
                helper.setText(content, true);
                messages.add(mimeMessage);
            } catch (Exception e) {
                logger.error("Failed to prepare job alarm email, logId={}, errorType={}", jobLog.getId(),
                        e.getClass().getSimpleName());
                alarmResult = false;
            }
        }
        if (!messages.isEmpty()) {
            try {
                sender.send(messages.toArray(MimeMessage[]::new));
            } catch (org.springframework.mail.MailException e) {
                logger.error("Failed to send job alarm email batch, logId={}, errorType={}", jobLog.getId(),
                        e.getClass().getSimpleName());
                alarmResult = false;
            }
        }
        return alarmResult;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 22:36
     * 说明: 将日志内容转为可嵌入邮件正文的文本，仅保留调度日志的换行标记。
     * @param message 原始日志，可为空
     * @return
     * 返回值: String
     * 返回值说明: 转义后的文本及换行标签
     */
    private static String formatLogText(String message) {
        return HtmlUtils.htmlEscape(Objects.toString(message, ""))
                .replace("&lt;br&gt;", "<br>").replace("\r\n", "\n").replace("\r", "\n").replace("\n", "<br>");
    }

    /**
     *
     * 作者: xuxueli
     * 时间: 2020/1/19
     * 说明: 生成带本地化表头的告警邮件模板。
     * @return
     * 返回值: String
     * 返回值说明: 包含执行器、任务和告警正文占位符的 HTML 模板
     */
    private static final String loadEmailJobAlarmTemplate(){
        String mailBodyTemplate = "<h5>" + I18nUtil.getString("jobconf_monitor_detail") + "：</h5>" +
                "<table border=\"1\" cellpadding=\"3\" style=\"border-collapse:collapse; width:80%;\" >\n" +
                "   <thead style=\"font-weight: bold;color: #ffffff;background-color: #ff8c00;\" >" +
                "      <tr>\n" +
                "         <td width=\"20%\" >"+ I18nUtil.getString("jobinfo_field_jobgroup") +"</td>\n" +
                "         <td width=\"10%\" >"+ I18nUtil.getString("jobinfo_field_id") +"</td>\n" +
                "         <td width=\"20%\" >"+ I18nUtil.getString("jobinfo_field_jobdesc") +"</td>\n" +
                "         <td width=\"10%\" >"+ I18nUtil.getString("jobconf_monitor_alarm_title") +"</td>\n" +
                "         <td width=\"40%\" >"+ I18nUtil.getString("jobconf_monitor_alarm_content") +"</td>\n" +
                "      </tr>\n" +
                "   </thead>\n" +
                "   <tbody>\n" +
                "      <tr>\n" +
                "         <td>{0}</td>\n" +
                "         <td>{1}</td>\n" +
                "         <td>{2}</td>\n" +
                "         <td>"+ I18nUtil.getString("jobconf_monitor_alarm_type") +"</td>\n" +
                "         <td>{3}</td>\n" +
                "      </tr>\n" +
                "   </tbody>\n" +
                "</table>";

        return mailBodyTemplate;
    }

}
