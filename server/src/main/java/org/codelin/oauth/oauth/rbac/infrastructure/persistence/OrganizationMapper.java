package org.codelin.oauth.oauth.rbac.infrastructure.persistence;

import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.codelin.oauth.oauth.rbac.domain.model.Organization;

/**
 * 组织Mapper
 */
@Mapper
public interface OrganizationMapper extends BaseMapper<Organization> {
}
