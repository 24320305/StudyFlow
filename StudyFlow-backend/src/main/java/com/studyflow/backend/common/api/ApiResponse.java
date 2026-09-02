package com.studyflow.backend.common.api;

public record ApiResponse<T>(String code, String message, T data, String requestId) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("OK", "success", data, RequestIdContext.current());
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(code, message, null, RequestIdContext.current());
    }
}
