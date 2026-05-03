package org.codelin.oauth.oauth.api.http;

import lombok.RequiredArgsConstructor;
import org.codelin.oauth.oauth.common.security.SecurityContextHolder;
import org.codelin.oauth.oauth.common.web.BizException;
import org.codelin.oauth.oauth.common.web.R;
import org.codelin.oauth.oauth.identity.application.ProfileService;
import org.codelin.oauth.oauth.identity.application.UserService;
import org.codelin.oauth.oauth.identity.domain.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final UserService userService;

    /**
     * 获取个人资料
     */
    @GetMapping(version = "1")
    public ResponseEntity<R<ProfileService.UserProfile>> getProfile() {
        Long userId = requireUserId();
        ProfileService.UserProfile profile = profileService.getProfile(userId);
        return R.ok(profile);
    }

    /**
     * 更新个人资料
     */
    @PutMapping(version = "1")
    public ResponseEntity<R<Void>> updateProfile(@RequestBody User update) {
        Long userId = requireUserId();
        profileService.updateProfile(userId, update);
        return R.ok();
    }

    /**
     * 发起更换邮箱
     */
    @PostMapping(value = "/email/change", version = "1")
    public ResponseEntity<R<Void>> initiateEmailChange(@RequestBody Map<String, String> body) {
        Long userId = requireUserId();
        String newEmail = body.get("email");
        if (newEmail == null || newEmail.isEmpty()) {
            return R.fail(400, "邮箱不能为空");
        }
        profileService.initiateEmailChange(userId, newEmail);
        return R.ok();
    }

    /**
     * 验证新邮箱
     */
    @PostMapping(value = "/email/verify", version = "1")
    public ResponseEntity<R<Void>> verifyEmailChange(@RequestBody Map<String, String> body) {
        Long userId = requireUserId();
        String code = body.get("code");
        if (code == null || code.isEmpty()) {
            return R.fail(400, "验证码不能为空");
        }
        profileService.verifyEmailChange(userId, code);
        return R.ok();
    }

    /**
     * 发起更换手机号
     */
    @PostMapping(value = "/phone/change", version = "1")
    public ResponseEntity<R<Void>> initiatePhoneChange(@RequestBody Map<String, String> body) {
        Long userId = requireUserId();
        String newPhone = body.get("phone");
        if (newPhone == null || newPhone.isEmpty()) {
            return R.fail(400, "手机号不能为空");
        }
        profileService.initiatePhoneChange(userId, newPhone);
        return R.ok();
    }

    /**
     * 验证新手机号
     */
    @PostMapping(value = "/phone/verify", version = "1")
    public ResponseEntity<R<Void>> verifyPhoneChange(@RequestBody Map<String, String> body) {
        Long userId = requireUserId();
        String code = body.get("code");
        if (code == null || code.isEmpty()) {
            return R.fail(400, "验证码不能为空");
        }
        profileService.verifyPhoneChange(userId, code);
        return R.ok();
    }

    /**
     * 修改密码
     */
    @PostMapping(value = "/password", version = "1")
    public ResponseEntity<R<Void>> changePassword(@RequestBody Map<String, String> body) {
        Long userId = requireUserId();
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");

        if (oldPassword == null || oldPassword.isEmpty()) {
            return R.fail(400, "旧密码不能为空");
        }
        if (newPassword == null || newPassword.isEmpty()) {
            return R.fail(400, "新密码不能为空");
        }

        userService.changePassword(userId, oldPassword, newPassword);
        return R.ok();
    }

    @PostMapping(value = "/avatar", version = "1")
    public ResponseEntity<R<Map<String, String>>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        String url = profileService.uploadAvatar(requireUserId(), file);
        return R.ok(Map.of("avatar", url));
    }

    /**
     * 获取当前用户ID
     */
    private Long requireUserId() {
        Long userId = SecurityContextHolder.getUserId();
        if (userId == null) {
            throw BizException.unauthorized("error.auth.login-required");
        }
        return userId;
    }
}
