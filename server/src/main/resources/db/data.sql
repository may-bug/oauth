-- =============================================
-- 默认数据 (通用)
-- 配合 spring.sql.init.continue-on-error=true 使用
-- 重复插入错误会被忽略
-- =============================================

-- 默认管理员账号 (密码: Aa123456)
INSERT INTO sys_user (id, username, password, nickname, status, email_verified, phone_verified) VALUES
(1, 'admin', '$2a$10$7DbGfmwULIrKw5t7v/PRaexgxM1G7EuBnmTfAyNSyQwEQZcvLF0.W', '超级管理员', 1, FALSE, FALSE);

-- 管理员凭证
INSERT INTO sys_user_credential (id, user_id, credential_type, credential_key, credential_value, verified, primary_flag) VALUES
(1, 1, 'password', 'admin', '$2a$10$7DbGfmwULIrKw5t7v/PRaexgxM1G7EuBnmTfAyNSyQwEQZcvLF0.W', TRUE, TRUE);

-- 管理员角色分配
INSERT INTO sys_user_role (id, user_id, role_id) VALUES (1, 1, 1);

-- 系统角色
INSERT INTO sys_role (id, code, name, description, org_id, is_system, sort_order, status) VALUES
(1, 'SUPER_ADMIN', '超级管理员', '拥有所有权限', NULL, TRUE, 1, 1);
INSERT INTO sys_role (id, code, name, description, org_id, is_system, sort_order, status) VALUES
(2, 'ADMIN', '管理员', '系统管理权限', NULL, TRUE, 2, 1);
INSERT INTO sys_role (id, code, name, description, org_id, is_system, sort_order, status) VALUES
(3, 'USER', '普通用户', '基本操作权限', NULL, TRUE, 3, 1);
INSERT INTO sys_role (id, code, name, description, org_id, is_system, sort_order, status) VALUES
(4, 'GUEST', '访客', '只读权限', NULL, TRUE, 4, 1);

-- 系统权限 - 菜单
INSERT INTO sys_permission (id, code, name, description, type, parent_id, path, icon, sort_order) VALUES
(1, 'system', '系统管理', '系统管理菜单', 1, NULL, '/system', 'setting', 1);
INSERT INTO sys_permission (id, code, name, description, type, parent_id, path, icon, sort_order) VALUES
(2, 'system:user', '用户管理', '用户管理菜单', 1, 1, '/system/user', 'user', 1);
INSERT INTO sys_permission (id, code, name, description, type, parent_id, path, icon, sort_order) VALUES
(3, 'system:role', '角色管理', '角色管理菜单', 1, 1, '/system/role', 'role', 2);
INSERT INTO sys_permission (id, code, name, description, type, parent_id, path, icon, sort_order) VALUES
(4, 'system:org', '组织管理', '组织管理菜单', 1, 1, '/system/org', 'org', 3);
INSERT INTO sys_permission (id, code, name, description, type, parent_id, path, icon, sort_order) VALUES
(5, 'system:config', '系统配置', '系统配置菜单', 1, 1, '/system/config', 'config', 4);
INSERT INTO sys_permission (id, code, name, description, type, parent_id, path, icon, sort_order) VALUES
(6, 'oauth', 'OAuth管理', 'OAuth管理菜单', 1, NULL, '/oauth', 'oauth', 2);
INSERT INTO sys_permission (id, code, name, description, type, parent_id, path, icon, sort_order) VALUES
(7, 'oauth:client', '客户端管理', '客户端管理菜单', 1, 6, '/oauth/client', 'client', 1);
INSERT INTO sys_permission (id, code, name, description, type, parent_id, path, icon, sort_order) VALUES
(8, 'oauth:scope', 'Scope管理', 'Scope管理菜单', 1, 6, '/oauth/client', 'scope', 2);

-- 系统权限 - 按钮
INSERT INTO sys_permission (id, code, name, description, type, parent_id, sort_order) VALUES
(20, 'user:create', '创建用户', '创建用户按钮', 2, 2, 1);
INSERT INTO sys_permission (id, code, name, description, type, parent_id, sort_order) VALUES
(21, 'user:edit', '编辑用户', '编辑用户按钮', 2, 2, 2);
INSERT INTO sys_permission (id, code, name, description, type, parent_id, sort_order) VALUES
(22, 'user:delete', '删除用户', '删除用户按钮', 2, 2, 3);
INSERT INTO sys_permission (id, code, name, description, type, parent_id, sort_order) VALUES
(23, 'user:reset-password', '重置密码', '重置密码按钮', 2, 2, 4);
INSERT INTO sys_permission (id, code, name, description, type, parent_id, sort_order) VALUES
(30, 'role:create', '创建角色', '创建角色按钮', 2, 3, 1);
INSERT INTO sys_permission (id, code, name, description, type, parent_id, sort_order) VALUES
(31, 'role:edit', '编辑角色', '编辑角色按钮', 2, 3, 2);
INSERT INTO sys_permission (id, code, name, description, type, parent_id, sort_order) VALUES
(32, 'role:delete', '删除角色', '删除角色按钮', 2, 3, 3);

