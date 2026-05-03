package org.codelin.oauth.oauth.rbac.application;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codelin.oauth.oauth.common.web.BizException;
import org.codelin.oauth.oauth.rbac.domain.model.Organization;
import org.codelin.oauth.oauth.rbac.domain.model.UserOrganization;
import org.codelin.oauth.oauth.rbac.infrastructure.persistence.OrganizationMapper;
import org.codelin.oauth.oauth.rbac.infrastructure.persistence.UserOrganizationMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 组织服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationMapper organizationMapper;
    private final UserOrganizationMapper userOrganizationMapper;

    /**
     * 获取组织列表
     */
    public List<Organization> list(int page, int size) {
        return organizationMapper.selectListByQuery(
                QueryWrapper.create()
                        .orderBy("sort_order", true)
                        .limit(size)
                        .offset((page - 1) * size)
        );
    }

    /**
     * 获取总数
     */
    public long count() {
        return organizationMapper.selectCountByQuery(QueryWrapper.create());
    }

    /**
     * 根据ID获取组织
     */
    public Organization getById(Long id) {
        return organizationMapper.selectOneById(id);
    }

    /**
     * 根据code获取组织
     */
    public Organization getByCode(String code) {
        return organizationMapper.selectOneByQuery(
                QueryWrapper.create().eq("code", code)
        );
    }

    /**
     * 创建组织
     */
    public Organization create(Organization org) {
        // 检查code是否已存在
        if (getByCode(org.getCode()) != null) {
            throw BizException.conflict("error.org.code-taken");
        }

        org.setCreatedAt(LocalDateTime.now());
        org.setUpdatedAt(LocalDateTime.now());
        org.setStatus(1);

        organizationMapper.insert(org);
        return org;
    }

    /**
     * 更新组织
     */
    public void update(Long id, Organization update) {
        Organization existing = organizationMapper.selectOneById(id);
        if (existing == null) {
            throw BizException.notFound("error.org.not-found");
        }

        if (update.getName() != null) {
            existing.setName(update.getName());
        }
        if (update.getParentId() != null) {
            existing.setParentId(update.getParentId());
        }
        if (update.getSortOrder() != null) {
            existing.setSortOrder(update.getSortOrder());
        }

        existing.setUpdatedAt(LocalDateTime.now());
        organizationMapper.update(existing);
    }

    /**
     * 禁用组织
     */
    public void disable(Long id) {
        Organization org = organizationMapper.selectOneById(id);
        if (org == null) {
            throw BizException.notFound("error.org.not-found");
        }
        org.setStatus(0);
        org.setUpdatedAt(LocalDateTime.now());
        organizationMapper.update(org);
    }

    /**
     * 启用组织
     */
    public void enable(Long id) {
        Organization org = organizationMapper.selectOneById(id);
        if (org == null) {
            throw BizException.notFound("error.org.not-found");
        }
        org.setStatus(1);
        org.setUpdatedAt(LocalDateTime.now());
        organizationMapper.update(org);
    }

    /**
     * 删除组织
     */
    public void delete(Long id) {
        organizationMapper.deleteById(id);
    }

    /**
     * 获取用户所在的组织列表
     */
    public List<Organization> getUserOrganizations(Long userId) {
        List<UserOrganization> userOrgs = userOrganizationMapper.selectListByQuery(
                QueryWrapper.create()
                        .eq("user_id", userId)
                        .eq("status", 1)
        );
        List<Long> orgIds = userOrgs.stream()
                .map(UserOrganization::getOrgId)
                .toList();
        if (orgIds.isEmpty()) {
            return List.of();
        }
        return organizationMapper.selectListByQuery(
                QueryWrapper.create().in("id", orgIds)
        );
    }

    /**
     * 添加用户到组织
     */
    public void addUserToOrganization(Long userId, Long orgId, Long roleId) {
        // 检查是否已在组织中
        UserOrganization existing = userOrganizationMapper.selectOneByQuery(
                QueryWrapper.create()
                        .eq("user_id", userId)
                        .eq("org_id", orgId)
        );
        if (existing != null) {
            throw BizException.conflict("error.org.user-already-in");
        }

        UserOrganization userOrg = new UserOrganization();
        userOrg.setUserId(userId);
        userOrg.setOrgId(orgId);
        userOrg.setRoleId(roleId);
        userOrg.setStatus(1);
        userOrg.setJoinedAt(LocalDateTime.now());

        userOrganizationMapper.insert(userOrg);
    }

    /**
     * 从组织移除用户
     */
    public void removeUserFromOrganization(Long userId, Long orgId) {
        userOrganizationMapper.deleteByQuery(
                QueryWrapper.create()
                        .eq("user_id", userId)
                        .eq("org_id", orgId)
        );
    }
}
