package org.codelin.oauth.oauth.common.security;

/**
 * 密码编码器接口
 */
public interface PasswordEncoder {

    /**
     * 编码原始密码
     */
    String encode(CharSequence rawPassword);

    /**
     * 验证密码是否匹配
     */
    boolean matches(CharSequence rawPassword, String encodedPassword);
}
