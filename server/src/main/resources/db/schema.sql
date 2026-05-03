-- =============================================
-- OAuth2 认证中心数据库表结构 (通用: H2/MySQL/PostgreSQL)
-- 规则: 不使用TINYINT(用INTEGER), 不使用内联COMMENT, 不使用数据库特有语法
-- =============================================

-- 组织表
CREATE TABLE IF NOT EXISTS sys_organization (
    id              BIGINT          PRIMARY KEY,
    name            VARCHAR(100)    NOT NULL,
    code            VARCHAR(50)     NOT NULL UNIQUE,
    parent_id       BIGINT          DEFAULT NULL,
    status          INTEGER         DEFAULT 1,      -- 0=禁用, 1=正常
    sort_order      INTEGER         DEFAULT 0,
    deleted         INTEGER         DEFAULT 0,      -- 逻辑删除: 0=正常, 1=已删除
    created_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id              BIGINT          PRIMARY KEY,
    username        VARCHAR(50)     NOT NULL UNIQUE,
    password        VARCHAR(255),
    nickname        VARCHAR(50),
    real_name       VARCHAR(50),
    avatar          VARCHAR(255),
    gender          INTEGER         DEFAULT 0,      -- 0=未知, 1=男, 2=女
    birthday        DATE,
    bio             VARCHAR(500),
    status          INTEGER         DEFAULT 1,      -- 0=禁用, 1=正常, 2=锁定
    email_verified  BOOLEAN         DEFAULT FALSE,
    phone_verified  BOOLEAN         DEFAULT FALSE,
    last_login_at   TIMESTAMP,
    last_login_ip   VARCHAR(50),
    deleted         INTEGER         DEFAULT 0,      -- 逻辑删除: 0=正常, 1=已删除
    created_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);

-- 用户凭证表
CREATE TABLE IF NOT EXISTS sys_user_credential (
    id              BIGINT          PRIMARY KEY,
    user_id         BIGINT          NOT NULL,
    credential_type VARCHAR(50)     NOT NULL,       -- password, email, phone, social_github, social_gitee, social_qq
    credential_key  VARCHAR(255)    NOT NULL,       -- 邮箱地址/手机号/social_uid
    credential_value VARCHAR(255),                  -- 密码hash/null
    verified        BOOLEAN         DEFAULT FALSE,
    primary_flag    BOOLEAN         DEFAULT FALSE,
    deleted         INTEGER         DEFAULT 0,      -- 逻辑删除: 0=正常, 1=已删除
    created_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(credential_type, credential_key)
);

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id              BIGINT          PRIMARY KEY,
    code            VARCHAR(50)     NOT NULL,
    name            VARCHAR(100)    NOT NULL,
    description     VARCHAR(255),
    org_id          BIGINT          DEFAULT NULL,   -- NULL=系统角色, 非NULL=组织角色
    is_system       BOOLEAN         DEFAULT FALSE,  -- 是否系统内置
    sort_order      INTEGER         DEFAULT 0,
    status          INTEGER         DEFAULT 1,      -- 0=禁用, 1=正常
    deleted         INTEGER         DEFAULT 0,      -- 逻辑删除: 0=正常, 1=已删除
    created_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(code, org_id)
);

