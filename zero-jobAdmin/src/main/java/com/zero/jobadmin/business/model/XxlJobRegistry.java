package com.zero.jobadmin.business.model;

import java.util.Date;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 对应 xxl_job_registry 的注册记录，保存执行器分组、地址和最近心跳。
 */
public class XxlJobRegistry {

    /** 执行器注册记录的主键。 */
    private long id;

    /** 执行器注册协议中的注册分组。 */
    private String registryGroup;
    /** 注册执行器组的应用名。 */
    private String registryKey;
    /** 执行器上报的可访问地址。 */
    private String registryValue;

    /** 最近一次注册或心跳刷新时间，用于计算记录是否过期。 */
    private Date updateTime;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getRegistryGroup() {
        return registryGroup;
    }

    public void setRegistryGroup(String registryGroup) {
        this.registryGroup = registryGroup;
    }

    public String getRegistryKey() {
        return registryKey;
    }

    public void setRegistryKey(String registryKey) {
        this.registryKey = registryKey;
    }

    public String getRegistryValue() {
        return registryValue;
    }

    public void setRegistryValue(String registryValue) {
        this.registryValue = registryValue;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
