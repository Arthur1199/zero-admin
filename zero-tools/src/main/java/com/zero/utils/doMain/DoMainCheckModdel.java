package com.zero.utils.doMain;

/**
 * 作者: Arthur 亚瑟
 * 时间: 2025/7/21 00:28
 * 说明:
 */
public class DoMainCheckModdel {

    //地区
    private String location;
    //响应时间
    private long responseTime;
    //ttl时间
    private long ttl;

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
    }

    public long getTtl() {
        return ttl;
    }

    public void setTtl(long ttl) {
        this.ttl = ttl;
    }

    @Override
    public String toString() {
        return "{" +
                "位置='" + location + '\'' +
                ", 解析时间=" + responseTime + ",毫秒" +
                ", TTL=" + ttl + ",毫秒" +
                '}';
    }
}
