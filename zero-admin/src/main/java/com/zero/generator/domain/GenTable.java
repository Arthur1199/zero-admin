package com.zero.generator.domain;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.apache.commons.lang3.ArrayUtils;
import com.zero.common.constant.GenConstants;
import com.zero.common.core.domain.BaseEntity;
import com.zero.common.utils.StringUtils;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 业务表 gen_table
 */
public class GenTable extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /**
     * 编号
     */
    private Long tableId;

    /**
     * 表名称
     */
    @NotBlank(message = "表名称不能为空")
    private String tableName;

    /**
     * 表描述
     */
    @NotBlank(message = "表描述不能为空")
    private String tableComment;

    /**
     * 关联父表的表名
     */
    private String subTableName;

    /**
     * 本表关联父表的外键名
     */
    private String subTableFkName;

    /**
     * 实体类名称(首字母大写)
     */
    @NotBlank(message = "实体类名称不能为空")
    private String className;

    /**
     * 使用的模板（crud单表操作 tree树表操作 sub主子表操作）
     */
    private String tplCategory;

    /**
     * 前端类型（element-ui模版 element-plus模版）
     */
    private String tplWebType;

    /**
     * 生成包路径
     */
    @NotBlank(message = "生成包路径不能为空")
    private String packageName;

    /**
     * 生成模块名
     */
    @NotBlank(message = "生成模块名不能为空")
    private String moduleName;

    /**
     * 生成业务名
     */
    @NotBlank(message = "生成业务名不能为空")
    private String businessName;

    /**
     * 生成功能名
     */
    @NotBlank(message = "生成功能名不能为空")
    private String functionName;

    /**
     * 生成作者
     */
    @NotBlank(message = "作者不能为空")
    private String functionAuthor;

    /**
     * 生成代码方式（0zip压缩包 1自定义路径）
     */
    private String genType;

    /**
     * 生成路径（不填默认项目路径）
     */
    private String genPath;

    /**
     * 主键信息
     */
    private GenTableColumn pkColumn;

    /**
     * 子表信息
     */
    private GenTable subTable;

    /**
     * 表列信息
     */
    @Valid
    private List<GenTableColumn> columns;

    /**
     * 其它生成选项
     */
    private String options;

    /**
     * 树编码字段
     */
    private String treeCode;

    /**
     * 树父编码字段
     */
    private String treeParentCode;

    /**
     * 树名称字段
     */
    private String treeName;

    /**
     * 上级菜单ID字段
     */
    private Long parentMenuId;

    /**
     * 上级菜单名称字段
     */
    private String parentMenuName;
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
     * 说明: 获取表名称
     * @return
     * 返回值: String
     * 返回值说明：获取表名称
     */

    public String getTableName()
    {
        return tableName;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置表名称
     * @param tableName 表名称参数
     * @return
     * 返回值: void
     * 返回值说明：设置表名称
     */

    public void setTableName(String tableName)
    {
        this.tableName = tableName;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取表Comment
     * @return
     * 返回值: String
     * 返回值说明：获取表Comment
     */

    public String getTableComment()
    {
        return tableComment;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置表Comment
     * @param tableComment 表Comment参数
     * @return
     * 返回值: void
     * 返回值说明：设置表Comment
     */

    public void setTableComment(String tableComment)
    {
        this.tableComment = tableComment;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Sub表名称
     * @return
     * 返回值: String
     * 返回值说明：获取Sub表名称
     */

    public String getSubTableName()
    {
        return subTableName;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Sub表名称
     * @param subTableName sub表名称参数
     * @return
     * 返回值: void
     * 返回值说明：设置Sub表名称
     */

    public void setSubTableName(String subTableName)
    {
        this.subTableName = subTableName;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Sub表Fk名称
     * @return
     * 返回值: String
     * 返回值说明：获取Sub表Fk名称
     */

    public String getSubTableFkName()
    {
        return subTableFkName;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Sub表Fk名称
     * @param subTableFkName sub表Fk名称参数
     * @return
     * 返回值: void
     * 返回值说明：设置Sub表Fk名称
     */

    public void setSubTableFkName(String subTableFkName)
    {
        this.subTableFkName = subTableFkName;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Class名称
     * @return
     * 返回值: String
     * 返回值说明：获取Class名称
     */

    public String getClassName()
    {
        return className;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Class名称
     * @param className class名称参数
     * @return
     * 返回值: void
     * 返回值说明：设置Class名称
     */

    public void setClassName(String className)
    {
        this.className = className;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取TplCategory
     * @return
     * 返回值: String
     * 返回值说明：获取TplCategory
     */

    public String getTplCategory()
    {
        return tplCategory;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置TplCategory
     * @param tplCategory tplCategory参数
     * @return
     * 返回值: void
     * 返回值说明：设置TplCategory
     */

    public void setTplCategory(String tplCategory)
    {
        this.tplCategory = tplCategory;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取TplWeb类型
     * @return
     * 返回值: String
     * 返回值说明：获取TplWeb类型
     */

    public String getTplWebType()
    {
        return tplWebType;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置TplWeb类型
     * @param tplWebType tplWeb类型参数
     * @return
     * 返回值: void
     * 返回值说明：设置TplWeb类型
     */

    public void setTplWebType(String tplWebType)
    {
        this.tplWebType = tplWebType;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Package名称
     * @return
     * 返回值: String
     * 返回值说明：获取Package名称
     */

    public String getPackageName()
    {
        return packageName;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Package名称
     * @param packageName package名称参数
     * @return
     * 返回值: void
     * 返回值说明：设置Package名称
     */

    public void setPackageName(String packageName)
    {
        this.packageName = packageName;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Module名称
     * @return
     * 返回值: String
     * 返回值说明：获取Module名称
     */

    public String getModuleName()
    {
        return moduleName;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Module名称
     * @param moduleName module名称参数
     * @return
     * 返回值: void
     * 返回值说明：设置Module名称
     */

    public void setModuleName(String moduleName)
    {
        this.moduleName = moduleName;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Business名称
     * @return
     * 返回值: String
     * 返回值说明：获取Business名称
     */

    public String getBusinessName()
    {
        return businessName;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Business名称
     * @param businessName business名称参数
     * @return
     * 返回值: void
     * 返回值说明：设置Business名称
     */

    public void setBusinessName(String businessName)
    {
        this.businessName = businessName;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Function名称
     * @return
     * 返回值: String
     * 返回值说明：获取Function名称
     */

    public String getFunctionName()
    {
        return functionName;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Function名称
     * @param functionName function名称参数
     * @return
     * 返回值: void
     * 返回值说明：设置Function名称
     */

    public void setFunctionName(String functionName)
    {
        this.functionName = functionName;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取FunctionAuthor
     * @return
     * 返回值: String
     * 返回值说明：获取FunctionAuthor
     */

    public String getFunctionAuthor()
    {
        return functionAuthor;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置FunctionAuthor
     * @param functionAuthor functionAuthor参数
     * @return
     * 返回值: void
     * 返回值说明：设置FunctionAuthor
     */

    public void setFunctionAuthor(String functionAuthor)
    {
        this.functionAuthor = functionAuthor;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取代码生成类型
     * @return
     * 返回值: String
     * 返回值说明：获取代码生成类型
     */

    public String getGenType()
    {
        return genType;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置代码生成类型
     * @param genType 代码生成类型参数
     * @return
     * 返回值: void
     * 返回值说明：设置代码生成类型
     */

    public void setGenType(String genType)
    {
        this.genType = genType;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取代码生成路径
     * @return
     * 返回值: String
     * 返回值说明：获取代码生成路径
     */

    public String getGenPath()
    {
        return genPath;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置代码生成路径
     * @param genPath 代码生成路径参数
     * @return
     * 返回值: void
     * 返回值说明：设置代码生成路径
     */

    public void setGenPath(String genPath)
    {
        this.genPath = genPath;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Pk字段
     * @return
     * 返回值: GenTableColumn
     * 返回值说明：获取Pk字段
     */

    public GenTableColumn getPkColumn()
    {
        return pkColumn;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Pk字段
     * @param pkColumn pk字段参数
     * @return
     * 返回值: void
     * 返回值说明：设置Pk字段
     */

    public void setPkColumn(GenTableColumn pkColumn)
    {
        this.pkColumn = pkColumn;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Sub表
     * @return
     * 返回值: GenTable
     * 返回值说明：获取Sub表
     */

    public GenTable getSubTable()
    {
        return subTable;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Sub表
     * @param subTable sub表参数
     * @return
     * 返回值: void
     * 返回值说明：设置Sub表
     */

    public void setSubTable(GenTable subTable)
    {
        this.subTable = subTable;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Columns
     * @return
     * 返回值: List<GenTableColumn>
     * 返回值说明：获取Columns
     */

    public List<GenTableColumn> getColumns()
    {
        return columns;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Columns
     * @param columns columns参数
     * @return
     * 返回值: void
     * 返回值说明：设置Columns
     */

    public void setColumns(List<GenTableColumn> columns)
    {
        this.columns = columns;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Options
     * @return
     * 返回值: String
     * 返回值说明：获取Options
     */

    public String getOptions()
    {
        return options;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Options
     * @param options options参数
     * @return
     * 返回值: void
     * 返回值说明：设置Options
     */

    public void setOptions(String options)
    {
        this.options = options;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取树编码
     * @return
     * 返回值: String
     * 返回值说明：获取树编码
     */

    public String getTreeCode()
    {
        return treeCode;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置树编码
     * @param treeCode 树编码参数
     * @return
     * 返回值: void
     * 返回值说明：设置树编码
     */

    public void setTreeCode(String treeCode)
    {
        this.treeCode = treeCode;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取树父节点编码
     * @return
     * 返回值: String
     * 返回值说明：获取树父节点编码
     */

    public String getTreeParentCode()
    {
        return treeParentCode;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置树父节点编码
     * @param treeParentCode 树父节点编码参数
     * @return
     * 返回值: void
     * 返回值说明：设置树父节点编码
     */

    public void setTreeParentCode(String treeParentCode)
    {
        this.treeParentCode = treeParentCode;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取树名称
     * @return
     * 返回值: String
     * 返回值说明：获取树名称
     */

    public String getTreeName()
    {
        return treeName;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置树名称
     * @param treeName 树名称参数
     * @return
     * 返回值: void
     * 返回值说明：设置树名称
     */

    public void setTreeName(String treeName)
    {
        this.treeName = treeName;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取父节点菜单ID
     * @return
     * 返回值: Long
     * 返回值说明：获取父节点菜单ID
     */

    public Long getParentMenuId()
    {
        return parentMenuId;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置父节点菜单ID
     * @param parentMenuId 父节点菜单ID参数
     * @return
     * 返回值: void
     * 返回值说明：设置父节点菜单ID
     */

    public void setParentMenuId(Long parentMenuId)
    {
        this.parentMenuId = parentMenuId;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取父节点菜单名称
     * @return
     * 返回值: String
     * 返回值说明：获取父节点菜单名称
     */

    public String getParentMenuName()
    {
        return parentMenuName;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置父节点菜单名称
     * @param parentMenuName 父节点菜单名称参数
     * @return
     * 返回值: void
     * 返回值说明：设置父节点菜单名称
     */

    public void setParentMenuName(String parentMenuName)
    {
        this.parentMenuName = parentMenuName;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 判断Sub
     * @return
     * 返回值: boolean
     * 返回值说明：判断Sub
     */

    public boolean isSub()
    {
        return isSub(this.tplCategory);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 判断Sub
     * @param tplCategory tplCategory参数
     * @return
     * 返回值: boolean
     * 返回值说明：判断Sub
     */

    public static boolean isSub(String tplCategory)
    {
        return tplCategory != null && StringUtils.equals(GenConstants.TPL_SUB, tplCategory);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 判断树
     * @return
     * 返回值: boolean
     * 返回值说明：判断树
     */

    public boolean isTree()
    {
        return isTree(this.tplCategory);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 判断树
     * @param tplCategory tplCategory参数
     * @return
     * 返回值: boolean
     * 返回值说明：判断树
     */

    public static boolean isTree(String tplCategory)
    {
        return tplCategory != null && StringUtils.equals(GenConstants.TPL_TREE, tplCategory);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 判断Crud
     * @return
     * 返回值: boolean
     * 返回值说明：判断Crud
     */

    public boolean isCrud()
    {
        return isCrud(this.tplCategory);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 判断Crud
     * @param tplCategory tplCategory参数
     * @return
     * 返回值: boolean
     * 返回值说明：判断Crud
     */

    public static boolean isCrud(String tplCategory)
    {
        return tplCategory != null && StringUtils.equals(GenConstants.TPL_CRUD, tplCategory);
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

    public boolean isSuperColumn(String javaField)
    {
        return isSuperColumn(this.tplCategory, javaField);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 判断Super字段
     * @param tplCategory tplCategory参数
     * @param javaField javaField参数
     * @return
     * 返回值: boolean
     * 返回值说明：判断Super字段
     */

    public static boolean isSuperColumn(String tplCategory, String javaField)
    {
        if (isTree(tplCategory))
        {
            return StringUtils.equalsAnyIgnoreCase(javaField,
                    ArrayUtils.addAll(GenConstants.TREE_ENTITY, GenConstants.BASE_ENTITY));
        }
        return StringUtils.equalsAnyIgnoreCase(javaField, GenConstants.BASE_ENTITY);
    }
}