package com.express.addressbook.common;

/**
 * 统一响应结构：{code, message, data}。
 *
 * @param <T> 业务数据类型
 */
public record R<T>(int code, String message, T data) {

    public static <T> R<T> ok(T data) {
        return new R<>(ResultCode.SUCCESS.code(), ResultCode.SUCCESS.message(), data);
    }

    public static R<Void> ok() {
        return new R<>(ResultCode.SUCCESS.code(), ResultCode.SUCCESS.message(), null);
    }

    public static <T> R<T> fail(int code, String message) {
        return new R<>(code, message, null);
    }

    public static <T> R<T> fail(int code, String message, T data) {
        return new R<>(code, message, data);
    }
}
