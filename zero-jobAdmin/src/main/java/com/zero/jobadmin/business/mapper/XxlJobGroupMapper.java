package com.zero.jobadmin.business.mapper;

import com.zero.jobadmin.business.model.XxlJobGroup;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * Created by xuxueli on 16/9/30.
 */
@Mapper
public interface XxlJobGroupMapper {

    public List<XxlJobGroup> findAll();

    public List<XxlJobGroup> findByAddressType(@Param("addressType") int addressType);

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

    public int remove(@Param("id") int id);

    public XxlJobGroup load(@Param("id") int id);

    public XxlJobGroup loadByAppname(@Param("appname") String appname);

    public List<XxlJobGroup> pageList(@Param("offset") int offset,
                                      @Param("pagesize") int pagesize,
                                      @Param("appname") String appname,
                                      @Param("name") String name);

    public int pageListCount(@Param("offset") int offset,
                             @Param("pagesize") int pagesize,
                             @Param("appname") String appname,
                             @Param("name") String name);

}
