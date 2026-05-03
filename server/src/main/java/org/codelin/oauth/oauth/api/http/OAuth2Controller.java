package org.codelin.oauth.oauth.api.http;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codelin.oauth.oauth.common.security.Anonymous;
import org.codelin.oauth.oauth.common.security.SecurityContextHolder;
import org.codelin.oauth.oauth.common.web.BizException;
import org.codelin.oauth.oauth.common.web.R;
import org.codelin.oauth.oauth.authorization.application.OAuth2Service;
import org.codelin.oauth.oauth.identity.application.UserService;
import org.codelin.oauth.oauth.identity.domain.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * OAuth2控制器
 */
@Slf4j
@RestController
@RequestMapping("/oauth2")
@RequiredArgsConstructor
public class OAuth2Controller {

    private final OAuth2Service oauth2Service;
    private final UserService userService;

    /**
     * 授权端点 - 生成授权码
     * <p>
     * 前端应先让用户登录，然后调用此接口获取授权码
     */
    @GetMapping(value = "/authorize", version = "1")
    public ResponseEntity<R<Map<String, Object>>> authorize(
            @RequestParam String client_id,
            @RequestParam String redirect_uri,
            @RequestParam(required = false) String scope,
            @RequestParam(required = false) String state,
            @RequestParam(defaultValue = "code") String response_type) {

        Long userId = SecurityContextHolder.getUserId();
        if (userId == null) {
            throw BizException.unauthorized("error.auth.login-required");
        }

        if (!"code".equals(response_type)) {
            throw BizException.badRequest("error.oauth.invalid-grant");
        }

        String code = oauth2Service.generateAuthorizationCode(
                client_id, redirect_uri, scope, state, userId);

        return R.ok(Map.of(
                "code", code,
                "state", state != null ? state : ""
        ));
    }

    /**
     * 授权端点 - 重定向方式（用于第三方应用跳转）
     */
    @Anonymous
    @GetMapping(value = "/authorize/redirect", version = "1")
    public void authorizeRedirect(
            @RequestParam String client_id,
            @RequestParam String redirect_uri,
            @RequestParam(required = false) String scope,
            @RequestParam(required = false) String state,
            @RequestParam(defaultValue = "code") String response_type,
            jakarta.servlet.http.HttpServletResponse response) throws Exception {

        Long userId = SecurityContextHolder.getUserId();
        if (userId == null) {
            // 未登录，重定向到登录页
            String loginUrl = "/login?redirect=" + java.net.URLEncoder.encode(
                    "/oauth2/authorize/redirect?client_id=" + client_id +
                    "&redirect_uri=" + redirect_uri +
                    (scope != null ? "&scope=" + scope : "") +
                    (state != null ? "&state=" + state : ""),
                    StandardCharsets.UTF_8);
            response.sendRedirect(loginUrl);
            return;
        }

        if (!"code".equals(response_type)) {
            response.sendError(400, "不支持的response_type");
            return;
        }

        String code = oauth2Service.generateAuthorizationCode(
                client_id, redirect_uri, scope, state, userId);

        // 构建回调URL
        StringBuilder callbackUrl = new StringBuilder(redirect_uri);
        callbackUrl.append(redirect_uri.contains("?") ? "&" : "?");
        callbackUrl.append("code=").append(code);
        if (state != null) {
            callbackUrl.append("&state=").append(state);
        }

        response.sendRedirect(callbackUrl.toString());
    }

    /**
     * Token端点 - 用授权码交换Token
     */
    @Anonymous
    @PostMapping(value = "/token", version = "1")
    public ResponseEntity<R<Map<String, Object>>> token(@RequestBody TokenRequest request) {
        Map<String, Object> tokens;

        switch (request.getGrant_type()) {
            case "authorization_code":
                tokens = oauth2Service.exchangeAuthorizationCode(
                        request.getCode(),
                        request.getClient_id(),
                        request.getClient_secret(),
                        request.getRedirect_uri());
                break;

            case "client_credentials":
                tokens = oauth2Service.clientCredentialsGrant(
                        request.getClient_id(),
                        request.getClient_secret(),
                        request.getScope());
                break;

            case "refresh_token":
                tokens = oauth2Service.refreshToken(
                        request.getRefresh_token(),
                        request.getClient_id(),
                        request.getClient_secret());
                break;

            default:
                return R.fail(400, "不支持的grant_type");
        }

        return R.ok(tokens);
    }

    /**
     * UserInfo端点 - 获取用户信息
     */
    @GetMapping(value = "/userinfo", version = "1")
    public ResponseEntity<R<Map<String, Object>>> userinfo() {
        Long userId = SecurityContextHolder.getUserId();
        if (userId == null) {
            throw BizException.unauthorized("error.auth.login-required");
        }

        User user = userService.getById(userId);
        if (user == null) {
            throw BizException.notFound("error.user.not-found");
        }

        // 获取当前token的scopes
        var auth = org.codelin.oauth.oauth.common.security.SecurityContextHolder.getAuthentication();
        java.util.Set<String> scopes = auth != null ? auth.getScopes() : java.util.Set.of();

        Map<String, Object> userinfo = new java.util.LinkedHashMap<>();
        userinfo.put("sub", userId.toString());

        if (scopes.contains("profile")) {
            userinfo.put("name", user.getNickname());
            userinfo.put("preferred_username", user.getUsername());
            userinfo.put("picture", user.getAvatar());
        }

        if (scopes.contains("email")) {
            var emailCred = userService.getCredential(userId, "email");
            if (emailCred != null) {
                userinfo.put("email", emailCred.getCredentialKey());
                userinfo.put("email_verified", emailCred.getVerified());
            }
        }

        if (scopes.contains("phone")) {
            var phoneCred = userService.getCredential(userId, "phone");
            if (phoneCred != null) {
                userinfo.put("phone_number", phoneCred.getCredentialKey());
                userinfo.put("phone_number_verified", phoneCred.getVerified());
            }
        }

        return R.ok(userinfo);
    }

    /**
     * 撤销Token端点
     */
    @Anonymous
    @PostMapping(value = "/revoke", version = "1")
    public ResponseEntity<R<Void>> revoke(@RequestBody RevokeRequest request) {
        oauth2Service.revokeToken(
                request.getToken(),
                request.getToken_type_hint(),
                request.getClient_id(),
                request.getClient_secret());
        return R.ok();
    }

    /**
     * Token内省端点（用于资源服务器验证Token）
     */
    @Anonymous
    @PostMapping(value = "/introspect", version = "1")
    public ResponseEntity<R<Map<String, Object>>> introspect(@RequestBody IntrospectRequest request) {
        Map<String, Object> result = oauth2Service.introspect(request.getToken());
        return R.ok(result);
    }

    // ========== 请求体 ==========

    @lombok.Data
    public static class TokenRequest {
        private String grant_type;
        private String code;
        private String client_id;
        private String client_secret;
        private String redirect_uri;
        private String scope;
        private String refresh_token;
    }

    @lombok.Data
    public static class RevokeRequest {
        private String token;
        private String token_type_hint;
        private String client_id;
        private String client_secret;
    }

    @lombok.Data
    public static class IntrospectRequest {
        private String token;
    }
}