-- 系统权限 - API
INSERT INTO sys_permission (id, code, name, description, type, parent_id, api_pattern, sort_order) VALUES
(100, 'user:read', '查看用户', '查看用户信息', 3, 2, 'GET:/admin/users/**', 1);
INSERT INTO sys_permission (id, code, name, description, type, parent_id, api_pattern, sort_order) VALUES
(101, 'user:write', '修改用户', '修改用户信息', 3, 2, 'PUT:/admin/users/**', 2);
INSERT INTO sys_permission (id, code, name, description, type, parent_id, api_pattern, sort_order) VALUES
(102, 'user:manage', '管理用户', '用户管理权限', 3, 2, '*:/admin/users/**', 3);
INSERT INTO sys_permission (id, code, name, description, type, parent_id, api_pattern, sort_order) VALUES
(110, 'role:read', '查看角色', '查看角色信息', 3, 3, 'GET:/admin/roles/**', 1);
INSERT INTO sys_permission (id, code, name, description, type, parent_id, api_pattern, sort_order) VALUES
(111, 'role:write', '修改角色', '修改角色信息', 3, 3, 'PUT:/admin/roles/**', 2);
INSERT INTO sys_permission (id, code, name, description, type, parent_id, api_pattern, sort_order) VALUES
(112, 'role:manage', '管理角色', '角色管理权限', 3, 3, '*:/admin/roles/**', 3);
INSERT INTO sys_permission (id, code, name, description, type, parent_id, api_pattern, sort_order) VALUES
(120, 'org:read', '查看组织', '查看组织信息', 3, 4, 'GET:/admin/orgs/**', 1);
INSERT INTO sys_permission (id, code, name, description, type, parent_id, api_pattern, sort_order) VALUES
(121, 'org:write', '修改组织', '修改组织信息', 3, 4, 'PUT:/admin/orgs/**', 2);
INSERT INTO sys_permission (id, code, name, description, type, parent_id, api_pattern, sort_order) VALUES
(122, 'org:manage', '管理组织', '组织管理权限', 3, 4, '*:/admin/orgs/**', 3);
INSERT INTO sys_permission (id, code, name, description, type, parent_id, api_pattern, sort_order) VALUES
(130, 'config:read', '查看配置', '查看系统配置', 3, 5, 'GET:/admin/config/**', 1);
INSERT INTO sys_permission (id, code, name, description, type, parent_id, api_pattern, sort_order) VALUES
(131, 'config:write', '修改配置', '修改系统配置', 3, 5, 'PUT:/admin/config/**', 2);
INSERT INTO sys_permission (id, code, name, description, type, parent_id, api_pattern, sort_order) VALUES
(140, 'client:read', '查看客户端', '查看OAuth客户端', 3, 7, 'GET:/admin/clients/**', 1);
INSERT INTO sys_permission (id, code, name, description, type, parent_id, api_pattern, sort_order) VALUES
(141, 'client:write', '修改客户端', '修改OAuth客户端', 3, 7, 'PUT:/admin/clients/**', 2);
INSERT INTO sys_permission (id, code, name, description, type, parent_id, api_pattern, sort_order) VALUES
(142, 'client:manage', '管理客户端', '客户端管理权限', 3, 7, '*:/admin/clients/**', 3);
INSERT INTO sys_permission (id, code, name, description, type, parent_id, api_pattern, sort_order) VALUES
(150, 'audit:read', '查看日志', '查看审计日志', 3, NULL, 'GET:/admin/audit-logs/**', 1);

-- 超级管理员拥有所有权限
INSERT INTO sys_role_permission (id, role_id, permission_id) SELECT id + 100000, 1, id FROM sys_permission;

