package org.codelin.oauth.oauth.common.security;

import lombok.Data;

import java.time.Instant;
import java.util.Set;

/**
 * Token信息
 */
@Data
public class TokenInfo {

    private String token;
    private Long userId;
    private Long orgId;
    private String clientId;
    private Set<String> scopes;
    private Instant expiresAt;

    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }
}
