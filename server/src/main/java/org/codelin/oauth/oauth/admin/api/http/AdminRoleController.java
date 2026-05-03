package org.codelin.oauth.oauth.admin.api.http;

import lombok.RequiredArgsConstructor;
import org.codelin.oauth.oauth.common.security.Permission;
import org.codelin.oauth.oauth.common.web.BizException;
import org.codelin.oauth.oauth.common.web.R;
import org.codelin.oauth.oauth.rbac.application.PermissionService;
import org.codelin.oauth.oauth.rbac.application.RoleService;
import org.codelin.oauth.oauth.rbac.domain.model.Role;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * 管理后台 - 角色管理
 */
@RestController
@RequestMapping("/admin/roles")
@RequiredArgsConstructor
public class AdminRoleController {

    private final RoleService roleService;
    private final PermissionService permissionService;

    /**
     * 角色列表
     */
    @Permission("role:read")
    @GetMapping(version = "1")
    public ResponseEntity<R<R.PageResult<Role>>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<Role> roles = roleService.list(page, size);
        long total = roleService.count();
        return R.okPage(roles, total, page, size);
    }

    /**
     * 获取角色详情
     */
    @Permission("role:read")
    @GetMapping(value = "/{id}", version = "1")
    public ResponseEntity<R<Role>> getById(@PathVariable Long id) {
        Role role = roleService.getById(id);
        if (role == null) {
            throw BizException.notFound("error.role.not-found");
        }
        return R.ok(role);
    }

    /**
     * 创建角色
     */
    @Permission("role:create")
    @PostMapping(version = "1")
    public ResponseEntity<R<Role>> create(@RequestBody Role role) {
        return R.ok(roleService.create(role));
    }

    /**
     * 更新角色
     */
    @Permission("role:edit")
    @PutMapping(value = "/{id}", version = "1")
    public ResponseEntity<R<Void>> update(@PathVariable Long id,
                                           @RequestBody Role update) {
        roleService.update(id, update);
        return R.ok();
    }

    /**
     * 禁用角色
     */
    @Permission("role:manage")
    @PostMapping(value = "/{id}/disable", version = "1")
    public ResponseEntity<R<Void>> disable(@PathVariable Long id) {
        roleService.disable(id);
        return R.ok();
    }

    /**
     * 启用角色
     */
    @Permission("role:manage")
    @PostMapping(value = "/{id}/enable", version = "1")
    public ResponseEntity<R<Void>> enable(@PathVariable Long id) {
        roleService.enable(id);
        return R.ok();
    }

    /**
     * 删除角色
     */
    @Permission("role:delete")
    @DeleteMapping(value = "/{id}", version = "1")
    public ResponseEntity<R<Void>> delete(@PathVariable Long id) {
        roleService.delete(id);
        return R.ok();
    }

    /**
     * 获取角色的权限
     */
    @Permission("role:read")
    @GetMapping(value = "/{id}/permissions", version = "1")
    public ResponseEntity<R<List<org.codelin.oauth.oauth.rbac.domain.model.Permission>>> getPermissions(@PathVariable Long id) {
        return R.ok(permissionService.getRolePermissions(id));
    }

    /**
     * 获取组织下的角色
     */
    @Permission("role:read")
    @GetMapping(value = "/org/{orgId}", version = "1")
    public ResponseEntity<R<List<Role>>> listByOrg(@PathVariable Long orgId) {
        return R.ok(roleService.listByOrg(orgId));
    }
}
