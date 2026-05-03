package org.codelin.oauth.oauth.identity.infrastructure.persistence;

import com.mybatisflex.core.BaseMapper;
import org.codelin.oauth.oauth.identity.domain.model.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
