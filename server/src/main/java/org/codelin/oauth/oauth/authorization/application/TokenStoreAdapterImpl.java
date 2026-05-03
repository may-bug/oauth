package org.codelin.oauth.oauth.authorization.application;

import lombok.RequiredArgsConstructor;
import org.codelin.oauth.oauth.common.security.TokenInfo;
import org.codelin.oauth.oauth.common.security.WebAuthenticationFilter;
import org.springframework.stereotype.Component;

/**
 * TokenStore适配器实现 - 放在authorization模块，因为依赖TokenStore
 */
@Component
@RequiredArgsConstructor
public class TokenStoreAdapterImpl implements WebAuthenticationFilter.TokenStoreAdapter {

    private final TokenService tokenService;

    @Override
    public TokenInfo findToken(String token) {
        return tokenService.validateAccessToken(token);
    }
}
