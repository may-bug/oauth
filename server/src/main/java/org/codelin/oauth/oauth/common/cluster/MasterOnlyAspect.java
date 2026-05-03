package org.codelin.oauth.oauth.common.cluster;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Master节点定时任务切面
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.cluster.enabled", havingValue = "true")
public class MasterOnlyAspect {

    private final ClusterManager clusterManager;

    @Around("@annotation(org.codelin.oauth.oauth.common.cluster.MasterOnly)")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!clusterManager.isMaster()) {
            log.debug("Skipping task {} on non-master node", joinPoint.getSignature().toShortString());
            return null;
        }

        log.debug("Executing task {} on master node", joinPoint.getSignature().toShortString());
        return joinPoint.proceed();
    }
}
