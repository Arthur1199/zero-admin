package com.zero.generator.mapper;

import java.util.List;
import com.zero.generator.domain.GenTable;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 业务 数据层
 */
public interface GenTableMapper
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
     * 说明: 查询表ID业务信息
     * @param id 业务ID
     * @return
     * 返回值: 业务信息
     * 返回值说明：查询表ID业务信息
     */
    public GenTable selectGenTableById(Long id);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 查询表名称业务信息
     * @param tableName 表名称
     * @return
     * 返回值: 业务信息
     * 返回值说明：查询表名称业务信息
     */
    public GenTable selectGenTableByName(String tableName);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 新增业务
     * @param genTable 业务信息
     * @return
     * 返回值: int
     * 返回值说明：新增业务
     */
    public int insertGenTable(GenTable genTable);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 修改业务
     * @param genTable 业务信息
     * @return
     * 返回值: int
     * 返回值说明：修改业务
     */
    public int updateGenTable(GenTable genTable);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 批量删除业务
     * @param ids 需要删除的数据ID
     * @return
     * 返回值: int
     * 返回值说明：批量删除业务
     */
    public int deleteGenTableByIds(Long[] ids);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 创建表
     * @param sql 表结构
     * @return
     * 返回值: int
     * 返回值说明：创建表
     */
    public int createTable(String sql);
}
