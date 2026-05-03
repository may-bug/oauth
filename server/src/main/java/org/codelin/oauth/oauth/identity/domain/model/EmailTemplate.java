package org.codelin.oauth.oauth.identity.domain.model;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 邮件模板实体
 */
@Data
@Table("sys_email_template")
public class EmailTemplate {

    @Id(keyType = KeyType.Generator, value = "snowflake")
    private Long id;

    private String code;

    private String name;

    private String subject;

    private String content;

    /**
     * 可用变量说明（JSON）
     */
    private String variables;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}
