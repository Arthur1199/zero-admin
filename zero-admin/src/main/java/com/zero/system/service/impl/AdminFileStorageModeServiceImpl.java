package com.zero.system.service.impl;

import com.zero.common.utils.StringUtils;
import com.zero.system.service.IAdminFileStorageModeService;
import com.zero.system.service.ISysConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 从管理后台系统参数读取通用文件及头像的上传方式。
 */
@Service
public class AdminFileStorageModeServiceImpl implements IAdminFileStorageModeService
{
    @Autowired
    private ISysConfigService configService;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 只有配置为 local 时使用本地存储，缺少配置或其他取值时交给文件服务。
     * @return
     * 返回值: boolean
     * 返回值说明: 是否使用独立文件服务
     */
    @Override
    public boolean useZeroFile()
    {
        String value = configService.selectConfigByKey(CONFIG_KEY);
        if (StringUtils.isEmpty(value))
        {
            return true;
        }
        return !MODE_LOCAL.equalsIgnoreCase(value.trim());
    }
}
