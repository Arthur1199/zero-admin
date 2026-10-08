package com.zero.common.annotation;

/**
 * Permission verification mode.
 *
 * @author zero
 */
public enum Logical
{
    /**
     * All permissions must match.
     */
    AND,

    /**
     * Any one permission can match.
     */
    OR
}
