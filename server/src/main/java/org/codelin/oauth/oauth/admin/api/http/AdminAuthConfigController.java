package org.codelin.oauth.oauth.admin.api.http;

import lombok.RequiredArgsConstructor;
import org.codelin.oauth.oauth.common.security.Permission;
import org.codelin.oauth.oauth.common.web.R;
import org.codelin.oauth.oauth.identity.application.AuthConfigService;
import org.codelin.oauth.oauth.identity.domain.model.AuthConfig;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理后台 - 认证方式配置
 */
@RestController
@RequestMapping("/admin/config/auth")
@RequiredArgsConstructor
public class AdminAuthConfigController {

    private final AuthConfigService authConfigService;

    /**
     * 获取认证方式配置列表
     */
    @Permission("config:read")
    @GetMapping(version = "1")
    public ResponseEntity<R<List<AuthConfig>>> list() {
        return R.ok(authConfigService.listAll());
    }

    /**
     * 批量更新认证方式配置
     */
    @Permission("config:write")
    @PutMapping(version = "1")
    public ResponseEntity<R<Void>> update(@RequestBody List<AuthConfig> configs) {
        authConfigService.update(configs);
        return R.ok();
    }
}
