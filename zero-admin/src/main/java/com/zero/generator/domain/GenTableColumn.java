package com.zero.generator.domain;

import jakarta.validation.constraints.NotBlank;
import com.zero.common.core.domain.BaseEntity;
import com.zero.common.utils.StringUtils;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 代码生成业务字段表 gen_table_column
 */
public class GenTableColumn extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /**
     * 编号
     */
    private Long columnId;

    /**
     * 归属表编号
     */
    private Long tableId;

    /**
     * 列名称
     */
    private String columnName;

    /**
     * 列描述
     */
    private String columnComment;

    /**
     * 列类型
     */
    private String columnType;

    /**
     * JAVA类型
     */
    private String javaType;

    /**
     * JAVA字段名
     */
    @NotBlank(message = "Java属性不能为空")
    private String javaField;

    /**
     * 是否主键（1是）
     */
    private String isPk;

    /**
     * 是否自增（1是）
     */
    private String isIncrement;

    /**
     * 是否必填（1是）
     */
    private String isRequired;

    /**
     * 是否为插入字段（1是）
     */
    private String isInsert;

    /**
     * 是否编辑字段（1是）
     */
    private String isEdit;

    /**
     * 是否列表字段（1是）
     */
    private String isList;

    /**
     * 是否查询字段（1是）
     */
    private String isQuery;

    /**
     * 查询方式（EQ等于、NE不等于、GT大于、LT小于、LIKE模糊、BETWEEN范围）
     */
    private String queryType;

    /**
     * 显示类型（input文本框、textarea文本域、select下拉框、checkbox复选框、radio单选框、datetime日期控件、image图片上传控件、upload文件上传控件、editor富文本控件）
     */
    private String htmlType;

    /**
     * 字典类型
     */
    private String dictType;

    /**
     * 排序
     */
    private Integer sort;
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置字段ID
     * @param columnId 字段ID参数
     * @return
     * 返回值: void
     * 返回值说明：设置字段ID
     */

    public void setColumnId(Long columnId)
    {
        this.columnId = columnId;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取字段ID
     * @return
     * 返回值: Long
     * 返回值说明：获取字段ID
     */

    public Long getColumnId()
    {
        return columnId;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置表ID
     * @param tableId 表ID参数
     * @return
     * 返回值: void
     * 返回值说明：设置表ID
     */

    public void setTableId(Long tableId)
    {
        this.tableId = tableId;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取表ID
     * @return
     * 返回值: Long
     * 返回值说明：获取表ID
     */

    public Long getTableId()
    {
        return tableId;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置字段名称
     * @param columnName 字段名称参数
     * @return
     * 返回值: void
     * 返回值说明：设置字段名称
     */

    public void setColumnName(String columnName)
    {
        this.columnName = columnName;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取字段名称
     * @return
     * 返回值: String
     * 返回值说明：获取字段名称
     */

    public String getColumnName()
    {
        return columnName;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置字段Comment
     * @param columnComment 字段Comment参数
     * @return
     * 返回值: void
     * 返回值说明：设置字段Comment
     */

    public void setColumnComment(String columnComment)
    {
        this.columnComment = columnComment;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取字段Comment
     * @return
     * 返回值: String
     * 返回值说明：获取字段Comment
     */

    public String getColumnComment()
    {
        return columnComment;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置字段类型
     * @param columnType 字段类型参数
     * @return
     * 返回值: void
     * 返回值说明：设置字段类型
     */

    public void setColumnType(String columnType)
    {
        this.columnType = columnType;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取字段类型
     * @return
     * 返回值: String
     * 返回值说明：获取字段类型
     */

    public String getColumnType()
    {
        return columnType;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Java类型
     * @param javaType java类型参数
     * @return
     * 返回值: void
     * 返回值说明：设置Java类型
     */

    public void setJavaType(String javaType)
    {
        this.javaType = javaType;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Java类型
     * @return
     * 返回值: String
     * 返回值说明：获取Java类型
     */

    public String getJavaType()
    {
        return javaType;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置JavaField
     * @param javaField javaField参数
     * @return
     * 返回值: void
     * 返回值说明：设置JavaField
     */

    public void setJavaField(String javaField)
    {
        this.javaField = javaField;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取JavaField
     * @return
     * 返回值: String
     * 返回值说明：获取JavaField
     */

    public String getJavaField()
    {
        return javaField;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取CapJavaField
     * @return
     * 返回值: String
     * 返回值说明：获取CapJavaField
     */

    public String getCapJavaField()
    {
        return StringUtils.capitalize(javaField);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置IsPk
     * @param isPk isPk参数
     * @return
     * 返回值: void
     * 返回值说明：设置IsPk
     */

    public void setIsPk(String isPk)
    {
        this.isPk = isPk;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取IsPk
     * @return
     * 返回值: String
     * 返回值说明：获取IsPk
     */

    public String getIsPk()
    {
        return isPk;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 判断Pk
     * @return
     * 返回值: boolean
     * 返回值说明：判断Pk
     */

    public boolean isPk()
    {
        return isPk(this.isPk);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 判断Pk
     * @param isPk isPk参数
     * @return
     * 返回值: boolean
     * 返回值说明：判断Pk
     */

    public boolean isPk(String isPk)
    {
        return isPk != null && StringUtils.equals("1", isPk);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取IsIncrement
     * @return
     * 返回值: String
     * 返回值说明：获取IsIncrement
     */

    public String getIsIncrement()
    {
        return isIncrement;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置IsIncrement
     * @param isIncrement isIncrement参数
     * @return
     * 返回值: void
     * 返回值说明：设置IsIncrement
     */

    public void setIsIncrement(String isIncrement)
    {
        this.isIncrement = isIncrement;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 判断Increment
     * @return
     * 返回值: boolean
     * 返回值说明：判断Increment
     */

    public boolean isIncrement()
    {
        return isIncrement(this.isIncrement);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 判断Increment
     * @param isIncrement isIncrement参数
     * @return
     * 返回值: boolean
     * 返回值说明：判断Increment
     */

    public boolean isIncrement(String isIncrement)
    {
        return isIncrement != null && StringUtils.equals("1", isIncrement);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置IsRequired
     * @param isRequired isRequired参数
     * @return
     * 返回值: void
     * 返回值说明：设置IsRequired
     */

    public void setIsRequired(String isRequired)
    {
        this.isRequired = isRequired;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取IsRequired
     * @return
     * 返回值: String
     * 返回值说明：获取IsRequired
     */

    public String getIsRequired()
    {
        return isRequired;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 判断Required
     * @return
     * 返回值: boolean
     * 返回值说明：判断Required
     */

    public boolean isRequired()
    {
        return isRequired(this.isRequired);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 判断Required
     * @param isRequired isRequired参数
     * @return
     * 返回值: boolean
     * 返回值说明：判断Required
     */

    public boolean isRequired(String isRequired)
    {
        return isRequired != null && StringUtils.equals("1", isRequired);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Is新增
     * @param isInsert is新增参数
     * @return
     * 返回值: void
     * 返回值说明：设置Is新增
     */

    public void setIsInsert(String isInsert)
    {
        this.isInsert = isInsert;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Is新增
     * @return
     * 返回值: String
     * 返回值说明：获取Is新增
     */

    public String getIsInsert()
    {
        return isInsert;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 判断新增
     * @return
     * 返回值: boolean
     * 返回值说明：判断新增
     */

    public boolean isInsert()
    {
        return isInsert(this.isInsert);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 判断新增
     * @param isInsert is新增参数
     * @return
     * 返回值: boolean
     * 返回值说明：判断新增
     */

    public boolean isInsert(String isInsert)
    {
        return isInsert != null && StringUtils.equals("1", isInsert);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Is修改
     * @param isEdit is修改参数
     * @return
     * 返回值: void
     * 返回值说明：设置Is修改
     */

    public void setIsEdit(String isEdit)
    {
        this.isEdit = isEdit;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Is修改
     * @return
     * 返回值: String
     * 返回值说明：获取Is修改
     */

    public String getIsEdit()
    {
        return isEdit;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 判断修改
     * @return
     * 返回值: boolean
     * 返回值说明：判断修改
     */

    public boolean isEdit()
    {
        return isInsert(this.isEdit);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 判断修改
     * @param isEdit is修改参数
     * @return
     * 返回值: boolean
     * 返回值说明：判断修改
     */

    public boolean isEdit(String isEdit)
    {
        return isEdit != null && StringUtils.equals("1", isEdit);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Is列表
     * @param isList is列表参数
     * @return
     * 返回值: void
     * 返回值说明：设置Is列表
     */

    public void setIsList(String isList)
    {
        this.isList = isList;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Is列表
     * @return
     * 返回值: String
     * 返回值说明：获取Is列表
     */

    public String getIsList()
    {
        return isList;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 判断列表
     * @return
     * 返回值: boolean
     * 返回值说明：判断列表
     */

    public boolean isList()
    {
        return isList(this.isList);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 判断列表
     * @param isList is列表参数
     * @return
     * 返回值: boolean
     * 返回值说明：判断列表
     */

    public boolean isList(String isList)
    {
        return isList != null && StringUtils.equals("1", isList);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Is查询
     * @param isQuery is查询参数
     * @return
     * 返回值: void
     * 返回值说明：设置Is查询
     */

    public void setIsQuery(String isQuery)
    {
        this.isQuery = isQuery;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Is查询
     * @return
     * 返回值: String
     * 返回值说明：获取Is查询
     */

    public String getIsQuery()
    {
        return isQuery;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 判断查询
     * @return
     * 返回值: boolean
     * 返回值说明：判断查询
     */

    public boolean isQuery()
    {
        return isQuery(this.isQuery);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 判断查询
     * @param isQuery is查询参数
     * @return
     * 返回值: boolean
     * 返回值说明：判断查询
     */

    public boolean isQuery(String isQuery)
    {
        return isQuery != null && StringUtils.equals("1", isQuery);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置查询类型
     * @param queryType 查询类型参数
     * @return
     * 返回值: void
     * 返回值说明：设置查询类型
     */

    public void setQueryType(String queryType)
    {
        this.queryType = queryType;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取查询类型
     * @return
     * 返回值: String
     * 返回值说明：获取查询类型
     */

    public String getQueryType()
    {
        return queryType;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取HTML类型
     * @return
     * 返回值: String
     * 返回值说明：获取HTML类型
     */

    public String getHtmlType()
    {
        return htmlType;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置HTML类型
     * @param htmlType HTML类型参数
     * @return
     * 返回值: void
     * 返回值说明：设置HTML类型
     */

    public void setHtmlType(String htmlType)
    {
        this.htmlType = htmlType;
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
     * 说明: 获取字典类型
     * @return
     * 返回值: String
     * 返回值说明：获取字典类型
     */

    public String getDictType()
    {
        return dictType;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置排序
     * @param sort 排序参数
     * @return
     * 返回值: void
     * 返回值说明：设置排序
     */

    public void setSort(Integer sort)
    {
        this.sort = sort;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取排序
     * @return
     * 返回值: Integer
     * 返回值说明：获取排序
     */

    public Integer getSort()
    {
        return sort;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 判断Super字段
     * @return
     * 返回值: boolean
     * 返回值说明：判断Super字段
     */

    public boolean isSuperColumn()
    {
        return isSuperColumn(this.javaField);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 判断Super字段
     * @param javaField javaField参数
     * @return
     * 返回值: boolean
     * 返回值说明：判断Super字段
     */

    public static boolean isSuperColumn(String javaField)
    {
        return StringUtils.equalsAnyIgnoreCase(javaField,
                // BaseEntity
                "createBy", "createTime", "updateBy", "updateTime", "remark",
                // TreeEntity
                "parentName", "parentId", "orderNum", "ancestors");
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 判断Usable字段
     * @return
     * 返回值: boolean
     * 返回值说明：判断Usable字段
     */

    public boolean isUsableColumn()
    {
        return isUsableColumn(javaField);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 判断Usable字段
     * @param javaField javaField参数
     * @return
     * 返回值: boolean
     * 返回值说明：判断Usable字段
     */

    public static boolean isUsableColumn(String javaField)
    {
        // isSuperColumn()中的名单用于避免生成多余Domain属性，若某些属性在生成页面时需要用到不能忽略，则放在此处白名单
        return StringUtils.equalsAnyIgnoreCase(javaField, "parentId", "orderNum", "remark");
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: readConverterExp
     * @return
     * 返回值: String
     * 返回值说明：readConverterExp
     */

    public String readConverterExp()
    {
        String remarks = StringUtils.substringBetween(this.columnComment, "（", "）");
        StringBuffer sb = new StringBuffer();
        if (StringUtils.isNotEmpty(remarks))
        {
            for (String value : remarks.split(" "))
            {
                if (StringUtils.isNotEmpty(value))
                {
                    Object startStr = value.subSequence(0, 1);
                    String endStr = value.substring(1);
                    sb.append("").append(startStr).append("=").append(endStr).append(",");
                }
            }
            return sb.deleteCharAt(sb.length() - 1).toString();
        }
        else
        {
            return this.columnComment;
        }
    }
}
