package com.zero.system.service;

import java.util.List;
import com.zero.system.domain.SysApiAuth;

/**
 * 作者: Arthur 亚瑟
 * 时间: 2026/6/27 03:49
 * 说明: API谷歌验证配置服务层，封装配置管理、请求方式路径唯一性校验、状态校验和软删除处理
 *
 */
public interface ISysApiAuthService
{
    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/27 03:49
     * 说明: 查询API谷歌验证配置详情
     * @param id API谷歌验证配置ID
     * @return
     * 返回值: SysApiAuth
     * 返回值说明: API谷歌验证配置，不存在时返回null，已删除记录仍返回用于后台展示状态
     */
    public SysApiAuth selectApiAuthById(Long id);

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/27 03:49
     * 说明: 查询API谷歌验证配置列表
     * @param apiAuth 查询条件
     * @return
     * 返回值: List<SysApiAuth>
     * 返回值说明: API谷歌验证配置集合，包含正常、禁用和删除状态记录
     */
    public List<SysApiAuth> selectApiAuthList(SysApiAuth apiAuth);

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/27 04:49
     * 说明: 查询需要接口谷歌二次验证的API地址，供前端缓存匹配和后端拦截器兜底校验使用
     * @return
     * 返回值: List<SysApiAuth>
     * 返回值说明: 启用状态且开启谷歌验证的API规则集合，包含权限标识、请求方式和匹配路径
     */
    public List<SysApiAuth> selectGoogleAuthApis();

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/27 03:49
     * 说明: 新增API谷歌验证配置，写入前校验请求方式和接口路径唯一、状态取值和谷歌验证码开关
     * @param apiAuth API谷歌验证配置信息
     * @return
     * 返回值: int
     * 返回值说明: 影响行数
     */
    public int insertApiAuth(SysApiAuth apiAuth);

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/27 03:49
     * 说明: 修改API谷歌验证配置，写入前校验记录存在、请求方式和接口路径唯一、状态取值和谷歌验证码开关
     * @param apiAuth API谷歌验证配置信息
     * @return
     * 返回值: int
     * 返回值说明: 影响行数
     */
    public int updateApiAuth(SysApiAuth apiAuth);

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/27 03:49
     * 说明: 批量软删除API谷歌验证配置
     * @param ids API谷歌验证配置ID集合
     * @param updateBy 删除操作人
     * @return
     * 返回值: int
     * 返回值说明: 影响行数
     */
    public int deleteApiAuthByIds(Long[] ids, String updateBy);
}