-- 管理员权限
INSERT INTO sys_role_permission (id, role_id, permission_id)
SELECT id + 200000, 2, id FROM sys_permission WHERE code IN (
    'system', 'system:user', 'system:role', 'system:org', 'system:config',
    'oauth', 'oauth:client', 'oauth:scope',
    'user:read', 'user:write', 'user:manage', 'user:create', 'user:edit',
    'role:read', 'role:write', 'role:manage', 'role:create', 'role:edit',
    'org:read', 'org:write', 'org:manage',
    'config:read', 'config:write',
    'client:read', 'client:write', 'client:manage',
    'audit:read'
);

-- 普通用户权限
INSERT INTO sys_role_permission (id, role_id, permission_id)
SELECT id + 300000, 3, id FROM sys_permission WHERE code IN ('user:read');

-- 认证方式默认配置
INSERT INTO sys_auth_config (id, auth_type, enabled, config, description) VALUES
(1, 'password', TRUE, '{"min_length":8, "require_uppercase":true, "require_lowercase":true, "require_digit":true}', '账号密码登录');
INSERT INTO sys_auth_config (id, auth_type, enabled, config, description) VALUES
(2, 'email', FALSE, '{}', '邮箱验证码登录');
INSERT INTO sys_auth_config (id, auth_type, enabled, config, description) VALUES
(3, 'phone', FALSE, '{}', '手机号验证码登录');
-- 社交登录提供商配置
INSERT INTO sys_social_provider_config (id, provider, display_name, enabled, client_id, client_secret,
    authorize_url, token_url, token_method, token_response_form_encoded, token_field_name,
    scope, userinfo_url, userinfo_method, userinfo_auth_style,
    user_id_field, nickname_field, avatar_field, email_field,
    openid_enabled, sort_order) VALUES
(100, 'github', 'GitHub', FALSE, '', '',
    'https://github.com/login/oauth/authorize',
    'https://github.com/login/oauth/access_token',
    'POST', TRUE, 'access_token',
    'user:email',
    'https://api.github.com/user', 'GET', 'header',
    'id', 'login', 'avatar_url', 'email',
    FALSE, 1);
INSERT INTO sys_social_provider_config (id, provider, display_name, enabled, client_id, client_secret,
    authorize_url, token_url, token_method, token_response_form_encoded, token_field_name,
    scope, userinfo_url, userinfo_method, userinfo_auth_style,
    user_id_field, nickname_field, avatar_field, email_field,
    openid_enabled, sort_order) VALUES
(101, 'gitee', 'Gitee', FALSE, '', '',
    'https://gitee.com/oauth/authorize',
    'https://gitee.com/oauth/token',
    'POST', FALSE, 'access_token',
    'user_info',
    'https://gitee.com/api/v5/user', 'GET', 'query',
    'id', 'login', 'avatar_url', 'email',
    FALSE, 2);
INSERT INTO sys_social_provider_config (id, provider, display_name, enabled, client_id, client_secret,
    authorize_url, token_url, token_method, token_response_form_encoded, token_field_name,
    scope, userinfo_url, userinfo_method, userinfo_auth_style,
    user_id_field, nickname_field, avatar_field, email_field,
    openid_enabled, openid_url, openid_field_name, openid_response_field,
    extra_authorize_params, sort_order) VALUES
(102, 'qq', 'QQ', FALSE, '', '',
    'https://graph.qq.com/oauth2.0/authorize',
    'https://graph.qq.com/oauth2.0/token',
    'GET', FALSE, 'access_token',
    'get_user_info',
    'https://graph.qq.com/user/get_user_info', 'GET', 'query',
    'openid', 'nickname', 'figureurl_qq_2', '',
    TRUE, 'https://graph.qq.com/oauth2.0/me', 'openid', 'openid',
    '{"display":"pc"}', 3);

-- 验证码默认配置
INSERT INTO sys_captcha_config (id, enabled, captcha_type, length, expire_seconds, case_sensitive, noise_lines, noise_dots, rotation, wave) VALUES
(1, TRUE, 'svg', 4, 300, FALSE, 5, 50, TRUE, TRUE);

-- 需要验证码的端点
INSERT INTO sys_captcha_endpoint_config (id, endpoint, enabled, description) VALUES
(1, 'POST:/auth/login', TRUE, '登录');
INSERT INTO sys_captcha_endpoint_config (id, endpoint, enabled, description) VALUES
(2, 'POST:/auth/register', TRUE, '注册');
INSERT INTO sys_captcha_endpoint_config (id, endpoint, enabled, description) VALUES
(3, 'POST:/auth/login/email/send', TRUE, '发送邮箱验证码');

