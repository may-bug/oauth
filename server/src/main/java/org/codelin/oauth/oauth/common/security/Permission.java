package org.codelin.oauth.oauth.common.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 需要权限
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Permission {

    /**
     * 权限编码
     */
    String[] value();

    /**
     * 逻辑关系
     */
    Logical logical() default Logical.AND;

    enum Logical {
        AND, OR
    }
}
