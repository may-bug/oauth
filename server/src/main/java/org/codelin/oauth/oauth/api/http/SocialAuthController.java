package org.codelin.oauth.oauth.api.http;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codelin.oauth.oauth.common.cache.CacheStore;
import org.codelin.oauth.oauth.common.security.Anonymous;
import org.codelin.oauth.oauth.common.security.SecurityContextHolder;
import org.codelin.oauth.oauth.common.web.BizException;
import org.codelin.oauth.oauth.common.web.R;
import org.codelin.oauth.oauth.common.web.RequestUtils;
import org.codelin.oauth.oauth.identity.application.AuthService;
import org.codelin.oauth.oauth.identity.application.UserService;
import org.codelin.oauth.oauth.identity.application.social.SocialAuthProvider;
import org.codelin.oauth.oauth.identity.application.social.SocialProviderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/auth/social")
@RequiredArgsConstructor
public class SocialAuthController {

    private static final Duration STATE_TTL = Duration.ofMinutes(10);

    private final SocialProviderService socialProviderService;
    private final AuthService authService;
    private final UserService userService;
    private final CacheStore cacheStore;

    @Anonymous
    @GetMapping(value = "/{provider}", version = "1")
    public ResponseEntity<R<Map<String, String>>> getAuthUrl(
            @PathVariable String provider,
            @RequestParam(required = false) String redirectUri) {
        SocialAuthProvider authProvider = socialProviderService.getProvider(provider);
        String state = UUID.randomUUID().toString();
        cacheStore.set("social:state:" + state, provider, STATE_TTL);
        String url = authProvider.getAuthorizationUrl(
                redirectUri != null ? redirectUri : socialProviderService.getDefaultRedirectUri(provider), state);
        return R.ok(Map.of("auth_url", url, "state", state));
    }

    @Anonymous
    @GetMapping(value = "/{provider}/callback", version = "1")
    public ResponseEntity<R<Map<String, Object>>> callback(
            @PathVariable String provider, @RequestParam String code,
            @RequestParam(required = false) String state, HttpServletRequest request) {
        validateState(provider, state);
        SocialAuthProvider.SocialUserInfo socialUser = socialProviderService.getProvider(provider)
                .getUserInfo(code, socialProviderService.getDefaultRedirectUri(provider));
        return R.ok(authService.loginBySocial(provider, socialUser.getProviderUid(),
                socialUser.getNickname(), socialUser.getAvatar(), socialUser.getEmail(),
                RequestUtils.getClientIp(request)));
    }

    @GetMapping(value = "/{provider}/bind", version = "1")
    public ResponseEntity<R<Map<String, String>>> bindSocial(
            @PathVariable String provider, @RequestParam(required = false) String redirectUri) {
        Long userId = SecurityContextHolder.getUserId();
        if (userId == null) throw BizException.unauthorized("error.auth.login-required");
        String state = UUID.randomUUID().toString();
        cacheStore.set("social:bind:state:" + state, userId, STATE_TTL);
        String url = socialProviderService.getProvider(provider).getAuthorizationUrl(
                redirectUri != null ? redirectUri : socialProviderService.getDefaultRedirectUri(provider) + "/bind",
                state);
        return R.ok(Map.of("auth_url", url, "state", state));
    }

    @Anonymous
    @GetMapping(value = "/{provider}/callback/bind", version = "1")
    public ResponseEntity<R<Void>> bindCallback(@PathVariable String provider,
                                                  @RequestParam String code,
                                                  @RequestParam(required = false) String state) {
        Long userId = state != null ? (Long) cacheStore.get("social:bind:state:" + state) : null;
        if (userId == null) throw BizException.badRequest("error.auth.invalid-state");
        cacheStore.delete("social:bind:state:" + state);
        SocialAuthProvider.SocialUserInfo socialUser = socialProviderService.getProvider(provider)
                .getUserInfo(code, socialProviderService.getDefaultRedirectUri(provider) + "/bind");
        userService.bindSocialAccount(userId, provider, socialUser.getProviderUid());
        return R.ok();
    }

    @PostMapping(value = "/{provider}/unbind", version = "1")
    public ResponseEntity<R<Void>> unbindSocial(@PathVariable String provider) {
        Long userId = SecurityContextHolder.getUserId();
        if (userId == null) throw BizException.unauthorized("error.auth.login-required");
        userService.unbindSocialAccount(userId, provider);
        return R.ok();
    }

    private void validateState(String provider, String state) {
        if (state != null) {
            String cached = cacheStore.get("social:state:" + state);
            if (cached == null || !cached.equals(provider))
                throw BizException.badRequest("error.auth.invalid-state");
            cacheStore.delete("social:state:" + state);
        }
    }
}
