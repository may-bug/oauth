package org.codelin.oauth.oauth.authorization.infrastructure.persistence;

import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.codelin.oauth.oauth.authorization.domain.model.OAuth2Client;

/**
 * OAuth2客户端Mapper
 */
@Mapper
public interface OAuth2ClientMapper extends BaseMapper<OAuth2Client> {
}
