package org.codelin.oauth.oauth.identity.application.social;

import lombok.extern.slf4j.Slf4j;
import org.codelin.oauth.oauth.common.web.BizException;
import org.codelin.oauth.oauth.identity.domain.model.SocialProviderConfig;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * GitHub社交登录提供商
 * <p>
 * 特点：token响应是URL编码格式（非JSON），使用Bearer Header获取用户信息
 */
@Slf4j
@Component
public class GitHubProvider extends AbstractSocialProvider {

    public GitHubProvider(SocialProviderService providerService,
                          ObjectMapper objectMapper,
                          RestTemplate restTemplate) {
        super(providerService, objectMapper, restTemplate);
    }

    @Override
    public String getProvider() {
        return "github";
    }

    @Override
    protected String exchangeCodeForToken(SocialProviderConfig config,
                                           String code, String redirectUri) {
        try {
            // GitHub: POST with query params, response is form-encoded
            String url = String.format("%s?client_id=%s&client_secret=%s&code=%s&redirect_uri=%s",
                    config.getTokenUrl(), config.getClientId(), config.getClientSecret(),
                    code, redirectUri);

            String response = restTemplate.postForObject(url, null, String.class);
            if (response == null || response.isEmpty()) {
                throw BizException.badRequest("error.auth.login-failed");
            }

            // 解析 form-encoded: access_token=xxx&scope=xxx&token_type=bearer
            for (String pair : response.split("&")) {
                String[] kv = pair.split("=", 2);
                if (kv.length == 2 && "access_token".equals(kv[0])) {
                    return kv[1];
                }
            }

            log.error("GitHub token response missing access_token: {}", response);
            throw BizException.badRequest("error.auth.login-failed");
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("GitHub token exchange failed", e);
            throw BizException.badRequest("error.auth.login-failed");
        }
    }

    @Override
    protected SocialUserInfo fetchUserInfo(SocialProviderConfig config,
                                            String accessToken, String openId) {
        try {
            // GitHub: Bearer Header
            var headers = new org.springframework.http.HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.set("Accept", "application/vnd.github.v3+json");

            var entity = new org.springframework.http.HttpEntity<>(headers);
            var response = restTemplate.exchange(config.getUserinfoUrl(),
                    org.springframework.http.HttpMethod.GET, entity, String.class);

            JsonNode userNode = objectMapper.readTree(response.getBody());

            SocialUserInfo userInfo = new SocialUserInfo();
            userInfo.setProvider(getProvider());
            userInfo.setProviderUid(String.valueOf(userNode.get("id").asLong()));
            userInfo.setNickname(userNode.get("login").asText());
            userInfo.setAvatar(userNode.get("avatar_url").asText());
            userInfo.setEmail(userNode.has("email") && !userNode.get("email").isNull()
                    ? userNode.get("email").asText() : null);

            return userInfo;
        } catch (Exception e) {
            log.error("GitHub user info fetch failed", e);
            throw BizException.badRequest("error.auth.login-failed");
        }
    }
}
