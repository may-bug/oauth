package org.codelin.oauth.oauth.admin.api.http;

import lombok.RequiredArgsConstructor;
import org.codelin.oauth.oauth.common.security.Permission;
import org.codelin.oauth.oauth.common.web.BizException;
import org.codelin.oauth.oauth.common.web.R;
import org.codelin.oauth.oauth.identity.application.UserService;
import org.codelin.oauth.oauth.identity.domain.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理后台 - 用户管理
 */
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    /**
     * 用户列表
     */
    @Permission("user:read")
    @GetMapping(version = "1")
    public ResponseEntity<R<R.PageResult<User>>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<User> users = userService.list(page, size);
        long total = userService.count();
        return R.okPage(users, total, page, size);
    }

    /**
     * 获取用户详情
     */
    @Permission("user:read")
    @GetMapping(value = "/{id}", version = "1")
    public ResponseEntity<R<User>> getById(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            throw BizException.notFound("error.user.not-found");
        }
        // 清除敏感信息
        user.setPassword(null);
        return R.ok(user);
    }

    /**
     * 更新用户信息
     */
    @Permission("user:write")
    @PutMapping(value = "/{id}", version = "1")
    public ResponseEntity<R<Void>> update(@PathVariable Long id,
                                          @RequestBody User update) {
        userService.updateUser(id, update);
        return R.ok();
    }

    /**
     * 禁用用户
     */
    @Permission("user:manage")
    @PostMapping(value = "/{id}/disable", version = "1")
    public ResponseEntity<R<Void>> disable(@PathVariable Long id) {
        userService.updateStatus(id, 0);
        return R.ok();
    }

    /**
     * 启用用户
     */
    @Permission("user:manage")
    @PostMapping(value = "/{id}/enable", version = "1")
    public ResponseEntity<R<Void>> enable(@PathVariable Long id) {
        userService.updateStatus(id, 1);
        return R.ok();
    }

    /**
     * 重置密码
     */
    @Permission("user:manage")
    @PostMapping(value = "/{id}/reset-password", version = "1")
    public ResponseEntity<R<Void>> resetPassword(@PathVariable Long id,
                                                  @RequestBody Map<String, String> body) {
        String newPassword = body.get("newPassword");
        if (newPassword == null || newPassword.isEmpty()) {
            return R.fail(400, "新密码不能为空");
        }
        userService.resetPassword(id, newPassword);
        return R.ok();
    }
}
