package org.codelin.oauth.oauth.api.http;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.codelin.oauth.oauth.common.security.Anonymous;
import org.codelin.oauth.oauth.common.security.SecurityContextHolder;
import org.codelin.oauth.oauth.common.web.BizException;
import org.codelin.oauth.oauth.common.web.R;
import org.codelin.oauth.oauth.common.web.RequestUtils;
import org.codelin.oauth.oauth.identity.application.*;
import org.codelin.oauth.oauth.identity.infrastructure.captcha.SvgCaptchaGenerator;
import org.codelin.oauth.oauth.identity.domain.model.User;
import org.codelin.oauth.oauth.rbac.application.PermissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final SvgCaptchaGenerator captchaGenerator;
    private final CaptchaConfigService captchaConfigService;
    private final CaptchaValidator captchaValidator;
    private final CryptoConfigService cryptoConfigService;
    private final PermissionService permissionService;
    private final VerificationCodeService verificationCodeService;
    private final AuthConfigService authConfigService;
    private final CryptoService cryptoService;

    /** 账号密码登录 */
    @Anonymous
    @PostMapping(value = "/login", version = "1")
    public ResponseEntity<R<Map<String, Object>>> login(@RequestBody LoginRequest request,
                                                         HttpServletRequest httpRequest,
                                                         HttpServletResponse httpResponse) {
        Map<String, Object> tokens = authService.loginByUsername(
                request.getUsername(), request.getPassword(), RequestUtils.getClientIp(httpRequest));
        setJwtCookie(httpResponse, (String) tokens.get("access_token"), (int) tokens.get("expires_in"));
        return R.ok(tokens);
    }

    /** 发送邮箱验证码 */
    @Anonymous
    @PostMapping(value = "/login/email/send", version = "1")
    public ResponseEntity<R<Void>> sendEmailLoginCode(@RequestBody EmailRequest request) {
        verificationCodeService.sendEmailCode(request.getEmail(), "login");
        return R.ok();
    }

    /** 邮箱验证码登录 */
    @Anonymous
    @PostMapping(value = "/login/email", version = "1")
    public ResponseEntity<R<Map<String, Object>>> loginByEmail(@RequestBody EmailLoginRequest request,
                                                                HttpServletRequest httpRequest,
                                                                HttpServletResponse httpResponse) {
        Map<String, Object> tokens = authService.loginByEmail(
                request.getEmail(), request.getCode(), RequestUtils.getClientIp(httpRequest));
        setJwtCookie(httpResponse, (String) tokens.get("access_token"), (int) tokens.get("expires_in"));
        return R.ok(tokens);
    }

    /** 发送手机验证码 */
    @Anonymous
    @PostMapping(value = "/login/phone/send", version = "1")
    public ResponseEntity<R<Void>> sendPhoneLoginCode(@RequestBody PhoneRequest request) {
        throw BizException.badRequest("error.auth.sms-not-supported");
    }

    /** 手机验证码登录 */
    @Anonymous
    @PostMapping(value = "/login/phone", version = "1")
    public ResponseEntity<R<Map<String, Object>>> loginByPhone(@RequestBody PhoneLoginRequest request,
                                                                HttpServletRequest httpRequest,
                                                                HttpServletResponse httpResponse) {
        Map<String, Object> tokens = authService.loginByPhone(
                request.getPhone(), request.getCode(), RequestUtils.getClientIp(httpRequest));
        setJwtCookie(httpResponse, (String) tokens.get("access_token"), (int) tokens.get("expires_in"));
        return R.ok(tokens);
    }

    /** 用户注册 */
    @Anonymous
    @PostMapping(value = "/register", version = "1")
    public ResponseEntity<R<Map<String, Object>>> register(@RequestBody RegisterRequest request,
                                                            HttpServletRequest httpRequest,
                                                            HttpServletResponse httpResponse) {
        Map<String, Object> tokens = authService.register(request.getUsername(), request.getPassword(),
                request.getNickname(), request.getEmail(), request.getPhone(),
                RequestUtils.getClientIp(httpRequest));
        setJwtCookie(httpResponse, (String) tokens.get("access_token"), (int) tokens.get("expires_in"));
        return R.ok(tokens, "注册成功");
    }

    /** 发送注册邮箱验证码 */
    @Anonymous
    @PostMapping(value = "/register/email/send", version = "1")
    public ResponseEntity<R<Void>> sendRegisterEmailCode(@RequestBody EmailRequest request) {
        verificationCodeService.sendEmailCode(request.getEmail(), "register");
        return R.ok();
    }

    /** 登出 */
    @PostMapping(value = "/logout", version = "1")
    public ResponseEntity<R<Void>> logout(HttpServletRequest request, HttpServletResponse response) {
        String token = RequestUtils.extractToken(request);
        if (token != null) {
            authService.logout(token);
        }
        Cookie clearCookie = new Cookie(RequestUtils.AUTH_COOKIE_NAME, "");
        clearCookie.setHttpOnly(true);
        clearCookie.setPath("/");
        clearCookie.setMaxAge(0);
        response.addCookie(clearCookie);
        return R.ok();
    }

    /** 刷新Token */
    @Anonymous
    @PostMapping(value = "/refresh", version = "1")
    public ResponseEntity<R<Map<String, Object>>> refreshToken(@RequestBody RefreshTokenRequest request) {
        return R.ok(authService.refreshToken(request.getRefreshToken()));
    }

    /** 登录状态检查 */
    @Anonymous
    @GetMapping(value = "/status", version = "1")
    public ResponseEntity<R<Map<String, Object>>> status() {
        Long userId = SecurityContextHolder.getUserId();
        if (userId == null) return R.ok(Map.of("authenticated", false));
        User user = userService.getById(userId);
        return R.ok(Map.of("authenticated", true, "userId", userId,
                "username", user != null ? user.getUsername() : "",
                "nickname", user != null ? user.getNickname() : ""));
    }

    /** 当前用户信息 */
    @GetMapping(value = "/me", version = "1")
    public ResponseEntity<R<User>> getCurrentUser() {
        Long userId = SecurityContextHolder.getUserId();
        if (userId == null) throw BizException.unauthorized("error.auth.login-required");
        User user = userService.getById(userId);
        if (user == null) throw BizException.notFound("error.user.not-found");
        user.setPassword(null);
        return R.ok(user);
    }

    /** 当前用户权限 */
    @GetMapping(value = "/permissions", version = "1")
    public ResponseEntity<R<Map<String, Object>>> getPermissions() {
        Long userId = SecurityContextHolder.getUserId();
        if (userId == null) throw BizException.unauthorized("error.auth.login-required");
        return R.ok(permissionService.getGroupedPermissions(userId));
    }

    /** 认证配置 */
    @Anonymous
    @GetMapping(value = "/config", version = "1")
    public ResponseEntity<R<String>> getAuthConfig() {
        return R.ok(authConfigService.getClientConfig());
    }

    /** 验证码生成 */
    @Anonymous
    @GetMapping(value = "/captcha", version = "1")
    public ResponseEntity<R<Map<String, Object>>> getCaptcha() {
        SvgCaptchaGenerator.CaptchaConfig cfg = captchaConfigService.toGeneratorConfig();
        SvgCaptchaGenerator.CaptchaResult result = captchaGenerator.generate(cfg);
        long expireSec = captchaConfigService.getExpireSeconds();
        captchaValidator.store(result.getToken(), result.getCode(), expireSec);
        return R.ok(Map.of("svg", result.getSvg(), "token", result.getToken(), "expireSeconds", expireSec));
    }

    private void setJwtCookie(HttpServletResponse response, String token, int maxAgeSec) {
        Cookie cookie = new Cookie(RequestUtils.AUTH_COOKIE_NAME, token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(maxAgeSec);
        cookie.setAttribute("SameSite", "Lax");
        response.addCookie(cookie);
    }

    // ========== 请求体 ==========
    @lombok.Data public static class LoginRequest { private String username, password, captcha, captchaToken; }
    @lombok.Data public static class EmailRequest { private String email; }
    @lombok.Data public static class EmailLoginRequest { private String email, code; }
    @lombok.Data public static class PhoneRequest { private String phone; }
    @lombok.Data public static class PhoneLoginRequest { private String phone, code; }
    @lombok.Data public static class RegisterRequest { private String username, password, nickname, email, phone, captcha, captchaToken; }
    @lombok.Data public static class RefreshTokenRequest { private String refreshToken; }
}
