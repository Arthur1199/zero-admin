package com.zero.jobadmin.business.model;

import com.xxl.tool.core.StringTool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 官方执行器配置，保留通信凭据读写，诊断输出不得包含AccessToken
 */
public class XxlJobGroup {

    /** 执行器组主键。 */
    private int id;
    /** 与执行器注册请求匹配的应用名称。 */
    private String appname;
    /** 调度控制台展示的执行器组名称。 */
    private String name;

    /** 地址维护方式，0 为自动注册，1 为手工录入。 */
    private int addressType;
    /** 逗号分隔的地址快照，自动注册模式由注册监控刷新，手工模式使用维护值。 */
    private String addressList;

    /** 调度中心调用该执行器组使用的通信凭据，禁止写入日志。 */
    private String accessToken;
    /** 执行器组记录最近更新时间。 */
    private Date updateTime;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 22:23
     * 说明: 从当前地址配置解析节点，地址被清空后立即反映无可用节点。
     * @return
     * 返回值: List<String>
     * 返回值说明: 保持地址顺序的列表，无地址时为空
     */
    public List<String> getRegistryList() {
        return StringTool.isNotBlank(addressList) ? StringTool.split(addressList, ",") : null;
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
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 供执行器缓存日志输出诊断信息，排除AccessToken以防通信凭据落入日志
     * @return
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
