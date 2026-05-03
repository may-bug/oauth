package org.codelin.oauth.oauth.common.security;

import lombok.Getter;

import java.util.Set;

/**
 * OAuth2认证信息
 */
@Getter
public class OAuth2Authentication {

    private final Long userId;
    private final Long orgId;
    private final String token;
    private final Set<String> scopes;
    private final String clientId;

    public OAuth2Authentication(Long userId, Long orgId, String token,
                                 Set<String> scopes, String clientId) {
        this.userId = userId;
        this.orgId = orgId;
        this.token = token;
        this.scopes = scopes;
        this.clientId = clientId;
    }

    public boolean isAuthenticated() {
        return userId != null;
    }

    public boolean hasScope(String scope) {
        return scopes != null && scopes.contains(scope);
    }
}
