package org.codelin.oauth.oauth.authorization.domain.model;

import com.mybatisflex.core.query.QueryColumn;

/**
 * 授权模块表列定义
 */
public class TableDefs {

    public static final OAuth2ClientTable OAUTH2_CLIENT = new OAuth2ClientTable();

    public static class OAuth2ClientTable {
        public final QueryColumn ID = new QueryColumn("id");
        public final QueryColumn CLIENT_ID = new QueryColumn("client_id");
        public final QueryColumn CLIENT_SECRET = new QueryColumn("client_secret");
        public final QueryColumn CLIENT_NAME = new QueryColumn("client_name");
        public final QueryColumn CLIENT_TYPE = new QueryColumn("client_type");
        public final QueryColumn REDIRECT_URIS = new QueryColumn("redirect_uris");
        public final QueryColumn ALLOWED_SCOPES = new QueryColumn("allowed_scopes");
        public final QueryColumn OWNER_ID = new QueryColumn("owner_id");
        public final QueryColumn ACCESS_TOKEN_TTL = new QueryColumn("access_token_ttl");
        public final QueryColumn REFRESH_TOKEN_TTL = new QueryColumn("refresh_token_ttl");
        public final QueryColumn STATUS = new QueryColumn("status");
        public final QueryColumn CREATED_AT = new QueryColumn("created_at");
        public final QueryColumn UPDATED_AT = new QueryColumn("updated_at");
    }
}
