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
	
	public int delete(@Param("id") int id);

	public int updateToken(@Param("id") int id, @Param("token") String token);

}
