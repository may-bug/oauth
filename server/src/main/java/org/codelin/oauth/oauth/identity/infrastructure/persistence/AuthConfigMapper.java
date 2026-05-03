package org.codelin.oauth.oauth.identity.infrastructure.persistence;

import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.codelin.oauth.oauth.identity.domain.model.AuthConfig;

/**
 * 认证方式配置Mapper
 */
@Mapper
public interface AuthConfigMapper extends BaseMapper<AuthConfig> {
}
