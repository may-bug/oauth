package org.codelin.oauth.oauth.identity.application.social;

import lombok.extern.slf4j.Slf4j;
import org.codelin.oauth.oauth.common.web.BizException;
import org.codelin.oauth.oauth.identity.domain.model.SocialProviderConfig;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 社交登录提供商抽象基类 - 从DB读取配置，提供公共方法
 * <p>
 * 子类只需覆盖各自特殊的流程方法
 */
@Slf4j
public abstract class AbstractSocialProvider implements SocialAuthProvider {

    protected final SocialProviderService providerService;
    protected final ObjectMapper objectMapper;
    protected final RestTemplate restTemplate;

    protected AbstractSocialProvider(SocialProviderService providerService,
                                     ObjectMapper objectMapper,
                                     RestTemplate restTemplate) {
        this.providerService = providerService;
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
    }

    /**
     * 获取当前提供商的DB配置
     */
    protected SocialProviderConfig getConfig() {
        return providerService.getConfig(getProvider());
    }

    // ========== 默认实现：标准OAuth2流程 ==========

    @Override
    public String getAuthorizationUrl(String redirectUri, String state) {
        SocialProviderConfig config = getConfig();
        StringBuilder url = new StringBuilder(config.getAuthorizeUrl());
        url.append("?client_id=").append(encode(config.getClientId()))
                .append("&redirect_uri=").append(encode(redirectUri))
                .append("&state=").append(encode(state))
                .append("&response_type=code");
        if (config.getScope() != null && !config.getScope().isEmpty()) {
            url.append("&scope=").append(encode(config.getScope()));
        }
        appendExtraAuthorizeParams(url, config);
        return url.toString();
    }

    @Override
    public SocialUserInfo getUserInfo(String code, String redirectUri) {
        SocialProviderConfig config = getConfig();
        try {
            // 1. 换取token
            String accessToken = exchangeCodeForToken(config, code, redirectUri);

            // 2. 获取OpenID（如需要）
            String openId = fetchOpenId(config, accessToken);

            // 3. 获取用户信息
            return fetchUserInfo(config, accessToken, openId);
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to get user info, provider: {}", getProvider(), e);
            throw BizException.badRequest("error.auth.login-failed");
        }
    }

    // ========== 子类可覆盖的方法 ==========

    /**
     * 用code换取access_token - 子类覆盖以处理特殊逻辑
     */
    protected abstract String exchangeCodeForToken(SocialProviderConfig config,
                                                    String code, String redirectUri);

    /**
     * 获取OpenID（QQ等三步流程）- 默认返回null，子类覆盖
     */
    protected String fetchOpenId(SocialProviderConfig config, String accessToken) {
        return null;
    }

    /**
     * 获取用户信息 - 子类覆盖以处理特殊逻辑
     */
    protected abstract SocialUserInfo fetchUserInfo(SocialProviderConfig config,
                                                     String accessToken, String openId);

    /**
     * 追加额外授权参数 - 子类覆盖
     */
    protected void appendExtraAuthorizeParams(StringBuilder url, SocialProviderConfig config) {
        // 默认不做任何事
    }

    // ========== 公共工具方法 ==========

    protected String encode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return value;
        }
    }

    /**
     * 解析点号分隔的JSON路径（如 "data.id"）
     */
    protected JsonNode resolvePath(JsonNode node, String path) {
        if (path == null || path.isEmpty()) {
            return node;
        }
        String[] parts = path.split("\\.");
        JsonNode current = node;
        for (String part : parts) {
            if (current == null || current.isNull()) {
                return null;
            }
            current = current.get(part);
        }
        return current;
    }

    /**
     * 获取JSON节点的文本值
     */
    protected String getTextValue(JsonNode node, String path, String defaultValue) {
        JsonNode value = resolvePath(node, path);
        if (value == null || value.isNull()) {
            return defaultValue;
        }
        return value.asText();
    }
}
