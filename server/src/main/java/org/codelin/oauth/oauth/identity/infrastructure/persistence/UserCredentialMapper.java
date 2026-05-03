package org.codelin.oauth.oauth.identity.infrastructure.persistence;

import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.codelin.oauth.oauth.identity.domain.model.UserCredential;

/**
 * 用户凭证Mapper
 */
@Mapper
public interface UserCredentialMapper extends BaseMapper<UserCredential> {
}
