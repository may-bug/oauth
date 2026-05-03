package org.codelin.oauth.oauth.identity.domain.model;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 加密配置实体
 */
@Data
@Table("sys_crypto_config")
public class CryptoConfig {

    @Id(keyType = KeyType.Generator, value = "snowflake")
    private Long id;

    private Boolean enabled;

    @Column("rsa_key_size")
    private Integer rsaKeySize;

    @Column("rsa_public_key")
    private String rsaPublicKey;

    @Column("rsa_private_key")
    private String rsaPrivateKey;

    @Column("key_expire_days")
    private Integer keyExpireDays;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}
