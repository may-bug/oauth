package org.codelin.oauth.oauth.authorization.infrastructure.token;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codelin.oauth.oauth.common.security.TokenInfo;
import org.redisson.api.RMap;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "oauth.token.store-type", havingValue = "redis")
public class RedisTokenStore implements TokenStore {

    private final RedissonClient redissonClient;

    private static final String TOKEN_PREFIX = "oauth:token:";
    private static final String AUTH_CODE_PREFIX = "oauth:auth_code:";
    private static final String REFRESH_TOKEN_PREFIX = "oauth:refresh_token:";
    private static final String USER_TOKENS_PREFIX = "oauth:user_tokens:";

    @Override
    public void saveToken(TokenInfo tokenInfo) {
        String key = TOKEN_PREFIX + tokenInfo.getToken();
        long ttlSeconds = Duration.between(Instant.now(), tokenInfo.getExpiresAt()).getSeconds();
        if (ttlSeconds <= 0) return;

        RMap<String, String> map = redissonClient.getMap(key);
        map.put("userId", tokenInfo.getUserId() != null ? tokenInfo.getUserId().toString() : "");
        map.put("orgId", tokenInfo.getOrgId() != null ? tokenInfo.getOrgId().toString() : "");
        map.put("clientId", tokenInfo.getClientId() != null ? tokenInfo.getClientId() : "");
        map.put("scopes", tokenInfo.getScopes() != null ? String.join(",", tokenInfo.getScopes()) : "");
        map.put("expiresAt", tokenInfo.getExpiresAt().toString());
        map.expire(ttlSeconds, TimeUnit.SECONDS);

        if (tokenInfo.getUserId() != null) {
            RSet<String> userTokens = redissonClient.getSet(USER_TOKENS_PREFIX + tokenInfo.getUserId());
            userTokens.add(tokenInfo.getToken());
            userTokens.expire(ttlSeconds, TimeUnit.SECONDS);
        }
    }

    @Override
    public TokenInfo findToken(String token) {
        String key = TOKEN_PREFIX + token;
        RMap<String, String> map = redissonClient.getMap(key);
        if (!map.isExists()) return null;

        TokenInfo info = new TokenInfo();
        info.setToken(token);

        String userId = map.get("userId");
        if (userId != null && !userId.isEmpty()) info.setUserId(Long.parseLong(userId));
        String orgId = map.get("orgId");
        if (orgId != null && !orgId.isEmpty()) info.setOrgId(Long.parseLong(orgId));
        info.setClientId(map.get("clientId"));
        String scopes = map.get("scopes");
        if (scopes != null && !scopes.isEmpty()) info.setScopes(Set.of(scopes.split(",")));
        String expiresAt = map.get("expiresAt");
        if (expiresAt != null) info.setExpiresAt(Instant.parse(expiresAt));
        return info;
    }

    @Override
    public void removeToken(String token) {
        TokenInfo info = findToken(token);
        redissonClient.getMap(TOKEN_PREFIX + token).delete();
        if (info != null && info.getUserId() != null) {
            redissonClient.getSet(USER_TOKENS_PREFIX + info.getUserId()).remove(token);
        }
    }

    @Override
    public void revokeUserTokens(Long userId, String clientId) {
        RSet<String> userTokens = redissonClient.getSet(USER_TOKENS_PREFIX + userId);
        for (String token : userTokens.readAll()) {
            TokenInfo info = findToken(token);
            if (info != null && (clientId == null || clientId.equals(info.getClientId()))) {
                removeToken(token);
            }
        }
    }

    @Override
    public void saveAuthorizationCode(String code, AuthorizationCodeInfo info) {
        RMap<String, String> map = redissonClient.getMap(AUTH_CODE_PREFIX + code);
        map.put("code", info.code());
        map.put("clientId", info.clientId());
        map.put("userId", info.userId().toString());
        map.put("redirectUri", info.redirectUri() != null ? info.redirectUri() : "");
        map.put("scopes", info.scopes() != null ? String.join(",", info.scopes()) : "");
        map.put("state", info.state() != null ? info.state() : "");
        map.expire(600, TimeUnit.SECONDS);
    }

    @Override
    public AuthorizationCodeInfo consumeAuthorizationCode(String code) {
        String key = AUTH_CODE_PREFIX + code;
        RMap<String, String> map = redissonClient.getMap(key);
        if (!map.isExists()) return null;

        String clientId = map.get("clientId");
        String userId = map.get("userId");
        String redirectUri = map.get("redirectUri");
        String scopes = map.get("scopes");
        String state = map.get("state");
        map.delete();

        Set<String> scopeSet = (scopes != null && !scopes.isEmpty()) ? Set.of(scopes.split(",")) : Set.of();
        return new AuthorizationCodeInfo(code, clientId, Long.parseLong(userId), redirectUri, scopeSet, state);
    }

    @Override
    public void saveRefreshToken(String refreshToken, RefreshTokenInfo info) {
        RMap<String, String> map = redissonClient.getMap(REFRESH_TOKEN_PREFIX + refreshToken);
        map.put("token", info.token());
        map.put("accessToken", info.accessToken() != null ? info.accessToken() : "");
        map.put("clientId", info.clientId());
        map.put("userId", info.userId().toString());
        map.put("scopes", info.scopes() != null ? String.join(",", info.scopes()) : "");
        map.expire(86400, TimeUnit.SECONDS);
    }

    @Override
    public RefreshTokenInfo findRefreshToken(String refreshToken) {
        RMap<String, String> map = redissonClient.getMap(REFRESH_TOKEN_PREFIX + refreshToken);
        if (!map.isExists()) return null;

        String token = map.get("token");
        String accessToken = map.get("accessToken");
        String clientId = map.get("clientId");
        String userId = map.get("userId");
        String scopes = map.get("scopes");
        Set<String> scopeSet = (scopes != null && !scopes.isEmpty()) ? Set.of(scopes.split(",")) : Set.of();
        return new RefreshTokenInfo(token, accessToken, clientId, Long.parseLong(userId), scopeSet);
    }

    @Override
    public void removeRefreshToken(String refreshToken) {
        redissonClient.getMap(REFRESH_TOKEN_PREFIX + refreshToken).delete();
    }
}
