package com.zero.common.enums;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 用户状态
 */
public enum UserStatus
{
    OK("0", "正常"), DISABLE("1", "停用"), DELETED("2", "删除");
    /**
     * 编码字段
     */

    private final String code;
    /**
     * 信息字段
     */
    private final String info;

    UserStatus(String code, String info)
    {
        this.code = code;
        this.info = info;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取编码
     * @return
     * 返回值: String
     * 返回值说明：获取编码
     */

    public String getCode()
    {
        return code;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取信息
     * @return
     * 返回值: String
     * 返回值说明：获取信息
     */

    public String getInfo()
    {
        return info;
    }
}
