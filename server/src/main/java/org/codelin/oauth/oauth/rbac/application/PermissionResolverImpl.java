package org.codelin.oauth.oauth.rbac.application;

import lombok.RequiredArgsConstructor;
import org.codelin.oauth.oauth.common.security.AuthorizationFilter;
import org.codelin.oauth.oauth.rbac.domain.model.Role;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * 权限解析器实现 - 放在rbac模块，因为依赖rbac的服务
 */
@Component
@RequiredArgsConstructor
public class PermissionResolverImpl implements AuthorizationFilter.PermissionResolver {

    private final PermissionService permissionService;
    private final RoleService roleService;

    @Override
    public Set<String> getUserPermissions(Long userId, Long orgId) {
        return permissionService.getUserPermissionCodes(userId);
    }

    @Override
    public Set<String> getUserRoles(Long userId, Long orgId) {
        return roleService.getUserRoles(userId, orgId).stream()
                .map(Role::getCode)
                .collect(Collectors.toSet());
    }
}
