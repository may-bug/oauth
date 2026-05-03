package org.codelin.oauth.oauth.identity.infrastructure.persistence;

import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.codelin.oauth.oauth.identity.domain.model.EmailTemplate;

/**
 * 邮件模板Mapper
 */
@Mapper
public interface EmailTemplateMapper extends BaseMapper<EmailTemplate> {
}
