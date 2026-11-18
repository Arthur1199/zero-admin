package com.zero.system.service;

import java.util.List;
import com.zero.system.domain.SysMemo;
import com.zero.system.domain.SysMemoQuery;
import com.zero.system.domain.SysMemoRequest;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/17 21:20
 * 说明: 备忘录业务服务；普通账号按可见范围及创建人隔离，超级管理员可管理全部记录
 */
public interface ISysMemoService
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17 21:20
     * 说明: 查询当前用户可见的备忘录，不返回正文
     * @param query 标题、地址和可见范围筛选
     * @return
     * 返回值: List<SysMemo>
     * 返回值说明: 普通账号返回公开及本人元数据，超级管理员返回全部元数据
     */
    List<SysMemo> selectMemoList(SysMemoQuery query);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17 21:20
     * 说明: 查看可见备忘录并解密正文
     * @param memoId 备忘录ID
     * @return
     * 返回值: SysMemo
     * 返回值说明: 可见详情，不可见与不存在统一报错
     */
    SysMemo selectMemoById(Long memoId);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17 21:20
     * 说明: 新增备忘录，创建人取登录态，正文加密存储
     * @param request 经过业务校验的新增参数
     * @return
     * 返回值: int
     * 返回值说明: 成功插入条数
     */
    int insertMemo(SysMemoRequest request);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17 21:20
     * 说明: 创建人或超级管理员修改备忘录，必须版本未变化，原创建人保持不变
     * @param request 修改参数，ID和版本必填
     * @return
     * 返回值: int
     * 返回值说明: 成功修改条数，越权或版本冲突时报错
     */
    int updateMemo(SysMemoRequest request);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17 21:20
     * 说明: 创建人或超级管理员删除备忘录，不保留正文或回收站
     * @param memoId 备忘录ID
     * @param version 列表或详情中读取的版本
     * @return
     * 返回值: int
     * 返回值说明: 成功删除条数，越权或版本冲突时报错
     */
    int deleteMemo(Long memoId, Long version);
}

