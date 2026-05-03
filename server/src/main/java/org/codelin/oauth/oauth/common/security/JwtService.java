package org.codelin.oauth.oauth.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

/**
 * JWT 服务 - 生成、解析、校验 JWT Token
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtConfig jwtConfig;

    private volatile SecretKey cachedKey;

    /**
     * 生成 JWT Access Token
     */
    public String generateToken(Long userId, Long orgId, String clientId, Set<String> scopes, long ttlSeconds) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(ttlSeconds > 0 ? ttlSeconds : jwtConfig.getAccessTokenTtl());

        Map<String, Object> claims = new LinkedHashMap<>();
        if (orgId != null) {
            claims.put("org", orgId);
        }
        if (clientId != null) {
            claims.put("client", clientId);
        }
        if (scopes != null && !scopes.isEmpty()) {
            claims.put("scopes", String.join(",", scopes));
        }

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claims(claims)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(getKey())
                .compact();
    }

    /**
     * 解析并验证 JWT Token
     *
     * @return TokenInfo 如果有效; null 如果无效或过期
     */
    public TokenInfo parseToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            TokenInfo info = new TokenInfo();
            info.setToken(token);
            info.setUserId(Long.parseLong(claims.getSubject()));

            Number orgClaim = (Number) claims.get("org");
            info.setOrgId(orgClaim != null ? orgClaim.longValue() : null);

            info.setClientId((String) claims.get("client"));

            String scopesStr = (String) claims.get("scopes");
            if (scopesStr != null && !scopesStr.isEmpty()) {
                info.setScopes(new LinkedHashSet<>(Arrays.asList(scopesStr.split(","))));
            } else {
                info.setScopes(Set.of());
            }

            info.setExpiresAt(claims.getExpiration().toInstant());
            return info;

        } catch (ExpiredJwtException e) {
            log.debug("JWT expired: {}", e.getMessage());
            return null;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("JWT invalid: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取签名密钥（带缓存）
     */
    private SecretKey getKey() {
        if (cachedKey != null) {
            return cachedKey;
        }
        synchronized (this) {
            if (cachedKey != null) {
                return cachedKey;
            }
            String secret = jwtConfig.getSecret();
            if (secret == null || secret.isEmpty()) {
                // 自动生成随机密钥
                secret = UUID.randomUUID().toString() + UUID.randomUUID().toString();
                jwtConfig.setSecret(secret);
                log.info("JWT secret auto-generated");
            }
            cachedKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            return cachedKey;
        }
    }
}
