package org.codelin.oauth.oauth.identity.infrastructure.persistence;

import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.codelin.oauth.oauth.identity.domain.model.SocialProviderConfig;

@Mapper
public interface SocialProviderConfigMapper extends BaseMapper<SocialProviderConfig> {
}
