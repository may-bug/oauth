package org.codelin.oauth.oauth.authorization.infrastructure.token;

import lombok.extern.slf4j.Slf4j;
import org.codelin.oauth.oauth.common.security.TokenInfo;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 内存Token存储实现
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "oauth.token.store-type", havingValue = "memory", matchIfMissing = true)
public class InMemoryTokenStore implements TokenStore {

    private final ConcurrentHashMap<String, TokenInfo> tokenStore = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AuthorizationCodeInfo> codeStore = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, RefreshTokenInfo> refreshTokenStore = new ConcurrentHashMap<>();

    // 用户Token索引，用于批量撤销
    private final ConcurrentHashMap<String, Set<String>> userTokenIndex = new ConcurrentHashMap<>();

    @Override
    public void saveToken(TokenInfo tokenInfo) {
        tokenStore.put(tokenInfo.getToken(), tokenInfo);

        // 更新用户索引
        String indexKey = tokenInfo.getUserId() + ":" + tokenInfo.getClientId();
        userTokenIndex.computeIfAbsent(indexKey, k -> ConcurrentHashMap.newKeySet())
                .add(tokenInfo.getToken());

        log.debug("Token saved for user: {}, client: {}", tokenInfo.getUserId(), tokenInfo.getClientId());
    }

    @Override
    public TokenInfo findToken(String token) {
        return tokenStore.get(token);
    }

    @Override
    public void removeToken(String token) {
        TokenInfo info = tokenStore.remove(token);
        if (info != null) {
            String indexKey = info.getUserId() + ":" + info.getClientId();
            Set<String> tokens = userTokenIndex.get(indexKey);
            if (tokens != null) {
                tokens.remove(token);
            }
        }
    }

    @Override
    public void revokeUserTokens(Long userId, String clientId) {
        String indexKey = userId + ":" + clientId;
        Set<String> tokens = userTokenIndex.remove(indexKey);
        if (tokens != null) {
            tokens.forEach(tokenStore::remove);
            log.debug("Revoked {} tokens for user: {}, client: {}", tokens.size(), userId, clientId);
        }
    }

    @Override
    public void saveAuthorizationCode(String code, AuthorizationCodeInfo info) {
        codeStore.put(code, info);
    }

    @Override
    public AuthorizationCodeInfo consumeAuthorizationCode(String code) {
        return codeStore.remove(code);
    }

    @Override
    public void saveRefreshToken(String refreshToken, RefreshTokenInfo info) {
        refreshTokenStore.put(refreshToken, info);
    }

    @Override
    public RefreshTokenInfo findRefreshToken(String refreshToken) {
        return refreshTokenStore.get(refreshToken);
    }

    @Override
    public void removeRefreshToken(String refreshToken) {
        refreshTokenStore.remove(refreshToken);
    }

    /**
     * 定期清理过期Token
     */
    @Scheduled(fixedRate = 60000)
    public void cleanExpiredTokens() {
        Instant now = Instant.now();

        // 清理过期Access Token
        int removed = 0;
        var tokenIterator = tokenStore.entrySet().iterator();
        while (tokenIterator.hasNext()) {
            var entry = tokenIterator.next();
            if (entry.getValue().getExpiresAt() != null && entry.getValue().getExpiresAt().isBefore(now)) {
                TokenInfo info = entry.getValue();
                String indexKey = info.getUserId() + ":" + info.getClientId();
                Set<String> tokens = userTokenIndex.get(indexKey);
                if (tokens != null) {
                    tokens.remove(entry.getKey());
                }
                tokenIterator.remove();
                removed++;
            }
        }

        if (removed > 0) {
            log.debug("Cleaned {} expired tokens", removed);
        }
    }
}
