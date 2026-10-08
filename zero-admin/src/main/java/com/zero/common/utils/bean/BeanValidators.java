package com.zero.common.utils.bean;

import java.util.Set;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: bean对象属性验证
 */
public class BeanValidators
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 校验With异常
     * @param validator validator参数
     * @param object 对象参数
     * @param groups groups参数
     * @return
     * 返回值: void
     * 返回值说明：校验With异常
     */
    public static void validateWithException(Validator validator, Object object, Class<?>... groups)
            throws ConstraintViolationException
    {
        Set<ConstraintViolation<Object>> constraintViolations = validator.validate(object, groups);
        if (!constraintViolations.isEmpty())
        {
            throw new ConstraintViolationException(constraintViolations);
        }
    }
}
