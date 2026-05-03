package org.codelin.oauth.oauth.rbac.application;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codelin.oauth.oauth.common.web.BizException;
import org.codelin.oauth.oauth.rbac.domain.model.Role;
import org.codelin.oauth.oauth.rbac.domain.model.UserRole;
import org.codelin.oauth.oauth.rbac.infrastructure.persistence.RoleMapper;
import org.codelin.oauth.oauth.rbac.infrastructure.persistence.UserRoleMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;

    /**
     * 获取角色列表
     */
    public List<Role> list(int page, int size) {
        return roleMapper.selectListByQuery(
                QueryWrapper.create()
                        .orderBy("sort_order", true)
                        .limit(size)
                        .offset((page - 1) * size)
        );
    }

    /**
     * 获取组织下的角色列表
     */
    public List<Role> listByOrg(Long orgId) {
        return roleMapper.selectListByQuery(
                QueryWrapper.create()
                        .eq("org_id", orgId)
                        .orderBy("sort_order", true)
        );
    }

    /**
     * 获取系统角色列表
     */
    public List<Role> listSystemRoles() {
        return roleMapper.selectListByQuery(
                QueryWrapper.create()
                        .isNull("org_id")
                        .orderBy("sort_order", true)
        );
    }

    /**
     * 获取总数
     */
    public long count() {
        return roleMapper.selectCountByQuery(QueryWrapper.create());
    }

    /**
     * 根据ID获取角色
     */
    public Role getById(Long id) {
        return roleMapper.selectOneById(id);
    }

    /**
     * 根据code获取角色
     */
    public Role getByCode(String code, Long orgId) {
        return roleMapper.selectOneByQuery(
                QueryWrapper.create()
                        .eq("code", code)
                        .eq("org_id", orgId)
        );
    }

    /**
     * 创建角色
     */
    public Role create(Role role) {
        // 检查code是否已存在
        Role existing = getByCode(role.getCode(), role.getOrgId());
        if (existing != null) {
            throw BizException.conflict("error.role.code-taken");
        }

        role.setCreatedAt(LocalDateTime.now());
        role.setStatus(1);
        role.setIsSystem(false);

        roleMapper.insert(role);
        return role;
    }

    /**
     * 更新角色
     */
    public void update(Long id, Role update) {
        Role existing = roleMapper.selectOneById(id);
        if (existing == null) {
            throw BizException.notFound("error.role.not-found");
        }

        // 系统角色不允许修改编码
        if (Boolean.TRUE.equals(existing.getIsSystem()) && update.getCode() != null) {
            throw BizException.badRequest("error.role.system-role-forbidden");
        }

        if (update.getName() != null) {
            existing.setName(update.getName());
        }
        if (update.getDescription() != null) {
            existing.setDescription(update.getDescription());
        }
        if (update.getSortOrder() != null) {
            existing.setSortOrder(update.getSortOrder());
        }

        roleMapper.update(existing);
    }

    /**
     * 禁用角色
     */
    public void disable(Long id) {
        Role role = roleMapper.selectOneById(id);
        if (role == null) {
            throw BizException.notFound("error.role.not-found");
        }
        if (Boolean.TRUE.equals(role.getIsSystem())) {
            throw BizException.badRequest("error.role.system-role-forbidden");
        }
        role.setStatus(0);
        roleMapper.update(role);
    }

    /**
     * 启用角色
     */
    public void enable(Long id) {
        Role role = roleMapper.selectOneById(id);
        if (role == null) {
            throw BizException.notFound("error.role.not-found");
        }
        role.setStatus(1);
        roleMapper.update(role);
    }

    /**
     * 删除角色
     */
    public void delete(Long id) {
        Role role = roleMapper.selectOneById(id);
        if (role == null) {
            throw BizException.notFound("error.role.not-found");
        }
        if (Boolean.TRUE.equals(role.getIsSystem())) {
            throw BizException.badRequest("error.role.system-role-forbidden");
        }
        roleMapper.deleteById(id);
    }

    /**
     * 获取用户在指定组织的角色
     */
    public List<Role> getUserRoles(Long userId, Long orgId) {
        // 查询用户角色关联
        var query = QueryWrapper.create().eq("user_id", userId);
        if (orgId != null) {
            query = query.eq("org_id", orgId);
        }
        List<UserRole> userRoles = userRoleMapper.selectListByQuery(query);

        if (userRoles.isEmpty()) {
            return List.of();
        }

        List<Long> roleIds = userRoles.stream()
                .map(UserRole::getRoleId)
                .distinct()
                .toList();

        return roleMapper.selectListByQuery(
                QueryWrapper.create()
                        .in("id", roleIds)
                        .eq("status", 1));
    }
}
