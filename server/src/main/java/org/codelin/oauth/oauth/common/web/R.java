package org.codelin.oauth.oauth.common.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * 统一响应体
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class R<T> {

    private int code;
    private String message;
    private T data;

    public R() {
    }

    public R(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // ========== 成功响应 ==========

    public static <T> ResponseEntity<R<T>> ok(T data) {
        return ResponseEntity.ok(new R<>(0, "success", data));
    }

    public static ResponseEntity<R<Void>> ok() {
        return ResponseEntity.ok(new R<>(0, "success", null));
    }

    public static <T> ResponseEntity<R<T>> ok(T data, String message) {
        return ResponseEntity.ok(new R<>(0, message, data));
    }

    // ========== 分页响应 ==========

    public static <T> ResponseEntity<R<PageResult<T>>> okPage(List<T> records,
                                                                long total,
                                                                int page,
                                                                int size) {
        PageResult<T> pageResult = new PageResult<>(records, total, page, size);
        return ResponseEntity.ok(new R<>(0, "success", pageResult));
    }

    // ========== 错误响应（构造R对象，供 GlobalExceptionHandler 和 Filter 内部使用）==========

    public static <T> R<T> error(int code, String message) {
        return new R<>(code, message, null);
    }

    public static <T> ResponseEntity<R<T>> fail(int code, String message) {
        return ResponseEntity.badRequest().body(new R<>(code, message, null));
    }

    public static <T> ResponseEntity<R<T>> fail(int code, String message, T data) {
        return ResponseEntity.badRequest().body(new R<>(code, message, data));
    }

    public static <T> ResponseEntity<R<T>> unauthorized(String message) {
        return ResponseEntity.status(401).body(new R<>(401, message, null));
    }

    public static <T> ResponseEntity<R<T>> forbidden(String message) {
        return ResponseEntity.status(403).body(new R<>(403, message, null));
    }

    public static <T> ResponseEntity<R<T>> notFound(String message) {
        return ResponseEntity.status(404).body(new R<>(404, message, null));
    }

    public static <T> ResponseEntity<R<T>> tooManyRequests(String message) {
        return ResponseEntity.status(429).body(new R<>(429, message, null));
    }

    public static <T> ResponseEntity<R<T>> serverError(String message) {
        return ResponseEntity.status(500).body(new R<>(500, message, null));
    }

    /**
     * 分页结果
     */
    @Data
    public static class PageResult<T> {
        private List<T> records;
        private long total;
        private int page;
        private int size;

        public PageResult(List<T> records, long total, int page, int size) {
            this.records = records;
            this.total = total;
            this.page = page;
            this.size = size;
        }
    }
}
