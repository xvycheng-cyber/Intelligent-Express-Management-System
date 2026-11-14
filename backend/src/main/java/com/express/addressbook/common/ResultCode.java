package com.express.addressbook.common;

/**
 * 业务码与默认中文提示。HTTP 状态码在 GlobalExceptionHandler 中按 code 区间映射。
 */
public enum ResultCode {

    SUCCESS(0, "OK"),
    VALIDATION_FAILED(40001, "参数校验失败"),
    BAD_REQUEST_BODY(40002, "请求数据格式不正确"),
    LIMIT_EXCEEDED(40003, "请求数据超限"),
    NOT_FOUND(40401, "记录不存在或已被删除"),
    API_NOT_FOUND(40402, "接口不存在"),
    METHOD_NOT_ALLOWED(40501, "请求方法不支持"),
    INTERNAL_ERROR(50001, "服务器内部错误"),
    DATABASE_ERROR(50002, "数据库操作失败，请稍后重试");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int code() {
        return code;
    }

    public String message() {
        return message;
    }
}
