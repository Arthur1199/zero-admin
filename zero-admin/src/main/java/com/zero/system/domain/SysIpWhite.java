package com.zero.system.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.zero.common.core.domain.BaseEntity;

/**
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/16 05:06
 * 说明: 系统登录IP白名单实体，用于维护允许普通账号登录后台的IP规则
 *
 * 对应数据库表：sys_ip_white
 */
public class SysIpWhite extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /**
     * 白名单主键ID
     */
    private Long id;

    /**
     * IP地址，支持精确IP、通配符和IP段；该字段有唯一索引，避免重复规则
     */
    private String ipAddr;

    /**
     * IP归属地，精确IP新增或修改时可由后台自动解析国家、地区、城市等地址信息
     */
    private String ipRegion;

    /**
     * 状态，1启用参与登录校验，0关闭仅保留记录
     */
    private String status;

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 05:06
     * 说明: 获取白名单ID
     * @return
     * 返回值: Long
     * 返回值说明: 白名单ID
     */
    public Long getId()
    {
        return id;
    }

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 05:06
     * 说明: 设置白名单ID
     * @param id 白名单ID
     * @return
     * 返回值: void
     * 返回值说明: 设置白名单ID
     */
    public void setId(Long id)
    {
        this.id = id;
    }

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 05:06
     * 说明: 获取IP地址
     * @return
     * 返回值: String
     * 返回值说明: 精确IP、通配符或IP段
     */
    @NotBlank(message = "IP地址不能为空")
    @Size(min = 0, max = 100, message = "IP地址不能超过100个字符")
    public String getIpAddr()
    {
        return ipAddr;
    }

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 05:06
     * 说明: 设置IP地址
     * @param ipAddr IP地址，支持精确IP、通配符和IP段
     * @return
     * 返回值: void
     * 返回值说明: 设置IP地址
     */
    public void setIpAddr(String ipAddr)
    {
        this.ipAddr = ipAddr;
    }

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 05:06
     * 说明: 获取IP归属地
     * @return
     * 返回值: String
     * 返回值说明: IP归属地
     */
    @Size(min = 0, max = 255, message = "IP地区不能超过255个字符")
    public String getIpRegion()
    {
        return ipRegion;
    }

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 05:06
     * 说明: 设置IP归属地
     * @param ipRegion IP归属地，可为空，后台会在精确IP场景自动解析
     * @return
     * 返回值: void
     * 返回值说明: 设置IP归属地
     */
    public void setIpRegion(String ipRegion)
    {
        this.ipRegion = ipRegion;
    }

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 05:06
     * 说明: 获取白名单状态
     * @return
     * 返回值: String
     * 返回值说明: 1启用，0关闭
     */
    public String getStatus()
    {
        return status;
    }

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 05:06
     * 说明: 设置白名单状态
     * @param status 1启用，0关闭
     * @return
     * 返回值: void
     * 返回值说明: 设置白名单状态
     */
    public void setStatus(String status)
    {
        this.status = status;
    }

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 05:06
     * 说明: toString
     * @return
     * 返回值: String
     * 返回值说明: 白名单对象字符串
     */
    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("ipAddr", getIpAddr())
                .append("ipRegion", getIpRegion())
                .append("status", getStatus())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }
}
