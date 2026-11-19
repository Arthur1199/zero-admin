package com.zero.jobadmin.business.constant;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 定义任务是否参与自动调度的持久化状态。
 */
public enum TriggerStatus {

    /**
     * Stopped
     */
    STOPPED(0, "stopped"),

    /**
     * Running
     */
    RUNNING(1, "running");

    private int value;
    private String desc;

    TriggerStatus(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

}
