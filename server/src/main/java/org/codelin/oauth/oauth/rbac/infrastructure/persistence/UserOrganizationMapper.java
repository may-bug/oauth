package org.codelin.oauth.oauth.rbac.infrastructure.persistence;

import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.codelin.oauth.oauth.rbac.domain.model.UserOrganization;

/**
 * 用户组织关联Mapper
 */
@Mapper
public interface UserOrganizationMapper extends BaseMapper<UserOrganization> {
}
