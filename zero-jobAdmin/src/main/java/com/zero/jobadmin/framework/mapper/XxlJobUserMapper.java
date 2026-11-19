package com.zero.jobadmin.framework.mapper;

import com.zero.jobadmin.framework.model.XxlJobUser;
import com.xxl.tool.response.Response;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 读写 xxl_job_user 中的控制台账号、角色及会话信息。
 */
@Mapper
public interface XxlJobUserMapper {

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/18 20:06
	 * 说明: 用户管理列表只读取展示和编辑所需字段，认证字段由登录查询单独读取。
	 * @param offset 起始位置
	 * @param pagesize 本页最大条数
	 * @param username 账号筛选，可为空
	 * @param role 角色筛选，-1 表示全部
	 * @return
	 * 返回值: List<XxlJobUser>
	 * 返回值说明: 用户列表，密码和登录令牌为空
	 */
	public List<XxlJobUser> pageList(@Param("offset") int offset,
                                     @Param("pagesize") int pagesize,
                                     @Param("username") String username,
									 @Param("role") int role);
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 按账号和角色筛选条件统计调度控制台用户数。
	 * @param offset 与列表配套的偏移量，统计时不使用
	 * @param pagesize 与列表配套的条数，统计时不使用
	 * @param username 登录名筛选
	 * @param role 角色筛选，小于 0 时不限
	 * @return
	 * 返回值: int
	 * 返回值说明: 匹配用户总数
	 */
	public int pageListCount(@Param("offset") int offset,
							 @Param("pagesize") int pagesize,
							 @Param("username") String username,
							 @Param("role") int role);

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 按精确登录名读取调度控制台账号。
	 * @param username 控制台登录名
	 * @return
	 * 返回值: XxlJobUser
	 * 返回值说明: 匹配的用户，未找到时返回 null
	 */
	public XxlJobUser loadByUserName(@Param("username") String username);

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 读取指定主键的调度控制台账号。
	 * @param id 控制台用户主键
	 * @return
	 * 返回值: XxlJobUser
	 * 返回值说明: 用户记录，未找到时返回 null
	 */
	public XxlJobUser loadById(@Param("id") int id);

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 保存控制台账号、角色和执行器授权，并回填用户主键。
	 * @param xxlJobUser 待持久化的账号数据
	 * @return
	 * 返回值: int
	 * 返回值说明: 受影响的记录数
	 */
	public int save(XxlJobUser xxlJobUser);

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 按主键更新调度控制台账号。
	 * @param xxlJobUser 待更新的账号数据
	 * @return
	 * 返回值: int
	 * 返回值说明: 受影响的记录数
	 */
	public int update(XxlJobUser xxlJobUser);

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/18 22:13
	 * 说明: 仅在当前密码仍匹配时修改密码，避免覆盖并发重置和角色权限修改。
	 * @param id 当前已登录用户 ID
	 * @param previousHash 已校验的旧密码摘要
	 * @param newHash 新密码摘要
	 * @return
	 * 返回值: int
	 * 返回值说明: 成功修改返回 1，旧密码已变化或账号不存在返回 0
	 */
	int changePassword(@Param("id") int id, @Param("previousHash") String previousHash,
					   @Param("newHash") String newHash);
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 按主键物理删除调度控制台用户。
	 * @param id 控制台用户主键
	 * @return
	 * 返回值: int
	 * 返回值说明: 删除行数
	 */
	public int delete(@Param("id") int id);

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 更新控制台账号当前的登录令牌记录，供会话校验使用。
	 * @param id 控制台用户主键
	 * @param token 要保存的当前令牌
	 * @return
	 * 返回值: int
	 * 返回值说明: 受影响的记录数
	 */
	public int updateToken(@Param("id") int id, @Param("token") String token);

}
