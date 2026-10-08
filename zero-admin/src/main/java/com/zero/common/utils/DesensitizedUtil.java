package com.zero.common.utils;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 脱敏工具类
 */
public class DesensitizedUtil
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 密码的全部字符都用*代替，比如：******
     * @param password 密码
     * @return
     * 返回值: 脱敏后的密码
     * 返回值说明：密码的全部字符都用*代替，比如：******
     */
    public static String password(String password)
    {
        if (StringUtils.isBlank(password))
        {
            return StringUtils.EMPTY;
        }
        return StringUtils.repeat('*', password.length());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 车牌中间用*代替，如果是错误的车牌，不处理
     * @param carLicense 完整的车牌号
     * @return
     * 返回值: 脱敏后的车牌
     * 返回值说明：车牌中间用*代替，如果是错误的车牌，不处理
     */
    public static String carLicense(String carLicense)
    {
        if (StringUtils.isBlank(carLicense))
        {
            return StringUtils.EMPTY;
        }
        // 普通车牌
        if (carLicense.length() == 7)
        {
            carLicense = StringUtils.hide(carLicense, 3, 6);
        }
        else if (carLicense.length() == 8)
        {
            // 新能源车牌
            carLicense = StringUtils.hide(carLicense, 3, 7);
        }
        return carLicense;
    }
}
