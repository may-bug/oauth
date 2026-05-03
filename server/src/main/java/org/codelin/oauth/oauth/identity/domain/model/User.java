package org.codelin.oauth.oauth.identity.domain.model;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户实体
 */
@Data
@Table("sys_user")
public class User {

    @Id(keyType = KeyType.Generator, value = "snowflake")
    private Long id;

    private String username;

    private String password;

    private String nickname;

    private String realName;

    private String avatar;

    /**
     * 0=未知, 1=男, 2=女
     */
    private Integer gender;

    private LocalDate birthday;

    private String bio;

    /**
     * 0=禁用, 1=正常, 2=锁定
     */
    private Integer status;

    @Column("email_verified")
    private Boolean emailVerified;

    @Column("phone_verified")
    private Boolean phoneVerified;

    @Column("last_login_at")
    private LocalDateTime lastLoginAt;

    @Column("last_login_ip")
    private String lastLoginIp;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;

    @Column("deleted")
    private Integer deleted;
}
