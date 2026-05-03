package org.codelin.oauth.oauth.identity.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codelin.oauth.oauth.identity.domain.model.SmtpConfig;
import org.codelin.oauth.oauth.identity.infrastructure.persistence.SmtpConfigMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 邮件配置服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailConfigService {

    private final SmtpConfigMapper smtpConfigMapper;

    public SmtpConfig getSmtpConfig() {
        List<SmtpConfig> list = smtpConfigMapper.selectAll();
        return list.isEmpty() ? null : list.get(0);
    }

    public SmtpConfig getSmtpConfigMasked() {
        SmtpConfig config = getSmtpConfig();
        if (config != null) {
            config.setPassword("******");
        }
        return config;
    }

    public void updateSmtpConfig(SmtpConfig config) {
        List<SmtpConfig> list = smtpConfigMapper.selectAll();
        if (!list.isEmpty()) {
            SmtpConfig existing = list.get(0);
            if (config.getPassword() == null || config.getPassword().isEmpty()
                    || "******".equals(config.getPassword())) {
                config.setPassword(existing.getPassword());
            }
            config.setId(existing.getId());
            config.setUpdatedAt(LocalDateTime.now());
            smtpConfigMapper.update(config);
        } else {
            config.setUpdatedAt(LocalDateTime.now());
            smtpConfigMapper.insert(config);
        }
        log.info("SMTP config updated");
    }
}
