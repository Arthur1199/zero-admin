package com.zero.common.config.serializer;

import java.io.IOException;
import java.util.Objects;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.zero.common.annotation.Sensitive;
import com.zero.common.core.domain.model.LoginUser;
import com.zero.common.enums.DesensitizedType;
import com.zero.common.utils.SecurityUtils;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 数据脱敏序列化过滤
 */
public class SensitiveJsonSerializer extends JsonSerializer<String> implements ContextualSerializer
{
    /**
     * desensitized类型字段
     */
    private DesensitizedType desensitizedType;
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: serialize
     * @param value 值参数
     * @param gen 代码生成参数
     * @param serializers serializers参数
     * @return
     * 返回值: void
     * 返回值说明：serialize
     */

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException
    {
        if (desensitization())
        {
            gen.writeString(desensitizedType.desensitizer().apply(value));
        }
        else
        {
            gen.writeString(value);
        }
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 创建Contextual
     * @param prov prov参数
     * @param property property参数
     * @return
     * 返回值: JsonSerializer<>
     * 返回值说明：创建Contextual
     */

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property)
            throws JsonMappingException
    {
        Sensitive annotation = property.getAnnotation(Sensitive.class);
        if (Objects.nonNull(annotation) && Objects.equals(String.class, property.getType().getRawClass()))
        {
            this.desensitizedType = annotation.desensitizedType();
            return this;
        }
        return prov.findValueSerializer(property.getType(), property);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 是否需要脱敏处理
     * @return
     * 返回值: boolean
     * 返回值说明：是否需要脱敏处理
     */
    private boolean desensitization()
    {
        try
        {
            LoginUser securityUser = SecurityUtils.getLoginUser();
            // 管理员不脱敏
            return !securityUser.getUser().isAdmin();
        }
        catch (Exception e)
        {
            return true;
        }
    }
}
