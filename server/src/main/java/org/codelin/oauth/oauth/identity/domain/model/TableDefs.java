package org.codelin.oauth.oauth.identity.domain.model;

import com.mybatisflex.core.query.QueryColumn;

/**
 * 表列定义 - 静态变量方式，遵循MyBatis-Flex推荐做法
 */
public class TableDefs {

    public static final UserTable USER = new UserTable();
    public static final UserCredentialTable USER_CREDENTIAL = new UserCredentialTable();
    public static final SocialProviderConfigTable SOCIAL_PROVIDER_CONFIG = new SocialProviderConfigTable();
    public static final AuthConfigTable AUTH_CONFIG = new AuthConfigTable();

    public static class UserTable {
        public final QueryColumn ID = new QueryColumn("id");
        public final QueryColumn USERNAME = new QueryColumn("username");
        public final QueryColumn PASSWORD = new QueryColumn("password");
        public final QueryColumn NICKNAME = new QueryColumn("nickname");
        public final QueryColumn REAL_NAME = new QueryColumn("real_name");
        public final QueryColumn AVATAR = new QueryColumn("avatar");
        public final QueryColumn GENDER = new QueryColumn("gender");
        public final QueryColumn BIRTHDAY = new QueryColumn("birthday");
        public final QueryColumn BIO = new QueryColumn("bio");
        public final QueryColumn STATUS = new QueryColumn("status");
        public final QueryColumn EMAIL_VERIFIED = new QueryColumn("email_verified");
        public final QueryColumn PHONE_VERIFIED = new QueryColumn("phone_verified");
        public final QueryColumn LAST_LOGIN_AT = new QueryColumn("last_login_at");
        public final QueryColumn LAST_LOGIN_IP = new QueryColumn("last_login_ip");
        public final QueryColumn CREATED_AT = new QueryColumn("created_at");
        public final QueryColumn UPDATED_AT = new QueryColumn("updated_at");
    }

    public static class UserCredentialTable {
        public final QueryColumn ID = new QueryColumn("id");
        public final QueryColumn USER_ID = new QueryColumn("user_id");
        public final QueryColumn CREDENTIAL_TYPE = new QueryColumn("credential_type");
        public final QueryColumn CREDENTIAL_KEY = new QueryColumn("credential_key");
        public final QueryColumn CREDENTIAL_VALUE = new QueryColumn("credential_value");
        public final QueryColumn VERIFIED = new QueryColumn("verified");
        public final QueryColumn PRIMARY_FLAG = new QueryColumn("primary_flag");
        public final QueryColumn CREATED_AT = new QueryColumn("created_at");
    }

    public static class AuthConfigTable {
        public final QueryColumn ID = new QueryColumn("id");
        public final QueryColumn AUTH_TYPE = new QueryColumn("auth_type");
        public final QueryColumn ENABLED = new QueryColumn("enabled");
    }

    public static class SocialProviderConfigTable {
        public final QueryColumn ID = new QueryColumn("id");
        public final QueryColumn PROVIDER = new QueryColumn("provider");
        public final QueryColumn DISPLAY_NAME = new QueryColumn("display_name");
        public final QueryColumn ENABLED = new QueryColumn("enabled");
        public final QueryColumn CLIENT_ID = new QueryColumn("client_id");
        public final QueryColumn CLIENT_SECRET = new QueryColumn("client_secret");
        public final QueryColumn AUTHORIZE_URL = new QueryColumn("authorize_url");
        public final QueryColumn TOKEN_URL = new QueryColumn("token_url");
        public final QueryColumn TOKEN_METHOD = new QueryColumn("token_method");
        public final QueryColumn TOKEN_RESPONSE_FORM_ENCODED = new QueryColumn("token_response_form_encoded");
        public final QueryColumn TOKEN_FIELD_NAME = new QueryColumn("token_field_name");
        public final QueryColumn SCOPE = new QueryColumn("scope");
        public final QueryColumn USERINFO_URL = new QueryColumn("userinfo_url");
        public final QueryColumn USERINFO_METHOD = new QueryColumn("userinfo_method");
        public final QueryColumn USERINFO_AUTH_STYLE = new QueryColumn("userinfo_auth_style");
        public final QueryColumn USER_ID_FIELD = new QueryColumn("user_id_field");
        public final QueryColumn NICKNAME_FIELD = new QueryColumn("nickname_field");
        public final QueryColumn AVATAR_FIELD = new QueryColumn("avatar_field");
        public final QueryColumn EMAIL_FIELD = new QueryColumn("email_field");
        public final QueryColumn OPENID_ENABLED = new QueryColumn("openid_enabled");
        public final QueryColumn OPENID_URL = new QueryColumn("openid_url");
        public final QueryColumn OPENID_FIELD_NAME = new QueryColumn("openid_field_name");
        public final QueryColumn OPENID_RESPONSE_FIELD = new QueryColumn("openid_response_field");
        public final QueryColumn EXTRA_AUTHORIZE_PARAMS = new QueryColumn("extra_authorize_params");
        public final QueryColumn DEFAULT_REDIRECT_URI = new QueryColumn("default_redirect_uri");
        public final QueryColumn SORT_ORDER = new QueryColumn("sort_order");
        public final QueryColumn DELETED = new QueryColumn("deleted");
        public final QueryColumn CREATED_AT = new QueryColumn("created_at");
        public final QueryColumn UPDATED_AT = new QueryColumn("updated_at");
    }
}
