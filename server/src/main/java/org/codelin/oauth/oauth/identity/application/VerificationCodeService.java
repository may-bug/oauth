package org.codelin.oauth.oauth.identity.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codelin.oauth.oauth.common.cache.CacheStore;
import org.codelin.oauth.oauth.common.web.BizException;
import org.codelin.oauth.oauth.identity.application.notification.EmailService;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 验证码服务 - 生成、发送、校验邮箱/手机验证码
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationCodeService {

    private static final String CODE_CACHE_PREFIX = "vcode:";
    private static final String SEND_LIMIT_PREFIX = "vcode:limit:";

    /** 验证码有效期（秒） */
    private static final long CODE_TTL_SECONDS = 300;

    /** 发送间隔限制（秒） */
    private static final long SEND_INTERVAL_SECONDS = 60;

    /** 每日最大发送次数 */
    private static final int MAX_DAILY_SEND = 10;

    private final CacheStore cacheStore;
    private final EmailService emailService;

    /**
     * 发送邮箱验证码
     *
     * @param email  邮箱地址
     * @param scene  场景：login / register
     */
    public void sendEmailCode(String email, String scene) {
        // 检查发送频率
        checkSendLimit("email:" + email);

        // 生成验证码
        String code = generateCode(6);

        // 存储验证码
        String cacheKey = CODE_CACHE_PREFIX + "email:" + email + ":" + scene;
        cacheStore.set(cacheKey, code, Duration.ofSeconds(CODE_TTL_SECONDS));

        // 记录发送时间（用于频率限制）
        recordSend("email:" + email);

        // 发送邮件
        String subject = "login".equals(scene) ? "登录验证码" : "注册验证码";
        String content = buildEmailContent(code, scene);
        try {
            emailService.sendEmail(email, subject, content);
            log.info("Verification code sent to email: {}", email);
        } catch (Exception e) {
            log.error("Failed to send verification code to: {}", email, e);
            throw BizException.badRequest("error.email.send-failed");
        }
    }

    /**
     * 验证邮箱验证码
     *
     * @param email 邮箱地址
     * @param code  用户输入的验证码
     * @param scene 场景：login / register
     */
    public void verifyEmailCode(String email, String code, String scene) {
        String cacheKey = CODE_CACHE_PREFIX + "email:" + email + ":" + scene;
        verifyCode(cacheKey, code);
    }

    /**
     * 发送手机验证码
     *
     * @param phone 手机号
     * @param scene 场景：login / register
     */
    public void sendPhoneCode(String phone, String scene) {
        checkSendLimit("phone:" + phone);

        String code = generateCode(6);

        String cacheKey = CODE_CACHE_PREFIX + "phone:" + phone + ":" + scene;
        cacheStore.set(cacheKey, code, Duration.ofSeconds(CODE_TTL_SECONDS));

        recordSend("phone:" + phone);

        // 短信发送功能需要接入短信服务商
        log.info("Phone verification code generated for: {} (SMS sending not implemented)", phone);
        throw BizException.badRequest("error.sms.not-supported");
    }

    /**
     * 验证手机验证码
     *
     * @param phone 手机号
     * @param code  用户输入的验证码
     * @param scene 场景：login / register
     */
    public void verifyPhoneCode(String phone, String code, String scene) {
        String cacheKey = CODE_CACHE_PREFIX + "phone:" + phone + ":" + scene;
        verifyCode(cacheKey, code);
    }

    /**
     * 验证通用验证码
     */
    private void verifyCode(String cacheKey, String code) {
        if (code == null || code.isEmpty()) {
            throw BizException.badRequest("error.captcha.required");
        }

        String cachedCode = cacheStore.get(cacheKey);
        if (cachedCode == null) {
            throw BizException.badRequest("error.captcha.expired");
        }

        if (!cachedCode.equals(code)) {
            throw BizException.badRequest("error.captcha.invalid");
        }

        // 验证成功后删除（一次性）
        cacheStore.delete(cacheKey);
    }

    /**
     * 检查发送频率限制
     */
    private void checkSendLimit(String key) {
        String limitKey = SEND_LIMIT_PREFIX + key;
        String lastSend = cacheStore.get(limitKey);
        if (lastSend != null) {
            throw BizException.tooManyRequests("error.vcode.send-too-frequent");
        }
    }

    /**
     * 记录发送时间
     */
    private void recordSend(String key) {
        String limitKey = SEND_LIMIT_PREFIX + key;
        cacheStore.set(limitKey, "1", Duration.ofSeconds(SEND_INTERVAL_SECONDS));
    }

    /**
     * 生成数字验证码
     */
    private String generateCode(int length) {
        int min = (int) Math.pow(10, length - 1);
        int max = (int) Math.pow(10, length) - 1;
        int code = ThreadLocalRandom.current().nextInt(min, max + 1);
        return String.valueOf(code);
    }

    /**
     * 构建邮件内容
     */
    private String buildEmailContent(String code, String scene) {
        String action = "login".equals(scene) ? "登录" : "注册";
        return "<div style='font-family: Arial, sans-serif; max-width: 480px; margin: 0 auto;'>"
                + "<h2 style='color: #333;'>" + action + "验证码</h2>"
                + "<p>您正在进行" + action + "操作，验证码为：</p>"
                + "<div style='font-size: 32px; font-weight: bold; color: #1890ff; "
                + "letter-spacing: 8px; padding: 16px; background: #f5f5f5; "
                + "text-align: center; border-radius: 8px;'>" + code + "</div>"
                + "<p style='color: #999; font-size: 14px;'>验证码 " + (CODE_TTL_SECONDS / 60) + " 分钟内有效，请勿泄露给他人。</p>"
                + "</div>";
    }
}
