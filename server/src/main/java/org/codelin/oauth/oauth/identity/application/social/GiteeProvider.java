package org.codelin.oauth.oauth.identity.application.social;

import lombok.extern.slf4j.Slf4j;
import org.codelin.oauth.oauth.common.web.BizException;
import org.codelin.oauth.oauth.identity.domain.model.SocialProviderConfig;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Gitee社交登录提供商
 * <p>
 * 特点：标准OAuth2 POST换token（form body），query参数获取用户信息
 */
@Slf4j
@Component
public class GiteeProvider extends AbstractSocialProvider {

    public GiteeProvider(SocialProviderService providerService,
                         ObjectMapper objectMapper,
                         RestTemplate restTemplate) {
        super(providerService, objectMapper, restTemplate);
    }

    @Override
    public String getProvider() {
        return "gitee";
    }

    @Override
    protected String exchangeCodeForToken(SocialProviderConfig config,
                                           String code, String redirectUri) {
        try {
            // Gitee: POST with form body, response is JSON
            String body = String.format(
                    "grant_type=authorization_code&code=%s&client_id=%s&client_secret=%s&redirect_uri=%s",
                    code, config.getClientId(), config.getClientSecret(), redirectUri);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<String> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    config.getTokenUrl(), HttpMethod.POST, request, String.class);

            JsonNode node = objectMapper.readTree(response.getBody());
            JsonNode tokenNode = node.get("access_token");
            if (tokenNode == null || tokenNode.isNull()) {
                log.error("Gitee token response missing access_token: {}", response.getBody());
                throw BizException.badRequest("error.auth.login-failed");
            }
            return tokenNode.asText();
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("Gitee token exchange failed", e);
            throw BizException.badRequest("error.auth.login-failed");
        }
    }

    @Override
    protected SocialUserInfo fetchUserInfo(SocialProviderConfig config,
                                            String accessToken, String openId) {
        try {
            // Gitee: query参数传token
            String url = config.getUserinfoUrl() + "?access_token=" + encode(accessToken);
            String response = restTemplate.getForObject(url, String.class);
            JsonNode userNode = objectMapper.readTree(response);

            SocialUserInfo userInfo = new SocialUserInfo();
            userInfo.setProvider(getProvider());
            userInfo.setProviderUid(String.valueOf(userNode.get("id").asLong()));
            userInfo.setNickname(userNode.get("login").asText());
            userInfo.setAvatar(userNode.get("avatar_url").asText());
            userInfo.setEmail(userNode.has("email") && !userNode.get("email").isNull()
                    ? userNode.get("email").asText() : null);

            return userInfo;
        } catch (Exception e) {
            log.error("Gitee user info fetch failed", e);
            throw BizException.badRequest("error.auth.login-failed");
        }
    }
}
