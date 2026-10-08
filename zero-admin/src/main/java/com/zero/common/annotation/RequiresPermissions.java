package com.zero.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Permission check annotation.
 *
 * @author zero
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface RequiresPermissions
{
    /**
     * Required permission codes.
     */
    String[] value() default {};

    /**
     * Permission verification mode.
     */
    Logical logical() default Logical.AND;
}
