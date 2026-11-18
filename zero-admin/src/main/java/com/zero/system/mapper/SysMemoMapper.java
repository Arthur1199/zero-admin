package com.zero.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.zero.system.domain.SysMemo;
import com.zero.system.domain.SysMemoQuery;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/17 21:20
 * 说明: 备忘录数据访问；普通账号按所属用户隔离，超级管理员标志仅由服务端登录态生成
 */
public interface SysMemoMapper
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17 21:20
     * 说明: 分页查询当前用户可见的元数据，不查询正文，不使用部门数据权限
     * @param query 标题、地址、可见范围筛选
     * @param userId 当前登录用户ID，不可来自客户端
     * @param admin 是否超级管理员，由服务端判断；true查询全部记录
     * @return
     * 返回值: List<SysMemo>
     * 返回值说明: 普通账号公开及本人记录，超级管理员全部记录，按创建时间及ID倒序稳定分页
     */
    List<SysMemo> selectMemoList(@Param("query") SysMemoQuery query, @Param("userId") Long userId, @Param("admin") boolean admin);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17 21:20
     * 说明: 查询可见详情，普通账号仅公开或本人，超级管理员可查询全部
     * @param memoId 备忘录ID
     * @param userId 当前登录用户ID
     * @param admin 是否超级管理员，只接受服务端判断结果
     * @return
     * 返回值: SysMemo
     * 返回值说明: 可见记录及密文，不可见或不存在返回null
     */
    SysMemo selectVisibleMemo(@Param("memoId") Long memoId, @Param("userId") Long userId, @Param("admin") boolean admin);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17 21:20
     * 说明: 写入已加密的备忘录，身份及审计字段必须由服务端设置
     * @param memo 服务端构造的备忘录，仅包含密文
     * @return
     * 返回值: int
     * 返回值说明: 插入条数
     */
    int insertMemo(SysMemo memo);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17 21:20
     * 说明: 创建人或超级管理员按版本更新，不改变原创建人，防止并发覆盖
     * @param memo 服务端设置所属用户和版本的备忘录
     * @param userId 当前操作用户ID，由服务端登录态取得
     * @param admin 是否超级管理员，只接受服务端判断结果
     * @return
     * 返回值: int
     * 返回值说明: 1成功，0表示无权操作、记录不存在或版本冲突
     */
    int updateMemo(@Param("memo") SysMemo memo, @Param("userId") Long userId, @Param("admin") boolean admin);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17 21:20
     * 说明: 创建人或超级管理员按版本删除，避免误删已被并发修改的内容
     * @param memoId 备忘录ID
     * @param userId 当前登录用户ID
     * @param version 用户确认删除时的记录版本
     * @param admin 是否超级管理员，只接受服务端判断结果
     * @return
     * 返回值: int
     * 返回值说明: 1成功，0表示无权操作、记录不存在或版本冲突
     */
    int deleteMemo(@Param("memoId") Long memoId, @Param("userId") Long userId, @Param("version") Long version, @Param("admin") boolean admin);
}

