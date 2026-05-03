package org.codelin.oauth.oauth.admin.api.http;

import lombok.RequiredArgsConstructor;
import org.codelin.oauth.oauth.common.security.Permission;
import org.codelin.oauth.oauth.common.web.R;
import org.codelin.oauth.oauth.identity.application.CaptchaConfigService;
import org.codelin.oauth.oauth.identity.domain.model.CaptchaConfig;
import org.codelin.oauth.oauth.identity.domain.model.CaptchaEndpointConfig;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理后台 - 验证码配置管理
 */
@RestController
@RequestMapping("/admin/config/captcha")
@RequiredArgsConstructor
public class AdminCaptchaConfigController {

    private final CaptchaConfigService captchaConfigService;

    /**
     * 获取验证码配置
     */
    @Permission("config:read")
    @GetMapping(version = "1")
    public ResponseEntity<R<CaptchaConfig>> getConfig() {
        return R.ok(captchaConfigService.getConfig());
    }

    /**
     * 更新验证码配置
     */
    @Permission("config:write")
    @PutMapping(version = "1")
    public ResponseEntity<R<Void>> updateConfig(@RequestBody CaptchaConfig config) {
        captchaConfigService.updateConfig(config);
        return R.ok();
    }

    /**
     * 获取需要验证码的端点列表
     */
    @Permission("config:read")
    @GetMapping(value = "/endpoints", version = "1")
    public ResponseEntity<R<List<CaptchaEndpointConfig>>> getEndpoints() {
        return R.ok(captchaConfigService.getEndpointConfigs());
    }

    /**
     * 添加需要验证码的端点
     */
    @Permission("config:write")
    @PostMapping(value = "/endpoints", version = "1")
    public ResponseEntity<R<Void>> addEndpoint(@RequestBody CaptchaEndpointConfig config) {
        captchaConfigService.updateEndpointConfig(config);
        return R.ok();
    }

    /**
     * 更新端点配置
     */
    @Permission("config:write")
    @PutMapping(value = "/endpoints/{id}", version = "1")
    public ResponseEntity<R<Void>> updateEndpoint(@PathVariable Long id,
                                                   @RequestBody CaptchaEndpointConfig config) {
        config.setId(id);
        captchaConfigService.updateEndpointConfig(config);
        return R.ok();
    }

    /**
     * 删除端点配置
     */
    @Permission("config:write")
    @DeleteMapping(value = "/endpoints/{id}", version = "1")
    public ResponseEntity<R<Void>> deleteEndpoint(@PathVariable Long id) {
        captchaConfigService.deleteEndpointConfig(id);
        return R.ok();
    }
}
