package org.codelin.oauth.oauth.identity.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codelin.oauth.oauth.common.cache.CacheStore;
import org.codelin.oauth.oauth.common.web.BizException;
import org.springframework.stereotype.Component;

/**
 * 验证码验证器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CaptchaValidator {

    private static final String CAPTCHA_CACHE_PREFIX = "captcha:";

    private final CacheStore cacheStore;

    /**
     * 验证验证码
     *
     * @param token 验证码token
     * @param code  用户输入的验证码
     */
    public void validate(String token, String code) {
        validate(token, code, false);
    }

    /**
     * 验证验证码
     *
     * @param token        验证码token
     * @param code         用户输入的验证码
     * @param caseSensitive 是否区分大小写
     */
    public void validate(String token, String code, boolean caseSensitive) {
        if (token == null || token.isEmpty()) {
            throw BizException.badRequest("error.captcha.token-required");
        }
        if (code == null || code.isEmpty()) {
            throw BizException.badRequest("error.captcha.required");
        }

        String cacheKey = CAPTCHA_CACHE_PREFIX + token;
        String cachedCode = cacheStore.get(cacheKey);

        if (cachedCode == null) {
            throw BizException.badRequest("error.captcha.expired");
        }

        boolean matched;
        if (caseSensitive) {
            matched = cachedCode.equals(code);
        } else {
            matched = cachedCode.equalsIgnoreCase(code);
        }

        if (!matched) {
            throw BizException.badRequest("error.captcha.invalid");
        }

        // 验证成功后删除验证码（一次性使用）
        cacheStore.delete(cacheKey);
        log.debug("Captcha validated successfully for token: {}", token);
    }

    /**
     * 存储验证码到缓存
     *
     * @param token 验证码token
     * @param code  验证码值
     * @param ttlSeconds 过期时间（秒）
     */
    public void store(String token, String code, long ttlSeconds) {
        String cacheKey = CAPTCHA_CACHE_PREFIX + token;
        cacheStore.set(cacheKey, code, java.time.Duration.ofSeconds(ttlSeconds));
        log.debug("Captcha stored for token: {}, ttl: {}s", token, ttlSeconds);
    }
}
