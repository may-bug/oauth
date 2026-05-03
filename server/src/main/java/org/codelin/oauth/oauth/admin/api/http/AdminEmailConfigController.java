package org.codelin.oauth.oauth.admin.api.http;

import lombok.RequiredArgsConstructor;
import org.codelin.oauth.oauth.common.security.Permission;
import org.codelin.oauth.oauth.common.web.R;
import org.codelin.oauth.oauth.identity.application.EmailConfigService;
import org.codelin.oauth.oauth.identity.application.notification.EmailService;
import org.codelin.oauth.oauth.identity.application.notification.EmailTemplateService;
import org.codelin.oauth.oauth.identity.domain.model.EmailTemplate;
import org.codelin.oauth.oauth.identity.domain.model.SmtpConfig;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/config/email")
@RequiredArgsConstructor
public class AdminEmailConfigController {

    private final EmailConfigService emailConfigService;
    private final EmailTemplateService emailTemplateService;
    private final EmailService emailService;

    @Permission("config:read")
    @GetMapping(value = "/smtp", version = "1")
    public ResponseEntity<R<SmtpConfig>> getSmtpConfig() {
        return R.ok(emailConfigService.getSmtpConfigMasked());
    }

    @Permission("config:write")
    @PutMapping(value = "/smtp", version = "1")
    public ResponseEntity<R<Void>> updateSmtpConfig(@RequestBody SmtpConfig config) {
        emailConfigService.updateSmtpConfig(config);
        return R.ok();
    }

    @Permission("config:read")
    @GetMapping(value = "/templates", version = "1")
    public ResponseEntity<R<List<EmailTemplate>>> getTemplates() {
        return R.ok(emailTemplateService.listAll());
    }

    @Permission("config:write")
    @PutMapping(value = "/templates/{id}", version = "1")
    public ResponseEntity<R<Void>> updateTemplate(@PathVariable Long id, @RequestBody EmailTemplate template) {
        emailTemplateService.update(id, template);
        return R.ok();
    }

    @Permission("config:write")
    @PostMapping(value = "/test", version = "1")
    public ResponseEntity<R<Void>> sendTestEmail(@RequestBody Map<String, String> body) {
        String to = body.get("to");
        if (to == null || to.isEmpty()) return R.fail(400, "收件人不能为空");
        try {
            emailService.sendEmail(to, "测试邮件", "<h1>测试邮件</h1><p>这是一封测试邮件。</p>");
            return R.ok();
        } catch (Exception e) {
            return R.serverError("发送失败: " + e.getMessage());
        }
    }
}
