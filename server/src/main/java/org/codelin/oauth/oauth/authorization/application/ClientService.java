package org.codelin.oauth.oauth.authorization.application;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codelin.oauth.oauth.authorization.domain.model.OAuth2Client;
import org.codelin.oauth.oauth.authorization.infrastructure.persistence.OAuth2ClientMapper;
import org.codelin.oauth.oauth.common.web.BizException;
import org.codelin.oauth.oauth.common.security.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

import static org.codelin.oauth.oauth.authorization.domain.model.TableDefs.OAUTH2_CLIENT;

/**
 * OAuth2客户端服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClientService {

    private final OAuth2ClientMapper clientMapper;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * 根据clientId获取客户端
     */
    public OAuth2Client getByClientId(String clientId) {
        return clientMapper.selectOneByQuery(
                QueryWrapper.create().where(OAUTH2_CLIENT.CLIENT_ID.eq(clientId))
        );
    }

    /**
     * 获取客户端列表
     */
    public List<OAuth2Client> list(int page, int size) {
        return clientMapper.selectListByQuery(
                QueryWrapper.create()
                        .orderBy(OAUTH2_CLIENT.CREATED_AT.desc())
                        .limit(size)
                        .offset((page - 1) * size)
        );
    }

    /**
     * 获取总数
     */
    public long count() {
        return clientMapper.selectCountByQuery(new QueryWrapper());
    }

    /**
     * 根据ID获取
     */
    public OAuth2Client getById(Long id) {
        return clientMapper.selectOneById(id);
    }

    /**
     * 创建客户端
     */
    public OAuth2Client create(OAuth2Client client) {
        // 检查clientId是否已存在
        if (getByClientId(client.getClientId()) != null) {
            throw BizException.conflict("error.oauth.client-id-taken");
        }

        // 加密client_secret
        if (client.getClientSecret() != null && !client.getClientSecret().isEmpty()) {
            client.setClientSecret(passwordEncoder.encode(client.getClientSecret()));
        }

        client.setCreatedAt(LocalDateTime.now());
        client.setUpdatedAt(LocalDateTime.now());
        client.setStatus(1);

        clientMapper.insert(client);
        return client;
    }

    /**
     * 更新客户端
     */
    public void update(Long id, OAuth2Client update) {
        OAuth2Client existing = clientMapper.selectOneById(id);
        if (existing == null) {
            throw BizException.notFound("error.oauth.client-not-found");
        }

        // 更新字段
        if (update.getClientName() != null) {
            existing.setClientName(update.getClientName());
        }
        if (update.getClientType() != null) {
            existing.setClientType(update.getClientType());
        }
        if (update.getRedirectUris() != null) {
            existing.setRedirectUris(update.getRedirectUris());
        }
        if (update.getAllowedScopes() != null) {
            existing.setAllowedScopes(update.getAllowedScopes());
        }
        if (update.getAccessTokenTtl() != null) {
            existing.setAccessTokenTtl(update.getAccessTokenTtl());
        }
        if (update.getRefreshTokenTtl() != null) {
            existing.setRefreshTokenTtl(update.getRefreshTokenTtl());
        }

        existing.setUpdatedAt(LocalDateTime.now());
        clientMapper.update(existing);
    }

    /**
     * 重置客户端密钥
     */
    public String resetSecret(Long id) {
        OAuth2Client client = clientMapper.selectOneById(id);
        if (client == null) {
            throw BizException.notFound("error.oauth.client-not-found");
        }

        // 生成新的密钥
        String newSecret = generateSecret();
        client.setClientSecret(passwordEncoder.encode(newSecret));
        client.setUpdatedAt(LocalDateTime.now());
        clientMapper.update(client);

        return newSecret;
    }

    /**
     * 禁用客户端
     */
    public void disable(Long id) {
        OAuth2Client client = clientMapper.selectOneById(id);
        if (client == null) {
            throw BizException.notFound("error.oauth.client-not-found");
        }
        client.setStatus(0);
        client.setUpdatedAt(LocalDateTime.now());
        clientMapper.update(client);
    }

    /**
     * 启用客户端
     */
    public void enable(Long id) {
        OAuth2Client client = clientMapper.selectOneById(id);
        if (client == null) {
            throw BizException.notFound("error.oauth.client-not-found");
        }
        client.setStatus(1);
        client.setUpdatedAt(LocalDateTime.now());
        clientMapper.update(client);
    }

    /**
     * 删除客户端
     */
    public void delete(Long id) {
        clientMapper.deleteById(id);
    }

    /**
     * 验证客户端密钥
     */
    public boolean validateSecret(String clientId, String clientSecret) {
        OAuth2Client client = getByClientId(clientId);
        if (client == null || client.getStatus() != 1) {
            return false;
        }
        return passwordEncoder.matches(clientSecret, client.getClientSecret());
    }

    /**
     * 获取客户端允许的回调地址
     */
    public List<String> getRedirectUris(String clientId) {
        OAuth2Client client = getByClientId(clientId);
        if (client == null || client.getRedirectUris() == null) {
            return List.of();
        }
        try {
            return objectMapper.readValue(client.getRedirectUris(),
                    new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.error("Failed to parse redirect_uris: {}", client.getRedirectUris(), e);
            return List.of();
        }
    }

    /**
     * 获取客户端允许的scope
     */
    public List<String> getAllowedScopes(String clientId) {
        OAuth2Client client = getByClientId(clientId);
        if (client == null || client.getAllowedScopes() == null) {
            return List.of();
        }
        try {
            return objectMapper.readValue(client.getAllowedScopes(),
                    new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.error("Failed to parse allowed_scopes: {}", client.getAllowedScopes(), e);
            return List.of();
        }
    }

    /**
     * 生成随机密钥
     */
    private String generateSecret() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
