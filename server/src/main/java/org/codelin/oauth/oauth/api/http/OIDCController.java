package org.codelin.oauth.oauth.api.http;

import lombok.RequiredArgsConstructor;
import org.codelin.oauth.oauth.common.security.Anonymous;
import org.codelin.oauth.oauth.identity.application.CryptoConfigService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * OIDC发现端点
 */
@RestController
@RequiredArgsConstructor
public class OIDCController {

    private final CryptoConfigService cryptoConfigService;

    @Value("${app.server-url}")
    private String serverUrl;

    /**
     * OpenID Configuration
     */
    @Anonymous
    @GetMapping("/.well-known/openid-configuration")
    public ResponseEntity<Map<String, Object>> openIdConfiguration() {
        String issuer = getIssuer();

        Map<String, Object> config = new LinkedHashMap<>();
        config.put("issuer", issuer);
        config.put("authorization_endpoint", issuer + "/oauth2/authorize");
        config.put("token_endpoint", issuer + "/oauth2/token");
        config.put("userinfo_endpoint", issuer + "/oauth2/userinfo");
        config.put("jwks_uri", issuer + "/.well-known/jwks.json");
        config.put("revocation_endpoint", issuer + "/oauth2/revoke");
        config.put("introspection_endpoint", issuer + "/oauth2/introspect");

        config.put("response_types_supported", List.of("code"));
        config.put("grant_types_supported", List.of(
                "authorization_code", "client_credentials", "refresh_token"));
        config.put("subject_types_supported", List.of("public"));
        config.put("id_token_signing_alg_values_supported", List.of("RS256"));

        config.put("scopes_supported", List.of(
                "openid", "profile", "email", "phone", "address", "offline_access"));

        config.put("token_endpoint_auth_methods_supported", List.of(
                "client_secret_basic", "client_secret_post", "none"));

        config.put("claims_supported", List.of(
                "sub", "name", "preferred_username", "email", "email_verified",
                "phone_number", "phone_number_verified", "picture"));

        config.put("code_challenge_methods_supported", List.of("S256", "plain"));

        return ResponseEntity.ok(config);
    }

    /**
     * JWKS端点 - 公钥集
     */
    @Anonymous
    @GetMapping("/.well-known/jwks.json")
    public ResponseEntity<Map<String, Object>> jwks() {
        String publicKey = cryptoConfigService.getPublicKey();

        Map<String, Object> jwks = new LinkedHashMap<>();
        jwks.put("keys", List.of(Map.of(
                "kty", "RSA",
                "use", "sig",
                "keyid", "rsa-key-1",
                "alg", "RS256",
                "n", publicKey != null ? publicKey : "",
                "e", "AQAB"
        )));

        return ResponseEntity.ok(jwks);
    }

    /**
     * 获取Issuer
     */
    private String getIssuer() {
        return serverUrl;
    }
}
