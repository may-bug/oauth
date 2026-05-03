package org.codelin.oauth.oauth.common.cluster;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.cluster.enabled", havingValue = "true")
public class ClusterManager {

    private final RedissonClient redissonClient;

    @Value("${app.cluster.node-id:#{T(java.util.UUID).randomUUID().toString()}}")
    private String nodeId;

    @Value("${app.cluster.master-key:cluster:master}")
    private String masterKey;

    @Value("${app.cluster.lease-seconds:30}")
    private int leaseSeconds;

    private volatile boolean isMaster = false;
    private volatile String masterNodeId;

    public ClusterManager(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
        log.info("Cluster node initialized: {}", nodeId);
    }

    @Scheduled(fixedRate = 10000)
    public void tryBecomeMaster() {
        try {
            RBucket<String> bucket = redissonClient.getBucket(masterKey);
            boolean success = bucket.setIfAbsent(nodeId, Duration.ofSeconds(leaseSeconds));

            if (success) {
                if (!isMaster) log.info("Node {} became master", nodeId);
                isMaster = true;
                masterNodeId = nodeId;
            } else {
                String currentMaster = bucket.get();
                if (nodeId.equals(currentMaster)) {
                    bucket.expire(Duration.ofSeconds(leaseSeconds));
                } else {
                    if (isMaster) log.info("Node {} lost master role", nodeId);
                    isMaster = false;
                    masterNodeId = currentMaster;
                }
            }
        } catch (Exception e) {
            log.error("Failed to perform master election", e);
        }
    }

    public boolean isMaster() { return isMaster; }
    public String getMasterNodeId() { return masterNodeId; }
    public String getNodeId() { return nodeId; }

    public void releaseMaster() {
        try {
            RBucket<String> bucket = redissonClient.getBucket(masterKey);
            if (nodeId.equals(bucket.get())) {
                bucket.delete();
                isMaster = false;
                masterNodeId = null;
                log.info("Node {} released master role", nodeId);
            }
        } catch (Exception e) {
            log.error("Failed to release master role", e);
        }
    }
}
