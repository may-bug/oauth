package org.codelin.oauth.oauth.identity.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codelin.oauth.oauth.authorization.application.TokenService;
import org.codelin.oauth.oauth.common.security.TokenInfo;
import org.codelin.oauth.oauth.common.web.BizException;
import org.codelin.oauth.oauth.identity.domain.model.User;
import org.codelin.oauth.oauth.identity.domain.model.UserCredential;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 认证服务 - 登录、登出
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final TokenService tokenService;
    private final VerificationCodeService verificationCodeService;

    /**
     * 账号密码登录
     */
    public Map<String, Object> loginByUsername(String username, String password, String ip) {
        // 查找用户（支持用户名、邮箱、手机号）
        User user = findUserByLoginKey(username);
        if (user == null) {
            throw BizException.badRequest("error.auth.invalid-credentials");
        }

        // 检查用户状态
        checkUserStatus(user);

        // 验证密码
        if (!userService.verifyPassword(user.getId(), password)) {
            throw BizException.badRequest("error.auth.invalid-credentials");
        }

        // 更新登录信息
        userService.updateLastLogin(user.getId(), ip);

        // 生成Token
        return generateTokens(user.getId(), null, "web", Set.of("openid", "profile"));
    }

    /**
     * 邮箱验证码登录
     */
    public Map<String, Object> loginByEmail(String email, String code, String ip) {
        // 验证验证码
        verificationCodeService.verifyEmailCode(email, code, "login");

        // 查找或创建用户
        User user = userService.getByCredential("email", email);
        if (user == null) {
            // 自动创建用户
            user = userService.createUserByEmail(email, generateRandomPassword());
        }

        // 检查用户状态
        checkUserStatus(user);

        // 更新登录信息
        userService.updateLastLogin(user.getId(), ip);

        // 生成Token
        return generateTokens(user.getId(), null, "web", Set.of("openid", "profile", "email"));
    }

    /**
     * 手机号验证码登录
     */
    public Map<String, Object> loginByPhone(String phone, String code, String ip) {
        // 验证验证码
        verificationCodeService.verifyPhoneCode(phone, code, "login");

        // 查找或创建用户
        User user = userService.getByCredential("phone", phone);
        if (user == null) {
            // 自动创建用户
            user = userService.createUserByPhone(phone, generateRandomPassword());
        }

        // 检查用户状态
        checkUserStatus(user);

        // 更新登录信息
        userService.updateLastLogin(user.getId(), ip);

        // 生成Token
        return generateTokens(user.getId(), null, "web", Set.of("openid", "profile", "phone"));
    }

    /**
     * 社交登录
     */
    public Map<String, Object> loginBySocial(String provider, String providerUid,
                                              String providerName, String providerAvatar,
                                              String providerEmail, String ip) {
        // 查找已绑定的用户
        User user = userService.getByCredential("social_" + provider, providerUid);

        if (user == null) {
            // 创建新用户
            String username = provider + "_" + providerUid;
            String password = generateRandomPassword();
            user = userService.createUser(username, password, providerName);

            // 绑定社交账号
            UserCredential credential = new UserCredential();
            credential.setUserId(user.getId());
            credential.setCredentialType("social_" + provider);
            credential.setCredentialKey(providerUid);
            credential.setVerified(true);
            credential.setPrimaryFlag(false);
            credential.setCreatedAt(java.time.LocalDateTime.now());
            userService.saveCredential(credential);
        }

        // 检查用户状态
        checkUserStatus(user);

        // 更新登录信息
        userService.updateLastLogin(user.getId(), ip);

        // 生成Token
        return generateTokens(user.getId(), null, "web", Set.of("openid", "profile"));
    }

    /**
     * 登出
     */
    public void logout(String token) {
        tokenService.revokeToken(token);
        log.info("User logged out");
    }

    /**
     * 刷新Token
     */
    public Map<String, Object> refreshToken(String refreshToken) {
        TokenInfo newToken = tokenService.refreshToken(refreshToken, 3600);
        if (newToken == null) {
            throw BizException.badRequest("error.auth.token-invalid");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("access_token", newToken.getToken());
        result.put("refresh_token", refreshToken);
        result.put("expires_in", 3600);
        result.put("token_type", "Bearer");
        return result;
    }

    /**
     * 根据登录键查找用户（用户名、邮箱、手机号）
     */
    /**
     * 注册并自动登录
     */
    public Map<String, Object> register(String username, String password, String nickname,
                                         String email, String phone, String ip) {
        User user;
        if (email != null && !email.isEmpty()) {
            user = userService.createUserByEmail(email, password);
        } else if (phone != null && !phone.isEmpty()) {
            user = userService.createUserByPhone(phone, password);
        } else {
            user = userService.createUser(username, password, nickname);
        }
        return loginByUsername(user.getUsername(), password, ip);
    }

    private User findUserByLoginKey(String loginKey) {
        // 先尝试用户名
        User user = userService.getByUsername(loginKey);
        if (user != null) {
            return user;
        }

        // 尝试邮箱
        user = userService.getByCredential("email", loginKey);
        if (user != null) {
            return user;
        }

        // 尝试手机号
        return userService.getByCredential("phone", loginKey);
    }

    /**
     * 检查用户状态
     */
    private void checkUserStatus(User user) {
        if (user.getStatus() == 0) {
            throw BizException.badRequest("error.auth.user-disabled");
        }
        if (user.getStatus() == 2) {
            throw BizException.badRequest("error.auth.user-locked");
        }
    }

    /**
     * 生成Token对
     */
    private Map<String, Object> generateTokens(Long userId, Long orgId, String clientId,
                                                 Set<String> scopes) {
        // 生成Access Token
        TokenInfo accessToken = tokenService.generateAccessToken(userId, orgId, clientId, scopes);

        // 生成Refresh Token
        String refreshToken = tokenService.generateRefreshToken(userId, clientId, scopes,
                accessToken.getToken());

        Map<String, Object> result = new HashMap<>();
        result.put("access_token", accessToken.getToken());
        result.put("refresh_token", refreshToken);
        result.put("expires_in", 3600);
        result.put("token_type", "Bearer");
        return result;
    }

    /**
     * 生成随机密码（用于自动创建的用户）
     */
    private String generateRandomPassword() {
        return java.util.UUID.randomUUID().toString().substring(0, 16);
    }
}
