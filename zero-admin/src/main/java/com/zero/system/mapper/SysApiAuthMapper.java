package com.zero.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.zero.system.domain.SysApiAuth;

/**
 * 作者: Arthur 亚瑟
 * 时间: 2026/6/27 03:49
 * 说明: API谷歌验证配置数据层，负责sys_api_auth表的增删改查和请求方式路径唯一性查询
 *
 */
public interface SysApiAuthMapper
{
    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/27 03:49
     * 说明: 根据配置ID查询API谷歌验证配置详情
     * @param id API谷歌验证配置ID
     * @return
     * 返回值: SysApiAuth
     * 返回值说明: API谷歌验证配置，不存在时返回null，已删除记录仍返回用于后台展示状态
     */
    public SysApiAuth selectApiAuthById(Long id);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/27 05:36
     * 说明: 根据请求方式和接口路径查询未删除配置，用于新增和修改时避免同一接口动作重复配置
     * @param apiMethod API请求方式，ALL表示所有请求方式
     * @param apiUrl API匹配路径，建议保存Spring MVC匹配模板
     * @return
     * 返回值: SysApiAuth
     * 返回值说明: 未删除的API谷歌验证配置，不存在时返回null
     */
    public SysApiAuth selectApiAuthByMethodAndUrl(@Param("apiMethod") String apiMethod, @Param("apiUrl") String apiUrl);

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/27 03:49
     * 说明: 查询API谷歌验证配置列表，支持权限标识、请求路径、谷歌验证码、状态和创建时间范围筛选
     * @param apiAuth 查询条件
     * @return
     * 返回值: List<SysApiAuth>
     * 返回值说明: API谷歌验证配置集合，包含正常、禁用和删除状态记录
     */
    public List<SysApiAuth> selectApiAuthList(SysApiAuth apiAuth);

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/27 04:49
     * 说明: 查询当前启用且需要接口谷歌二次验证的API地址，用于前端弹窗匹配和后端拦截器安全校验
     * @return
     * 返回值: List<SysApiAuth>
     * 返回值说明: API配置集合，仅包含api_status=1且is_need_google=1的配置
     */
    public List<SysApiAuth> selectGoogleAuthApis();

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/27 03:49
     * 说明: 新增API谷歌验证配置
     * @param apiAuth API谷歌验证配置信息
     * @return
     * 返回值: int
     * 返回值说明: 影响行数
     */
    public int insertApiAuth(SysApiAuth apiAuth);

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/27 03:49
     * 说明: 修改API谷歌验证配置
     * @param apiAuth API谷歌验证配置信息
     * @return
     * 返回值: int
     * 返回值说明: 影响行数
     */
    public int updateApiAuth(SysApiAuth apiAuth);

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/27 03:49
     * 说明: 批量软删除API谷歌验证配置，删除后仍可在列表筛选展示，但不再参与后续谷歌验证匹配
     * @param ids API谷歌验证配置ID集合
     * @param updateBy 删除操作人
     * @return
     * 返回值: int
     * 返回值说明: 影响行数
     */
    public int deleteApiAuthByIds(@Param("ids") Long[] ids, @Param("updateBy") String updateBy);
}