-- 加密默认配置
INSERT INTO sys_crypto_config (id, enabled, rsa_key_size, key_expire_days) VALUES
(1, TRUE, 2048, 90);

-- 需要加密的端点
INSERT INTO sys_crypto_endpoint_config (id, endpoint, enabled, encrypted_fields) VALUES
(1, 'POST:/auth/login', TRUE, '["password","captcha"]');
INSERT INTO sys_crypto_endpoint_config (id, endpoint, enabled, encrypted_fields) VALUES
(2, 'POST:/auth/register', TRUE, '["password"]');
INSERT INTO sys_crypto_endpoint_config (id, endpoint, enabled, encrypted_fields) VALUES
(3, 'POST:/profile/password', TRUE, '["oldPassword","newPassword"]');

-- OAuth2默认Scope
INSERT INTO oauth2_scope (id, code, name, description, is_default) VALUES
(1, 'openid', 'OpenID', '用户唯一标识', TRUE);
INSERT INTO oauth2_scope (id, code, name, description, is_default) VALUES
(2, 'profile', 'Profile', '用户资料(姓名、头像)', TRUE);
INSERT INTO oauth2_scope (id, code, name, description, is_default) VALUES
(3, 'email', 'Email', '邮箱地址', FALSE);
INSERT INTO oauth2_scope (id, code, name, description, is_default) VALUES
(4, 'phone', 'Phone', '手机号', FALSE);
INSERT INTO oauth2_scope (id, code, name, description, is_default) VALUES
(5, 'address', 'Address', '地址信息', FALSE);
INSERT INTO oauth2_scope (id, code, name, description, is_default) VALUES
(6, 'offline_access', 'Offline Access', '获取Refresh Token', FALSE);

-- 邮件模板 - 注册验证
INSERT INTO sys_email_template (id, code, name, subject, content, variables) VALUES
(1, 'register_verify', '注册验证码', '${system_name} - 注册验证码',
'<!DOCTYPE html>
<html>
<head><meta charset="UTF-8"></head>
<body style="font-family:Arial,sans-serif;margin:0;padding:0;">
<div style="max-width:600px;margin:0 auto;background:#f9f9f9;">
  <div style="background:#4A90D9;color:white;padding:20px;text-align:center;">
    <h1>${system_name}</h1>
  </div>
  <div style="padding:30px;">
    <p>您好，${nickname}：</p>
    <p>您正在进行邮箱验证，验证码为：</p>
    <p style="font-size:32px;font-weight:bold;color:#4A90D9;letter-spacing:5px;text-align:center;">${code}</p>
    <p>验证码 ${expire_minutes} 分钟内有效。</p>
    <p>如非本人操作，请忽略此邮件。</p>
  </div>
  <div style="padding:20px;text-align:center;color:#999;font-size:12px;">
    <p>&copy; ${year} ${system_name}. All rights reserved.</p>
  </div>
</div>
</body>
</html>',
'{"system_name":"系统名称","nickname":"用户昵称","code":"验证码","expire_minutes":"过期时间(分钟)","year":"当前年份"}');

-- 邮件模板 - 登录验证码
INSERT INTO sys_email_template (id, code, name, subject, content, variables) VALUES
(2, 'login_code', '登录验证码', '${system_name} - 登录验证码',
'<!DOCTYPE html>
<html>
<head><meta charset="UTF-8"></head>
<body style="font-family:Arial,sans-serif;margin:0;padding:0;">
<div style="max-width:600px;margin:0 auto;background:#f9f9f9;">
  <div style="background:#4A90D9;color:white;padding:20px;text-align:center;">
    <h1>${system_name}</h1>
  </div>
  <div style="padding:30px;">
    <p>您好，${nickname}：</p>
    <p>您正在登录，验证码为：</p>
    <p style="font-size:32px;font-weight:bold;color:#4A90D9;letter-spacing:5px;text-align:center;">${code}</p>
    <p>验证码 ${expire_minutes} 分钟内有效。</p>
    <p>登录IP：${login_ip}</p>
    <p>如非本人操作，请立即修改密码。</p>
  </div>
  <div style="padding:20px;text-align:center;color:#999;font-size:12px;">
    <p>&copy; ${year} ${system_name}. All rights reserved.</p>
  </div>
</div>
</body>
</html>',
'{"system_name":"系统名称","nickname":"用户昵称","code":"验证码","expire_minutes":"过期时间(分钟)","year":"当前年份","login_ip":"登录IP"}');
