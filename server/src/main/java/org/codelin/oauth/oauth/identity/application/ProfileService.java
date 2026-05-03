package org.codelin.oauth.oauth.identity.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codelin.oauth.oauth.common.cache.CacheStore;
import org.codelin.oauth.oauth.common.web.BizException;
import org.codelin.oauth.oauth.identity.domain.model.User;
import org.codelin.oauth.oauth.identity.domain.model.UserCredential;
import org.codelin.oauth.oauth.identity.application.notification.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * 用户资料服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserService userService;
    private final CacheStore cacheStore;
    private final NotificationService notificationService;
    private final org.codelin.oauth.oauth.identity.infrastructure.storage.FileStorage fileStorage;

    /**
     * 获取用户资料
     */
    public UserProfile getProfile(Long userId) {
        User user = userService.getById(userId);
        if (user == null) {
            throw BizException.notFound("error.user.not-found");
        }

        List<UserCredential> credentials = userService.getCredentials(userId);

        UserProfile profile = new UserProfile();
        profile.setId(user.getId());
        profile.setUsername(user.getUsername());
        profile.setNickname(user.getNickname());
        profile.setRealName(user.getRealName());
        profile.setAvatar(user.getAvatar());
        profile.setGender(user.getGender());
        profile.setBirthday(user.getBirthday());
        profile.setBio(user.getBio());

        // 填充凭证信息
        for (UserCredential cred : credentials) {
            switch (cred.getCredentialType()) {
                case "email" -> {
                    profile.setEmail(cred.getCredentialKey());
                    profile.setEmailVerified(cred.getVerified());
                }
                case "phone" -> {
                    profile.setPhone(cred.getCredentialKey());
                    profile.setPhoneVerified(cred.getVerified());
                }
                case "social_github" -> profile.setGithubBound(true);
                case "social_gitee" -> profile.setGiteeBound(true);
                case "social_qq" -> profile.setQqBound(true);
            }
        }

        return profile;
    }

    /**
     * 更新用户资料
     */
    @Transactional
    public void updateProfile(Long userId, User update) {
        userService.updateUser(userId, update);
    }

    /**
     * 发起更换邮箱（发送验证码）
     */
    public void initiateEmailChange(Long userId, String newEmail) {
        // 检查新邮箱是否已被使用
        User existing = userService.getByCredential("email", newEmail);
        if (existing != null) {
            throw BizException.conflict("error.user.email-taken");
        }

        // 生成验证码并存储
        String code = generateVerifyCode();
        cacheStore.set("email_change:" + userId, Map.of("email", newEmail, "code", code),
                Duration.ofMinutes(10));

        // 发送验证码到新邮箱
        notificationService.sendEmail(newEmail, "邮箱验证码",
                "您的邮箱验证码是：<b>" + code + "</b>，10分钟内有效。");

        log.info("Email change initiated for user: {}, new email: {}", userId, newEmail);
    }

    /**
     * 验证新邮箱
     */
    @Transactional
    public void verifyEmailChange(Long userId, String code) {
        @SuppressWarnings("unchecked")
        Map<String, String> data = cacheStore.get("email_change:" + userId);
        if (data == null) {
            throw BizException.badRequest("error.verify-code.expired");
        }

        if (!code.equals(data.get("code"))) {
            throw BizException.badRequest("error.verify-code.invalid");
        }

        String newEmail = data.get("email");
        cacheStore.delete("email_change:" + userId);

        // 更新或创建邮箱凭证
        UserCredential credential = userService.getCredential(userId, "email");
        if (credential != null) {
            credential.setCredentialKey(newEmail);
            credential.setVerified(true);
            userService.updateCredential(credential);
        } else {
            credential = new UserCredential();
            credential.setUserId(userId);
            credential.setCredentialType("email");
            credential.setCredentialKey(newEmail);
            credential.setVerified(true);
            credential.setPrimaryFlag(false);
            credential.setCreatedAt(java.time.LocalDateTime.now());
            userService.saveCredential(credential);
        }

        log.info("Email changed for user: {}", userId);
    }

    /**
     * 发起更换手机号（发送验证码）
     */
    public void initiatePhoneChange(Long userId, String newPhone) {
        // 检查新手机号是否已被使用
        User existing = userService.getByCredential("phone", newPhone);
        if (existing != null) {
            throw BizException.conflict("error.user.phone-taken");
        }

        // 生成验证码并存储
        String code = generateVerifyCode();
        cacheStore.set("phone_change:" + userId, Map.of("phone", newPhone, "code", code),
                Duration.ofMinutes(10));

        // 发送验证码到新手机
        notificationService.sendSms(newPhone, "您的手机验证码是：" + code + "，10分钟内有效。");

        log.info("Phone change initiated for user: {}, new phone: {}", userId, newPhone);
    }

    /**
     * 验证新手机号
     */
    @Transactional
    public void verifyPhoneChange(Long userId, String code) {
        @SuppressWarnings("unchecked")
        Map<String, String> data = cacheStore.get("phone_change:" + userId);
        if (data == null) {
            throw BizException.badRequest("error.verify-code.expired");
        }

        if (!code.equals(data.get("code"))) {
            throw BizException.badRequest("error.verify-code.invalid");
        }

        String newPhone = data.get("phone");
        cacheStore.delete("phone_change:" + userId);

        // 更新或创建手机凭证
        UserCredential credential = userService.getCredential(userId, "phone");
        if (credential != null) {
            credential.setCredentialKey(newPhone);
            credential.setVerified(true);
            userService.updateCredential(credential);
        } else {
            credential = new UserCredential();
            credential.setUserId(userId);
            credential.setCredentialType("phone");
            credential.setCredentialKey(newPhone);
            credential.setVerified(true);
            credential.setPrimaryFlag(false);
            credential.setCreatedAt(java.time.LocalDateTime.now());
            userService.saveCredential(credential);
        }

        log.info("Phone changed for user: {}", userId);
    }

    /**
     * 生成6位验证码
     */
    private String generateVerifyCode() {
        return String.format("%06d", new java.util.Random().nextInt(1000000));
    }

    /**
     * 上传头像
     */
    @Transactional
    public String uploadAvatar(Long userId, org.springframework.web.multipart.MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/"))
            throw BizException.badRequest("error.file.invalid-type");
        if (file.getSize() > 2 * 1024 * 1024)
            throw BizException.badRequest("error.file.too-large");

        try {
            String ext = "";
            String name = file.getOriginalFilename();
            if (name != null) {
                int dot = name.lastIndexOf('.');
                if (dot >= 0) ext = name.substring(dot);
            }
            String path = "avatars/" + userId + "/" + java.util.UUID.randomUUID() + ext;
            String url = fileStorage.upload(path, file.getInputStream(), contentType);
            userService.updateAvatar(userId, url);
            return url;
        } catch (Exception e) {
            throw new RuntimeException("Avatar upload failed", e);
        }
    }

    /**
     * 用户资料DTO
     */
    @lombok.Data
    public static class UserProfile {
        private Long id;
        private String username;
        private String nickname;
        private String realName;
        private String avatar;
        private Integer gender;
        private java.time.LocalDate birthday;
        private String bio;

        private String email;
        private Boolean emailVerified;
        private String phone;
        private Boolean phoneVerified;

        private Boolean githubBound = false;
        private Boolean giteeBound = false;
        private Boolean qqBound = false;
    }
}
