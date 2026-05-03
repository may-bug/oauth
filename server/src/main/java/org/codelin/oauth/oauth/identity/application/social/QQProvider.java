package org.codelin.oauth.oauth.identity.application.social;

import lombok.extern.slf4j.Slf4j;
import org.codelin.oauth.oauth.common.web.BizException;
import org.codelin.oauth.oauth.identity.domain.model.SocialProviderConfig;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

/**
 * QQ社交登录提供商
 * <p>
 * 特点：
 * 1. GET方式换取token
 * 2. 三步流程：code→token, token→openid, token+openid→userinfo
 * 3. 额外授权参数 display=pc
 * 4. 用户信息需要 oauth_consumer_key + openid 参数
 */
@Slf4j
@Component
public class QQProvider extends AbstractSocialProvider {

    public QQProvider(SocialProviderService providerService,
                      ObjectMapper objectMapper,
                      RestTemplate restTemplate) {
        super(providerService, objectMapper, restTemplate);
    }

    @Override
    public String getProvider() {
        return "qq";
    }

    @Override
    protected void appendExtraAuthorizeParams(StringBuilder url, SocialProviderConfig config) {
        if (config.getExtraAuthorizeParams() != null && !config.getExtraAuthorizeParams().isEmpty()) {
            try {
                Map<String, String> extra = objectMapper.readValue(
                        config.getExtraAuthorizeParams(),
                        objectMapper.getTypeFactory().constructMapType(Map.class, String.class, String.class));
                extra.forEach((k, v) -> url.append("&").append(k).append("=").append(encode(v)));
            } catch (Exception e) {
                log.warn("Failed to parse extra_authorize_params: {}", config.getExtraAuthorizeParams());
            }
        }
    }

    @Override
    protected String exchangeCodeForToken(SocialProviderConfig config,
                                           String code, String redirectUri) {
        try {
            // QQ: GET方式换token
            String url = String.format(
                    "%s?grant_type=authorization_code&code=%s&client_id=%s&client_secret=%s&redirect_uri=%s&fmt=json",
                    config.getTokenUrl(), code, config.getClientId(), config.getClientSecret(), redirectUri);

            String response = restTemplate.getForObject(url, String.class);
            JsonNode node = objectMapper.readTree(response);

            JsonNode tokenNode = node.get("access_token");
            if (tokenNode == null || tokenNode.isNull()) {
                log.error("QQ token response missing access_token: {}", response);
                throw BizException.badRequest("error.auth.login-failed");
            }
            return tokenNode.asString();
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("QQ token exchange failed", e);
            throw BizException.badRequest("error.auth.login-failed");
        }
    }

    @Override
    protected String fetchOpenId(SocialProviderConfig config, String accessToken) {
        // QQ三步流程：用token获取openid
        try {
            String url = config.getOpenidUrl() + "?access_token=" + encode(accessToken) + "&fmt=json";
            String response = restTemplate.getForObject(url, String.class);
            JsonNode node = objectMapper.readTree(response);

            JsonNode openidNode = node.get("openid");
            if (openidNode == null || openidNode.isNull()) {
                log.error("QQ openid response missing openid: {}", response);
                throw BizException.badRequest("error.auth.login-failed");
            }
            return openidNode.asString();
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("QQ openid fetch failed", e);
            throw BizException.badRequest("error.auth.login-failed");
        }
    }

    @Override
    protected SocialUserInfo fetchUserInfo(SocialProviderConfig config,
                                            String accessToken, String openId) {
        try {
            // QQ: query参数传 token + oauth_consumer_key + openid
            String url = String.format("%s?access_token=%s&oauth_consumer_key=%s&openid=%s",
                    config.getUserinfoUrl(), encode(accessToken),
                    encode(config.getClientId()), encode(openId));

            String response = restTemplate.getForObject(url, String.class);
            JsonNode userNode = objectMapper.readTree(response);

            SocialUserInfo userInfo = new SocialUserInfo();
            userInfo.setProvider(getProvider());
            userInfo.setProviderUid(openId); // QQ用openid作为用户标识
            userInfo.setNickname(userNode.get("nickname").asText());
            userInfo.setAvatar(userNode.get("figureurl_qq_2").asText());
            // QQ不返回邮箱
            userInfo.setEmail(null);

            return userInfo;
        } catch (Exception e) {
            log.error("QQ user info fetch failed", e);
            throw BizException.badRequest("error.auth.login-failed");
        }
    }
}
