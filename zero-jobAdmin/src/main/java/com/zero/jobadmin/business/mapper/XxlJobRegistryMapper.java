package com.zero.jobadmin.business.mapper;

import com.zero.jobadmin.business.model.XxlJobRegistry;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * Created by xuxueli on 16/9/30.
 */
@Mapper
public interface XxlJobRegistryMapper {

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 19:58
     * 说明: 查找超过心跳有效期的节点，主键对应数据库 BIGINT。
     * @param timeout 心跳超时秒数
     * @param nowTime 本轮监控时间
     * @return
     * 返回值: List<Long>
     * 返回值说明: 超时节点的候选 ID，无候选时为空列表
     */
    List<Long> findDead(@Param("timeout") int timeout, @Param("nowTime") Date nowTime);

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

    public List<XxlJobRegistry> findAll(@Param("timeout") int timeout,
                                        @Param("nowTime") Date nowTime);

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

    public int registryDelete(@Param("registryGroup") String registryGroup,
                          @Param("registryKey") String registryKey,
                          @Param("registryValue") String registryValue);

    public int removeByRegistryGroupAndKey(@Param("registryGroup") String registryGroup,
                                           @Param("registryKey") String registryKey);

}
