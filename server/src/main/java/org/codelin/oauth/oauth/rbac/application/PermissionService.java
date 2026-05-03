package org.codelin.oauth.oauth.rbac.application;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codelin.oauth.oauth.common.cache.CacheStore;
import org.codelin.oauth.oauth.common.web.BizException;
import org.codelin.oauth.oauth.rbac.domain.model.Permission;
import org.codelin.oauth.oauth.rbac.domain.model.RolePermission;
import org.codelin.oauth.oauth.rbac.infrastructure.persistence.PermissionMapper;
import org.codelin.oauth.oauth.rbac.infrastructure.persistence.RolePermissionMapper;
import org.codelin.oauth.oauth.rbac.infrastructure.persistence.UserRoleMapper;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 权限服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionMapper permissionMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final UserRoleMapper userRoleMapper;
    private final CacheStore cacheStore;

    private static final String PERMISSION_CACHE_PREFIX = "permission:";
    private static final String ROLE_PERMISSIONS_CACHE_PREFIX = "role_permissions:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    /**
     * 获取所有权限
     */
    public List<Permission> listAll() {
        return permissionMapper.selectListByQuery(
                QueryWrapper.create().orderBy("sort_order", true)
        );
    }

    /**
     * 根据类型获取权限
     *
     * @param type 1=菜单, 2=按钮, 3=API
     */
    public List<Permission> listByType(Integer type) {
        return permissionMapper.selectListByQuery(
                QueryWrapper.create()
                        .eq("type", type)
                        .orderBy("sort_order", true)
        );
    }

    /**
     * 根据ID获取权限
     */
    public Permission getById(Long id) {
        return permissionMapper.selectOneById(id);
    }

    /**
     * 根据code获取权限
     */
    public Permission getByCode(String code) {
        return permissionMapper.selectOneByQuery(
                QueryWrapper.create().eq("code", code)
        );
    }

    /**
     * 创建权限
     */
    public Permission create(Permission permission) {
        if (getByCode(permission.getCode()) != null) {
            throw BizException.conflict("error.permission.code-taken");
        }
        permissionMapper.insert(permission);
        clearPermissionCache();
        return permission;
    }

    /**
     * 更新权限
     */
    public void update(Long id, Permission update) {
        Permission existing = permissionMapper.selectOneById(id);
        if (existing == null) {
            throw BizException.notFound("error.permission.not-found");
        }

        if (update.getName() != null) {
            existing.setName(update.getName());
        }
        if (update.getDescription() != null) {
            existing.setDescription(update.getDescription());
        }
        if (update.getPath() != null) {
            existing.setPath(update.getPath());
        }
        if (update.getIcon() != null) {
            existing.setIcon(update.getIcon());
        }
        if (update.getApiPattern() != null) {
            existing.setApiPattern(update.getApiPattern());
        }
        if (update.getSortOrder() != null) {
            existing.setSortOrder(update.getSortOrder());
        }

        permissionMapper.update(existing);
        clearPermissionCache();
    }

    /**
     * 删除权限
     */
    public void delete(Long id) {
        permissionMapper.deleteById(id);
        clearPermissionCache();
    }

    /**
     * 获取角色的权限ID列表
     */
    public List<Long> getRolePermissionIds(Long roleId) {
        String cacheKey = ROLE_PERMISSIONS_CACHE_PREFIX + roleId;
        List<Long> permissionIds = cacheStore.get(cacheKey);

        if (permissionIds == null) {
            List<RolePermission> rps = rolePermissionMapper.selectListByQuery(
                    QueryWrapper.create().eq("role_id", roleId));
            permissionIds = rps.stream()
                    .map(RolePermission::getPermissionId)
                    .distinct()
                    .toList();
            cacheStore.set(cacheKey, permissionIds, CACHE_TTL);
        }
        return permissionIds;
    }

    /**
     * 获取角色的权限列表
     */
    public List<Permission> getRolePermissions(Long roleId) {
        List<Long> permissionIds = getRolePermissionIds(roleId);
        if (permissionIds.isEmpty()) {
            return List.of();
        }
        return permissionMapper.selectListByQuery(
                QueryWrapper.create().in("id", permissionIds));
    }

    /**
     * 获取用户的权限ID列表（通过用户→角色→权限）
     */
    public List<Long> getUserPermissionIds(Long userId) {
        String cacheKey = PERMISSION_CACHE_PREFIX + "user:" + userId;
        List<Long> permissionIds = cacheStore.get(cacheKey);

        if (permissionIds == null) {
            // 查用户角色
            var userRoles = userRoleMapper.selectListByQuery(
                    QueryWrapper.create().eq("user_id", userId));
            // 查每个角色的权限，合并去重
            permissionIds = userRoles.stream()
                    .flatMap(ur -> getRolePermissionIds(ur.getRoleId()).stream())
                    .distinct()
                    .toList();
            cacheStore.set(cacheKey, permissionIds, CACHE_TTL);
        }
        return permissionIds;
    }

    /**
     * 获取用户的权限列表
     */
    public List<Permission> getUserPermissions(Long userId) {
        List<Long> permissionIds = getUserPermissionIds(userId);
        if (permissionIds.isEmpty()) {
            return List.of();
        }
        return permissionMapper.selectListByQuery(
                QueryWrapper.create().in("id", permissionIds));
    }

    /**
     * 获取用户的权限编码集合
     */
    public Set<String> getUserPermissionCodes(Long userId) {
        return getUserPermissions(userId).stream()
                .map(Permission::getCode)
                .collect(Collectors.toSet());
    }

    /**
     * 检查用户是否有指定权限
     */
    public boolean hasPermission(Long userId, String permissionCode) {
        Set<String> codes = getUserPermissionCodes(userId);
        return codes.contains(permissionCode);
    }

    /**
     * 获取用户的分组权限（菜单/按钮/API）
     */
    public java.util.Map<String, Object> getGroupedPermissions(Long userId) {
        List<Permission> permissions = getUserPermissions(userId);
        List<Permission> menus = permissions.stream().filter(p -> p.getType() == 1).toList();
        List<Permission> buttons = permissions.stream().filter(p -> p.getType() == 2).toList();
        List<String> apis = permissions.stream()
                .filter(p -> p.getType() == 3)
                .map(Permission::getCode)
                .toList();
        return java.util.Map.of("menus", menus, "buttons", buttons, "apis", apis);
    }

    /**
     * 检查用户是否有指定API权限
     *
     * @param method HTTP方法
     * @param path   API路径
     */
    public boolean hasApiPermission(Long userId, String method, String path) {
        List<Permission> permissions = getUserPermissions(userId);
        for (Permission permission : permissions) {
            if (permission.getType() == 3 && permission.getApiPattern() != null) {
                if (matchApiPattern(permission.getApiPattern(), method, path)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 匹配API模式
     * 模式格式: GET:/users/**
     */
    private boolean matchApiPattern(String pattern, String method, String path) {
        String[] parts = pattern.split(":");
        if (parts.length != 2) {
            return false;
        }

        String patternMethod = parts[0];
        String patternPath = parts[1];

        // 检查方法
        if (!"*".equals(patternMethod) && !patternMethod.equalsIgnoreCase(method)) {
            return false;
        }

        // 检查路径（支持**通配符）
        if (patternPath.endsWith("/**")) {
            String prefix = patternPath.substring(0, patternPath.length() - 3);
            return path.startsWith(prefix);
        }
        return patternPath.equals(path);
    }

    /**
     * 清除权限缓存
     */
    private void clearPermissionCache() {
        // 清除所有权限相关缓存
        cacheStore.delete(cacheStore.keys(PERMISSION_CACHE_PREFIX + "*"));
        cacheStore.delete(cacheStore.keys(ROLE_PERMISSIONS_CACHE_PREFIX + "*"));
    }
}
