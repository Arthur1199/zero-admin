package com.zero.system.service.impl;

import com.zero.common.utils.StringUtils;
import com.zero.system.service.IAdminFileStorageModeService;
import com.zero.system.service.ISysConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminFileStorageModeServiceImpl implements IAdminFileStorageModeService
{
    @Autowired
    private ISysConfigService configService;

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
