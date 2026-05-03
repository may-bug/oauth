package org.codelin.oauth.oauth.identity.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codelin.oauth.oauth.common.cache.CacheStore;
import org.codelin.oauth.oauth.common.web.BizException;
import org.codelin.oauth.oauth.identity.application.social.SocialProviderService;
import org.codelin.oauth.oauth.identity.domain.model.*;
import org.codelin.oauth.oauth.identity.infrastructure.persistence.AuthConfigMapper;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 认证方式配置服务（带缓存）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthConfigService {

    private final AuthConfigMapper mapper;
    private final CacheStore cacheStore;
    private final SocialProviderService socialProviderService;
    private final CaptchaConfigService captchaConfigService;
    private final CryptoConfigService cryptoConfigService;
    private final ObjectMapper objectMapper;

    private static final String CACHE_KEY = "auth:config";
    private static final String CLIENT_CONFIG_KEY = "auth:client-config";
    private static final Duration CACHE_TTL = Duration.ofMinutes(10);

    public List<AuthConfig> listAll() {
        List<AuthConfig> cached = cacheStore.get(CACHE_KEY);
        if (cached != null) return cached;
        List<AuthConfig> configs = mapper.selectAll();
        cacheStore.set(CACHE_KEY, configs, CACHE_TTL);
        return configs;
    }

    public boolean isEnabled(String authType) {
        return listAll().stream()
                .anyMatch(c -> authType.equals(c.getAuthType()) && Boolean.TRUE.equals(c.getEnabled()));
    }

    public void update(List<AuthConfig> list) {
        for (AuthConfig item : list) {
            if (item.getAuthType() == null || item.getAuthType().isEmpty())
                throw BizException.badRequest("error.bad-request");
            AuthConfig existing = getByType(item.getAuthType());
            if (existing != null) {
                existing.setEnabled(item.getEnabled());
                if (item.getDescription() != null) existing.setDescription(item.getDescription());
                existing.setUpdatedAt(LocalDateTime.now());
                mapper.update(existing);
            }
        }
        cacheStore.delete(CACHE_KEY);
        cacheStore.delete(CLIENT_CONFIG_KEY);
        log.info("Auth config updated: {}", list.size());
    }

    /**
     * 构建前端认证配置（Base64 编码的 JSON）
     */
    public String getClientConfig() {
        String cached = cacheStore.get(CLIENT_CONFIG_KEY);
        if (cached != null) return cached;

        Map<String, Object> config = new LinkedHashMap<>();
        config.put("passwordLogin", isEnabled("password"));
        config.put("emailLogin", isEnabled("email"));
        config.put("phoneLogin", isEnabled("phone"));

        // 社交登录
        List<Map<String, Object>> socialList = new ArrayList<>();
        for (SocialProviderConfig sp : socialProviderService.listAll()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("provider", sp.getProvider());
            item.put("name", sp.getDisplayName());
            item.put("enabled", Boolean.TRUE.equals(sp.getEnabled()));
            socialList.add(item);
        }
        config.put("socialLogin", socialList);

        // 验证码
        Map<String, Object> captcha = new LinkedHashMap<>();
        CaptchaConfig cc = captchaConfigService.getConfig();
        captcha.put("enabled", Boolean.TRUE.equals(cc.getEnabled()));
        captcha.put("type", cc.getCaptchaType());
        captcha.put("length", cc.getLength());
        captcha.put("expireSeconds", cc.getExpireSeconds());
        List<Map<String, Object>> ceList = new ArrayList<>();
        for (var ep : captchaConfigService.getEndpointConfigs()) {
            ceList.add(Map.of("endpoint", ep.getEndpoint(), "enabled", ep.getEnabled()));
        }
        captcha.put("endpoints", ceList);
        config.put("captcha", captcha);

        // 加密
        Map<String, Object> crypto = new LinkedHashMap<>();
        CryptoConfig crc = cryptoConfigService.getConfig();
        crypto.put("enabled", Boolean.TRUE.equals(crc.getEnabled()));
        crypto.put("keySize", crc.getRsaKeySize());
        crypto.put("publicKey", cryptoConfigService.ensureKeysExist());
        List<Map<String, Object>> crEndpoints = new ArrayList<>();
        for (var ep : cryptoConfigService.getEndpointConfigs()) {
            String[] parts = ep.getEndpoint().split(":");
            crEndpoints.add(Map.of(
                    "endpoint", ep.getEndpoint(),
                    "enabled", ep.getEnabled(),
                    "encryptedFields", cryptoConfigService.getEncryptedFields(parts[0], parts[1])));
        }
        crypto.put("endpoints", crEndpoints);
        config.put("crypto", crypto);

        try {
            String json = objectMapper.writeValueAsString(config);
            String encoded = Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
            cacheStore.set(CLIENT_CONFIG_KEY, encoded, CACHE_TTL);
            return encoded;
        } catch (Exception e) {
            throw new RuntimeException("Failed to build client config", e);
        }
    }

    public void invalidateClientConfigCache() {
        cacheStore.delete(CLIENT_CONFIG_KEY);
    }

    private AuthConfig getByType(String authType) {
        return mapper.selectOneByQuery(
                com.mybatisflex.core.query.QueryWrapper.create().eq("auth_type", authType));
    }
}
