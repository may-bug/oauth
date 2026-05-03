package org.codelin.oauth.oauth.identity.application;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codelin.oauth.oauth.common.cache.CacheStore;
import org.codelin.oauth.oauth.identity.domain.model.CryptoConfig;
import org.codelin.oauth.oauth.identity.domain.model.CryptoEndpointConfig;
import org.codelin.oauth.oauth.identity.infrastructure.persistence.CryptoConfigMapper;
import org.codelin.oauth.oauth.identity.infrastructure.persistence.CryptoEndpointConfigMapper;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 加密配置服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CryptoConfigService {

    private final CryptoConfigMapper cryptoConfigMapper;
    private final CryptoEndpointConfigMapper endpointConfigMapper;
    private final CryptoService cryptoService;
    private final ObjectMapper objectMapper;
    private final CacheStore cacheStore;

    private static final String CACHE_KEY_CONFIG = "crypto:config";
    private static final String CACHE_KEY_ENDPOINTS = "crypto:endpoints";
    private static final Duration CACHE_TTL = Duration.ofMinutes(10);

    /**
     * 获取加密配置（带缓存）
     */
    public CryptoConfig getConfig() {
        CryptoConfig cached = cacheStore.get(CACHE_KEY_CONFIG);
        if (cached != null) {
            return cached;
        }
        List<CryptoConfig> list = cryptoConfigMapper.selectAll();
        CryptoConfig config = (list != null && !list.isEmpty()) ? list.getFirst() : getDefaultConfig();
        cacheStore.set(CACHE_KEY_CONFIG, config, CACHE_TTL);
        return config;
    }

    /**
     * 检查加密是否启用
     */
    public boolean isEnabled() {
        CryptoConfig config = getConfig();
        return Boolean.TRUE.equals(config.getEnabled());
    }

    /**
     * 获取所有端点配置（带缓存）
     */
    public List<CryptoEndpointConfig> getEndpointConfigs() {
        List<CryptoEndpointConfig> cached = cacheStore.get(CACHE_KEY_ENDPOINTS);
        if (cached != null) {
            return cached;
        }
        List<CryptoEndpointConfig> configs = endpointConfigMapper.selectAll();
        cacheStore.set(CACHE_KEY_ENDPOINTS, configs, CACHE_TTL);
        return configs;
    }

    /**
     * 检查指定端点是否需要加密
     */
    public boolean requiresEncryption(String method, String endpoint) {
        if (!isEnabled()) {
            return false;
        }

        String fullEndpoint = method + ":" + endpoint;
        List<CryptoEndpointConfig> configs = getEndpointConfigs();
        for (CryptoEndpointConfig ep : configs) {
            if (fullEndpoint.equals(ep.getEndpoint())
                    && Boolean.TRUE.equals(ep.getEnabled())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取端点需要加密的字段
     */
    public List<String> getEncryptedFields(String method, String endpoint) {
        String fullEndpoint = method + ":" + endpoint;
        List<CryptoEndpointConfig> configs = getEndpointConfigs();
        CryptoEndpointConfig match = configs.stream()
                .filter(ep -> fullEndpoint.equals(ep.getEndpoint())
                        && Boolean.TRUE.equals(ep.getEnabled()))
                .findFirst().orElse(null);

        if (match == null || match.getEncryptedFields() == null) {
            return Collections.emptyList();
        }

        try {
            return objectMapper.readValue(match.getEncryptedFields(),
                    new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.error("Failed to parse encrypted fields: {}", match.getEncryptedFields(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取RSA公钥
     */
    public String getPublicKey() {
        CryptoConfig config = getConfig();
        return config.getRsaPublicKey();
    }

    /**
     * 获取RSA私钥
     */
    public String getPrivateKey() {
        CryptoConfig config = getConfig();
        return config.getRsaPrivateKey();
    }

    /**
     * 初始化或轮换密钥对
     */
    public void rotateKeys() {
        CryptoConfig config = getConfig();
        int keySize = config.getRsaKeySize() != null ? config.getRsaKeySize() : 2048;

        Map<String, String> keyPair = cryptoService.generateKeyPair(keySize);

        config.setRsaPublicKey(keyPair.get("publicKey"));
        config.setRsaPrivateKey(keyPair.get("privateKey"));
        config.setUpdatedAt(LocalDateTime.now());

        if (config.getId() != null) {
            cryptoConfigMapper.update(config);
        } else {
            cryptoConfigMapper.insert(config);
        }

        log.info("RSA key pair rotated, key size: {}", keySize);
        cacheStore.delete(CACHE_KEY_CONFIG);
    }

    /**
     * 更新加密配置
     */
    public void updateConfig(CryptoConfig config) {
        List<CryptoConfig> list = cryptoConfigMapper.selectAll();
        CryptoConfig existing = (list != null && !list.isEmpty()) ? list.getFirst() : null;
        if (existing != null && existing.getId() != null) {
            config.setId(existing.getId());
            config.setRsaPublicKey(existing.getRsaPublicKey());
            config.setRsaPrivateKey(existing.getRsaPrivateKey());
            cryptoConfigMapper.update(config);
        } else {
            cryptoConfigMapper.insert(config);
        }
        cacheStore.delete(CACHE_KEY_CONFIG);
    }

    /**
     * 更新端点加密配置
     */
    public void updateEndpointConfig(CryptoEndpointConfig config) {
        CryptoEndpointConfig existing = endpointConfigMapper.selectOneByQuery(
                com.mybatisflex.core.query.QueryWrapper.create()
                        .eq("endpoint", config.getEndpoint())
        );
        if (existing != null) {
            config.setId(existing.getId());
            endpointConfigMapper.update(config);
        } else {
            endpointConfigMapper.insert(config);
        }
        cacheStore.delete(CACHE_KEY_ENDPOINTS);
    }

    /**
     * 删除端点配置
     */
    public void deleteEndpointConfig(Long id) {
        endpointConfigMapper.deleteById(id);
        cacheStore.delete(CACHE_KEY_ENDPOINTS);
    }

    /**
     * 确保密钥存在，返回公钥
     */
    public String ensureKeysExist() {
        String key = getPublicKey();
        if (key == null || key.isEmpty()) {
            rotateKeys();
            key = getPublicKey();
        }
        return key;
    }

    /**
     * 默认配置
     */
    private CryptoConfig getDefaultConfig() {
        CryptoConfig config = new CryptoConfig();
        config.setEnabled(true);
        config.setRsaKeySize(2048);
        config.setKeyExpireDays(90);
        return config;
    }
}
