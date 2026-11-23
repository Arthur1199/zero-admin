package com.zero.common.config.serializer;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import com.zero.common.annotation.Sensitive;
import com.zero.common.core.domain.model.LoginUser;
import com.zero.common.enums.DesensitizedType;
import com.zero.common.utils.SecurityUtils;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/23 21:12
 * 说明: 根据当前登录身份和属性注解，在生成 JSON 时输出原值或脱敏值。
 */
public class SensitiveJsonSerializer extends ValueSerializer<String>
{
    /** 当前属性的脱敏规则，各属性独立绑定以避免并发访问共享可变状态。 */
    private final DesensitizedType desensitizedType;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/23 21:12
     * 说明: 供 Jackson 实例化注解关联的序列化器。
     * @return
     * 返回值: 无
     * 返回值说明: 创建尚未绑定具体属性的实例。
     */
    public SensitiveJsonSerializer()
    {
        this(null);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/23 21:12
     * 说明: 为当前属性创建独立序列化器。
     * @param desensitizedType 当前属性的脱敏规则
     * @return
     * 返回值: 无
     * 返回值说明: 规则在实例生命周期内保持不变。
     */
    private SensitiveJsonSerializer(DesensitizedType desensitizedType)
    {
        this.desensitizedType = desensitizedType;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/23 21:12
     * 说明: 按原有权限规则对受保护的字符串脱敏。
     * @param value 待输出的非空字符串
     * @param gen 当前响应的 JSON 生成器
     * @param context Jackson 3 序列化上下文
     * @return
     * 返回值: void
     * 返回值说明: 写入原值或脱敏值。
     */

    @Override
    public void serialize(String value, JsonGenerator gen, SerializationContext context)
    {
        if (desensitizedType != null && desensitization())
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
     * 时间: 2026/11/23 21:12
     * 说明: 从当前字符串属性读取脱敏规则，其余属性沿用默认序列化。
     * @param context Jackson 3 序列化上下文
     * @param property 当前属性，根值序列化时可为空
     * @return
     * 返回值: ValueSerializer<?>
     * 返回值说明: 当前属性专用的脱敏序列化器或默认序列化器。
     */

    @Override
    public ValueSerializer<?> createContextual(SerializationContext context, BeanProperty property)
    {
        if (property == null)
        {
            return context.findValueSerializer(String.class);
        }
        Sensitive annotation = property.getAnnotation(Sensitive.class);
        if (annotation != null && property.getType().hasRawClass(String.class))
        {
            return new SensitiveJsonSerializer(annotation.desensitizedType());
        }
        return context.findValueSerializer(property.getType());
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