-- 权限表
CREATE TABLE IF NOT EXISTS sys_permission (
    id              BIGINT          PRIMARY KEY,
    code            VARCHAR(100)    NOT NULL UNIQUE,
    name            VARCHAR(100)    NOT NULL,
    description     VARCHAR(255),
    type            INTEGER         NOT NULL,       -- 1=菜单, 2=按钮, 3=API
    parent_id       BIGINT          DEFAULT NULL,
    path            VARCHAR(255),                   -- 菜单路由路径
    icon            VARCHAR(100),                   -- 菜单图标
    api_pattern     VARCHAR(255),                   -- API模式, 如 GET:/v1/users/**
    sort_order      INTEGER         DEFAULT 0,
    deleted         INTEGER         DEFAULT 0,      -- 逻辑删除: 0=正常, 1=已删除
    created_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    id              BIGINT          PRIMARY KEY,
    user_id         BIGINT          NOT NULL,
    role_id         BIGINT          NOT NULL,
    org_id          BIGINT          DEFAULT NULL,
    created_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, role_id, org_id)
);

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS sys_role_permission (
    id              BIGINT          PRIMARY KEY,
    role_id         BIGINT          NOT NULL,
    permission_id   BIGINT          NOT NULL,
    created_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (role_id, permission_id)
);

-- 用户组织关联表
CREATE TABLE IF NOT EXISTS sys_user_organization (
    id              BIGINT          PRIMARY KEY,
    user_id         BIGINT          NOT NULL,
    org_id          BIGINT          NOT NULL,
    role_id         BIGINT,                         -- 在该组织下的主角色
    status          INTEGER         DEFAULT 1,      -- 0=待审批, 1=正常, 2=拒绝
    joined_at       TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, org_id)
);

-- 索引
CREATE INDEX IF NOT EXISTS idx_user_credential_user_id ON sys_user_credential(user_id);
CREATE INDEX IF NOT EXISTS idx_user_role_user_id ON sys_user_role(user_id);
CREATE INDEX IF NOT EXISTS idx_role_permission_role_id ON sys_role_permission(role_id);
CREATE INDEX IF NOT EXISTS idx_user_org_user_id ON sys_user_organization(user_id);
CREATE INDEX IF NOT EXISTS idx_user_org_org_id ON sys_user_organization(org_id);

-- =============================================
-- OAuth2 相关表
-- =============================================

-- OAuth2 客户端表
CREATE TABLE IF NOT EXISTS oauth2_client (
    id              BIGINT          PRIMARY KEY,
    client_id       VARCHAR(100)    NOT NULL UNIQUE,
    client_secret   VARCHAR(255),
    client_name     VARCHAR(100)    NOT NULL,
    client_type     INTEGER         DEFAULT 1,      -- 1=PUBLIC, 2=CONFIDENTIAL, 3=BEARER_ONLY
    redirect_uris   TEXT,                           -- JSON数组，允许的回调地址
    allowed_scopes  TEXT,                           -- JSON数组，允许的scope
    owner_id        BIGINT,                         -- 创建者用户ID
    access_token_ttl INTEGER        DEFAULT 3600,   -- Access Token有效期(秒)
    refresh_token_ttl INTEGER       DEFAULT 86400,  -- Refresh Token有效期(秒)
    status          INTEGER         DEFAULT 1,      -- 0=禁用, 1=正常
    deleted         INTEGER         DEFAULT 0,      -- 逻辑删除: 0=正常, 1=已删除
    created_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);

-- OAuth2 Scope表
CREATE TABLE IF NOT EXISTS oauth2_scope (
    id              BIGINT          PRIMARY KEY,
    code            VARCHAR(50)     NOT NULL UNIQUE,
    name            VARCHAR(100)    NOT NULL,
    description     VARCHAR(255),
    is_default      BOOLEAN         DEFAULT FALSE,  -- 是否默认授予
    deleted         INTEGER         DEFAULT 0,      -- 逻辑删除: 0=正常, 1=已删除
    created_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);

-- OAuth2 客户端Scope关联表
CREATE TABLE IF NOT EXISTS oauth2_client_scope (
    client_id       VARCHAR(100)    NOT NULL,
    scope_id        BIGINT          NOT NULL,
    created_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (client_id, scope_id)
);

-- OAuth2 授权码表
CREATE TABLE IF NOT EXISTS oauth2_authorization_code (
    id              BIGINT          PRIMARY KEY,
    code            VARCHAR(100)    NOT NULL UNIQUE,
    client_id       VARCHAR(100)    NOT NULL,
    user_id         BIGINT          NOT NULL,
    redirect_uri    VARCHAR(500),
    scope           VARCHAR(500),                   -- 空格分隔的scope
    state           VARCHAR(500),
    expires_at      TIMESTAMP       NOT NULL,
    created_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);

-- OAuth2 Access Token表
CREATE TABLE IF NOT EXISTS oauth2_access_token (
    id              BIGINT          PRIMARY KEY,
    token           VARCHAR(500)    NOT NULL UNIQUE,
    client_id       VARCHAR(100)    NOT NULL,
    user_id         BIGINT,
    scope           VARCHAR(500),                   -- 空格分隔的scope
    token_type      VARCHAR(50)     DEFAULT 'Bearer',
    expires_at      TIMESTAMP       NOT NULL,
    created_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);

-- OAuth2 Refresh Token表
CREATE TABLE IF NOT EXISTS oauth2_refresh_token (
    id              BIGINT          PRIMARY KEY,
    token           VARCHAR(500)    NOT NULL UNIQUE,
    access_token_id BIGINT,
    client_id       VARCHAR(100)    NOT NULL,
    user_id         BIGINT,
    scope           VARCHAR(500),                   -- 空格分隔的scope
    expires_at      TIMESTAMP       NOT NULL,
    created_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);

-- 社交账号绑定表
CREATE TABLE IF NOT EXISTS oauth2_social_account (
    id              BIGINT          PRIMARY KEY,
    user_id         BIGINT          NOT NULL,
    provider        VARCHAR(50)     NOT NULL,       -- github, gitee, qq
    provider_uid    VARCHAR(100)    NOT NULL,
    provider_name   VARCHAR(100),
    provider_avatar VARCHAR(255),
    provider_email  VARCHAR(100),
    deleted         INTEGER         DEFAULT 0,      -- 逻辑删除: 0=正常, 1=已删除
    created_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(provider, provider_uid)
);

-- OAuth2索引
CREATE INDEX IF NOT EXISTS idx_auth_code_client ON oauth2_authorization_code(client_id);
CREATE INDEX IF NOT EXISTS idx_auth_code_user ON oauth2_authorization_code(user_id);
CREATE INDEX IF NOT EXISTS idx_access_token_client ON oauth2_access_token(client_id);
CREATE INDEX IF NOT EXISTS idx_access_token_user ON oauth2_access_token(user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_token_client ON oauth2_refresh_token(client_id);
CREATE INDEX IF NOT EXISTS idx_refresh_token_user ON oauth2_refresh_token(user_id);
CREATE INDEX IF NOT EXISTS idx_social_account_user ON oauth2_social_account(user_id);

-- =============================================
-- 配置表
-- =============================================

-- 认证方式配置表
CREATE TABLE IF NOT EXISTS sys_auth_config (
    id              BIGINT          PRIMARY KEY,
    auth_type       VARCHAR(50)     NOT NULL UNIQUE, -- password, email, phone, social_github, social_gitee, social_qq
    enabled         BOOLEAN         DEFAULT FALSE,
    config          TEXT,                            -- JSON配置
    description     VARCHAR(255),
    deleted         INTEGER         DEFAULT 0,      -- 逻辑删除: 0=正常, 1=已删除
    updated_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    updated_by      BIGINT
);

-- 验证码配置表
CREATE TABLE IF NOT EXISTS sys_captcha_config (
    id              BIGINT          PRIMARY KEY,
    enabled         BOOLEAN         DEFAULT TRUE,
    captcha_type    VARCHAR(20)     DEFAULT 'svg',
    length          INTEGER         DEFAULT 4,
    expire_seconds  INTEGER         DEFAULT 300,
    case_sensitive  BOOLEAN         DEFAULT FALSE,
    noise_lines     INTEGER         DEFAULT 5,
    noise_dots      INTEGER         DEFAULT 50,
    rotation        BOOLEAN         DEFAULT TRUE,
    wave            BOOLEAN         DEFAULT TRUE,
    deleted         INTEGER         DEFAULT 0,      -- 逻辑删除: 0=正常, 1=已删除
    updated_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    updated_by      BIGINT
);

-- 验证码端点配置表
CREATE TABLE IF NOT EXISTS sys_captcha_endpoint_config (
    id              BIGINT          PRIMARY KEY,
    endpoint        VARCHAR(100)    NOT NULL UNIQUE, -- POST:/v1/auth/login
    enabled         BOOLEAN         DEFAULT TRUE,
    description     VARCHAR(200),
    deleted         INTEGER         DEFAULT 0       -- 逻辑删除: 0=正常, 1=已删除
);

-- 加密配置表
CREATE TABLE IF NOT EXISTS sys_crypto_config (
    id              BIGINT          PRIMARY KEY,
    enabled         BOOLEAN         DEFAULT TRUE,
    rsa_key_size    INTEGER         DEFAULT 2048,
    rsa_public_key  TEXT,                            -- 公钥PEM
    rsa_private_key TEXT,                            -- 私钥PEM(加密存储)
    key_expire_days INTEGER         DEFAULT 90,
    deleted         INTEGER         DEFAULT 0,      -- 逻辑删除: 0=正常, 1=已删除
    created_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);

-- 加密端点配置表
CREATE TABLE IF NOT EXISTS sys_crypto_endpoint_config (
    id              BIGINT          PRIMARY KEY,
    endpoint        VARCHAR(100)    NOT NULL UNIQUE, -- POST:/v1/auth/login
    enabled         BOOLEAN         DEFAULT TRUE,
    encrypted_fields TEXT,                           -- JSON数组，需要加密的字段
    deleted         INTEGER         DEFAULT 0       -- 逻辑删除: 0=正常, 1=已删除
);

-- 邮件模板表
CREATE TABLE IF NOT EXISTS sys_email_template (
    id              BIGINT          PRIMARY KEY,
    code            VARCHAR(50)     NOT NULL UNIQUE, -- register_verify, login_code
    name            VARCHAR(100)    NOT NULL,
    subject         VARCHAR(200)    NOT NULL,        -- 邮件主题(支持变量)
    content         TEXT            NOT NULL,        -- HTML模板
    variables       TEXT,                            -- 可用变量说明(JSON)
    deleted         INTEGER         DEFAULT 0,      -- 逻辑删除: 0=正常, 1=已删除
    created_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);

-- 社交登录提供商配置表
CREATE TABLE IF NOT EXISTS sys_social_provider_config (
    id                      BIGINT          PRIMARY KEY,
    provider                VARCHAR(50)     NOT NULL UNIQUE,    -- github, gitee, qq, wechat...
    display_name            VARCHAR(100)    NOT NULL,            -- 显示名称
    enabled                 BOOLEAN         DEFAULT FALSE,
    client_id               VARCHAR(255)    NOT NULL,
    client_secret           VARCHAR(255)    NOT NULL,
    authorize_url           VARCHAR(500)    NOT NULL,            -- 授权URL
    token_url               VARCHAR(500)    NOT NULL,            -- Token交换URL
    token_method            VARCHAR(10)     DEFAULT 'POST',      -- POST or GET
    token_response_form_encoded BOOLEAN     DEFAULT FALSE,       -- true=URL编码格式(GitHub), false=JSON
    token_field_name        VARCHAR(50)     DEFAULT 'access_token',
    scope                   VARCHAR(255),                        -- 请求的scope
    userinfo_url            VARCHAR(500)    NOT NULL,            -- 用户信息URL
    userinfo_method         VARCHAR(10)     DEFAULT 'GET',       -- GET or POST
    userinfo_auth_style     VARCHAR(20)     DEFAULT 'header',    -- header(Bearer)/query(参数)
    user_id_field           VARCHAR(100)    DEFAULT 'id',        -- 用户ID字段(支持点号路径: data.id)
    nickname_field          VARCHAR(100)    DEFAULT 'login',     -- 昵称字段
    avatar_field            VARCHAR(100)    DEFAULT 'avatar_url',-- 头像字段
    email_field             VARCHAR(100)    DEFAULT 'email',     -- 邮箱字段
    openid_enabled          BOOLEAN         DEFAULT FALSE,       -- true=启用OpenID步骤(QQ等)
    openid_url              VARCHAR(500),                        -- OpenID获取URL
    openid_field_name       VARCHAR(50)     DEFAULT 'openid',    -- OpenID参数名
    openid_response_field   VARCHAR(100)    DEFAULT 'openid',    -- OpenID响应字段
    extra_authorize_params  TEXT,                                -- 额外授权参数(JSON)
    default_redirect_uri    VARCHAR(500),                        -- 默认回调地址
    sort_order              INTEGER         DEFAULT 0,
    deleted                 INTEGER         DEFAULT 0,           -- 逻辑删除
    created_at              TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);

-- SMTP配置表
CREATE TABLE IF NOT EXISTS sys_smtp_config (
    id              BIGINT          PRIMARY KEY,
    host            VARCHAR(100)    NOT NULL,
    port            INTEGER         DEFAULT 465,
    username        VARCHAR(100)    NOT NULL,
    password        VARCHAR(255)    NOT NULL,        -- 加密存储
    from_address    VARCHAR(100)    NOT NULL,
    from_name       VARCHAR(100)    DEFAULT 'OAuth认证中心',
    ssl_enabled     BOOLEAN         DEFAULT TRUE,
    enabled         BOOLEAN         DEFAULT FALSE,
    deleted         INTEGER         DEFAULT 0,      -- 逻辑删除: 0=正常, 1=已删除
    updated_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- 审计日志表
-- =============================================

CREATE TABLE IF NOT EXISTS sys_audit_log (
    id              BIGINT          PRIMARY KEY,
    trace_id        VARCHAR(64),                    -- 链路追踪ID
    user_id         BIGINT,                         -- 操作人
    username        VARCHAR(50),                    -- 操作人用户名
    org_id          BIGINT,                         -- 操作人所在组织
    action          VARCHAR(50)     NOT NULL,       -- 操作类型
    resource_type   VARCHAR(50),                    -- 资源类型
    resource_id     VARCHAR(100),                   -- 资源ID
    detail          TEXT,                            -- 操作详情(JSON)
    ip              VARCHAR(50),
    user_agent      VARCHAR(500),
    status          INTEGER         DEFAULT 1,      -- 1=成功, 0=失败
    error_msg       VARCHAR(500),
    created_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);

-- 审计日志索引
CREATE INDEX IF NOT EXISTS idx_audit_user_id ON sys_audit_log(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_action ON sys_audit_log(action);
CREATE INDEX IF NOT EXISTS idx_audit_created ON sys_audit_log(created_at);
CREATE INDEX IF NOT EXISTS idx_audit_resource ON sys_audit_log(resource_type, resource_id);
