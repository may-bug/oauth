package org.codelin.oauth.oauth.common.config;

import com.mybatisflex.core.keygen.IKeyGenerator;
import com.mybatisflex.core.keygen.KeyGeneratorFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.net.NetworkInterface;

/**
 * MyBatis-Flex 配置
 */
@Slf4j
@Configuration
public class MyBatisFlexConfig {

    /**
     * 在BeanFactory初始化阶段注册雪花ID生成器（早于Mapper初始化）
     */
    @Bean
    static BeanFactoryPostProcessor snowflakeKeyGeneratorRegistrar() {
        return beanFactory -> {
            long workerId = resolveWorkerId();
            IKeyGenerator generator = new SnowflakeKeyGenerator(workerId, 1);
            KeyGeneratorFactory.register("snowflake", generator);
            log.info("Snowflake key generator registered: workerId={}", workerId);
        };
    }

    private static final long MAX_WORKER_ID = ~(-1L << 5L); // 31

    private static long resolveWorkerId() {
        try {
            InetAddress ip = InetAddress.getLocalHost();
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);
            if (network != null) {
                byte[] mac = network.getHardwareAddress();
                if (mac != null) {
                    long id = ((0x000000FF & (long) mac[mac.length - 2])
                            | (0x0000FF00 & (((long) mac[mac.length - 1]) << 8))) >> 6;
                    return id % (MAX_WORKER_ID + 1);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to resolve worker ID from MAC, using random", e);
        }
        return 1L;
    }

    /**
     * 雪花ID生成器实现
     */
    static class SnowflakeKeyGenerator implements IKeyGenerator {

        private static final long EPOCH = 1704067200000L; // 2024-01-01
        private static final long WORKER_ID_BITS = 5L;
        private static final long DATACENTER_ID_BITS = 5L;
        private static final long SEQUENCE_BITS = 5L;
        private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
        private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS);
        private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;
        private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

        private final long workerId;
        private final long datacenterId;
        private long sequence = 0L;
        private long lastTimestamp = -1L;

        SnowflakeKeyGenerator(long workerId, long datacenterId) {
            if (workerId > MAX_WORKER_ID || workerId < 0) {
                throw new IllegalArgumentException("Worker ID must be between 0 and " + MAX_WORKER_ID);
            }
            if (datacenterId > MAX_DATACENTER_ID || datacenterId < 0) {
                throw new IllegalArgumentException("Datacenter ID must be between 0 and " + MAX_DATACENTER_ID);
            }
            this.workerId = workerId;
            this.datacenterId = datacenterId;
        }

        @Override
        public Object generate(Object entity, String keyColumn) {
            return nextId();
        }

        public synchronized long nextId() {
            long timestamp = System.currentTimeMillis();
            if (timestamp < lastTimestamp) {
                throw new RuntimeException("Clock moved backwards by " + (lastTimestamp - timestamp) + "ms");
            }
            if (timestamp == lastTimestamp) {
                sequence = (sequence + 1) & SEQUENCE_MASK;
                if (sequence == 0) {
                    while (timestamp <= lastTimestamp) {
                        timestamp = System.currentTimeMillis();
                    }
                }
            } else {
                sequence = 0L;
            }
            lastTimestamp = timestamp;
            return ((timestamp - EPOCH) << TIMESTAMP_SHIFT)
                    | (datacenterId << (SEQUENCE_BITS + WORKER_ID_BITS))
                    | (workerId << SEQUENCE_BITS)
                    | sequence;
        }
    }
}
