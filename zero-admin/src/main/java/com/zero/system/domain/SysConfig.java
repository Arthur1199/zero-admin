package com.zero.system.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.zero.common.annotation.Excel;
import com.zero.common.annotation.Excel.ColumnType;
import com.zero.common.core.domain.BaseEntity;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 参数配置表 sys_config
 */
public class SysConfig extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /**
     * 参数主键
     */
    @Excel(name = "参数主键", cellType = ColumnType.NUMERIC)
    private Long configId;

    /**
     * 参数名称
     */
    @Excel(name = "参数名称")
    private String configName;

    /**
     * 参数键名
     */
    @Excel(name = "参数键名")
    private String configKey;

    /**
     * 参数键值
     */
    @Excel(name = "参数键值")
    private String configValue;

    /**
     * 系统内置（Y是 N否）
     */
    @Excel(name = "系统内置", readConverterExp = "Y=是,N=否")
    private String configType;
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取参数配置ID
     * @return
     * 返回值: Long
     * 返回值说明：获取参数配置ID
     */

    public Long getConfigId()
    {
        return configId;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置参数配置ID
     * @param configId 参数配置ID参数
     * @return
     * 返回值: void
     * 返回值说明：设置参数配置ID
     */

    public void setConfigId(Long configId)
    {
        this.configId = configId;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取参数配置名称
     * @return
     * 返回值: String
     * 返回值说明：获取参数配置名称
     */

    @NotBlank(message = "参数名称不能为空")
    @Size(min = 0, max = 100, message = "参数名称不能超过100个字符")
    public String getConfigName()
    {
        return configName;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置参数配置名称
     * @param configName 参数配置名称参数
     * @return
     * 返回值: void
     * 返回值说明：设置参数配置名称
     */

    public void setConfigName(String configName)
    {
        this.configName = configName;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取参数配置键名
     * @return
     * 返回值: String
     * 返回值说明：获取参数配置键名
     */

    @NotBlank(message = "参数键名长度不能为空")
    @Size(min = 0, max = 100, message = "参数键名长度不能超过100个字符")
    public String getConfigKey()
    {
        return configKey;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置参数配置键名
     * @param configKey 参数配置键名参数
     * @return
     * 返回值: void
     * 返回值说明：设置参数配置键名
     */

    public void setConfigKey(String configKey)
    {
        this.configKey = configKey;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取参数配置值
     * @return
     * 返回值: String
     * 返回值说明：获取参数配置值
     */

    @NotBlank(message = "参数键值不能为空")
    @Size(min = 0, max = 500, message = "参数键值长度不能超过500个字符")
    public String getConfigValue()
    {
        return configValue;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置参数配置值
     * @param configValue 参数配置值参数
     * @return
     * 返回值: void
     * 返回值说明：设置参数配置值
     */

    public void setConfigValue(String configValue)
    {
        this.configValue = configValue;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取参数配置类型
     * @return
     * 返回值: String
     * 返回值说明：获取参数配置类型
     */

    public String getConfigType()
    {
        return configType;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置参数配置类型
     * @param configType 参数配置类型参数
     * @return
     * 返回值: void
     * 返回值说明：设置参数配置类型
     */

    public void setConfigType(String configType)
    {
        this.configType = configType;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: toString
     * @return
     * 返回值: String
     * 返回值说明：toString
     */

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("configId", getConfigId())
            .append("configName", getConfigName())
            .append("configKey", getConfigKey())
            .append("configValue", getConfigValue())
            .append("configType", getConfigType())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
