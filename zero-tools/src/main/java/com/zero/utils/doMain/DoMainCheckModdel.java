package com.zero.utils.doMain;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 保存一次 DNS 查询节点的地区、响应耗时和记录 TTL。
 */
public class DoMainCheckModdel {

    /** DNS 查询节点的地区及网络标识。 */
    private String location;
    /** 查询节点返回 DNS 结果的耗时，单位毫秒。 */
    private long responseTime;
    /** DNS 查询结果报告的记录 TTL。 */
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
