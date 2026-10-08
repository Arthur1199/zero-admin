package com.zero.common.core.domain.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.zero.common.annotation.Excel;
import com.zero.common.annotation.Excel.ColumnType;
import com.zero.common.constant.UserConstants;
import com.zero.common.core.domain.BaseEntity;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 字典数据表 sys_dict_data
 */
public class SysDictData extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /**
     * 字典编码
     */
    @Excel(name = "字典编码", cellType = ColumnType.NUMERIC)
    private Long dictCode;

    /**
     * 字典排序
     */
    @Excel(name = "字典排序", cellType = ColumnType.NUMERIC)
    private Long dictSort;

    /**
     * 字典标签
     */
    @Excel(name = "字典标签")
    private String dictLabel;

    /**
     * 字典键值
     */
    @Excel(name = "字典键值")
    private String dictValue;

    /**
     * 字典类型
     */
    @Excel(name = "字典类型")
    private String dictType;

    /**
     * 样式属性（其他样式扩展）
     */
    private String cssClass;

    /**
     * 表格字典样式
     */
    private String listClass;

    /**
     * 是否默认（Y是 N否）
     */
    @Excel(name = "是否默认", readConverterExp = "Y=是,N=否")
    private String isDefault;

    /**
     * 状态（0正常 1停用）
     */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取字典编码
     * @return
     * 返回值: Long
     * 返回值说明：获取字典编码
     */

    public Long getDictCode()
    {
        return dictCode;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置字典编码
     * @param dictCode 字典编码参数
     * @return
     * 返回值: void
     * 返回值说明：设置字典编码
     */

    public void setDictCode(Long dictCode)
    {
        this.dictCode = dictCode;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取字典排序
     * @return
     * 返回值: Long
     * 返回值说明：获取字典排序
     */

    public Long getDictSort()
    {
        return dictSort;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置字典排序
     * @param dictSort 字典排序参数
     * @return
     * 返回值: void
     * 返回值说明：设置字典排序
     */

    public void setDictSort(Long dictSort)
    {
        this.dictSort = dictSort;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取字典Label
     * @return
     * 返回值: String
     * 返回值说明：获取字典Label
     */

    @NotBlank(message = "字典标签不能为空")
    @Size(min = 0, max = 100, message = "字典标签长度不能超过100个字符")
    public String getDictLabel()
    {
        return dictLabel;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置字典Label
     * @param dictLabel 字典Label参数
     * @return
     * 返回值: void
     * 返回值说明：设置字典Label
     */

    public void setDictLabel(String dictLabel)
    {
        this.dictLabel = dictLabel;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取字典值
     * @return
     * 返回值: String
     * 返回值说明：获取字典值
     */

    @NotBlank(message = "字典键值不能为空")
    @Size(min = 0, max = 100, message = "字典键值长度不能超过100个字符")
    public String getDictValue()
    {
        return dictValue;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置字典值
     * @param dictValue 字典值参数
     * @return
     * 返回值: void
     * 返回值说明：设置字典值
     */

    public void setDictValue(String dictValue)
    {
        this.dictValue = dictValue;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取字典类型
     * @return
     * 返回值: String
     * 返回值说明：获取字典类型
     */

    @NotBlank(message = "字典类型不能为空")
    @Size(min = 0, max = 100, message = "字典类型长度不能超过100个字符")
    public String getDictType()
    {
        return dictType;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置字典类型
     * @param dictType 字典类型参数
     * @return
     * 返回值: void
     * 返回值说明：设置字典类型
     */

    public void setDictType(String dictType)
    {
        this.dictType = dictType;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取CssClass
     * @return
     * 返回值: String
     * 返回值说明：获取CssClass
     */

    @Size(min = 0, max = 100, message = "样式属性长度不能超过100个字符")
    public String getCssClass()
    {
        return cssClass;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置CssClass
     * @param cssClass cssClass参数
     * @return
     * 返回值: void
     * 返回值说明：设置CssClass
     */

    public void setCssClass(String cssClass)
    {
        this.cssClass = cssClass;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取列表Class
     * @return
     * 返回值: String
     * 返回值说明：获取列表Class
     */

    public String getListClass()
    {
        return listClass;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置列表Class
     * @param listClass 列表Class参数
     * @return
     * 返回值: void
     * 返回值说明：设置列表Class
     */

    public void setListClass(String listClass)
    {
        this.listClass = listClass;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Default
     * @return
     * 返回值: boolean
     * 返回值说明：获取Default
     */

    public boolean getDefault()
    {
        return UserConstants.YES.equals(this.isDefault);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取IsDefault
     * @return
     * 返回值: String
     * 返回值说明：获取IsDefault
     */

    public String getIsDefault()
    {
        return isDefault;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置IsDefault
     * @param isDefault isDefault参数
     * @return
     * 返回值: void
     * 返回值说明：设置IsDefault
     */

    public void setIsDefault(String isDefault)
    {
        this.isDefault = isDefault;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取状态
     * @return
     * 返回值: String
     * 返回值说明：获取状态
     */

    public String getStatus()
    {
        return status;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置状态
     * @param status 状态参数
     * @return
     * 返回值: void
     * 返回值说明：设置状态
     */

    public void setStatus(String status)
    {
        this.status = status;
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
            .append("dictCode", getDictCode())
            .append("dictSort", getDictSort())
            .append("dictLabel", getDictLabel())
            .append("dictValue", getDictValue())
            .append("dictType", getDictType())
            .append("cssClass", getCssClass())
            .append("listClass", getListClass())
            .append("isDefault", getIsDefault())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
