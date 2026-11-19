package com.zero.jobadmin.business.mapper;

import com.zero.jobadmin.business.model.XxlJobRegistry;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 读写 xxl_job_registry 中执行器注册地址及心跳时间。
 */
@Mapper
public interface XxlJobRegistryMapper {

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 19:58
     * 说明: 按主键顺序限量查找超过心跳有效期的节点，主键对应数据库 BIGINT。
     * @param timeout 心跳超时秒数
     * @param nowTime 本轮监控时间
     * @param limit 每轮最多读取的候选数量
     * @return
     * 返回值: List<Long>
     * 返回值说明: 超时节点的候选 ID，无候选时为空列表
     */
    List<Long> findDead(@Param("timeout") int timeout, @Param("nowTime") Date nowTime, @Param("limit") int limit);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 19:56
     * 说明: 清理仍然超时的候选节点，保留查询后已续约的节点。
     * @param ids 本轮查询得到的非空候选节点 ID 列表
     * @param timeout 心跳超时秒数
     * @param nowTime 本轮查询超时节点时使用的时间
     * @return
     * 返回值: int
     * 返回值说明: 实际清理的节点数
     */
    int removeDead(@Param("ids") List<Long> ids, @Param("timeout") int timeout, @Param("nowTime") Date nowTime);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 查询心跳未超过指定超时时长的注册记录。
     * @param timeout 超时阈值，单位秒
     * @param nowTime 判断心跳是否有效的基准时间
     * @return
     * 返回值: List<XxlJobRegistry>
     * 返回值说明: 最后心跳处于有效窗口的注册记录
     */
    public List<XxlJobRegistry> findAll(@Param("timeout") int timeout,
                                        @Param("nowTime") Date nowTime);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 22:23
     * 说明: 在有权限的执行器组切换注册方式时，查询该应用的有效心跳。
     * @param registryGroup 注册类型
     * @param registryKey 已有执行器组的 AppName
     * @param timeout 心跳有效期，单位秒
     * @param nowTime 本轮查询的基准时间
     * @return
     * 返回值: List<XxlJobRegistry>
     * 返回值说明: 只返回类型、应用名和节点地址，结果仍由调用方按应用名精确匹配
     */
    List<XxlJobRegistry> findActiveByGroupAndKey(@Param("registryGroup") String registryGroup,
                                               @Param("registryKey") String registryKey,
                                               @Param("timeout") int timeout,
                                               @Param("nowTime") Date nowTime);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 按注册类型、应用名和地址写入注册记录，已注册时刷新心跳。
     * @param registryGroup 注册分组
     * @param registryKey 注册应用名
     * @param registryValue 执行器地址
     * @param updateTime 心跳时间
     * @return
     * 返回值: int
     * 返回值说明: 数据库返回的影响行数
     */
    public int registrySaveOrUpdate(@Param("registryGroup") String registryGroup,
                            @Param("registryKey") String registryKey,
                            @Param("registryValue") String registryValue,
                            @Param("updateTime") Date updateTime);

    /*public int registryUpdate(@Param("registryGroup") String registryGroup,
                              @Param("registryKey") String registryKey,
                              @Param("registryValue") String registryValue,
                              @Param("updateTime") Date updateTime);

    public int registrySave(@Param("registryGroup") String registryGroup,
                            @Param("registryKey") String registryKey,
                            @Param("registryValue") String registryValue,
                            @Param("updateTime") Date updateTime);*/

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 精确删除主动下线的单个执行器注册地址。
     * @param registryGroup 注册分组
     * @param registryKey 注册应用名
     * @param registryValue 执行器地址
     * @return
     * 返回值: int
     * 返回值说明: 删除行数
     */
    public int registryDelete(@Param("registryGroup") String registryGroup,
                          @Param("registryKey") String registryKey,
                          @Param("registryValue") String registryValue);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 删除指定注册分组和应用名称下的全部地址。
     * @param registryGroup 注册分组
     * @param registryKey 注册应用名
     * @return
     * 返回值: int
     * 返回值说明: 删除行数
     */
    public int removeByRegistryGroupAndKey(@Param("registryGroup") String registryGroup,
                                           @Param("registryKey") String registryKey);

}
