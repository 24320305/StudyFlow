package com.studyflow.backend.common.api;

import java.util.UUID;

public final class RequestIdContext {

    private static final ThreadLocal<String> REQUEST_ID = new ThreadLocal<>();

    private RequestIdContext() {
    }

    public static String current() {
        String requestId = REQUEST_ID.get();
        return requestId == null ? "unknown" : requestId;
    }

    public static void set(String requestId) {
        REQUEST_ID.set(requestId == null || requestId.isBlank() ? UUID.randomUUID().toString() : requestId);
    }

    public static void clear() {
        REQUEST_ID.remove();
    }
}
