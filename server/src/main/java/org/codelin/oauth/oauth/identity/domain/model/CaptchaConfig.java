package org.codelin.oauth.oauth.identity.domain.model;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 验证码配置实体
 */
@Data
@Table("sys_captcha_config")
public class CaptchaConfig {

    @Id(keyType = KeyType.Generator, value = "snowflake")
    private Long id;

    private Boolean enabled;

    @Column("captcha_type")
    private String captchaType;

    private Integer length;

    @Column("expire_seconds")
    private Integer expireSeconds;

    @Column("case_sensitive")
    private Boolean caseSensitive;

    @Column("noise_lines")
    private Integer noiseLines;

    @Column("noise_dots")
    private Integer noiseDots;

    private Boolean rotation;

    private Boolean wave;

    @Column("updated_at")
    private LocalDateTime updatedAt;

    @Column("updated_by")
    private Long updatedBy;
}
