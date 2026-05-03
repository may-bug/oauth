package org.codelin.oauth.oauth.identity.application.social;

/**
 * 社交登录提供商接口
 */
public interface SocialAuthProvider {

    /**
     * 获取提供商名称
     */
    String getProvider();

    /**
     * 获取授权URL
     */
    String getAuthorizationUrl(String redirectUri, String state);

    /**
     * 用code换取用户信息
     */
    SocialUserInfo getUserInfo(String code, String redirectUri);

    /**
     * 社交用户信息
     */
    @lombok.Data
    class SocialUserInfo {
        private String provider;
        private String providerUid;
        private String nickname;
        private String avatar;
        private String email;
    }
}
