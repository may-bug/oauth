package org.codelin.oauth.oauth.identity.infrastructure.persistence;

import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.codelin.oauth.oauth.identity.domain.model.CryptoEndpointConfig;

/**
 * 加密端点配置Mapper
 */
@Mapper
public interface CryptoEndpointConfigMapper extends BaseMapper<CryptoEndpointConfig> {
}
