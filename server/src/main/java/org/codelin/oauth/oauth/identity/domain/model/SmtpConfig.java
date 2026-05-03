package org.codelin.oauth.oauth.identity.domain.model;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * SMTP配置实体
 */
@Data
@Table("sys_smtp_config")
public class SmtpConfig {

    @Id(keyType = KeyType.Generator, value = "snowflake")
    private Long id;

    private String host;

    private Integer port;

    private String username;

    private String password;

    @Column("from_address")
    private String fromAddress;

    @Column("from_name")
    private String fromName;

    @Column("ssl_enabled")
    private Boolean sslEnabled;

    private Boolean enabled;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}
