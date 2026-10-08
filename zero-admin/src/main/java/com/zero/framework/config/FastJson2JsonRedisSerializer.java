package com.zero.framework.config;

import java.nio.charset.Charset;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.filter.Filter;
import com.zero.common.constant.Constants;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: Redis使用FastJson序列化
 */
public class FastJson2JsonRedisSerializer<T> implements RedisSerializer<T>
{
    /**
     * DEFAULT_CHARSET字段
     */
    public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    static final Filter AUTO_TYPE_FILTER = JSONReader.autoTypeFilter(Constants.JSON_WHITELIST_STR);
    /**
     * clazz字段
     */

    private Class<T> clazz;
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: FastJson2JSONRedisSerializer
     * @param clazz clazz参数
     */

    public FastJson2JsonRedisSerializer(Class<T> clazz)
    {
        super();
        this.clazz = clazz;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: serialize
     * @param t t参数
     * @return
     * 返回值: byte[]
     * 返回值说明：serialize
     */

    @Override
    public byte[] serialize(T t) throws SerializationException
    {
        if (t == null)
        {
            return new byte[0];
        }
        return JSON.toJSONString(t, JSONWriter.Feature.WriteClassName).getBytes(DEFAULT_CHARSET);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: deserialize
     * @param bytes bytes参数
     * @return
     * 返回值: T
     * 返回值说明：deserialize
     */

    @Override
    public T deserialize(byte[] bytes) throws SerializationException
    {
        if (bytes == null || bytes.length <= 0)
        {
            return null;
        }
        String str = new String(bytes, DEFAULT_CHARSET);

        return JSON.parseObject(str, clazz, AUTO_TYPE_FILTER);
    }
}
