package org.codelin.oauth.oauth.admin.api.http;

import lombok.RequiredArgsConstructor;
import org.codelin.oauth.oauth.common.security.Permission;
import org.codelin.oauth.oauth.common.web.R;
import org.codelin.oauth.oauth.identity.application.CryptoConfigService;
import org.codelin.oauth.oauth.identity.domain.model.CryptoConfig;
import org.codelin.oauth.oauth.identity.domain.model.CryptoEndpointConfig;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理后台 - 加密配置管理
 */
@RestController
@RequestMapping("/admin/config/crypto")
@RequiredArgsConstructor
public class AdminCryptoConfigController {

    private final CryptoConfigService cryptoConfigService;

    /**
     * 获取加密配置
     */
    @Permission("config:read")
    @GetMapping(version = "1")
    public ResponseEntity<R<CryptoConfig>> getConfig() {
        CryptoConfig config = cryptoConfigService.getConfig();
        // 不返回私钥
        config.setRsaPrivateKey(null);
        return R.ok(config);
    }

    /**
     * 更新加密配置
     */
    @Permission("config:write")
    @PutMapping(version = "1")
    public ResponseEntity<R<Void>> updateConfig(@RequestBody CryptoConfig config) {
        cryptoConfigService.updateConfig(config);
        return R.ok();
    }

    /**
     * 轮换密钥对
     */
    @Permission("config:write")
    @PostMapping(value = "/rotate-keys", version = "1")
    public ResponseEntity<R<Map<String, String>>> rotateKeys() {
        cryptoConfigService.rotateKeys();
        String publicKey = cryptoConfigService.getPublicKey();
        return R.ok(Map.of("publicKey", publicKey));
    }

    /**
     * 获取需要加密的端点列表
     */
    @Permission("config:read")
    @GetMapping(value = "/endpoints", version = "1")
    public ResponseEntity<R<List<CryptoEndpointConfig>>> getEndpoints() {
        return R.ok(cryptoConfigService.getEndpointConfigs());
    }

    /**
     * 添加需要加密的端点
     */
    @Permission("config:write")
    @PostMapping(value = "/endpoints", version = "1")
    public ResponseEntity<R<Void>> addEndpoint(@RequestBody CryptoEndpointConfig config) {
        cryptoConfigService.updateEndpointConfig(config);
        return R.ok();
    }

    /**
     * 更新端点配置
     */
    @Permission("config:write")
    @PutMapping(value = "/endpoints/{id}", version = "1")
    public ResponseEntity<R<Void>> updateEndpoint(@PathVariable Long id,
                                                   @RequestBody CryptoEndpointConfig config) {
        config.setId(id);
        cryptoConfigService.updateEndpointConfig(config);
        return R.ok();
    }

    /**
     * 删除端点配置
     */
    @Permission("config:write")
    @DeleteMapping(value = "/endpoints/{id}", version = "1")
    public ResponseEntity<R<Void>> deleteEndpoint(@PathVariable Long id) {
        cryptoConfigService.deleteEndpointConfig(id);
        return R.ok();
    }
}
