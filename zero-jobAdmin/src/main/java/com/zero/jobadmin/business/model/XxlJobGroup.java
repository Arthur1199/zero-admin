package com.zero.jobadmin.business.model;

import com.xxl.tool.core.StringTool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 *
 * 作者: xuxueli
 * 时间: 2016/9/30
 * 说明: 官方执行器配置，保留通信凭据读写，诊断输出不得包含AccessToken
 */
public class XxlJobGroup {

    private int id;
    private String appname;
    private String name;

    private int addressType;        // 执行器地址类型：0=自动注册、1=手动录入
    private String addressList;     // 执行器地址列表，多地址逗号分隔(手动录入)

    private String accessToken;
    private Date updateTime;

    // registry list
    private List<String> registryList;  // 执行器地址列表(系统注册)
    public List<String> getRegistryList() {

        if (StringTool.isNotBlank(addressList)) {
            registryList = StringTool.split(addressList, ",");
        }
        return registryList;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAppname() {
        return appname;
    }

    public void setAppname(String appname) {
        this.appname = appname;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAddressType() {
        return addressType;
    }

    public void setAddressType(int addressType) {
        this.addressType = addressType;
    }

    public String getAddressList() {
        return addressList;
    }

    public void setAddressList(String addressList) {
        this.addressList = addressList;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    /**
     *
     * 作者: xuxueli
     * 时间: 2026/11/18 15:53
     * 说明: 供执行器缓存日志输出诊断信息，排除AccessToken以防通信凭据落入日志
     * @return 不包含执行器通信凭据的对象说明
     * 返回值: String
     * 返回值说明: 仅包含执行器基础信息，不影响实际认证字段
     */
    @Override
    public String toString() {
        return "XxlJobGroup{" +
                "id=" + id +
                ", appname='" + appname + '\'' +
                ", name='" + name + '\'' +
                ", addressType=" + addressType +
                ", addressList='" + addressList + '\'' +
                ", updateTime=" + updateTime +
                '}';
    }

}
