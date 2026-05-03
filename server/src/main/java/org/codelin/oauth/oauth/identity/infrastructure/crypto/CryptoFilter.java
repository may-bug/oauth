package org.codelin.oauth.oauth.identity.infrastructure.crypto;

import org.jspecify.annotations.NonNull;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codelin.oauth.oauth.common.web.R;
import org.codelin.oauth.oauth.identity.application.CryptoConfigService;
import org.codelin.oauth.oauth.identity.application.CryptoService;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * 加密过滤器 - 解密需要加密的请求字段
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 50)
@RequiredArgsConstructor
public class CryptoFilter extends OncePerRequestFilter {

    private final CryptoConfigService cryptoConfigService;
    private final CryptoService cryptoService;
    private final ObjectMapper objectMapper;
    private final MessageSource messageSource;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String method = request.getMethod();
        String uri = request.getRequestURI();

        // 检查是否需要解密
        if (cryptoConfigService.requiresEncryption(method, uri) && "POST".equalsIgnoreCase(method)) {
            List<String> encryptedFields = cryptoConfigService.getEncryptedFields(method, uri);

            if (!encryptedFields.isEmpty()) {
                try {
                    // 直接读取原始请求体
                    byte[] rawBody = request.getInputStream().readAllBytes();

                    if (rawBody.length > 0) {
                        String bodyStr = new String(rawBody, StandardCharsets.UTF_8);
                        log.debug("Crypto filter raw body (first 200 chars): {}",
                                bodyStr.length() > 200 ? bodyStr.substring(0, 200) : bodyStr);

                        Map<String, Object> jsonBody = objectMapper.readValue(bodyStr, Map.class);

                        // 获取私钥
                        String privateKey = cryptoConfigService.getPrivateKey();
                        if (privateKey == null || privateKey.isEmpty()) {
                            log.error("RSA private key not configured, call GET /auth/keys first to generate keys");
                            writeError(response, "error.server");
                            return;
                        }
                        log.debug("Private key length: {}", privateKey.length());

                        // 解密指定字段
                        for (String field : encryptedFields) {
                            Object encryptedValue = jsonBody.get(field);
                            if (encryptedValue instanceof String s && !s.isEmpty()) {
                                log.debug("Decrypting field '{}', encrypted length: {}", field, s.length());
                                try {
                                    String decryptedValue = cryptoService.decrypt(s, privateKey);
                                    jsonBody.put(field, decryptedValue);
                                    log.debug("Field '{}' decrypted successfully", field);
                                } catch (Exception e) {
                                    Throwable cause = e;
                                    while (cause.getCause() != null) {
                                        cause = cause.getCause();
                                    }
                                    log.warn("Failed to decrypt field '{}': {}", field, cause.toString());
                                    writeError(response, "error.bad-request");
                                    return;
                                }
                            } else {
                                log.debug("Field '{}' not found or empty in request body", field);
                            }
                        }

                        // 创建解密后的请求包装器，后续控制器读到的是解密后内容
                        byte[] decryptedBody = objectMapper.writeValueAsBytes(jsonBody);
                        DecryptedRequestWrapper decryptedRequest = new DecryptedRequestWrapper(
                                request, decryptedBody);
                        filterChain.doFilter(decryptedRequest, response);
                        return;
                    }
                } catch (Exception e) {
                    log.error("Crypto filter error", e);
                    writeError(response, "error.server");
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 写入错误响应
     */
    private void writeError(HttpServletResponse response, String messageKey) throws IOException {
        String message = messageSource.getMessage(
                messageKey, null, messageKey, LocaleContextHolder.getLocale());
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(
                R.error(400, message)
        ));
    }
}
