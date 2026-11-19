package com.zero.jobadmin.business.mapper;

import com.zero.jobadmin.business.model.XxlJobGroup;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 读写 xxl_job_group 的执行器组配置及地址信息。
 */
@Mapper
public interface XxlJobGroupMapper {

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 读取全部执行器组，按应用名、名称和主键排序。
     * @return
     * 返回值: List<XxlJobGroup>
     * 返回值说明: 执行器组列表
     */
    public List<XxlJobGroup> findAll();

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 查找使用指定地址维护方式的执行器组。
     * @param addressType 地址类型，0 为自动注册，1 为手工配置
     * @return
     * 返回值: List<XxlJobGroup>
     * 返回值说明: 符合条件的执行器组列表
     */
    public List<XxlJobGroup> findByAddressType(@Param("addressType") int addressType);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 写入执行器组，并回填数据库生成的主键。
     * @param xxlJobGroup 要保存的执行器组
     * @return
     * 返回值: int
     * 返回值说明: 受影响的记录数
     */
    public int save(XxlJobGroup xxlJobGroup);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 20:00
     * 说明: 保存管理端配置，自动注册模式未变化时保留在线节点地址。
     * @param group 编辑后的执行器组配置
     * @param previousAddressType 编辑提交时读到的注册方式
     * @return
     * 返回值: int
     * 返回值说明: 保存命中的记录数
     */
    int update(@Param("group") XxlJobGroup group, @Param("previousAddressType") int previousAddressType);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 22:25
     * 说明: 仅当执行器组仍处于自动注册且读取的配置未变化时，写入本轮在线节点。
     * @param group 本轮读取的执行器组快照，包含 ID、原地址和更新时间
     * @param addressList 在线地址列表，无在线节点时为空
     * @param nowTime 本轮监控时间
     * @return
     * 返回值: int
     * 返回值说明: 快照仍有效且节点列表有变化时返回 1，否则为 0
     */
    int refreshAutoRegisteredAddresses(@Param("group") XxlJobGroup group, @Param("addressList") String addressList,
                                       @Param("nowTime") Date nowTime);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 按主键物理删除执行器组记录。
     * @param id 记录主键
     * @return
     * 返回值: int
     * 返回值说明: 受影响的记录数
     */
    public int remove(@Param("id") int id);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 查询单个执行器组的当前配置。
     * @param id 记录主键
     * @return
     * 返回值: XxlJobGroup
     * 返回值说明: 执行器组，未找到时返回 null
     */
    public XxlJobGroup load(@Param("id") int id);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 按应用名精确查询执行器组。
     * @param appname 执行器注册使用的应用名
     * @return
     * 返回值: XxlJobGroup
     * 返回值说明: 匹配的执行器组，未找到时返回 null
     */
    public XxlJobGroup loadByAppname(@Param("appname") String appname);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 按应用名、名称模糊筛选执行器组并分页。
     * @param offset 分页偏移量，从 0 开始
     * @param pagesize 每页最多记录数
     * @param appname 应用名筛选，可为空
     * @param name 执行器名称筛选，可为空
     * @return
     * 返回值: List<XxlJobGroup>
     * 返回值说明: 当前页执行器组
     */
    public List<XxlJobGroup> pageList(@Param("offset") int offset,
                                      @Param("pagesize") int pagesize,
                                      @Param("appname") String appname,
                                      @Param("name") String name);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 按执行器组分页列表的筛选条件统计总数。
     * @param offset 与列表配套的偏移量，统计时不使用
     * @param pagesize 与列表配套的条数，统计时不使用
     * @param appname 应用名筛选
     * @param name 执行器名称筛选
     * @return
     * 返回值: int
     * 返回值说明: 匹配记录总数
     */
    public int pageListCount(@Param("offset") int offset,
                             @Param("pagesize") int pagesize,
                             @Param("appname") String appname,
                             @Param("name") String name);

}
