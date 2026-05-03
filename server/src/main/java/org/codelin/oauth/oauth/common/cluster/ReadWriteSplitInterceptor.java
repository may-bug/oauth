package org.codelin.oauth.oauth.common.cluster;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 读写分离拦截器（MyBatis插件）
 * <p>
 * 注意：实际的读写分离需要配置多数据源，这里只是标记读写操作
 */
@Slf4j
@Component
@Intercepts({
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class})
})
@ConditionalOnProperty(name = "app.cluster.enabled", havingValue = "true")
public class ReadWriteSplitInterceptor implements Interceptor {

    private static final ThreadLocal<Boolean> IS_READ = new ThreadLocal<>();

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement ms = (MappedStatement) invocation.getArgs()[0];

        boolean isRead = ms.getSqlCommandType() == SqlCommandType.SELECT;
        IS_READ.set(isRead);

        try {
            if (isRead) {
                // 读操作 - 可以路由到从库
                log.debug("Read operation: {}", ms.getId());
            } else {
                // 写操作 - 必须路由到主库
                log.debug("Write operation: {}", ms.getId());
            }
            return invocation.proceed();
        } finally {
            IS_READ.remove();
        }
    }

    /**
     * 当前操作是否是读操作
     */
    public static boolean isReadOperation() {
        Boolean isRead = IS_READ.get();
        return isRead != null && isRead;
    }
}
