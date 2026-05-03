package org.codelin.oauth.oauth.admin.api.http;

import lombok.RequiredArgsConstructor;
import org.codelin.oauth.oauth.common.security.Permission;
import org.codelin.oauth.oauth.common.web.R;
import org.codelin.oauth.oauth.rbac.application.PermissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理后台 - 权限管理
 */
@RestController
@RequestMapping("/admin/permissions")
@RequiredArgsConstructor
public class AdminPermissionController {

    private final PermissionService permissionService;

    /**
     * 权限列表
     */
    @Permission("config:read")
    @GetMapping(version = "1")
    public ResponseEntity<R<List<org.codelin.oauth.oauth.rbac.domain.model.Permission>>> list() {
        return R.ok(permissionService.listAll());
    }
}
