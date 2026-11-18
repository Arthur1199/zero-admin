package com.zero.jobadmin.business.scheduler.type;

import com.zero.jobadmin.business.scheduler.cron.CronExpression;
import com.zero.jobadmin.business.scheduler.type.strategy.CronScheduleType;
import com.zero.jobadmin.business.scheduler.type.strategy.FixRateScheduleType;
import com.zero.jobadmin.business.scheduler.type.strategy.NoneScheduleType;
import com.zero.jobadmin.framework.util.I18nUtil;
import com.xxl.tool.response.Response;

import java.text.MessageFormat;

/**
 *
 * 作者: xuxueli
 * 时间: 2020/10/29 21:11:23
 * 说明: 定义调度类型及其配置校验规则。
 */
public enum ScheduleTypeEnum {

    NONE(I18nUtil.getString("schedule_type_none"), new NoneScheduleType()),

    /**
     * schedule by cron
     */
    CRON(I18nUtil.getString("schedule_type_cron"), new CronScheduleType()),

    /**
     * schedule by fixed rate (in seconds)
     */
    FIX_RATE(I18nUtil.getString("schedule_type_fix_rate"), new FixRateScheduleType()),

    /**
     * schedule by fix delay (in seconds)， after the last time
     */
    /*FIX_DELAY(I18nUtil.getString("schedule_type_fix_delay"))*/;

    /** xxl_job_info.schedule_conf 的字符上限。 */
    private static final int SCHEDULE_CONF_LIMIT = 128;

    private final String title;
    private final ScheduleType scheduleType;;

    ScheduleTypeEnum(String title, ScheduleType scheduleType) {
        this.title = title;
        this.scheduleType = scheduleType;
    }

    public String getTitle() {
        return title;
    }

    public ScheduleType getScheduleType() {
        return scheduleType;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 21:44
     * 说明: 为任务保存和预览检查调度配置的字段容量和语法。
     * @param type 已匹配的类型，无法匹配时为空
     * @param config Cron 表达式或固定间隔秒数，NONE 可省略
     * @return
     * 返回值: Response<String>
     * 返回值说明: 字段可保存且语法有效时返回成功
     */
    public static Response<String> validateConfiguration(ScheduleTypeEnum type, String config) {
        String invalid = I18nUtil.getString("schedule_type") + I18nUtil.getString("system_invalid");
        if (type == null) {
            return Response.ofFail(invalid);
        }
        if (config != null && config.codePointCount(0, config.length()) > SCHEDULE_CONF_LIMIT) {
            return Response.ofFail(MessageFormat.format(I18nUtil.getString("jobinfo_field_too_long"),
                    I18nUtil.getString("jobinfo_field_schedule_conf"), SCHEDULE_CONF_LIMIT));
        }
        if (type == CRON && (config == null || config.isBlank() || !CronExpression.isValidExpression(config))) {
            return Response.ofFail("Cron" + I18nUtil.getString("system_invalid"));
        }
        if (type == FIX_RATE) {
            try {
                FixRateScheduleType.parseIntervalSeconds(config);
            } catch (IllegalArgumentException e) {
                return Response.ofFail(invalid);
            }
        }
        return Response.ofSuccess();
    }

    /**
     * match by name
     *
     * @param name          name of ScheduleTypeEnum
     * @param defaultItem   default item
     * @return ScheduleTypeEnum
     */
    public static ScheduleTypeEnum match(String name, ScheduleTypeEnum defaultItem){
        for (ScheduleTypeEnum item: ScheduleTypeEnum.values()) {
            if (item.name().equals(name)) {
                return item;
            }
        }
        return defaultItem;
    }

}
