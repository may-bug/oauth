package org.codelin.oauth.oauth.admin.api.http;

import lombok.RequiredArgsConstructor;
import org.codelin.oauth.oauth.common.security.Permission;
import org.codelin.oauth.oauth.common.web.R;
import org.codelin.oauth.oauth.identity.application.social.SocialProviderService;
import org.codelin.oauth.oauth.identity.domain.model.SocialProviderConfig;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理后台 - 社交登录提供商配置管理
 */
@RestController
@RequestMapping("/admin/config/social-providers")
@RequiredArgsConstructor
public class AdminSocialProviderController {

    private final SocialProviderService socialProviderService;

    /**
     * 获取所有提供商列表
     */
    @Permission("config:read")
    @GetMapping(version = "1")
    public ResponseEntity<R<List<SocialProviderConfig>>> list() {
        List<SocialProviderConfig> list = socialProviderService.listAll();
        list.forEach(this::maskSecret);
        return R.ok(list);
    }

    /**
     * 获取单个提供商配置
     */
    @Permission("config:read")
    @GetMapping(value = "/{provider}", version = "1")
    public ResponseEntity<R<SocialProviderConfig>> get(@PathVariable String provider) {
        SocialProviderConfig config = socialProviderService.getByProvider(provider);
        if (config == null) {
            return R.notFound("error.not-found");
        }
        maskSecret(config);
        return R.ok(config);
    }

    /**
     * 创建提供商
     */
    @Permission("config:write")
    @PostMapping(version = "1")
    public ResponseEntity<R<Void>> create(@RequestBody SocialProviderConfig config) {
        socialProviderService.createOrUpdate(config);
        return R.ok();
    }

    /**
     * 更新提供商配置
     */
    @Permission("config:write")
    @PutMapping(value = "/{provider}", version = "1")
    public ResponseEntity<R<Void>> update(@PathVariable String provider,
                                           @RequestBody SocialProviderConfig config) {
        config.setProvider(provider);
        socialProviderService.createOrUpdate(config);
        return R.ok();
    }

    /**
     * 删除提供商
     */
    @Permission("config:write")
    @DeleteMapping(value = "/{provider}", version = "1")
    public ResponseEntity<R<Void>> delete(@PathVariable String provider) {
        socialProviderService.delete(provider);
        return R.ok();
    }

    /**
     * 启用/禁用提供商
     */
    @Permission("config:write")
    @PutMapping(value = "/{provider}/toggle", version = "1")
    public ResponseEntity<R<Void>> toggle(@PathVariable String provider,
                                           @RequestBody Map<String, Boolean> body) {
        Boolean enabled = body.get("enabled");
        if (enabled == null) {
            return R.fail(400, "enabled不能为空");
        }
        socialProviderService.toggleEnabled(provider, enabled);
        return R.ok();
    }

    /**
     * 脱敏处理 - 隐藏client_secret
     */
    private void maskSecret(SocialProviderConfig config) {
        if (config.getClientSecret() != null && !config.getClientSecret().isEmpty()) {
            config.setClientSecret("******");
        }
    }
}
