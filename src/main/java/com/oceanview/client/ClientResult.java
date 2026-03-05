package com.oceanview.client;

public record ClientResult<T>(boolean success, String message, T data) {
    public static <T> ClientResult<T> ok(String message, T data) {
        return new ClientResult<>(true, message, data);
    }

    public static <T> ClientResult<T> fail(String message) {
        return new ClientResult<>(false, message, null);
    }
}
