package org.codelin.oauth.oauth.identity.domain.model;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

/**
 * 验证码端点配置实体
 */
@Data
@Table("sys_captcha_endpoint_config")
public class CaptchaEndpointConfig {

    @Id(keyType = KeyType.Generator, value = "snowflake")
    private Long id;

    private String endpoint;

    private Boolean enabled;

    private String description;
}
