package com.zero.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.zero.common.constant.ApiAuthConstants;
import com.zero.common.exception.ServiceException;
import com.zero.common.utils.StringUtils;
import com.zero.system.domain.SysApiAuth;
import com.zero.system.mapper.SysApiAuthMapper;
import com.zero.system.service.AdminUserRefreshService;
import com.zero.system.service.ISysApiAuthService;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/6/27 03:49
 * 说明: API谷歌验证配置服务层实现，负责配置校验、软删除及修改提交后的在线客户端刷新
 *
 */
@Service
public class SysApiAuthServiceImpl implements ISysApiAuthService
{
    /**
     * API谷歌验证配置数据访问，读取管理列表和后续谷歌验证匹配所需的配置记录
     */
    @Autowired
    private SysApiAuthMapper apiAuthMapper;

    /** 配置修改提交后广播刷新，复用现有多节点在线通信。 */
    @Autowired
    private AdminUserRefreshService clientRefreshService;

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/27 03:49
     * 说明: 查询API谷歌验证配置详情
     * @param id API谷歌验证配置ID
     * @return
     * 返回值: SysApiAuth
     * 返回值说明: API谷歌验证配置，不存在时返回null，已删除记录仍返回用于后台展示状态
     */
    @Override
    public SysApiAuth selectApiAuthById(Long id)
    {
        return apiAuthMapper.selectApiAuthById(id);
    }

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/27 03:49
     * 说明: 查询API谷歌验证配置列表
     * @param apiAuth 查询条件
     * @return
     * 返回值: List<SysApiAuth>
     * 返回值说明: API谷歌验证配置集合，包含正常、禁用和删除状态记录
     */
    @Override
    public List<SysApiAuth> selectApiAuthList(SysApiAuth apiAuth)
    {
        return apiAuthMapper.selectApiAuthList(apiAuth);
    }

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/27 04:49
     * 说明: 查询启用状态下需要接口谷歌二次验证的API地址，统一供前端和拦截器使用
     * @return
     * 返回值: List<SysApiAuth>
     * 返回值说明: API规则集合；没有配置时返回空集合
     */
    @Override
    public List<SysApiAuth> selectGoogleAuthApis()
    {
        return apiAuthMapper.selectGoogleAuthApis();
    }

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/27 03:49
     * 说明: 新增API谷歌验证配置，写入前统一清理输入、填充默认值并校验同一请求方式和接口路径唯一
     * @param apiAuth API谷歌验证配置信息
     * @return
     * 返回值: int
     * 返回值说明: 影响行数
     */
    @Override
    public int insertApiAuth(SysApiAuth apiAuth)
    {
        prepareApiAuth(apiAuth, false);
        checkApiMethodUrlUnique(apiAuth);
        return apiAuthMapper.insertApiAuth(apiAuth);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/27 03:49
     * 说明: 修改API谷歌验证配置，允许编辑恢复状态；提交成功后广播刷新，使在线客户端重新加载规则
     * @param apiAuth API谷歌验证配置信息
     * @return
     * 返回值: int
     * 返回值说明: 影响行数；成功提交后广播刷新，不改变原配置校验和返回规则
     */
    @Override
    @Transactional
    public int updateApiAuth(SysApiAuth apiAuth)
    {
        if (apiAuth == null || StringUtils.isNull(apiAuth.getId()))
        {
            throw new ServiceException("API谷歌验证配置ID不能为空");
        }
        SysApiAuth oldApiAuth = apiAuthMapper.selectApiAuthById(apiAuth.getId());
        if (StringUtils.isNull(oldApiAuth))
        {
            throw new ServiceException("API谷歌验证配置不存在");
        }
        prepareApiAuth(apiAuth, true);
        checkApiMethodUrlUnique(apiAuth);
        int rows = apiAuthMapper.updateApiAuth(apiAuth);
        if (rows > 0)
        {
            clientRefreshService.afterApiAuthChanged();
        }
        return rows;
    }

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/27 03:49
     * 说明: 批量软删除API谷歌验证配置，保留历史审计信息并让配置不再参与列表和谷歌验证匹配
     * @param ids API谷歌验证配置ID集合
     * @param updateBy 删除操作人
     * @return
     * 返回值: int
     * 返回值说明: 影响行数
     */
    @Override
    public int deleteApiAuthByIds(Long[] ids, String updateBy)
    {
        if (StringUtils.isEmpty(ids))
        {
            throw new ServiceException("请选择要删除的API谷歌验证配置");
        }
        return apiAuthMapper.deleteApiAuthByIds(ids, updateBy);
    }

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/27 03:49
     * 说明: 规范化API谷歌验证配置输入，避免空白标识、非法状态和非法谷歌验证码开关进入数据库
     * @param apiAuth API谷歌验证配置信息
     * @param update 是否为修改场景，修改场景不允许传入删除状态
     * @return
     * 返回值: void
     * 返回值说明: 原对象会被补齐默认状态、默认谷歌验证码开关和清理后的字符串
     */
    private void prepareApiAuth(SysApiAuth apiAuth, boolean update)
    {
        apiAuth.setApiPermission(StringUtils.trim(apiAuth.getApiPermission()));
        apiAuth.setApiUrl(StringUtils.trim(apiAuth.getApiUrl()));
        apiAuth.setApiMethod(StringUtils.trim(apiAuth.getApiMethod()));
        if (StringUtils.isEmpty(apiAuth.getApiMethod()))
        {
            apiAuth.setApiMethod(ApiAuthConstants.METHOD_ALL);
        }
        else
        {
            apiAuth.setApiMethod(apiAuth.getApiMethod().toUpperCase());
        }
        if (StringUtils.isEmpty(apiAuth.getApiDesc()))
        {
            apiAuth.setApiDesc(StringUtils.EMPTY);
        }
        else
        {
            apiAuth.setApiDesc(StringUtils.trim(apiAuth.getApiDesc()));
        }
        if (StringUtils.isNull(apiAuth.getApiStatus()))
        {
            apiAuth.setApiStatus(ApiAuthConstants.STATUS_ENABLED);
        }
        if (StringUtils.isNull(apiAuth.getIsNeedGoogle()))
        {
            apiAuth.setIsNeedGoogle(ApiAuthConstants.NEED_GOOGLE_NO);
        }
        if (!ApiAuthConstants.STATUS_ENABLED.equals(apiAuth.getApiStatus())
                && !ApiAuthConstants.STATUS_DISABLED.equals(apiAuth.getApiStatus()))
        {
            throw new ServiceException(update ? "API状态只能为正常或禁用" : "API状态不正确");
        }
        if (!ApiAuthConstants.NEED_GOOGLE_YES.equals(apiAuth.getIsNeedGoogle())
                && !ApiAuthConstants.NEED_GOOGLE_NO.equals(apiAuth.getIsNeedGoogle()))
        {
            throw new ServiceException("是否需要谷歌验证码取值不正确");
        }
        if (!isAllowedApiMethod(apiAuth.getApiMethod()))
        {
            throw new ServiceException("API请求方式不正确");
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/27 05:36
     * 说明: 校验同一请求方式和接口路径在未删除配置中唯一，避免无权限注解接口按路径匹配时命中多条规则
     * @param apiAuth API谷歌验证配置信息
     * @return
     * 返回值: void
     * 返回值说明: 重复时抛出ServiceException
     */
    private void checkApiMethodUrlUnique(SysApiAuth apiAuth)
    {
        Long id = StringUtils.isNull(apiAuth.getId()) ? -1L : apiAuth.getId();
        SysApiAuth info = apiAuthMapper.selectApiAuthByMethodAndUrl(apiAuth.getApiMethod(), apiAuth.getApiUrl());
        if (StringUtils.isNotNull(info) && info.getId().longValue() != id.longValue())
        {
            throw new ServiceException("API请求方式和请求路径已存在");
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/27 05:36
     * 说明: 校验后台允许配置的请求方式，避免非法METHOD导致前后端拦截规则不一致
     * @param apiMethod API请求方式
     * @return
     * 返回值: boolean
     * 返回值说明: true-合法请求方式，false-非法请求方式
     */
    private boolean isAllowedApiMethod(String apiMethod)
    {
        return ApiAuthConstants.METHOD_ALL.equals(apiMethod)
                || ApiAuthConstants.METHOD_GET.equals(apiMethod)
                || ApiAuthConstants.METHOD_POST.equals(apiMethod)
                || ApiAuthConstants.METHOD_PUT.equals(apiMethod)
                || ApiAuthConstants.METHOD_DELETE.equals(apiMethod)
                || ApiAuthConstants.METHOD_PATCH.equals(apiMethod);
    }
}
