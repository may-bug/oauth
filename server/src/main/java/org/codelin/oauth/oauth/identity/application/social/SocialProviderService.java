package org.codelin.oauth.oauth.identity.application.social;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.codelin.oauth.oauth.common.web.BizException;
import org.codelin.oauth.oauth.identity.domain.model.SocialProviderConfig;
import org.codelin.oauth.oauth.identity.infrastructure.persistence.SocialProviderConfigMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 社交登录提供商服务 - CRUD + 缓存 + 提供商注册
 */
@Slf4j
@Service
public class SocialProviderService {

    private final SocialProviderConfigMapper configMapper;

    /** 运行时注册的提供商实现（Spring自动注入） */
    private final List<SocialAuthProvider> providers;

    public SocialProviderService(SocialProviderConfigMapper configMapper,
                                 @Lazy List<SocialAuthProvider> providers) {
        this.configMapper = configMapper;
        this.providers = providers;
    }

    private volatile Map<String, SocialProviderConfig> configCache;

    /**
     * 根据名称获取提供商实现
     */
    public SocialAuthProvider getProvider(String provider) {
        // 确保DB中已启用
        SocialProviderConfig config = getConfigFromCache(provider);
        if (config == null || !Boolean.TRUE.equals(config.getEnabled())) {
            throw BizException.badRequest("error.auth.social-provider-not-supported");
        }
        // 查找注册的实现
        return providers.stream()
                .filter(p -> p.getProvider().equals(provider))
                .findFirst()
                .orElseThrow(() -> BizException.badRequest("error.auth.social-provider-not-supported"));
    }

    /**
     * 获取提供商配置（供实现类使用）
     */
    public SocialProviderConfig getConfig(String provider) {
        SocialProviderConfig config = getConfigFromCache(provider);
        if (config == null) {
            throw BizException.badRequest("error.auth.social-provider-not-supported");
        }
        return config;
    }

    // ========== CRUD ==========

    public List<SocialProviderConfig> listAll() {
        return configMapper.selectListByQuery(
                QueryWrapper.create().orderBy("sort_order", true));
    }

    public SocialProviderConfig getByProvider(String provider) {
        return configMapper.selectOneByQuery(
                QueryWrapper.create().eq("provider", provider));
    }

    public void createOrUpdate(SocialProviderConfig config) {
        SocialProviderConfig existing = getByProvider(config.getProvider());
        if (existing != null) {
            config.setId(existing.getId());
            if (config.getClientSecret() == null || config.getClientSecret().isEmpty()
                    || "******".equals(config.getClientSecret())) {
                config.setClientSecret(existing.getClientSecret());
            }
            config.setUpdatedAt(LocalDateTime.now());
            configMapper.update(config);
        } else {
            config.setUpdatedAt(LocalDateTime.now());
            configMapper.insert(config);
        }
        invalidateCache();
    }

    public void delete(String provider) {
        SocialProviderConfig config = getByProvider(provider);
        if (config != null) {
            configMapper.deleteById(config.getId());
            invalidateCache();
        }
    }

    public void toggleEnabled(String provider, boolean enabled) {
        SocialProviderConfig config = getByProvider(provider);
        if (config != null) {
            config.setEnabled(enabled);
            config.setUpdatedAt(LocalDateTime.now());
            configMapper.update(config);
            invalidateCache();
        }
    }

    // ========== 缓存 ==========

    private SocialProviderConfig getConfigFromCache(String provider) {
        Map<String, SocialProviderConfig> cache = configCache;
        if (cache == null) {
            synchronized (this) {
                cache = configCache;
                if (cache == null) {
                    cache = loadCache();
                    configCache = cache;
                }
            }
        }
        return cache.get(provider);
    }

    private Map<String, SocialProviderConfig> loadCache() {
        List<SocialProviderConfig> list = configMapper.selectListByQuery(
                QueryWrapper.create().eq("deleted", 0));
        Map<String, SocialProviderConfig> map = new ConcurrentHashMap<>();
        for (SocialProviderConfig config : list) {
            map.put(config.getProvider(), config);
        }
        log.info("Loaded {} social provider configs", map.size());
        return map;
    }

    public String getDefaultRedirectUri(String provider) {
        SocialProviderConfig config = getConfigFromCache(provider);
        if (config != null && config.getDefaultRedirectUri() != null
                && !config.getDefaultRedirectUri().isEmpty()) {
            return config.getDefaultRedirectUri();
        }
        return "http://localhost:8080/auth/social/" + provider + "/callback";
    }

    public void invalidateCache() {
        configCache = null;
    }
}
