package org.codelin.oauth.oauth.identity.application;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codelin.oauth.oauth.common.web.BizException;
import org.codelin.oauth.oauth.identity.domain.model.User;
import org.codelin.oauth.oauth.identity.domain.model.UserCredential;
import org.codelin.oauth.oauth.identity.infrastructure.persistence.UserCredentialMapper;
import org.codelin.oauth.oauth.identity.infrastructure.persistence.UserMapper;
import org.codelin.oauth.oauth.common.security.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.codelin.oauth.oauth.identity.domain.model.TableDefs.USER;
import static org.codelin.oauth.oauth.identity.domain.model.TableDefs.USER_CREDENTIAL;

/**
 * 用户服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final UserCredentialMapper credentialMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * 根据ID获取用户
     */
    public User getById(Long id) {
        return userMapper.selectOneById(id);
    }

    /**
     * 根据用户名获取用户
     */
    public User getByUsername(String username) {
        return userMapper.selectOneByQuery(
                QueryWrapper.create()
                        .where(USER.USERNAME.eq(username))
        );
    }

    /**
     * 根据凭证获取用户
     */
    public User getByCredential(String credentialType, String credentialKey) {
        UserCredential credential = credentialMapper.selectOneByQuery(
                QueryWrapper.create()
                        .where(USER_CREDENTIAL.CREDENTIAL_TYPE.eq(credentialType))
                        .and(USER_CREDENTIAL.CREDENTIAL_KEY.eq(credentialKey))
        );
        if (credential == null) {
            return null;
        }
        return getById(credential.getUserId());
    }

    /**
     * 获取用户的凭证
     */
    public List<UserCredential> getCredentials(Long userId) {
        return credentialMapper.selectListByQuery(
                QueryWrapper.create()
                        .where(USER_CREDENTIAL.USER_ID.eq(userId))
        );
    }

    /**
     * 获取用户指定类型的凭证
     */
    public UserCredential getCredential(Long userId, String credentialType) {
        return credentialMapper.selectOneByQuery(
                QueryWrapper.create()
                        .where(USER_CREDENTIAL.USER_ID.eq(userId))
                        .and(USER_CREDENTIAL.CREDENTIAL_TYPE.eq(credentialType))
        );
    }

    /**
     * 创建用户
     */
    @Transactional
    public User createUser(String username, String password, String nickname) {
        // 检查用户名是否存在
        if (getByUsername(username) != null) {
            throw BizException.conflict("error.user.already-exists");
        }

        // 创建用户
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(nickname != null ? nickname : username);
        user.setStatus(1);
        user.setEmailVerified(false);
        user.setPhoneVerified(false);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(user);

        // 创建密码凭证
        UserCredential credential = new UserCredential();
        credential.setUserId(user.getId());
        credential.setCredentialType("password");
        credential.setCredentialKey(username);
        credential.setCredentialValue(user.getPassword());
        credential.setVerified(true);
        credential.setPrimaryFlag(true);
        credential.setCreatedAt(LocalDateTime.now());
        credentialMapper.insert(credential);

        log.info("User created: id={}, username={}", user.getId(), username);
        return user;
    }

    /**
     * 创建用户（通过邮箱）
     */
    @Transactional
    public User createUserByEmail(String email, String password) {
        // 检查邮箱是否已注册
        if (getByCredential("email", email) != null) {
            throw BizException.conflict("error.user.email-taken");
        }

        // 创建用户
        User user = createUser(email, password, email.substring(0, email.indexOf('@')));

        // 创建邮箱凭证
        UserCredential credential = new UserCredential();
        credential.setUserId(user.getId());
        credential.setCredentialType("email");
        credential.setCredentialKey(email);
        credential.setVerified(true);
        credential.setPrimaryFlag(false);
        credential.setCreatedAt(LocalDateTime.now());
        credentialMapper.insert(credential);

        return user;
    }

    /**
     * 创建用户（通过手机号）
     */
    @Transactional
    public User createUserByPhone(String phone, String password) {
        // 检查手机号是否已注册
        if (getByCredential("phone", phone) != null) {
            throw BizException.conflict("error.user.phone-taken");
        }

        // 创建用户
        User user = createUser(phone, password, "用户" + phone.substring(phone.length() - 4));

        // 创建手机凭证
        UserCredential credential = new UserCredential();
        credential.setUserId(user.getId());
        credential.setCredentialType("phone");
        credential.setCredentialKey(phone);
        credential.setVerified(true);
        credential.setPrimaryFlag(false);
        credential.setCreatedAt(LocalDateTime.now());
        credentialMapper.insert(credential);

        return user;
    }

    /**
     * 更新用户信息
     */
    @Transactional
    public void updateUser(Long id, User update) {
        User user = getById(id);
        if (user == null) {
            throw BizException.notFound("error.user.not-found");
        }

        if (update.getNickname() != null) {
            user.setNickname(update.getNickname());
        }
        if (update.getRealName() != null) {
            user.setRealName(update.getRealName());
        }
        if (update.getGender() != null) {
            user.setGender(update.getGender());
        }
        if (update.getBirthday() != null) {
            user.setBirthday(update.getBirthday());
        }
        if (update.getBio() != null) {
            user.setBio(update.getBio());
        }
        if (update.getAvatar() != null) {
            user.setAvatar(update.getAvatar());
        }

        user.setUpdatedAt(LocalDateTime.now());
        userMapper.update(user);
    }

    /**
     * 更新用户状态
     */
    @Transactional
    public void updateStatus(Long id, Integer status) {
        User user = getById(id);
        if (user == null) {
            throw BizException.notFound("error.user.not-found");
        }
        user.setStatus(status);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.update(user);
    }

    /**
     * 修改密码
     */
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = getById(userId);
        if (user == null) {
            throw BizException.notFound("error.user.not-found");
        }

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw BizException.badRequest("error.user.password-mismatch");
        }

        // 只更新密码字段
        String encodedPassword = passwordEncoder.encode(newPassword);
        User partial = new User();
        partial.setId(userId);
        partial.setPassword(encodedPassword);
        partial.setUpdatedAt(LocalDateTime.now());
        userMapper.update(partial);

        // 更新密码凭证
        UserCredential credential = getCredential(userId, "password");
        if (credential != null) {
            credential.setCredentialValue(encodedPassword);
            credentialMapper.update(credential);
        }

        log.info("Password changed for user: {}", userId);
    }

    /**
     * 重置密码（管理员操作）
     */
    @Transactional
    public void resetPassword(Long userId, String newPassword) {
        User user = getById(userId);
        if (user == null) {
            throw BizException.notFound("error.user.not-found");
        }

        String encodedPassword = passwordEncoder.encode(newPassword);
        User partial = new User();
        partial.setId(userId);
        partial.setPassword(encodedPassword);
        partial.setUpdatedAt(LocalDateTime.now());
        userMapper.update(partial);

        // 更新密码凭证
        UserCredential credential = getCredential(userId, "password");
        if (credential != null) {
            credential.setCredentialValue(encodedPassword);
            credentialMapper.update(credential);
        }

        log.info("Password reset for user: {}", userId);
    }

    /**
     * 验证密码
     */
    public boolean verifyPassword(Long userId, String password) {
        User user = getById(userId);
        if (user == null) {
            return false;
        }
        return passwordEncoder.matches(password, user.getPassword());
    }

    /**
     * 更新最后登录信息
     */
    public void updateLastLogin(Long userId, String ip) {
        User update = new User();
        update.setId(userId);
        update.setLastLoginAt(LocalDateTime.now());
        update.setLastLoginIp(ip);
        userMapper.update(update);
    }

    public void updateAvatar(Long userId, String avatarUrl) {
        User u = new User();
        u.setId(userId);
        u.setAvatar(avatarUrl);
        u.setUpdatedAt(LocalDateTime.now());
        userMapper.update(u);
    }

    /**
     * 分页查询用户
     */
    public List<User> list(int page, int size) {
        return userMapper.paginate(page, size,
                QueryWrapper.create().orderBy(USER.CREATED_AT.desc())
        ).getRecords();
    }

    /**
     * 用户总数
     */
    public long count() {
        return userMapper.selectCountByQuery(QueryWrapper.create());
    }

    /**
     * 保存凭证
     */
    public void saveCredential(UserCredential credential) {
        credentialMapper.insert(credential);
    }

    /**
     * 更新凭证
     */
    public void updateCredential(UserCredential credential) {
        credentialMapper.update(credential);
    }

    /**
     * 绑定社交账号
     */
    @Transactional
    public void bindSocialAccount(Long userId, String provider, String providerUid) {
        // 检查是否已绑定
        UserCredential existing = getCredential("social_" + provider, providerUid);
        if (existing != null) {
            if (existing.getUserId().equals(userId)) {
                return; // 已绑定到当前用户
            }
            throw BizException.conflict("error.user.social-already-bound");
        }

        UserCredential credential = new UserCredential();
        credential.setUserId(userId);
        credential.setCredentialType("social_" + provider);
        credential.setCredentialKey(providerUid);
        credential.setVerified(true);
        credential.setPrimaryFlag(false);
        credential.setCreatedAt(LocalDateTime.now());
        credentialMapper.insert(credential);

        log.info("Social account bound: userId={}, provider={}, uid={}", userId, provider, providerUid);
    }

    /**
     * 解绑社交账号
     */
    @Transactional
    public void unbindSocialAccount(Long userId, String provider) {
        UserCredential credential = getCredential(userId, "social_" + provider);
        if (credential == null) {
            throw BizException.badRequest("error.user.social-not-bound");
        }
        credentialMapper.deleteById(credential.getId());

        log.info("Social account unbound: userId={}, provider={}", userId, provider);
    }

    /**
     * 根据凭证类型和key获取凭证（检查唯一性）
     */
    private UserCredential getCredential(String credentialType, String credentialKey) {
        return credentialMapper.selectOneByQuery(
                QueryWrapper.create()
                        .where(USER_CREDENTIAL.CREDENTIAL_TYPE.eq(credentialType))
                        .and(USER_CREDENTIAL.CREDENTIAL_KEY.eq(credentialKey))
        );
    }
}
