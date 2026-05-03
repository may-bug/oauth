package org.codelin.oauth.oauth.identity.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codelin.oauth.oauth.common.cache.CacheStore;
import org.codelin.oauth.oauth.identity.domain.model.CaptchaConfig;
import org.codelin.oauth.oauth.identity.domain.model.CaptchaEndpointConfig;
import org.codelin.oauth.oauth.identity.infrastructure.captcha.SvgCaptchaGenerator;
import org.codelin.oauth.oauth.identity.infrastructure.persistence.CaptchaConfigMapper;
import org.codelin.oauth.oauth.identity.infrastructure.persistence.CaptchaEndpointConfigMapper;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 验证码配置服务（数据库配置）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CaptchaConfigService {

    private final CaptchaConfigMapper captchaConfigMapper;
    private final CaptchaEndpointConfigMapper endpointConfigMapper;
    private final CacheStore cacheStore;

    private static final String CACHE_KEY_CONFIG = "captcha:config";
    private static final String CACHE_KEY_ENDPOINTS = "captcha:endpoints";
    private static final Duration CACHE_TTL = Duration.ofMinutes(10);

    /**
     * 获取验证码配置（带缓存）
     */
    public CaptchaConfig getConfig() {
        CaptchaConfig cached = cacheStore.get(CACHE_KEY_CONFIG);
        if (cached != null) {
            return cached;
        }
        List<CaptchaConfig> list = captchaConfigMapper.selectAll();
        CaptchaConfig config = (list != null && !list.isEmpty()) ? list.getFirst() : getDefaultConfig();
        cacheStore.set(CACHE_KEY_CONFIG, config, CACHE_TTL);
        return config;
    }

    /**
     * 检查验证码是否启用
     */
    public boolean isEnabled() {
        CaptchaConfig config = getConfig();
        return Boolean.TRUE.equals(config.getEnabled());
    }

    /**
     * 获取所有端点配置（带缓存）
     */
    public List<CaptchaEndpointConfig> getEndpointConfigs() {
        List<CaptchaEndpointConfig> cached = cacheStore.get(CACHE_KEY_ENDPOINTS);
        if (cached != null) {
            return cached;
        }
        List<CaptchaEndpointConfig> configs = endpointConfigMapper.selectAll();
        cacheStore.set(CACHE_KEY_ENDPOINTS, configs, CACHE_TTL);
        return configs;
    }

    /**
     * 检查指定端点是否需要验证码
     */
    public boolean requiresCaptcha(String method, String endpoint) {
        if (!isEnabled()) {
            return false;
        }

        String fullEndpoint = method + ":" + endpoint;
        List<CaptchaEndpointConfig> configs = getEndpointConfigs();
        for (CaptchaEndpointConfig ep : configs) {
            if (fullEndpoint.equals(ep.getEndpoint())
                    && Boolean.TRUE.equals(ep.getEnabled())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 将数据库配置转换为SvgCaptchaGenerator.CaptchaConfig
     */
    public SvgCaptchaGenerator.CaptchaConfig toGeneratorConfig() {
        CaptchaConfig dbConfig = getConfig();
        SvgCaptchaGenerator.CaptchaConfig generatorConfig = new SvgCaptchaGenerator.CaptchaConfig();
        generatorConfig.setLength(dbConfig.getLength() != null ? dbConfig.getLength() : 4);
        generatorConfig.setCaseSensitive(Boolean.TRUE.equals(dbConfig.getCaseSensitive()));
        generatorConfig.setNoiseLines(dbConfig.getNoiseLines() != null ? dbConfig.getNoiseLines() : 5);
        generatorConfig.setNoiseDots(dbConfig.getNoiseDots() != null ? dbConfig.getNoiseDots() : 50);
        generatorConfig.setRotation(Boolean.TRUE.equals(dbConfig.getRotation()));
        generatorConfig.setWave(Boolean.TRUE.equals(dbConfig.getWave()));
        return generatorConfig;
    }

    /**
     * 获取验证码过期时间（秒）
     */
    public long getExpireSeconds() {
        CaptchaConfig config = getConfig();
        return config.getExpireSeconds() != null ? config.getExpireSeconds() : 300;
    }

    /**
     * 更新验证码配置
     */
    public void updateConfig(CaptchaConfig config) {
        List<CaptchaConfig> list = captchaConfigMapper.selectAll();
        CaptchaConfig existing = (list != null && !list.isEmpty()) ? list.getFirst() : null;
        if (existing != null && existing.getId() != null) {
            config.setId(existing.getId());
            captchaConfigMapper.update(config);
        } else {
            captchaConfigMapper.insert(config);
        }
        cacheStore.delete(CACHE_KEY_CONFIG);
    }

    /**
     * 更新端点验证码配置
     */
    public void updateEndpointConfig(CaptchaEndpointConfig config) {
        CaptchaEndpointConfig existing = endpointConfigMapper.selectOneByQuery(
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
     * 默认配置
     */
    private CaptchaConfig getDefaultConfig() {
        CaptchaConfig config = new CaptchaConfig();
        config.setEnabled(true);
        config.setCaptchaType("svg");
        config.setLength(4);
        config.setExpireSeconds(300);
        config.setCaseSensitive(false);
        config.setNoiseLines(5);
        config.setNoiseDots(50);
        config.setRotation(true);
        config.setWave(true);
        return config;
    }
}
