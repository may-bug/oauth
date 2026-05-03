package org.codelin.oauth.oauth.common.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * 业务异常 - code 与 HTTP 状态码一致
     */
    @ExceptionHandler(BizException.class)
    public ResponseEntity<R<Void>> handleBiz(BizException e) {
        log.warn("Business exception: code={}, message={}", e.getCode(), e.getMessage());

        String message;
        if (e.getMessageKey() != null) {
            message = messageSource.getMessage(
                    e.getMessageKey(),
                    e.getArgs(),
                    e.getMessage(),
                    LocaleContextHolder.getLocale()
            );
        } else {
            message = e.getMessage();
        }

        return ResponseEntity.status(e.getCode()).body(R.error(e.getCode(), message));
    }

    /**
     * 参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<R<Void>> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        log.warn("Validation error: {}", message);
        return R.fail(400, message);
    }

    /**
     * 404 - 静默处理，不打ERROR日志
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<R<Void>> handleNotFound(NoResourceFoundException e) {
        String message = messageSource.getMessage(
                "error.not-found", null, "Not Found", LocaleContextHolder.getLocale());
        return R.notFound(message);
    }

    /**
     * 未知异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<R<Void>> handleException(Exception e) {
        log.error("Unexpected error", e);

        String message = messageSource.getMessage(
                "error.server",
                null,
                "Server internal error",
                LocaleContextHolder.getLocale()
        );

        return R.serverError(message);
    }
}
