package org.codelin.oauth.oauth.identity.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 应用启动时自动初始化RSA密钥对（如果不存在）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CryptoInitializer implements ApplicationRunner {

    private final CryptoConfigService cryptoConfigService;

    @Override
    public void run(ApplicationArguments args) {
        String privateKey = cryptoConfigService.getPrivateKey();
        if (privateKey == null || privateKey.isEmpty()) {
            log.info("RSA key pair not found, generating...");
            cryptoConfigService.rotateKeys();
            log.info("RSA key pair generated successfully");
        }
    }
}
