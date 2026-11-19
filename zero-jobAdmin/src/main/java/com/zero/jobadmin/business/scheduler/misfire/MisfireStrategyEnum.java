package com.zero.jobadmin.business.scheduler.misfire;

import com.zero.jobadmin.business.scheduler.misfire.strategy.MisfireDoNothing;
import com.zero.jobadmin.business.scheduler.misfire.strategy.MisfireFireOnceNow;
import com.zero.jobadmin.framework.util.I18nUtil;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 定义调度过期策略及其处理器，供任务配置和调度扫描选择。
 */
public enum MisfireStrategyEnum {

    /**
     * do nothing
     */
    DO_NOTHING(I18nUtil.getString("misfire_strategy_do_nothing"), new MisfireDoNothing()),

    /**
     * fire once now
     */
    FIRE_ONCE_NOW(I18nUtil.getString("misfire_strategy_fire_once_now"), new MisfireFireOnceNow());

    private final String title;
    private final MisfireHandler misfireHandler;

    MisfireStrategyEnum(String title, MisfireHandler misfireHandler) {
        this.title = title;
        this.misfireHandler = misfireHandler;
    }

    public String getTitle() {
        return title;
    }

    public MisfireHandler getMisfireHandler() {
        return misfireHandler;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: match misfire strategy
     * @param name name of misfire strategy
     * @param defaultItem default misfire strategy
     * @return
     * 返回值: MisfireStrategyEnum
     * 返回值说明: misfire strategy
     */
    public static MisfireStrategyEnum match(String name, MisfireStrategyEnum defaultItem){
        for (MisfireStrategyEnum item: MisfireStrategyEnum.values()) {
            if (item.name().equals(name)) {
                return item;
            }
        }
        return defaultItem;
    }

}
