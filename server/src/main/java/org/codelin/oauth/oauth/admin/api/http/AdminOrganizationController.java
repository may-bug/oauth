package org.codelin.oauth.oauth.admin.api.http;

import lombok.RequiredArgsConstructor;
import org.codelin.oauth.oauth.common.security.Permission;
import org.codelin.oauth.oauth.common.web.BizException;
import org.codelin.oauth.oauth.common.web.R;
import org.codelin.oauth.oauth.rbac.application.OrganizationService;
import org.codelin.oauth.oauth.rbac.domain.model.Organization;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理后台 - 组织管理
 */
@RestController
@RequestMapping("/admin/orgs")
@RequiredArgsConstructor
public class AdminOrganizationController {

    private final OrganizationService organizationService;

    /**
     * 组织列表
     */
    @Permission("org:read")
    @GetMapping(version = "1")
    public ResponseEntity<R<R.PageResult<Organization>>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<Organization> orgs = organizationService.list(page, size);
        long total = organizationService.count();
        return R.okPage(orgs, total, page, size);
    }

    /**
     * 获取组织详情
     */
    @Permission("org:read")
    @GetMapping(value = "/{id}", version = "1")
    public ResponseEntity<R<Organization>> getById(@PathVariable Long id) {
        Organization org = organizationService.getById(id);
        if (org == null) {
            throw BizException.notFound("error.org.not-found");
        }
        return R.ok(org);
    }

    /**
     * 创建组织
     */
    @Permission("org:write")
    @PostMapping(version = "1")
    public ResponseEntity<R<Organization>> create(@RequestBody Organization org) {
        return R.ok(organizationService.create(org));
    }

    /**
     * 更新组织
     */
    @Permission("org:write")
    @PutMapping(value = "/{id}", version = "1")
    public ResponseEntity<R<Void>> update(@PathVariable Long id,
                                           @RequestBody Organization update) {
        organizationService.update(id, update);
        return R.ok();
    }

    /**
     * 禁用组织
     */
    @Permission("org:manage")
    @PostMapping(value = "/{id}/disable", version = "1")
    public ResponseEntity<R<Void>> disable(@PathVariable Long id) {
        organizationService.disable(id);
        return R.ok();
    }

    /**
     * 启用组织
     */
    @Permission("org:manage")
    @PostMapping(value = "/{id}/enable", version = "1")
    public ResponseEntity<R<Void>> enable(@PathVariable Long id) {
        organizationService.enable(id);
        return R.ok();
    }

    /**
     * 删除组织
     */
    @Permission("org:manage")
    @DeleteMapping(value = "/{id}", version = "1")
    public ResponseEntity<R<Void>> delete(@PathVariable Long id) {
        organizationService.delete(id);
        return R.ok();
    }

    /**
     * 添加用户到组织
     */
    @Permission("org:manage")
    @PostMapping(value = "/{orgId}/users", version = "1")
    public ResponseEntity<R<Void>> addUser(@PathVariable Long orgId,
                                             @RequestBody Map<String, Long> body) {
        Long userId = body.get("userId");
        Long roleId = body.get("roleId");
        if (userId == null) {
            return R.fail(400, "用户ID不能为空");
        }
        organizationService.addUserToOrganization(userId, orgId, roleId);
        return R.ok();
    }

    /**
     * 从组织移除用户
     */
    @Permission("org:manage")
    @DeleteMapping(value = "/{orgId}/users/{userId}", version = "1")
    public ResponseEntity<R<Void>> removeUser(@PathVariable Long orgId,
                                                @PathVariable Long userId) {
        organizationService.removeUserFromOrganization(userId, orgId);
        return R.ok();
    }
}
