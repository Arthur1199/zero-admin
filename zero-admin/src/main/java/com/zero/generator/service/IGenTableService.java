package com.zero.generator.service;

import java.util.List;
import java.util.Map;
import com.zero.generator.domain.GenTable;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 业务 服务层
 */
public interface IGenTableService
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 查询业务列表
     * @param genTable 业务信息
     * @return
     * 返回值: 业务集合
     * 返回值说明：查询业务列表
     */
    public List<GenTable> selectGenTableList(GenTable genTable);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 查询据库列表
     * @param genTable 业务信息
     * @return
     * 返回值: 数据库表集合
     * 返回值说明：查询据库列表
     */
    public List<GenTable> selectDbTableList(GenTable genTable);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 查询据库列表
     * @param tableNames 表名称组
     * @return
     * 返回值: 数据库表集合
     * 返回值说明：查询据库列表
     */
    public List<GenTable> selectDbTableListByNames(String[] tableNames);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 查询所有表信息
     * @return
     * 返回值: 表信息集合
     * 返回值说明：查询所有表信息
     */
    public List<GenTable> selectGenTableAll();

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 查询业务信息
     * @param id 业务ID
     * @return
     * 返回值: 业务信息
     * 返回值说明：查询业务信息
     */
    public GenTable selectGenTableById(Long id);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 修改业务
     * @param genTable 业务信息
     * @return
     * 返回值: 结果
     * 返回值说明：修改业务
     */
    public void updateGenTable(GenTable genTable);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 删除业务信息
     * @param tableIds 需要删除的表数据ID
     * @return
     * 返回值: 结果
     * 返回值说明：删除业务信息
     */
    public void deleteGenTableByIds(Long[] tableIds);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 创建表
     * @param sql 创建表语句
     * @return
     * 返回值: 结果
     * 返回值说明：创建表
     */
    public boolean createTable(String sql);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 导入表结构
     * @param tableList 导入表列表
     * @param operName 操作人员
     * @return
     * 返回值: void
     * 返回值说明：导入表结构
     */
    public void importGenTable(List<GenTable> tableList, String operName);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 预览代码
     * @param tableId 表编号
     * @return
     * 返回值: 预览数据列表
     * 返回值说明：预览代码
     */
    public Map<String, String> previewCode(Long tableId);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 生成代码（下载方式）
     * @param tableName 表名称
     * @return
     * 返回值: 数据
     * 返回值说明：生成代码（下载方式）
     */
    public byte[] downloadCode(String tableName);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 生成代码（自定义路径）
     * @param tableName 表名称
     * @return
     * 返回值: 数据
     * 返回值说明：生成代码（自定义路径）
     */
    public void generatorCode(String tableName);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 同步数据库
     * @param tableName 表名称
     * @return
     * 返回值: void
     * 返回值说明：同步数据库
     */
    public void synchDb(String tableName);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 批量生成代码（下载方式）
     * @param tableNames 表数组
     * @return
     * 返回值: 数据
     * 返回值说明：批量生成代码（下载方式）
     */
    public byte[] downloadCode(String[] tableNames);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 修改保存参数校验
     * @param genTable 业务信息
     * @return
     * 返回值: void
     * 返回值说明：修改保存参数校验
     */
    public void validateEdit(GenTable genTable);
}
