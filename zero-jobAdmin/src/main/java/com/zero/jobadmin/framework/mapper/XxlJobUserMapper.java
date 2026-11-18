package com.zero.jobadmin.framework.mapper;

import com.zero.jobadmin.framework.model.XxlJobUser;
import com.xxl.tool.response.Response;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * @author xuxueli 2019-05-04 16:44:59
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
	public int pageListCount(@Param("offset") int offset,
							 @Param("pagesize") int pagesize,
							 @Param("username") String username,
							 @Param("role") int role);

	public XxlJobUser loadByUserName(@Param("username") String username);

	public XxlJobUser loadById(@Param("id") int id);

	public int save(XxlJobUser xxlJobUser);

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
	
	public int delete(@Param("id") int id);

	public int updateToken(@Param("id") int id, @Param("token") String token);

}
