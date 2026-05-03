package org.codelin.oauth.oauth.common.cluster;

import java.lang.annotation.*;

/**
 * 标记定时任务只在Master节点执行
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MasterOnly {
}
