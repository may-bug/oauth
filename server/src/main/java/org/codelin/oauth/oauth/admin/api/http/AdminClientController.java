package org.codelin.oauth.oauth.admin.api.http;

import lombok.RequiredArgsConstructor;
import org.codelin.oauth.oauth.authorization.application.ClientService;
import org.codelin.oauth.oauth.authorization.domain.model.OAuth2Client;
import org.codelin.oauth.oauth.common.security.Permission;
import org.codelin.oauth.oauth.common.web.BizException;
import org.codelin.oauth.oauth.common.web.R;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理后台 - OAuth2客户端管理
 */
@RestController
@RequestMapping("/admin/clients")
@RequiredArgsConstructor
public class AdminClientController {

    private final ClientService clientService;

    /**
     * 客户端列表
     */
    @Permission("client:read")
    @GetMapping(version = "1")
    public ResponseEntity<R<R.PageResult<OAuth2Client>>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<OAuth2Client> clients = clientService.list(page, size);
        long total = clientService.count();
        // 清除敏感信息
        clients.forEach(c -> c.setClientSecret(null));
        return R.okPage(clients, total, page, size);
    }

    /**
     * 获取客户端详情
     */
    @Permission("client:read")
    @GetMapping(value = "/{id}", version = "1")
    public ResponseEntity<R<OAuth2Client>> getById(@PathVariable Long id) {
        OAuth2Client client = clientService.getById(id);
        if (client == null) {
            throw BizException.notFound("error.oauth.client-not-found");
        }
        // 清除敏感信息
        client.setClientSecret(null);
        return R.ok(client);
    }

    /**
     * 创建客户端
     */
    @Permission("client:write")
    @PostMapping(version = "1")
    public ResponseEntity<R<Map<String, String>>> create(@RequestBody OAuth2Client client) {
        // 生成client_id和client_secret
        if (client.getClientId() == null || client.getClientId().isEmpty()) {
            client.setClientId(java.util.UUID.randomUUID().toString().replace("-", ""));
        }
        String secret = null;
        if (client.getClientSecret() == null || client.getClientSecret().isEmpty()) {
            secret = java.util.UUID.randomUUID().toString().replace("-", "");
            client.setClientSecret(secret);
        }

        clientService.create(client);

        // 返回client_id和明文client_secret（仅此一次）
        return R.ok(Map.of(
                "client_id", client.getClientId(),
                "client_secret", secret != null ? secret : "(已设置)"
        ));
    }

    /**
     * 更新客户端
     */
    @Permission("client:write")
    @PutMapping(value = "/{id}", version = "1")
    public ResponseEntity<R<Void>> update(@PathVariable Long id,
                                           @RequestBody OAuth2Client update) {
        clientService.update(id, update);
        return R.ok();
    }

    /**
     * 重置客户端密钥
     */
    @Permission("client:manage")
    @PostMapping(value = "/{id}/reset-secret", version = "1")
    public ResponseEntity<R<Map<String, String>>> resetSecret(@PathVariable Long id) {
        String newSecret = clientService.resetSecret(id);
        return R.ok(Map.of("client_secret", newSecret));
    }

    /**
     * 禁用客户端
     */
    @Permission("client:manage")
    @PostMapping(value = "/{id}/disable", version = "1")
    public ResponseEntity<R<Void>> disable(@PathVariable Long id) {
        clientService.disable(id);
        return R.ok();
    }

    /**
     * 启用客户端
     */
    @Permission("client:manage")
    @PostMapping(value = "/{id}/enable", version = "1")
    public ResponseEntity<R<Void>> enable(@PathVariable Long id) {
        clientService.enable(id);
        return R.ok();
    }

    /**
     * 删除客户端
     */
    @Permission("client:manage")
    @DeleteMapping(value = "/{id}", version = "1")
    public ResponseEntity<R<Void>> delete(@PathVariable Long id) {
        clientService.delete(id);
        return R.ok();
    }
}
