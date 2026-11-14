package com.express.addressbook.common;

/**
 * 可预期的业务异常：由 Service/Controller 抛出，由全局异常处理器转换为统一响应。
 */
public class BusinessException extends RuntimeException {

    private final int code;
    private final Object data;

    public BusinessException(int code, String message) {
        this(code, message, null);
    }

    public BusinessException(int code, String message, Object data) {
        super(message);
        this.code = code;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public Object getData() {
        return data;
    }
}
