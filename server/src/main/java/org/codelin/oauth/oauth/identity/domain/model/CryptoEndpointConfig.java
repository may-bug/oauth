package org.codelin.oauth.oauth.identity.domain.model;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

/**
 * 加密端点配置实体
 */
@Data
@Table("sys_crypto_endpoint_config")
public class CryptoEndpointConfig {

    @Id(keyType = KeyType.Generator, value = "snowflake")
    private Long id;

    private String endpoint;

    private Boolean enabled;

    @Column("encrypted_fields")
    private String encryptedFields;
}
