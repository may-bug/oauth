package org.codelin.oauth.oauth.admin.audit;

import java.lang.annotation.*;

/**
 * 审计日志注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Auditable {

    /**
     * 操作类型
     */
    String action();

    /**
     * 资源类型
     */
    String resourceType() default "";

    /**
     * 资源ID的SpEL表达式，例如: #id, #user.id
     */
    String resourceId() default "";

    /**
     * 操作描述
     */
    String description() default "";

    /**
     * 是否记录请求参数
     */
    boolean logParams() default true;

    /**
     * 是否记录返回结果
     */
    boolean logResult() default false;
}
