package com.zero.common.utils.uuid;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: ID生成器工具类
 */
public class IdUtils
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取随机UUID
     * @return
     * 返回值: 随机UUID
     * 返回值说明：获取随机UUID
     */
    public static String randomUUID()
    {
        return UUID.randomUUID().toString();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 简化的UUID，去掉了横线
     * @return
     * 返回值: 简化的UUID，去掉了横线
     * 返回值说明：简化的UUID，去掉了横线
     */
    public static String simpleUUID()
    {
        return UUID.randomUUID().toString(true);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取随机UUID，使用性能更好的ThreadLocalRandom生成UUID
     * @return
     * 返回值: 随机UUID
     * 返回值说明：获取随机UUID，使用性能更好的ThreadLocalRandom生成UUID
     */
    public static String fastUUID()
    {
        return UUID.fastUUID().toString();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 简化的UUID，去掉了横线，使用性能更好的ThreadLocalRandom生成UUID
     * @return
     * 返回值: 简化的UUID，去掉了横线
     * 返回值说明：简化的UUID，去掉了横线，使用性能更好的ThreadLocalRandom生成UUID
     */
    public static String fastSimpleUUID()
    {
        return UUID.fastUUID().toString(true);
    }
}
