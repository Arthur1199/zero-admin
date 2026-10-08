package com.zero.system.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.zero.common.core.domain.BaseEntity;

/**
 * 作者: Arthur 亚瑟
 * 时间: 2026/6/27 03:49
 * 说明: API谷歌验证配置实体，用于维护接口权限标识、API请求路径、谷歌验证码要求和启停状态
 *
 * 对应数据库表：sys_api_auth
 */
public class SysApiAuth extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /**
     * API谷歌验证配置主键ID
     */
    private Long id;

    /**
     * API权限标识，可选；填写后作为路径和请求方式命中后的附加校验条件
     */
    private String apiPermission;

    /**
     * API请求路径，可配置完整地址或后端接口路径
     */
    private String apiUrl;

    /**
     * API请求方式，ALL表示所有请求方式，具体值如GET、POST、PUT、DELETE、PATCH
     */
    private String apiMethod;

    /**
     * 是否需要谷歌验证码，0否，1是
     */
    private Integer isNeedGoogle;

    /**
     * API描述信息，说明该转发配置对应的业务用途
     */
    private String apiDesc;

    /**
     * API状态，-1删除，0禁用，1正常
     */
    private Integer apiStatus;

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/27 03:49
     * 说明: 获取API谷歌验证配置ID
     * @return
     * 返回值: Long
     * 返回值说明: API谷歌验证配置ID
     */
    public Long getId()
    {
        return id;
    }

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/27 03:49
     * 说明: 设置API谷歌验证配置ID
     * @param id API谷歌验证配置ID
     * @return
     * 返回值: void
     * 返回值说明: 设置API谷歌验证配置ID
     */
    public void setId(Long id)
    {
        this.id = id;
    }

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/27 03:49
     * 说明: 获取API权限标识
     * @return
     * 返回值: String
     * 返回值说明: API权限标识
     */
    @Size(min = 0, max = 255, message = "API权限标识不能超过255个字符")
    public String getApiPermission()
    {
        return apiPermission;
    }

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/27 03:49
     * 说明: 设置API权限标识
     * @param apiPermission API权限标识，可选；填写后必须与Controller权限注解一致
     * @return
     * 返回值: void
     * 返回值说明: 设置API权限标识
     */
    public void setApiPermission(String apiPermission)
    {
        this.apiPermission = apiPermission;
    }

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/27 03:49
     * 说明: 获取API请求路径
     * @return
     * 返回值: String
     * 返回值说明: API请求路径
     */
    @NotBlank(message = "API请求路径不能为空")
    @Size(min = 0, max = 500, message = "API请求路径不能超过500个字符")
    public String getApiUrl()
    {
        return apiUrl;
    }

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/27 03:49
     * 说明: 设置API请求路径
     * @param apiUrl API请求路径，可配置完整地址或后端接口路径
     * @return
     * 返回值: void
     * 返回值说明: 设置API请求路径
     */
    public void setApiUrl(String apiUrl)
    {
        this.apiUrl = apiUrl;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/27 05:36
     * 说明: 获取API请求方式，用于区分同一路径下不同HTTP动作的二次验证规则
     * @return
     * 返回值: String
     * 返回值说明: ALL、GET、POST、PUT、DELETE、PATCH之一
     */
    @NotBlank(message = "API请求方式不能为空")
    @Size(min = 0, max = 10, message = "API请求方式不能超过10个字符")
    public String getApiMethod()
    {
        return apiMethod;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/27 05:36
     * 说明: 设置API请求方式，用于按METHOD + Spring匹配路径精确控制接口谷歌验证
     * @param apiMethod API请求方式，ALL表示所有请求方式
     * @return
     * 返回值: void
     * 返回值说明: 设置API请求方式
     */
    public void setApiMethod(String apiMethod)
    {
        this.apiMethod = apiMethod;
    }

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/27 03:49
     * 说明: 获取是否需要谷歌验证码
     * @return
     * 返回值: Integer
     * 返回值说明: 0否，1是
     */
    @NotNull(message = "是否需要谷歌验证码不能为空")
    public Integer getIsNeedGoogle()
    {
        return isNeedGoogle;
    }

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/27 03:49
     * 说明: 设置是否需要谷歌验证码
     * @param isNeedGoogle 是否需要谷歌验证码，0否，1是
     * @return
     * 返回值: void
     * 返回值说明: 设置谷歌验证码要求
     */
    public void setIsNeedGoogle(Integer isNeedGoogle)
    {
        this.isNeedGoogle = isNeedGoogle;
    }

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/27 03:49
     * 说明: 获取API描述信息
     * @return
     * 返回值: String
     * 返回值说明: API描述信息
     */
    @Size(min = 0, max = 500, message = "API描述信息不能超过500个字符")
    public String getApiDesc()
    {
        return apiDesc;
    }

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/27 03:49
     * 说明: 设置API描述信息
     * @param apiDesc API描述信息，说明接口谷歌验证配置业务用途
     * @return
     * 返回值: void
     * 返回值说明: 设置API描述信息
     */
    public void setApiDesc(String apiDesc)
    {
        this.apiDesc = apiDesc;
    }

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/27 03:49
     * 说明: 获取API状态
     * @return
     * 返回值: Integer
     * 返回值说明: -1删除，0禁用，1正常
     */
    @NotNull(message = "API状态不能为空")
    public Integer getApiStatus()
    {
        return apiStatus;
    }

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/27 03:49
     * 说明: 设置API状态
     * @param apiStatus API状态，-1删除，0禁用，1正常
     * @return
     * 返回值: void
     * 返回值说明: 设置API状态
     */
    public void setApiStatus(Integer apiStatus)
    {
        this.apiStatus = apiStatus;
    }

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/27 03:49
     * 说明: toString
     * @return
     * 返回值: String
     * 返回值说明: API谷歌验证配置对象字符串
     */
    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("apiPermission", getApiPermission())
                .append("apiUrl", getApiUrl())
                .append("apiMethod", getApiMethod())
                .append("isNeedGoogle", getIsNeedGoogle())
                .append("apiDesc", getApiDesc())
                .append("apiStatus", getApiStatus())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }
}
