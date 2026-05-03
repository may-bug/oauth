package org.codelin.oauth.oauth.identity.infrastructure.captcha;

import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codelin.oauth.oauth.common.web.R;
import org.codelin.oauth.oauth.identity.application.CaptchaConfigService;
import org.codelin.oauth.oauth.identity.application.CaptchaValidator;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 验证码过滤器 - 验证需要验证码的端点
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 100)
@RequiredArgsConstructor
public class CaptchaFilter extends OncePerRequestFilter {

    private static final String CAPTCHA_TOKEN_HEADER = "X-Captcha-Token";
    private static final String CAPTCHA_CODE_HEADER = "X-Captcha-Code";

    private final CaptchaConfigService captchaConfigService;
    private final CaptchaValidator captchaValidator;
    private final ObjectMapper objectMapper;
    private final MessageSource messageSource;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String method = request.getMethod();
        String uri = request.getRequestURI();

        // 检查是否需要验证码
        if (captchaConfigService.requiresCaptcha(method, uri)) {
            String captchaToken = request.getHeader(CAPTCHA_TOKEN_HEADER);
            String captchaCode = request.getHeader(CAPTCHA_CODE_HEADER);

            // 从查询参数获取
            if (isEmpty(captchaToken)) {
                captchaToken = request.getParameter("captchaToken");
            }
            if (isEmpty(captchaCode)) {
                captchaCode = request.getParameter("captchaCode");
            }

            // 从JSON body获取
            if (isEmpty(captchaToken) || isEmpty(captchaCode)) {
                ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request, 8192);
                byte[] body = wrappedRequest.getContentAsByteArray();
                if (body.length > 0) {
                    try {
                        Map jsonBody = objectMapper.readValue(
                                new String(body, StandardCharsets.UTF_8), Map.class);
                        if (isEmpty(captchaToken)) {
                            Object val = jsonBody.get("captchaToken");
                            if (val != null) {
                                captchaToken = val.toString();
                            }
                        }
                        if (isEmpty(captchaCode)) {
                            Object val = jsonBody.get("captcha");
                            if (val != null) {
                                captchaCode = val.toString();
                            }
                        }
                    } catch (Exception e) {
                        log.debug("Failed to parse request body for captcha: {}", e.getMessage());
                    }
                }
                // 使用包装后的请求继续
                request = wrappedRequest;
            }

            try {
                captchaValidator.validate(captchaToken, captchaCode);
            } catch (Exception e) {
                log.warn("Captcha validation failed for {} {}: {}", method, uri, e.getMessage());
                writeError(response, e.getMessage());
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isEmpty(String s) {
        return s == null || s.isEmpty();
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
